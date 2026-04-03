const axios = require('axios');
const crypto = require('crypto');

class NagadService {
  constructor() {
    this.baseURL = process.env.NAGAD_BASE_URL || 'http://sandbox.mynagad.com:10080/remote-payment-gateway-1.0/api/dfs';
    this.merchantId = process.env.NAGAD_MERCHANT_ID;
    this.merchantNumber = process.env.NAGAD_MERCHANT_NUMBER;
    this.publicKey = process.env.NAGAD_PUBLIC_KEY;
    this.privateKey = process.env.NAGAD_PRIVATE_KEY;
  }

  // Generate signature
  generateSignature(data) {
    const sign = crypto.createSign('SHA256');
    sign.update(data);
    sign.end();
    return sign.sign(this.privateKey, 'base64');
  }

  // Initiate payment
  async initiatePayment(amount, orderId) {
    try {
      const timestamp = Date.now().toString();
      const sensitiveData = {
        merchantId: this.merchantId,
        datetime: timestamp,
        orderId: orderId,
        challenge: this.generateRandomString(40),
      };

      const payload = Buffer.from(JSON.stringify(sensitiveData)).toString('base64');
      const signature = this.generateSignature(payload);

      const response = await axios.post(
        `${this.baseURL}/check-out/initialize/${this.merchantId}/${orderId}`,
        {
          dateTime: timestamp,
          sensitiveData: payload,
          signature: signature,
        },
        {
          headers: {
            'Content-Type': 'application/json',
            'X-KM-Api-Version': 'v-0.2.0',
            'X-KM-IP-V4': '103.100.0.1',
            'X-KM-Client-Type': 'PC_WEB',
          },
        }
      );

      return {
        success: true,
        paymentRef: response.data.paymentReferenceId,
        challenge: response.data.challenge,
      };
    } catch (error) {
      console.error('Nagad initiate error:', error.response?.data || error.message);
      return {
        success: false,
        error: 'Payment initiation failed',
      };
    }
  }

  // Complete payment
  async completePayment(paymentRef, challenge) {
    try {
      const sensitiveData = {
        merchantId: this.merchantId,
        orderId: paymentRef,
        currencyCode: '050', // BDT
        amount: '100',
        challenge: challenge,
      };

      const payload = Buffer.from(JSON.stringify(sensitiveData)).toString('base64');
      const signature = this.generateSignature(payload);

      const response = await axios.post(
        `${this.baseURL}/check-out/complete/${paymentRef}`,
        {
          paymentReferenceId: paymentRef,
          sensitiveData: payload,
          signature: signature,
          merchantCallbackURL: process.env.NAGAD_CALLBACK_URL,
        },
        {
          headers: {
            'Content-Type': 'application/json',
            'X-KM-Api-Version': 'v-0.2.0',
          },
        }
      );

      return {
        success: response.data.status === 'Success',
        callbackUrl: response.data.callBackUrl,
        data: response.data,
      };
    } catch (error) {
      console.error('Nagad complete error:', error.response?.data || error.message);
      return {
        success: false,
        error: 'Payment completion failed',
      };
    }
  }

  // Verify payment
  async verifyPayment(paymentRef) {
    try {
      const response = await axios.get(
        `${this.baseURL}/verify/payment/${paymentRef}`,
        {
          headers: {
            'X-KM-Api-Version': 'v-0.2.0',
          },
        }
      );

      return {
        success: response.data.status === 'Success',
        data: response.data,
      };
    } catch (error) {
      console.error('Nagad verify error:', error.response?.data || error.message);
      return {
        success: false,
        error: 'Verification failed',
      };
    }
  }

  generateRandomString(length) {
    return crypto.randomBytes(length).toString('hex').slice(0, length);
  }
}

module.exports = new NagadService();
