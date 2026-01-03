const jwt = require('jsonwebtoken');
const User = require('../models/User');
const Pharmacy = require('../models/Pharmacy');

// Protect routes - require authentication
const protect = async (req, res, next) => {
  try {
    let token;

    // Check for token in header
    if (req.headers.authorization && req.headers.authorization.startsWith('Bearer')) {
      token = req.headers.authorization.split(' ')[1];
    }

    if (!token) {
      return res.status(401).json({
        success: false,
        message: 'Not authorized to access this route'
      });
    }

    try {
      // Verify token
      const decoded = jwt.verify(token, process.env.JWT_SECRET || 'your-secret-key');

      let entity;
      if (decoded.type === 'pharmacy') {
        // Get pharmacy from token
        entity = await Pharmacy.findById(decoded.id).select('-password');
        if (!entity) {
          return res.status(401).json({
            success: false,
            message: 'No pharmacy found with this token'
          });
        }
        if (!entity.isActive) {
          return res.status(401).json({
            success: false,
            message: 'Pharmacy account is deactivated'
          });
        }
        req.user = { ...entity.toObject(), type: 'pharmacy' };
      } else {
        // Get user from token (default behavior)
        entity = await User.findById(decoded.id).select('-password');
        if (!entity) {
          return res.status(401).json({
            success: false,
            message: 'No user found with this token'
          });
        }
        if (!entity.isActive) {
          return res.status(401).json({
            success: false,
            message: 'User account is deactivated'
          });
        }
        req.user = entity;
      }

      next();
    } catch (error) {
      return res.status(401).json({
        success: false,
        message: 'Not authorized to access this route'
      });
    }
  } catch (error) {
    return res.status(500).json({
      success: false,
      message: 'Server error'
    });
  }
};

// Grant access to specific roles
const authorize = (...roles) => {
  return (req, res, next) => {
    if (!roles.includes(req.user.role)) {
      return res.status(403).json({
        success: false,
        message: `User role ${req.user.role} is not authorized to access this route`
      });
    }
    next();
  };
};

// Check if user owns resource or is admin
const ownerOrAdmin = (req, res, next) => {
  if (req.user.role === 'admin' || req.user._id.toString() === req.params.id) {
    next();
  } else {
    return res.status(403).json({
      success: false,
      message: 'Not authorized to access this resource'
    });
  }
};

module.exports = {
  protect,
  authorize,
  ownerOrAdmin
};