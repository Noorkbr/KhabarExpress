const Payment = require('../models/Payment');
const Order = require('../models/Order');
const bkashService = require('../services/bkashService');
const nagadService = require('../services/nagadService');
const sslCommerzService = require('../services/sslCommerzService');
const { getIO } = require('../config/socket');

// Create payment
exports.createPayment = async (req, res, next) => {
  try {
    const { orderId, method, returnUrl } = req.body;

    // Get order
    const order = await Order.findById(orderId);
    if (!order) {
      return res.status(404).json({
        success: false,
        message: 'Order not found',
      });
    }

    // Check if order belongs to user
    if (order.userId.toString() !== req.user.userId) {
      return res.status(403).json({
        success: false,
        message: 'Not authorized',
      });
    }

    // Check if order already has successful payment
    const existingPayment = await Payment.findOne({
      order: orderId,
      status: 'success',
    });

    if (existingPayment) {
      return res.status(400).json({
        success: false,
        message: 'Order already paid',
      });
    }

    // Create payment record
    const payment = await Payment.create({
      order: orderId,
      user: req.user.userId,
      amount: order.totalAmount,
      method,
      status: 'initiated',
    });

    let paymentResponse;

    // Process payment based on method
    switch (method) {
      case 'bkash':
        paymentResponse = await bkashService.createPayment({
          amount: order.totalAmount,
          orderId: order._id.toString(),
          paymentId: payment._id.toString(),
        });
        
        payment.gateway = {
          provider: 'bkash',
          paymentId: paymentResponse.paymentID,
          sessionKey: paymentResponse.sessionKey,
        };
        break;

      case 'nagad':
        paymentResponse = await nagadService.createPayment({
          amount: order.totalAmount,
          orderId: order._id.toString(),
          paymentId: payment._id.toString(),
        });
        
        payment.gateway = {
          provider: 'nagad',
          paymentId: paymentResponse.paymentReferenceId,
          sessionKey: paymentResponse.challenge,
        };
        break;

      case 'card':
      case 'rocket':
        paymentResponse = await sslCommerzService.createPayment({
          amount: order.totalAmount,
          orderId: order._id.toString(),
          paymentId: payment._id.toString(),
          customerName: req.user.name,
          customerEmail: req.user.email || `user${req.user.userId}@khabarlagbe.com`,
          customerPhone: req.user.phone,
          returnUrl: returnUrl || `${process.env.FRONTEND_URL}/payment/callback`,
        });
        
        payment.gateway = {
          provider: 'sslcommerz',
          transactionId: paymentResponse.sessionkey,
        };
        break;

      case 'cod':
        // Cash on delivery - no payment gateway needed
        payment.status = 'pending';
        order.paymentStatus = 'pending';
        await order.save();
        await payment.save();

        return res.json({
          success: true,
          message: 'Order placed successfully with cash on delivery',
          data: {
            payment,
            paymentMethod: 'cod',
          },
        });

      default:
        return res.status(400).json({
          success: false,
          message: 'Invalid payment method',
        });
    }

    await payment.save();

    res.json({
      success: true,
      message: 'Payment initiated successfully',
      data: {
        payment,
        redirectUrl: paymentResponse.bkashURL || paymentResponse.callBackUrl || paymentResponse.GatewayPageURL,
      },
    });
  } catch (error) {
    console.error('Payment creation error:', error);
    next(error);
  }
};

