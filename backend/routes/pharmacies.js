const express = require('express');
const { body, validationResult, query } = require('express-validator');
const Pharmacy = require('../models/Pharmacy');
const Medicine = require('../models/Medicine');
const { protect, authorize } = require('../middleware/auth');

const router = express.Router();

// @route   GET /api/pharmacies
// @desc    Get all pharmacies with optional filters
// @access  Public
router.get('/', [
  query('search').optional().trim(),
  query('latitude').optional().isFloat(),
  query('longitude').optional().isFloat(),
  query('radius').optional().isFloat({ min: 0 }),
  query('medicine').optional().isMongoId(),
  query('limit').optional().isInt({ min: 1, max: 100 }),
  query('page').optional().isInt({ min: 1 })
], async (req, res) => {
  try {
    const errors = validationResult(req);
    if (!errors.isEmpty()) {
      return res.status(400).json({
        success: false,
        message: 'Validation failed',
        errors: errors.array()
      });
    }

    const {
      search,
      latitude,
      longitude,
      radius = 10, // Default 10km radius
      medicine,
      limit = 20,
      page = 1
    } = req.query;

    let query = { isActive: true };

    // Text search
    if (search) {
      query.$or = [
        { name: { $regex: search, $options: 'i' } },
        { address: { $regex: search, $options: 'i' } }
      ];
    }

    // Location-based search
    if (latitude && longitude) {
      query.location = {
        $near: {
          $geometry: {
            type: 'Point',
            coordinates: [parseFloat(longitude), parseFloat(latitude)]
          },
          $maxDistance: parseFloat(radius) * 1000 // Convert km to meters
        }
      };
    }

    // Medicine availability filter
    if (medicine) {
      query.stock = {
        $elemMatch: {
          medicine: medicine,
          stock: { $gt: 0 }
        }
      };
    }

    const skip = (parseInt(page) - 1) * parseInt(limit);

    const pharmacies = await Pharmacy.find(query)
      .populate('stock.medicine', 'name genericName category requiresPrescription')
      .select('-__v')
      .sort({ rating: -1, totalReviews: -1 })
      .limit(parseInt(limit))
      .skip(skip);

    const total = await Pharmacy.countDocuments(query);

    res.json({
      success: true,
      data: pharmacies,
      pagination: {
        page: parseInt(page),
        limit: parseInt(limit),
        total,
        pages: Math.ceil(total / parseInt(limit))
      }
    });
  } catch (error) {
    console.error('Get pharmacies error:', error);
    res.status(500).json({
      success: false,
      message: 'Server error'
    });
  }
});

// @route   GET /api/pharmacies/medicines
// @desc    Get all medicines for pharmacy stock management
// @access  Private (Pharmacy)
router.get('/medicines', protect, async (req, res) => {
  try {
    const medicines = await Medicine.find({ isActive: true })
      .select('name genericName category requiresPrescription price')
      .sort({ name: 1 });

    res.json({
      success: true,
      data: medicines
    });
  } catch (error) {
    console.error('Get medicines error:', error);
    res.status(500).json({
      success: false,
      message: 'Server error'
    });
  }
});

// @route   GET /api/pharmacies/search/medicines
// @desc    Search medicines across pharmacies
// @access  Public
router.get('/search/medicines', [
  query('q').trim().isLength({ min: 1 }).withMessage('Search query required'),
  query('latitude').optional().isFloat(),
  query('longitude').optional().isFloat(),
  query('radius').optional().isFloat({ min: 0 }),
  query('limit').optional().isInt({ min: 1, max: 50 })
], async (req, res) => {
  try {
    const errors = validationResult(req);
    if (!errors.isEmpty()) {
      return res.status(400).json({
        success: false,
        message: 'Validation failed',
        errors: errors.array()
      });
    }

    const {
      q,
      latitude,
      longitude,
      radius = 10,
      limit = 20
    } = req.query;

    let locationFilter = {};
    if (latitude && longitude) {
      locationFilter = {
        location: {
          $near: {
            $geometry: {
              type: 'Point',
              coordinates: [parseFloat(longitude), parseFloat(latitude)]
            },
            $maxDistance: parseFloat(radius) * 1000
          }
        }
      };
    }

    const pharmacies = await Pharmacy.find({
      ...locationFilter,
      isActive: true,
      stock: {
        $elemMatch: {
          stock: { $gt: 0 },
          medicine: {
            $in: await Medicine.find({
              $or: [
                { name: { $regex: q, $options: 'i' } },
                { genericName: { $regex: q, $options: 'i' } }
              ],
              isActive: true
            }).distinct('_id')
          }
        }
      }
    })
    .populate('stock.medicine', 'name genericName category requiresPrescription price')
    .select('name address phone latitude longitude stock')
    .limit(parseInt(limit));

    // Filter and format results
    const results = [];
    pharmacies.forEach(pharmacy => {
      pharmacy.stock.forEach(stockItem => {
        if (stockItem.stock > 0 &&
            (stockItem.medicine.name.toLowerCase().includes(q.toLowerCase()) ||
             (stockItem.medicine.genericName && stockItem.medicine.genericName.toLowerCase().includes(q.toLowerCase())))) {
          results.push({
            pharmacy: {
              _id: pharmacy._id,
              name: pharmacy.name,
              address: pharmacy.address,
              phone: pharmacy.phone,
              latitude: pharmacy.latitude,
              longitude: pharmacy.longitude
            },
            medicine: stockItem.medicine,
            stock: stockItem.stock,
            price: stockItem.price
          });
        }
      });
    });

    res.json({
      success: true,
      data: results.slice(0, limit)
    });
  } catch (error) {
    console.error('Search medicines error:', error);
    res.status(500).json({
      success: false,
      message: 'Server error'
    });
  }
});

