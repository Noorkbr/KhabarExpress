const Review = require('../models/Review');
const Order = require('../models/Order');
const Restaurant = require('../models/Restaurant');
const Rider = require('../models/Rider');
const mongoose = require('mongoose');

// Create review
exports.createReview = async (req, res, next) => {
  try {
    const { orderId, foodRating, deliveryRating, review, reviewBn, images } = req.body;

    // Check if order exists and belongs to user
    const order = await Order.findById(orderId);
    if (!order) {
      return res.status(404).json({
        success: false,
        message: 'Order not found',
      });
    }

    if (order.userId.toString() !== req.user.userId) {
      return res.status(403).json({
        success: false,
        message: 'Not authorized to review this order',
      });
    }

    if (order.status !== 'delivered') {
      return res.status(400).json({
        success: false,
        message: 'Can only review delivered orders',
      });
    }

    // Check if review already exists
    const existingReview = await Review.findOne({ order: orderId });
    if (existingReview) {
      return res.status(400).json({
        success: false,
        message: 'Review already exists for this order',
      });
    }

    // Create review
    const newReview = await Review.create({
      order: orderId,
      user: req.user.userId,
      restaurant: order.restaurantId,
      rider: order.riderId,
      foodRating,
      deliveryRating,
      review,
      reviewBn,
      images,
    });

    // Update restaurant rating
    const restaurantReviews = await Review.find({ restaurant: order.restaurantId });
    const avgRating = restaurantReviews.reduce((sum, r) => sum + r.foodRating, 0) / restaurantReviews.length;
    await Restaurant.findByIdAndUpdate(order.restaurantId, {
      rating: avgRating,
      totalReviews: restaurantReviews.length,
    });

    // Update rider rating if delivery rating provided
    if (deliveryRating && order.riderId) {
      const riderReviews = await Review.find({ rider: order.riderId, deliveryRating: { $exists: true } });
      const avgDeliveryRating = riderReviews.reduce((sum, r) => sum + r.deliveryRating, 0) / riderReviews.length;
      await Rider.findByIdAndUpdate(order.riderId, {
        rating: avgDeliveryRating,
        totalReviews: riderReviews.length,
      });
    }

    res.status(201).json({
      success: true,
      message: 'Review submitted successfully',
      data: newReview,
    });
  } catch (error) {
    next(error);
  }
};

