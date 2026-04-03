const axios = require('axios');

class SSLCommerzService {
  constructor() {
    this.storeId = process.env.SSLCOMMERZ_STORE_ID;
    this.storePassword = process.env.SSLCOMMERZ_STORE_PASSWORD;
    this.baseURL = process.env.SSLCOMMERZ_IS_LIVE === 'true'
      ? 'https://securepay.sslcommerz.com'
      : 'https://sandbox.sslcommerz.com';
  }

  // Initiate payment session
  async initiateSession(orderData) {
    try {
      const { orderId, amount, customerName, customerEmail, customerPhone, productName } = orderData;

      const data = {
        store_id: this.storeId,
        store_passwd: this.storePassword,
        total_amount: (amount / 100).toFixed(2), // Convert paisa to BDT
        currency: 'BDT',
        tran_id: orderId,
        success_url: `${process.env.BASE_URL}/api/v1/payment/sslcommerz/success`,
        fail_url: `${process.env.BASE_URL}/api/v1/payment/sslcommerz/fail`,
        cancel_url: `${process.env.BASE_URL}/api/v1/payment/sslcommerz/cancel`,
        ipn_url: `${process.env.BASE_URL}/api/v1/payment/sslcommerz/ipn`,
        cus_name: customerName,
        cus_email: customerEmail || 'customer@khabarlagbe.com',
        cus_phone: customerPhone,
        cus_add1: 'Dhaka',
        cus_city: 'Dhaka',
        cus_country: 'Bangladesh',
        shipping_method: 'NO',
        product_name: productName || 'Food Order',
        product_category: 'Food',
        product_profile: 'general',
        multi_card_name: 'visa,master,amex,brac,citybank,dbbl',
      };

      const response = await axios.post(`${this.baseURL}/gwprocess/v4/api.php`, 
        new URLSearchParams(data).toString(),
        {
          headers: {
            'Content-Type': 'application/x-www-form-urlencoded',
          },
        }
      );

      if (response.data.status === 'SUCCESS') {
        return {
          success: true,
          sessionKey: response.data.sessionkey,
          gatewayPageURL: response.data.GatewayPageURL,
        };
      } else {
        return {
          success: false,
          error: response.data.failedreason || 'Session initiation failed',
        };
      }
    } catch (error) {
      console.error('SSLCommerz initiate error:', error.response?.data || error.message);
      return {
        success: false,
        error: 'Payment session creation failed',
      };
    }
  }

  // Validate payment
  async validatePayment(valId) {
    try {
      const response = await axios.get(`${this.baseURL}/validator/api/validationserverAPI.php`, {
        params: {
          val_id: valId,
          store_id: this.storeId,
          store_passwd: this.storePassword,
          format: 'json',
        },
      });

      if (response.data.status === 'VALID' || response.data.status === 'VALIDATED') {
        return {
          success: true,
          data: response.data,
        };
      } else {
        return {
          success: false,
          error: 'Invalid transaction',
        };
      }
    } catch (error) {
      console.error('SSLCommerz validate error:', error.response?.data || error.message);
      return {
        success: false,
        error: 'Validation failed',
      };
    }
  }

  // Initiate refund
  async initiateRefund(bankTranId, refundAmount, refundRemarks) {
    try {
      const data = {
        refund_amount: (refundAmount / 100).toFixed(2),
        refund_remarks: refundRemarks || 'Customer request',
        bank_tran_id: bankTranId,
        refe_id: Date.now().toString(),
        store_id: this.storeId,
        store_passwd: this.storePassword,
      };

      const response = await axios.post(
        `${this.baseURL}/validator/api/merchantTransIDvalidationAPI.php`,
        new URLSearchParams(data).toString(),
        {
          headers: {
            'Content-Type': 'application/x-www-form-urlencoded',
          },
        }
      );

      return {
        success: response.data.status === 'success',
        data: response.data,
      };
    } catch (error) {
      console.error('SSLCommerz refund error:', error.response?.data || error.message);
      return {
        success: false,
        error: 'Refund initiation failed',
      };
    }
  }
}

module.exports = new SSLCommerzService();
