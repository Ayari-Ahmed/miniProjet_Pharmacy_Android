const mongoose = require('mongoose');

const pharmacyStockSchema = new mongoose.Schema({
  medicine: {
    type: mongoose.Schema.Types.ObjectId,
    ref: 'Medicine',
    required: true
  },
  stock: {
    type: Number,
    required: true,
    min: [0, 'Stock cannot be negative'],
    default: 0
  },
  price: {
    type: Number,
    required: true,
    min: [0, 'Price cannot be negative']
  },
  lastUpdated: {
    type: Date,
    default: Date.now
  }
});

const pharmacySchema = new mongoose.Schema({
   name: {
     type: String,
     required: [true, 'Pharmacy name is required'],
     trim: true,
     maxlength: [100, 'Pharmacy name cannot be more than 100 characters']
   },
   address: {
     type: String,
     required: [true, 'Address is required'],
     trim: true
   },
   phone: {
     type: String,
     required: [true, 'Phone number is required'],
     trim: true
   },
   email: {
     type: String,
     required: [true, 'Email is required'],
     unique: true,
     lowercase: true,
     validate: {
       validator: function(email) {
         return /^\w+([.-]?\w+)*@\w+([.-]?\w+)*(\.\w{2,3})+$/.test(email);
       },
       message: 'Please enter a valid email'
     }
   },
   password: {
     type: String,
     required: [true, 'Password is required'],
     minlength: [6, 'Password must be at least 6 characters'],
     select: false // Don't include password in queries by default
   },
  latitude: {
    type: Number,
    required: [true, 'Latitude is required']
  },
  longitude: {
    type: Number,
    required: [true, 'Longitude is required']
  },
  operatingHours: {
    monday: { open: String, close: String },
    tuesday: { open: String, close: String },
    wednesday: { open: String, close: String },
    thursday: { open: String, close: String },
    friday: { open: String, close: String },
    saturday: { open: String, close: String },
    sunday: { open: String, close: String }
  },
  services: [{
    type: String,
    enum: ['24/7', 'Home Delivery', 'Prescription', 'Consultation', 'Emergency']
  }],
  rating: {
    type: Number,
    min: 0,
    max: 5,
    default: 0
  },
  totalReviews: {
    type: Number,
    default: 0
  },
  licenseNumber: {
    type: String,
    trim: true
  },
  pharmacistName: {
    type: String,
    trim: true
  },
  stock: [pharmacyStockSchema],
  isActive: {
    type: Boolean,
    default: true
  },
  imageUrl: {
    type: String,
    trim: true
  }
}, {
  timestamps: true
});

// Index for better query performance
pharmacySchema.index({ email: 1 });

// Hash password before saving
pharmacySchema.pre('save', async function(next) {
  if (!this.isModified('password')) return next();

  try {
    const bcrypt = require('bcryptjs');
    const salt = await bcrypt.genSalt(12);
    this.password = await bcrypt.hash(this.password, salt);
    next();
  } catch (error) {
    next(error);
  }
});

// Compare password method
pharmacySchema.methods.comparePassword = async function(candidatePassword) {
  return await require('bcryptjs').compare(candidatePassword, this.password);
};

// Remove password from JSON output
pharmacySchema.methods.toJSON = function() {
  const pharmacyObject = this.toObject();
  delete pharmacyObject.password;
  return pharmacyObject;
};

// Indexes for better query performance
pharmacySchema.index({ location: '2dsphere' });
pharmacySchema.index({ name: 1 });
pharmacySchema.index({ isActive: 1 });
pharmacySchema.index({ 'stock.medicine': 1 });

// Virtual for total medicines count
pharmacySchema.virtual('totalMedicines').get(function() {
  return this.stock ? this.stock.length : 0;
});

// Virtual for available medicines count (stock > 0)
pharmacySchema.virtual('availableMedicines').get(function() {
  return this.stock ? this.stock.filter(item => item.stock > 0).length : 0;
});

// Method to check if medicine is available
pharmacySchema.methods.hasMedicine = function(medicineId, quantity = 1) {
  const stockItem = this.stock.find(item =>
    item.medicine.toString() === medicineId.toString()
  );
  return stockItem && stockItem.stock >= quantity;
};

// Method to get medicine stock
pharmacySchema.methods.getMedicineStock = function(medicineId) {
  const stockItem = this.stock.find(item =>
    item.medicine.toString() === medicineId.toString()
  );
  return stockItem ? stockItem.stock : 0;
};

// Method to update medicine stock
pharmacySchema.methods.updateStock = function(medicineId, newStock, newPrice = null) {
  const stockItem = this.stock.find(item =>
    item.medicine.toString() === medicineId.toString()
  );

  if (stockItem) {
    stockItem.stock = newStock;
    if (newPrice !== null) {
      stockItem.price = newPrice;
    }
    stockItem.lastUpdated = new Date();
  } else {
    this.stock.push({
      medicine: medicineId,
      stock: newStock,
      price: newPrice || 0
    });
  }
};

// Ensure virtual fields are serialized
pharmacySchema.set('toJSON', { virtuals: true });

module.exports = mongoose.model('Pharmacy', pharmacySchema);