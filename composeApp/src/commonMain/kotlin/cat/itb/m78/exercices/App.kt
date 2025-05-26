package cat.itb.m78.exercices

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import coil3.compose.AsyncImage
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.DefaultRequest
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.utils.io.InternalAPI
import kotlinx.coroutines.IO
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.datetime.Clock
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.daysUntil
import kotlinx.datetime.toDatePeriod
import kotlinx.datetime.toLocalDate
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.compose.resources.imageResource






enum class EScreen {
    ViewE, SelectE, OrderE
}


@Serializable
data class Dish (var name: String, var imageUrl: String, var price: Double, var ingredientsName: List<String>, var description: String, var ingredients: List<Ingridient> = emptyList())


@Serializable
data class Ingridient(var id: Int, var name: String, var expirationDate: String)




@Composable
fun App() {
    val sharedViewModel: ItemViewModel = viewModel()
    var currentScreen by remember { mutableStateOf(EScreen.SelectE) }

    when (currentScreen) {
        EScreen.SelectE -> ListScreen(
            onSeeList = { currentScreen = EScreen.OrderE },
            onViewI = { currentScreen = EScreen.ViewE },
            viewModel = sharedViewModel
        )

        EScreen.ViewE -> DishScreen(
            onGoBack = { currentScreen = EScreen.SelectE },
            viewModel = sharedViewModel
        )

        EScreen.OrderE -> OrderScreen(
            onGoBack = { currentScreen = EScreen.SelectE },
            viewModel = sharedViewModel
        )
    }
}





