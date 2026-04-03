const Order = require('../models/Order');
const Restaurant = require('../models/Restaurant');
const MenuItem = require('../models/MenuItem');
const { getIO } = require('../config/socket');

// Create order
exports.createOrder = async (req, res, next) => {
  try {
    const { restaurantId, items, deliveryAddress, paymentMethod, specialInstructions, promoCode } = req.body;

    // Validate restaurant
    const restaurant = await Restaurant.findById(restaurantId);
    if (!restaurant || !restaurant.isActive) {
      return res.status(400).json({
        success: false,
        message: 'Restaurant not available',
      });
    }

    // Calculate totals
    let subtotal = 0;
    const orderItems = [];

    for (const item of items) {
      const menuItem = await MenuItem.findById(item.menuItemId);
      if (!menuItem || !menuItem.isAvailable) {
        return res.status(400).json({
          success: false,
          message: `Item ${menuItem?.name || 'unknown'} is not available`,
        });
      }

      let itemPrice = menuItem.price;
      const customizations = [];

      // Calculate customization costs
      if (item.customizations) {
        for (const custom of item.customizations) {
          const customOption = menuItem.customizations
            .find(c => c.name === custom.name)
            ?.options.find(o => o.name === custom.option);
          
          if (customOption) {
            itemPrice += customOption.price;
            customizations.push({
              name: custom.name,
              option: custom.option,
              price: customOption.price,
            });
          }
        }
      }

      const itemTotal = itemPrice * item.quantity;
      subtotal += itemTotal;

      orderItems.push({
        menuItem: menuItem._id,
        name: menuItem.name,
        quantity: item.quantity,
        price: itemPrice,
        customizations,
        specialInstructions: item.specialInstructions,
      });
    }

    // Calculate VAT (5% in Bangladesh)
    const vat = Math.round(subtotal * 0.05);

    // Calculate delivery fee (simplified - based on distance)
    const deliveryFee = 5000; // 50 BDT in paisa

    // Apply promo code discount (TODO: implement promo code validation)
    const discount = 0;

    const total = subtotal + vat + deliveryFee - discount;

    // Create order
    const order = await Order.create({
      user: req.userId,
      restaurant: restaurantId,
      items: orderItems,
      deliveryAddress,
      subtotal,
      deliveryFee,
      vat,
      discount,
      total,
      paymentMethod,
      specialInstructions,
      statusHistory: [{
        status: 'pending',
        timestamp: new Date(),
        note: 'Order placed',
      }],
    });

    // Populate order details
    await order.populate('restaurant', 'name logo phone');

    // Emit real-time event
    try {
      const io = getIO();
      io.of('/order').to(`order:${order._id}`).emit('order:created', {
        orderId: order._id,
        status: order.status,
      });
    } catch (err) {
      console.error('Socket emit error:', err);
    }

    res.status(201).json({
      success: true,
      message: 'Order created successfully',
      data: { order },
    });
  } catch (error) {
    next(error);
  }
};

// Get order by ID
exports.getOrderById = async (req, res, next) => {
  try {
    const { id } = req.params;

    const order = await Order.findById(id)
      .populate('restaurant', 'name logo phone address')
      .populate('rider', 'name phone profileImage rating')
      .populate('items.menuItem', 'name image');

    if (!order) {
      return res.status(404).json({
        success: false,
        message: 'Order not found',
      });
    }

    // Check if user owns this order
    if (order.user.toString() !== req.userId.toString()) {
      return res.status(403).json({
        success: false,
        message: 'Access denied',
      });
    }

    res.status(200).json({
      success: true,
      data: { order },
    });
  } catch (error) {
    next(error);
  }
};

// Get user orders
exports.getUserOrders = async (req, res, next) => {
  try {
    const { page = 1, limit = 10, status } = req.query;

    const query = { user: req.userId };
    if (status) {
      query.status = status;
    }

    const orders = await Order.find(query)
      .sort({ createdAt: -1 })
      .limit(parseInt(limit))
      .skip((parseInt(page) - 1) * parseInt(limit))
      .populate('restaurant', 'name logo rating')
      .select('-items.menuItem -statusHistory');

    const total = await Order.countDocuments(query);

    res.status(200).json({
      success: true,
      data: {
        orders,
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

// Cancel order
exports.cancelOrder = async (req, res, next) => {
  try {
    const { id } = req.params;
    const { reason } = req.body;

    const order = await Order.findById(id);

    if (!order) {
      return res.status(404).json({
        success: false,
        message: 'Order not found',
      });
    }

    // Check ownership
    if (order.user.toString() !== req.userId.toString()) {
      return res.status(403).json({
        success: false,
        message: 'Access denied',
      });
    }

    // Check if order can be cancelled
    if (['delivered', 'cancelled', 'on_the_way'].includes(order.status)) {
      return res.status(400).json({
        success: false,
        message: 'Order cannot be cancelled',
      });
    }

    order.status = 'cancelled';
    order.cancellationReason = reason;
    order.statusHistory.push({
      status: 'cancelled',
      timestamp: new Date(),
      note: reason,
    });

    await order.save();

    // Emit real-time event
    try {
      const io = getIO();
      io.of('/order').to(`order:${order._id}`).emit('order:cancelled', {
        orderId: order._id,
        status: order.status,
      });
    } catch (err) {
      console.error('Socket emit error:', err);
    }

    res.status(200).json({
      success: true,
      message: 'Order cancelled successfully',
      data: { order },
    });
  } catch (error) {
    next(error);
  }
};

// Track order (real-time updates handled via Socket.IO)
exports.trackOrder = async (req, res, next) => {
  try {
    const { id } = req.params;

    const order = await Order.findById(id)
      .populate('restaurant', 'name logo address location')
      .populate('rider', 'name phone currentLocation')
      .select('status estimatedDelivery actualDelivery statusHistory deliveryAddress');

    if (!order) {
      return res.status(404).json({
        success: false,
        message: 'Order not found',
      });
    }

    res.status(200).json({
      success: true,
      data: { order },
    });
  } catch (error) {
    next(error);
  }
};
