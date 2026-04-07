const User = require('../models/User');
const Restaurant = require('../models/Restaurant');
const Order = require('../models/Order');
const Rider = require('../models/Rider');
const Payment = require('../models/Payment');
const Review = require('../models/Review');
const Setting = require('../models/Setting');

// ── Helpers ────────────────────────────────────────────────────

/** Escape regex-special characters to prevent ReDoS / NoSQL injection via $regex */
const escapeRegex = (s) => String(s).replace(/[$()*+.?[\\\]^{|}]/g, '\\$&');

/** Cast query param to string and reject objects / arrays from user input */
const safeStr = (val) => (val && typeof val === 'string' ? val : null);

/** Validate a date string and return a Date, or null */
const safeDate = (val) => {
  const d = new Date(safeStr(val) || '');
  return Number.isNaN(d.getTime()) ? null : d;
};

// ── Enum allow-lists ──────────────────────────────────────────
const ALLOWED_ORDER_STATUSES = ['pending', 'confirmed', 'preparing', 'ready', 'picked_up', 'on_the_way', 'delivered', 'cancelled'];
const ALLOWED_PAYMENT_METHODS = ['bkash', 'nagad', 'rocket', 'upay', 'card', 'cod'];
const ALLOWED_PAYMENT_STATUSES = ['pending', 'initiated', 'success', 'failed', 'refunded'];
const ALLOWED_APPROVAL_STATUSES = ['pending', 'approved', 'rejected'];
const ALLOWED_RIDER_STATUSES = ['available', 'busy', 'offline'];
const ALLOWED_USER_ROLES = ['customer', 'admin'];

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
    const limit = Math.min(parseInt(req.query.limit) || 20, 100);

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
          .populate('user', 'name phone')
          .populate('restaurant', 'name')
          .populate('rider', 'name')
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
            let str = typeof val === 'object' ? JSON.stringify(val) : String(val);
            // Prevent CSV formula injection by prefixing dangerous characters
            if (/^[=+\-@\t\r]/.test(str)) {
              str = `'${str}`;
            }
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

// ──────────────────────────────────────────────────────────────
// Users Management
// ──────────────────────────────────────────────────────────────

exports.listUsers = async (req, res, next) => {
  try {
    const page = parseInt(req.query.page) || 1;
    const limit = Math.min(parseInt(req.query.limit) || 20, 100);
    const search = safeStr(req.query.search);
    const role = ALLOWED_USER_ROLES.includes(safeStr(req.query.role)) ? safeStr(req.query.role) : null;

    const query = {};
    if (search) {
      const escaped = escapeRegex(search);
      query.$or = [
        { name: { $regex: escaped, $options: 'i' } },
        { phone: { $regex: escaped, $options: 'i' } },
        { email: { $regex: escaped, $options: 'i' } },
      ];
    }
    if (role) query.role = role;

    const total = await User.countDocuments(query);
    const users = await User.find(query)
      .sort({ createdAt: -1 })
      .skip((page - 1) * limit)
      .limit(limit)
      .select('-__v');

    res.json({
      success: true,
      data: users,
      pagination: { page, limit, total, pages: Math.ceil(total / limit) },
    });
  } catch (error) {
    next(error);
  }
};

exports.getUserById = async (req, res, next) => {
  try {
    const user = await User.findById(req.params.id).select('-__v');
    if (!user) return res.status(404).json({ success: false, message: 'User not found' });

    const orderCount = await Order.countDocuments({ user: req.params.id });
    res.json({ success: true, data: { ...user.toObject(), orderCount } });
  } catch (error) {
    next(error);
  }
};

exports.updateUser = async (req, res, next) => {
  try {
    const roleInput = safeStr(req.body.role);
    const role = ALLOWED_USER_ROLES.includes(roleInput) ? roleInput : undefined;
    const { isBanned } = req.body;
    const update = {};
    if (role !== undefined) update.role = role;
    if (isBanned !== undefined) update.isBanned = Boolean(isBanned);

    const user = await User.findByIdAndUpdate(req.params.id, update, { new: true, runValidators: true }).select('-__v');
    if (!user) return res.status(404).json({ success: false, message: 'User not found' });

    res.json({ success: true, data: user });
  } catch (error) {
    next(error);
  }
};

// ──────────────────────────────────────────────────────────────
// Orders Management
// ──────────────────────────────────────────────────────────────

