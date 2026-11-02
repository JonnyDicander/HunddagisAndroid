package com.example.minforstaapp

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.minforstaapp.ui.theme.MinForstaAppTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MinForstaAppTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    HunddagisApp()
                }
            }
        }
    }
}

@Composable
fun HunddagisApp() {
    val register = remember { DogRegister() }
    var currentScreen by remember { mutableStateOf(0) }
    val context = LocalContext.current

    when (currentScreen) {
        0 -> HuvudMeny(onNavigate = { currentScreen = it })
        1 -> RegistreraHundSkarm(onBack = { currentScreen = 0 }, onDogRegistered = {
            if (register.addDog(it)) {
                Toast.makeText(context, "Hunden ${it.name} har registrerats!", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(context, "Chipnumret finns redan!", Toast.LENGTH_SHORT).show()
            }
        })
        2 -> VisaHundarSkarm(register, false) { currentScreen = 0 }
        3 -> SokHundSkarm(register) { currentScreen = 0 }
        8 -> VisaHundarSkarm(register, true) { currentScreen = 0 }
        9 -> StatistikSkarm(register) { currentScreen = 0 }
    }
}

@Composable
fun HuvudMeny(onNavigate: (Int) -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp).verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("===== HUNDDAGIS =====", style = MaterialTheme.typography.headlineMedium, modifier = Modifier.padding(vertical = 16.dp))
        MenuButton("1. Registrera hund") { onNavigate(1) }
        MenuButton("2. Visa alla hundar") { onNavigate(2) }
        MenuButton("3. Sök efter hund") { onNavigate(3) }
        MenuButton("4. Ändra hunduppgifter") { onNavigate(2) }
        MenuButton("8. Visa incheckade hundar") { onNavigate(8) }
        MenuButton("9. Visa statistik & kostnader") { onNavigate(9) }
    }
}

@Composable
fun MenuButton(text: String, onClick: () -> Unit) {
    Button(onClick = onClick, modifier = Modifier.fillMaxWidth(0.85f).height(48.dp)) { Text(text) }
}

@Composable
fun RegistreraHundSkarm(onBack: () -> Unit, onDogRegistered: (Dog) -> Unit) {
    var name by remember { mutableStateOf("") }
    var breed by remember { mutableStateOf("") }
    var chip by remember { mutableStateOf("") }
    var gender by remember { mutableStateOf("H") }
    var ageText by remember { mutableStateOf("") }
    var sit by remember { mutableStateOf(false) }
    var neutered by remember { mutableStateOf(false) }
    var groom by remember { mutableStateOf(false) }
    var vision by remember { mutableStateOf(false) }
    var hearing by remember { mutableStateOf(false) }

    val focusManager = LocalFocusManager.current

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp).verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("--- REGISTRERA HUND ---", style = MaterialTheme.typography.headlineSmall)

        OutlinedTextField(
            value = name, onValueChange = { name = it }, label = { Text("Namn") }, modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
            keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Next) })
        )
        OutlinedTextField(
            value = breed, onValueChange = { breed = it }, label = { Text("Ras") }, modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
            keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Next) })
        )
        OutlinedTextField(
            value = chip, onValueChange = { chip = it }, label = { Text("Chipnummer") }, modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
            keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Next) })
        )
        OutlinedTextField(
            value = ageText, onValueChange = { ageText = it }, label = { Text("Ålder") }, modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() })
        )

        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
            Text("Kön: ${if (gender == "H") "Hane" else "Tik"}")
            Spacer(modifier = Modifier.weight(1f))
            Button(onClick = { gender = if (gender == "H") "T" else "H" }) { Text("Ändra") }
        }

        FormSwitch("Kan sitta med andra hundar", sit) { sit = it }
        FormSwitch("Kastrerad", neutered) { neutered = it }
        FormSwitch("Behöver pälsvård", groom) { groom = it }
        FormSwitch("Synnedsättning", vision) { vision = it }
        FormSwitch("Hörselnedsättning", hearing) { hearing = it }

        Button(onClick = {
            val age = ageText.toIntOrNull() ?: 0
            onDogRegistered(Dog(name, breed, chip, gender, age, sit, neutered, groom, vision, hearing))
            onBack()
        }, modifier = Modifier.fillMaxWidth()) { Text("Spara och registrera hund") }
        OutlinedButton(onClick = onBack, modifier = Modifier.fillMaxWidth()) { Text("Avbryt") }
    }
}


@Composable
fun FormSwitch(label: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text(label)
        Spacer(modifier = Modifier.weight(1f))
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

@Preview(showBackground = true)
@Composable
fun AppPreview() { MinForstaAppTheme { HunddagisApp() } }
