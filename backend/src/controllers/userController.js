const User = require('../models/User');

// Get user profile
exports.getProfile = async (req, res, next) => {
  try {
    const user = await User.findById(req.userId)
      .populate('favorites', 'name logo rating cuisines')
      .select('-__v');

    res.status(200).json({
      success: true,
      data: { user },
    });
  } catch (error) {
    next(error);
  }
};

// Update user profile
exports.updateProfile = async (req, res, next) => {
  try {
    const { name, nameBn, email, preferredLanguage, profileImage } = req.body;

    const user = await User.findById(req.userId);

    if (!user) {
      return res.status(404).json({
        success: false,
        message: 'User not found',
      });
    }

    // Update fields
    if (name) user.name = name;
    if (nameBn) user.nameBn = nameBn;
    if (email) user.email = email;
    if (preferredLanguage) user.preferredLanguage = preferredLanguage;
    if (profileImage) user.profileImage = profileImage;

    await user.save();

    res.status(200).json({
      success: true,
      message: 'Profile updated successfully',
      data: { user },
    });
  } catch (error) {
    next(error);
  }
};

// Add address
exports.addAddress = async (req, res, next) => {
  try {
    const { label, houseNo, roadNo, area, thana, district, division, postalCode, landmark, coordinates, isDefault } = req.body;

    const user = await User.findById(req.userId);

    if (!user) {
      return res.status(404).json({
        success: false,
        message: 'User not found',
      });
    }

    // If this is set as default, unset other defaults
    if (isDefault) {
      user.addresses.forEach(addr => {
        addr.isDefault = false;
      });
    }

    // Add new address
    user.addresses.push({
      label,
      houseNo,
      roadNo,
      area,
      thana,
      district,
      division,
      postalCode,
      landmark,
      location: {
        type: 'Point',
        coordinates, // [longitude, latitude]
      },
      isDefault: isDefault || user.addresses.length === 0,
    });

    await user.save();

    res.status(201).json({
      success: true,
      message: 'Address added successfully',
      data: { addresses: user.addresses },
    });
  } catch (error) {
    next(error);
  }
};

// Update address
exports.updateAddress = async (req, res, next) => {
  try {
    const { addressId } = req.params;
    const updates = req.body;

    const user = await User.findById(req.userId);

    if (!user) {
      return res.status(404).json({
        success: false,
        message: 'User not found',
      });
    }

    const address = user.addresses.id(addressId);

    if (!address) {
      return res.status(404).json({
        success: false,
        message: 'Address not found',
      });
    }

    // If setting as default, unset others
    if (updates.isDefault) {
      user.addresses.forEach(addr => {
        addr.isDefault = false;
      });
    }

    // Update address fields
    Object.keys(updates).forEach(key => {
      if (key === 'coordinates') {
        address.location.coordinates = updates.coordinates;
      } else {
        address[key] = updates[key];
      }
    });

    await user.save();

    res.status(200).json({
      success: true,
      message: 'Address updated successfully',
      data: { addresses: user.addresses },
    });
  } catch (error) {
    next(error);
  }
};

// Delete address
exports.deleteAddress = async (req, res, next) => {
  try {
    const { addressId } = req.params;

    const user = await User.findById(req.userId);

    if (!user) {
      return res.status(404).json({
        success: false,
        message: 'User not found',
      });
    }

    user.addresses = user.addresses.filter(addr => addr._id.toString() !== addressId);

    // If deleted address was default and there are other addresses, make first one default
    if (user.addresses.length > 0 && !user.addresses.some(addr => addr.isDefault)) {
      user.addresses[0].isDefault = true;
    }

    await user.save();

    res.status(200).json({
      success: true,
      message: 'Address deleted successfully',
      data: { addresses: user.addresses },
    });
  } catch (error) {
    next(error);
  }
};

// Toggle favorite restaurant
exports.toggleFavorite = async (req, res, next) => {
  try {
    const { restaurantId } = req.params;

    const user = await User.findById(req.userId);

    if (!user) {
      return res.status(404).json({
        success: false,
        message: 'User not found',
      });
    }

    const index = user.favorites.indexOf(restaurantId);

    if (index > -1) {
      // Remove from favorites
      user.favorites.splice(index, 1);
    } else {
      // Add to favorites
      user.favorites.push(restaurantId);
    }

    await user.save();

    res.status(200).json({
      success: true,
      message: index > -1 ? 'Removed from favorites' : 'Added to favorites',
      data: { favorites: user.favorites },
    });
  } catch (error) {
    next(error);
  }
};

// Get favorite restaurants
exports.getFavorites = async (req, res, next) => {
  try {
    const user = await User.findById(req.userId).populate('favorites');

    res.status(200).json({
      success: true,
      data: { favorites: user.favorites },
    });
  } catch (error) {
    next(error);
  }
};
