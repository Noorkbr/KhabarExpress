const mongoose = require('mongoose');

const orderItemSchema = new mongoose.Schema({
  menuItem: {
    type: mongoose.Schema.Types.ObjectId,
    ref: 'MenuItem',
    required: true,
  },
  name: String,
  quantity: {
    type: Number,
    required: true,
    min: 1,
  },
  price: Number, // unit price in BDT paisa
  customizations: [{
    name: String,
    option: String,
    price: Number,
  }],
  specialInstructions: String,
});

const orderSchema = new mongoose.Schema({
  orderNumber: {
    type: String,
    unique: true,
  },
  user: {
    type: mongoose.Schema.Types.ObjectId,
    ref: 'User',
    required: true,
  },
  restaurant: {
    type: mongoose.Schema.Types.ObjectId,
    ref: 'Restaurant',
    required: true,
  },
  rider: {
    type: mongoose.Schema.Types.ObjectId,
    ref: 'Rider',
  },
  items: [orderItemSchema],
  deliveryAddress: {
    label: String,
    houseNo: String,
    roadNo: String,
    area: String,
    thana: String,
    district: String,
    postalCode: String,
    landmark: String,
    location: {
      type: {
        type: String,
        default: 'Point',
      },
      coordinates: [Number],
    },
  },
  subtotal: {
    type: Number,
    required: true, // in BDT paisa
  },
  deliveryFee: {
    type: Number,
    default: 0,
  },
  vat: {
    type: Number,
    default: 0, // 5% in Bangladesh
  },
  discount: {
    type: Number,
    default: 0,
  },
  total: {
    type: Number,
    required: true,
  },
  paymentMethod: {
    type: String,
    enum: ['bkash', 'nagad', 'rocket', 'card', 'cod'],
    required: true,
  },
  paymentStatus: {
    type: String,
    enum: ['pending', 'paid', 'failed', 'refunded'],
    default: 'pending',
  },
  paymentRef: String, // Payment gateway reference
  status: {
    type: String,
    enum: ['pending', 'confirmed', 'preparing', 'ready', 'picked_up', 'on_the_way', 'delivered', 'cancelled'],
    default: 'pending',
  },
  statusHistory: [{
    status: String,
    timestamp: {
      type: Date,
      default: Date.now,
    },
    note: String,
  }],
  specialInstructions: String,
  scheduledFor: Date,
  estimatedDelivery: Date,
  actualDelivery: Date,
  cancellationReason: String,
  rating: {
    foodRating: Number,
    deliveryRating: Number,
    review: String,
  },
}, {
  timestamps: true,
});

// Generate order number before saving
orderSchema.pre('save', async function(next) {
  if (!this.orderNumber) {
    const count = await mongoose.model('Order').countDocuments();
    this.orderNumber = `KL${Date.now().toString().slice(-8)}${String(count + 1).padStart(4, '0')}`;
  }
  next();
});

orderSchema.index({ user: 1, createdAt: -1 });
orderSchema.index({ restaurant: 1, status: 1 });
orderSchema.index({ rider: 1, status: 1 });

module.exports = mongoose.model('Order', orderSchema);
