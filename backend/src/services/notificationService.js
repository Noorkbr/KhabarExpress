const admin = require('../config/firebase');

class NotificationService {
  // Send push notification to a single device
  async sendToDevice(deviceToken, notification, data = {}) {
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
            channelId: 'khabarlagbe_orders',
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
    try {
      const message = {
        tokens: deviceTokens,
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

  // Order notifications
  async sendOrderConfirmation(userId, order) {
    // TODO: Get user's FCM token from database
    // const deviceToken = await getUserDeviceToken(userId);
    
    return this.sendToDevice('dummy-token', {
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

    return this.sendToDevice('dummy-token', message, {
      type: 'order_status',
      orderId: order._id.toString(),
      status,
    });
  }
}

module.exports = new NotificationService();
