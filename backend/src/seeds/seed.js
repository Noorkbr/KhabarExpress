#!/usr/bin/env node

/**
 * KhabarExpress Database Seeder
 *
 * Seeds the database with sample categories, delivery zones,
 * a test restaurant with menu items, and a promo code.
 *
 * Usage:
 *   node src/seeds/seed.js            # seed data
 *   node src/seeds/seed.js --drop     # drop existing data then seed
 */

require('dotenv').config();
const mongoose = require('mongoose');

const Zone = require('../models/Zone');
const Restaurant = require('../models/Restaurant');
const Category = require('../models/Category');
const MenuItem = require('../models/MenuItem');
const PromoCode = require('../models/PromoCode');

const DATABASE_URL = process.env.DATABASE_URL || 'mongodb://localhost:27017/khabarexpress';
const shouldDrop = process.argv.includes('--drop');

// ── Sample Zones (Dhaka) ──────────────────────────────────────

const zones = [
  {
    name: 'Gulshan',
    nameBn: 'গুলশান',
    polygon: {
      type: 'Polygon',
      coordinates: [[
        [90.405, 23.795],
        [90.425, 23.795],
        [90.425, 23.810],
        [90.405, 23.810],
        [90.405, 23.795],
      ]],
    },
    center: { type: 'Point', coordinates: [90.415, 23.803] },
    deliveryFee: 3000,
    perKmFee: 800,
    estimatedTime: '25-35 min',
  },
  {
    name: 'Banani',
    nameBn: 'বনানী',
    polygon: {
      type: 'Polygon',
      coordinates: [[
        [90.395, 23.785],
        [90.415, 23.785],
        [90.415, 23.800],
        [90.395, 23.800],
        [90.395, 23.785],
      ]],
    },
    center: { type: 'Point', coordinates: [90.405, 23.793] },
    deliveryFee: 3000,
    perKmFee: 800,
    estimatedTime: '25-40 min',
  },
  {
    name: 'Dhanmondi',
    nameBn: 'ধানমন্ডি',
    polygon: {
      type: 'Polygon',
      coordinates: [[
        [90.365, 23.735],
        [90.390, 23.735],
        [90.390, 23.755],
        [90.365, 23.755],
        [90.365, 23.735],
      ]],
    },
    center: { type: 'Point', coordinates: [90.378, 23.745] },
    deliveryFee: 3500,
    perKmFee: 900,
    estimatedTime: '30-45 min',
  },
  {
    name: 'Uttara',
    nameBn: 'উত্তরা',
    polygon: {
      type: 'Polygon',
      coordinates: [[
        [90.385, 23.860],
        [90.415, 23.860],
        [90.415, 23.880],
        [90.385, 23.880],
        [90.385, 23.860],
      ]],
    },
    center: { type: 'Point', coordinates: [90.400, 23.870] },
    deliveryFee: 4000,
    perKmFee: 1000,
    estimatedTime: '35-50 min',
  },
  {
    name: 'Mirpur',
    nameBn: 'মিরপুর',
    polygon: {
      type: 'Polygon',
      coordinates: [[
        [90.345, 23.800],
        [90.375, 23.800],
        [90.375, 23.825],
        [90.345, 23.825],
        [90.345, 23.800],
      ]],
    },
    center: { type: 'Point', coordinates: [90.360, 23.813] },
    deliveryFee: 3500,
    perKmFee: 900,
    estimatedTime: '30-45 min',
  },
];

// ── Category templates (per restaurant) ──────────────────────

const categoryTemplates = [
  { name: 'Biriyani', nameBn: 'বিরিয়ানি', order: 1 },
  { name: 'Rice & Curry', nameBn: 'ভাত ও তরকারি', order: 2 },
  { name: 'Kebab & Grill', nameBn: 'কাবাব ও গ্রিল', order: 3 },
  { name: 'Snacks', nameBn: 'স্ন্যাকস', order: 4 },
  { name: 'Drinks', nameBn: 'পানীয়', order: 5 },
  { name: 'Desserts', nameBn: 'মিষ্টি', order: 6 },
];

// ── Menu item templates (mapped by category name) ────────────

const menuItemTemplates = {
  Biriyani: [
    { name: 'Kacchi Biriyani', nameBn: 'কাচ্চি বিরিয়ানি', price: 28000, prepTime: 20, isPopular: true },
    { name: 'Tehari', nameBn: 'তেহারি', price: 18000, prepTime: 15 },
    { name: 'Morog Polao', nameBn: 'মুরগ পোলাও', price: 22000, prepTime: 20 },
  ],
  'Rice & Curry': [
    { name: 'Chicken Curry with Rice', nameBn: 'মুরগির তরকারি ও ভাত', price: 16000, prepTime: 15, isPopular: true },
    { name: 'Beef Bhuna with Rice', nameBn: 'গরুর ভুনা ও ভাত', price: 20000, prepTime: 20 },
    { name: 'Fish Curry with Rice', nameBn: 'মাছের তরকারি ও ভাত', price: 18000, prepTime: 15 },
  ],
  'Kebab & Grill': [
    { name: 'Seekh Kebab (4 pcs)', nameBn: 'সিখ কাবাব (৪ পিস)', price: 14000, prepTime: 15, spiceLevel: 'Medium' },
    { name: 'Chicken Tikka', nameBn: 'চিকেন টিক্কা', price: 16000, prepTime: 20, isPopular: true, spiceLevel: 'Hot' },
    { name: 'Tandoori Chicken Half', nameBn: 'তান্দুরি চিকেন হাফ', price: 24000, prepTime: 25, spiceLevel: 'Medium' },
  ],
  Snacks: [
    { name: 'Singara (5 pcs)', nameBn: 'সিঙ্গারা (৫ পিস)', price: 5000, prepTime: 10 },
    { name: 'Fuchka (8 pcs)', nameBn: 'ফুচকা (৮ পিস)', price: 6000, prepTime: 10, isPopular: true },
    { name: 'Chicken Roll', nameBn: 'চিকেন রোল', price: 8000, prepTime: 10 },
  ],
  Drinks: [
    { name: 'Borhani', nameBn: 'বোরহানি', price: 4000, prepTime: 5, isPopular: true },
    { name: 'Lassi', nameBn: 'লাচ্ছি', price: 5000, prepTime: 5 },
    { name: 'Lemon Soda', nameBn: 'লেবু সোডা', price: 3000, prepTime: 5 },
  ],
  Desserts: [
    { name: 'Firni', nameBn: 'ফিরনি', price: 6000, prepTime: 5 },
    { name: 'Jilapi (250g)', nameBn: 'জিলাপি (২৫০ গ্রাম)', price: 5000, prepTime: 5 },
    { name: 'Rasmalai (2 pcs)', nameBn: 'রসমালাই (২ পিস)', price: 7000, prepTime: 5, isPopular: true },
  ],
};

