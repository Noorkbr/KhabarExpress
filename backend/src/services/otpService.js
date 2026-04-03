const { cache } = require('../config/redis');

class OTPService {
  constructor() {
    this.otpExpiry = parseInt(process.env.OTP_EXPIRY_MINUTES || '10') * 60; // seconds
    this.otpLength = parseInt(process.env.OTP_LENGTH || '6');
  }

  // Generate OTP
  generateOTP() {
    const digits = '0123456789';
    let otp = '';
    for (let i = 0; i < this.otpLength; i++) {
      otp += digits[Math.floor(Math.random() * 10)];
    }
    return otp;
  }

  // Send OTP via SMS (integration with Bangladesh SMS providers)
  async sendOTP(phone) {
    try {
      const otp = this.generateOTP();

      // Store OTP in Redis cache
      const key = `otp:${phone}`;
      await cache.set(key, otp, this.otpExpiry);

      // TODO: Integrate with Bangladesh SMS providers
      // Options: SSL Wireless, Banglalink, Robi, etc.
      // Example:
      // await this.sendSMS(phone, `Your KhabarLagbe OTP is: ${otp}. Valid for ${process.env.OTP_EXPIRY_MINUTES} minutes.`);

      // For development, log OTP
      if (process.env.NODE_ENV === 'development') {
        console.log(`📱 OTP for ${phone}: ${otp}`);
      }

      return true;
    } catch (error) {
      console.error('OTP send error:', error);
      return false;
    }
  }

  // Verify OTP
  async verifyOTP(phone, otp) {
    try {
      const key = `otp:${phone}`;
      const storedOTP = await cache.get(key);

      if (!storedOTP) {
        return false; // OTP expired or doesn't exist
      }

      const isValid = storedOTP === otp;

      if (isValid) {
        // Delete OTP after successful verification
        await cache.del(key);
      }

      return isValid;
    } catch (error) {
      console.error('OTP verify error:', error);
      return false;
    }
  }

  // Send SMS (placeholder for actual SMS gateway integration)
  async sendSMS(phone, message) {
    try {
      // Integration with Bangladesh SMS providers
      // SSL Wireless API
      // const response = await axios.post('https://sms.sslwireless.com/pushapi/dynamic/server.php', {
      //   user: process.env.SSL_SMS_USER,
      //   pass: process.env.SSL_SMS_PASSWORD,
      //   sid: process.env.SSL_SMS_SID,
      //   msisdn: phone,
      //   sms: message,
      // });

      console.log(`SMS to ${phone}: ${message}`);
      return true;
    } catch (error) {
      console.error('SMS send error:', error);
      return false;
    }
  }
}

module.exports = new OTPService();
