package com.example.minforstaapp

import java.time.LocalDateTime
import java.time.Duration

class Dog(
    var name: String,
    var breed: String,
    val chipNumber: String,
    var gender: String,
    var age: Int,
    var canSitWithOthers: Boolean,
    var neutered: Boolean,
    var needsGrooming: Boolean,
    var visionImpaired: Boolean,
    var hearingImpaired: Boolean
) {
    // Status för incheckning (motsvarar dina privata variabler)
    var isCheckedIn: Boolean = false
        private set // Kan bara ändras inifrån klassen via funktionen nedan

    var checkInTime: LocalDateTime? = null
    var checkOutTime: LocalDateTime? = null

    companion object {
        private const val HOURLY_RATE = 50.0
    }

    // Motsvarar public void setCheckedIn(boolean checkedIn)
    fun updateCheckInStatus(checkedIn: Boolean) {
        if (this.isCheckedIn == checkedIn) return

        this.isCheckedIn = checkedIn

        if (checkedIn) {
            checkInTime = LocalDateTime.now()
            checkOutTime = null
        } else {
            checkOutTime = LocalDateTime.now()
        }
    }

    // Motsvarar public long getStayMinutes()
    fun getStayMinutes(): Long {
        val startTime = checkInTime ?: return 0
        val endTime = if (isCheckedIn) LocalDateTime.now() else checkOutTime ?: return 0

        val minutes = Duration.between(startTime, endTime).toMinutes()
        return Math.max(0, minutes)
    }

    // Motsvarar public double getCost()
    fun getCost(): Double {
        return getStayMinutes() * HOURLY_RATE / 60.0
    }
}