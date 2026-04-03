// Application constants

// Order statuses
exports.ORDER_STATUS = {
  PENDING: 'pending',
  CONFIRMED: 'confirmed',
  PREPARING: 'preparing',
  READY: 'ready',
  PICKED_UP: 'picked_up',
  ON_THE_WAY: 'on_the_way',
  DELIVERED: 'delivered',
  CANCELLED: 'cancelled',
};

// Payment methods
exports.PAYMENT_METHODS = {
  BKASH: 'bkash',
  NAGAD: 'nagad',
  ROCKET: 'rocket',
  CARD: 'card',
  COD: 'cod',
};

// Payment statuses
exports.PAYMENT_STATUS = {
  PENDING: 'pending',
  INITIATED: 'initiated',
  PAID: 'paid',
  FAILED: 'failed',
  REFUNDED: 'refunded',
};

// Rider statuses
exports.RIDER_STATUS = {
  OFFLINE: 'offline',
  AVAILABLE: 'available',
  BUSY: 'busy',
  ON_BREAK: 'on_break',
};

// User roles
exports.USER_ROLES = {
  CUSTOMER: 'customer',
  ADMIN: 'admin',
};

// Bangladesh VAT rate
exports.VAT_RATE = 0.05; // 5%

// Delivery fee structure (in paisa)
exports.DELIVERY_FEE = {
  BASE: 3000, // ৳30
  PER_KM: 1000, // ৳10 per km
  MINIMUM: 3000,
  MAXIMUM: 8000,
};

// Business hours
exports.DEFAULT_BUSINESS_HOURS = [
  { day: 'Monday', openTime: '09:00', closeTime: '23:00', isClosed: false },
  { day: 'Tuesday', openTime: '09:00', closeTime: '23:00', isClosed: false },
  { day: 'Wednesday', openTime: '09:00', closeTime: '23:00', isClosed: false },
  { day: 'Thursday', openTime: '09:00', closeTime: '23:00', isClosed: false },
  { day: 'Friday', openTime: '09:00', closeTime: '23:00', isClosed: false },
  { day: 'Saturday', openTime: '09:00', closeTime: '23:00', isClosed: false },
  { day: 'Sunday', openTime: '09:00', closeTime: '23:00', isClosed: false },
];

// Bangladesh major cities
exports.BD_CITIES = [
  'Dhaka',
  'Chittagong',
  'Sylhet',
  'Khulna',
  'Rajshahi',
  'Barisal',
  'Rangpur',
  'Mymensingh',
];

// Popular Dhaka areas
exports.DHAKA_AREAS = [
  'Gulshan',
  'Banani',
  'Dhanmondi',
  'Uttara',
  'Mirpur',
  'Mohammadpur',
  'Bashundhara',
  'Badda',
  'Baridhara',
  'Motijheel',
  'Old Dhaka',
];
