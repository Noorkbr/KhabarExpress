const Restaurant = require('../models/Restaurant');
const MenuItem = require('../models/MenuItem');

// Get restaurants (with filters and pagination)
exports.getRestaurants = async (req, res, next) => {
  try {
    const { 
      lat, 
      lng, 
      radius = 10, // km
      cuisine, 
      search, 
      minRating,
      page = 1,
      limit = 20,
      sortBy = 'rating' // rating, deliveryTime, distance
    } = req.query;

    const query = { isActive: true };

    // Search by name or cuisine
    if (search) {
      query.$or = [
        { name: { $regex: search, $options: 'i' } },
        { nameBn: { $regex: search, $options: 'i' } },
        { cuisines: { $regex: search, $options: 'i' } },
      ];
    }

    // Filter by cuisine
    if (cuisine) {
      query.cuisines = cuisine;
    }

    // Filter by minimum rating
    if (minRating) {
      query.rating = { $gte: parseFloat(minRating) };
    }

    // Geospatial query if location provided
    if (lat && lng) {
      query.location = {
        $near: {
          $geometry: {
            type: 'Point',
            coordinates: [parseFloat(lng), parseFloat(lat)],
          },
          $maxDistance: parseInt(radius) * 1000, // Convert km to meters
        },
      };
    }

    let sortOptions = {};
    if (sortBy === 'rating') sortOptions = { rating: -1, totalReviews: -1 };
    if (sortBy === 'deliveryTime') sortOptions = { 'deliveryTime.min': 1 };

    const restaurants = await Restaurant.find(query)
      .sort(sortOptions)
      .limit(parseInt(limit))
      .skip((parseInt(page) - 1) * parseInt(limit))
      .select('-__v');

    const total = await Restaurant.countDocuments(query);

    res.status(200).json({
      success: true,
      data: {
        restaurants,
        pagination: {
          page: parseInt(page),
          limit: parseInt(limit),
          total,
          pages: Math.ceil(total / parseInt(limit)),
        },
      },
    });
  } catch (error) {
    next(error);
  }
};

// Get restaurant by ID with menu
exports.getRestaurantById = async (req, res, next) => {
  try {
    const { id } = req.params;

    const restaurant = await Restaurant.findById(id).select('-__v');

    if (!restaurant) {
      return res.status(404).json({
        success: false,
        message: 'Restaurant not found',
      });
    }

    // Get menu items
    const menuItems = await MenuItem.find({ 
      restaurant: id, 
      isAvailable: true 
    }).select('-__v');

    // Group menu items by category
    const menuByCategory = menuItems.reduce((acc, item) => {
      if (!acc[item.category]) {
        acc[item.category] = [];
      }
      acc[item.category].push(item);
      return acc;
    }, {});

    res.status(200).json({
      success: true,
      data: {
        restaurant,
        menu: menuByCategory,
      },
    });
  } catch (error) {
    next(error);
  }
};

// Get featured restaurants
exports.getFeaturedRestaurants = async (req, res, next) => {
  try {
    const restaurants = await Restaurant.find({ 
      isActive: true, 
      featured: true 
    })
      .sort({ rating: -1 })
      .limit(10)
      .select('-__v');

    res.status(200).json({
      success: true,
      data: { restaurants },
    });
  } catch (error) {
    next(error);
  }
};

// Search restaurants and menu items
exports.searchAll = async (req, res, next) => {
  try {
    const { query: searchQuery } = req.query;

    if (!searchQuery) {
      return res.status(400).json({
        success: false,
        message: 'Search query is required',
      });
    }

    // Search restaurants
    const restaurants = await Restaurant.find({
      isActive: true,
      $or: [
        { name: { $regex: searchQuery, $options: 'i' } },
        { nameBn: { $regex: searchQuery, $options: 'i' } },
        { cuisines: { $regex: searchQuery, $options: 'i' } },
      ],
    }).limit(10);

    // Search menu items
    const menuItems = await MenuItem.find({
      isAvailable: true,
      $or: [
        { name: { $regex: searchQuery, $options: 'i' } },
        { nameBn: { $regex: searchQuery, $options: 'i' } },
        { description: { $regex: searchQuery, $options: 'i' } },
        { categoryName: { $regex: searchQuery, $options: 'i' } },
      ],
    })
      .populate('restaurant', 'name logo rating')
      .limit(20);

    res.status(200).json({
      success: true,
      data: {
        restaurants,
        menuItems,
      },
    });
  } catch (error) {
    next(error);
  }
};