// bKash callback handler
exports.bkashCallback = async (req, res, next) => {
  try {
    const { paymentID, status } = req.query;

    const payment = await Payment.findOne({
      'gateway.paymentId': paymentID,
    });

    if (!payment) {
      return res.status(404).json({
        success: false,
        message: 'Payment not found',
      });
    }

    if (status === 'success') {
      // Execute payment
      const executeResponse = await bkashService.executePayment({
        paymentID,
      });

      if (executeResponse.statusCode === '0000') {
        payment.status = 'success';
        payment.gateway.transactionId = executeResponse.trxID;
        payment.gatewayResponse = executeResponse;

        // Update order
        const order = await Order.findById(payment.order);
        order.paymentStatus = 'paid';
        order.status = 'confirmed';
        await order.save();

        // Emit order update
        const io = getIO();
        io.to(`order_${order._id}`).emit('orderStatusUpdate', {
          orderId: order._id,
          status: 'confirmed',
          paymentStatus: 'paid',
        });
      } else {
        payment.status = 'failed';
        payment.gatewayResponse = executeResponse;
      }
    } else {
      payment.status = 'failed';
    }

    await payment.save();

    res.json({
      success: payment.status === 'success',
      message: payment.status === 'success' ? 'Payment successful' : 'Payment failed',
      data: payment,
    });
  } catch (error) {
    console.error('bKash callback error:', error);
    next(error);
  }
};

// Nagad callback handler
exports.nagadCallback = async (req, res, next) => {
  try {
    const { payment_ref_id, status } = req.body;

    const payment = await Payment.findOne({
      'gateway.paymentId': payment_ref_id,
    });

    if (!payment) {
      return res.status(404).json({
        success: false,
        message: 'Payment not found',
      });
    }

    if (status === 'Success') {
      // Verify payment
      const verifyResponse = await nagadService.verifyPayment({
        paymentReferenceId: payment_ref_id,
      });

      if (verifyResponse.status === 'Success') {
        payment.status = 'success';
        payment.gateway.transactionId = verifyResponse.issuerPaymentRefNo;
        payment.gatewayResponse = verifyResponse;

        // Update order
        const order = await Order.findById(payment.order);
        order.paymentStatus = 'paid';
        order.status = 'confirmed';
        await order.save();

        // Emit order update
        const io = getIO();
        io.to(`order_${order._id}`).emit('orderStatusUpdate', {
          orderId: order._id,
          status: 'confirmed',
          paymentStatus: 'paid',
        });
      } else {
        payment.status = 'failed';
        payment.gatewayResponse = verifyResponse;
      }
    } else {
      payment.status = 'failed';
    }

    await payment.save();

    res.json({
      success: payment.status === 'success',
      message: payment.status === 'success' ? 'Payment successful' : 'Payment failed',
      data: payment,
    });
  } catch (error) {
    console.error('Nagad callback error:', error);
    next(error);
  }
};

// SSL Commerz callback handler
exports.sslCommerzCallback = async (req, res, next) => {
  try {
    const { tran_id, status, val_id } = req.body;

    const payment = await Payment.findOne({
      'gateway.transactionId': tran_id,
    });

    if (!payment) {
      return res.status(404).json({
        success: false,
        message: 'Payment not found',
      });
    }

    if (status === 'VALID' || status === 'VALIDATED') {
      // Validate payment
      const validateResponse = await sslCommerzService.validatePayment({
        validationId: val_id,
      });

      if (validateResponse.status === 'VALID' || validateResponse.status === 'VALIDATED') {
        payment.status = 'success';
        payment.gateway.transactionId = validateResponse.tran_id;
        payment.gatewayResponse = validateResponse;

        // Update order
        const order = await Order.findById(payment.order);
        order.paymentStatus = 'paid';
        order.status = 'confirmed';
        await order.save();

        // Emit order update
        const io = getIO();
        io.to(`order_${order._id}`).emit('orderStatusUpdate', {
          orderId: order._id,
          status: 'confirmed',
          paymentStatus: 'paid',
        });
      } else {
        payment.status = 'failed';
        payment.gatewayResponse = validateResponse;
      }
    } else {
      payment.status = 'failed';
      payment.gatewayResponse = req.body;
    }

    await payment.save();

    res.json({
      success: payment.status === 'success',
      message: payment.status === 'success' ? 'Payment successful' : 'Payment failed',
      data: payment,
    });
  } catch (error) {
    console.error('SSL Commerz callback error:', error);
    next(error);
  }
};

