const mongoose = require('mongoose');

const paymentSchema = new mongoose.Schema({
  order: {
    type: mongoose.Schema.Types.ObjectId,
    ref: 'Order',
    required: true,
  },
  user: {
    type: mongoose.Schema.Types.ObjectId,
    ref: 'User',
    required: true,
  },
  amount: {
    type: Number,
    required: true, // in BDT paisa
  },
  method: {
    type: String,
    enum: ['bkash', 'nagad', 'rocket', 'card', 'cod'],
    required: true,
  },
  status: {
    type: String,
    enum: ['pending', 'initiated', 'success', 'failed', 'refunded'],
    default: 'pending',
  },
  gateway: {
    provider: String, // 'bkash', 'nagad', 'sslcommerz'
    transactionId: String,
    paymentId: String,
    sessionKey: String,
  },
  gatewayResponse: mongoose.Schema.Types.Mixed,
  refund: {
    amount: Number,
    reason: String,
    refundedAt: Date,
    refundId: String,
  },
  metadata: mongoose.Schema.Types.Mixed,
}, {
  timestamps: true,
});

paymentSchema.index({ order: 1 });
paymentSchema.index({ user: 1, createdAt: -1 });
paymentSchema.index({ 'gateway.transactionId': 1 });

module.exports = mongoose.model('Payment', paymentSchema);
