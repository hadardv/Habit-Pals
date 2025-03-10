package com.example.habitpals.models

data class FeedUpdate(
    val friendId: String,
    val friendName: String,
    val type: String,
    val habitName: String,
    val timestamp: Long,
    val profilePicture: String,
    val likes: Int = 0,
    val feedDocumentId: String
)
