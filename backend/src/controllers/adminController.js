const User = require('../models/User');
const Restaurant = require('../models/Restaurant');
const Order = require('../models/Order');
const Rider = require('../models/Rider');
const Payment = require('../models/Payment');
const Review = require('../models/Review');

// Get dashboard statistics
exports.getDashboardStats = async (req, res, next) => {
  try {
    const today = new Date();
    today.setHours(0, 0, 0, 0);

    const tomorrow = new Date(today);
    tomorrow.setDate(tomorrow.getDate() + 1);

    // Today's stats
    const todayOrders = await Order.countDocuments({
      createdAt: { $gte: today, $lt: tomorrow },
    });

    const todayRevenue = await Order.aggregate([
      {
        $match: {
          createdAt: { $gte: today, $lt: tomorrow },
          status: { $ne: 'cancelled' },
        },
      },
      {
        $group: {
          _id: null,
          total: { $sum: '$totalAmount' },
        },
      },
    ]);

    // Overall stats
    const totalUsers = await User.countDocuments({ role: 'customer' });
    const totalRestaurants = await Restaurant.countDocuments({ isActive: true });
    const totalRiders = await Rider.countDocuments({ isApproved: true });
    const totalOrders = await Order.countDocuments();

    // Pending approvals
    const pendingRestaurants = await Restaurant.countDocuments({ isApproved: false });
    const pendingRiders = await Rider.countDocuments({ isApproved: false });

    // Active orders
    const activeOrders = await Order.countDocuments({
      status: { $in: ['pending', 'confirmed', 'preparing', 'ready', 'picked_up', 'on_the_way'] },
    });

    // Online riders
    const onlineRiders = await Rider.countDocuments({ status: { $in: ['available', 'busy'] } });

    res.json({
      success: true,
      data: {
        today: {
          orders: todayOrders,
          revenue: todayRevenue[0]?.total || 0,
        },
        totals: {
          users: totalUsers,
          restaurants: totalRestaurants,
          riders: totalRiders,
          orders: totalOrders,
        },
        pending: {
          restaurants: pendingRestaurants,
          riders: pendingRiders,
        },
        active: {
          orders: activeOrders,
          riders: onlineRiders,
        },
      },
    });
  } catch (error) {
    next(error);
  }
};

// Get recent activities
exports.getRecentActivities = async (req, res, next) => {
  try {
    const limit = parseInt(req.query.limit) || 20;

    const recentOrders = await Order.find()
      .populate('userId', 'name phone')
      .populate('restaurantId', 'name')
      .populate('riderId', 'name')
      .sort({ createdAt: -1 })
      .limit(limit)
      .select('orderNumber status totalAmount createdAt');

    const recentReviews = await Review.find({ isPublished: true })
      .populate('user', 'name')
      .populate('restaurant', 'name')
      .sort({ createdAt: -1 })
      .limit(10)
      .select('foodRating review createdAt');

    res.json({
      success: true,
      data: {
        orders: recentOrders,
        reviews: recentReviews,
      },
    });
  } catch (error) {
    next(error);
  }
};

// Get analytics data
exports.getAnalytics = async (req, res, next) => {
  try {
    const { startDate, endDate, type = 'daily' } = req.query;

    const start = startDate ? new Date(startDate) : new Date(Date.now() - 30 * 24 * 60 * 60 * 1000);
    const end = endDate ? new Date(endDate) : new Date();

    let groupBy;
    switch (type) {
      case 'hourly':
        groupBy = {
          year: { $year: '$createdAt' },
          month: { $month: '$createdAt' },
          day: { $dayOfMonth: '$createdAt' },
          hour: { $hour: '$createdAt' },
        };
        break;
      case 'daily':
        groupBy = {
          year: { $year: '$createdAt' },
          month: { $month: '$createdAt' },
          day: { $dayOfMonth: '$createdAt' },
        };
        break;
      case 'monthly':
        groupBy = {
          year: { $year: '$createdAt' },
          month: { $month: '$createdAt' },
        };
        break;
      default:
        groupBy = {
          year: { $year: '$createdAt' },
          month: { $month: '$createdAt' },
          day: { $dayOfMonth: '$createdAt' },
        };
    }

    // Orders analytics
    const ordersAnalytics = await Order.aggregate([
      {
        $match: {
          createdAt: { $gte: start, $lte: end },
        },
      },
      {
        $group: {
          _id: groupBy,
          totalOrders: { $sum: 1 },
          totalRevenue: { $sum: '$totalAmount' },
          cancelledOrders: {
            $sum: { $cond: [{ $eq: ['$status', 'cancelled'] }, 1, 0] },
          },
          deliveredOrders: {
            $sum: { $cond: [{ $eq: ['$status', 'delivered'] }, 1, 0] },
          },
        },
      },
      { $sort: { '_id.year': 1, '_id.month': 1, '_id.day': 1, '_id.hour': 1 } },
    ]);

    // Popular restaurants
    const popularRestaurants = await Order.aggregate([
      {
        $match: {
          createdAt: { $gte: start, $lte: end },
          status: { $ne: 'cancelled' },
        },
      },
      {
        $group: {
          _id: '$restaurantId',
          totalOrders: { $sum: 1 },
          totalRevenue: { $sum: '$totalAmount' },
        },
      },
      { $sort: { totalOrders: -1 } },
      { $limit: 10 },
      {
        $lookup: {
          from: 'restaurants',
          localField: '_id',
          foreignField: '_id',
          as: 'restaurant',
        },
      },
      { $unwind: '$restaurant' },
    ]);

    // Top riders
    const topRiders = await Order.aggregate([
      {
        $match: {
          createdAt: { $gte: start, $lte: end },
          status: 'delivered',
          riderId: { $exists: true },
        },
      },
      {
        $group: {
          _id: '$riderId',
          totalDeliveries: { $sum: 1 },
          totalEarnings: { $sum: '$deliveryFee' },
        },
      },
      { $sort: { totalDeliveries: -1 } },
      { $limit: 10 },
      {
        $lookup: {
          from: 'riders',
          localField: '_id',
          foreignField: '_id',
          as: 'rider',
        },
      },
      { $unwind: '$rider' },
    ]);

    // Payment method distribution
    const paymentMethods = await Payment.aggregate([
      {
        $match: {
          createdAt: { $gte: start, $lte: end },
          status: 'success',
        },
      },
      {
        $group: {
          _id: '$method',
          count: { $sum: 1 },
          totalAmount: { $sum: '$amount' },
        },
      },
      { $sort: { count: -1 } },
    ]);

    res.json({
      success: true,
      data: {
        orders: ordersAnalytics,
        popularRestaurants,
        topRiders,
        paymentMethods,
      },
    });
  } catch (error) {
    next(error);
  }
};

