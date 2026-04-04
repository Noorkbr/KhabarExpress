package com.khabarexpress.seller.data.remote.api

import com.khabarexpress.seller.data.remote.dto.*
import retrofit2.Response
import retrofit2.http.*

interface MenuApi {
    @GET("menu-items/restaurant/{restaurantId}")
    suspend fun getMenuItems(
        @Path("restaurantId") restaurantId: String
    ): Response<MenuItemListResponse>

    @POST("menu-items")
    suspend fun createMenuItem(
        @Header("Authorization") token: String,
        @Body request: CreateMenuItemRequest
    ): Response<MenuItemResponse>

    @PUT("menu-items/{id}")
    suspend fun updateMenuItem(
        @Header("Authorization") token: String,
        @Path("id") menuItemId: String,
        @Body request: UpdateMenuItemRequest
    ): Response<MenuItemResponse>

    @PATCH("menu-items/{id}/availability")
    suspend fun toggleAvailability(
        @Header("Authorization") token: String,
        @Path("id") menuItemId: String
    ): Response<MenuItemResponse>

    @DELETE("menu-items/{id}")
    suspend fun deleteMenuItem(
        @Header("Authorization") token: String,
        @Path("id") menuItemId: String
    ): Response<Unit>
}
