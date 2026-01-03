const express = require('express');
const { body, validationResult, query } = require('express-validator');
const Order = require('../models/Order');
const Pharmacy = require('../models/Pharmacy');
const User = require('../models/User');
const { protect, authorize } = require('../middleware/auth');

const router = express.Router();

// @route   POST /api/orders
// @desc    Create new order
// @access  Private
router.post('/', [
  protect,
  body('pharmacy').isMongoId().withMessage('Valid pharmacy ID required'),
  body('items').isArray({ min: 1 }).withMessage('At least one item required'),
  body('items.*.medicine').isMongoId().withMessage('Valid medicine ID required'),
  body('items.*.quantity').isInt({ min: 1 }).withMessage('Quantity must be at least 1'),
  body('deliveryAddress').trim().isLength({ min: 5 }).withMessage('Delivery address required'),
  body('paymentMethod').optional().isIn(['cash', 'card', 'online']).withMessage('Invalid payment method')
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
      pharmacy: pharmacyId,
      items,
      deliveryAddress,
      deliveryLatitude,
      deliveryLongitude,
      specialInstructions,
      prescriptionUrl
    } = req.body;

    // Verify pharmacy exists
    const pharmacy = await Pharmacy.findById(pharmacyId);
    if (!pharmacy) {
      return res.status(404).json({
        success: false,
        message: 'Pharmacy not found'
      });
    }

    // Check stock availability and calculate total
    let totalAmount = 0;
    const orderItems = [];

    for (const item of items) {
      const stockItem = pharmacy.stock.find(s =>
        s.medicine.toString() === item.medicine
      );

      if (!stockItem) {
        return res.status(400).json({
          success: false,
          message: `Medicine ${item.medicine} not available at this pharmacy`
        });
      }

      if (stockItem.stock < item.quantity) {
        return res.status(400).json({
          success: false,
          message: `Insufficient stock for medicine. Available: ${stockItem.stock}`
        });
      }

      orderItems.push({
        medicine: item.medicine,
        quantity: item.quantity,
        price: stockItem.price,
        pharmacy: pharmacyId
      });

      totalAmount += stockItem.price * item.quantity;
    }

    // Create order
    const order = await Order.create({
      customer: req.user._id,
      pharmacy: pharmacyId,
      items: orderItems,
      totalAmount,
      deliveryAddress,
      deliveryLatitude,
      deliveryLongitude,
      specialInstructions,
      prescriptionUrl
    });

    // Populate order data
    await order.populate([
      { path: 'pharmacy', select: 'name address phone' },
      { path: 'items.medicine', select: 'name genericName requiresPrescription' }
    ]);

    res.status(201).json({
      success: true,
      message: 'Order created successfully',
      data: order
    });
  } catch (error) {
    console.error('Create order error:', error);
    res.status(500).json({
      success: false,
      message: 'Server error'
    });
  }
});

// @route   GET /api/orders
// @desc    Get user orders
// @access  Private
router.get('/', [
  protect,
  query('status').optional().isIn(['pending', 'confirmed', 'processing', 'ready', 'delivering', 'delivered', 'cancelled']),
  query('limit').optional().isInt({ min: 1, max: 50 }),
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

    const { status, limit = 20, page = 1 } = req.query;
    const skip = (parseInt(page) - 1) * parseInt(limit);

    let query = { customer: req.user._id };

    if (status) {
      query.status = status;
    }

    const orders = await Order.find(query)
      .populate('pharmacy', 'name address phone')
      .populate('driver', 'name phone')
      .populate('items.medicine', 'name genericName')
      .sort({ createdAt: -1 })
      .limit(parseInt(limit))
      .skip(skip)
      .lean(); // Return plain objects instead of Mongoose documents

    const total = await Order.countDocuments(query);

    res.json({
      success: true,
      data: orders,
      pagination: {
        page: parseInt(page),
        limit: parseInt(limit),
        total,
        pages: Math.ceil(total / parseInt(limit))
      }
    });
  } catch (error) {
    console.error('Get orders error:', error);
    res.status(500).json({
      success: false,
      message: 'Server error'
    });
  }
});

// @route   GET /api/orders/:id
// @desc    Get order by ID
// @access  Private
router.get('/:id', protect, async (req, res) => {
  try {
    const order = await Order.findById(req.params.id)
      .populate('customer', 'name email phone')
      .populate('pharmacy', 'name address phone latitude longitude')
      .populate('driver', 'name phone')
      .populate('items.medicine', 'name genericName description category requiresPrescription');

    if (!order) {
      return res.status(404).json({
        success: false,
        message: 'Order not found'
      });
    }

    // Check if user owns this order or is admin
    if (order.customer._id.toString() !== req.user._id.toString() && req.user.role !== 'admin') {
      return res.status(403).json({
        success: false,
        message: 'Not authorized to view this order'
      });
    }

    res.json({
      success: true,
      data: order
    });
  } catch (error) {
    console.error('Get order error:', error);
    if (error.kind === 'ObjectId') {
      return res.status(404).json({
        success: false,
        message: 'Order not found'
      });
    }
    res.status(500).json({
      success: false,
      message: 'Server error'
    });
  }
});

