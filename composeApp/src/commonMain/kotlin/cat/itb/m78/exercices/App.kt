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


object AuthManager {
    private var token: String? = null

    fun saveToken(newToken: String){
        token = newToken
    }

    fun getToken(): String? = token
}


object MyApi{
    private val url = "https://api-bar-g9d5c3fshvargsbk.northeurope-01.azurewebsites.net/api/"
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


    @OptIn(InternalAPI::class)


    suspend fun listMenu(): List<Dish> {
        val response = client.get(url + "menu") {
            AuthManager.getToken()?.let { token ->
                header(HttpHeaders.Authorization, "Bearer $token")
            }
            accept(ContentType.Application.Json)
        }

        val responseText = response.bodyAsText()


        return response.body()
    }

    suspend fun listStock(): List<Ingridient> {
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



enum class EScreen {
    ViewE, SelectE, OrderE
}


@Serializable
data class Dish (var name: String, var imageUrl: String, var price: Double, var description: String, var ingredients: List<Ingridient>)


@Serializable
data class Ingridient(var id: Int, var name: String, var expirationDate: String)

class ItemViewModel : ViewModel() {
    var Dishes = mutableStateListOf<Dish>()
    var IngStock = mutableStateListOf<Ingridient>()
    var SelectedList = mutableStateListOf<Dish?>()
    var seleccionat = mutableStateOf<Dish?>(null)

    //lista para guardar los platos a descontar
    var DishToDiscount = mutableStateListOf<Dish>()
    //lista para guardar ingredientes a punto de caducar
    var IngToDiscount = mutableStateListOf<Ingridient>()
    //lista para devolver a la api que ingredientes borrar
    var IngToErrase = mutableStateListOf<Ingridient>()


    fun addDish(dish: Dish?) {
        SelectedList.add(dish)
    }

    fun moveaside(dish: Dish){
        dish.ingredients.forEach { ing ->
            IngToErrase.add(ing)
            IngStock.remove(ing)
        }
        Dishes.clear()
        DishToDiscount.clear()
        IngToDiscount.clear()
        getNearExpirationIng()
        viewModelScope.launch(Dispatchers.Default){
            val dishesFromApi = MyApi.listMenu()
            Dishes.addAll(dishesFromApi)
            getDiscountedDish()
        }

    }


    fun getNearExpirationIng(){
            IngStock.forEach { ingridient ->
            val ingDate = LocalDate.parse(ingridient.expirationDate.split("T")[0])
            val locDate =  Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
            val diff = locDate.daysUntil(ingDate)

            if(diff <= 7){
                IngToDiscount.add(ingridient)
            }
        }
    }

    fun getDiscountedDish(){
        Dishes.forEach { dish ->
            if (dish.ingredients.any{it in IngToDiscount}) {
                DishToDiscount.add(dish)
            }
        }
        DishToDiscount.forEach { dish ->
            Dishes.remove(dish)
        }
    }

    init {
        viewModelScope.launch(Dispatchers.Default) {
            MyApi.login("admin@admin", "admin1234")
            val dishesFromApi = MyApi.listMenu()
            Dishes.addAll(dishesFromApi)
            val ingridientsFromApi = MyApi.listStock()
            IngStock.addAll(ingridientsFromApi)
            IngStock.forEach { ing ->
                println(ing.name)
            }
            getNearExpirationIng()
            getDiscountedDish()
        }
    }
}


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



@Composable
fun OrderScreen(
    onGoBack: () -> Unit,
    viewModel: ItemViewModel,
    )
{
    val selectedList = viewModel.SelectedList

    Scaffold {

        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            selectedList.forEach { selected ->
                item {
                    selected?.let {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = it.name,
                                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium)
                                )

                                Button(
                                    onClick = {
                                        viewModel.SelectedList.remove(selected)
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                                ) {
                                    Text("Delete", color = Color.White)
                                }
                            }
                        }
                    }
                }
            }
        }

        FloatingActionButton(
            onClick = { onGoBack() },
            modifier = Modifier
                .padding(24.dp)
        ) {
            Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
        }
    }
}