// Restaurant self-service registration
exports.registerRestaurant = async (req, res) => {
  try {
    const {
      name,
      nameBn,
      phone,
      email,
      location,
      address,
      cuisines,
      category,
      deliveryRadius,
      businessHours,
      documents,
      payoutMethod,
    } = req.body;

    if (!name || !phone || !location || !address) {
      return res.status(400).json({
        success: false,
        message: 'Name, phone, location, and address are required',
      });
    }

    const restaurant = new Restaurant({
      name,
      nameBn,
      phone,
      email,
      location,
      address,
      cuisines,
      category,
      deliveryRadius,
      businessHours,
      documents,
      payoutMethod,
      approvalStatus: 'pending',
    });

    await restaurant.save();

    res.status(201).json({
      success: true,
      message: 'Restaurant registered successfully. Awaiting approval.',
      data: restaurant,
    });
  } catch (error) {
    console.error('Error registering restaurant:', error);
    res.status(500).json({
      success: false,
      message: 'Failed to register restaurant',
      error: error.message,
    });
  }
};

// Update restaurant profile
exports.updateProfile = async (req, res) => {
  try {
    const restaurant = await Restaurant.findById(req.user.restaurantId);

    if (!restaurant) {
      return res.status(404).json({
        success: false,
        message: 'Restaurant not found',
      });
    }

    const {
      name,
      nameBn,
      description,
      descriptionBn,
      phone,
      email,
      cuisines,
      category,
      coverImage,
      logo,
      deliveryRadius,
      minOrderAmount,
      payoutMethod,
    } = req.body;

    if (name) restaurant.name = name;
    if (nameBn) restaurant.nameBn = nameBn;
    if (description) restaurant.description = description;
    if (descriptionBn) restaurant.descriptionBn = descriptionBn;
    if (phone) restaurant.phone = phone;
    if (email) restaurant.email = email;
    if (cuisines) restaurant.cuisines = cuisines;
    if (category) restaurant.category = category;
    if (coverImage) restaurant.coverImage = coverImage;
    if (logo) restaurant.logo = logo;
    if (deliveryRadius) restaurant.deliveryRadius = deliveryRadius;
    if (minOrderAmount !== undefined) restaurant.minOrderAmount = minOrderAmount;
    if (payoutMethod) restaurant.payoutMethod = payoutMethod;

    await restaurant.save();

    res.json({
      success: true,
      message: 'Profile updated successfully',
      data: restaurant,
    });
  } catch (error) {
    console.error('Error updating profile:', error);
    res.status(500).json({
      success: false,
      message: 'Failed to update profile',
      error: error.message,
    });
  }
};

// Update restaurant location
exports.updateLocation = async (req, res) => {
  try {
    const restaurant = await Restaurant.findById(req.user.restaurantId);

    if (!restaurant) {
      return res.status(404).json({
        success: false,
        message: 'Restaurant not found',
      });
    }

    const { location, address } = req.body;

    if (location) restaurant.location = location;
    if (address) restaurant.address = address;

    await restaurant.save();

    res.json({
      success: true,
      message: 'Location updated successfully',
      data: restaurant,
    });
  } catch (error) {
    console.error('Error updating location:', error);
    res.status(500).json({
      success: false,
      message: 'Failed to update location',
      error: error.message,
    });
  }
};

// Update business hours
exports.updateBusinessHours = async (req, res) => {
  try {
    const restaurant = await Restaurant.findById(req.user.restaurantId);

    if (!restaurant) {
      return res.status(404).json({
        success: false,
        message: 'Restaurant not found',
      });
    }

    const { businessHours } = req.body;

    if (businessHours) {
      restaurant.businessHours = businessHours;
      await restaurant.save();
    }

    res.json({
      success: true,
      message: 'Business hours updated successfully',
      data: restaurant,
    });
  } catch (error) {
    console.error('Error updating business hours:', error);
    res.status(500).json({
      success: false,
      message: 'Failed to update business hours',
      error: error.message,
    });
  }
};

// Toggle restaurant open/close status
exports.toggleOpenStatus = async (req, res) => {
  try {
    const restaurant = await Restaurant.findById(req.user.restaurantId);

    if (!restaurant) {
      return res.status(404).json({
        success: false,
        message: 'Restaurant not found',
      });
    }

    restaurant.isOpen = !restaurant.isOpen;
    await restaurant.save();

    res.json({
      success: true,
      message: `Restaurant is now ${restaurant.isOpen ? 'open' : 'closed'}`,
      data: { isOpen: restaurant.isOpen },
    });
  } catch (error) {
    console.error('Error toggling status:', error);
    res.status(500).json({
      success: false,
      message: 'Failed to toggle status',
      error: error.message,
    });
  }
};