// @route   PUT /api/orders/:id/cancel
// @desc    Cancel order
// @access  Private
router.put('/:id/cancel', protect, async (req, res) => {
  try {
    const order = await Order.findById(req.params.id);

    if (!order) {
      return res.status(404).json({
        success: false,
        message: 'Order not found'
      });
    }

    // Check if user owns this order
    if (order.customer.toString() !== req.user._id.toString()) {
      return res.status(403).json({
        success: false,
        message: 'Not authorized to cancel this order'
      });
    }

    // Check if order can be cancelled
    if (!['pending', 'confirmed'].includes(order.status)) {
      return res.status(400).json({
        success: false,
        message: 'Order cannot be cancelled at this stage'
      });
    }

    order.updateStatus('cancelled', 'Cancelled by customer');
    await order.save();

    res.json({
      success: true,
      message: 'Order cancelled successfully',
      data: order
    });
  } catch (error) {
    console.error('Cancel order error:', error);
    res.status(500).json({
      success: false,
      message: 'Server error'
    });
  }
});

// @route   PUT /api/orders/:id/rate
// @desc    Rate delivered order
// @access  Private
router.put('/:id/rate', [
  protect,
  body('rating').isInt({ min: 1, max: 5 }).withMessage('Rating must be 1-5'),
  body('review').optional().trim().isLength({ max: 500 }).withMessage('Review too long')
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

    const { rating, review } = req.body;

    const order = await Order.findById(req.params.id);

    if (!order) {
      return res.status(404).json({
        success: false,
        message: 'Order not found'
      });
    }

    // Check if user owns this order
    if (order.customer.toString() !== req.user._id.toString()) {
      return res.status(403).json({
        success: false,
        message: 'Not authorized to rate this order'
      });
    }

    // Check if order is delivered
    if (order.status !== 'delivered') {
      return res.status(400).json({
        success: false,
        message: 'Can only rate delivered orders'
      });
    }

    order.rating = rating;
    if (review) order.review = review;
    await order.save();

    res.json({
      success: true,
      message: 'Order rated successfully',
      data: order
    });
  } catch (error) {
    console.error('Rate order error:', error);
    res.status(500).json({
      success: false,
      message: 'Server error'
    });
  }
});

// @route   GET /api/orders/driver/available
// @desc    Get available orders for drivers
// @access  Private (Driver only)
router.get('/driver/available', [
  protect,
  authorize('driver'),
  query('latitude').optional().isFloat(),
  query('longitude').optional().isFloat(),
  query('radius').optional().isFloat({ min: 0 })
], async (req, res) => {
  try {
    const { latitude, longitude, radius = 10 } = req.query;

    let query = {
      status: 'ready',
      driver: { $exists: false }
    };

    // Filter by location if provided
    if (latitude && longitude) {
      query.deliveryLatitude = { $exists: true };
      query.deliveryLongitude = { $exists: true };
      // Note: In production, you'd use geospatial queries here
    }

    const orders = await Order.find(query)
      .populate('customer', 'name phone')
      .populate('pharmacy', 'name address phone latitude longitude')
      .populate('items.medicine', 'name')
      .sort({ createdAt: 1 })
      .limit(20);

    res.json({
      success: true,
      data: orders
    });
  } catch (error) {
    console.error('Get available orders error:', error);
    res.status(500).json({
      success: false,
      message: 'Server error'
    });
  }
});

// @route   PUT /api/orders/:id/assign-driver
// @desc    Assign driver to order
// @access  Private (Driver only)
router.put('/:id/assign-driver', protect, authorize('driver'), async (req, res) => {
  try {
    const order = await Order.findById(req.params.id);

    if (!order) {
      return res.status(404).json({
        success: false,
        message: 'Order not found'
      });
    }

    if (order.status !== 'ready') {
      return res.status(400).json({
        success: false,
        message: 'Order is not ready for delivery'
      });
    }

    if (order.driver) {
      return res.status(400).json({
        success: false,
        message: 'Order already assigned to a driver'
      });
    }

    order.assignDriver(req.user._id);
    await order.save();

    await order.populate([
      { path: 'customer', select: 'name phone' },
      { path: 'pharmacy', select: 'name address phone' }
    ]);

    res.json({
      success: true,
      message: 'Order assigned successfully',
      data: order
    });
  } catch (error) {
    console.error('Assign driver error:', error);
    res.status(500).json({
      success: false,
      message: 'Server error'
    });
  }
});

