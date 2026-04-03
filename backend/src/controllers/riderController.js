const Rider = require('../models/Rider');
const Order = require('../models/Order');
const mongoose = require('mongoose');
const { getIO } = require('../config/socket');

// Register rider
exports.registerRider = async (req, res, next) => {
  try {
    const {
      name,
      phone,
      email,
      nid,
      drivingLicense,
      vehicleType,
      vehicleNumber,
      zone,
    } = req.body;

    // Check if rider already exists
    const existingRider = await Rider.findOne({ phone });
    if (existingRider) {
      return res.status(400).json({
        success: false,
        message: 'Rider with this phone number already exists',
      });
    }

    // Create rider
    const rider = await Rider.create({
      name,
      phone,
      email,
      nid,
      drivingLicense,
      vehicleType,
      vehicleNumber,
      zone,
      isApproved: false, // Requires admin approval
    });

    res.status(201).json({
      success: true,
      message: 'Rider registered successfully. Awaiting admin approval.',
      data: rider,
    });
  } catch (error) {
    next(error);
  }
};

// Get rider profile
exports.getRiderProfile = async (req, res, next) => {
  try {
    const rider = await Rider.findById(req.user.riderId);
    if (!rider) {
      return res.status(404).json({
        success: false,
        message: 'Rider not found',
      });
    }

    res.json({
      success: true,
      data: rider,
    });
  } catch (error) {
    next(error);
  }
};

// Update rider profile
exports.updateRiderProfile = async (req, res, next) => {
  try {
    const allowedUpdates = ['name', 'email', 'vehicleType', 'vehicleNumber', 'zone'];
    const updates = Object.keys(req.body)
      .filter(key => allowedUpdates.includes(key))
      .reduce((obj, key) => {
        obj[key] = req.body[key];
        return obj;
      }, {});

    const rider = await Rider.findByIdAndUpdate(
      req.user.riderId,
      updates,
      { new: true, runValidators: true }
    );

    res.json({
      success: true,
      message: 'Profile updated successfully',
      data: rider,
    });
  } catch (error) {
    next(error);
  }
};

// Update rider status (online/offline/on_break)
exports.updateStatus = async (req, res, next) => {
  try {
    const { status } = req.body;
    
    if (!['offline', 'available', 'on_break'].includes(status)) {
      return res.status(400).json({
        success: false,
        message: 'Invalid status. Must be offline, available, or on_break',
      });
    }

    const rider = await Rider.findByIdAndUpdate(
      req.user.riderId,
      { status },
      { new: true }
    );

    // Emit status change via Socket.IO
    const io = getIO();
    io.to('admin').emit('riderStatusChanged', {
      riderId: rider._id,
      status: rider.status,
    });

    res.json({
      success: true,
      message: 'Status updated successfully',
      data: { status: rider.status },
    });
  } catch (error) {
    next(error);
  }
};

// Update rider location
exports.updateLocation = async (req, res, next) => {
  try {
    const { latitude, longitude } = req.body;

    if (!latitude || !longitude) {
      return res.status(400).json({
        success: false,
        message: 'Latitude and longitude are required',
      });
    }

    const rider = await Rider.findByIdAndUpdate(
      req.user.riderId,
      {
        currentLocation: {
          type: 'Point',
          coordinates: [longitude, latitude],
        },
      },
      { new: true }
    );

    // Emit location update via Socket.IO for active deliveries
    const activeOrder = await Order.findOne({
      riderId: req.user.riderId,
      status: { $in: ['picked_up', 'on_the_way'] },
    });

    if (activeOrder) {
      const io = getIO();
      io.to(`order_${activeOrder._id}`).emit('riderLocationUpdate', {
        riderId: rider._id,
        location: { latitude, longitude },
      });
    }

    res.json({
      success: true,
      message: 'Location updated successfully',
    });
  } catch (error) {
    next(error);
  }
};

// Get available orders for rider
exports.getAvailableOrders = async (req, res, next) => {
  try {
    const rider = await Rider.findById(req.user.riderId);
    
    if (!rider) {
      return res.status(404).json({
        success: false,
        message: 'Rider not found',
      });
    }

    if (rider.status !== 'available') {
      return res.status(400).json({
        success: false,
        message: 'Rider must be in available status to view orders',
      });
    }

    // Find orders that are ready for pickup and don't have a rider assigned
    const orders = await Order.find({
      status: 'ready',
      riderId: null,
    })
      .populate('restaurantId', 'name location phone')
      .populate('userId', 'name phone')
      .sort({ createdAt: 1 })
      .limit(10);

    res.json({
      success: true,
      data: orders,
    });
  } catch (error) {
    next(error);
  }
};

// Accept order
exports.acceptOrder = async (req, res, next) => {
  try {
    const { orderId } = req.params;

    const order = await Order.findById(orderId);
    if (!order) {
      return res.status(404).json({
        success: false,
        message: 'Order not found',
      });
    }

    if (order.riderId) {
      return res.status(400).json({
        success: false,
        message: 'Order already assigned to a rider',
      });
    }

    if (order.status !== 'ready') {
      return res.status(400).json({
        success: false,
        message: 'Order is not ready for pickup',
      });
    }

    // Assign rider to order
    order.riderId = req.user.riderId;
    order.status = 'picked_up';
    await order.save();

    // Update rider status to busy
    await Rider.findByIdAndUpdate(req.user.riderId, { status: 'busy' });

    // Emit order update via Socket.IO
    const io = getIO();
    io.to(`order_${order._id}`).emit('orderStatusUpdate', {
      orderId: order._id,
      status: 'picked_up',
      riderId: req.user.riderId,
    });

    res.json({
      success: true,
      message: 'Order accepted successfully',
      data: order,
    });
  } catch (error) {
    next(error);
  }
};

