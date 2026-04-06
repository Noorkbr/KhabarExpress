const axios = require('axios');

class UpayService {
  constructor() {
    this.baseURL = process.env.UPAY_BASE_URL || 'https://sandbox.upay.com.bd/api';
    this.merchantId = process.env.UPAY_MERCHANT_ID;
    this.merchantKey = process.env.UPAY_MERCHANT_KEY;
  }

  // Create payment
  async createPayment({ amount, orderId, paymentId, customerPhone, returnUrl }) {
    try {
      const response = await axios.post(`${this.baseURL}/create-payment`, {
        merchant_id: this.merchantId,
        amount: (amount / 100).toFixed(2), // Convert paisa to BDT
        currency: 'BDT',
        order_id: orderId,
        payment_id: paymentId,
        customer_phone: customerPhone,
        callback_url: `${process.env.BASE_URL}/api/v1/payments/upay/callback`,
        return_url: returnUrl || `${process.env.FRONTEND_URL}/payment/callback`,
      }, {
        headers: {
          'Content-Type': 'application/json',
          'X-Merchant-Key': this.merchantKey,
        },
      });

      return {
        success: true,
        paymentId: response.data.payment_id,
        redirectUrl: response.data.redirect_url || response.data.GatewayPageURL,
        sessionKey: response.data.session_key,
      };
    } catch (error) {
      console.error('Upay create payment error:', error.response?.data || error.message);
      return {
        success: false,
        error: error.response?.data?.message || 'Upay payment creation failed',
      };
    }
  }

  // Verify payment
  async verifyPayment({ transactionId }) {
    try {
      const response = await axios.get(`${this.baseURL}/verify-payment/${transactionId}`, {
        headers: {
          'X-Merchant-Key': this.merchantKey,
        },
      });

      return {
        success: response.data.status === 'completed',
        status: response.data.status,
        transactionId: response.data.transaction_id,
        data: response.data,
      };
    } catch (error) {
      console.error('Upay verify payment error:', error.response?.data || error.message);
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
        merchant_id: this.merchantId,
        transaction_id: transactionId,
        amount: (amount / 100).toFixed(2),
        reason: reason || 'Customer request',
      }, {
        headers: {
          'Content-Type': 'application/json',
          'X-Merchant-Key': this.merchantKey,
        },
      });

      return {
        success: true,
        refundTrxID: response.data.refund_id,
        data: response.data,
      };
    } catch (error) {
      console.error('Upay refund error:', error.response?.data || error.message);
      return {
        success: false,
        error: 'Refund failed',
      };
    }
  }
}

module.exports = new UpayService();
