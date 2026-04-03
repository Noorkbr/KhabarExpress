package com.khabarexpress.buyer.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "recent_searches")
data class RecentSearchEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val query: String,
    val searchType: String, // "restaurant", "dish", "cuisine"
    val timestamp: Long
)
