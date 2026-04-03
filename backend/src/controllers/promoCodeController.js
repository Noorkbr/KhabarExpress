const PromoCode = require('../models/PromoCode');
const Order = require('../models/Order');

// Create promo code (Admin only)
exports.createPromoCode = async (req, res, next) => {
  try {
    const {
      code,
      description,
      descriptionBn,
      type,
      value,
      minOrderAmount,
      maxDiscount,
      usageLimit,
      validFrom,
      validUntil,
      applicableTo,
      restaurants,
    } = req.body;

    // Check if code already exists
    const existingCode = await PromoCode.findOne({ code: code.toUpperCase() });
    if (existingCode) {
      return res.status(400).json({
        success: false,
        message: 'Promo code already exists',
      });
    }

    const promoCode = await PromoCode.create({
      code: code.toUpperCase(),
      description,
      descriptionBn,
      type,
      value,
      minOrderAmount,
      maxDiscount,
      usageLimit,
      validFrom,
      validUntil,
      applicableTo,
      restaurants,
    });

    res.status(201).json({
      success: true,
      message: 'Promo code created successfully',
      data: promoCode,
    });
  } catch (error) {
    next(error);
  }
};

// Validate and apply promo code
exports.validatePromoCode = async (req, res, next) => {
  try {
    const { code, restaurantId, orderAmount } = req.body;

    const promoCode = await PromoCode.findOne({ code: code.toUpperCase() });
    
    if (!promoCode) {
      return res.status(404).json({
        success: false,
        message: 'Invalid promo code',
      });
    }

    // Check if promo code is active
    if (!promoCode.isActive) {
      return res.status(400).json({
        success: false,
        message: 'Promo code is no longer active',
      });
    }

    // Check validity period
    const now = new Date();
    if (now < promoCode.validFrom || now > promoCode.validUntil) {
      return res.status(400).json({
        success: false,
        message: 'Promo code has expired or not yet valid',
      });
    }

    // Check minimum order amount
    if (orderAmount < promoCode.minOrderAmount) {
      return res.status(400).json({
        success: false,
        message: `Minimum order amount of ৳${promoCode.minOrderAmount / 100} required`,
      });
    }

    // Check total usage limit
    if (promoCode.usageLimit?.total && promoCode.usageCount >= promoCode.usageLimit.total) {
      return res.status(400).json({
        success: false,
        message: 'Promo code usage limit reached',
      });
    }

    // Check per-user usage limit
    const userUsageCount = await Order.countDocuments({
      userId: req.user.userId,
      promoCode: promoCode._id,
    });

    if (userUsageCount >= promoCode.usageLimit?.perUser) {
      return res.status(400).json({
        success: false,
        message: 'You have already used this promo code',
      });
    }

    // Check if applicable to first order only
    if (promoCode.applicableTo === 'first_order') {
      const userOrderCount = await Order.countDocuments({
        userId: req.user.userId,
        status: { $ne: 'cancelled' },
      });

      if (userOrderCount > 0) {
        return res.status(400).json({
          success: false,
          message: 'This promo code is only for first-time orders',
        });
      }
    }

    // Check if applicable to specific restaurants
    if (promoCode.applicableTo === 'specific_restaurants') {
      if (!promoCode.restaurants.includes(restaurantId)) {
        return res.status(400).json({
          success: false,
          message: 'This promo code is not applicable to this restaurant',
        });
      }
    }

    // Calculate discount
    let discount = 0;
    if (promoCode.type === 'percentage') {
      discount = (orderAmount * promoCode.value) / 100;
      if (promoCode.maxDiscount && discount > promoCode.maxDiscount) {
        discount = promoCode.maxDiscount;
      }
    } else if (promoCode.type === 'fixed') {
      discount = promoCode.value;
    }

    // Ensure discount doesn't exceed order amount
    discount = Math.min(discount, orderAmount);

    res.json({
      success: true,
      message: 'Promo code is valid',
      data: {
        promoCode: {
          _id: promoCode._id,
          code: promoCode.code,
          description: promoCode.description,
          type: promoCode.type,
          value: promoCode.value,
        },
        discount,
        finalAmount: orderAmount - discount,
      },
    });
  } catch (error) {
    next(error);
  }
};