// @route   GET /api/pharmacies/:id
// @desc    Get pharmacy by ID
// @access  Public
router.get('/:id', async (req, res) => {
  try {
    const pharmacy = await Pharmacy.findById(req.params.id)
      .populate('stock.medicine', 'name genericName description category requiresPrescription price imageUrl')
      .select('-__v');

    if (!pharmacy) {
      return res.status(404).json({
        success: false,
        message: 'Pharmacy not found'
      });
    }

    res.json({
      success: true,
      data: pharmacy
    });
  } catch (error) {
    console.error('Get pharmacy error:', error);
    if (error.kind === 'ObjectId') {
      return res.status(404).json({
        success: false,
        message: 'Pharmacy not found'
      });
    }
    res.status(500).json({
      success: false,
      message: 'Server error'
    });
  }
});

// @route   GET /api/pharmacies/:id/stock
// @desc    Get pharmacy stock for specific medicine
// @access  Public
router.get('/:id/stock', [
  query('medicine').isMongoId().withMessage('Valid medicine ID required')
], async (req, res) => {
  try {
    const errors = validationResult(req);
    if (!errors.isEmpty()) {
      return res.status(400).json({
        success: false,
        message: 'Validation failed',
        errors: errors.array()
      });
    }

    const { medicine } = req.query;

    const pharmacy = await Pharmacy.findById(req.params.id)
      .select('stock name')
      .populate('stock.medicine', 'name price requiresPrescription');

    if (!pharmacy) {
      return res.status(404).json({
        success: false,
        message: 'Pharmacy not found'
      });
    }

    const stockItem = pharmacy.stock.find(item =>
      item.medicine._id.toString() === medicine
    );

    if (!stockItem) {
      return res.json({
        success: true,
        data: {
          pharmacy: pharmacy.name,
          medicine: null,
          available: false,
          stock: 0,
          price: 0
        }
      });
    }

    res.json({
      success: true,
      data: {
        pharmacy: pharmacy.name,
        medicine: stockItem.medicine,
        available: stockItem.stock > 0,
        stock: stockItem.stock,
        price: stockItem.price
      }
    });
  } catch (error) {
    console.error('Get pharmacy stock error:', error);
    res.status(500).json({
      success: false,
      message: 'Server error'
    });
  }
});

// @route   POST /api/pharmacies
// @desc    Create new pharmacy
// @access  Private (Admin only)
router.post('/', [
  protect,
  authorize('admin'),
  body('name').trim().isLength({ min: 2, max: 100 }).withMessage('Name must be 2-100 characters'),
  body('address').trim().isLength({ min: 5 }).withMessage('Address is required'),
  body('phone').trim().isLength({ min: 8 }).withMessage('Valid phone number required'),
  body('latitude').isFloat({ min: -90, max: 90 }).withMessage('Valid latitude required'),
  body('longitude').isFloat({ min: -180, max: 180 }).withMessage('Valid longitude required')
], async (req, res) => {
  try {
    const errors = validationResult(req);
    if (!errors.isEmpty()) {
      return res.status(400).json({
        success: false,
        message: 'Validation failed',
        errors: errors.array()
      });
    }

    const pharmacyData = { ...req.body };
    pharmacyData.location = {
      type: 'Point',
      coordinates: [pharmacyData.longitude, pharmacyData.latitude]
    };

    const pharmacy = await Pharmacy.create(pharmacyData);

    res.status(201).json({
      success: true,
      message: 'Pharmacy created successfully',
      data: pharmacy
    });
  } catch (error) {
    console.error('Create pharmacy error:', error);
    if (error.code === 11000) {
      return res.status(400).json({
        success: false,
        message: 'Pharmacy with this information already exists'
      });
    }
    res.status(500).json({
      success: false,
      message: 'Server error'
    });
  }
});

// @route   PUT /api/pharmacies/:id/stock
// @desc    Update pharmacy stock
// @access  Private (Admin/Pharmacy owner)
router.put('/:id/stock', [
  protect,
  body('medicine').isMongoId().withMessage('Valid medicine ID required'),
  body('stock').isInt({ min: 0 }).withMessage('Stock must be non-negative'),
  body('price').optional().isFloat({ min: 0 }).withMessage('Price must be non-negative')
], async (req, res) => {
  try {
    const errors = validationResult(req);
    if (!errors.isEmpty()) {
      return res.status(400).json({
        success: false,
        message: 'Validation failed',
        errors: errors.array()
      });
    }

    const { medicine, stock, price } = req.body;

    const pharmacy = await Pharmacy.findById(req.params.id);

    if (!pharmacy) {
      return res.status(404).json({
        success: false,
        message: 'Pharmacy not found'
      });
    }

    // Check if user is admin or pharmacy owner
    const isAdmin = req.user.role === 'admin';
    const isPharmacyOwner = req.user.type === 'pharmacy' && req.user._id.toString() === pharmacy._id.toString();

    if (!isAdmin && !isPharmacyOwner) {
      return res.status(403).json({
        success: false,
        message: 'Not authorized to update this pharmacy stock'
      });
    }

    pharmacy.updateStock(medicine, stock, price);
    await pharmacy.save();

    // Populate medicine data in stock items
    await pharmacy.populate('stock.medicine', 'name genericName');

    res.json({
      success: true,
      message: 'Stock updated successfully',
      data: pharmacy.stock
    });
  } catch (error) {
    console.error('Update stock error:', error);
    res.status(500).json({
      success: false,
      message: 'Server error'
    });
  }
});

module.exports = router;