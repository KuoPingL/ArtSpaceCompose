package com.javanrhinos.artspace

import android.content.res.Configuration
import android.os.Bundle
import android.view.MotionEvent
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.pointerInteropFilter
import androidx.compose.ui.layout.SubcomposeLayout
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.onPlaced
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.javanrhinos.artspace.ui.theme.ArtSpaceTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.roundToInt

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ArtSpaceTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colors.background
                ) {
                    ArtSpace()
                }
            }
        }
    }
}

data class Artwork(val artwork: String, val artist: String, val year: Int, val imgId: Int, val descriptionId: Int)

@OptIn(ExperimentalMaterialApi::class, ExperimentalComposeUiApi::class)
@Composable
fun ArtSpace() {

    // To create Snackbar
    val scaffoldState: ScaffoldState = rememberScaffoldState()

    var index by remember {
        mutableStateOf(0)
    }

    val artworks = listOf(
        Artwork("Black Sparrow", "Charles Bukowski", 1827, R.drawable.black_sparrow_charles_bukowski, R.string.black_sparrow_charles_bukowski),
        Artwork("Lake and Village", "Herman Hesse", 1887, R.drawable.lake_and_village_herman_hesse, R.string.lake_and_village_herman_hesse),
        Artwork("On the Road", "Jack Kerouac", 1915, R.drawable.on_the_road_jack_kerouac, R.string.on_the_road_jack_kerouac),
        Artwork("Typewriter", "Genter Grass", 1723, R.drawable.typewriter_genter_grass, R.string.typewriter_genter_grass),
        Artwork("The Declaration of Independence", "John Trumbull", 1786, R.drawable.the_declaration_of_independence_1786_john_trumbull, R.string.the_declaration_of_independence_1786_john_trumbull)
    )

    val onNext = {
        index += 1
        index %= artworks.size
    }

    val onPrevious = {
        index -= 1
        if(index < 0) {
            index = artworks.size - 1
        }
        index %= artworks.size
    }

    var shouldResetArtworkOffset = false
    val swipeableState = rememberSwipeableState(0, confirmStateChange = {state ->

        if (state == -1) {
            shouldResetArtworkOffset = true
            onPrevious()
        } else if (state == 1) {
            shouldResetArtworkOffset = true
            onNext()
        }

        true
    })

    var offsetX by remember { mutableStateOf(0f) }
    var shouldConsume by remember {
        mutableStateOf(true)
    }
    var width by remember {
        mutableStateOf(0)
    }


    var eventX by remember {
        mutableStateOf(0f)
    }

    Scaffold(scaffoldState = scaffoldState, modifier = Modifier.wrapContentSize()) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .padding(start = 8.dp, end = 8.dp, top = 2.dp, bottom = 4.dp)
                .fillMaxHeight(),
        ) {
            Column(verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .weight(1f)
                    .onGloballyPositioned {
                        if (width == it.size.width) return@onGloballyPositioned
                        width = it.size.width
                    }
                    .pointerInteropFilter {
                        when (it.action) {
                            MotionEvent.ACTION_DOWN -> {
                                if (!shouldConsume) {
                                    shouldConsume = true
                                }

                                eventX = it.x
                            }

                            MotionEvent.ACTION_MOVE -> {
                                if (shouldConsume) {
                                    offsetX = it.x - eventX

                                    if (offsetX >= width / 5) {
                                        onNext()
                                        shouldConsume = false
                                        offsetX = 0f
                                    } else if (abs(offsetX) >= width / 5) {
                                        onPrevious()
                                        shouldConsume = false
                                        offsetX = 0f
                                    }

                                } else {
                                    return@pointerInteropFilter false
                                }
                            }
                        }

                        true
                    }
                    .offset { IntOffset(offsetX.roundToInt(), 0) }
                    .alpha((-abs(offsetX) + width / 5) / (width / 5))
            ) {
                ArtworkWall(
                    artwork = artworks[index],
                    scaffoldState = scaffoldState,
                    onNext = onNext,
                    onPrevious = onPrevious
                )
//                SwipeableArtworkWall (
//                    artwork = artworks[index],
//                    scaffoldState = scaffoldState,
//                    swipeableState = swipeableState,
//                    shouldReset = shouldResetArtworkOffset
//                )
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                ArtworkDescription(artwork = artworks[index])
                Spacer(modifier = Modifier.height(20.dp))
                DisplayController(onNext = onNext, onPrevious = onPrevious)
            }

        }
    }

}