// Get all promo codes (Admin)
exports.getAllPromoCodes = async (req, res, next) => {
  try {
    const { page = 1, limit = 20, isActive, type } = req.query;
    const skip = (page - 1) * limit;

    const query = {};
    if (isActive !== undefined) query.isActive = isActive === 'true';
    if (type) query.type = type;

    const promoCodes = await PromoCode.find(query)
      .populate('restaurants', 'name')
      .sort({ createdAt: -1 })
      .skip(skip)
      .limit(parseInt(limit));

    const total = await PromoCode.countDocuments(query);

    res.json({
      success: true,
      data: {
        promoCodes,
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

// Get active promo codes (User)
exports.getActivePromoCodes = async (req, res, next) => {
  try {
    const { restaurantId } = req.query;
    const now = new Date();

    const query = {
      isActive: true,
      validFrom: { $lte: now },
      validUntil: { $gte: now },
    };

    // Filter by restaurant if provided
    if (restaurantId) {
      query.$or = [
        { applicableTo: 'all' },
        { applicableTo: 'first_order' },
        { applicableTo: 'specific_restaurants', restaurants: restaurantId },
      ];
    } else {
      query.applicableTo = { $in: ['all', 'first_order'] };
    }

    const promoCodes = await PromoCode.find(query)
      .select('code description descriptionBn type value minOrderAmount maxDiscount')
      .sort({ createdAt: -1 });

    res.json({
      success: true,
      data: promoCodes,
    });
  } catch (error) {
    next(error);
  }
};

// Get promo code by ID (Admin)
exports.getPromoCodeById = async (req, res, next) => {
  try {
    const { promoId } = req.params;

    const promoCode = await PromoCode.findById(promoId).populate('restaurants', 'name');
    
    if (!promoCode) {
      return res.status(404).json({
        success: false,
        message: 'Promo code not found',
      });
    }

    res.json({
      success: true,
      data: promoCode,
    });
  } catch (error) {
    next(error);
  }
};

// Update promo code (Admin)
exports.updatePromoCode = async (req, res, next) => {
  try {
    const { promoId } = req.params;
    const updates = req.body;

    // Don't allow code to be changed
    delete updates.code;
    delete updates.usageCount;

    const promoCode = await PromoCode.findByIdAndUpdate(
      promoId,
      updates,
      { new: true, runValidators: true }
    );

    if (!promoCode) {
      return res.status(404).json({
        success: false,
        message: 'Promo code not found',
      });
    }

    res.json({
      success: true,
      message: 'Promo code updated successfully',
      data: promoCode,
    });
  } catch (error) {
    next(error);
  }
};

// Toggle promo code status (Admin)
exports.togglePromoCodeStatus = async (req, res, next) => {
  try {
    const { promoId } = req.params;

    const promoCode = await PromoCode.findById(promoId);
    
    if (!promoCode) {
      return res.status(404).json({
        success: false,
        message: 'Promo code not found',
      });
    }

    promoCode.isActive = !promoCode.isActive;
    await promoCode.save();

    res.json({
      success: true,
      message: `Promo code ${promoCode.isActive ? 'activated' : 'deactivated'} successfully`,
      data: promoCode,
    });
  } catch (error) {
    next(error);
  }
};

// Delete promo code (Admin)
exports.deletePromoCode = async (req, res, next) => {
  try {
    const { promoId } = req.params;

    const promoCode = await PromoCode.findByIdAndDelete(promoId);
    
    if (!promoCode) {
      return res.status(404).json({
        success: false,
        message: 'Promo code not found',
      });
    }

    res.json({
      success: true,
      message: 'Promo code deleted successfully',
    });
  } catch (error) {
    next(error);
  }
};

// Get promo code statistics (Admin)
exports.getPromoCodeStats = async (req, res, next) => {
  try {
    const { promoId } = req.params;

    const promoCode = await PromoCode.findById(promoId);
    
    if (!promoCode) {
      return res.status(404).json({
        success: false,
        message: 'Promo code not found',
      });
    }

    // Get orders using this promo code
    const orders = await Order.find({ promoCode: promoId, status: { $ne: 'cancelled' } });

    const stats = {
      totalUsage: orders.length,
      totalDiscount: orders.reduce((sum, order) => sum + (order.discount || 0), 0),
      totalRevenue: orders.reduce((sum, order) => sum + order.totalAmount, 0),
      uniqueUsers: new Set(orders.map(order => order.userId.toString())).size,
    };

    res.json({
      success: true,
      data: {
        promoCode,
        stats,
      },
    });
  } catch (error) {
    next(error);
  }
};

module.exports = exports;
