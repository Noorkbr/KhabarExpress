package com.khabarexpress.buyer.util

import com.khabarexpress.buyer.domain.model.*

object SampleData {
    
    fun getBangladeshRestaurants() = listOf(
        getStarKababRestaurant(),
        getKacchiBhaiRestaurant(),
        getSultansDineRestaurant(),
        getChilloxRestaurant(),
        getFakruddinRestaurant(),
        getCafeCinnamonRestaurant(),
        getKFCBangladeshRestaurant(),
        getFoodRepublicRestaurant(),
        getMadchefRestaurant(),
        getTakeoutRestaurant()
    )
    
    private fun getStarKababRestaurant() = Restaurant(
        id = "1",
        name = "Star Kabab & Restaurant",
        description = "Authentic Mughlai cuisine with premium kacchi biriyani",
        imageUrl = "https://images.unsplash.com/photo-1563379091339-03b21ab4a4f8?w=800",
        cuisine = listOf("Bengali Traditional", "Biriyani & Tehari", "Mughlai"),
        rating = 4.6,
        totalReviews = 450,
        deliveryTime = 35,
        deliveryFee = 0.0,
        minOrderAmount = 300.0,
        isOpen = true,
        distance = 2.1,
        latitude = 23.7808875,
        longitude = 90.4133503,
        address = "Gulshan 2, Dhaka",
        tags = listOf("Featured", "Free Delivery"),
        categories = listOf(
            MenuCategory(
                id = "cat1",
                name = "Biriyani Special",
                items = listOf(
                    MenuItem(
                        id = "item1",
                        name = "Kacchi Biriyani (Half)",
                        description = "Premium mutton kacchi biriyani with aromatic rice",
                        price = 350.0,
                        imageUrl = "https://images.unsplash.com/photo-1563379091339-03b21ab4a4f8?w=400",
                        customizations = listOf(
                            CustomizationOption(
                                id = "spice1",
                                name = "Spice Level",
                                type = CustomizationType.SINGLE_SELECT,
                                options = listOf(
                                    CustomizationChoice("mild", "Mild", 0.0),
                                    CustomizationChoice("medium", "Medium", 0.0),
                                    CustomizationChoice("hot", "Hot", 0.0)
                                ),
                                isRequired = true
                            )
                        ),
                        rating = 4.8,
                        totalOrders = 1250
                    ),
                    MenuItem(
                        id = "item2",
                        name = "Kacchi Biriyani (Full)",
                        description = "Full plate premium mutton kacchi biriyani",
                        price = 650.0,
                        imageUrl = "https://images.unsplash.com/photo-1563379091339-03b21ab4a4f8?w=400",
                        rating = 4.8,
                        totalOrders = 980
                    ),
                    MenuItem(
                        id = "item3",
                        name = "Tehari with Beef",
                        description = "Dhaka style beef tehari with special spices",
                        price = 280.0,
                        rating = 4.5,
                        totalOrders = 650
                    ),
                    MenuItem(
                        id = "item4",
                        name = "Morog Polao",
                        description = "Chicken polao with boiled egg",
                        price = 250.0,
                        rating = 4.4,
                        totalOrders = 420
                    )
                )
            ),
            MenuCategory(
                id = "cat2",
                name = "Traditional Dishes",
                items = listOf(
                    MenuItem(
                        id = "item5",
                        name = "Beef Bhuna",
                        description = "Slow cooked beef with traditional spices",
                        price = 320.0,
                        rating = 4.6,
                        totalOrders = 380
                    ),
                    MenuItem(
                        id = "item6",
                        name = "Chicken Roast",
                        description = "Marinated chicken roast with special masala",
                        price = 280.0,
                        rating = 4.5,
                        totalOrders = 520
                    )
                )
            ),
            MenuCategory(
                id = "cat3",
                name = "Beverages",
                items = listOf(
                    MenuItem(
                        id = "item7",
                        name = "Borhani",
                        description = "Traditional spiced yogurt drink",
                        price = 60.0,
                        rating = 4.3,
                        totalOrders = 890
                    ),
                    MenuItem(
                        id = "item8",
                        name = "Lassi",
                        description = "Sweet yogurt drink",
                        price = 80.0,
                        rating = 4.2,
                        totalOrders = 450
                    )
                )
            ),
            MenuCategory(
                id = "cat4",
                name = "Sweets",
                items = listOf(
                    MenuItem(
                        id = "item9",
                        name = "Firni",
                        description = "Traditional rice pudding",
                        price = 100.0,
                        rating = 4.4,
                        totalOrders = 320
                    )
                )
            )
        )
    )
    
