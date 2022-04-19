package com.spotify.playit.video.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Divider
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.spotify.playit.video.R
import com.spotify.playit.video.ui.theme.fontFamily
import com.spotify.playit.video.util.Screen
import com.spotify.playit.video.util.Status

@Composable
fun HomeScreen(
    viewModel: HomeScreenViewModel = hiltViewModel(),
    navController: NavController
) {

    Box(
        modifier = Modifier
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

           val videoItems = viewModel.videosResource.observeAsState().value

            when(videoItems?.status){
                Status.SUCCESS -> {
                    val list = videoItems.data?.keys?.toList() ?: listOf()
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ){
                        itemsIndexed(list){index, item->
                            SongItem(
                                title = item,
                                description = "${ videoItems.data?.getValue(list[index])?.size} Files",
                                isDirectory = true,
                                onClick = {
                                    navController.navigate(Screen.VideoListScreen.route+"/${it}")
                                }
                            )

                            if (index < list.lastIndex){
                                Divider(color = Color.LightGray, thickness = 0.5.dp)
                            }

                        }
                    }
                }

                Status.ERROR -> {
                    Text(
                        text = videoItems.message ?: "Unknown error occurred",
                        fontFamily = fontFamily,
                        fontWeight = FontWeight.Light,
                        fontSize = 16.sp
                    )
                }

                Status.LOADING -> {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = Color.White
                    )
                }

            }





        }
    }

}
