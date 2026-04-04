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

    // Actual admin profit from payments (5% deduction)
    const profitData = await Payment.aggregate([
      {
        $match: {
          createdAt: { $gte: start, $lte: end },
          status: 'success',
        },
      },
      {
        $group: {
          _id: null,
          totalAdminProfit: { $sum: '$adminProfit' },
          totalRestaurantPayout: { $sum: '$restaurantPayout' },
          totalPayments: { $sum: '$amount' },
          paymentCount: { $sum: 1 },
        },
      },
    ]);

    const profitSummary = profitData[0] || {
      totalAdminProfit: 0,
      totalRestaurantPayout: 0,
      totalPayments: 0,
      paymentCount: 0,
    };

    platformRevenue.adminProfit = profitSummary.totalAdminProfit;
    platformRevenue.restaurantPayout = profitSummary.totalRestaurantPayout;
    platformRevenue.totalPayments = profitSummary.totalPayments;
    platformRevenue.paymentCount = profitSummary.paymentCount;

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
    const { type, startDate, endDate, format = 'json' } = req.query;

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

    // CSV export support
    if (format === 'csv') {
      const csvRows = [];
      if (data.length > 0) {
        // Build flat headers from first record
        const headers = Object.keys(data[0]).filter(k => k !== '__v');
        csvRows.push(headers.join(','));
        for (const row of data) {
          const values = headers.map(h => {
            const val = row[h];
            if (val === null || val === undefined) return '';
            const str = typeof val === 'object' ? JSON.stringify(val) : String(val);
            // Escape CSV special characters
            return `"${str.replace(/"/g, '""')}"`;
          });
          csvRows.push(values.join(','));
        }
      }
      res.setHeader('Content-Type', 'text/csv');
      res.setHeader('Content-Disposition', `attachment; filename=${type}_report_${start.toISOString().slice(0, 10)}_${end.toISOString().slice(0, 10)}.csv`);
      return res.send(csvRows.join('\n'));
    }

    res.json({
      success: true,
      data,
    });
  } catch (error) {
    next(error);
  }
};

// Get admin profit analytics (detailed payment profit breakdown)
exports.getProfitAnalytics = async (req, res, next) => {
  try {
    const { startDate, endDate, groupBy = 'daily' } = req.query;

    const start = startDate ? new Date(startDate) : new Date(Date.now() - 30 * 24 * 60 * 60 * 1000);
    const end = endDate ? new Date(endDate) : new Date();

    let dateGroup;
    switch (groupBy) {
      case 'hourly':
        dateGroup = {
          year: { $year: '$createdAt' },
          month: { $month: '$createdAt' },
          day: { $dayOfMonth: '$createdAt' },
          hour: { $hour: '$createdAt' },
        };
        break;
      case 'monthly':
        dateGroup = {
          year: { $year: '$createdAt' },
          month: { $month: '$createdAt' },
        };
        break;
      default: // daily
        dateGroup = {
          year: { $year: '$createdAt' },
          month: { $month: '$createdAt' },
          day: { $dayOfMonth: '$createdAt' },
        };
    }

    // Profit over time
    const profitTimeline = await Payment.aggregate([
      {
        $match: {
          createdAt: { $gte: start, $lte: end },
          status: 'success',
        },
      },
      {
        $group: {
          _id: dateGroup,
          totalAmount: { $sum: '$amount' },
          adminProfit: { $sum: '$adminProfit' },
          restaurantPayout: { $sum: '$restaurantPayout' },
          transactionCount: { $sum: 1 },
        },
      },
      { $sort: { '_id.year': 1, '_id.month': 1, '_id.day': 1, '_id.hour': 1 } },
    ]);

    // Profit by payment method
    const profitByMethod = await Payment.aggregate([
      {
        $match: {
          createdAt: { $gte: start, $lte: end },
          status: 'success',
        },
      },
      {
        $group: {
          _id: '$method',
          totalAmount: { $sum: '$amount' },
          adminProfit: { $sum: '$adminProfit' },
          restaurantPayout: { $sum: '$restaurantPayout' },
          transactionCount: { $sum: 1 },
        },
      },
      { $sort: { adminProfit: -1 } },
    ]);

    // Totals
    const totals = await Payment.aggregate([
      {
        $match: {
          createdAt: { $gte: start, $lte: end },
          status: 'success',
        },
      },
      {
        $group: {
          _id: null,
          totalAmount: { $sum: '$amount' },
          totalAdminProfit: { $sum: '$adminProfit' },
          totalRestaurantPayout: { $sum: '$restaurantPayout' },
          totalTransactions: { $sum: 1 },
          avgAdminProfit: { $avg: '$adminProfit' },
        },
      },
    ]);

    const summary = totals[0] || {
      totalAmount: 0,
      totalAdminProfit: 0,
      totalRestaurantPayout: 0,
      totalTransactions: 0,
      avgAdminProfit: 0,
    };

    res.json({
      success: true,
      data: {
        summary,
        timeline: profitTimeline,
        byPaymentMethod: profitByMethod,
        adminProfitRate: parseFloat(process.env.ADMIN_PROFIT_RATE || '5'),
      },
    });
  } catch (error) {
    next(error);
  }
};

