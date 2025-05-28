package cat.itb.m78.exercices.bar.viewModels

import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cat.itb.m78.exercices.bar.MyApi
import cat.itb.m78.exercices.bar.models.Dish
import cat.itb.m78.exercices.db.SelectedDishes
import kotlinx.coroutines.launch

class OrderViewModel(private val order: List<SelectedDishes>): ViewModel() {
    val dishes = mutableStateOf<List<Dish>>(emptyList())
    val totalPrice = mutableDoubleStateOf(0.0)

    init {
        viewModelScope.launch {
            dishes.value = MyApi.listMenu()
            dishes.value = order.mapNotNull {
                selected -> dishes.value.find { it.name == selected.name }
            }

            totalPrice.doubleValue = dishes.value.sumOf { it.price }
        }
    }

}