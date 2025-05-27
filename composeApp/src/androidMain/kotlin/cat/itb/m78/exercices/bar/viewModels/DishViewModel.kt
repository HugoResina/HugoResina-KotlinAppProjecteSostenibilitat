package cat.itb.m78.exercices.bar.viewModels

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cat.itb.m78.exercices.bar.MyApi
import cat.itb.m78.exercices.bar.models.Dish
import kotlinx.coroutines.launch

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