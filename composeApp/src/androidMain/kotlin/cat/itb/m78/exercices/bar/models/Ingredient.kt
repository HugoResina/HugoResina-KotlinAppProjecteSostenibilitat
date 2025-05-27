package cat.itb.m78.exercices.bar.models

import kotlinx.serialization.Serializable

@Serializable
data class Ingredient(
    var id: Int,
    var name: String,
    var expirationDate: String
)