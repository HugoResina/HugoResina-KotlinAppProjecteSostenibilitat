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



    fun isDishInStock(dish: Dish): Boolean {
        // Verificar si todos los ingredientes del plato están en stock o con descuento
        return dish.ingredientsName.all { ingredientName ->
            val inStock = IngStock.any { it.name == ingredientName }
            val inDiscount = IngToDiscount.any { it.name == ingredientName }
            inStock || inDiscount
        }
    }

    fun moveAside(dish: Dish) {
        if (isDishInStock(dish)) {
            // 1) Añadir el plato directamente
            addDish(dish)

            // 2) Actualizamos stock / ingredientes / listas en background
            viewModelScope.launch(Dispatchers.Default) {
                val ingredientsToDiscount = mutableListOf<Ingridient>()
                val ingredientsInStock = mutableListOf<Ingridient>()

                // 3) Copiar las listas para evitar modificación concurrente
                val ingToDiscountCopy = IngToDiscount.toList()
                val ingStockCopy = IngStock.toList()
                val ingToErraseCopy = IngToErrase.toList()

                // 4) Creamos listas auxiliares para los ingredientes que deben ser añadidos a IngToErrase
                val newIngToErrase = mutableListOf<Ingridient>()

                // Recorrer todos los ingredientes del plato
                dish.ingredients.forEach { DIng ->
                    val ingredientName = DIng.name

                    // Verificar si el ingrediente ya está en las listas de descuento o stock
                    val discountMatch = ingToDiscountCopy.any { it.name == ingredientName }
                    val stockMatch = ingStockCopy.any { it.name == ingredientName }

                    // Si el ingrediente está en descuento y no está en IngToErrase, lo añadimos
                    if (discountMatch && !ingToErraseCopy.any { it.name == ingredientName }) {
                        newIngToErrase.add(DIng)
                    }
                    // Si el ingrediente está en stock y no está en IngToErrase, lo añadimos
                    else if (stockMatch && !ingToErraseCopy.any { it.name == ingredientName }) {
                        newIngToErrase.add(DIng)
                    }
                }

                // 5) Ahora podemos actualizar IngToErrase con los nuevos ingredientes
                IngToErrase.addAll(newIngToErrase)

                // 6) Eliminar los ingredientes de IngToDiscount y IngStock
                val idsToRemove = newIngToErrase.map { it.id }.toSet()

                // 7) Eliminar los ingredientes de IngToDiscount
                IngToDiscount.removeAll { it.id in idsToRemove }

                // 8) Eliminar los ingredientes de IngStock
                IngStock.removeAll { it.id in idsToRemove }

                // 9) Recalcular ingredientes a punto de caducar
                getNearExpirationIng()

                // 10) Refrescar el menú desde la API
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

        // Información de depuración
        println("DESCUENTO")
        IngToDiscount.forEach { d -> println("Descuento: $d") }
        println("---")
        println("STOCK")
        IngStock.forEach { d -> println("Stock: $d") }
        println("---")
        println("TOERASE")
        IngToErrase.forEach { d -> println("Errase: $d") }
        println("---")
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

    fun addDish(dish: Dish?) {
        if (dish != null) {
            // 1) Añadir el plato a SelectedList
            SelectedList.add(dish)

            // 2) Recorrer los ingredientes del plato
            val missingIngredients = mutableListOf<Ingridient>()
            val discountedIngredients = mutableListOf<Ingridient>()
            val allIngredientsInStockOrDiscount = dish.ingredients.all { ingredient ->
                val inStock = IngStock.any { it.name == ingredient.name }
                val inDiscount = IngToDiscount.any { it.name == ingredient.name }
                if (!inStock && !inDiscount) {
                    missingIngredients.add(ingredient) // Guardar ingredientes faltantes
                }
                if (inDiscount) {
                    discountedIngredients.add(ingredient) // Guardar ingredientes con descuento
                }
                inStock || inDiscount // Verificar si está en stock o descuento
            }

            // 3) Si hay ingredientes faltantes, añadir el plato a NoStockDishes
            if (missingIngredients.isNotEmpty()) {
                NoStockDishes.add(dish)
            }
            // 4) Si tiene ingredientes con descuento, añadir el plato a DishToDiscount
            else if (discountedIngredients.isNotEmpty()) {
                DishToDiscount.add(dish)
            } else {
                // Si está completo, lo mantenemos en Dishes
                Dishes.add(dish)
            }

            // 5) Eliminar los ingredientes de IngToDiscount y IngStock si están en uso
            val idsToRemove = (discountedIngredients + missingIngredients).map { it.id }.toSet()

            IngToDiscount.removeAll { it.id in idsToRemove }
            IngStock.removeAll { it.id in idsToRemove }

            // 6) Recalcular ingredientes a punto de caducar y refrescar listas
            getNearExpirationIng()
            getDiscountedOrNoStockDish()
        }
    }

    fun getDiscountedOrNoStockDish() {
        // Limpiar listas de platos sin stock y con descuento
        DishToDiscount.clear()
        NoStockDishes.clear()

        // Recorremos cada plato
        for (dish in Dishes) {
            var hasMissingIngredients = false
            var hasDiscountedIngredients = false

            // Recorremos cada ingrediente del plato
            for (ingredientName in dish.ingredientsName) {
                // Validamos si el ingrediente está en stock o con descuento
                val inStock = IngStock.any { it.name == ingredientName }
                val inDiscount = IngToDiscount.any { it.name == ingredientName }

                // Si el ingrediente no está en stock ni en descuento, lo marcamos como "falta"
                if (!inStock && !inDiscount) {
                    hasMissingIngredients = true
                }

                // Si el ingrediente está en descuento, lo marcamos
                if (inDiscount) {
                    hasDiscountedIngredients = true
                }
            }

            // Si tiene ingredientes faltantes o está vacío, lo ponemos en NoStockDishes
            if (dish.ingredientsName.isEmpty() || hasMissingIngredients) {
                NoStockDishes += dish
            }
            // Si tiene ingredientes en descuento, lo ponemos en DishToDiscount
            else if (hasDiscountedIngredients) {
                DishToDiscount += dish
            }
        }

        // Finalmente quitamos los platos que han sido puestos en NoStockDishes y DishToDiscount
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

                println("Error inicializando datos")
            } finally {
                isLoading.value = false
            }
        }
    }


}