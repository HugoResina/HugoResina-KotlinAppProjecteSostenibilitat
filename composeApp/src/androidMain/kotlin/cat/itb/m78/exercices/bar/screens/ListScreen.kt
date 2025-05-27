package cat.itb.m78.exercices.bar.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
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
import cat.itb.m78.exercices.bar.viewModels.ListDishesViewModel
import coil3.compose.AsyncImage

@Composable
fun ListScreen(navigateToDishScreen: (String) -> Unit, navigateToOrderScreen: () -> Unit){
    val viewModel = viewModel { ListDishesViewModel() }

    val dishes = viewModel.dishes
    val discountedDishes = viewModel.discountedDishes
    val noStockDishes = viewModel.noStockDishes

    ListScreenArguments(navigateToDishScreen, navigateToOrderScreen, dishes.value,
        discountedDishes.value, noStockDishes.value)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListScreenArguments(navigateToDishScreen: (String) -> Unit, navigateToOrderScreen: () -> Unit,
                        dishes: List<Dish>, discountedDishes: List<Dish>, noStockDishes: List<Dish>){

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
        }
    ) { innerPadding ->
        Column(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            if (dishes.isEmpty()){
                CircularProgressIndicator()
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(minSize = 150.dp),
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    contentPadding = PaddingValues(bottom = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    dishes.forEach { dish ->
                        item {
                            Card(
                                shape = RoundedCornerShape(12.dp),
                                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .aspectRatio(1f)
                                    .clickable {
                                        navigateToDishScreen(dish.name)
                                    }
                            ) {
                                Box(
                                    modifier = Modifier.fillMaxSize()
                                        .border(
                                            width = 1.dp,
                                            shape = RoundedCornerShape(12.dp),
                                            color = Color.Gray
                                        )
                                ) {
                                    AsyncImage(
                                        model = dish.imageUrl,
                                        contentDescription = dish.name,
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Crop
                                    )

                                    Box(
                                        modifier = Modifier
                                            .border(
                                                width = 1.dp,
                                                shape = RoundedCornerShape(4.dp),
                                                color = Color.Gray
                                            )
                                            .align(Alignment.BottomCenter)
                                            .fillMaxWidth()
                                            .background(
                                                Brush.verticalGradient(
                                                    colors = listOf(
                                                        Color.Transparent,
                                                        Color.Black.copy(alpha = 0.6f)
                                                    )
                                                )
                                            )
                                            .padding(8.dp)

                                    ) {
                                        Text(
                                            text = dish.name,
                                            color = Color.White,
                                            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                                            modifier = Modifier.align(Alignment.Center)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    if (discountedDishes.isNotEmpty()){
                        discountedDishes.forEach { dish ->
                            item {
                                Card(
                                    shape = RoundedCornerShape(12.dp),
                                    elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .aspectRatio(1f)
                                        .clickable {
                                            navigateToDishScreen(dish.name)
                                        }
                                ) {
                                    Box(
                                        modifier = Modifier.fillMaxSize()
                                            .border(
                                                width = 1.dp,
                                                shape = RoundedCornerShape(12.dp),
                                                color = Color.Yellow
                                            )
                                    ) {
                                        AsyncImage(
                                            model = dish.imageUrl,
                                            contentDescription = dish.name,
                                            modifier = Modifier.fillMaxSize(),
                                            contentScale = ContentScale.Crop
                                        )

                                        Box(
                                            modifier = Modifier
                                                .border(
                                                    width = 1.dp,
                                                    shape = RoundedCornerShape(4.dp),
                                                    color = Color.Yellow
                                                )
                                                .align(Alignment.BottomCenter)
                                                .fillMaxWidth()
                                                .background(
                                                    Brush.verticalGradient(
                                                        colors = listOf(
                                                            Color.Transparent,
                                                            Color.Black.copy(alpha = 0.6f)
                                                        )
                                                    )
                                                )

                                                .padding(8.dp)

                                        ) {
                                            Text(
                                                text = dish.name,
                                                color = Color.White,
                                                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                                                modifier = Modifier.align(Alignment.Center)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    if (noStockDishes.isNotEmpty()){
                        noStockDishes.forEach { dish ->
                            item {
                                Card(
                                    shape = RoundedCornerShape(12.dp),
                                    elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .aspectRatio(1f)

                                ) {
                                    Box(
                                        modifier = Modifier.fillMaxSize()
                                            .border(
                                                width = 1.dp,
                                                shape = RoundedCornerShape(12.dp),
                                                color = Color.Red
                                            )
                                    ) {
                                        AsyncImage(
                                            model = dish.imageUrl,
                                            contentDescription = dish.name,
                                            modifier = Modifier.fillMaxSize(),
                                            contentScale = ContentScale.Crop
                                        )

                                        Box(
                                            modifier = Modifier
                                                .border(
                                                    width = 1.dp,
                                                    shape = RoundedCornerShape(4.dp),
                                                    color = Color.Red
                                                )
                                                .align(Alignment.BottomCenter)
                                                .fillMaxWidth()
                                                .background(
                                                    Brush.verticalGradient(
                                                        colors = listOf(
                                                            Color.Transparent,
                                                            Color.Black.copy(alpha = 0.6f)
                                                        )
                                                    )
                                                )

                                                .padding(8.dp)

                                        ) {
                                            Text(
                                                text = dish.name,
                                                color = Color.White,
                                                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                                                modifier = Modifier.align(Alignment.Center)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                }

            }
        }
    }
}