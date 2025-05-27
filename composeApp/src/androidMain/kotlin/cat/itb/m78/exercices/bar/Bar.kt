package cat.itb.m78.exercices.bar

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import cat.itb.m78.exercices.bar.Screens.ListScreen
import kotlinx.serialization.Serializable

object Destination {
    @Serializable
    data object ListScreen
    @Serializable
    data class DishScreen(val id: Int)
    @Serializable
    data object OrderScreen
}

@Composable
fun Bar(){
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = Destination.ListScreen){
        composable<Destination.ListScreen>{
            ListScreen(
                navigateToDishScreen = { id: Int ->
                    navController.navigate(Destination.DishScreen(id))
                },
                navigateToOrderScreen = {
                    navController.navigate(Destination.OrderScreen)
                }
            )
        }
    }
}