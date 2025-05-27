package cat.itb.m78.exercices.bar.viewModels

import androidx.lifecycle.ViewModel
import cat.itb.m78.exercices.sqldelight.database
import androidx.compose.runtime.mutableStateOf

class DatabaseViewModel: ViewModel() {
    private val dbQueries = database.selectedDishesQueries

    var isDishSelected = mutableStateOf(false)
        private set

    fun getSelectedDishes() = dbQueries.selectAll().executeAsList()

    fun addSelectedDish(dishName: String) {
        dbQueries.insert(dishName)
        isDishSelected.value = true
    }

    fun removeSelectedDish(dishName: String) {
        dbQueries.delete(dishName)
        isDishSelected.value = false
    }

    fun isDishSelected(dishName: String) {
        isDishSelected.value = dbQueries.selectByName(dishName).executeAsOneOrNull() != null
    }
}