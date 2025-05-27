package cat.itb.m78.exercices.bar.Screens

import androidx.compose.runtime.Composable

@Composable
fun ListScreen(navigateToDishScreen: (Int) -> Unit, navigateToOrderScreen: () -> Unit){

    ListScreenArguments(navigateToDishScreen, navigateToOrderScreen)
}

@Composable
fun ListScreenArguments(navigateToDishScreen: (Int) -> Unit, navigateToOrderScreen: () -> Unit){

}