// Get restaurant analytics
exports.getAnalytics = async (req, res) => {
  try {
    const Order = require('../models/Order');
    const { period = 'today' } = req.query;

    let startDate = new Date();
    if (period === 'today') {
      startDate.setHours(0, 0, 0, 0);
    } else if (period === 'week') {
      startDate.setDate(startDate.getDate() - 7);
    } else if (period === 'month') {
      startDate.setMonth(startDate.getMonth() - 1);
    }

    const orders = await Order.find({
      restaurant: req.user.restaurantId,
      createdAt: { $gte: startDate },
    });

    const totalOrders = orders.length;
    const totalRevenue = orders.reduce((sum, order) => sum + (order.total - order.deliveryFee), 0);
    const averageOrderValue = totalOrders > 0 ? totalRevenue / totalOrders : 0;

    res.json({
      success: true,
      data: {
        totalOrders,
        totalRevenue,
        averageOrderValue,
        period,
      },
    });
  } catch (error) {
    console.error('Error getting analytics:', error);
    res.status(500).json({
      success: false,
      message: 'Failed to get analytics',
      error: error.message,
    });
  }
};

// Admin: Get all restaurants
exports.getAllRestaurants = async (req, res) => {
  try {
    const { page = 1, limit = 20, status } = req.query;
    const query = {};

    if (status) {
      query.approvalStatus = status;
    }

    const restaurants = await Restaurant.find(query)
      .sort({ createdAt: -1 })
      .limit(parseInt(limit))
      .skip((parseInt(page) - 1) * parseInt(limit));

    const total = await Restaurant.countDocuments(query);

    res.json({
      success: true,
      data: {
        restaurants,
        pagination: {
          page: parseInt(page),
          limit: parseInt(limit),
          total,
          pages: Math.ceil(total / parseInt(limit)),
        },
      },
    });
  } catch (error) {
    console.error('Error getting restaurants:', error);
    res.status(500).json({
      success: false,
      message: 'Failed to get restaurants',
      error: error.message,
    });
  }
};

// Admin: Get pending restaurants
exports.getPendingRestaurants = async (req, res) => {
  try {
    const restaurants = await Restaurant.find({
      approvalStatus: 'pending',
    }).sort({ createdAt: -1 });

    res.json({
      success: true,
      data: restaurants,
    });
  } catch (error) {
    console.error('Error getting pending restaurants:', error);
    res.status(500).json({
      success: false,
      message: 'Failed to get pending restaurants',
      error: error.message,
    });
  }
};

// Admin: Approve restaurant
exports.approveRestaurant = async (req, res) => {
  try {
    const restaurant = await Restaurant.findById(req.params.id);

    if (!restaurant) {
      return res.status(404).json({
        success: false,
        message: 'Restaurant not found',
      });
    }

    restaurant.approvalStatus = 'approved';
    restaurant.isActive = true;
    await restaurant.save();

    res.json({
      success: true,
      message: 'Restaurant approved successfully',
      data: restaurant,
    });
  } catch (error) {
    console.error('Error approving restaurant:', error);
    res.status(500).json({
      success: false,
      message: 'Failed to approve restaurant',
      error: error.message,
    });
  }
};

// Admin: Reject restaurant
exports.rejectRestaurant = async (req, res) => {
  try {
    const { reason } = req.body;
    const restaurant = await Restaurant.findById(req.params.id);

    if (!restaurant) {
      return res.status(404).json({
        success: false,
        message: 'Restaurant not found',
      });
    }

    restaurant.approvalStatus = 'rejected';
    restaurant.rejectionReason = reason;
    restaurant.isActive = false;
    await restaurant.save();

    res.json({
      success: true,
      message: 'Restaurant rejected',
      data: restaurant,
    });
  } catch (error) {
    console.error('Error rejecting restaurant:', error);
    res.status(500).json({
      success: false,
      message: 'Failed to reject restaurant',
      error: error.message,
    });
  }
};

// Admin: Suspend restaurant
exports.suspendRestaurant = async (req, res) => {
  try {
    const restaurant = await Restaurant.findById(req.params.id);

    if (!restaurant) {
      return res.status(404).json({
        success: false,
        message: 'Restaurant not found',
      });
    }

    restaurant.isActive = !restaurant.isActive;
    await restaurant.save();

    res.json({
      success: true,
      message: `Restaurant ${restaurant.isActive ? 'activated' : 'suspended'}`,
      data: restaurant,
    });
  } catch (error) {
    console.error('Error suspending restaurant:', error);
    res.status(500).json({
      success: false,
      message: 'Failed to suspend restaurant',
      error: error.message,
    });
  }
};