@Composable
fun ListScreen(
    onSeeList: () -> Unit,
    onViewI: () -> Unit,
    viewModel: ItemViewModel
) {
    val itemList = viewModel.Dishes
    val discountList = viewModel.DishToDiscount


    /*if (itemList.isEmpty()) {
        for (i in 1..15) {
            val placeholder = Dish("Plato $i", "https://fakeimg.pl/400x400/?text=Dish+$i", i.toDouble(), "", )
            itemList.add(placeholder)
        }
    }*/

    Scaffold {


        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 150.dp),
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            contentPadding = PaddingValues(bottom = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            itemList.forEach { dish ->
                item {
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(1f)
                            .clickable {
                                viewModel.seleccionat.value = dish
                                onViewI()
                            }
                    ) {
                        Box(modifier = Modifier.fillMaxSize()
                            .border(width = 1.dp, shape = RoundedCornerShape(12.dp), color = Color.Gray)
                        ) {
                            AsyncImage(
                                model = dish.imageUrl,
                                contentDescription = dish.name,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )

                            Box(
                                modifier = Modifier
                                    .border(width = 1.dp, shape = RoundedCornerShape(4.dp), color = Color.Gray)
                                    .align(Alignment.BottomCenter)
                                    .fillMaxWidth()
                                    .background(
                                        Brush.verticalGradient(
                                            colors = listOf(
                                                Color.Transparent,
                                                Color.Black.copy(alpha = 0.6f)
                                            )
                                        )
                                    )

                                    .padding(8.dp)

                            ) {
                                Text(
                                    text = dish.name,
                                    color = Color.White,
                                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                                    modifier = Modifier.align(Alignment.Center)
                                )
                            }
                        }
                    }
                }
            }

            discountList.forEach { dish ->
                item {
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(1f)
                            .clickable {
                                viewModel.seleccionat.value = dish
                                onViewI()
                            }
                    ) {
                        Box(modifier = Modifier.fillMaxSize()
                            .border(width = 1.dp, shape = RoundedCornerShape(12.dp), color = Color.Yellow)
                        ) {
                            AsyncImage(
                                model = dish.imageUrl,
                                contentDescription = dish.name,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )

                            Box(
                                modifier = Modifier
                                    .border(width = 1.dp, shape = RoundedCornerShape(4.dp), color = Color.Yellow)
                                    .align(Alignment.BottomCenter)
                                    .fillMaxWidth()
                                    .background(
                                        Brush.verticalGradient(
                                            colors = listOf(
                                                Color.Transparent,
                                                Color.Black.copy(alpha = 0.6f)
                                            )
                                        )
                                    )

                                    .padding(8.dp)

                            ) {
                                Text(
                                    text = dish.name,
                                    color = Color.White,
                                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                                    modifier = Modifier.align(Alignment.Center)
                                )
                            }
                        }
                    }
                }
            }

        }
    }

    FloatingActionButton(
        onClick = { onSeeList() },
        modifier = Modifier

            .padding(24.dp),
        containerColor = MaterialTheme.colorScheme.primary
    ) {
       Text("See order")
    }
}

@Composable
fun DishScreen(
    onGoBack: () -> Unit,
    viewModel: ItemViewModel,

) {
    val selectedDish = viewModel.seleccionat.value

    Scaffold {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .background(MaterialTheme.colorScheme.background)
        ) {
            selectedDish?.let { dish ->
                AsyncImage(
                    model = dish.imageUrl,
                    contentDescription = dish.name,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(280.dp),
                    contentScale = ContentScale.Crop
                )

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            //25" "
                            text = dish.name + "                         "  + dish.price + "€",

                            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold)
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = dish.description,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    OutlinedButton(
                        onClick = onGoBack,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(50)
                    ) {
                        Icon(Icons.Default.ArrowBack, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Volver")
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Button(
                        onClick = {
                            viewModel.addDish(dish)
                            viewModel.moveaside(dish)
                            onGoBack()

                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(50),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Añadir")
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))
            } ?: run {
                Text(
                    text = "No se seleccionó ningún plato.",
                    modifier = Modifier.padding(16.dp),
                    color = Color.Red
                )
            }
        }
    }
}