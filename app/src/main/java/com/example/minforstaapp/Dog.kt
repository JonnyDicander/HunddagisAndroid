package com.example.minforstaapp

import kotlin.math.max

data class Dog(
    val name: String,
    val breed: String,
    val chipNumber: String,
    val gender: String,
    val age: Int,
    val sitsNicely: Boolean,
    val isNeutered: Boolean,
    val needsGrooming: Boolean,
    val visionImpaired: Boolean,
    val hearingImpaired: Boolean,
    val isSearchDog: Boolean = false, // Kan lånas ut till Polis / Missing People
    val isCheckedIn: Boolean = false,
    val checkInTimestamp: Long? = null, // Starttid för nuvarande dagispass
    val isOnSearchMission: Boolean = false, // Ute på Polis/MP-uppdrag just nu
    val missionStartTimestamp: Long? = null, // Starttid för nuvarande sökuppdrag
    val todayMinutes: Long = 0L,
    val todayCostExVat: Double = 0.0,
    val monthlyDaycareMinutes: Long = 0L,
    val monthlyDaycareCostExVat: Double = 0.0,
    val monthlyMissionMinutes: Long = 0L,
    val monthlyMissionCostExVat: Double = 0.0,
    val monthlyMissionCount: Int = 0
) {
    companion object {
        const val HOURLY_RATE = 50.0 // 50 kr/h inkl. mat, pälsvård m.m.
        const val MISSION_BASE_FEE = 1000.0 // 1000 kr för upp till 3 timmar
        const val MISSION_INCLUDED_MINUTES = 180L // 3 timmar = 180 minuter
        const val MISSION_EXTRA_HOURLY_RATE = 500.0 // 500 kr/h efter 3 timmar
        const val VAT_RATE = 0.25 // 25 % moms

        fun calculateDaycareCost(minutes: Long): Double {
            return (minutes / 60.0) * HOURLY_RATE
        }

        fun calculateMissionCost(minutes: Long): Double {
            if (minutes <= 0L) return 0.0
            val extraMinutes = max(0L, minutes - MISSION_INCLUDED_MINUTES)
            val extraCost = (extraMinutes / 60.0) * MISSION_EXTRA_HOURLY_RATE
            return MISSION_BASE_FEE + extraCost
        }
    }

    // Räknar ut minuter för pågående incheckning (från 0 vid varje ny incheckning)
    fun getCurrentSessionMinutes(nowMillis: Long = System.currentTimeMillis()): Long {
        if (!isCheckedIn || checkInTimestamp == null) return 0L
        return max(0L, (nowMillis - checkInTimestamp) / 60_000L)
    }

    fun getCurrentSessionSeconds(nowMillis: Long = System.currentTimeMillis()): Long {
        if (!isCheckedIn || checkInTimestamp == null) return 0L
        return max(0L, (nowMillis - checkInTimestamp) / 1000L)
    }

    fun getCurrentSessionCost(nowMillis: Long = System.currentTimeMillis()): Double {
        return calculateDaycareCost(getCurrentSessionMinutes(nowMillis))
    }

    // Räknar ut minuter och kostnad för pågående Polis/Missing People-uppdrag
    fun getCurrentMissionMinutes(nowMillis: Long = System.currentTimeMillis()): Long {
        if (!isOnSearchMission || missionStartTimestamp == null) return 0L
        return max(0L, (nowMillis - missionStartTimestamp) / 60_000L)
    }

    fun getCurrentMissionSeconds(nowMillis: Long = System.currentTimeMillis()): Long {
        if (!isOnSearchMission || missionStartTimestamp == null) return 0L
        return max(0L, (nowMillis - missionStartTimestamp) / 1000L)
    }

    fun getCurrentMissionCost(nowMillis: Long = System.currentTimeMillis()): Double {
        if (!isOnSearchMission) return 0.0
        return calculateMissionCost(max(1L, getCurrentMissionMinutes(nowMillis)))
    }

    // Total kostnad idag (avslutade pass idag + pågående pass)
    fun getTotalTodayCostExVat(nowMillis: Long = System.currentTimeMillis()): Double {
        return todayCostExVat + getCurrentSessionCost(nowMillis) + getCurrentMissionCost(nowMillis)
    }

    // Total månadskostnad exkl. moms (avslutade pass + eventuellt pågående)
    fun getTotalMonthlyCostExVat(nowMillis: Long = System.currentTimeMillis()): Double {
        return monthlyDaycareCostExVat + monthlyMissionCostExVat +
                getCurrentSessionCost(nowMillis) + getCurrentMissionCost(nowMillis)
    }

    fun getTotalMonthlyVat(nowMillis: Long = System.currentTimeMillis()): Double {
        return getTotalMonthlyCostExVat(nowMillis) * VAT_RATE
    }

    fun getTotalMonthlyCostIncVat(nowMillis: Long = System.currentTimeMillis()): Double {
        return getTotalMonthlyCostExVat(nowMillis) * (1.0 + VAT_RATE)
    }
}