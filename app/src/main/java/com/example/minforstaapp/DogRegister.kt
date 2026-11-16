package com.example.minforstaapp

import android.content.Context
import androidx.compose.runtime.mutableStateListOf
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlin.math.max

class DogRegister(context: Context) {
    private val prefs = context.getSharedPreferences("dog_prefs", Context.MODE_PRIVATE)
    private val gson = Gson()
    private val dogs = mutableStateListOf<Dog>()

    init {
        loadDogs()
    }

    private fun saveDogs() {
        val json = gson.toJson(dogs.toList())
        prefs.edit().putString("saved_dogs", json).apply()
    }

    private fun loadDogs() {
        val json = prefs.getString("saved_dogs", null)
        if (json != null) {
            val type = object : TypeToken<List<Dog>>() {}.type
            val savedList: List<Dog>? = gson.fromJson(json, type)
            if (savedList != null) {
                dogs.clear()
                dogs.addAll(savedList)
            }
        }
    }

    fun addDog(dog: Dog): Boolean {
        if (findDog(dog.chipNumber) != null) {
            return false
        }
        dogs.add(dog)
        saveDogs()
        return true
    }

    fun updateDog(updatedDog: Dog) {
        val index = dogs.indexOfFirst { it.chipNumber == updatedDog.chipNumber }
        if (index != -1) {
            dogs[index] = updatedDog
            saveDogs()
        }
    }

    // Checkar in hunden och börjar räkna från 0
    fun checkInDog(chipNumber: String) {
        val dog = findDog(chipNumber) ?: return
        if (!dog.isCheckedIn) {
            updateDog(
                dog.copy(
                    isCheckedIn = true,
                    checkInTimestamp = System.currentTimeMillis()
                )
            )
        }
    }

    // Checkar ut hunden, sparar kostnaden till dag & månad och nollställer timern
    fun checkOutDog(chipNumber: String) {
        val dog = findDog(chipNumber) ?: return
        if (dog.isCheckedIn) {
            val now = System.currentTimeMillis()
            val sessionMinutes = max(1L, dog.getCurrentSessionMinutes(now))
            val sessionCost = Dog.calculateDaycareCost(sessionMinutes)

            updateDog(
                dog.copy(
                    isCheckedIn = false,
                    checkInTimestamp = null,
                    todayMinutes = dog.todayMinutes + sessionMinutes,
                    todayCostExVat = dog.todayCostExVat + sessionCost,
                    monthlyDaycareMinutes = dog.monthlyDaycareMinutes + sessionMinutes,
                    monthlyDaycareCostExVat = dog.monthlyDaycareCostExVat + sessionCost
                )
            )
        }
    }

    // Testfunktion: Lägger till 1 timme (60 min) på pågående incheckning för enkel testning
    fun addTestHourToCheckIn(chipNumber: String) {
        val dog = findDog(chipNumber) ?: return
        if (dog.isCheckedIn && dog.checkInTimestamp != null) {
            updateDog(
                dog.copy(checkInTimestamp = dog.checkInTimestamp - 3_600_000L)
            )
        }
    }

    // Startar utlåning till Polis / Missing People (från 0 min)
    fun startSearchMission(chipNumber: String) {
        val dog = findDog(chipNumber) ?: return
        if (dog.isSearchDog && !dog.isOnSearchMission) {
            updateDog(
                dog.copy(
                    isOnSearchMission = true,
                    missionStartTimestamp = System.currentTimeMillis()
                )
            )
        }
    }

    // Avslutar Polis / Missing People-uppdrag och lägger kostnaden på månadsfakturan
    fun endSearchMission(chipNumber: String) {
        val dog = findDog(chipNumber) ?: return
        if (dog.isOnSearchMission) {
            val now = System.currentTimeMillis()
            val missionMinutes = max(1L, dog.getCurrentMissionMinutes(now))
            val missionCost = Dog.calculateMissionCost(missionMinutes)

            updateDog(
                dog.copy(
                    isOnSearchMission = false,
                    missionStartTimestamp = null,
                    todayCostExVat = dog.todayCostExVat + missionCost,
                    monthlyMissionMinutes = dog.monthlyMissionMinutes + missionMinutes,
                    monthlyMissionCostExVat = dog.monthlyMissionCostExVat + missionCost,
                    monthlyMissionCount = dog.monthlyMissionCount + 1
                )
            )
        }
    }

    // Testfunktion: Lägger till 1 timme på pågående Polis/MP-uppdrag
    fun addTestHourToMission(chipNumber: String) {
        val dog = findDog(chipNumber) ?: return
        if (dog.isOnSearchMission && dog.missionStartTimestamp != null) {
            updateDog(
                dog.copy(missionStartTimestamp = dog.missionStartTimestamp - 3_600_000L)
            )
        }
    }

    // Nollställer dagens ackumulerade siffror (påverkar inte månadsrapporten)
    fun resetTodayStats() {
        for (i in dogs.indices) {
            dogs[i] = dogs[i].copy(todayMinutes = 0L, todayCostExVat = 0.0)
        }
        saveDogs()
    }

    // Nollställer månadsrapporten inför en ny månad
    fun resetMonthlyReport() {
        for (i in dogs.indices) {
            dogs[i] = dogs[i].copy(
                todayMinutes = 0L,
                todayCostExVat = 0.0,
                monthlyDaycareMinutes = 0L,
                monthlyDaycareCostExVat = 0.0,
                monthlyMissionMinutes = 0L,
                monthlyMissionCostExVat = 0.0,
                monthlyMissionCount = 0
            )
        }
        saveDogs()
    }

    fun findDog(chipNumber: String): Dog? {
        return dogs.find { it.chipNumber.equals(chipNumber.trim(), ignoreCase = true) }
    }

    fun searchDogs(query: String): List<Dog> {
        val trimmed = query.trim()
        if (trimmed.isEmpty()) return emptyList()
        return dogs.filter {
            it.name.contains(trimmed, ignoreCase = true) ||
                    it.chipNumber.contains(trimmed, ignoreCase = true) ||
                    it.breed.contains(trimmed, ignoreCase = true)
        }
    }

    fun removeDog(chipNumber: String): Boolean {
        val dog = findDog(chipNumber)
        return if (dog != null) {
            dogs.remove(dog)
            saveDogs()
            true
        } else {
            false
        }
    }

    fun getAllDogs(): List<Dog> = dogs

    fun getCheckedInDogs(): List<Dog> = dogs.filter { it.isCheckedIn }

    fun getSearchDogs(): List<Dog> = dogs.filter { it.isSearchDog }

    fun getStatistics(): DogStats {
        val total = dogs.size
        val checkedIn = dogs.count { it.isCheckedIn }
        val grooming = dogs.count { it.needsGrooming }
        val vision = dogs.count { it.visionImpaired }
        val hearing = dogs.count { it.hearingImpaired }
        val searchDogs = dogs.count { it.isSearchDog }
        val activeMissions = dogs.count { it.isOnSearchMission }

        return DogStats(
            totalDogs = total,
            checkedInDogs = checkedIn,
            notCheckedInDogs = total - checkedIn,
            groomingDogs = grooming,
            visionImpairedDogs = vision,
            hearingImpairedDogs = hearing,
            searchDogs = searchDogs,
            activeMissions = activeMissions
        )
    }
}

data class DogStats(
    val totalDogs: Int,
    val checkedInDogs: Int,
    val notCheckedInDogs: Int,
    val groomingDogs: Int,
    val visionImpairedDogs: Int,
    val hearingImpairedDogs: Int,
    val searchDogs: Int,
    val activeMissions: Int
)