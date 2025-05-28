package cat.itb.m78.exercices.bar.viewModels

import androidx.lifecycle.ViewModel
import cat.itb.m78.exercices.sqldelight.database
import androidx.compose.runtime.mutableStateOf
import cat.itb.m78.exercices.db.SelectedDishes

class DatabaseViewModel: ViewModel() {
    private val dbQueries = database.selectedDishesQueries

    var selectedDishes = mutableStateOf<List<SelectedDishes>>(emptyList())
        private set

    var isDishSelected = mutableStateOf(false)
        private set

    fun getSelectedDishes() = dbQueries.selectAll().executeAsList()

    fun refreshSelectedDishes() {
        selectedDishes.value = dbQueries.selectAll().executeAsList()
    }

    fun addSelectedDish(dishName: String) {
        dbQueries.insert(dishName)
        refreshSelectedDishes()
        isDishSelected.value = true
    }

    fun removeSelectedDish(dishName: String) {
        dbQueries.delete(dishName)
        refreshSelectedDishes()
        isDishSelected.value = false
    }

    fun isDishSelected(dishName: String) {
        isDishSelected.value = dbQueries.selectByName(dishName).executeAsOneOrNull() != null
    }

    // Clear all selected dishes from database
    fun clearSelectedDishes() {
        dbQueries.deleteAll()
        refreshSelectedDishes()
    }
}