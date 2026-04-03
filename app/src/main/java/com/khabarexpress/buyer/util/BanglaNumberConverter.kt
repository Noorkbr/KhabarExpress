package com.khabarexpress.buyer.util

object BanglaNumberConverter {
    private val englishToBanglaMap = mapOf(
        '0' to '০', '1' to '১', '2' to '২', '3' to '৩', '4' to '৪',
        '5' to '৫', '6' to '৬', '7' to '৭', '8' to '৮', '9' to '৯'
    )
    
    private val banglaToEnglishMap = mapOf(
        '০' to '0', '১' to '1', '২' to '2', '৩' to '3', '৪' to '4',
        '৫' to '5', '৬' to '6', '৭' to '7', '৮' to '8', '৯' to '9'
    )
    
    /**
     * Converts English numerals to Bangla numerals
     * Example: "123" -> "১২৩"
     */
    fun toBangla(text: String): String {
        return text.map { char ->
            englishToBanglaMap[char] ?: char
        }.joinToString("")
    }
    
    /**
     * Converts Bangla numerals to English numerals
     * Example: "১২৩" -> "123"
     */
    fun toEnglish(text: String): String {
        return text.map { char ->
            banglaToEnglishMap[char] ?: char
        }.joinToString("")
    }
    
    /**
     * Converts integer to Bangla numeral string
     */
    fun intToBangla(number: Int): String {
        return toBangla(number.toString())
    }
    
    /**
     * Converts double to Bangla numeral string
     */
    fun doubleToBangla(number: Double): String {
        return toBangla(number.toString())
    }
}