exports.listOrders = async (req, res, next) => {
  try {
    const page = parseInt(req.query.page) || 1;
    const limit = Math.min(parseInt(req.query.limit) || 20, 100);
    const statusRaw = safeStr(req.query.status);
    const paymentMethodRaw = safeStr(req.query.paymentMethod);
    const search = safeStr(req.query.search);

    const status = ALLOWED_ORDER_STATUSES.includes(statusRaw) ? statusRaw : null;
    const paymentMethod = ALLOWED_PAYMENT_METHODS.includes(paymentMethodRaw) ? paymentMethodRaw : null;
    const startDate = safeDate(req.query.startDate);
    const endDate = safeDate(req.query.endDate);

    const query = {};
    if (status) query.status = status;
    if (paymentMethod) query.paymentMethod = paymentMethod;
    if (startDate || endDate) {
      query.createdAt = {};
      if (startDate) query.createdAt.$gte = startDate;
      if (endDate) query.createdAt.$lte = endDate;
    }
    if (search) {
      query.$or = [
        { orderNumber: { $regex: escapeRegex(search), $options: 'i' } },
      ];
    }

    const total = await Order.countDocuments(query);
    const orders = await Order.find(query)
      .populate('user', 'name phone')
      .populate('restaurant', 'name')
      .populate('rider', 'name phone')
      .sort({ createdAt: -1 })
      .skip((page - 1) * limit)
      .limit(limit)
      .select('-__v');

    res.json({
      success: true,
      data: orders,
      pagination: { page, limit, total, pages: Math.ceil(total / limit) },
    });
  } catch (error) {
    next(error);
  }
};

exports.getOrderById = async (req, res, next) => {
  try {
    const order = await Order.findById(req.params.id)
      .populate('user', 'name phone email')
      .populate('restaurant', 'name phone address')
      .populate('rider', 'name phone')
      .populate('items.menuItem', 'name price')
      .select('-__v');

    if (!order) return res.status(404).json({ success: false, message: 'Order not found' });
    res.json({ success: true, data: order });
  } catch (error) {
    next(error);
  }
};

exports.updateOrderStatus = async (req, res, next) => {
  try {
    const statusRaw = safeStr(req.body.status);
    const status = ALLOWED_ORDER_STATUSES.includes(statusRaw) ? statusRaw : null;
    if (!status) return res.status(400).json({ success: false, message: 'Invalid status value' });

    const cancellationReason = safeStr(req.body.cancellationReason);
    const update = { status };
    if (cancellationReason) update.cancellationReason = cancellationReason;
    update.$push = { statusHistory: { status, timestamp: new Date(), note: cancellationReason } };

    const order = await Order.findByIdAndUpdate(req.params.id, update, { new: true, runValidators: true });
    if (!order) return res.status(404).json({ success: false, message: 'Order not found' });

    res.json({ success: true, data: order });
  } catch (error) {
    next(error);
  }
};

// ──────────────────────────────────────────────────────────────
// Restaurants Management
// ──────────────────────────────────────────────────────────────

exports.listRestaurants = async (req, res, next) => {
  try {
    const page = parseInt(req.query.page) || 1;
    const limit = Math.min(parseInt(req.query.limit) || 20, 100);
    const approvalStatusRaw = safeStr(req.query.approvalStatus);
    const search = safeStr(req.query.search);

    const approvalStatus = ALLOWED_APPROVAL_STATUSES.includes(approvalStatusRaw) ? approvalStatusRaw : null;

    const query = {};
    if (approvalStatus) query.approvalStatus = approvalStatus;
    if (search) {
      const escaped = escapeRegex(search);
      query.$or = [
        { name: { $regex: escaped, $options: 'i' } },
        { phone: { $regex: escaped, $options: 'i' } },
      ];
    }

    const total = await Restaurant.countDocuments(query);
    const restaurants = await Restaurant.find(query)
      .sort({ createdAt: -1 })
      .skip((page - 1) * limit)
      .limit(limit)
      .select('-__v');

    res.json({
      success: true,
      data: restaurants,
      pagination: { page, limit, total, pages: Math.ceil(total / limit) },
    });
  } catch (error) {
    next(error);
  }
};

exports.updateRestaurantStatus = async (req, res, next) => {
  try {
    const approvalStatusRaw = safeStr(req.body.approvalStatus);
    const approvalStatus = ALLOWED_APPROVAL_STATUSES.includes(approvalStatusRaw) ? approvalStatusRaw : undefined;
    const isActive = req.body.isActive !== undefined ? Boolean(req.body.isActive) : undefined;
    const rejectionReason = safeStr(req.body.rejectionReason);
    const update = {};
    if (approvalStatus !== undefined) update.approvalStatus = approvalStatus;
    if (isActive !== undefined) update.isActive = isActive;
    if (rejectionReason) update.rejectionReason = rejectionReason;

    const restaurant = await Restaurant.findByIdAndUpdate(req.params.id, update, { new: true, runValidators: true });
    if (!restaurant) return res.status(404).json({ success: false, message: 'Restaurant not found' });

    res.json({ success: true, data: restaurant });
  } catch (error) {
    next(error);
  }
};

// ──────────────────────────────────────────────────────────────
// Riders Management
// ──────────────────────────────────────────────────────────────