// ── Seed logic ───────────────────────────────────────────────

async function seed() {
  try {
    await mongoose.connect(DATABASE_URL);
    console.log('✅ Connected to MongoDB');

    if (shouldDrop) {
      console.log('🗑️  Dropping existing seed data...');
      await Promise.all([
        Zone.deleteMany({}),
        Category.deleteMany({}),
        MenuItem.deleteMany({}),
        PromoCode.deleteMany({ code: 'WELCOME50' }),
      ]);
    }

    // 1. Zones
    const createdZones = await Zone.insertMany(zones);
    console.log(`📍 Created ${createdZones.length} delivery zones`);

    // 2. Find or create a test restaurant
    let restaurant = await Restaurant.findOne({ phone: '+8801700000001' });
    if (!restaurant) {
      restaurant = await Restaurant.create({
        name: 'KhabarExpress Test Kitchen',
        nameBn: 'খবর এক্সপ্রেস টেস্ট কিচেন',
        description: 'Sample restaurant for development and testing',
        descriptionBn: 'ডেভেলপমেন্ট ও টেস্টিং-এর জন্য নমুনা রেস্তোরাঁ',
        phone: '+8801700000001',
        email: 'test-kitchen@khabarexpress.com',
        cuisines: ['Bangladeshi', 'Indian', 'Street Food'],
        category: 'Restaurant',
        location: {
          type: 'Point',
          coordinates: [90.415, 23.803], // Gulshan
        },
        address: {
          street: 'Road 103',
          area: 'Gulshan-2',
          thana: 'Gulshan',
          district: 'Dhaka',
          postalCode: '1212',
        },
        businessHours: [
          { day: 'Saturday', openTime: '10:00', closeTime: '23:00', isClosed: false },
          { day: 'Sunday', openTime: '10:00', closeTime: '23:00', isClosed: false },
          { day: 'Monday', openTime: '10:00', closeTime: '23:00', isClosed: false },
          { day: 'Tuesday', openTime: '10:00', closeTime: '23:00', isClosed: false },
          { day: 'Wednesday', openTime: '10:00', closeTime: '23:00', isClosed: false },
          { day: 'Thursday', openTime: '10:00', closeTime: '23:00', isClosed: false },
          { day: 'Friday', openTime: '12:00', closeTime: '23:00', isClosed: false },
        ],
        rating: 4.5,
        deliveryTime: { min: 25, max: 40 },
        minOrderAmount: 10000, // ৳100
        deliveryRadius: 5,
        deliveryZones: createdZones.slice(0, 2).map((z) => z._id),
        commission: 15,
        approvalStatus: 'approved',
        isActive: true,
        isOpen: true,
        featured: true,
      });
      console.log('🍳 Created test restaurant');
    } else {
      console.log('🍳 Test restaurant already exists, reusing');
    }

    // 3. Categories
    const createdCategories = await Category.insertMany(
      categoryTemplates.map((cat) => ({
        ...cat,
        restaurant: restaurant._id,
      }))
    );
    console.log(`📂 Created ${createdCategories.length} menu categories`);

    // 4. Menu items
    const menuItems = [];
    for (const cat of createdCategories) {
      const templates = menuItemTemplates[cat.name] || [];
      for (const item of templates) {
        menuItems.push({
          ...item,
          restaurant: restaurant._id,
          category: cat._id,
          categoryName: cat.name,
          isHalal: true,
          isAvailable: true,
        });
      }
    }
    const createdItems = await MenuItem.insertMany(menuItems);
    console.log(`🍔 Created ${createdItems.length} menu items`);

    // 5. Promo code
    const existingPromo = await PromoCode.findOne({ code: 'WELCOME50' });
    if (!existingPromo) {
      await PromoCode.create({
        code: 'WELCOME50',
        description: '50% off on your first order (max ৳100)',
        descriptionBn: 'প্রথম অর্ডারে ৫০% ছাড় (সর্বোচ্চ ৳১০০)',
        type: 'percentage',
        value: 50,
        minOrderAmount: 15000, // ৳150
        maxDiscount: 10000, // ৳100
        usageLimit: { total: 10000, perUser: 1 },
        validFrom: new Date(),
        validUntil: new Date(Date.now() + 365 * 24 * 60 * 60 * 1000),
        applicableTo: 'first_order',
        isActive: true,
      });
      console.log('🎟️  Created WELCOME50 promo code');
    }

    console.log('\n✅ Seeding complete!');
    process.exit(0);
  } catch (error) {
    console.error('❌ Seeding error:', error);
    process.exit(1);
  }
}

seed();
