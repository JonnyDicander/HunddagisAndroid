package com.example.minforstaapp

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import java.util.Locale

@Composable
fun SokHundSkarm(register: DogRegister, onBack: () -> Unit) {
    var query by remember { mutableStateOf("") }
    var dog by remember { mutableStateOf<Dog?>(null) }
    var clicked by remember { mutableStateOf(false) }
    var refresh by remember { mutableStateOf(0) }
    val focusManager = LocalFocusManager.current

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp).verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("--- SÖK EFTER HUND ---", style = MaterialTheme.typography.headlineSmall)
        OutlinedTextField(
            value = query,
            onValueChange = { query = it },
            label = { Text("Ange namn eller chipnummer") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(onSearch = {
                dog = register.searchDog(query)
                clicked = true
                focusManager.clearFocus()
            })
        )
        Button({ dog = register.searchDog(query); clicked = true; focusManager.clearFocus() }, modifier = Modifier.fillMaxWidth()) { Text("Sök") }

        key(refresh) {
            val d = dog
            if (d != null) {
                HundKort(d, register, { refresh++ }, { register.removeDog(d.chipNumber); dog = null })
            } else if (clicked) {
                Text("Ingen hund hittades!", color = MaterialTheme.colorScheme.error)
            }
        }
        OutlinedButton(onClick = onBack, modifier = Modifier.fillMaxWidth()) { Text("Tillbaka till huvudmenyn") }
    }
}

@Composable
fun HundKort(dog: Dog, register: DogRegister, onRefresh: () -> Unit, onRemove: () -> Unit) {
    var isEditing by remember { mutableStateOf(false) }
    var name by remember { mutableStateOf(dog.name) }
    var breed by remember { mutableStateOf(dog.breed) }
    var ageText by remember { mutableStateOf(dog.age.toString()) }
    val focusManager = LocalFocusManager.current

    Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            if (isEditing) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Namn") },
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                    keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) })
                )
                OutlinedTextField(
                    value = breed,
                    onValueChange = { breed = it },
                    label = { Text("Ras") },
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                    keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) })
                )
                OutlinedTextField(
                    value = ageText,
                    onValueChange = { ageText = it },
                    label = { Text("Ålder") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() })
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button({
                        dog.name = name
                        dog.breed = breed
                        dog.age = ageText.toIntOrNull() ?: dog.age
                        register.updateDog(dog) // Sparar ändringen permanent!
                        isEditing = false
                        onRefresh()
                    }) { Text("Spara") }
                    OutlinedButton({ isEditing = false }) { Text("Avbryt") }
                }
            } else {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Namn: ${dog.name}", style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.weight(1f))
                    TextButton({ name = dog.name; breed = dog.breed; ageText = dog.age.toString(); isEditing = true }) { Text("Ändra") }
                }
                Text("Ras: ${dog.breed} | Chip: ${dog.chipNumber} | Ålder: ${dog.age} år")
                Text("Kön: ${if (dog.gender == "H") "Hane" else "Tik"} | Status: ${if (dog.isCheckedIn) "Incheckad" else "Utcheckad"}")
                if (dog.isCheckedIn) {
                    val formateradKostnad = String.format(Locale.US, "%.2f", dog.getCost())
                    Text("Kostnad: $formateradKostnad kr")
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(top = 8.dp)) {
                    Button({
                        dog.isCheckedIn = !dog.isCheckedIn // Ändrar status direkt utan felaktigt anrop
                        register.updateDog(dog)            // Sparar incheckningsstatus permanent!
                        onRefresh()
                    }) { Text(if (dog.isCheckedIn) "Checka ut" else "Checka in") }
                    OutlinedButton({ onRemove(); onRefresh() }) { Text("Ta bort") }
                }
            }
        }
    }
}

@Composable
fun VisaHundarSkarm(register: DogRegister, enbartIncheckade: Boolean, onBack: () -> Unit) {
    var refresh by remember { mutableStateOf(0) }
    val dogs = if (enbartIncheckade) register.getCheckedInDogs() else register.getAllDogs()

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp).verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = if (enbartIncheckade) "--- INCHECKADE HUNDAR ---" else "--- REGISTRERADE HUNDAR ---",
            style = MaterialTheme.typography.headlineSmall
        )

        if (dogs.isEmpty()) {
            Text("Inga hundar att visa här!", modifier = Modifier.padding(vertical = 32.dp))
        } else {
            key(refresh) {
                dogs.forEach { HundKort(it, register, { refresh++ }, { register.removeDog(it.chipNumber) }) }
            }
        }

        OutlinedButton(onClick = onBack, modifier = Modifier.fillMaxWidth()) {
            Text("Tillbaka till huvudmenyn")
        }
    }
}

@Composable
fun StatistikSkarm(register: DogRegister, onBack: () -> Unit) {
    val stats = register.getStatistics()
    val totalCostFormaterad = String.format(Locale.US, "%.2f", register.getTotalCostOfCheckedIn())

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp).verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("--- STATISTIK ---", style = MaterialTheme.typography.titleMedium)
                Text("Totalt registrerade: ${stats.totalDogs}")
                Text("Incheckade: ${stats.checkedInDogs} | Ej incheckade: ${stats.notCheckedInDogs}")
                Text("Pälsvård: ${stats.groomingDogs} | Synnedsättning: ${stats.visionImpairedDogs}")
                Text("Total kostnad (incheckade): $totalCostFormaterad kr")
            }
        }
        OutlinedButton(onClick = onBack, modifier = Modifier.fillMaxWidth()) { Text("Tillbaka till huvudmenyn") }
    }
}