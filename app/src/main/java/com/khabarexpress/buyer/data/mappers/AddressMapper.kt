package com.khabarexpress.buyer.data.mappers

import com.khabarexpress.buyer.data.local.entity.AddressEntity
import com.khabarexpress.buyer.data.remote.dto.AddressDto
import com.khabarexpress.buyer.domain.model.Address

/**
 * Mapper functions for address related models
 */

/**
 * Convert AddressDto from API to domain Address model
 */
fun AddressDto.toDomainModel(): Address {
    return Address(
        id = id,
        label = label,
        houseNo = houseNo,
        roadNo = roadNo,
        area = area,
        thana = thana,
        district = district,
        division = division,
        postalCode = postalCode,
        landmark = landmark,
        deliveryInstructions = deliveryInstructions,
        latitude = latitude,
        longitude = longitude,
        isDefault = false // Default status is managed locally
    )
}

/**
 * Convert AddressEntity from Room database to domain Address model
 */
fun AddressEntity.toDomainModel(): Address {
    return Address(
        id = id,
        label = label,
        houseNo = houseNo,
        roadNo = roadNo,
        area = area,
        thana = thana,
        district = district,
        division = division,
        postalCode = postalCode,
        landmark = landmark,
        deliveryInstructions = deliveryInstructions,
        latitude = latitude,
        longitude = longitude,
        isDefault = isDefault
    )
}

/**
 * Convert domain Address to AddressEntity for Room database
 */
fun Address.toEntity(userId: String): AddressEntity {
    return AddressEntity(
        id = id,
        userId = userId,
        label = label,
        houseNo = houseNo,
        roadNo = roadNo,
        area = area,
        thana = thana,
        district = district,
        division = division,
        postalCode = postalCode,
        landmark = landmark,
        deliveryInstructions = deliveryInstructions,
        latitude = latitude,
        longitude = longitude,
        isDefault = isDefault
    )
}

/**
 * Convert domain Address to AddressDto for API
 */
fun Address.toDto(): AddressDto {
    return AddressDto(
        id = id,
        label = label,
        houseNo = houseNo,
        roadNo = roadNo,
        area = area,
        thana = thana,
        district = district,
        division = division,
        postalCode = postalCode,
        landmark = landmark,
        deliveryInstructions = deliveryInstructions,
        latitude = latitude,
        longitude = longitude
    )
}
