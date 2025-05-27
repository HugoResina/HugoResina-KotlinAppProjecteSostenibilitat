package cat.itb.m78.exercices.bar.ViewModels


import cat.itb.m78.exercices.bar.Models.Dish
import cat.itb.m78.exercices.bar.Models.Ingredient
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.DefaultRequest
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.accept
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

object AuthManager {
    private var token: String? = null

    fun saveToken(newToken: String){
        token = newToken
    }

    fun getToken(): String? = token
}


object MyApi{
    private const val url = "https://api-bar-g9d5c3fshvargsbk.northeurope-01.azurewebsites.net/api/"
    private val client = HttpClient(){
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
            })
        }
        install(DefaultRequest) {
            headers.remove(HttpHeaders.Authorization)
            AuthManager.getToken()?.let { token ->
                header(HttpHeaders.Authorization, "Bearer $token")
            }
        }
    }

    suspend fun listMenu(): List<Dish> {
        val response = client.get(url + "menu") {
            AuthManager.getToken()?.let { token ->
                header(HttpHeaders.Authorization, "Bearer $token")
            }
            accept(ContentType.Application.Json)
        }

        return response.body()
    }

    suspend fun getDishByName(dishName: String): Dish {
        val response = client.get(url + "dish/name/" + dishName) {
            AuthManager.getToken()?.let { token ->
                header(HttpHeaders.Authorization, "Bearer $token")
            }
            accept(ContentType.Application.Json)
        }

        return response.body()
    }

    suspend fun listStock(): List<Ingredient> {
        //ingridients endpoint
        val response = client.get(url + "ingredient") {
            AuthManager.getToken()?.let { token ->
                header(HttpHeaders.Authorization, "Bearer $token")
            }
            accept(ContentType.Application.Json)
        }

        val responseText = response.bodyAsText()
        println("Raw response: $responseText")

        return response.body()
    }

    suspend fun login(email: String, password: String) {
        val response = client.post(url + "auth/login") {
            contentType(ContentType.Application.Json)
            setBody(mapOf("email" to email, "password" to password))
        }

        if (response.status.isSuccess()) {
            val rawToken: String = response.body()
            val token = rawToken.trim('"')
            println("Cleaned token: $token")
            AuthManager.saveToken(token)
        } else {
            val errorMessage: String = response.body()
            println("Login failed: $errorMessage")
        }
    }
}