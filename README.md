# Art Space using Compose
This is an exercise from Android [Official Course](https://developer.android.com/codelabs/basic-android-kotlin-compose-art-space?authuser=1&continue=https%3A%2F%2Fdeveloper.android.com%2Fcourses%2Fpathways%2Fandroid-basics-compose-unit-2-pathway-3%3Fauthuser%3D1%23codelab-https%3A%2F%2Fdeveloper.android.com%2Fcodelabs%2Fbasic-android-kotlin-compose-art-space#3) for Jetpack Compose.

<img src="/artspace_landscape.png" style="width:70%" /><img src="/artspace_portrait.png" style="width:30%" />

## Obstacles
Even though this is a beginner exercise, it has proven to be quite challenging. 
Here are some of the main obstacles that was faced and conquered during this exercise.

1. Displaying a **Snackbar**
    For people with experience with traditional Android, you should know that a Snackbar is created as follows :

    ```kotlin
    Snackbar.make(contentView, "I’m Snackbar", Snackbar.LENGTH_LONG).show();
    ```
    which is just the same as Toast. 
    
    <br>
   
    However, in Compose, the creation of Snackbar requires the usage of a CoroutineScope.
    <br>

    The composable displaying the Snackbar must be wrapped within a **Scaffold** so that a **ScaffoldState** 
    can be passed to it, allowing it to trigger Snackbar by calling :
    
    ```kotlin
        scaffoldState.snackbarHostState.showSnackbar(...)
    ```
<br>

2. Swipe to display next / previous image
   This obstacle is the one I find most challenging.
   To solve it, I have tried various ways including :

   a. Using **pointerInput** along with **detectDragGestures**.
      This does help me in detecting swipe gesture and allowing the image to become transparent while moving towards the edges.
      However, problems occur when the new image shows up.
      
      <br>
      As the new image shows up, I simply make the offset of the image to 0, which makes total sense. 
      But, user might not be done with the dragging action yet. 
      This causes the new Image to act upon the old dragging action, making the image shift and become transparent.
      
      <br>
      I tried to add a flag to decide when a new image shows up, but that did not fix the issue.
      So I realized, as long as the dragging action is not terminated, this issue will always occur.

   b. Using **swipeable** api
      I tried using **swipeable** thinking that the problem can be solved by stating three anchor points (initial, left and right).
      However, the problem occur when I tried to trigger the image back to the original state using a **LaunchedEffect**.
      <br>
      ```kotlin
      LaunchedEffect(shouldReset) {
        if (shouldResetArtworkOffset) {
            coroutineScope.launch {
                swipeableState.snapTo(0)
                shouldResetArtworkOffset = false
            }
        }
      }
      ```
      nothing happens. 
      Perhaps I used it incorrectly, I will definitely take a look later. But for now, this is not an option for me.

   c. Using **pointerInteropFilter**
      This is basically the `onTouchEvent` api from traditional Android, so with the same or similar API, everything works just the way expected.

3. Making the `Next` and `Previous` button the same width in Landscape orientation
   Obviously, when I say "making them the same width", it doesn't mean to wrap them inside a **Row** and making them the same weight.
   
   <br>
   Instead, the two buttons are to be separated on each end with a wrap content width, but the shorter one needs to be the same as as the longer one.

   <br>
   
   In order to solve it, **SubcomposeLayout** is used.
   It trigger subcompose the actual content during the measuring stage, allowing us to get the final size of the composable.
   Based on the measured size, we can make the target composable the same size as the measured size.
