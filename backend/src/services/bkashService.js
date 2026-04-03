const axios = require('axios');

class BkashService {
  constructor() {
    this.baseURL = process.env.BKASH_BASE_URL || 'https://checkout.sandbox.bka sh.com/v1.2.0-beta';
    this.appKey = process.env.BKASH_APP_KEY;
    this.appSecret = process.env.BKASH_APP_SECRET;
    this.username = process.env.BKASH_USERNAME;
    this.password = process.env.BKASH_PASSWORD;
    this.token = null;
    this.tokenExpiry = null;
  }

  // Get authentication token
  async getToken() {
    try {
      if (this.token && this.tokenExpiry && new Date() < this.tokenExpiry) {
        return this.token;
      }

      const response = await axios.post(`${this.baseURL}/checkout/token/grant`, {
        app_key: this.appKey,
        app_secret: this.appSecret,
      }, {
        headers: {
          'Content-Type': 'application/json',
          username: this.username,
          password: this.password,
        },
      });

      this.token = response.data.id_token;
      this.tokenExpiry = new Date(Date.now() + 3600 * 1000); // 1 hour
      return this.token;
    } catch (error) {
      console.error('bKash token error:', error.response?.data || error.message);
      throw new Error('Failed to get bKash token');
    }
  }

  // Create payment
  async createPayment(amount, orderId, customerPhone) {
    try {
      const token = await this.getToken();

      const response = await axios.post(`${this.baseURL}/checkout/payment/create`, {
        amount: (amount / 100).toFixed(2), // Convert paisa to BDT
        currency: 'BDT',
        intent: 'sale',
        merchantInvoiceNumber: orderId,
      }, {
        headers: {
          'Content-Type': 'application/json',
          Authorization: `Bearer ${token}`,
          'X-APP-Key': this.appKey,
        },
      });

      return {
        success: true,
        paymentId: response.data.paymentID,
        bkashURL: response.data.bkashURL,
      };
    } catch (error) {
      console.error('bKash create payment error:', error.response?.data || error.message);
      return {
        success: false,
        error: error.response?.data?.errorMessage || 'Payment creation failed',
      };
    }
  }

  // Execute payment
  async executePayment(paymentId) {
    try {
      const token = await this.getToken();

      const response = await axios.post(`${this.baseURL}/checkout/payment/execute/${paymentId}`, {}, {
        headers: {
          'Content-Type': 'application/json',
          Authorization: `Bearer ${token}`,
          'X-APP-Key': this.appKey,
        },
      });

      return {
        success: response.data.transactionStatus === 'Completed',
        transactionId: response.data.trxID,
        data: response.data,
      };
    } catch (error) {
      console.error('bKash execute payment error:', error.response?.data || error.message);
      return {
        success: false,
        error: error.response?.data?.errorMessage || 'Payment execution failed',
      };
    }
  }

  // Query payment status
  async queryPayment(paymentId) {
    try {
      const token = await this.getToken();

      const response = await axios.get(`${this.baseURL}/checkout/payment/query/${paymentId}`, {
        headers: {
          'Content-Type': 'application/json',
          Authorization: `Bearer ${token}`,
          'X-APP-Key': this.appKey,
        },
      });

      return {
        success: true,
        status: response.data.transactionStatus,
        data: response.data,
      };
    } catch (error) {
      console.error('bKash query payment error:', error.response?.data || error.message);
      return {
        success: false,
        error: 'Query failed',
      };
    }
  }

  // Refund payment
  async refund(paymentId, amount, reason) {
    try {
      const token = await this.getToken();

      const response = await axios.post(`${this.baseURL}/checkout/payment/refund`, {
        paymentID: paymentId,
        amount: (amount / 100).toFixed(2),
        trxID: paymentId,
        sku: 'refund',
        reason: reason || 'Customer request',
      }, {
        headers: {
          'Content-Type': 'application/json',
          Authorization: `Bearer ${token}`,
          'X-APP-Key': this.appKey,
        },
      });

      return {
        success: true,
        refundId: response.data.refundTrxID,
        data: response.data,
      };
    } catch (error) {
      console.error('bKash refund error:', error.response?.data || error.message);
      return {
        success: false,
        error: 'Refund failed',
      };
    }
  }
}

module.exports = new BkashService();
