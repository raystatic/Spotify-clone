package com.spotify.playit.video.videoList

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
class VideoViewModel @Inject constructor(
    private val repository: VideoRepository,
    private val savedStateHandle: SavedStateHandle
):ViewModel() {

    private val _videos = MutableLiveData<Pair<String,List<Video>>>()
    val videos:LiveData<Pair<String, List<Video>>> get() = _videos

    init {
        savedStateHandle.get<String>("dir_name")?.let {
            getVideosOfDirFromDevice(it)
        }

    }

    private fun getVideosOfDirFromDevice(dir: String) = viewModelScope.launch {
        repository.getVideosFromDevice()
            .onEach {
               _videos.value = Pair(dir,it.data?.get(dir) ?: listOf())
            }
            .catch {
                _videos.value = Pair(dir,listOf())
            }
            .launchIn(viewModelScope)
    }
}