    private fun getKacchiBhaiRestaurant() = Restaurant(
        id = "2",
        name = "Kacchi Bhai",
        description = "Home of the best kacchi biriyani in Dhaka",
        imageUrl = "https://images.unsplash.com/photo-1633945274405-b6c8069047b0?w=800",
        cuisine = listOf("Kacchi House", "Biriyani & Tehari"),
        rating = 4.8,
        totalReviews = 890,
        deliveryTime = 40,
        deliveryFee = 30.0,
        minOrderAmount = 400.0,
        isOpen = true,
        distance = 3.5,
        latitude = 23.7925,
        longitude = 90.4078,
        address = "Banani, Dhaka",
        tags = listOf("Popular", "Most Ordered"),
        categories = listOf(
            MenuCategory(
                id = "cat1",
                name = "Signature Kacchi",
                items = listOf(
                    MenuItem(
                        id = "k1",
                        name = "Premium Kacchi (Half)",
                        description = "Our signature kacchi with tender mutton",
                        price = 420.0,
                        imageUrl = "https://images.unsplash.com/photo-1563379091339-03b21ab4a4f8?w=400",
                        rating = 4.9,
                        totalOrders = 2100
                    ),
                    MenuItem(
                        id = "k2",
                        name = "Premium Kacchi (Full)",
                        description = "Full plate signature kacchi",
                        price = 780.0,
                        rating = 4.9,
                        totalOrders = 1850
                    )
                )
            )
        )
    )
    
    private fun getSultansDineRestaurant() = Restaurant(
        id = "3",
        name = "Sultans Dine",
        description = "Traditional Bengali and Mughlai dishes",
        imageUrl = "https://images.unsplash.com/photo-1585937421612-70a008356fbe?w=800",
        cuisine = listOf("Bengali Traditional", "Mughlai", "Biriyani & Tehari"),
        rating = 4.5,
        totalReviews = 320,
        deliveryTime = 30,
        deliveryFee = 40.0,
        minOrderAmount = 350.0,
        isOpen = true,
        distance = 1.8,
        latitude = 23.8103,
        longitude = 90.4125,
        address = "Uttara, Dhaka",
        tags = listOf("Featured")
    )
    
    private fun getChilloxRestaurant() = Restaurant(
        id = "4",
        name = "Chillox",
        description = "Modern café with fusion dishes and great ambiance",
        imageUrl = "https://images.unsplash.com/photo-1517248135467-4c7edcad34c4?w=800",
        cuisine = listOf("Café & Coffee", "Fast Food", "Chinese-Bangla"),
        rating = 4.4,
        totalReviews = 275,
        deliveryTime = 25,
        deliveryFee = 50.0,
        minOrderAmount = 250.0,
        isOpen = true,
        distance = 2.8,
        latitude = 23.7465,
        longitude = 90.3763,
        address = "Dhanmondi, Dhaka",
        tags = listOf("Trending"),
        categories = listOf(
            MenuCategory(
                id = "cat1",
                name = "Fast Food",
                items = listOf(
                    MenuItem(
                        id = "ch1",
                        name = "Chicken Burger",
                        description = "Grilled chicken burger with special sauce",
                        price = 280.0,
                        rating = 4.5,
                        totalOrders = 520
                    ),
                    MenuItem(
                        id = "ch2",
                        name = "Beef Burger",
                        description = "Juicy beef patty with cheese",
                        price = 320.0,
                        rating = 4.6,
                        totalOrders = 480
                    )
                )
            ),
            MenuCategory(
                id = "cat2",
                name = "Chinese-Bangla",
                items = listOf(
                    MenuItem(
                        id = "ch3",
                        name = "Chicken Chowmein",
                        description = "Stir-fried noodles with chicken and vegetables",
                        price = 250.0,
                        rating = 4.3,
                        totalOrders = 650
                    ),
                    MenuItem(
                        id = "ch4",
                        name = "Thai Soup",
                        description = "Spicy Thai soup with seafood",
                        price = 280.0,
                        rating = 4.4,
                        totalOrders = 320
                    )
                )
            )
        )
    )
    
    private fun getFakruddinRestaurant() = Restaurant(
        id = "5",
        name = "Fakruddin Biriyani",
        description = "Legendary biriyani since 1985",
        imageUrl = "https://images.unsplash.com/photo-1563379091339-03b21ab4a4f8?w=800",
        cuisine = listOf("Biriyani & Tehari", "Bengali Traditional"),
        rating = 4.7,
        totalReviews = 1240,
        deliveryTime = 45,
        deliveryFee = 35.0,
        minOrderAmount = 500.0,
        isOpen = true,
        distance = 4.2,
        latitude = 23.7644,
        longitude = 90.3686,
        address = "Old Dhaka",
        tags = listOf("Featured", "Popular", "Most Ordered")
    )
    
