package com.khabarexpress.buyer.domain.model

data class User(
    val id: String,
    val name: String,
    val email: String,
    val phone: String,
    val profileImageUrl: String? = null,
    val savedAddresses: List<Address> = emptyList(),
    val createdAt: Long = System.currentTimeMillis()
)

data class Address(
    val id: String,
    val label: String, // Home, Work, Other
    val houseNo: String,
    val roadNo: String,
    val area: String,           // e.g., Gulshan, Dhanmondi
    val thana: String,          // Police station area
    val district: String,       // Dhaka, Chittagong, etc.
    val division: String,       // Dhaka, Chittagong, Sylhet, etc.
    val postalCode: String,
    val landmark: String? = null,      // Near X mosque/school
    val deliveryInstructions: String? = null,
    val latitude: Double,
    val longitude: Double,
    val isDefault: Boolean = false
) {
    // Helper function to get full address string
    fun getFullAddress(): String {
        return buildString {
            append("House: $houseNo, Road: $roadNo, $area")
            append(", $thana, $district")
            append(", $division - $postalCode")
            landmark?.let { append("\nLandmark: $it") }
        }
    }
}
