package com.example.minforstaapp

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import java.util.Locale

enum class HundFilter {
    ALLA, INCHECKADE, SOKHUNDAR
}

// Hjälpfunktion som ger aktuell tid varje sekund så att timern tickar live i UI:t
@Composable
fun rememberCurrentTimeMillis(): Long {
    var now by remember { mutableStateOf(System.currentTimeMillis()) }
    LaunchedEffect(Unit) {
        while (true) {
            delay(1000L)
            now = System.currentTimeMillis()
        }
    }
    return now
}

fun formatDuration(totalSeconds: Long): String {
    val hours = totalSeconds / 3600
    val minutes = (totalSeconds % 3600) / 60
    val seconds = totalSeconds % 60
    return String.format(Locale.US, "%02d:%02d:%02d (%d min)", hours, minutes, seconds, totalSeconds / 60)
}

fun formatMinutes(totalMinutes: Long): String {
    val hours = totalMinutes / 60
    val mins = totalMinutes % 60
    return "${hours}h ${mins}m ($totalMinutes min)"
}

fun formatKr(amount: Double): String {
    return String.format(Locale.US, "%.2f kr", amount)
}

@Composable
fun SkarmRubrik(titel: String, onBack: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onBack, modifier = Modifier.size(36.dp)) {
            Icon(Icons.Default.ArrowBack, contentDescription = "Tillbaka", tint = MaterialTheme.colorScheme.primary)
        }
        Text(
            text = titel,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(start = 8.dp)
        )
    }
}

