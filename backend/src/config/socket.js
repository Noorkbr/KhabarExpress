const { Server } = require('socket.io');

let io;

const setupSocket = (server) => {
  io = new Server(server, {
    cors: {
      origin: process.env.CORS_ORIGIN || '*',
      methods: ['GET', 'POST'],
    },
  });

  // Order tracking namespace
  const orderNamespace = io.of('/order');
  
  orderNamespace.on('connection', (socket) => {
    console.log(`📱 Client connected to order tracking: ${socket.id}`);

    // Subscribe to order updates
    socket.on('subscribe', (orderId) => {
      socket.join(`order:${orderId}`);
      console.log(`👁️ Client ${socket.id} subscribed to order ${orderId}`);
    });

    // Unsubscribe from order updates
    socket.on('unsubscribe', (orderId) => {
      socket.leave(`order:${orderId}`);
      console.log(`👋 Client ${socket.id} unsubscribed from order ${orderId}`);
    });

    socket.on('disconnect', () => {
      console.log(`📱 Client disconnected: ${socket.id}`);
    });
  });

  // Rider location namespace
  const riderNamespace = io.of('/rider');
  
  riderNamespace.on('connection', (socket) => {
    console.log(`🏍️ Rider connected: ${socket.id}`);

    // Rider location update
    socket.on('location', (data) => {
      const { riderId, lat, lng, orderId } = data;
      // Broadcast to order tracking
      orderNamespace.to(`order:${orderId}`).emit('rider:location', {
        riderId,
        lat,
        lng,
        timestamp: new Date(),
      });
    });

    socket.on('disconnect', () => {
      console.log(`🏍️ Rider disconnected: ${socket.id}`);
    });
  });

  // Restaurant namespace for order notifications
  const restaurantNamespace = io.of('/restaurant');
  
  restaurantNamespace.on('connection', (socket) => {
    console.log(`🍽️ Restaurant connected: ${socket.id}`);

    // Subscribe to restaurant orders
    socket.on('subscribe', (restaurantId) => {
      socket.join(`restaurant:${restaurantId}`);
      console.log(`👁️ Restaurant ${socket.id} subscribed to ${restaurantId}`);
    });

    socket.on('unsubscribe', (restaurantId) => {
      socket.leave(`restaurant:${restaurantId}`);
      console.log(`👋 Restaurant ${socket.id} unsubscribed from ${restaurantId}`);
    });

    socket.on('disconnect', () => {
      console.log(`🍽️ Restaurant disconnected: ${socket.id}`);
    });
  });

  // Admin namespace for live monitoring
  const adminNamespace = io.of('/admin');
  
  adminNamespace.on('connection', (socket) => {
    console.log(`👨‍💼 Admin connected: ${socket.id}`);

    // Subscribe to all platform updates
    socket.on('subscribe', (channel) => {
      socket.join(channel);
      console.log(`👁️ Admin ${socket.id} subscribed to ${channel}`);
    });

    socket.on('disconnect', () => {
      console.log(`👨‍💼 Admin disconnected: ${socket.id}`);
    });
  });

  console.log('✅ Socket.IO configured');
  return io;
};

const getIO = () => {
  if (!io) {
    throw new Error('Socket.IO not initialized');
  }
  return io;
};

module.exports = setupSocket;
module.exports.getIO = getIO;