// Export financial report as CSV
exports.exportFinancialReport = async (req, res, next) => {
  try {
    const { startDate, endDate } = req.query;

    const start = startDate ? new Date(startDate) : new Date(Date.now() - 30 * 24 * 60 * 60 * 1000);
    const end = endDate ? new Date(endDate) : new Date();

    const payments = await Payment.find({
      createdAt: { $gte: start, $lte: end },
      status: 'success',
    })
      .populate('user', 'name phone')
      .populate('order', 'orderNumber')
      .sort({ createdAt: -1 })
      .lean();

    // Build CSV
    const csvHeaders = [
      'Date',
      'Order Number',
      'Customer Name',
      'Customer Phone',
      'Payment Method',
      'Gateway',
      'Transaction ID',
      'Total Amount (BDT)',
      'Admin Profit Rate (%)',
      'Admin Profit (BDT)',
      'Restaurant Payout (BDT)',
      'Status',
    ];

    const csvRows = [csvHeaders.join(',')];

    for (const p of payments) {
      const row = [
        new Date(p.createdAt).toISOString().slice(0, 19).replace('T', ' '),
        p.order?.orderNumber || 'N/A',
        `"${(p.user?.name || 'N/A').replace(/"/g, '""')}"`,
        p.user?.phone || 'N/A',
        p.method,
        p.gateway?.provider || 'N/A',
        p.gateway?.transactionId || 'N/A',
        (p.amount / 100).toFixed(2),
        p.adminProfitRate || 5,
        ((p.adminProfit || 0) / 100).toFixed(2),
        ((p.restaurantPayout || 0) / 100).toFixed(2),
        p.status,
      ];
      csvRows.push(row.join(','));
    }

    res.setHeader('Content-Type', 'text/csv');
    res.setHeader('Content-Disposition', `attachment; filename=financial_report_${start.toISOString().slice(0, 10)}_${end.toISOString().slice(0, 10)}.csv`);
    res.send(csvRows.join('\n'));
  } catch (error) {
    next(error);
  }
};

// Get restaurant verification stats
exports.getVerificationStats = async (req, res, next) => {
  try {
    const pending = await Restaurant.countDocuments({ approvalStatus: 'pending' });
    const approved = await Restaurant.countDocuments({ approvalStatus: 'approved' });
    const rejected = await Restaurant.countDocuments({ approvalStatus: 'rejected' });
    const suspended = await Restaurant.countDocuments({ approvalStatus: 'approved', isActive: false });

    res.json({
      success: true,
      data: {
        pending,
        approved,
        rejected,
        suspended,
        total: pending + approved + rejected,
      },
    });
  } catch (error) {
    next(error);
  }
};

module.exports = exports;