    private fun getCafeCinnamonRestaurant() = Restaurant(
        id = "6",
        name = "Café Cinnamon",
        description = "Premium coffee and continental breakfast",
        imageUrl = "https://images.unsplash.com/photo-1554118811-1e0d58224f24?w=800",
        cuisine = listOf("Café & Coffee", "Breakfast", "Healthy"),
        rating = 4.3,
        totalReviews = 156,
        deliveryTime = 20,
        deliveryFee = 40.0,
        minOrderAmount = 200.0,
        isOpen = true,
        distance = 1.5,
        latitude = 23.7808,
        longitude = 90.4217,
        address = "Gulshan 1, Dhaka",
        tags = listOf("New"),
        categories = listOf(
            MenuCategory(
                id = "cat1",
                name = "Coffee",
                items = listOf(
                    MenuItem(
                        id = "caf1",
                        name = "Cappuccino",
                        description = "Classic cappuccino with steamed milk",
                        price = 180.0,
                        rating = 4.5,
                        totalOrders = 420
                    ),
                    MenuItem(
                        id = "caf2",
                        name = "Cold Coffee",
                        description = "Iced coffee with cream",
                        price = 200.0,
                        rating = 4.6,
                        totalOrders = 380
                    )
                )
            ),
            MenuCategory(
                id = "cat2",
                name = "Breakfast",
                items = listOf(
                    MenuItem(
                        id = "caf3",
                        name = "Paratha with Omelette",
                        description = "Traditional paratha with egg omelette",
                        price = 150.0,
                        rating = 4.4,
                        totalOrders = 290
                    )
                )
            )
        )
    )
    
    private fun getKFCBangladeshRestaurant() = Restaurant(
        id = "7",
        name = "KFC Bangladesh",
        description = "Finger lickin' good fried chicken",
        imageUrl = "https://images.unsplash.com/photo-1626082927389-6cd097cdc6ec?w=800",
        cuisine = listOf("Fast Food", "American"),
        rating = 4.2,
        totalReviews = 580,
        deliveryTime = 30,
        deliveryFee = 0.0,
        minOrderAmount = 300.0,
        isOpen = true,
        distance = 2.3,
        latitude = 23.7925,
        longitude = 90.4078,
        address = "Banani, Dhaka",
        tags = listOf("Free Delivery")
    )
    
    private fun getFoodRepublicRestaurant() = Restaurant(
        id = "8",
        name = "The Food Republic",
        description = "Multi-cuisine restaurant with diverse menu",
        imageUrl = "https://images.unsplash.com/photo-1552566626-52f8b828add9?w=800",
        cuisine = listOf("Chinese-Bangla", "Thai-Bangla", "Fast Food"),
        rating = 4.4,
        totalReviews = 412,
        deliveryTime = 35,
        deliveryFee = 45.0,
        minOrderAmount = 350.0,
        isOpen = true,
        distance = 3.1,
        latitude = 23.8041,
        longitude = 90.4152,
        address = "Bashundhara, Dhaka",
        tags = listOf("Popular")
    )
    
    private fun getMadchefRestaurant() = Restaurant(
        id = "9",
        name = "Madchef",
        description = "Contemporary fusion cuisine with a twist",
        imageUrl = "https://images.unsplash.com/photo-1414235077428-338989a2e8c0?w=800",
        cuisine = listOf("Fast Food", "Chinese-Bangla", "Thai-Bangla"),
        rating = 4.3,
        totalReviews = 298,
        deliveryTime = 30,
        deliveryFee = 35.0,
        minOrderAmount = 300.0,
        isOpen = true,
        distance = 2.6,
        latitude = 23.7515,
        longitude = 90.3883,
        address = "Mohammadpur, Dhaka",
        tags = listOf("Trending")
    )
    
    private fun getTakeoutRestaurant() = Restaurant(
        id = "10",
        name = "Takeout",
        description = "Quick bites and delicious meals on the go",
        imageUrl = "https://images.unsplash.com/photo-1504674900247-0877df9cc836?w=800",
        cuisine = listOf("Fast Food", "Street Food", "Bengali Traditional"),
        rating = 4.1,
        totalReviews = 205,
        deliveryTime = 20,
        deliveryFee = 30.0,
        minOrderAmount = 200.0,
        isOpen = true,
        distance = 1.2,
        latitude = 23.7583,
        longitude = 90.3714,
        address = "Mirpur, Dhaka",
        tags = listOf("Fast Delivery"),
        categories = listOf(
            MenuCategory(
                id = "cat1",
                name = "Street Food",
                items = listOf(
                    MenuItem(
                        id = "st1",
                        name = "Fuchka (8 pcs)",
                        description = "Crispy fuchka with spicy tamarind water",
                        price = 80.0,
                        isVegetarian = true,
                        rating = 4.2,
                        totalOrders = 680
                    ),
                    MenuItem(
                        id = "st2",
                        name = "Chotpoti",
                        description = "Traditional chotpoti with egg",
                        price = 100.0,
                        rating = 4.3,
                        totalOrders = 520
                    ),
                    MenuItem(
                        id = "st3",
                        name = "Singara (4 pcs)",
                        description = "Crispy samosa filled with spiced potatoes",
                        price = 60.0,
                        isVegetarian = true,
                        rating = 4.1,
                        totalOrders = 420
                    )
                )
            )
        )
    )
}
