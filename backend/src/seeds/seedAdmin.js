#!/usr/bin/env node

/**
 * Admin Seeder — creates or updates the admin user
 *
 * Usage:
 *   node backend/src/seeds/seedAdmin.js
 *   (from repo root)
 *
 * Or from backend directory:
 *   node src/seeds/seedAdmin.js
 */

require('dotenv').config({ path: require('path').resolve(__dirname, '../../.env') });
const mongoose = require('mongoose');

// We need the User model after bcrypt hook is attached, so require directly
const User = require('../models/User');

const DATABASE_URL = process.env.DATABASE_URL || 'mongodb://localhost:27017/khabarexpress';

const ADMIN_PHONE = process.env.ADMIN_PHONE || '+8801883688374';
const ADMIN_PASSWORD = process.env.ADMIN_DEFAULT_PASSWORD || '16741210@Noor';
const ADMIN_NAME = 'Noor Admin';

async function seedAdmin() {
  await mongoose.connect(DATABASE_URL);
  console.log('✅ Connected to MongoDB');

  try {
    let admin = await User.findOne({ phone: ADMIN_PHONE }).select('+password');

    if (admin) {
      console.log(`ℹ️  Admin user already exists (${ADMIN_PHONE}) — updating…`);
      admin.name = ADMIN_NAME;
      admin.password = ADMIN_PASSWORD; // will be re-hashed by pre-save hook
      admin.role = 'admin';
      admin.isVerified = true;
      await admin.save();
      console.log('✅ Admin user updated');
    } else {
      await User.create({
        phone: ADMIN_PHONE,
        name: ADMIN_NAME,
        password: ADMIN_PASSWORD,
        role: 'admin',
        isVerified: true,
      });
      console.log(`✅ Admin user created (${ADMIN_PHONE})`);
    }
  } catch (err) {
    console.error('❌ Error seeding admin:', err.message);
    process.exit(1);
  } finally {
    await mongoose.disconnect();
    console.log('✅ Disconnected from MongoDB');
  }
}

seedAdmin();
