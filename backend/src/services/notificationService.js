const admin = require('../config/firebase');
const User = require('../models/User');

class NotificationService {
  // Send push notification to a single device
  async sendToDevice(deviceToken, notification, data = {}) {
    if (!deviceToken) {
      console.warn('⚠️ No device token provided, skipping notification');
      return { success: false, error: 'No device token' };
    }

    try {
      const message = {
        token: deviceToken,
        notification: {
          title: notification.title,
          body: notification.body,
          imageUrl: notification.image,
        },
        data: {
          ...data,
          click_action: 'FLUTTER_NOTIFICATION_CLICK',
        },
        android: {
          priority: 'high',
          notification: {
            sound: 'default',
            channelId: 'khabarexpress_orders',
          },
        },
      };

      const response = await admin.messaging().send(message);
      console.log('✅ Notification sent:', response);
      return { success: true, messageId: response };
    } catch (error) {
      console.error('❌ Notification error:', error);
      return { success: false, error: error.message };
    }
  }

  // Send to multiple devices
  async sendToMultipleDevices(deviceTokens, notification, data = {}) {
    const validTokens = (deviceTokens || []).filter(Boolean);
    if (validTokens.length === 0) {
      console.warn('⚠️ No valid device tokens, skipping notification');
      return { success: false, error: 'No valid device tokens' };
    }

    try {
      const message = {
        tokens: validTokens,
        notification: {
          title: notification.title,
          body: notification.body,
        },
        data,
      };

      const response = await admin.messaging().sendMulticast(message);
      console.log(`✅ Notifications sent: ${response.successCount} success, ${response.failureCount} failed`);
      return {
        success: true,
        successCount: response.successCount,
        failureCount: response.failureCount,
      };
    } catch (error) {
      console.error('❌ Multi notification error:', error);
      return { success: false, error: error.message };
    }
  }

  // Look up a user's FCM token from the database
  async getUserDeviceToken(userId) {
    try {
      const user = await User.findById(userId).select('+fcmToken');
      return user?.fcmToken || null;
    } catch (error) {
      console.error('Error fetching user device token:', error);
      return null;
    }
  }

  // Order notifications
  async sendOrderConfirmation(userId, order) {
    const deviceToken = await this.getUserDeviceToken(userId);

    return this.sendToDevice(deviceToken, {
      title: 'Order Confirmed! 🎉',
      body: `Your order #${order.orderNumber} has been confirmed. Preparing your food!`,
    }, {
      type: 'order_confirmed',
      orderId: order._id.toString(),
    });
  }

  async sendOrderStatusUpdate(userId, order, status) {
    const messages = {
      preparing: { title: '👨‍🍳 Chef is cooking!', body: 'Your delicious food is being prepared' },
      ready: { title: '✅ Food Ready!', body: 'Your order is ready for pickup' },
      picked_up: { title: '🏍️ Rider on the way!', body: 'Your food is being delivered' },
      on_the_way: { title: '🚀 Almost there!', body: 'Rider will arrive in a few minutes' },
      delivered: { title: '🎉 Delivered!', body: 'Enjoy your meal! Please rate your experience' },
    };

    const message = messages[status];
    if (!message) return;

    const deviceToken = await this.getUserDeviceToken(userId);

    return this.sendToDevice(deviceToken, message, {
      type: 'order_status',
      orderId: order._id.toString(),
      status,
    });
  }
}

module.exports = new NotificationService();