// Update order status (for rider)
exports.updateOrderStatus = async (req, res, next) => {
  try {
    const { orderId } = req.params;
    const { status, deliveryProof } = req.body;

    const order = await Order.findById(orderId);
    if (!order) {
      return res.status(404).json({
        success: false,
        message: 'Order not found',
      });
    }

    if (order.riderId.toString() !== req.user.riderId) {
      return res.status(403).json({
        success: false,
        message: 'Not authorized to update this order',
      });
    }

    const validStatuses = ['picked_up', 'on_the_way', 'delivered'];
    if (!validStatuses.includes(status)) {
      return res.status(400).json({
        success: false,
        message: 'Invalid status',
      });
    }

    order.status = status;
    
    if (status === 'delivered') {
      order.deliveredAt = new Date();
      if (deliveryProof) {
        order.deliveryProof = deliveryProof;
      }
      
      // Update rider stats and set back to available
      await Rider.findByIdAndUpdate(req.user.riderId, {
        $inc: { totalDeliveries: 1 },
        status: 'available',
      });
    }

    await order.save();

    // Emit order update via Socket.IO
    const io = getIO();
    io.to(`order_${order._id}`).emit('orderStatusUpdate', {
      orderId: order._id,
      status: order.status,
    });

    res.json({
      success: true,
      message: 'Order status updated successfully',
      data: order,
    });
  } catch (error) {
    next(error);
  }
};

// Get rider earnings
exports.getEarnings = async (req, res, next) => {
  try {
    const { startDate, endDate } = req.query;

    const query = {
      riderId: req.user.riderId,
      status: 'delivered',
    };

    if (startDate && endDate) {
      query.deliveredAt = {
        $gte: new Date(startDate),
        $lte: new Date(endDate),
      };
    }

    const orders = await Order.find(query);

    const totalEarnings = orders.reduce((sum, order) => {
      return sum + (order.deliveryFee || 0);
    }, 0);

    const stats = {
      totalDeliveries: orders.length,
      totalEarnings,
      averageEarningsPerDelivery: orders.length > 0 ? totalEarnings / orders.length : 0,
    };

    res.json({
      success: true,
      data: stats,
    });
  } catch (error) {
    next(error);
  }
};

// Get rider delivery history
exports.getDeliveryHistory = async (req, res, next) => {
  try {
    const page = parseInt(req.query.page) || 1;
    const limit = parseInt(req.query.limit) || 20;
    const skip = (page - 1) * limit;

    const orders = await Order.find({
      riderId: req.user.riderId,
      status: 'delivered',
    })
      .populate('restaurantId', 'name')
      .populate('userId', 'name')
      .sort({ deliveredAt: -1 })
      .skip(skip)
      .limit(limit);

    const total = await Order.countDocuments({
      riderId: req.user.riderId,
      status: 'delivered',
    });

    res.json({
      success: true,
      data: {
        orders,
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

// Get rider stats
exports.getRiderStats = async (req, res, next) => {
  try {
    const rider = await Rider.findById(req.user.riderId);

    const today = new Date();
    today.setHours(0, 0, 0, 0);

    const todayDeliveries = await Order.countDocuments({
      riderId: req.user.riderId,
      status: 'delivered',
      deliveredAt: { $gte: today },
    });

    const todayEarnings = await Order.aggregate([
      {
        $match: {
          riderId: new mongoose.Types.ObjectId(req.user.riderId),
          status: 'delivered',
          deliveredAt: { $gte: today },
        },
      },
      {
        $group: {
          _id: null,
          total: { $sum: '$deliveryFee' },
        },
      },
    ]);

    const stats = {
      rating: rider.rating,
      totalDeliveries: rider.totalDeliveries,
      todayDeliveries,
      todayEarnings: todayEarnings[0]?.total || 0,
      status: rider.status,
    };

    res.json({
      success: true,
      data: stats,
    });
  } catch (error) {
    next(error);
  }
};

// Admin: Get all riders
exports.getAllRiders = async (req, res, next) => {
  try {
    const { page = 1, limit = 20, status, zone, isApproved } = req.query;
    const skip = (page - 1) * limit;

    const query = {};
    if (status) query.status = status;
    if (zone) query.zone = zone;
    if (isApproved !== undefined) query.isApproved = isApproved === 'true';

    const riders = await Rider.find(query)
      .sort({ createdAt: -1 })
      .skip(skip)
      .limit(parseInt(limit));

    const total = await Rider.countDocuments(query);

    res.json({
      success: true,
      data: {
        riders,
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

// Admin: Approve rider
exports.approveRider = async (req, res, next) => {
  try {
    const { riderId } = req.params;

    const rider = await Rider.findByIdAndUpdate(
      riderId,
      { isApproved: true },
      { new: true }
    );

    if (!rider) {
      return res.status(404).json({
        success: false,
        message: 'Rider not found',
      });
    }

    res.json({
      success: true,
      message: 'Rider approved successfully',
      data: rider,
    });
  } catch (error) {
    next(error);
  }
};

// Admin: Suspend rider
exports.suspendRider = async (req, res, next) => {
  try {
    const { riderId } = req.params;
    const { reason } = req.body;

    const rider = await Rider.findByIdAndUpdate(
      riderId,
      {
        isApproved: false,
        status: 'offline',
        suspensionReason: reason,
      },
      { new: true }
    );

    if (!rider) {
      return res.status(404).json({
        success: false,
        message: 'Rider not found',
      });
    }

    res.json({
      success: true,
      message: 'Rider suspended successfully',
      data: rider,
    });
  } catch (error) {
    next(error);
  }
};

module.exports = exports;
