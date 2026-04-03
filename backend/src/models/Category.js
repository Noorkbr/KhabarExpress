const mongoose = require('mongoose');

const categorySchema = new mongoose.Schema({
  restaurant: {
    type: mongoose.Schema.Types.ObjectId,
    ref: 'Restaurant',
    required: true,
  },
  name: {
    type: String,
    required: true,
  },
  nameBn: String,
  description: String,
  descriptionBn: String,
  image: String,
  order: {
    type: Number,
    default: 0,
  },
  isActive: {
    type: Boolean,
    default: true,
  },
}, {
  timestamps: true,
});

categorySchema.index({ restaurant: 1, order: 1 });

module.exports = mongoose.model('Category', categorySchema);
