const mongoose = require('mongoose');

const medicineSchema = new mongoose.Schema({
  name: {
    type: String,
    required: [true, 'Medicine name is required'],
    trim: true,
    maxlength: [100, 'Medicine name cannot be more than 100 characters']
  },
  genericName: {
    type: String,
    trim: true
  },
  description: {
    type: String,
    trim: true,
    maxlength: [500, 'Description cannot be more than 500 characters']
  },
  category: {
    type: String,
    required: [true, 'Category is required'],
    enum: ['Pain Relief', 'Antibiotics', 'Cardiovascular', 'Diabetes', 'Respiratory', 'Digestive', 'Skin Care', 'Vitamins', 'Other'],
    default: 'Other'
  },
  dosage: {
    type: String,
    trim: true
  },
  strength: {
    type: String,
    trim: true
  },
  manufacturer: {
    type: String,
    trim: true
  },
  requiresPrescription: {
    type: Boolean,
    default: false
  },
  price: {
    type: Number,
    required: [true, 'Price is required'],
    min: [0, 'Price cannot be negative']
  },
  imageUrl: {
    type: String,
    trim: true
  },
  isActive: {
    type: Boolean,
    default: true
  },
  sideEffects: [{
    type: String,
    trim: true
  }],
  usage: {
    type: String,
    trim: true
  }
}, {
  timestamps: true
});

// Indexes for better query performance
medicineSchema.index({ name: 1 });
medicineSchema.index({ category: 1 });
medicineSchema.index({ requiresPrescription: 1 });
medicineSchema.index({ isActive: 1 });

// Virtual for formatted price
medicineSchema.virtual('formattedPrice').get(function() {
  return this.price ? `TND ${this.price.toFixed(2)}` : 'Price not available';
});

// Ensure virtual fields are serialized
medicineSchema.set('toJSON', { virtuals: true });

module.exports = mongoose.model('Medicine', medicineSchema);