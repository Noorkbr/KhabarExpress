const Category = require('../models/Category');
const MenuItem = require('../models/MenuItem');

// Get all categories for a restaurant
exports.getRestaurantCategories = async (req, res) => {
  try {
    const categories = await Category.find({
      restaurant: req.params.restaurantId,
      isActive: true,
    }).sort({ order: 1 });

    res.json({
      success: true,
      data: categories,
    });
  } catch (error) {
    console.error('Error fetching categories:', error);
    res.status(500).json({
      success: false,
      message: 'Failed to fetch categories',
      error: error.message,
    });
  }
};

// Create new category (restaurant owner only)
exports.createCategory = async (req, res) => {
  try {
    const { name, nameBn, description, descriptionBn, image, order } = req.body;

    if (!name) {
      return res.status(400).json({
        success: false,
        message: 'Category name is required',
      });
    }

    const category = new Category({
      restaurant: req.user.restaurantId, // From auth middleware
      name,
      nameBn,
      description,
      descriptionBn,
      image,
      order,
    });

    await category.save();

    res.status(201).json({
      success: true,
      message: 'Category created successfully',
      data: category,
    });
  } catch (error) {
    console.error('Error creating category:', error);
    res.status(500).json({
      success: false,
      message: 'Failed to create category',
      error: error.message,
    });
  }
};

// Update category (restaurant owner only)
exports.updateCategory = async (req, res) => {
  try {
    const { name, nameBn, description, descriptionBn, image, order, isActive } = req.body;

    const category = await Category.findOne({
      _id: req.params.id,
      restaurant: req.user.restaurantId,
    });

    if (!category) {
      return res.status(404).json({
        success: false,
        message: 'Category not found',
      });
    }

    if (name !== undefined) category.name = name;
    if (nameBn !== undefined) category.nameBn = nameBn;
    if (description !== undefined) category.description = description;
    if (descriptionBn !== undefined) category.descriptionBn = descriptionBn;
    if (image !== undefined) category.image = image;
    if (order !== undefined) category.order = order;
    if (isActive !== undefined) category.isActive = isActive;

    await category.save();

    res.json({
      success: true,
      message: 'Category updated successfully',
      data: category,
    });
  } catch (error) {
    console.error('Error updating category:', error);
    res.status(500).json({
      success: false,
      message: 'Failed to update category',
      error: error.message,
    });
  }
};

// Delete category (restaurant owner only)
exports.deleteCategory = async (req, res) => {
  try {
    const category = await Category.findOne({
      _id: req.params.id,
      restaurant: req.user.restaurantId,
    });

    if (!category) {
      return res.status(404).json({
        success: false,
        message: 'Category not found',
      });
    }

    // Check if any menu items are in this category
    const itemsInCategory = await MenuItem.countDocuments({
      category: req.params.id,
    });

    if (itemsInCategory > 0) {
      return res.status(400).json({
        success: false,
        message: `Cannot delete category. ${itemsInCategory} menu item(s) are in this category`,
      });
    }

    await category.deleteOne();

    res.json({
      success: true,
      message: 'Category deleted successfully',
    });
  } catch (error) {
    console.error('Error deleting category:', error);
    res.status(500).json({
      success: false,
      message: 'Failed to delete category',
      error: error.message,
    });
  }
};

// Reorder categories (restaurant owner only)
exports.reorderCategories = async (req, res) => {
  try {
    const { categoryIds } = req.body; // Array of category IDs in new order

    if (!Array.isArray(categoryIds)) {
      return res.status(400).json({
        success: false,
        message: 'categoryIds must be an array',
      });
    }

    // Update order for each category
    const updatePromises = categoryIds.map((categoryId, index) =>
      Category.findOneAndUpdate(
        { _id: categoryId, restaurant: req.user.restaurantId },
        { order: index },
        { new: true }
      )
    );

    await Promise.all(updatePromises);

    res.json({
      success: true,
      message: 'Categories reordered successfully',
    });
  } catch (error) {
    console.error('Error reordering categories:', error);
    res.status(500).json({
      success: false,
      message: 'Failed to reorder categories',
      error: error.message,
    });
  }
};
