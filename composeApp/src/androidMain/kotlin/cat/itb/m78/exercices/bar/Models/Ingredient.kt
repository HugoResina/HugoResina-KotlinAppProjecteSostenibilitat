package cat.itb.m78.exercices.bar.Models

import kotlinx.serialization.Serializable

@Serializable
data class Ingredient(
    var id: Int,
    var name: String,
    var expirationDate: String
)