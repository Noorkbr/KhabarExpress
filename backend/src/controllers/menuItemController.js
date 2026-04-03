const MenuItem = require('../models/MenuItem');
const Category = require('../models/Category');

// Get menu items for a restaurant
exports.getRestaurantMenuItems = async (req, res) => {
  try {
    const { categoryId } = req.query;
    
    const query = {
      restaurant: req.params.restaurantId,
      isAvailable: true,
    };

    if (categoryId) {
      query.category = categoryId;
    }

    const menuItems = await MenuItem.find(query)
      .populate('category', 'name nameBn')
      .sort({ isPopular: -1, createdAt: -1 });

    res.json({
      success: true,
      data: menuItems,
    });
  } catch (error) {
    console.error('Error fetching menu items:', error);
    res.status(500).json({
      success: false,
      message: 'Failed to fetch menu items',
      error: error.message,
    });
  }
};

// Get single menu item
exports.getMenuItem = async (req, res) => {
  try {
    const menuItem = await MenuItem.findById(req.params.id)
      .populate('category', 'name nameBn')
      .populate('restaurant', 'name nameBn logo');

    if (!menuItem) {
      return res.status(404).json({
        success: false,
        message: 'Menu item not found',
      });
    }

    res.json({
      success: true,
      data: menuItem,
    });
  } catch (error) {
    console.error('Error fetching menu item:', error);
    res.status(500).json({
      success: false,
      message: 'Failed to fetch menu item',
      error: error.message,
    });
  }
};

// Create menu item (restaurant owner only)
exports.createMenuItem = async (req, res) => {
  try {
    const {
      name,
      nameBn,
      description,
      descriptionBn,
      image,
      category,
      price,
      discountPrice,
      isVegetarian,
      isVegan,
      spiceLevel,
      allergens,
      customizations,
      prepTime,
    } = req.body;

    if (!name || !category || !price) {
      return res.status(400).json({
        success: false,
        message: 'Name, category, and price are required',
      });
    }

    // Verify category belongs to restaurant
    const categoryDoc = await Category.findOne({
      _id: category,
      restaurant: req.user.restaurantId,
    });

    if (!categoryDoc) {
      return res.status(400).json({
        success: false,
        message: 'Invalid category',
      });
    }

    const menuItem = new MenuItem({
      restaurant: req.user.restaurantId,
      name,
      nameBn,
      description,
      descriptionBn,
      image,
      category,
      categoryName: categoryDoc.name,
      price,
      discountPrice,
      isVegetarian,
      isVegan,
      spiceLevel,
      allergens,
      customizations,
      prepTime,
    });

    await menuItem.save();

    res.status(201).json({
      success: true,
      message: 'Menu item created successfully',
      data: menuItem,
    });
  } catch (error) {
    console.error('Error creating menu item:', error);
    res.status(500).json({
      success: false,
      message: 'Failed to create menu item',
      error: error.message,
    });
  }
};

// Update menu item (restaurant owner only)
exports.updateMenuItem = async (req, res) => {
  try {
    const menuItem = await MenuItem.findOne({
      _id: req.params.id,
      restaurant: req.user.restaurantId,
    });

    if (!menuItem) {
      return res.status(404).json({
        success: false,
        message: 'Menu item not found',
      });
    }

    const {
      name,
      nameBn,
      description,
      descriptionBn,
      image,
      category,
      price,
      discountPrice,
      isVegetarian,
      isVegan,
      spiceLevel,
      allergens,
      customizations,
      prepTime,
      isAvailable,
      isPopular,
    } = req.body;

    // If category is being changed, verify it belongs to restaurant
    if (category && category !== menuItem.category.toString()) {
      const categoryDoc = await Category.findOne({
        _id: category,
        restaurant: req.user.restaurantId,
      });

      if (!categoryDoc) {
        return res.status(400).json({
          success: false,
          message: 'Invalid category',
        });
      }

      menuItem.category = category;
      menuItem.categoryName = categoryDoc.name;
    }

    if (name !== undefined) menuItem.name = name;
    if (nameBn !== undefined) menuItem.nameBn = nameBn;
    if (description !== undefined) menuItem.description = description;
    if (descriptionBn !== undefined) menuItem.descriptionBn = descriptionBn;
    if (image !== undefined) menuItem.image = image;
    if (price !== undefined) menuItem.price = price;
    if (discountPrice !== undefined) menuItem.discountPrice = discountPrice;
    if (isVegetarian !== undefined) menuItem.isVegetarian = isVegetarian;
    if (isVegan !== undefined) menuItem.isVegan = isVegan;
    if (spiceLevel !== undefined) menuItem.spiceLevel = spiceLevel;
    if (allergens !== undefined) menuItem.allergens = allergens;
    if (customizations !== undefined) menuItem.customizations = customizations;
    if (prepTime !== undefined) menuItem.prepTime = prepTime;
    if (isAvailable !== undefined) menuItem.isAvailable = isAvailable;
    if (isPopular !== undefined) menuItem.isPopular = isPopular;

    await menuItem.save();

    res.json({
      success: true,
      message: 'Menu item updated successfully',
      data: menuItem,
    });
  } catch (error) {
    console.error('Error updating menu item:', error);
    res.status(500).json({
      success: false,
      message: 'Failed to update menu item',
      error: error.message,
    });
  }
};

// Toggle menu item availability (restaurant owner only)
exports.toggleAvailability = async (req, res) => {
  try {
    const menuItem = await MenuItem.findOne({
      _id: req.params.id,
      restaurant: req.user.restaurantId,
    });

    if (!menuItem) {
      return res.status(404).json({
        success: false,
        message: 'Menu item not found',
      });
    }

    menuItem.isAvailable = !menuItem.isAvailable;
    await menuItem.save();

    res.json({
      success: true,
      message: `Menu item ${menuItem.isAvailable ? 'available' : 'unavailable'}`,
      data: menuItem,
    });
  } catch (error) {
    console.error('Error toggling availability:', error);
    res.status(500).json({
      success: false,
      message: 'Failed to toggle availability',
      error: error.message,
    });
  }
};

// Delete menu item (restaurant owner only)
exports.deleteMenuItem = async (req, res) => {
  try {
    const menuItem = await MenuItem.findOne({
      _id: req.params.id,
      restaurant: req.user.restaurantId,
    });

    if (!menuItem) {
      return res.status(404).json({
        success: false,
        message: 'Menu item not found',
      });
    }

    await menuItem.deleteOne();

    res.json({
      success: true,
      message: 'Menu item deleted successfully',
    });
  } catch (error) {
    console.error('Error deleting menu item:', error);
    res.status(500).json({
      success: false,
      message: 'Failed to delete menu item',
      error: error.message,
    });
  }
};
