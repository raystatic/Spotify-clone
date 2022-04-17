package com.spotify.playit.video.home

import android.util.Log
import android.widget.Space
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.Divider
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.spotify.playit.video.R
import com.spotify.playit.video.ui.theme.fontFamily

@Composable
fun HomeScreen(
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 32.dp, vertical = 48.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
        ) {

            Column(
                modifier = Modifier
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ic_video),
                    contentDescription = "video",
                    modifier = Modifier
                        .size(150.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Offline Videos",
                    color = Color.White,
                    fontFamily = fontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp
                )
            }

            Spacer(modifier = Modifier.height(48.dp))

            val list = listOf("Documents", "Downloads", "DCIM")
            
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ){
                itemsIndexed(list){index, item->
                    SongItem(
                        title = item,
                        description = "8 Files",
                        isDirectory = true,
                        onClick = {

                        }
                    )

                    if (index < list.lastIndex){
                        Divider(color = Color.LightGray, thickness = 1.dp)
                    }

                }
            }



        }
    }

}
