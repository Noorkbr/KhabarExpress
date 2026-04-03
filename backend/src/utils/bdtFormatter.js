// Format BDT currency
exports.formatBDT = (amountInPaisa) => {
  const bdt = amountInPaisa / 100;
  return `৳${bdt.toFixed(2)}`;
};

// Convert BDT to paisa
exports.bdtToPaisa = (bdt) => {
  return Math.round(bdt * 100);
};

// Convert paisa to BDT
exports.paisaToBdt = (paisa) => {
  return paisa / 100;
};

// Format with Bangla number system (optional)
exports.formatBanglaNumber = (number) => {
  const banglaDigits = ['০', '১', '২', '৩', '৪', '৫', '৬', '৭', '৮', '৯'];
  return number.toString().split('').map(digit => {
    return /\d/.test(digit) ? banglaDigits[parseInt(digit)] : digit;
  }).join('');
};
