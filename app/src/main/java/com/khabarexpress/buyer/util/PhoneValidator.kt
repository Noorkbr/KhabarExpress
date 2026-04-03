package com.khabarexpress.buyer.util

object PhoneValidator {
    private val BD_PHONE_REGEX = Regex("^\\+880\\s?1[3-9]\\d{8}$")
    private val BD_PHONE_WITHOUT_CODE_REGEX = Regex("^01[3-9]\\d{8}$")
    
    /**
     * Validates Bangladesh phone number
     * Accepts formats:
     * - +880 1XXX-XXXXXX
     * - +8801XXXXXXXXX
     * - 01XXXXXXXXX
     */
    fun isValidBangladeshPhone(phone: String): Boolean {
        val cleaned = phone.replace(Regex("[\\s-]"), "")
        return BD_PHONE_REGEX.matches(cleaned) || BD_PHONE_WITHOUT_CODE_REGEX.matches(cleaned)
    }
    
    /**
     * Formats phone number to standard BD format: +880 1XXX-XXXXXX
     */
    fun formatBangladeshPhone(phone: String): String {
        val cleaned = phone.replace(Regex("[^0-9+]"), "")
        
        return when {
            cleaned.startsWith("+880") -> {
                val number = cleaned.substring(4)
                if (number.length == 10) {
                    "+880 ${number.substring(0, 4)}-${number.substring(4)}"
                } else {
                    cleaned
                }
            }
            cleaned.startsWith("880") -> {
                val number = cleaned.substring(3)
                if (number.length == 10) {
                    "+880 ${number.substring(0, 4)}-${number.substring(4)}"
                } else {
                    cleaned
                }
            }
            cleaned.startsWith("0") && cleaned.length == 11 -> {
                val number = cleaned.substring(1)
                "+880 ${number.substring(0, 4)}-${number.substring(4)}"
            }
            else -> cleaned
        }
    }
    
    /**
     * Gets the operator name from phone number
     */
    fun getOperatorName(phone: String): String? {
        val cleaned = phone.replace(Regex("[^0-9]"), "")
        val prefix = when {
            cleaned.startsWith("880") -> cleaned.substring(3, 6)
            cleaned.startsWith("0") -> cleaned.substring(0, 3)
            else -> return null
        }
        
        return Constants.Phone.BD_OPERATORS[prefix]
    }
    
    /**
     * Converts phone to international format with country code
     * Validates the phone number before conversion
     */
    fun toInternationalFormat(phone: String): String? {
        if (!isValidBangladeshPhone(phone)) {
            return null
        }
        
        val cleaned = phone.replace(Regex("[^0-9]"), "")
        return when {
            cleaned.startsWith("880") -> "+$cleaned"
            cleaned.startsWith("0") && cleaned.length == 11 -> "+880${cleaned.substring(1)}"
            cleaned.startsWith("+880") -> cleaned
            else -> "+880$cleaned"
        }
    }
}
