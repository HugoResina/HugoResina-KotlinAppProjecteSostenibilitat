package cat.itb.m78.exercices.bar.models

import kotlinx.serialization.Serializable

@Serializable
data class Dish (
    var name: String,
    var imageUrl: String,
    var price: Double,
    var ingredientsName: List<String>,
    var description: String,
    var ingredients: List<Ingredient> = emptyList()
)
