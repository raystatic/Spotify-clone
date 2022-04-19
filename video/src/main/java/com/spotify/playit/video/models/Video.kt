package com.spotify.playit.video.models

import android.net.Uri

data class Video(
    val uri:Uri,
    val name:String,
    val duration:Int,
    val size:Int,
    val dir:String
)