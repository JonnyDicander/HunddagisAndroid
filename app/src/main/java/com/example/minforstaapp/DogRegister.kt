package com.example.minforstaapp

class DogRegister {
    // Lista som lagrar alla registrerade hundar (motsvarar din ArrayList)
    private val dogs = mutableListOf<Dog>()

    // Motsvarar register.addDog(dog) - returnerar false om chipnumret redan finns
    fun addDog(dog: Dog): Boolean {
        if (findDog(dog.chipNumber) != null) {
            return false
        }
        dogs.add(dog)
        return true
    }

    // Motsvarar register.findDog(chipNumber)
    fun findDog(chipNumber: String): Dog? {
        return dogs.find { it.chipNumber == chipNumber }
    }

    // Motsvarar din register.searchDog(search) för både namn och chipnummer
    fun searchDog(query: String): Dog? {
        return dogs.find {
            it.name.equals(query, ignoreCase = true) || it.chipNumber == query
        }
    }

    // Motsvarar register.removeDog(chipNumber)
    fun removeDog(chipNumber: String): Boolean {
        val dog = findDog(chipNumber)
        return if (dog != null) {
            dogs.remove(dog)
            true
        } else {
            false
        }
    }

    // Hämtar alla registrerade hundar till gränssnittet
    fun getAllDogs(): List<Dog> {
        return dogs
    }

    // Returnerar bara incheckade hundar (används för showCheckedInDogs)
    fun getCheckedInDogs(): List<Dog> {
        return dogs.filter { it.isCheckedIn }
    }

    // Beräknar statistik över alla räknare (motsvarar showStatistics)
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

    // Beräknar totala kostnaden för alla incheckade hundar just nu (motsvarar showCosts)
    fun getTotalCostOfCheckedIn(): Double {
        return dogs.filter { it.isCheckedIn }.sumOf { it.getCost() }
    }
}

// En liten hjälpklass (data class) för att skicka vidare statistiken till Android-skärmen
data class DogStats(
    val totalDogs: Int,
    val checkedInDogs: Int,
    val notCheckedInDogs: Int,
    val groomingDogs: Int,
    val visionImpairedDogs: Int,
    val hearingImpairedDogs: Int
)
