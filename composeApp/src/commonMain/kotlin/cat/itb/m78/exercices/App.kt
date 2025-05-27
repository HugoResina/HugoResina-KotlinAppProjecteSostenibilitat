package cat.itb.m78.exercices

import androidx.compose.runtime.*
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.serialization.Serializable


enum class EScreen {
    ViewE, SelectE, OrderE
}


@Serializable
data class Dish (var name: String, var imageUrl: String, var price: Double, var ingredientsName: List<String>, var description: String, var ingredients: List<Ingredient> = emptyList())

@Serializable
data class Ingredient(var id: Int, var name: String, var expirationDate: String)

@Composable
fun App() {
    val sharedViewModel: ItemViewModel = viewModel()
    var currentScreen by remember { mutableStateOf(EScreen.SelectE) }

    when (currentScreen) {
        EScreen.SelectE -> ListScreen(
            onSeeList = { currentScreen = EScreen.OrderE },
            onViewI = { currentScreen = EScreen.ViewE },
            viewModel = sharedViewModel
        )

        EScreen.ViewE -> DishScreen(
            onGoBack = { currentScreen = EScreen.SelectE },
            viewModel = sharedViewModel
        )

        EScreen.OrderE -> OrderScreen(
            onGoBack = { currentScreen = EScreen.SelectE },
            viewModel = sharedViewModel
        )
    }
}





