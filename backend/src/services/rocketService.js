const axios = require('axios');

class RocketService {
  constructor() {
    this.baseURL = process.env.ROCKET_BASE_URL || 'https://sandbox.dutchbanglabank.com/rocket/api';
    this.username = process.env.ROCKET_USERNAME;
    this.password = process.env.ROCKET_PASSWORD;
  }

  // Create payment
  async createPayment({ amount, orderId, paymentId, customerPhone, returnUrl }) {
    try {
      const response = await axios.post(`${this.baseURL}/create-payment`, {
        amount: (amount / 100).toFixed(2), // Convert paisa to BDT
        currency: 'BDT',
        order_id: orderId,
        payment_id: paymentId,
        customer_phone: customerPhone,
        callback_url: `${process.env.BASE_URL}/api/v1/payments/rocket/callback`,
        return_url: returnUrl || `${process.env.FRONTEND_URL}/payment/callback`,
      }, {
        headers: {
          'Content-Type': 'application/json',
          Authorization: `Basic ${Buffer.from(`${this.username}:${this.password}`).toString('base64')}`,
        },
      });

      return {
        success: true,
        paymentId: response.data.payment_id,
        redirectUrl: response.data.redirect_url || response.data.GatewayPageURL,
        sessionKey: response.data.session_key,
      };
    } catch (error) {
      console.error('Rocket create payment error:', error.response?.data || error.message);
      return {
        success: false,
        error: error.response?.data?.message || 'Rocket payment creation failed',
      };
    }
  }

  // Verify payment
  async verifyPayment({ transactionId }) {
    try {
      // Sanitize transactionId to prevent SSRF
      const sanitizedId = encodeURIComponent(String(transactionId));

      const response = await axios.get(`${this.baseURL}/verify-payment/${sanitizedId}`, {
        headers: {
          Authorization: `Basic ${Buffer.from(`${this.username}:${this.password}`).toString('base64')}`,
        },
      });

      return {
        success: response.data.status === 'completed',
        status: response.data.status,
        transactionId: response.data.transaction_id,
        data: response.data,
      };
    } catch (error) {
      console.error('Rocket verify payment error:', error.response?.data || error.message);
      return {
        success: false,
        error: 'Verification failed',
      };
    }
  }

  // Refund payment
  async refundPayment({ transactionId, amount, reason }) {
    try {
      const response = await axios.post(`${this.baseURL}/refund`, {
        transaction_id: transactionId,
        amount: (amount / 100).toFixed(2),
        reason: reason || 'Customer request',
      }, {
        headers: {
          'Content-Type': 'application/json',
          Authorization: `Basic ${Buffer.from(`${this.username}:${this.password}`).toString('base64')}`,
        },
      });

      return {
        success: true,
        refundTrxID: response.data.refund_id,
        data: response.data,
      };
    } catch (error) {
      console.error('Rocket refund error:', error.response?.data || error.message);
      return {
        success: false,
        error: 'Refund failed',
      };
    }
  }
}

module.exports = new RocketService();
