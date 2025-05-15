package com.example.trevana.models

import com.google.firebase.Timestamp
import com.google.firebase.firestore.GeoPoint
import com.google.firebase.firestore.IgnoreExtraProperties

@IgnoreExtraProperties
data class Post(
    // Required Fields
    val title: String = "",
    val price: Double = 0.0,
    val category: String = "",
    val description: String = "",
    val images: List<String> = emptyList(),
    val userId: String = "",
    val timestamp: Timestamp = Timestamp.now(),

    // Optional Fields
    val location: String = "",                   // Text address
    val coordinates: GeoPoint? = null,           // Firestore GeoPoint for maps
    val status: String = "active",               // active/sold/expired
    val searchKeywords: List<String> = emptyList() // For search functionality
) {
    // Empty constructor required for Firestore deserialization
    constructor() : this(
        title = "",
        price = 0.0,
        category = "",
        description = "",
        images = emptyList(),
        userId = "",
        timestamp = Timestamp.now(),
        location = "",
        coordinates = null,
        status = "active",
        searchKeywords = emptyList()
    )
}