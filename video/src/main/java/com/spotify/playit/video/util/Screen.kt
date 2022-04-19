package com.spotify.playit.video.util

sealed class Screen(val route:String){
    object HomeScreen:Screen("homeScreen")
    object VideoListScreen:Screen("videoListScreen")
}
