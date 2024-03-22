package com.example.snake

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MaterialTheme.shapes
import androidx.compose.material3.Shapes
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.snake.ui.theme.SnakeTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlin.random.Random


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SnakeTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                 Snake()
                }
            }
        }
    }
}


data class State(val food:Pair<Int, Int>, val snake:List<Pair<Int,Int>>)

class Game(private val scope: CoroutineScope){
    private val mutex=Mutex()
    private val mutableState = MutableStateFlow(State(food=Pair(5,5), snake=listOf(Pair(7,7))))
    val state =mutableState

    var move =Pair(1,0)
        set(value){
         scope.launch {
             mutex.withLock {
                 field= value
             }
         }
     }

    init{
       scope.launch {
         var snakeLength =4
           while (true){
               delay(150)
               mutableState.update {
                   val newPosition= it.snake.first().let {poz->
                       mutex.withLock {
                           Pair(
                               (poz.first + move.first + BOARD_SIZE) % BOARD_SIZE,
                               (poz.second + move.second + BOARD_SIZE) % BOARD_SIZE
                           )
                       }

                   }
                   if(newPosition == it.food){
                       snakeLength++
                   }
                   if(it.snake.contains(newPosition)){
                       snakeLength =4
                   }
                   it.copy(
                       food = if(newPosition == it.food) Pair(
                           java.util.Random().nextInt(BOARD_SIZE),
                           java.util.Random().nextInt(BOARD_SIZE),
                           )else it.food,
                                snake = listOf(newPosition) + it.snake.take(snakeLength-1)
                   )
               }
           }
       }
    }
    companion object{
        const val  BOARD_SIZE =16
    }
}
@Composable
fun Snake(game: Game){
    val state = game.state.collectAsState(initial=null)
    Column(horizontalAlignment = Alignment.CenterHorizontally){
        state.value?.let{
            Board(it)
        }
    }

}
@Composable
fun Buttons(onDirectionChange:){
    val buttonSize = Modifier.size(64.dp)
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(24.dp)){

    }
}

@Composable
fun Board(state: State) {
   BoxWithConstraints(Modifier.padding(16.dp)){
       val tileSize = maxWidth /Game.BOARD_SIZE

       Box(
           Modifier
               .size(maxWidth)
               .border(2.dp, Color.DarkGray)
       )
           Box(Modifier.offset(x=tileSize * state.food.first, y = tileSize * state.food.second)
               .size(tileSize)
               .background(Color.DarkGray, CircleShape)
           )
       state.snake.forEach{
           Box(modifier = Modifier.offset(x=tileSize * it.first, y = tileSize * it.second)
               .size(tileSize)
               .background(Color.DarkGray))
       }
   }
}

@Preview(showBackground = true)
@Composable
fun SnakePreview() {
    SnakeTheme {
        Snake()
    }
}