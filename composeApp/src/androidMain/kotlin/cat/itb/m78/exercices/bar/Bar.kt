package cat.itb.m78.exercices.bar

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import cat.itb.m78.exercices.bar.screens.DishScreen
import cat.itb.m78.exercices.bar.screens.ListScreen
import kotlinx.serialization.Serializable

object Destination {
    @Serializable
    data object ListScreen
    @Serializable
    data class DishScreen(val name: String)
    @Serializable
    data object OrderScreen
}

@Composable
fun Bar(){
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = Destination.ListScreen){
        composable<Destination.ListScreen>{
            ListScreen(
                navigateToDishScreen = { name: String ->
                    navController.navigate(Destination.DishScreen(name))
                },
                navigateToOrderScreen = {
                    navController.navigate(Destination.OrderScreen)
                }
            )
        }

        composable<Destination.DishScreen>{ backStack ->
            val dishName = backStack.toRoute<Destination.DishScreen>().name

            DishScreen(
                navigateToListScreen = { navController.navigate(Destination.ListScreen) },
                dishName = dishName
            )
        }

    }
}