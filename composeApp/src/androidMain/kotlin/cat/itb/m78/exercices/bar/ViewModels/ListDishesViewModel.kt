package cat.itb.m78.exercices.bar.ViewModels

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cat.itb.m78.exercices.bar.Models.Dish
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.daysUntil
import kotlinx.datetime.toLocalDateTime

class ListDishesViewModel : ViewModel() {
    var discountedDishes = mutableStateOf<List<Dish>>(emptyList())
    var noStockDishes = mutableStateOf<List<Dish>>(emptyList())
    var isLoading = mutableStateOf(true)

    init {
        viewModelScope.launch {
            try {
                MyApi.login("admin@admin", "admin1234")
                val menu = MyApi.listMenu()
                getDiscountedOrNoStockDish(menu)

                isLoading.value = false
            } catch (e: Exception){
                println("Error inicializando datos")
            } finally {
                isLoading.value = false
            }
        }
    }

    private fun getDiscountedOrNoStockDish(dishes: List<Dish>) {
        dishes.forEach { dish ->
            if (!dish.ingredients.any()) noStockDishes.value += dish
            dish.ingredients.forEach { ingredient ->
                val expirationDate = LocalDate.parse(ingredient.expirationDate.split("T")[0])
                val currentDate = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
                val daysUntilExpiration = currentDate.date.daysUntil(expirationDate)

                if (daysUntilExpiration <= 7) discountedDishes.value += dish
            }
        }
    }


}