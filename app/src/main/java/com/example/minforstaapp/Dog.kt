package com.example.minforstaapp

data class Dog(
    var name: String,
    var breed: String,
    val chipNumber: String,
    val gender: String,
    var age: Int,
    val sitsNicely: Boolean,
    val isNeutered: Boolean,
    var needsGrooming: Boolean,
    val visionImpaired: Boolean,
    val hearingImpaired: Boolean,
    var isCheckedIn: Boolean = false
) {
    fun getCost(): Double {
        return 150.0
    }
}