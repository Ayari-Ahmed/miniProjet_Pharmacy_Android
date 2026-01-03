const express = require('express');
const jwt = require('jsonwebtoken');
const { body, validationResult } = require('express-validator');
const Pharmacy = require('../models/Pharmacy');
const { protect } = require('../middleware/auth');

const router = express.Router();

// Generate JWT Token
const generateToken = (id) => {
  return jwt.sign({ id, type: 'pharmacy' }, process.env.JWT_SECRET || 'your-secret-key', {
    expiresIn: '30d'
  });
};

// @route   POST /api/pharmacies/auth/register
// @desc    Register pharmacy
// @access  Public
router.post('/register', [
  body('name').trim().isLength({ min: 2, max: 100 }).withMessage('Name must be 2-100 characters'),
  body('email').isEmail().normalizeEmail().withMessage('Please provide a valid email'),
  body('password').isLength({ min: 6 }).withMessage('Password must be at least 6 characters'),
  body('phone').trim().isLength({ min: 8 }).withMessage('Phone number is required'),
  body('address').trim().isLength({ min: 5 }).withMessage('Address is required'),
  body('latitude').isFloat().withMessage('Latitude is required'),
  body('longitude').isFloat().withMessage('Longitude is required')
], async (req, res) => {
  try {
    // Check for validation errors
    const errors = validationResult(req);
    if (!errors.isEmpty()) {
      return res.status(400).json({
        success: false,
        message: 'Validation failed',
        errors: errors.array()
      });
    }

    const { name, email, password, phone, address, latitude, longitude, licenseNumber, pharmacistName } = req.body;

    // Check if pharmacy exists
    const existingPharmacy = await Pharmacy.findOne({ email });
    if (existingPharmacy) {
      return res.status(400).json({
        success: false,
        message: 'Pharmacy already exists with this email'
      });
    }

    // Create pharmacy
    const pharmacy = await Pharmacy.create({
      name,
      email,
      password,
      phone,
      address,
      latitude,
      longitude,
      licenseNumber,
      pharmacistName
    });

    // Populate medicine data (though new pharmacy won't have stock yet)
    await pharmacy.populate('stock.medicine', 'name genericName category requiresPrescription price');

    // Generate token
    const token = generateToken(pharmacy._id);

    res.status(201).json({
      success: true,
      message: 'Pharmacy registered successfully',
      data: {
        pharmacy,
        token
      }
    });
  } catch (error) {
    console.error('Pharmacy register error:', error);
    res.status(500).json({
      success: false,
      message: 'Server error during registration'
    });
  }
});

// @route   POST /api/pharmacies/auth/login
// @desc    Login pharmacy
// @access  Public
router.post('/login', [
  body('email').isEmail().normalizeEmail().withMessage('Please provide a valid email'),
  body('password').exists().withMessage('Password is required')
], async (req, res) => {
  try {
    // Check for validation errors
    const errors = validationResult(req);
    if (!errors.isEmpty()) {
      return res.status(400).json({
        success: false,
        message: 'Validation failed',
        errors: errors.array()
      });
    }

    const { email, password } = req.body;

    // Check for pharmacy
    const pharmacy = await Pharmacy.findOne({ email }).select('+password');
    if (!pharmacy) {
      return res.status(401).json({
        success: false,
        message: 'Invalid credentials'
      });
    }

    // Check if pharmacy is active
    if (!pharmacy.isActive) {
      return res.status(401).json({
        success: false,
        message: 'Account is deactivated'
      });
    }

    // Check password
    const isMatch = await pharmacy.comparePassword(password);
    if (!isMatch) {
      return res.status(401).json({
        success: false,
        message: 'Invalid credentials'
      });
    }

    // Populate medicine data
    await pharmacy.populate('stock.medicine', 'name genericName category requiresPrescription price');

    // Generate token
    const token = generateToken(pharmacy._id);

    res.json({
      success: true,
      message: 'Login successful',
      data: {
        pharmacy,
        token
      }
    });
  } catch (error) {
    console.error('Pharmacy login error:', error);
    res.status(500).json({
      success: false,
      message: 'Server error during login'
    });
  }
});

// @route   GET /api/pharmacies/auth/me
// @desc    Get current pharmacy
// @access  Private
router.get('/me', protect, async (req, res) => {
  try {
    // Check if it's a pharmacy token
    if (req.user.type !== 'pharmacy') {
      return res.status(403).json({
        success: false,
        message: 'Not authorized'
      });
    }

    const pharmacy = await Pharmacy.findById(req.user._id).populate('stock.medicine', 'name genericName category requiresPrescription price');

    res.json({
      success: true,
      data: {
        pharmacy: pharmacy
      }
    });
  } catch (error) {
    console.error('Get pharmacy me error:', error);
    res.status(500).json({
      success: false,
      message: 'Server error'
    });
  }
});

// @route   PUT /api/pharmacies/auth/update-profile
// @desc    Update pharmacy profile
// @access  Private
router.put('/update-profile', [
  protect,
  body('name').optional().trim().isLength({ min: 2, max: 100 }).withMessage('Name must be 2-100 characters'),
  body('phone').optional().trim(),
  body('address').optional().trim(),
  body('latitude').optional().isFloat(),
  body('longitude').optional().isFloat()
], async (req, res) => {
  try {
    // Check for validation errors
    const errors = validationResult(req);
    if (!errors.isEmpty()) {
      return res.status(400).json({
        success: false,
        message: 'Validation failed',
        errors: errors.array()
      });
    }

    // Check if it's a pharmacy token
    if (req.user.type !== 'pharmacy') {
      return res.status(403).json({
        success: false,
        message: 'Not authorized'
      });
    }

    const { name, phone, address, latitude, longitude } = req.body;

    const updateData = {};
    if (name) updateData.name = name;
    if (phone) updateData.phone = phone;
    if (address) updateData.address = address;
    if (latitude !== undefined) updateData.latitude = latitude;
    if (longitude !== undefined) updateData.longitude = longitude;

    const pharmacy = await Pharmacy.findByIdAndUpdate(
      req.user._id,
      updateData,
      { new: true, runValidators: true }
    ).populate('stock.medicine', 'name genericName category requiresPrescription price');

    res.json({
      success: true,
      message: 'Profile updated successfully',
      data: {
        pharmacy: pharmacy
      }
    });
  } catch (error) {
    console.error('Update pharmacy profile error:', error);
    res.status(500).json({
      success: false,
      message: 'Server error during profile update'
    });
  }
});

module.exports = router;