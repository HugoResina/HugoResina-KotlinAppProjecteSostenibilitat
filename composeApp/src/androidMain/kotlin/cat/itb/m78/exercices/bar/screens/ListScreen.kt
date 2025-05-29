package cat.itb.m78.exercices.bar.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import cat.itb.m78.exercices.bar.models.Dish
import cat.itb.m78.exercices.bar.viewModels.DatabaseViewModel
import cat.itb.m78.exercices.bar.viewModels.ListDishesViewModel
import coil3.compose.AsyncImage

@Composable
fun ListScreen(navigateToDishScreen: (String) -> Unit, navigateToOrderScreen: () -> Unit){
    val dbViewModel = viewModel { DatabaseViewModel() }
    val viewModel = viewModel { ListDishesViewModel() }

    val isLoading = viewModel.isLoading
    val dishes = viewModel.dishes
    val discountedDishes = viewModel.discountedDishes
    val noStockDishes = viewModel.noStockDishes

    ListScreenArguments(
        navigateToDishScreen,
        navigateToOrderScreen,
        isLoading.value,
        dishes.value,
        discountedDishes.value,
        noStockDishes.value
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListScreenArguments(
    navigateToDishScreen: (String) -> Unit,
    navigateToOrderScreen: () -> Unit,
    isLoading: Boolean,
    dishes: List<Dish>,
    discountedDishes: List<Dish>,
    noStockDishes: List<Dish>
){

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.primary,
                ),
                title = {
                    Text(
                        "La Hisenda Saborosa",
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { navigateToOrderScreen() },
                icon = { Icon(Icons.Filled.ShoppingCart, null) },
                text = { Text(text = "Veure comanda") },
            )
        }
    ) { innerPadding ->
        Column(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            if (isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator()
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Carregant dades...",
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(minSize = 150.dp),
                    modifier = Modifier.fillMaxSize().padding(16.dp),
                    contentPadding = PaddingValues(bottom = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Normal dishes
                    items(dishes) { dish ->
                        DishCard(dish, Color.Gray, navigateToDishScreen)
                    }
                    // Discounted dishes
                    items(discountedDishes) { dish ->
                        DishCard(dish, Color.Yellow, navigateToDishScreen)
                    }
                    // No-stock dishes
                    items(noStockDishes) { dish ->
                        DishCard(dish, Color.Red, navigateToDishScreen, clickable = false)
                    }
                }
            }
        }
    }
}

@Composable
private fun DishCard(
    dish: Dish,
    borderColor: Color,
    onClick: (String) -> Unit,
    clickable: Boolean = true
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(8.dp),
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .let { m -> if (clickable) m.clickable { onClick(dish.name) } else m }
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            AsyncImage(
                model = dish.imageUrl,
                contentDescription = dish.name,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
            // Out of stock overlay
            if (borderColor == Color.Red) {
                Box(modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.4f)))
                Text(
                    text = "Sense stock",
                    color = Color.White,
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                    modifier = Modifier.align(Alignment.Center)
                )
            }
            // Discount badge
            if (borderColor == Color.Yellow) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .background(Color.Red, RoundedCornerShape(bottomEnd = 8.dp))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "15% descompte",
                        color = Color.White,
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold)
                    )
                }
            }
            // Price tag
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .background(Color.Black.copy(alpha = 0.6f), RoundedCornerShape(topStart = 8.dp))
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
                Text(
                    text = String.format("%.2f €", dish.price),
                    color = Color.White,
                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold)
                )
            }
        }
    }
}
