package cat.itb.m78.exercices.bar.viewModels

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cat.itb.m78.exercices.bar.MyApi
import cat.itb.m78.exercices.bar.models.Dish
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.daysUntil
import kotlinx.datetime.toLocalDateTime

class ListDishesViewModel : ViewModel() {
    var dishes = mutableStateOf<List<Dish>>(emptyList())
    var discountedDishes = mutableStateOf<List<Dish>>(emptyList())
    var noStockDishes = mutableStateOf<List<Dish>>(emptyList())

    init {
        viewModelScope.launch {
            try {
                MyApi.login("admin@admin", "admin1234")
                dishes.value = MyApi.listMenu()
                getDiscountedOrNoStockDish()
                categorizeDishes()
            } catch (e: Exception){
                println("Error inicializando datos")
            }
        }
    }

    private fun getDiscountedOrNoStockDish() {
        dishes.value.forEach { dish ->
            if (!dish.ingredients.any()) noStockDishes.value += dish
            dish.ingredients.forEach { ingredient ->
                val expirationDate = LocalDate.parse(ingredient.expirationDate.split("T")[0])
                val currentDate = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
                val daysUntilExpiration = currentDate.date.daysUntil(expirationDate)
                if (daysUntilExpiration <= 7) discountedDishes.value += dish
            }
        }
    }

    private fun categorizeDishes(){
        val discountedNames = discountedDishes.value.map { it.name }
        val noStockNames = noStockDishes.value.map { it.name }
        val forbiddenNames = discountedNames + noStockNames

        dishes.value = dishes.value.filter { dish ->
            dish.name !in forbiddenNames
        }
    }


}