// Get payment by order ID
exports.getPaymentByOrderId = async (req, res, next) => {
  try {
    const { orderId } = req.params;

    const payment = await Payment.findOne({ order: orderId })
      .populate('order')
      .populate('user', 'name phone email');

    if (!payment) {
      return res.status(404).json({
        success: false,
        message: 'Payment not found',
      });
    }

    res.json({
      success: true,
      data: payment,
    });
  } catch (error) {
    next(error);
  }
};

// Get payment history for user
exports.getPaymentHistory = async (req, res, next) => {
  try {
    const { page = 1, limit = 20 } = req.query;
    const skip = (page - 1) * limit;

    const payments = await Payment.find({ user: req.user.userId })
      .populate('order', 'orderNumber totalAmount createdAt')
      .sort({ createdAt: -1 })
      .skip(skip)
      .limit(parseInt(limit));

    const total = await Payment.countDocuments({ user: req.user.userId });

    res.json({
      success: true,
      data: {
        payments,
        pagination: {
          total,
          page: parseInt(page),
          pages: Math.ceil(total / limit),
        },
      },
    });
  } catch (error) {
    next(error);
  }
};

// Refund payment (Admin only)
exports.refundPayment = async (req, res, next) => {
  try {
    const { paymentId } = req.params;
    const { reason, amount } = req.body;

    const payment = await Payment.findById(paymentId);
    if (!payment) {
      return res.status(404).json({
        success: false,
        message: 'Payment not found',
      });
    }

    if (payment.status !== 'success') {
      return res.status(400).json({
        success: false,
        message: 'Only successful payments can be refunded',
      });
    }

    let refundResponse;
    const refundAmount = amount || payment.amount;

    // Process refund based on payment method
    switch (payment.gateway.provider) {
      case 'bkash':
        refundResponse = await bkashService.refundPayment({
          paymentID: payment.gateway.paymentId,
          amount: refundAmount,
          trxID: payment.gateway.transactionId,
          reason,
        });
        break;

      case 'nagad':
        // Nagad refund implementation
        return res.status(501).json({
          success: false,
          message: 'Nagad refunds not yet implemented',
        });

      case 'sslcommerz':
        refundResponse = await sslCommerzService.refundPayment({
          bankTransactionId: payment.gateway.transactionId,
          refundAmount: refundAmount,
          reason,
        });
        break;

      default:
        return res.status(400).json({
          success: false,
          message: 'Refund not supported for this payment method',
        });
    }

    // Update payment record
    payment.status = 'refunded';
    payment.refund = {
      amount: refundAmount,
      reason,
      refundedAt: new Date(),
      refundId: refundResponse.refundTrxID || refundResponse.refund_ref_id,
    };

    await payment.save();

    // Update order
    const order = await Order.findById(payment.order);
    order.paymentStatus = 'refunded';
    order.status = 'cancelled';
    await order.save();

    res.json({
      success: true,
      message: 'Payment refunded successfully',
      data: payment,
    });
  } catch (error) {
    console.error('Refund error:', error);
    next(error);
  }
};

// Admin: Get all payments
exports.getAllPayments = async (req, res, next) => {
  try {
    const { page = 1, limit = 20, status, method } = req.query;
    const skip = (page - 1) * limit;

    const query = {};
    if (status) query.status = status;
    if (method) query.method = method;

    const payments = await Payment.find(query)
      .populate('user', 'name phone')
      .populate('order', 'orderNumber totalAmount')
      .sort({ createdAt: -1 })
      .skip(skip)
      .limit(parseInt(limit));

    const total = await Payment.countDocuments(query);

    res.json({
      success: true,
      data: {
        payments,
        pagination: {
          total,
          page: parseInt(page),
          pages: Math.ceil(total / limit),
        },
      },
    });
  } catch (error) {
    next(error);
  }
};

module.exports = exports;
