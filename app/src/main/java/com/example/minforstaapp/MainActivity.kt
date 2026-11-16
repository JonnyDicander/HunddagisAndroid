package com.example.minforstaapp

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.minforstaapp.ui.theme.MinForstaAppTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val darkPurpleColorScheme = darkColorScheme(
                primary = Color(0xFFBB86FC),
                onPrimary = Color(0xFF121212),
                primaryContainer = Color(0xFF3700B3),
                onPrimaryContainer = Color(0xFFEDE7F6),
                secondary = Color(0xFF03DAC6),
                background = Color(0xFF121212),
                surface = Color(0xFF1E1E1E),
                onSurface = Color(0xFFE0E0E0),
                surfaceVariant = Color(0xFF2C2C2C),
                onSurfaceVariant = Color(0xFFB0B0B0)
            )

            MaterialTheme(colorScheme = darkPurpleColorScheme) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    HunddagisApp()
                }
            }
        }
    }
}

@Composable
fun HunddagisApp() {
    val context = LocalContext.current
    val register = remember { DogRegister(context) }
    var currentScreen by remember { mutableStateOf(0) }

    BackHandler(enabled = currentScreen != 0) {
        currentScreen = 0
    }

    when (currentScreen) {
        0 -> HuvudMeny(
            checkedInCount = register.getCheckedInDogs().size,
            totalCount = register.getAllDogs().size,
            searchDogsCount = register.getSearchDogs().size,
            onNavigate = { currentScreen = it }
        )
        1 -> RegistreraHundSkarm(
            onBack = { currentScreen = 0 },
            onDogRegistered = { dog ->
                if (register.addDog(dog)) {
                    Toast.makeText(context, "Hunden ${dog.name} har välkomnats till J och J's! 🐾", Toast.LENGTH_SHORT).show()
                    true
                } else {
                    Toast.makeText(context, "Chipnumret finns redan i registret!", Toast.LENGTH_SHORT).show()
                    false
                }
            }
        )
        2 -> VisaHundarSkarm(register, filterMode = HundFilter.ALLA) { currentScreen = 0 }
        3 -> SokHundSkarm(register) { currentScreen = 0 }
        7 -> VisaHundarSkarm(register, filterMode = HundFilter.SOKHUNDAR) { currentScreen = 0 }
        8 -> VisaHundarSkarm(register, filterMode = HundFilter.INCHECKADE) { currentScreen = 0 }
        9 -> StatistikSkarm(register) { currentScreen = 0 }
    }
}

@Composable
fun HuvudMeny(
    checkedInCount: Int,
    totalCount: Int,
    searchDogsCount: Int,
    onNavigate: (Int) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.Pets,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(10.dp))
        Text(
            "🐾 J och J's Hunddagis 🐶",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            "Incheckade: $checkedInCount av $totalCount hundar • 50 kr/h",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.secondary
        )

        Spacer(modifier = Modifier.height(28.dp))

        Column(
            modifier = Modifier.fillMaxWidth(0.95f),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            MenuCardButton("Registrera ny hund", "🐶", Icons.Default.AddCircle) { onNavigate(1) }
            MenuCardButton("Visa & hantera hundar ($totalCount)", "📋", Icons.Default.List) { onNavigate(2) }
            MenuCardButton("Incheckade hundar ($checkedInCount)", "⏱️", Icons.Default.CheckCircle) { onNavigate(8) }
            MenuCardButton("Polis & Missing People ($searchDogsCount)", "🚨", Icons.Default.VerifiedUser) { onNavigate(7) }
            MenuCardButton("Sök efter hund", "🔍", Icons.Default.Search) { onNavigate(3) }
            MenuCardButton("Månadsrapport & Fakturering", "📊", Icons.Default.BarChart) { onNavigate(9) }
        }
    }
}

@Composable
fun MenuCardButton(text: String, emoji: String, icon: androidx.compose.ui.graphics.vector.ImageVector, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(62.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = emoji, style = MaterialTheme.typography.titleLarge)
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = text,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.weight(1f))
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
fun RegistreraHundSkarm(onBack: () -> Unit, onDogRegistered: (Dog) -> Boolean) {
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
    var isSearchDog by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val focusManager = LocalFocusManager.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        SkarmRubrik(titel = "🐶 Registrera ny hund", onBack = onBack)

        OutlinedTextField(
            value = name, onValueChange = { name = it },
            label = { Text("Hundens namn *") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
            keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Next) })
        )
        OutlinedTextField(
            value = breed, onValueChange = { breed = it },
            label = { Text("Ras *") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
            keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Next) })
        )
        OutlinedTextField(
            value = chip, onValueChange = { chip = it },
            label = { Text("Chipnummer *") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
            keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Next) })
        )
        OutlinedTextField(
            value = ageText, onValueChange = { ageText = it },
            label = { Text("Ålder (år)") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() })
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Kön: ${if (gender == "H") "Hane 🐕" else "Tik 🐩"}", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.weight(1f))
                Button(
                    onClick = { gender = if (gender == "H") "T" else "H" },
                    modifier = Modifier.height(36.dp),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                ) {
                    Text("Byt kön", style = MaterialTheme.typography.bodySmall)
                }
            }
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    "✨ Egenskaper & Tjänster (50 kr/h inkl. allt)",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(2.dp))
                FormSwitch("Sitter fint med andra 🐾", sit) { sit = it }
                FormSwitch("Kastrerad ✂️", neutered) { neutered = it }
                FormSwitch("Behöver pälsvård (ingår) 🪮", groom) { groom = it }
                FormSwitch("Synnedsättning 👁️‍🗨️", vision) { vision = it }
                FormSwitch("Hörselnedsättning 🦻", hearing) { hearing = it }
                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                FormSwitch("Sökhund: Polis / Missing People 🚨", isSearchDog) { isSearchDog = it }
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        Button(
            onClick = {
                if (name.isBlank() || breed.isBlank() || chip.isBlank()) {
                    Toast.makeText(context, "Vänligen fyll i namn, ras och chipnummer! ⚠️", Toast.LENGTH_SHORT).show()
                    return@Button
                }
                val age = ageText.toIntOrNull() ?: 0
                val nyHund = Dog(
                    name = name.trim(),
                    breed = breed.trim(),
                    chipNumber = chip.trim(),
                    gender = gender,
                    age = age,
                    sitsNicely = sit,
                    isNeutered = neutered,
                    needsGrooming = groom,
                    visionImpaired = vision,
                    hearingImpaired = hearing,
                    isSearchDog = isSearchDog
                )
                if (onDogRegistered(nyHund)) {
                    onBack()
                }
            },
            modifier = Modifier.fillMaxWidth().height(48.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text("💖 Spara och registrera hund", fontWeight = FontWeight.Bold)
        }

        OutlinedButton(
            onClick = onBack,
            modifier = Modifier.fillMaxWidth().height(46.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text("Avbryt")
        }
    }
}

@Composable
fun FormSwitch(label: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

@Preview(showBackground = true)
@Composable
fun AppPreview() {
    MinForstaAppTheme {
        HunddagisApp()
    }
}