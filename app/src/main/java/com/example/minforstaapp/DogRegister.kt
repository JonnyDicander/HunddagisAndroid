package com.example.minforstaapp

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class DogRegister(context: Context) {
    private val prefs = context.getSharedPreferences("dog_prefs", Context.MODE_PRIVATE)
    private val gson = Gson()
    private val dogs = mutableListOf<Dog>()

    init {
        loadDogs()
    }

    private fun saveDogs() {
        val json = gson.toJson(dogs)
        prefs.edit().putString("saved_dogs", json).apply()
    }

    private fun loadDogs() {
        val json = prefs.getString("saved_dogs", null)
        if (json != null) {
            val type = object : TypeToken<List<Dog>>() {}.type
            val savedList: List<Dog> = gson.fromJson(json, type)
            dogs.clear()
            dogs.addAll(savedList)
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

    // Uppdaterar en befintlig hund (t.ex. ras eller incheckningsstatus) och sparar direkt
    fun updateDog(updatedDog: Dog) {
        val index = dogs.indexOfFirst { it.chipNumber == updatedDog.chipNumber }
        if (index != -1) {
            dogs[index] = updatedDog
            saveDogs()
        }
    }

    fun findDog(chipNumber: String): Dog? {
        return dogs.find { it.chipNumber == chipNumber }
    }

    fun searchDog(query: String): Dog? {
        return dogs.find {
            it.name.equals(query, ignoreCase = true) || it.chipNumber == query
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

    // Använd denna när status (t.ex. incheckning) ändras för att spara tillståndet
    fun updateDogStatus() {
        saveDogs()
    }

    fun getAllDogs(): List<Dog> {
        return dogs
    }

    fun getCheckedInDogs(): List<Dog> {
        return dogs.filter { it.isCheckedIn }
    }

    fun getStatistics(): DogStats {
        val total = dogs.size
        val checkedIn = dogs.count { it.isCheckedIn }
        val grooming = dogs.count { it.needsGrooming }
        val vision = dogs.count { it.visionImpaired }
        val hearing = dogs.count { it.hearingImpaired }

        return DogStats(
            totalDogs = total,
            checkedInDogs = checkedIn,
            notCheckedInDogs = total - checkedIn,
            groomingDogs = grooming,
            visionImpairedDogs = vision,
            hearingImpairedDogs = hearing
        )
    }

    fun getTotalCostOfCheckedIn(): Double {
        return dogs.filter { it.isCheckedIn }.sumOf { it.getCost() }
    }
}

data class DogStats(
    val totalDogs: Int,
    val checkedInDogs: Int,
    val notCheckedInDogs: Int,
    val groomingDogs: Int,
    val visionImpairedDogs: Int,
    val hearingImpairedDogs: Int
)