// @route   PUT /api/orders/:id/update-status
// @desc    Update order status (Driver/Admin)
// @access  Private
router.put('/:id/update-status', [
  protect,
  body('status').isIn(['confirmed', 'processing', 'ready', 'delivering', 'delivered']).withMessage('Invalid status'),
  body('note').optional().trim()
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

    const { status, note } = req.body;

    const order = await Order.findById(req.params.id);

    if (!order) {
      return res.status(404).json({
        success: false,
        message: 'Order not found'
      });
    }

    // Check permissions
    const isDriver = req.user.role === 'driver' && order.driver?.toString() === req.user._id.toString();
    const isAdmin = req.user.role === 'admin';

    if (!isDriver && !isAdmin) {
      return res.status(403).json({
        success: false,
        message: 'Not authorized to update this order'
      });
    }

    order.updateStatus(status, note || `Status updated by ${req.user.role}`);
    await order.save();

    // If order is delivered, update pharmacy stock
    if (status === 'delivered') {
      const pharmacy = await Pharmacy.findById(order.pharmacy);
      if (pharmacy) {
        order.items.forEach(item => {
          const currentStock = pharmacy.getMedicineStock(item.medicine);
          pharmacy.updateStock(item.medicine, Math.max(0, currentStock - item.quantity));
        });
        await pharmacy.save();
      }
    }

    res.json({
      success: true,
      message: 'Order status updated successfully',
      data: order
    });
  } catch (error) {
    console.error('Update order status error:', error);
    res.status(500).json({
      success: false,
      message: 'Server error'
    });
  }
});

// @route   GET /api/orders/pharmacy/my-orders
// @desc    Get orders for pharmacy owner
// @access  Private (Pharmacy owner only)
router.get('/pharmacy/my-orders', [
  protect,
  query('status').optional().isIn(['pending', 'confirmed', 'processing', 'ready', 'delivering', 'delivered', 'cancelled']),
  query('limit').optional().isInt({ min: 1, max: 50 }),
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

    // Check if it's a pharmacy token
    if (req.user.type !== 'pharmacy') {
      return res.status(403).json({
        success: false,
        message: 'Not authorized'
      });
    }

    const { status, limit = 20, page = 1 } = req.query;
    const skip = (parseInt(page) - 1) * parseInt(limit);

    let query = { pharmacy: req.user._id };

    if (status) {
      query.status = status;
    }

    const orders = await Order.find(query)
      .populate('pharmacy', 'name address phone')
      .populate('driver', 'name phone')
      .populate('items.medicine', 'name genericName')
      .sort({ createdAt: -1 })
      .limit(parseInt(limit))
      .skip(skip);

    const total = await Order.countDocuments(query);

    res.json({
      success: true,
      data: orders,
      pagination: {
        page: parseInt(page),
        limit: parseInt(limit),
        total,
        pages: Math.ceil(total / parseInt(limit))
      }
    });
  } catch (error) {
    console.error('Get pharmacy orders error:', error);
    res.status(500).json({
      success: false,
      message: 'Server error'
    });
  }
});

// @route   PUT /api/orders/:id/pharmacy-status
// @desc    Update order status by pharmacy
// @access  Private (Pharmacy owner only)
router.put('/:id/pharmacy-status', [
  protect,
  body('status').isIn(['confirmed', 'processing', 'ready']).withMessage('Invalid status for pharmacy update'),
  body('note').optional().trim()
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

    // Check if it's a pharmacy token
    if (req.user.type !== 'pharmacy') {
      return res.status(403).json({
        success: false,
        message: 'Not authorized'
      });
    }

    const { status, note } = req.body;

    const order = await Order.findById(req.params.id);

    if (!order) {
      return res.status(404).json({
        success: false,
        message: 'Order not found'
      });
    }

    // Check if pharmacy owns this order
    if (order.pharmacy.toString() !== req.user._id.toString()) {
      return res.status(403).json({
        success: false,
        message: 'Not authorized to update this order'
      });
    }

    // Check if status transition is valid for pharmacy
    const validStatuses = ['confirmed', 'processing', 'ready'];
    if (!validStatuses.includes(status)) {
      return res.status(400).json({
        success: false,
        message: 'Invalid status update for pharmacy'
      });
    }

    order.updateStatus(status, note || `Status updated by pharmacy`);
    await order.save();

    await order.populate([
      { path: 'customer', select: 'name phone' },
      { path: 'pharmacy', select: 'name address phone' }
    ]);

    res.json({
      success: true,
      message: 'Order status updated successfully',
      data: order
    });
  } catch (error) {
    console.error('Update pharmacy order status error:', error);
    res.status(500).json({
      success: false,
      message: 'Server error'
    });
  }
});

module.exports = router;