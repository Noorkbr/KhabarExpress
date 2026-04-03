package com.khabarexpress.buyer.util

object Constants {
    // Bangladesh Locations
    object Locations {
        val DHAKA_AREAS = listOf(
            "Gulshan", "Banani", "Dhanmondi", "Uttara", "Mirpur", "Mohammadpur",
            "Bashundhara", "Motijheel", "Farmgate", "Tejgaon", "Badda", "Rampura",
            "Khilgaon", "Jatrabari", "Mohakhali", "Lalmatia", "New Market"
        )
        
        val CHITTAGONG_AREAS = listOf(
            "GEC Circle", "Agrabad", "Nasirabad", "Khulshi", "Panchlaish", "Halishahar"
        )
        
        val SYLHET_AREAS = listOf(
            "Zindabazar", "Amberkhana", "Bondor Bazar", "Mira Bazar"
        )
        
        val DISTRICTS = listOf(
            "Dhaka", "Chittagong", "Sylhet", "Rajshahi", "Khulna", "Barisal", 
            "Rangpur", "Mymensingh", "Comilla", "Gazipur", "Narayanganj"
        )
        
        val DIVISIONS = listOf(
            "Dhaka", "Chittagong", "Sylhet", "Rajshahi", "Khulna", 
            "Barisal", "Rangpur", "Mymensingh"
        )
    }
    
    // Bangladesh Cuisine Categories
    object Cuisines {
        val CATEGORIES = listOf(
            "Bengali Traditional",
            "Biriyani & Tehari",
            "Kacchi House",
            "Chinese-Bangla",
            "Thai-Bangla",
            "Fast Food",
            "Street Food",
            "Mishti & Sweets",
            "Iftar Special",
            "Breakfast",
            "Healthy",
            "Café & Coffee"
        )
    }
    
    // Phone number validation
    object Phone {
        val BD_OPERATORS = mapOf(
            "017" to "Grameenphone",
            "018" to "Robi",
            "019" to "Banglalink",
            "015" to "Teletalk",
            "016" to "Airtel",
            "013" to "Grameenphone"
        )
        
        const val BD_COUNTRY_CODE = "+880"
    }
    
    // Sample Restaurant Names (Bangladesh style)
    object Restaurants {
        val SAMPLE_NAMES = listOf(
            "Star Kabab & Restaurant",
            "Kacchi Bhai",
            "Sultans Dine",
            "Madchef",
            "Chillox",
            "Takeout",
            "Pizza Hut BD",
            "KFC Bangladesh",
            "Café Cinnamon",
            "North End Coffee Roasters",
            "The Food Republic",
            "Fakruddin Biriyani",
            "Handi Restaurant"
        )
    }
    
    // Menu Items (Bangladesh style)
    object MenuItems {
        val BIRIYANI_ITEMS = listOf(
            "Kacchi Biriyani (Half)",
            "Kacchi Biriyani (Full)",
            "Tehari with Beef",
            "Morog Polao",
            "Chicken Biriyani"
        )
        
        val TRADITIONAL_ITEMS = listOf(
            "Beef Bhuna",
            "Chicken Roast",
            "Mutton Rezala",
            "Shorshe Ilish",
            "Prawn Malai Curry"
        )
        
        val STREET_FOOD = listOf(
            "Fuchka (8 pcs)",
            "Chotpoti",
            "Jhalmuri",
            "Singara",
            "Samosa"
        )
        
        val BEVERAGES = listOf(
            "Borhani",
            "Lassi",
            "Matha",
            "Cha (Tea)"
        )
        
        val SWEETS = listOf(
            "Roshogolla",
            "Sandesh",
            "Doi",
            "Firni",
            "Jilapi"
        )
    }
    
    // Currency
    const val CURRENCY_SYMBOL = "৳"
    const val CURRENCY_CODE = "BDT"
    
    // VAT
    const val VAT_PERCENTAGE = 5.0 // 5% VAT in Bangladesh
    const val TAX_RATE = 0.05 // 5% VAT as decimal
    
    // Delivery
    const val DEFAULT_DELIVERY_FEE = 30.0 // ৳30
    const val FREE_DELIVERY_THRESHOLD = 500.0 // Free delivery above ৳500
    
    // Validation
    const val MIN_PASSWORD_LENGTH = 6
    const val MIN_NAME_LENGTH = 2
    const val BD_PHONE_PATTERN = "^(\\+?88)?01[3-9]\\d{8}$"
}