exports.listRiders = async (req, res, next) => {
  try {
    const page = parseInt(req.query.page) || 1;
    const limit = Math.min(parseInt(req.query.limit) || 20, 100);
    const statusRaw = safeStr(req.query.status);
    const search = safeStr(req.query.search);

    const status = ALLOWED_RIDER_STATUSES.includes(statusRaw) ? statusRaw : null;

    const query = {};
    if (status) query.status = status;
    if (search) {
      const escaped = escapeRegex(search);
      query.$or = [
        { name: { $regex: escaped, $options: 'i' } },
        { phone: { $regex: escaped, $options: 'i' } },
      ];
    }

    const total = await Rider.countDocuments(query);
    const riders = await Rider.find(query)
      .sort({ createdAt: -1 })
      .skip((page - 1) * limit)
      .limit(limit)
      .select('-__v');

    res.json({
      success: true,
      data: riders,
      pagination: { page, limit, total, pages: Math.ceil(total / limit) },
    });
  } catch (error) {
    next(error);
  }
};

// ──────────────────────────────────────────────────────────────
// Payments Management
// ──────────────────────────────────────────────────────────────

exports.listPayments = async (req, res, next) => {
  try {
    const page = parseInt(req.query.page) || 1;
    const limit = Math.min(parseInt(req.query.limit) || 20, 100);
    const methodRaw = safeStr(req.query.method);
    const statusRaw = safeStr(req.query.status);

    const method = ALLOWED_PAYMENT_METHODS.includes(methodRaw) ? methodRaw : null;
    const status = ALLOWED_PAYMENT_STATUSES.includes(statusRaw) ? statusRaw : null;
    const startDate = safeDate(req.query.startDate);
    const endDate = safeDate(req.query.endDate);

    const query = {};
    if (method) query.method = method;
    if (status) query.status = status;
    if (startDate || endDate) {
      query.createdAt = {};
      if (startDate) query.createdAt.$gte = startDate;
      if (endDate) query.createdAt.$lte = endDate;
    }

    const total = await Payment.countDocuments(query);
    const payments = await Payment.find(query)
      .populate('user', 'name phone')
      .populate('order', 'orderNumber total')
      .sort({ createdAt: -1 })
      .skip((page - 1) * limit)
      .limit(limit)
      .select('-__v');

    res.json({
      success: true,
      data: payments,
      pagination: { page, limit, total, pages: Math.ceil(total / limit) },
    });
  } catch (error) {
    next(error);
  }
};

// ──────────────────────────────────────────────────────────────
// Settings Management (persisted to MongoDB)
// ──────────────────────────────────────────────────────────────

// Allowed settings fields with type coercion
const SETTINGS_SCHEMA = {
  appName: (v) => (typeof v === 'string' ? v.slice(0, 100) : undefined),
  deliveryFee: (v) => { const n = Number(v); return Number.isFinite(n) && n >= 0 ? n : undefined; },
  adminProfitRate: (v) => { const n = Number(v); return Number.isFinite(n) && n >= 0 && n <= 100 ? n : undefined; },
  minOrderAmount: (v) => { const n = Number(v); return Number.isFinite(n) && n >= 0 ? n : undefined; },
  maintenanceMode: (v) => (typeof v === 'boolean' ? v : undefined),
  paymentMethods: (v) => {
    if (typeof v !== 'object' || v === null || Array.isArray(v)) return undefined;
    const allowed = ['bkash', 'rocket', 'upay', 'sslcommerz', 'cod'];
    const result = {};
    for (const key of allowed) {
      if (typeof v[key] === 'boolean') result[key] = v[key];
    }
    return Object.keys(result).length > 0 ? result : undefined;
  },
};

exports.getSettings = async (req, res, next) => {
  try {
    let settings = await Setting.findOne({ key: 'platform' });
    if (!settings) {
      settings = await Setting.create({
        key: 'platform',
        deliveryFee: parseInt(process.env.BASE_DELIVERY_FEE) || 20,
        adminProfitRate: parseFloat(process.env.ADMIN_PROFIT_RATE) || 5,
        minOrderAmount: parseInt(process.env.MIN_ORDER_AMOUNT) || 50,
        maintenanceMode: process.env.MAINTENANCE_MODE === 'true',
      });
    }
    res.json({ success: true, data: settings });
  } catch (error) {
    next(error);
  }
};

exports.updateSettings = async (req, res, next) => {
  try {
    // Whitelist and coerce incoming fields
    const update = {};
    for (const [field, coerce] of Object.entries(SETTINGS_SCHEMA)) {
      if (req.body[field] !== undefined) {
        const val = coerce(req.body[field]);
        if (val !== undefined) {
          if (field === 'paymentMethods') {
            // Merge payment methods instead of replacing
            for (const [k, v] of Object.entries(val)) {
              update[`paymentMethods.${k}`] = v;
            }
          } else {
            update[field] = val;
          }
        }
      }
    }

    const settings = await Setting.findOneAndUpdate(
      { key: 'platform' },
      { $set: update },
      { new: true, upsert: true, runValidators: true }
    );

    res.json({ success: true, data: settings });
  } catch (error) {
    next(error);
  }
};

module.exports = exports;
