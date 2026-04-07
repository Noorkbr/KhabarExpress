const { Server } = require('socket.io');
const jwt = require('jsonwebtoken');

let io;

/**
 * Socket.IO authentication middleware.
 * Verifies JWT token from handshake auth or query parameters.
 * Attaches decoded user data to socket.user on success.
 * @param {import('socket.io').Socket} socket - The connecting socket.
 * @param {Function} next - Express-style next callback.
 */
const socketAuthMiddleware = (socket, next) => {
  const token = socket.handshake.auth?.token || socket.handshake.query?.token;
  if (!token) {
    return next(new Error('Authentication required'));
  }
  try {
    const decoded = jwt.verify(token, process.env.JWT_SECRET);
    socket.user = decoded;
    next();
  } catch (err) {
    next(new Error('Invalid or expired token'));
  }
};

const setupSocket = (server) => {
  const rawOrigins = process.env.CORS_ORIGIN || '*';
  const origins = rawOrigins === '*'
    ? '*'
    : rawOrigins.split(',').map((o) => o.trim()).filter(Boolean);

  io = new Server(server, {
    cors: {
      origin: origins,
      methods: ['GET', 'POST'],
      credentials: true,
    },
  });

  // Order tracking namespace
  const orderNamespace = io.of('/order');
  orderNamespace.use(socketAuthMiddleware);
  
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
  riderNamespace.use(socketAuthMiddleware);
  
  riderNamespace.on('connection', (socket) => {
    // Verify this is actually a rider
    if (!socket.user.riderId) {
      socket.disconnect(true);
      return;
    }
    console.log(`🏍️ Rider connected: ${socket.id}`);

    // Rider location update — only accept from the authenticated rider
    socket.on('location', (data) => {
      const { lat, lng, orderId } = data;
      // Broadcast to order tracking using the authenticated rider's ID
      orderNamespace.to(`order:${orderId}`).emit('rider:location', {
        riderId: socket.user.riderId,
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
  restaurantNamespace.use(socketAuthMiddleware);
  
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
  adminNamespace.use(socketAuthMiddleware);
  
  adminNamespace.on('connection', (socket) => {
    // Verify this is actually an admin
    if (socket.user.role !== 'admin') {
      socket.disconnect(true);
      return;
    }
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
