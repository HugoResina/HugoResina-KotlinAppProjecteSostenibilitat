package cat.itb.m78.exercices.bar.ViewModels

import androidx.compose.runtime.MutableState
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
import m78exercices.composeapp.generated.resources.Res

class DishViewModel(dishName: String) : ViewModel() {
    private val dummyDish = Dish("", "", 0.0, emptyList(), "", emptyList())
    val dish = mutableStateOf(dummyDish)

    init {
        viewModelScope.launch {
            try {
                dish.value = MyApi.getDishByName(dishName)
            } catch (e: Exception){
                println("Error inicializando datos")
            }
        }
    }
}