package cat.itb.m78.exercices

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage


@Composable
fun ListScreen(
    onSeeList: () -> Unit,
    onViewI: () -> Unit,
    viewModel: ItemViewModel
) {
    val itemList = viewModel.Dishes
    val discountList = viewModel.DishToDiscount
    val noStock = viewModel.NoStockDishes
    val isLoading = viewModel.isLoading.value  // Comprobar si estamos en carga

    // Mostrar un mensaje o indicador de carga mientras las listas están vacías
    if (isLoading) {
        CircularProgressIndicator(modifier = Modifier.fillMaxSize(), color = Color.Blue)
        return
    }

    Scaffold {


        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 150.dp),
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            contentPadding = PaddingValues(bottom = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (itemList.isNotEmpty()) {
                itemList.forEach { dish ->
                    item {
                        Card(
                            shape = RoundedCornerShape(12.dp),
                            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .aspectRatio(1f)
                                .clickable {
                                    viewModel.seleccionat.value = dish
                                    onViewI()
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
            }
            if (discountList.isNotEmpty()) {
                discountList.forEach { dish ->
                    item {
                        Card(
                            shape = RoundedCornerShape(12.dp),
                            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .aspectRatio(1f)
                                .clickable {
                                    viewModel.seleccionat.value = dish
                                    onViewI()
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

            if (noStock.isNotEmpty()) {
                noStock.forEach { dish ->
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

    FloatingActionButton(
        onClick = { onSeeList() },
        modifier = Modifier

            .padding(24.dp),
        containerColor = MaterialTheme.colorScheme.primary
    ) {
        Text("See order")
    }
}