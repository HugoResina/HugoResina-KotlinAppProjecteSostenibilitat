package cat.itb.m78.exercices

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.daysUntil
import kotlinx.datetime.toLocalDateTime

class ItemViewModel : ViewModel() {
    var Dishes = mutableStateListOf<Dish>()
    var NoStockDishes = mutableStateListOf<Dish>()
    var IngStock = mutableStateListOf<Ingridient>()
    var SelectedList = mutableStateListOf<Dish?>()
    var seleccionat = mutableStateOf<Dish?>(null)

    var isLoading = mutableStateOf(true)  // Indicador de carga

    //lista para guardar los platos a descontar
    var DishToDiscount = mutableStateListOf<Dish>()
    //lista para guardar ingredientes a punto de caducar
    var IngToDiscount = mutableStateListOf<Ingridient>()
    //lista para devolver a la api que ingredientes borrar
    var IngToErrase = mutableStateListOf<Ingridient>()


    fun addDish(dish: Dish?) {
        SelectedList.add(dish)
    }
    fun isDishInStock(dish: Dish): Boolean {
        // Verificar si todos los ingredientes del plato están en stock o con descuento
        return dish.ingredientsName.all { ingredientName ->
            val inStock = IngStock.any { it.name == ingredientName }
            val inDiscount = IngToDiscount.any { it.name == ingredientName }
            inStock || inDiscount
        }
    }
    fun moveAside(dish: Dish) {
        IngToDiscount.forEach { d ->
            println("disc")
            println(d)
        }
        println("---")
        IngStock.forEach { d ->
            println("stock")
            println(d)
        }
        println("---")
        if (isDishInStock(dish)) {
            // 1) Añadimos YA el plato
            addDish(dish)

            // 2) Actualizamos stock / ingredientes / listas en background
            viewModelScope.launch(Dispatchers.Default) {
                // Mover ingredientes a eliminar
                IngToErrase.addAll(dish.ingredients)

                // Eliminar ingredientes usados del stock
                IngStock.removeAll { it in dish.ingredients }

                // Recalcular ingredientes a punto de caducar
                IngToDiscount.clear()
                getNearExpirationIng()

                // Refrescar menú desde la API y reclasificar
                val fresh = MyApi.listMenu()
                Dishes.clear()
                Dishes.addAll(fresh)

                DishToDiscount.clear()
                NoStockDishes.clear()
                getDiscountedOrNoStockDish()
            }
        } else {
            println("El plato ${dish.name} no tiene stock suficiente.")
        }
    }

    fun getNearExpirationIng() {
        val expired = mutableListOf<Ingridient>()

        val stockCopy = IngStock.toList() // Copia para evitar modificación concurrente
        for (ingredient in stockCopy) {
            val ingDate = LocalDate.parse(ingredient.expirationDate.split("T")[0])
            val locDate = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
            val diff = locDate.daysUntil(ingDate)

            if (diff <= 7) {
                expired.add(ingredient)
            }
        }

        IngToDiscount.clear()
        IngToDiscount.addAll(expired)

        // Ahora es seguro modificar la lista original
        IngStock.removeAll(expired)
    }

    fun getDiscountedOrNoStockDish() {
        DishToDiscount.clear()
        NoStockDishes.clear()

        for (dish in Dishes) {
            // 1) Sin stock
            val missing = dish.ingredientsName.any { name ->
                IngStock.none { it.name == name } && IngToDiscount.none { it.name == name }
            }
            if (dish.ingredientsName.isEmpty() || missing) {
                NoStockDishes += dish
                continue
            }

            // 2) Descuento
            val hasNearExpiry = dish.ingredientsName.any { name ->
                IngToDiscount.any { it.name == name }
            }
            if (hasNearExpiry) {
                DishToDiscount += dish
                continue
            }

            // 3) Pleno stock: no añadimos a ninguna lista
        }

        // Finalmente quitamos de Dishes los que pusimos en las otras dos
        Dishes.removeAll(NoStockDishes)
        Dishes.removeAll(DishToDiscount)
    }

    init {
        viewModelScope.launch {
            isLoading.value = true
            try {
                MyApi.login("admin@admin", "admin1234")

                // Lanzamos ambas peticiones en paralelo
                val (dishes, ingredients) = coroutineScope {
                    val d = async(Dispatchers.IO) { MyApi.listMenu() }
                    val i = async(Dispatchers.IO) { MyApi.listStock() }
                    d.await() to i.await()
                }

                // Sustituimos el contenido de los MutableStateList
                Dishes.clear()
                Dishes.addAll(dishes)

                IngStock.clear()
                IngStock.addAll(ingredients)

                // Calculamos caducidades y clasificamos platos
                getNearExpirationIng()
                getDiscountedOrNoStockDish()

            } catch (e: Exception) {
                // Aquí podrías exponer un mensaje de error al UI
                println("Error inicializando datos")
            } finally {
                isLoading.value = false
            }
        }
    }


}