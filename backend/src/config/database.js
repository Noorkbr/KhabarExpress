const mongoose = require('mongoose');

const MAX_RETRIES = 5;
const INITIAL_RETRY_DELAY_MS = 5000;

mongoose.connection.on('connected', () => {
  console.log('✅ Mongoose connected to MongoDB');
});

mongoose.connection.on('disconnected', () => {
  console.warn('⚠️ Mongoose disconnected from MongoDB');
});

mongoose.connection.on('error', (err) => {
  console.error('❌ Mongoose connection error:', err.message);
});

const connectDatabase = async (retries = MAX_RETRIES, delayMs = INITIAL_RETRY_DELAY_MS) => {
  try {
    const conn = await mongoose.connect(
      process.env.DATABASE_URL || 'mongodb://localhost:27017/khabarexpress'
    );

    console.log(`MongoDB Connected: ${conn.connection.host}`);
    return conn;
  } catch (error) {
    console.error('Database connection error:', error.message);

    if (retries > 0) {
      console.log(`🔄 Retrying database connection in ${delayMs / 1000}s... (${retries} attempts left)`);
      await new Promise((resolve) => setTimeout(resolve, delayMs));
      return connectDatabase(retries - 1, delayMs * 2);
    }

    throw error;
  }
};

module.exports = connectDatabase;
