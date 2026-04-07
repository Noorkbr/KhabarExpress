const Payment = require('../models/Payment');
const Order = require('../models/Order');
const bkashService = require('../services/bkashService');
const nagadService = require('../services/nagadService');
const sslCommerzService = require('../services/sslCommerzService');
const rocketService = require('../services/rocketService');
const upayService = require('../services/upayService');
const { getIO } = require('../config/socket');

// Default admin profit rate (5%)
const ADMIN_PROFIT_RATE = parseFloat(process.env.ADMIN_PROFIT_RATE || '5');

// Calculate and store admin profit on a payment
const applyAdminProfit = (payment) => {
  const rate = ADMIN_PROFIT_RATE;
  payment.adminProfitRate = rate;
  payment.adminProfit = Math.round(payment.amount * (rate / 100));
  payment.restaurantPayout = payment.amount - payment.adminProfit;
};

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
    if (order.user.toString() !== req.userId.toString()) {
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
      user: req.userId,
      amount: order.total,
      method,
      status: 'initiated',
    });

    let paymentResponse;

    // Process payment based on method
    switch (method) {
      case 'bkash':
        paymentResponse = await bkashService.createPayment(
          order.total,
          order._id.toString(),
          req.user?.phone
        );
        
        payment.gateway = {
          provider: 'bkash',
          paymentId: paymentResponse.paymentId,
          sessionKey: paymentResponse.sessionKey,
        };
        break;

      case 'nagad':
        paymentResponse = await nagadService.initiatePayment(
          order.total,
          order._id.toString()
        );
        
        payment.gateway = {
          provider: 'nagad',
          paymentId: paymentResponse.paymentRef,
          sessionKey: paymentResponse.challenge,
        };
        break;

      case 'rocket':
        paymentResponse = await rocketService.createPayment({
          amount: order.total,
          orderId: order._id.toString(),
          paymentId: payment._id.toString(),
          customerPhone: req.user.phone,
          returnUrl: returnUrl || `${process.env.FRONTEND_URL}/payment/callback`,
        });

        payment.gateway = {
          provider: 'rocket',
          paymentId: paymentResponse.paymentId,
          sessionKey: paymentResponse.sessionKey,
        };
        break;

      case 'upay':
        paymentResponse = await upayService.createPayment({
          amount: order.total,
          orderId: order._id.toString(),
          paymentId: payment._id.toString(),
          customerPhone: req.user.phone,
          returnUrl: returnUrl || `${process.env.FRONTEND_URL}/payment/callback`,
        });

        payment.gateway = {
          provider: 'upay',
          paymentId: paymentResponse.paymentId,
          sessionKey: paymentResponse.sessionKey,
        };
        break;

      case 'card':
        paymentResponse = await sslCommerzService.initiateSession({
          orderId: order._id.toString(),
          amount: order.total,
          customerName: req.user?.name || 'Customer',
          customerEmail: req.user?.email || `user${req.userId}@khabarexpress.com`,
          customerPhone: req.user?.phone || '',
          productName: 'Food Order',
        });
        
        payment.gateway = {
          provider: 'sslcommerz',
          transactionId: paymentResponse.sessionKey,
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
        redirectUrl: paymentResponse.bkashURL || paymentResponse.callbackUrl || paymentResponse.gatewayPageURL || paymentResponse.redirectUrl,
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
      const executeResponse = await bkashService.executePayment(paymentID);

      if (executeResponse.success) {
        payment.status = 'success';
        payment.gateway.transactionId = executeResponse.trxID;
        payment.gatewayResponse = executeResponse;
        applyAdminProfit(payment);

        // Update order
        const order = await Order.findById(payment.order);
        order.paymentStatus = 'paid';
        order.status = 'confirmed';
        await order.save();

        // Emit order update
        const io = getIO();
        io.of('/order').to(`order:${order._id}`).emit('orderStatusUpdate', {
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
      const verifyResponse = await nagadService.verifyPayment(payment_ref_id);

      if (verifyResponse.success) {
        payment.status = 'success';
        payment.gateway.transactionId = verifyResponse.data?.issuerPaymentRefNo;
        payment.gatewayResponse = verifyResponse;
        applyAdminProfit(payment);

        // Update order
        const order = await Order.findById(payment.order);
        order.paymentStatus = 'paid';
        order.status = 'confirmed';
        await order.save();

        // Emit order update
        const io = getIO();
        io.of('/order').to(`order:${order._id}`).emit('orderStatusUpdate', {
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
      const validateResponse = await sslCommerzService.validatePayment(val_id);

      if (validateResponse.success) {
        payment.status = 'success';
        payment.gateway.transactionId = validateResponse.data?.tran_id;
        payment.gatewayResponse = validateResponse.data;
        applyAdminProfit(payment);

        // Update order
        const order = await Order.findById(payment.order);
        order.paymentStatus = 'paid';
        order.status = 'confirmed';
        await order.save();

        // Emit order update
        const io = getIO();
        io.of('/order').to(`order:${order._id}`).emit('orderStatusUpdate', {
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

// Rocket callback handler
exports.rocketCallback = async (req, res, next) => {
  try {
    const { payment_id, transaction_id, status } = req.body;

    // Sanitize inputs to prevent NoSQL injection
    if (typeof payment_id !== 'string' || typeof status !== 'string') {
      return res.status(400).json({ success: false, message: 'Invalid callback parameters' });
    }

    const payment = await Payment.findOne({
      'gateway.paymentId': String(payment_id),
    });

    if (!payment) {
      return res.status(404).json({
        success: false,
        message: 'Payment not found',
      });
    }

    if (status === 'completed' || status === 'success') {
      // Verify payment
      const verifyResponse = await rocketService.verifyPayment({
        transactionId: String(transaction_id || ''),
      });

      if (verifyResponse.success) {
        payment.status = 'success';
        payment.gateway.transactionId = verifyResponse.transactionId;
        payment.gatewayResponse = verifyResponse.data;
        applyAdminProfit(payment);

        // Update order
        const order = await Order.findById(payment.order);
        if (order) {
          order.paymentStatus = 'paid';
          order.status = 'confirmed';
          await order.save();

          // Emit order update
          const io = getIO();
          io.of('/order').to(`order:${order._id}`).emit('orderStatusUpdate', {
            orderId: order._id,
            status: 'confirmed',
            paymentStatus: 'paid',
          });
        }
      } else {
        payment.status = 'failed';
        payment.gatewayResponse = verifyResponse;
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
    console.error('Rocket callback error:', error);
    next(error);
  }
};

// Upay callback handler
exports.upayCallback = async (req, res, next) => {
  try {
    const { payment_id, transaction_id, status } = req.body;

    // Sanitize inputs to prevent NoSQL injection
    if (typeof payment_id !== 'string' || typeof status !== 'string') {
      return res.status(400).json({ success: false, message: 'Invalid callback parameters' });
    }

    const payment = await Payment.findOne({
      'gateway.paymentId': String(payment_id),
    });

    if (!payment) {
      return res.status(404).json({
        success: false,
        message: 'Payment not found',
      });
    }

    if (status === 'completed' || status === 'success') {
      // Verify payment
      const verifyResponse = await upayService.verifyPayment({
        transactionId: String(transaction_id || ''),
      });

      if (verifyResponse.success) {
        payment.status = 'success';
        payment.gateway.transactionId = verifyResponse.transactionId;
        payment.gatewayResponse = verifyResponse.data;
        applyAdminProfit(payment);

        // Update order
        const order = await Order.findById(payment.order);
        if (order) {
          order.paymentStatus = 'paid';
          order.status = 'confirmed';
          await order.save();

          // Emit order update
          const io = getIO();
          io.of('/order').to(`order:${order._id}`).emit('orderStatusUpdate', {
            orderId: order._id,
            status: 'confirmed',
            paymentStatus: 'paid',
          });
        }
      } else {
        payment.status = 'failed';
        payment.gatewayResponse = verifyResponse;
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
    console.error('Upay callback error:', error);
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
    const page = parseInt(req.query.page) || 1;
    const limit = Math.min(parseInt(req.query.limit) || 20, 100);
    const skip = (page - 1) * limit;

    const payments = await Payment.find({ user: req.userId })
      .populate('order', 'orderNumber total createdAt')
      .sort({ createdAt: -1 })
      .skip(skip)
      .limit(limit);

    const total = await Payment.countDocuments({ user: req.userId });

    res.json({
      success: true,
      data: {
        payments,
        pagination: {
          total,
          page,
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
        refundResponse = await bkashService.refund(
          payment.gateway.paymentId,
          refundAmount,
          reason
        );
        break;

      case 'nagad':
        // Nagad refund implementation
        return res.status(501).json({
          success: false,
          message: 'Nagad refunds not yet implemented',
        });

      case 'sslcommerz':
        refundResponse = await sslCommerzService.initiateRefund(
          payment.gateway.transactionId,
          refundAmount,
          reason
        );
        break;

      case 'rocket':
        refundResponse = await rocketService.refundPayment({
          transactionId: payment.gateway.transactionId,
          amount: refundAmount,
          reason,
        });
        break;

      case 'upay':
        refundResponse = await upayService.refundPayment({
          transactionId: payment.gateway.transactionId,
          amount: refundAmount,
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
    const page = parseInt(req.query.page) || 1;
    const limit = Math.min(parseInt(req.query.limit) || 20, 100);
    const skip = (page - 1) * limit;

    // Validate and whitelist query filters to prevent NoSQL injection
    const VALID_STATUSES = ['pending', 'initiated', 'success', 'failed', 'refunded'];
    const VALID_METHODS = ['bkash', 'nagad', 'rocket', 'upay', 'card', 'cod'];

    const query = {};
    if (typeof req.query.status === 'string' && VALID_STATUSES.includes(req.query.status)) {
      query.status = req.query.status;
    }
    if (typeof req.query.method === 'string' && VALID_METHODS.includes(req.query.method)) {
      query.method = req.query.method;
    }

    const payments = await Payment.find(query)
      .populate('user', 'name phone')
      .populate('order', 'orderNumber total')
      .sort({ createdAt: -1 })
      .skip(skip)
      .limit(limit);

    const total = await Payment.countDocuments(query);

    res.json({
      success: true,
      data: {
        payments,
        pagination: {
          total,
          page,
          pages: Math.ceil(total / limit),
        },
      },
    });
  } catch (error) {
    next(error);
  }
};

module.exports = exports;