// Get reviews for restaurant
exports.getRestaurantReviews = async (req, res, next) => {
  try {
    const { restaurantId } = req.params;
    const { page = 1, limit = 20, rating } = req.query;
    const skip = (page - 1) * limit;

    const query = { restaurant: restaurantId, isPublished: true };
    if (rating) query.foodRating = parseInt(rating);

    const reviews = await Review.find(query)
      .populate('user', 'name profileImage')
      .populate('order', 'orderNumber')
      .sort({ createdAt: -1 })
      .skip(skip)
      .limit(parseInt(limit));

    const total = await Review.countDocuments(query);

    // Get rating distribution
    const distribution = await Review.aggregate([
      { $match: { restaurant: new mongoose.Types.ObjectId(restaurantId), isPublished: true } },
      { $group: { _id: '$foodRating', count: { $sum: 1 } } },
      { $sort: { _id: -1 } },
    ]);

    res.json({
      success: true,
      data: {
        reviews,
        distribution,
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

// Get reviews by user
exports.getUserReviews = async (req, res, next) => {
  try {
    const { page = 1, limit = 20 } = req.query;
    const skip = (page - 1) * limit;

    const reviews = await Review.find({ user: req.user.userId })
      .populate('restaurant', 'name logo')
      .populate('order', 'orderNumber totalAmount')
      .sort({ createdAt: -1 })
      .skip(skip)
      .limit(parseInt(limit));

    const total = await Review.countDocuments({ user: req.user.userId });

    res.json({
      success: true,
      data: {
        reviews,
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

// Update review
exports.updateReview = async (req, res, next) => {
  try {
    const { reviewId } = req.params;
    const { foodRating, deliveryRating, review, reviewBn, images } = req.body;

    const existingReview = await Review.findById(reviewId);
    if (!existingReview) {
      return res.status(404).json({
        success: false,
        message: 'Review not found',
      });
    }

    if (existingReview.user.toString() !== req.user.userId) {
      return res.status(403).json({
        success: false,
        message: 'Not authorized to update this review',
      });
    }

    // Update review
    if (foodRating !== undefined) existingReview.foodRating = foodRating;
    if (deliveryRating !== undefined) existingReview.deliveryRating = deliveryRating;
    if (review !== undefined) existingReview.review = review;
    if (reviewBn !== undefined) existingReview.reviewBn = reviewBn;
    if (images !== undefined) existingReview.images = images;

    await existingReview.save();

    // Recalculate restaurant rating
    const restaurantReviews = await Review.find({ restaurant: existingReview.restaurant });
    const avgRating = restaurantReviews.reduce((sum, r) => sum + r.foodRating, 0) / restaurantReviews.length;
    await Restaurant.findByIdAndUpdate(existingReview.restaurant, {
      rating: avgRating,
    });

    // Recalculate rider rating if applicable
    if (existingReview.rider && deliveryRating !== undefined) {
      const riderReviews = await Review.find({ rider: existingReview.rider, deliveryRating: { $exists: true } });
      const avgDeliveryRating = riderReviews.reduce((sum, r) => sum + r.deliveryRating, 0) / riderReviews.length;
      await Rider.findByIdAndUpdate(existingReview.rider, {
        rating: avgDeliveryRating,
      });
    }

    res.json({
      success: true,
      message: 'Review updated successfully',
      data: existingReview,
    });
  } catch (error) {
    next(error);
  }
};

// Delete review
exports.deleteReview = async (req, res, next) => {
  try {
    const { reviewId } = req.params;

    const review = await Review.findById(reviewId);
    if (!review) {
      return res.status(404).json({
        success: false,
        message: 'Review not found',
      });
    }

    if (review.user.toString() !== req.user.userId) {
      return res.status(403).json({
        success: false,
        message: 'Not authorized to delete this review',
      });
    }

    await review.deleteOne();

    // Recalculate restaurant rating
    const restaurantReviews = await Review.find({ restaurant: review.restaurant });
    const avgRating = restaurantReviews.length > 0
      ? restaurantReviews.reduce((sum, r) => sum + r.foodRating, 0) / restaurantReviews.length
      : 0;
    
    await Restaurant.findByIdAndUpdate(review.restaurant, {
      rating: avgRating,
      totalReviews: restaurantReviews.length,
    });

    // Recalculate rider rating if applicable
    if (review.rider && review.deliveryRating) {
      const riderReviews = await Review.find({ rider: review.rider, deliveryRating: { $exists: true } });
      const avgDeliveryRating = riderReviews.length > 0
        ? riderReviews.reduce((sum, r) => sum + r.deliveryRating, 0) / riderReviews.length
        : 0;
      
      await Rider.findByIdAndUpdate(review.rider, {
        rating: avgDeliveryRating,
        totalReviews: riderReviews.length,
      });
    }

    res.json({
      success: true,
      message: 'Review deleted successfully',
    });
  } catch (error) {
    next(error);
  }
};

// Restaurant owner: Respond to review
exports.respondToReview = async (req, res, next) => {
  try {
    const { reviewId } = req.params;
    const { response } = req.body;

    const review = await Review.findById(reviewId);
    if (!review) {
      return res.status(404).json({
        success: false,
        message: 'Review not found',
      });
    }

    // Check if user owns the restaurant
    const restaurant = await Restaurant.findById(review.restaurant);
    if (restaurant.ownerId.toString() !== req.user.userId) {
      return res.status(403).json({
        success: false,
        message: 'Not authorized to respond to this review',
      });
    }

    review.response = {
      text: response,
      timestamp: new Date(),
    };

    await review.save();

    res.json({
      success: true,
      message: 'Response added successfully',
      data: review,
    });
  } catch (error) {
    next(error);
  }
};

// Admin: Toggle review visibility
exports.toggleReviewVisibility = async (req, res, next) => {
  try {
    const { reviewId } = req.params;

    const review = await Review.findById(reviewId);
    if (!review) {
      return res.status(404).json({
        success: false,
        message: 'Review not found',
      });
    }

    review.isPublished = !review.isPublished;
    await review.save();

    res.json({
      success: true,
      message: `Review ${review.isPublished ? 'published' : 'hidden'} successfully`,
      data: review,
    });
  } catch (error) {
    next(error);
  }
};

// Admin: Get all reviews
exports.getAllReviews = async (req, res, next) => {
  try {
    const { page = 1, limit = 20, restaurant, user, rating } = req.query;
    const skip = (page - 1) * limit;

    const query = {};
    if (restaurant) query.restaurant = restaurant;
    if (user) query.user = user;
    if (rating) query.foodRating = parseInt(rating);

    const reviews = await Review.find(query)
      .populate('user', 'name phone')
      .populate('restaurant', 'name')
      .populate('order', 'orderNumber')
      .sort({ createdAt: -1 })
      .skip(skip)
      .limit(parseInt(limit));

    const total = await Review.countDocuments(query);

    res.json({
      success: true,
      data: {
        reviews,
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