@OptIn(ExperimentalMaterialApi::class, ExperimentalComposeUiApi::class)
@Composable
fun ArtworkWall(artwork: Artwork,
                scaffoldState: ScaffoldState,
                onNext: () -> Unit,
                onPrevious: () -> Unit
) {
    val coroutineScope: CoroutineScope = rememberCoroutineScope()

    Box(
        modifier = Modifier
            .padding(start = 20.dp, end = 20.dp, top = 20.dp, bottom = 20.dp)
            .border(width = 2.dp, color = Color.Black)
            .wrapContentSize()
            )
        {

        Image(
            painter = painterResource(id = artwork.imgId),
            contentDescription = stringResource(id = artwork.descriptionId),
            modifier = Modifier
                .padding(start = 30.dp, end = 30.dp, top = 30.dp, bottom = 30.dp)
                .pointerInput(null) {
                    detectTapGestures(onLongPress = {
                        coroutineScope.launch {
                            scaffoldState.snackbarHostState.showSnackbar(
                                artwork.artwork + " by " + artwork.artist,
                                "Do Something"
                            )
                        }
                    })
                }
        )
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun SwipeableArtworkWall(artwork: Artwork,
                         scaffoldState: ScaffoldState,
                         swipeableState: SwipeableState<Int>,
                         shouldReset: Boolean = false)
{

    var shouldResetArtworkOffset by remember {
        mutableStateOf(false)
    }

    if (shouldReset) shouldResetArtworkOffset = shouldReset

    val coroutineScope: CoroutineScope = rememberCoroutineScope()

    var width by remember {
        mutableStateOf(0)
    }

    val configuration = LocalConfiguration.current
    val screenWidth = with(LocalDensity.current) {
        configuration.screenWidthDp.dp.toPx()
    }

    LaunchedEffect(shouldReset) {
        if (shouldResetArtworkOffset) {
            coroutineScope.launch {
                swipeableState.snapTo(0)
                shouldResetArtworkOffset = false
            }
        }
    }

    // px to state
    val anchors = mapOf(
        -screenWidth/4f to -1,
        0f to 0,
        screenWidth/4f to 1
    )

    Box(
        modifier = Modifier
            .onGloballyPositioned {
                if (width == it.size.width) return@onGloballyPositioned
                width = it.size.width
            }
            .swipeable(
                state = swipeableState,
                anchors = anchors,
                Orientation.Horizontal,
                thresholds = { _, _ ->
                    FractionalThreshold(0.5f)
                })
            .offset { IntOffset(swipeableState.offset.value.roundToInt(), 0) }
            .alpha((-abs(swipeableState.offset.value) + width / 4) / (width / 4))
            .padding(start = 20.dp, end = 20.dp, top = 20.dp, bottom = 20.dp)
            .border(width = 2.dp, color = Color.Black)
            .wrapContentSize()
    )
    {

        Image(
            painter = painterResource(id = artwork.imgId),
            contentDescription = stringResource(id = artwork.descriptionId),
            modifier = Modifier
                .padding(start = 30.dp, end = 30.dp, top = 30.dp, bottom = 30.dp)
                .pointerInput(null) {
                    detectTapGestures(onLongPress = {
                        coroutineScope.launch {
                            scaffoldState.snackbarHostState.showSnackbar(
                                artwork.artwork + " by " + artwork.artist,
                            )
                        }
                    })
                }
        )
    }
}

@Composable
fun ArtworkDescription(artwork: Artwork) {
    Column(horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxWidth()) {
        Spacer(modifier = Modifier.height(5.dp))
        Card(modifier = Modifier
            .shadow(elevation = 2.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(10.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center) {
                Text(text = artwork.artwork, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(5.dp))
                Text(buildAnnotatedString {
                    append("${artwork.artist} (")
                    withStyle(style = SpanStyle(fontStyle = FontStyle.Italic)) {
                        append("${artwork.year}")
                    }
                    append(")")
                }, fontSize = 16.sp)
            }
        }
    }
}

@Composable
fun DisplayController(onNext: ()->Unit, onPrevious: ()->Unit) {

    val configuration = LocalConfiguration.current
    when (configuration.orientation) {
        Configuration.ORIENTATION_LANDSCAPE -> {
            DisplayLandscapeController(onNext = onNext, onPrevious = onPrevious)
        }
        else -> {
            DisplayPortraitController(onNext = onNext, onPrevious = onPrevious)
        }
    }


}

@Composable
fun DisplayLandscapeController(onNext: ()->Unit, onPrevious: ()->Unit) {
    Row(horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp)) {

        SubcomposeLayout { constraints ->
            val mainPlaceables = subcompose("previous") {
                Button(onClick = onPrevious,
                    modifier = Modifier.wrapContentSize()) {
                    Text(text = "Previous")
                }
            }.map {
                it.measure(constraints)
            }

            val maxSize = mainPlaceables.fold(IntSize.Zero) { currentMax, placeable ->
                IntSize(
                    width = maxOf(currentMax.width, placeable.width),
                    height = maxOf(currentMax.height, placeable.height)
                )
            }


            layout(maxSize.width, maxSize.height) {
                mainPlaceables.forEach { it.placeRelative(0, 0) }
                subcompose("Next") {
                    Button(onClick = onNext, modifier = Modifier.width(maxSize.width.toDp())) {
                        Text(text = "Next")
                    }
                }.forEach {
                    val placeable = it.measure(constraints)
                    placeable.placeRelative(constraints.maxWidth - maxSize.width, 0)
                }
            }
        }
    }
}

@Composable
fun DisplayPortraitController(onNext: ()->Unit, onPrevious: ()->Unit) {
    Row(horizontalArrangement = Arrangement.End,
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp)) {
        Button(onClick = onPrevious,
            modifier = Modifier.weight(1f)) {
            Text(text = "Previous")
        }
        Spacer(modifier = Modifier.width(8.dp))
        Button(onClick = onNext, modifier = Modifier.weight(1f)) {
            Text(text = "Next")
        }
    }
}

// This is just an example for defining Margin using padding
@Composable
fun MarginWall() {
    Column() {
        Box(
            Modifier
                .background(Color.Cyan)
                .fillMaxWidth()
                .height(100.dp)
                .padding(10.dp)
                .background(Color.LightGray)
                .padding(start = 5.dp, top = 5.dp, end = 5.dp)){
            Text(text = "Inner Text", modifier = Modifier.fillMaxWidth().background(Color.Green), textAlign = TextAlign.Start)
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun DefaultPreview() {
    ArtSpaceTheme {
        ArtSpace()
    }
}