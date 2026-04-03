const Zone = require('../models/Zone');
const Restaurant = require('../models/Restaurant');

// Get all active zones
exports.getActiveZones = async (req, res) => {
  try {
    const zones = await Zone.find({ isActive: true })
      .select('-__v')
      .sort({ name: 1 });

    res.json({
      success: true,
      data: zones,
    });
  } catch (error) {
    console.error('Error fetching zones:', error);
    res.status(500).json({
      success: false,
      message: 'Failed to fetch zones',
      error: error.message,
    });
  }
};

// Get all zones (admin only)
exports.getAllZones = async (req, res) => {
  try {
    const zones = await Zone.find()
      .select('-__v')
      .sort({ createdAt: -1 });

    res.json({
      success: true,
      data: zones,
    });
  } catch (error) {
    console.error('Error fetching all zones:', error);
    res.status(500).json({
      success: false,
      message: 'Failed to fetch zones',
      error: error.message,
    });
  }
};

// Get zone by ID
exports.getZoneById = async (req, res) => {
  try {
    const zone = await Zone.findById(req.params.id);

    if (!zone) {
      return res.status(404).json({
        success: false,
        message: 'Zone not found',
      });
    }

    res.json({
      success: true,
      data: zone,
    });
  } catch (error) {
    console.error('Error fetching zone:', error);
    res.status(500).json({
      success: false,
      message: 'Failed to fetch zone',
      error: error.message,
    });
  }
};

// Create new zone (admin only)
exports.createZone = async (req, res) => {
  try {
    const { name, nameBn, polygon, center, deliveryFee, perKmFee, estimatedTime } = req.body;

    // Validate required fields
    if (!name || !nameBn || !polygon || !center) {
      return res.status(400).json({
        success: false,
        message: 'Name, nameBn, polygon, and center are required',
      });
    }

    const zone = new Zone({
      name,
      nameBn,
      polygon,
      center,
      deliveryFee,
      perKmFee,
      estimatedTime,
    });

    await zone.save();

    res.status(201).json({
      success: true,
      message: 'Zone created successfully',
      data: zone,
    });
  } catch (error) {
    console.error('Error creating zone:', error);
    res.status(500).json({
      success: false,
      message: 'Failed to create zone',
      error: error.message,
    });
  }
};

// Update zone (admin only)
exports.updateZone = async (req, res) => {
  try {
    const { name, nameBn, polygon, center, deliveryFee, perKmFee, estimatedTime, isActive } = req.body;

    const zone = await Zone.findById(req.params.id);

    if (!zone) {
      return res.status(404).json({
        success: false,
        message: 'Zone not found',
      });
    }

    // Update fields
    if (name !== undefined) zone.name = name;
    if (nameBn !== undefined) zone.nameBn = nameBn;
    if (polygon !== undefined) zone.polygon = polygon;
    if (center !== undefined) zone.center = center;
    if (deliveryFee !== undefined) zone.deliveryFee = deliveryFee;
    if (perKmFee !== undefined) zone.perKmFee = perKmFee;
    if (estimatedTime !== undefined) zone.estimatedTime = estimatedTime;
    if (isActive !== undefined) zone.isActive = isActive;

    await zone.save();

    res.json({
      success: true,
      message: 'Zone updated successfully',
      data: zone,
    });
  } catch (error) {
    console.error('Error updating zone:', error);
    res.status(500).json({
      success: false,
      message: 'Failed to update zone',
      error: error.message,
    });
  }
};

// Toggle zone active status (admin only)
exports.toggleZoneActive = async (req, res) => {
  try {
    const zone = await Zone.findById(req.params.id);

    if (!zone) {
      return res.status(404).json({
        success: false,
        message: 'Zone not found',
      });
    }

    zone.isActive = !zone.isActive;
    await zone.save();

    res.json({
      success: true,
      message: `Zone ${zone.isActive ? 'activated' : 'deactivated'} successfully`,
      data: zone,
    });
  } catch (error) {
    console.error('Error toggling zone status:', error);
    res.status(500).json({
      success: false,
      message: 'Failed to toggle zone status',
      error: error.message,
    });
  }
};

// Delete zone (admin only)
exports.deleteZone = async (req, res) => {
  try {
    const zone = await Zone.findById(req.params.id);

    if (!zone) {
      return res.status(404).json({
        success: false,
        message: 'Zone not found',
      });
    }

    // Check if any restaurants are in this zone
    const restaurantsInZone = await Restaurant.countDocuments({
      'location.coordinates': {
        $geoWithin: {
          $geometry: zone.polygon,
        },
      },
    });

    if (restaurantsInZone > 0) {
      return res.status(400).json({
        success: false,
        message: `Cannot delete zone. ${restaurantsInZone} restaurant(s) are still in this zone`,
      });
    }

    await zone.deleteOne();

    res.json({
      success: true,
      message: 'Zone deleted successfully',
    });
  } catch (error) {
    console.error('Error deleting zone:', error);
    res.status(500).json({
      success: false,
      message: 'Failed to delete zone',
      error: error.message,
    });
  }
};

// Get restaurants in a zone
exports.getZoneRestaurants = async (req, res) => {
  try {
    const zone = await Zone.findById(req.params.id);

    if (!zone) {
      return res.status(404).json({
        success: false,
        message: 'Zone not found',
      });
    }

    const restaurants = await Restaurant.find({
      'location.coordinates': {
        $geoWithin: {
          $geometry: zone.polygon,
        },
      },
      isActive: true,
    }).select('name nameBn logo rating totalReviews deliveryTime minOrderAmount');

    res.json({
      success: true,
      data: {
        zone,
        restaurants,
        count: restaurants.length,
      },
    });
  } catch (error) {
    console.error('Error fetching zone restaurants:', error);
    res.status(500).json({
      success: false,
      message: 'Failed to fetch zone restaurants',
      error: error.message,
    });
  }
};
