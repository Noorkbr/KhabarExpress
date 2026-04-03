const app = require('./app');
const connectDatabase = require('./config/database');
const { createServer } = require('http');
const setupSocket = require('./config/socket');

const PORT = process.env.PORT || 3000;
const NODE_ENV = process.env.NODE_ENV || 'development';

// Create HTTP server
const server = createServer(app);

// Setup Socket.IO
setupSocket(server);

// Database connection
connectDatabase()
  .then(() => {
    console.log('✅ Database connected successfully');
    
    // Start server
    server.listen(PORT, () => {
      console.log('');
      console.log('🚀 ========================================');
      console.log(`🍛 KhabarLagbe Backend API`);
      console.log('🚀 ========================================');
      console.log(`📡 Server running on port ${PORT}`);
      console.log(`🌍 Environment: ${NODE_ENV}`);
      console.log(`🔗 Health check: http://localhost:${PORT}/health`);
      console.log(`📚 API Base: http://localhost:${PORT}/api/v1`);
      console.log('🚀 ========================================');
      console.log('');
    });
  })
  .catch((error) => {
    console.error('❌ Database connection failed:', error.message);
    process.exit(1);
  });

// Handle unhandled promise rejections
process.on('unhandledRejection', (err) => {
  console.error('❌ Unhandled Promise Rejection:', err);
  server.close(() => process.exit(1));
});

// Handle uncaught exceptions
process.on('uncaughtException', (err) => {
  console.error('❌ Uncaught Exception:', err);
  process.exit(1);
});

// Graceful shutdown
process.on('SIGTERM', () => {
  console.log('👋 SIGTERM received. Shutting down gracefully...');
  server.close(() => {
    console.log('✅ Server closed');
    process.exit(0);
  });
});
