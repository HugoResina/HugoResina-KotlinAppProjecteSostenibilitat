package cat.itb.m78.exercices.bar.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Done
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import androidx.lifecycle.viewmodel.compose.viewModel
import cat.itb.m78.exercices.bar.models.Dish
import cat.itb.m78.exercices.bar.viewModels.DatabaseViewModel
import cat.itb.m78.exercices.bar.MyApi
import android.widget.Toast
import androidx.compose.material.icons.Icons
import androidx.compose.ui.draw.clip
import coil3.compose.rememberAsyncImagePainter
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.daysUntil
import kotlinx.datetime.toLocalDateTime

@Composable
fun OrderScreen(navigateToListScreen: () -> Unit) {
    val dbViewModel: DatabaseViewModel = viewModel()
    val selectedList by dbViewModel.selectedDishes

    LaunchedEffect(Unit) {
        dbViewModel.refreshSelectedDishes()
    }

    val allMenu by produceState(initialValue = emptyList<Dish>()) {
        value = MyApi.listMenu()
    }

    val orderedDishes by remember(selectedList, allMenu) {
        mutableStateOf(allMenu.filter { dish -> selectedList.any { it.name == dish.name } })
    }

    val totalPrice by remember(orderedDishes) {
        mutableDoubleStateOf(
            orderedDishes.sumOf { dish ->
                val today = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
                val needsDiscount = dish.ingredients.any {
                    val exp = LocalDate.parse(it.expirationDate.split("T")[0])
                    today.daysUntil(exp) <= 7
                }
                if (needsDiscount) dish.price * 0.85 else dish.price
            }
        )
    }

    OrderScreenArguments(
        navigateToListScreen = navigateToListScreen,
        selectedList = orderedDishes,
        totalPrice = totalPrice,
        removeSelectedDish = dbViewModel::removeSelectedDish,
        clearSelectedDishes = dbViewModel::clearSelectedDishes
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderScreenArguments(
    navigateToListScreen: () -> Unit,
    selectedList: List<Dish>,
    totalPrice: Double,
    removeSelectedDish: (String) -> Unit,
    clearSelectedDishes: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var dishNameToDelete by remember { mutableStateOf("") }
    var showDialog by remember { mutableStateOf(false) }
    val bottomSheetState = rememberModalBottomSheetState()

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.primary,
                ),
                title = { Text("Comanda", maxLines = 1, overflow = TextOverflow.Ellipsis) },
                navigationIcon = {
                    IconButton(onClick = { navigateToListScreen() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = null
                        )
                    }
                }
            )
        },

    ) { innerPadding ->
        Column(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            if (selectedList.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Encara no has afegit cap plat!")
                }
            } else {
                Spacer(Modifier.height(8.dp))

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .background(MaterialTheme.colorScheme.surface),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(vertical = 8.dp)
                ) {
                    items(selectedList, key = { it.name }) { dish ->
                        // Check if any ingredient is near expiration (<=7 days)
                        val nowDate = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
                        val isNearExpiry = remember(dish) {
                            dish.ingredients.any {
                                val exp = LocalDate.parse(it.expirationDate.split("T")[0])
                                nowDate.daysUntil(exp) <= 7
                            }
                        }
                        // Calculate discounted price
                        val discountedPrice = remember(dish) { dish.price * 0.85 }

                        Card(
                            modifier = Modifier
                                .fillMaxWidth(),
                            elevation = CardDefaults.cardElevation(6.dp),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Image(
                                    painter = rememberAsyncImagePainter(dish.imageUrl),
                                    contentDescription = dish.name,
                                    modifier = Modifier
                                        .size(60.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                )
                                Spacer(Modifier.width(16.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = dish.name,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    if (isNearExpiry) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                text = "${dish.price} €",
                                                style = MaterialTheme.typography.bodyMedium,
                                                textDecoration = TextDecoration.LineThrough,
                                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(
                                                text = String.format("%.2f €", discountedPrice),
                                                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                                                color = Color.Red
                                            )
                                        }
                                    } else {
                                        Text(
                                            text = "${dish.price} €",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }
                                IconButton(onClick = { dishNameToDelete = dish.name; showDialog = true }) {
                                    Icon(
                                        Icons.Default.Delete,
                                        contentDescription = "Eliminar",
                                        tint = Color.Red
                                    )
                                }
                            }
                        }
                    }
                }

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    shape = RoundedCornerShape(12.dp),
                    elevation = CardDefaults.cardElevation(8.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Total: $totalPrice €",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        FloatingActionButton(
                            onClick = {
                                scope.launch {
                                    val ids = selectedList.flatMap { it.ingredients }.map { it.id }
                                    MyApi.removeUsedIngredients(ids)
                                    clearSelectedDishes()
                                    Toast.makeText(context, "Comanda feta correctament!", Toast.LENGTH_LONG).show()
                                    navigateToListScreen()
                                }
                            },
                            containerColor = MaterialTheme.colorScheme.secondary,
                            contentColor = MaterialTheme.colorScheme.onSecondary
                        ) {
                            Icon(Icons.Default.Done, contentDescription = "Confirm order")
                        }
                    }
                }
            }

            if (showDialog) {
                ModalBottomSheet(
                    onDismissRequest = { showDialog = false; },
                    sheetState = bottomSheetState
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("Voleu eliminar el plat seleccionat de la comanda?")
                        Spacer(Modifier.height(8.dp))
                        Row(
                            horizontalArrangement = Arrangement.Center,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            TextButton(onClick = {
                                showDialog = false
                            }) {
                                Text("No")
                            }
                            TextButton(onClick = {
                                showDialog = false
                                removeSelectedDish(dishNameToDelete)
                            }) {
                                Text("Si")
                            }
                        }
                    }
                }
            }
        }
    }
}