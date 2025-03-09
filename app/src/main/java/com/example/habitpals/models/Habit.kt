package com.example.habitpals.models

data class Habit(
    val habitId: String = "",
    val title: String = "",
    val duration: Int = 0,
    var progress: Int = 0
)