@Composable
fun SokHundSkarm(register: DogRegister, onBack: () -> Unit) {
    var query by remember { mutableStateOf("") }
    val focusManager = LocalFocusManager.current
    val nowMillis = rememberCurrentTimeMillis()
    val results = register.searchDogs(query)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        SkarmRubrik(titel = "🔍 Sök efter hund", onBack = onBack)

        OutlinedTextField(
            value = query,
            onValueChange = { query = it },
            label = { Text("Sök på namn, ras eller chipnummer") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            singleLine = true,
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            trailingIcon = {
                if (query.isNotEmpty()) {
                    IconButton(onClick = { query = "" }) {
                        Icon(Icons.Default.Clear, contentDescription = "Rensa")
                    }
                }
            },
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(onSearch = { focusManager.clearFocus() })
        )

        if (query.isNotBlank()) {
            if (results.isEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Text(
                        "Ingen hund matchade \"$query\" 🐾",
                        modifier = Modifier.padding(16.dp),
                        color = MaterialTheme.colorScheme.error
                    )
                }
            } else {
                Text(
                    "Hittade ${results.size} hund(ar):",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.align(Alignment.Start)
                )
                results.forEach { dog ->
                    key(dog.chipNumber) {
                        HundKort(dog = dog, register = register, nowMillis = nowMillis)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))
        OutlinedButton(
            onClick = onBack,
            modifier = Modifier.fillMaxWidth().height(48.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text("Tillbaka till huvudmenyn")
        }
    }
}

@Composable
fun HundKort(dog: Dog, register: DogRegister, nowMillis: Long) {
    var isEditing by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    var name by remember(dog.name) { mutableStateOf(dog.name) }
    var breed by remember(dog.breed) { mutableStateOf(dog.breed) }
    var ageText by remember(dog.age) { mutableStateOf(dog.age.toString()) }
    var groom by remember(dog.needsGrooming) { mutableStateOf(dog.needsGrooming) }
    var isSearchDog by remember(dog.isSearchDog) { mutableStateOf(dog.isSearchDog) }
    val focusManager = LocalFocusManager.current

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Ta bort ${dog.name}?") },
            text = { Text("Är du säker på att du vill radera ${dog.name} (Chip: ${dog.chipNumber}) permanent ur registret?") },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteDialog = false
                        register.removeDog(dog.chipNumber)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Ta bort")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showDeleteDialog = false }) {
                    Text("Avbryt")
                }
            }
        )
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            if (isEditing) {
                Text("✏️ Redigera uppgifter", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Namn") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                    keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) })
                )
                OutlinedTextField(
                    value = breed,
                    onValueChange = { breed = it },
                    label = { Text("Ras") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                    keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) })
                )
                OutlinedTextField(
                    value = ageText,
                    onValueChange = { ageText = it },
                    label = { Text("Ålder (år)") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() })
                )
                FormSwitch("Behöver pälsvård (ingår) 🪮", groom) { groom = it }
                FormSwitch("Sökhund: Polis / Missing People 🚨", isSearchDog) { isSearchDog = it }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = {
                            val updated = dog.copy(
                                name = name.trim().ifEmpty { dog.name },
                                breed = breed.trim().ifEmpty { dog.breed },
                                age = ageText.toIntOrNull() ?: dog.age,
                                needsGrooming = groom,
                                isSearchDog = isSearchDog
                            )
                            register.updateDog(updated)
                            isEditing = false
                        },
                        shape = RoundedCornerShape(12.dp)
                    ) { Text("Spara") }
                    OutlinedButton(
                        onClick = { isEditing = false },
                        shape = RoundedCornerShape(12.dp)
                    ) { Text("Avbryt") }
                }
            } else {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "${if (dog.gender == "H") "🐕" else "🐩"} ${dog.name}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    TextButton(onClick = { isEditing = true }) {
                        Text("✏️ Ändra")
                    }
                }

                Text("Ras: ${dog.breed} • Ålder: ${dog.age} år • Chip: ${dog.chipNumber}", style = MaterialTheme.typography.bodyMedium)

                val traits = buildList {
                    if (dog.isSearchDog) add("🚨 Sökhund (Polis/MP)")
                    if (dog.sitsNicely) add("🐾 Sitter fint")
                    if (dog.isNeutered) add("✂️ Kastrerad")
                    if (dog.needsGrooming) add("🪮 Pälsvård ingår")
                    if (dog.visionImpaired) add("👁️‍🗨️ Synnedsättning")
                    if (dog.hearingImpaired) add("🦻 Hörselnedsättning")
                }
                if (traits.isNotEmpty()) {
                    Text(
                        text = traits.joinToString(" • "),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

                // INCHECKNINGSSTATUS & REALTIDSTIMER (obegränsad tid, startar från 0 vid ny incheckning)
                if (dog.isCheckedIn) {
                    val sessionSec = dog.getCurrentSessionSeconds(nowMillis)
                    val sessionCost = dog.getCurrentSessionCost(nowMillis)
                    Text(
                        text = "✅ Incheckad (50 kr/h inkl. mat & pälsvård)",
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.secondary
                    )
                    Text("⏱️ Pågående pass: ${formatDuration(sessionSec)}")
                    Text("💰 Kostnad detta pass: ${formatKr(sessionCost)} (exkl. moms)")
                } else {
                    Text(
                        text = "🏠 Status: Utcheckad (Timpris: 50 kr/h)",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // POLIS / MISSING PEOPLE-UPPDRAG
                if (dog.isSearchDog) {
                    if (dog.isOnSearchMission) {
                        val missionSec = dog.getCurrentMissionSeconds(nowMillis)
                        val missionCost = dog.getCurrentMissionCost(nowMillis)
                        Text(
                            text = "🚨 UTE PÅ SÖKUPPDRAG (Polis / Missing People)",
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text("⏱️ Uppdragstid: ${formatDuration(missionSec)}")
                        Text("💰 Uppdragskostnad: ${formatKr(missionCost)} (1000 kr för 3h, därefter 500 kr/h)")
                    } else {
                        Text(
                            text = "🚨 Tillgänglig för Polis / Missing People (1000 kr start / 3h + 500 kr/h)",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                // SUMMERING FÖR AKTUELL DAG & MÅNAD PÅ HUNDKORTET
                val todayTotal = dog.getTotalTodayCostExVat(nowMillis)
                val monthTotalExVat = dog.getTotalMonthlyCostExVat(nowMillis)
                val monthTotalIncVat = dog.getTotalMonthlyCostIncVat(nowMillis)

                Text(
                    text = "📅 Kostnad idag: ${formatKr(todayTotal)} exkl. moms | 🧾 Månad: ${formatKr(monthTotalIncVat)} inkl. 25% moms (${formatKr(monthTotalExVat)} exkl.)",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                // KNAPPAR FÖR DAGIS IN/UTCHECKNING (+1h testknapp)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(top = 4.dp)) {
                    Button(
                        onClick = {
                            if (dog.isCheckedIn) {
                                register.checkOutDog(dog.chipNumber)
                            } else {
                                register.checkInDog(dog.chipNumber)
                            }
                        },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(if (dog.isCheckedIn) "Checka ut" else "Checka in (från 0:00)")
                    }

                    if (dog.isCheckedIn) {
                        FilledTonalButton(
                            onClick = { register.addTestHourToCheckIn(dog.chipNumber) },
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("⚡ +1h")
                        }
                    }

                    OutlinedButton(
                        onClick = { showDeleteDialog = true },
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Ta bort")
                    }
                }

                // KNAPPAR FÖR POLIS / MISSING PEOPLE-UPPDRAG
                if (dog.isSearchDog) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(top = 2.dp)) {
                        FilledTonalButton(
                            onClick = {
                                if (dog.isOnSearchMission) {
                                    register.endSearchMission(dog.chipNumber)
                                } else {
                                    register.startSearchMission(dog.chipNumber)
                                }
                            },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                if (dog.isOnSearchMission) "Avsluta sökuppdrag"
                                else "🚨 Starta sökuppdrag (Polis / MP)"
                            )
                        }
                        if (dog.isOnSearchMission) {
                            OutlinedButton(
                                onClick = { register.addTestHourToMission(dog.chipNumber) },
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("⚡ +1h sök")
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun VisaHundarSkarm(register: DogRegister, filterMode: HundFilter, onBack: () -> Unit) {
    val nowMillis = rememberCurrentTimeMillis()
    val dogs = when (filterMode) {
        HundFilter.ALLA -> register.getAllDogs()
        HundFilter.INCHECKADE -> register.getCheckedInDogs()
        HundFilter.SOKHUNDAR -> register.getSearchDogs()
    }

    val rubrik = when (filterMode) {
        HundFilter.ALLA -> "📋 Alla hundar (${dogs.size})"
        HundFilter.INCHECKADE -> "⏱️ Incheckade hundar (${dogs.size})"
        HundFilter.SOKHUNDAR -> "🚨 Polis & Missing People (${dogs.size})"
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        SkarmRubrik(titel = rubrik, onBack = onBack)

        if (dogs.isEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                val emptyText = when (filterMode) {
                    HundFilter.INCHECKADE -> "Inga hundar är incheckade just nu! 🐾"
                    HundFilter.SOKHUNDAR -> "Inga hundar är markerade som sökhundar för Polis / Missing People ännu! 🚨\n\nDu kan aktivera detta när du registrerar eller ändrar en hund."
                    HundFilter.ALLA -> "Inga hundar är registrerade ännu! 🐶"
                }
                Text(
                    text = emptyText,
                    modifier = Modifier.padding(24.dp),
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        } else {
            dogs.forEach { dog ->
                key(dog.chipNumber) {
                    HundKort(dog = dog, register = register, nowMillis = nowMillis)
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))
        OutlinedButton(
            onClick = onBack,
            modifier = Modifier.fillMaxWidth().height(48.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text("Tillbaka till huvudmenyn")
        }
    }
}

@Composable
fun StatistikSkarm(register: DogRegister, onBack: () -> Unit) {
    val nowMillis = rememberCurrentTimeMillis()
    val stats = register.getStatistics()
    val allDogs = register.getAllDogs()

    // Beräkningar för aktuell dag
    val todayCostExVat = allDogs.sumOf { it.getTotalTodayCostExVat(nowMillis) }
    val todayVat = todayCostExVat * Dog.VAT_RATE
    val todayCostIncVat = todayCostExVat + todayVat

    // Beräkningar för månadsrapport
    val monthDaycareExVat = allDogs.sumOf { it.monthlyDaycareCostExVat + it.getCurrentSessionCost(nowMillis) }
    val monthMissionExVat = allDogs.sumOf { it.monthlyMissionCostExVat + it.getCurrentMissionCost(nowMillis) }
    val monthTotalExVat = monthDaycareExVat + monthMissionExVat
    val monthTotalVat = monthTotalExVat * Dog.VAT_RATE
    val monthTotalIncVat = monthTotalExVat + monthTotalVat

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        SkarmRubrik(titel = "📊 Månadsrapport & Faktura", onBack = onBack)

        // KORT 1: AKTUELL DAG
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("📅 Kostnad för aktuell dag", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                StatRad("Incheckade just nu", "${stats.checkedInDogs} av ${stats.totalDogs} st")
                StatRad("Pågående sökuppdrag (Polis/MP)", "${stats.activeMissions} st")
                HorizontalDivider(modifier = Modifier.padding(vertical = 2.dp))
                StatRad("Dagens intäkt (exkl. moms)", formatKr(todayCostExVat))
                StatRad("Moms (25 %)", formatKr(todayVat))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Totalt idag (inkl. moms)", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary)
                    Text(formatKr(todayCostIncVat), fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary)
                }
                OutlinedButton(
                    onClick = { register.resetTodayStats() },
                    modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Nollställ dagens avslutade pass")
                }
            }
        }

        // KORT 2: MÅNADSRAPPORT & FAKTURERING (25 % MOMS)
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("🧾 Månadsrapport (Fakturaunderlag)", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                StatRad("Dagisvistelse (50 kr/h inkl. allt)", formatKr(monthDaycareExVat))
                StatRad("Polis / Missing People-uppdrag", formatKr(monthMissionExVat))
                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                StatRad("Netto att fakturera (exkl. moms)", formatKr(monthTotalExVat))
                StatRad("Moms (25 %)", formatKr(monthTotalVat))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("💰 ATT FAKTURERA (inkl. 25% moms)", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary)
                    Text(formatKr(monthTotalIncVat), fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary)
                }
            }
        }

        // KORT 3: SPECIFIKATION PER HUND
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("🐶 Fakturaspecifikation per hund", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)

                if (allDogs.isEmpty()) {
                    Text("Inga registrerade hundar ännu.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                } else {
                    allDogs.forEach { dog ->
                        val totalDaycareMin = dog.monthlyDaycareMinutes + dog.getCurrentSessionMinutes(nowMillis)
                        val totalMissionMin = dog.monthlyMissionMinutes + dog.getCurrentMissionMinutes(nowMillis)
                        val dogExVat = dog.getTotalMonthlyCostExVat(nowMillis)
                        val dogVat = dog.getTotalMonthlyVat(nowMillis)
                        val dogIncVat = dog.getTotalMonthlyCostIncVat(nowMillis)

                        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("${dog.name} (${dog.breed})", fontWeight = FontWeight.Bold)
                                Text(formatKr(dogIncVat) + " inkl. moms", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary)
                            }
                            Text(
                                text = "Dagistid: ${formatMinutes(totalDaycareMin)} • Exkl. moms: ${formatKr(dogExVat)} • Moms (25%): ${formatKr(dogVat)}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            if (dog.isSearchDog || totalMissionMin > 0) {
                                val missionsCount = dog.monthlyMissionCount + if (dog.isOnSearchMission) 1 else 0
                                Text(
                                    text = "🚨 Polis/MP-uppdrag: $missionsCount st (${formatMinutes(totalMissionMin)})",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                            HorizontalDivider(modifier = Modifier.padding(top = 6.dp))
                        }
                    }
                }

                OutlinedButton(
                    onClick = { register.resetMonthlyReport() },
                    modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Ny månad (Nollställ månadsrapport)")
                }
            }
        }

        OutlinedButton(
            onClick = onBack,
            modifier = Modifier.fillMaxWidth().height(48.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text("Tillbaka till huvudmenyn")
        }
    }
}

@Composable
fun StatRad(etikett: String, varde: String) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(etikett, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(varde, fontWeight = FontWeight.SemiBold)
    }
}