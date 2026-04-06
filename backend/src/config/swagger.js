const swaggerJsdoc = require('swagger-jsdoc');

const options = {
  definition: {
    openapi: '3.0.0',
    info: {
      title: 'KhabarExpress API',
      version: '1.0.0',
      description: 'Food delivery platform API for Bangladesh — covers authentication, restaurants, menus, orders, payments, riders, and administration.',
      contact: {
        name: 'KhabarExpress',
        email: 'support@khabarexpress.com',
      },
      license: {
        name: 'MIT',
      },
    },
    servers: [
      { url: '/api/v1', description: 'API v1' },
    ],
    components: {
      securitySchemes: {
        bearerAuth: {
          type: 'http',
          scheme: 'bearer',
          bearerFormat: 'JWT',
        },
      },
      schemas: {
        Error: {
          type: 'object',
          properties: {
            success: { type: 'boolean', example: false },
            message: { type: 'string' },
          },
        },
        Pagination: {
          type: 'object',
          properties: {
            total: { type: 'integer' },
            page: { type: 'integer' },
            pages: { type: 'integer' },
          },
        },
        // ── Auth ──────────────────────────
        SendOtpRequest: {
          type: 'object',
          required: ['phone'],
          properties: {
            phone: { type: 'string', example: '+8801712345678', description: 'Bangladesh phone number' },
          },
        },
        VerifyOtpRequest: {
          type: 'object',
          required: ['phone', 'otp'],
          properties: {
            phone: { type: 'string', example: '+8801712345678' },
            otp: { type: 'string', example: '123456' },
          },
        },
        AuthResponse: {
          type: 'object',
          properties: {
            success: { type: 'boolean' },
            data: {
              type: 'object',
              properties: {
                token: { type: 'string' },
                refreshToken: { type: 'string' },
                user: { $ref: '#/components/schemas/User' },
              },
            },
          },
        },
        // ── User ──────────────────────────
        User: {
          type: 'object',
          properties: {
            _id: { type: 'string' },
            phone: { type: 'string' },
            name: { type: 'string' },
            email: { type: 'string' },
            profileImage: { type: 'string' },
            preferredLanguage: { type: 'string', enum: ['en', 'bn'] },
            role: { type: 'string', enum: ['customer', 'admin'] },
          },
        },
        // ── Restaurant ────────────────────
        Restaurant: {
          type: 'object',
          properties: {
            _id: { type: 'string' },
            name: { type: 'string' },
            nameBn: { type: 'string' },
            cuisines: { type: 'array', items: { type: 'string' } },
            rating: { type: 'number' },
            deliveryTime: {
              type: 'object',
              properties: { min: { type: 'integer' }, max: { type: 'integer' } },
            },
            isOpen: { type: 'boolean' },
            featured: { type: 'boolean' },
            approvalStatus: { type: 'string', enum: ['pending', 'approved', 'rejected'] },
          },
        },
        // ── MenuItem ──────────────────────
        MenuItem: {
          type: 'object',
          properties: {
            _id: { type: 'string' },
            name: { type: 'string' },
            nameBn: { type: 'string' },
            price: { type: 'integer', description: 'Price in BDT paisa' },
            category: { type: 'string' },
            isAvailable: { type: 'boolean' },
            isPopular: { type: 'boolean' },
            spiceLevel: { type: 'string', enum: ['None', 'Mild', 'Medium', 'Hot', 'Extra Hot'] },
          },
        },
        // ── Order ─────────────────────────
        Order: {
          type: 'object',
          properties: {
            _id: { type: 'string' },
            orderNumber: { type: 'string' },
            status: { type: 'string', enum: ['pending', 'confirmed', 'preparing', 'ready', 'picked_up', 'on_the_way', 'delivered', 'cancelled'] },
            paymentStatus: { type: 'string', enum: ['pending', 'paid', 'refunded'] },
            totalAmount: { type: 'integer', description: 'Total in BDT paisa' },
          },
        },
        // ── Payment ───────────────────────
        Payment: {
          type: 'object',
          properties: {
            _id: { type: 'string' },
            order: { type: 'string' },
            amount: { type: 'integer', description: 'Amount in BDT paisa' },
            method: { type: 'string', enum: ['bkash', 'nagad', 'rocket', 'upay', 'card', 'cod'] },
            status: { type: 'string', enum: ['pending', 'initiated', 'success', 'failed', 'refunded'] },
          },
        },
        CreatePaymentRequest: {
          type: 'object',
          required: ['orderId', 'method'],
          properties: {
            orderId: { type: 'string' },
            method: { type: 'string', enum: ['bkash', 'nagad', 'rocket', 'upay', 'card', 'cod'] },
            returnUrl: { type: 'string' },
          },
        },
        // ── Zone ──────────────────────────
        Zone: {
          type: 'object',
          properties: {
            _id: { type: 'string' },
            name: { type: 'string' },
            nameBn: { type: 'string' },
            deliveryFee: { type: 'integer' },
            estimatedTime: { type: 'string' },
            isActive: { type: 'boolean' },
          },
        },
        // ── Category ──────────────────────
        Category: {
          type: 'object',
          properties: {
            _id: { type: 'string' },
            name: { type: 'string' },
            nameBn: { type: 'string' },
            restaurant: { type: 'string' },
            order: { type: 'integer' },
            isActive: { type: 'boolean' },
          },
        },
        // ── PromoCode ─────────────────────
        PromoCode: {
          type: 'object',
          properties: {
            _id: { type: 'string' },
            code: { type: 'string' },
            type: { type: 'string', enum: ['percentage', 'fixed'] },
            value: { type: 'number' },
            minOrderAmount: { type: 'integer' },
            maxDiscount: { type: 'integer' },
            isActive: { type: 'boolean' },
          },
        },
        // ── Review ────────────────────────
        Review: {
          type: 'object',
          properties: {
            _id: { type: 'string' },
            foodRating: { type: 'integer', minimum: 1, maximum: 5 },
            deliveryRating: { type: 'integer', minimum: 1, maximum: 5 },
            review: { type: 'string' },
          },
        },
        // ── Rider ─────────────────────────
        Rider: {
          type: 'object',
          properties: {
            _id: { type: 'string' },
            name: { type: 'string' },
            phone: { type: 'string' },
            vehicleType: { type: 'string', enum: ['motorcycle', 'bicycle', 'car'] },
            status: { type: 'string', enum: ['offline', 'available', 'busy', 'on_break'] },
          },
        },
      },
    },
    tags: [
      { name: 'Health', description: 'Server health check' },
      { name: 'Auth', description: 'Authentication & OTP' },
      { name: 'Users', description: 'User profile management' },
      { name: 'Restaurants', description: 'Browse and manage restaurants' },
      { name: 'Menu Items', description: 'Restaurant menu management' },
      { name: 'Categories', description: 'Menu categories' },
      { name: 'Orders', description: 'Order lifecycle management' },
      { name: 'Payments', description: 'Payment processing (bKash, Nagad, Rocket, Upay, SSL Commerz, COD)' },
      { name: 'Reviews', description: 'Ratings and reviews' },
      { name: 'Zones', description: 'Delivery zone management' },
      { name: 'Riders', description: 'Rider/delivery personnel' },
      { name: 'Promo Codes', description: 'Discount and promo codes' },
      { name: 'Upload', description: 'Image/file uploads' },
      { name: 'Admin', description: 'Admin dashboard and analytics' },
    ],
    paths: {
      // ── Health ──────────────────────
      '/': {
        get: {
          tags: ['Health'],
          summary: 'API info',
          responses: { 200: { description: 'API info with available endpoints' } },
        },
      },
      // ── Auth ────────────────────────
      '/auth/send-otp': {
        post: {
          tags: ['Auth'],
          summary: 'Send OTP to phone number',
          requestBody: { required: true, content: { 'application/json': { schema: { $ref: '#/components/schemas/SendOtpRequest' } } } },
          responses: {
            200: { description: 'OTP sent successfully' },
            400: { description: 'Invalid phone number' },
            429: { description: 'Too many requests' },
          },
        },
      },
      '/auth/verify-otp': {
        post: {
          tags: ['Auth'],
          summary: 'Verify OTP and authenticate',
          requestBody: { required: true, content: { 'application/json': { schema: { $ref: '#/components/schemas/VerifyOtpRequest' } } } },
          responses: {
            200: { description: 'Authentication successful', content: { 'application/json': { schema: { $ref: '#/components/schemas/AuthResponse' } } } },
            400: { description: 'Invalid OTP' },
          },
        },
      },
      '/auth/refresh-token': {
        post: {
          tags: ['Auth'],
          summary: 'Refresh access token',
          requestBody: { required: true, content: { 'application/json': { schema: { type: 'object', properties: { refreshToken: { type: 'string' } } } } } },
          responses: { 200: { description: 'New access token' }, 401: { description: 'Invalid refresh token' } },
        },
      },
      '/auth/logout': {
        post: {
          tags: ['Auth'],
          summary: 'Logout',
          security: [{ bearerAuth: [] }],
          responses: { 200: { description: 'Logged out' } },
        },
      },
      // ── Users ───────────────────────
      '/users/profile': {
        get: {
          tags: ['Users'],
          summary: 'Get user profile',
          security: [{ bearerAuth: [] }],
          responses: { 200: { description: 'User profile' } },
        },
        patch: {
          tags: ['Users'],
          summary: 'Update user profile',
          security: [{ bearerAuth: [] }],
          responses: { 200: { description: 'Profile updated' } },
        },
      },
      // ── Restaurants ─────────────────
      '/restaurants': {
        get: {
          tags: ['Restaurants'],
          summary: 'List restaurants',
          parameters: [
            { name: 'page', in: 'query', schema: { type: 'integer' } },
            { name: 'limit', in: 'query', schema: { type: 'integer' } },
          ],
          responses: { 200: { description: 'Restaurant list' } },
        },
      },
      '/restaurants/featured': {
        get: {
          tags: ['Restaurants'],
          summary: 'Get featured restaurants',
          responses: { 200: { description: 'Featured restaurant list' } },
        },
      },
      '/restaurants/search': {
        get: {
          tags: ['Restaurants'],
          summary: 'Search restaurants',
          parameters: [{ name: 'q', in: 'query', schema: { type: 'string' }, description: 'Search query' }],
          responses: { 200: { description: 'Search results' } },
        },
      },
      '/restaurants/{id}': {
        get: {
          tags: ['Restaurants'],
          summary: 'Get restaurant by ID',
          parameters: [{ name: 'id', in: 'path', required: true, schema: { type: 'string' } }],
          responses: { 200: { description: 'Restaurant details' }, 404: { description: 'Not found' } },
        },
      },
      '/restaurants/register': {
        post: {
          tags: ['Restaurants'],
          summary: 'Register a new restaurant',
          requestBody: { required: true, content: { 'application/json': { schema: { $ref: '#/components/schemas/Restaurant' } } } },
          responses: { 201: { description: 'Restaurant created' } },
        },
      },
      // ── Menu Items ──────────────────
      '/menu-items/restaurant/{restaurantId}': {
        get: {
          tags: ['Menu Items'],
          summary: 'Get menu items for a restaurant',
          parameters: [{ name: 'restaurantId', in: 'path', required: true, schema: { type: 'string' } }],
          responses: { 200: { description: 'Menu item list' } },
        },
      },
      // ── Categories ──────────────────
      '/categories/restaurant/{restaurantId}': {
        get: {
          tags: ['Categories'],
          summary: 'Get categories for a restaurant',
          parameters: [{ name: 'restaurantId', in: 'path', required: true, schema: { type: 'string' } }],
          responses: { 200: { description: 'Category list' } },
        },
      },
      // ── Orders ──────────────────────
      '/orders': {
        post: {
          tags: ['Orders'],
          summary: 'Create a new order',
          security: [{ bearerAuth: [] }],
          responses: { 201: { description: 'Order created' } },
        },
        get: {
          tags: ['Orders'],
          summary: 'Get user orders',
          security: [{ bearerAuth: [] }],
          parameters: [
            { name: 'page', in: 'query', schema: { type: 'integer' } },
            { name: 'status', in: 'query', schema: { type: 'string' } },
          ],
          responses: { 200: { description: 'Order list' } },
        },
      },
      '/orders/{id}': {
        get: {
          tags: ['Orders'],
          summary: 'Get order details',
          security: [{ bearerAuth: [] }],
          parameters: [{ name: 'id', in: 'path', required: true, schema: { type: 'string' } }],
          responses: { 200: { description: 'Order details' }, 404: { description: 'Not found' } },
        },
      },
      '/orders/{id}/cancel': {
        patch: {
          tags: ['Orders'],
          summary: 'Cancel an order',
          security: [{ bearerAuth: [] }],
          parameters: [{ name: 'id', in: 'path', required: true, schema: { type: 'string' } }],
          responses: { 200: { description: 'Order cancelled' } },
        },
      },
      // ── Payments ────────────────────
      '/payments/create': {
        post: {
          tags: ['Payments'],
          summary: 'Create a payment',
          security: [{ bearerAuth: [] }],
          requestBody: { required: true, content: { 'application/json': { schema: { $ref: '#/components/schemas/CreatePaymentRequest' } } } },
          responses: {
            200: { description: 'Payment initiated' },
            400: { description: 'Invalid request' },
            404: { description: 'Order not found' },
          },
        },
      },
      '/payments/order/{orderId}': {
        get: {
          tags: ['Payments'],
          summary: 'Get payment by order ID',
          security: [{ bearerAuth: [] }],
          parameters: [{ name: 'orderId', in: 'path', required: true, schema: { type: 'string' } }],
          responses: { 200: { description: 'Payment details' } },
        },
      },
      '/payments/history': {
        get: {
          tags: ['Payments'],
          summary: 'Get payment history',
          security: [{ bearerAuth: [] }],
          parameters: [
            { name: 'page', in: 'query', schema: { type: 'integer' } },
            { name: 'limit', in: 'query', schema: { type: 'integer' } },
          ],
          responses: { 200: { description: 'Payment history' } },
        },
      },
      // ── Reviews ─────────────────────
      '/reviews/restaurant/{restaurantId}': {
        get: {
          tags: ['Reviews'],
          summary: 'Get reviews for a restaurant',
          parameters: [{ name: 'restaurantId', in: 'path', required: true, schema: { type: 'string' } }],
          responses: { 200: { description: 'Review list' } },
        },
      },
      // ── Zones ───────────────────────
      '/zones': {
        get: {
          tags: ['Zones'],
          summary: 'List delivery zones',
          responses: { 200: { description: 'Zone list' } },
        },
      },
      '/zones/{id}': {
        get: {
          tags: ['Zones'],
          summary: 'Get zone by ID',
          parameters: [{ name: 'id', in: 'path', required: true, schema: { type: 'string' } }],
          responses: { 200: { description: 'Zone details' } },
        },
      },
      // ── Riders ──────────────────────
      '/riders/register': {
        post: {
          tags: ['Riders'],
          summary: 'Register as a rider',
          responses: { 201: { description: 'Rider registered' } },
        },
      },
      // ── Promo Codes ─────────────────
      '/promo-codes/validate': {
        post: {
          tags: ['Promo Codes'],
          summary: 'Validate a promo code',
          security: [{ bearerAuth: [] }],
          requestBody: { required: true, content: { 'application/json': { schema: { type: 'object', properties: { code: { type: 'string' }, orderAmount: { type: 'integer' } } } } } },
          responses: { 200: { description: 'Validation result' } },
        },
      },
      '/promo-codes/active': {
        get: {
          tags: ['Promo Codes'],
          summary: 'Get active promo codes',
          security: [{ bearerAuth: [] }],
          responses: { 200: { description: 'Active promo code list' } },
        },
      },
      // ── Upload ──────────────────────
      '/upload/restaurant': {
        post: {
          tags: ['Upload'],
          summary: 'Upload restaurant image',
          security: [{ bearerAuth: [] }],
          requestBody: { required: true, content: { 'multipart/form-data': { schema: { type: 'object', properties: { image: { type: 'string', format: 'binary' } } } } } },
          responses: { 200: { description: 'Upload successful' } },
        },
      },
      // ── Admin ───────────────────────
      '/admin/dashboard': {
        get: {
          tags: ['Admin'],
          summary: 'Admin dashboard stats',
          security: [{ bearerAuth: [] }],
          responses: { 200: { description: 'Dashboard statistics' } },
        },
      },
      '/admin/analytics': {
        get: {
          tags: ['Admin'],
          summary: 'Admin analytics',
          security: [{ bearerAuth: [] }],
          responses: { 200: { description: 'Analytics data' } },
        },
      },
    },
  },
  apis: [], // We define paths inline above
};

const swaggerSpec = swaggerJsdoc(options);

module.exports = swaggerSpec;