// Get revenue analytics
exports.getRevenueAnalytics = async (req, res, next) => {
  try {
    const { startDate, endDate } = req.query;

    const start = startDate ? new Date(startDate) : new Date(Date.now() - 30 * 24 * 60 * 60 * 1000);
    const end = endDate ? new Date(endDate) : new Date();

    const revenueData = await Order.aggregate([
      {
        $match: {
          createdAt: { $gte: start, $lte: end },
          status: { $ne: 'cancelled' },
        },
      },
      {
        $group: {
          _id: null,
          totalRevenue: { $sum: '$totalAmount' },
          totalOrders: { $sum: 1 },
          deliveryFees: { $sum: '$deliveryFee' },
          discounts: { $sum: '$discount' },
          subtotal: { $sum: '$subtotal' },
          tax: { $sum: '$tax' },
        },
      },
    ]);

    const platformRevenue = revenueData[0] || {
      totalRevenue: 0,
      totalOrders: 0,
      deliveryFees: 0,
      discounts: 0,
      subtotal: 0,
      tax: 0,
    };

    // Calculate commission (assume 15% commission)
    platformRevenue.commission = platformRevenue.subtotal * 0.15;

    res.json({
      success: true,
      data: platformRevenue,
    });
  } catch (error) {
    next(error);
  }
};

// Get user growth analytics
exports.getUserGrowthAnalytics = async (req, res, next) => {
  try {
    const { startDate, endDate } = req.query;

    const start = startDate ? new Date(startDate) : new Date(Date.now() - 30 * 24 * 60 * 60 * 1000);
    const end = endDate ? new Date(endDate) : new Date();

    const userGrowth = await User.aggregate([
      {
        $match: {
          createdAt: { $gte: start, $lte: end },
        },
      },
      {
        $group: {
          _id: {
            year: { $year: '$createdAt' },
            month: { $month: '$createdAt' },
            day: { $dayOfMonth: '$createdAt' },
          },
          newUsers: { $sum: 1 },
        },
      },
      { $sort: { '_id.year': 1, '_id.month': 1, '_id.day': 1 } },
    ]);

    res.json({
      success: true,
      data: userGrowth,
    });
  } catch (error) {
    next(error);
  }
};

// Export data
exports.exportData = async (req, res, next) => {
  try {
    const { type, startDate, endDate } = req.query;

    const start = startDate ? new Date(startDate) : new Date(Date.now() - 30 * 24 * 60 * 60 * 1000);
    const end = endDate ? new Date(endDate) : new Date();

    let data;

    switch (type) {
      case 'orders':
        data = await Order.find({
          createdAt: { $gte: start, $lte: end },
        })
          .populate('userId', 'name phone')
          .populate('restaurantId', 'name')
          .populate('riderId', 'name')
          .lean();
        break;

      case 'payments':
        data = await Payment.find({
          createdAt: { $gte: start, $lte: end },
        })
          .populate('user', 'name phone')
          .populate('order', 'orderNumber')
          .lean();
        break;

      case 'reviews':
        data = await Review.find({
          createdAt: { $gte: start, $lte: end },
        })
          .populate('user', 'name')
          .populate('restaurant', 'name')
          .lean();
        break;

      default:
        return res.status(400).json({
          success: false,
          message: 'Invalid export type',
        });
    }

    res.json({
      success: true,
      data,
    });
  } catch (error) {
    next(error);
  }
};

module.exports = exports;
