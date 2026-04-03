// Validate Bangladesh phone number format
exports.isValidBDPhone = (phone) => {
  // Format: +880XXXXXXXXXX (13 digits total)
  const bdPhoneRegex = /^\+880[1-9]\d{9}$/;
  return bdPhoneRegex.test(phone);
};

// Format phone number to Bangladesh format
exports.formatBDPhone = (phone) => {
  // Remove all non-digit characters
  let cleaned = phone.replace(/\D/g, '');
  
  // If starts with 880, add +
  if (cleaned.startsWith('880')) {
    return `+${cleaned}`;
  }
  
  // If starts with 0, replace with +880
  if (cleaned.startsWith('0')) {
    return `+880${cleaned.substring(1)}`;
  }
  
  // If just 10 digits, add +880
  if (cleaned.length === 10) {
    return `+880${cleaned}`;
  }
  
  return phone;
};

// Get phone operator
exports.getPhoneOperator = (phone) => {
  const cleaned = phone.replace(/\D/g, '');
  const prefix = cleaned.substring(cleaned.length - 10, cleaned.length - 8);
  
  const operators = {
    '13': 'Grameenphone',
    '14': 'Banglalink',
    '15': 'Teletalk',
    '16': 'Airtel',
    '17': 'Grameenphone',
    '18': 'Robi',
    '19': 'Banglalink',
  };
  
  return operators[prefix] || 'Unknown';
};
