package com.spotify.playit.video.home

import androidx.lifecycle.*
import com.spotify.playit.video.data.VideoRepository
import com.spotify.playit.video.models.Video
import com.spotify.playit.video.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeScreenViewModel @Inject constructor(
    private val repository: VideoRepository
):ViewModel() {


    private val _videosResource = MutableLiveData<Resource<Map<String,List<Video>>>>()
    val videosResource: LiveData<Resource<Map<String, List<Video>>>> get() = _videosResource

    init {
        getVideosFromDevice()
    }

    private fun getVideosFromDevice() = viewModelScope.launch {
        repository.getVideosFromDevice()
            .onEach {
                _videosResource.value = it
            }
            .catch {
                _videosResource.value = Resource.error("Something went wrong..", null)
            }
            .launchIn(viewModelScope)
    }
}
