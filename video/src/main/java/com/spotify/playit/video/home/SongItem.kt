package com.spotify.playit.video.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.spotify.playit.video.R
import com.spotify.playit.video.ui.theme.fontFamily

@Composable
fun SongItem(
    modifier: Modifier = Modifier,
    title:String,
    isDirectory:Boolean = false,
    description:String?=null,
    onClick:(String) -> Unit
) {
    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = modifier
            .fillMaxWidth()
            .clickable {
                onClick(title)
            }
            .padding(16.dp)
    ) {
        Text(
            text = title,
            fontSize = 16.sp,
            fontFamily = fontFamily,
            fontWeight = FontWeight.Light,
            color = Color.White,
        )

       if (isDirectory){
           Image(
               painter = painterResource(id = R.drawable.ic_right_arrow),
               contentDescription = "right",
               modifier = Modifier.size(16.dp),
               alignment = Alignment.Center
           )
       }
    }


}