package com.spotify.playit.video

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.content.ContextCompat
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.spotify.playit.video.home.HomeScreen
import com.spotify.playit.video.ui.theme.SpotifyTheme
import com.spotify.playit.video.util.Screen
import com.spotify.playit.video.videoList.VideoListScreen
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class VideoActivity : ComponentActivity() {

    private lateinit var requestPermissionLauncher: ActivityResultLauncher<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        requestPermissionLauncher =
            registerForActivityResult(
                ActivityResultContracts.RequestPermission()
            ) { isGranted: Boolean ->
                if (isGranted) {
                    Toast.makeText(this, "Permission granted", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "Permission not granted", Toast.LENGTH_SHORT).show()
                }
            }

        requestPermissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)


        setContent {

            val contentResolver = contentResolver
            
            val navController = rememberNavController()

            SpotifyTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colors.background
                ) {
                    NavHost(
                        navController = navController,
                        startDestination = Screen.HomeScreen.route
                    ){
                        composable(Screen.HomeScreen.route){
                            HomeScreen(
                                navController = navController
                            )
                        }
                        
                        composable(Screen.VideoListScreen.route+"/{dir_name}"){
                            VideoListScreen(
                                navController = navController
                            )
                        }
                    }
                    

                }
            }
        }
    }
}

@Composable
fun Greeting(name: String) {
    Text(text = "Hello $name!")
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    SpotifyTheme {
        Greeting("Android")
    }
}