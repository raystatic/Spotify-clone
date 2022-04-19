package com.spotify.playit.video.videoList

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.Divider
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.spotify.playit.video.R
import com.spotify.playit.video.home.SongItem
import com.spotify.playit.video.ui.theme.fontFamily

@Composable
fun VideoListScreen(
    videoViewModel: VideoViewModel = hiltViewModel(),
    navController: NavController
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 32.dp, vertical = 32.dp)
    ) {

        val videos = videoViewModel.videos.observeAsState().value

        if (videos!=null){
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                Row {
                    Image(
                        painter = painterResource(id = R.drawable.ic_action_back),
                        contentDescription = "back",
                        modifier = Modifier
                            .size(24.dp)
                            .clickable {
                                navController.popBackStack()
                            }
                    )

                    Spacer(modifier = Modifier.width(10.dp))

                    Text(
                        text = videos.first,
                        fontFamily = fontFamily,
                        fontWeight = FontWeight.Medium,
                        fontSize = 16.sp,
                        color = Color.White
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                if (videos.second.isNullOrEmpty()){
                    Text(
                        text = "No videos found!",
                        fontFamily = fontFamily,
                        fontWeight = FontWeight.Medium,
                        fontSize = 16.sp,
                        color = Color.White
                    )
                }else{
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ){
                        itemsIndexed(videos.second){index, item->
                            SongItem(
                                title = item.name,
                                description = "Duration: ${item.duration}, Size: ${item.size}",
                                isDirectory = false,
                                onClick = {

                                }
                            )

                            if (index < videos.second.lastIndex){
                                Divider(color = Color.LightGray, thickness = 0.5.dp)
                            }

                        }
                    }
                }
            }
        }


    }
}