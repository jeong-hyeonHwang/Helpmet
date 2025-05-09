package com.a303.helpmet.presentation.feature.voiceinteraction.sound

import android.content.Context
import android.media.MediaPlayer
import com.a303.helpmet.R
import com.a303.helpmet.domain.model.DirectionState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class TickSoundManager(
    private val context: Context,
    private val directionState: StateFlow<DirectionState>,
    private val scope: CoroutineScope
) {
    private var job: Job? = null
    private var mediaPlayer: MediaPlayer? = null

    fun start(){
        job?.cancel()
        job = scope.launch {
            directionState.collectLatest { state ->
                when(state){
                    DirectionState.Left, DirectionState.Right -> {
                        playTickSound()
                    }
                    DirectionState.None -> {
                        stop()
                    }
                }
            }
        }
    }

    private fun playTickSound(){
        if(mediaPlayer == null){
            mediaPlayer = MediaPlayer.create(context, R.raw.tick_sound).apply {
                isLooping = true
                setVolume(0f, 0f)
                start()
                setOnErrorListener { _, what, extra ->
                    stop()
                    true
                }
            }
            fadeInVolume()
        }else if (mediaPlayer?.isPlaying == false){
            mediaPlayer?.start()
            fadeInVolume()
        }
    }

    private fun fadeInVolume(durationMs: Long = 500L) {
        val steps = 20
        val interval = durationMs / steps
        var currentStep = 0
        CoroutineScope(Dispatchers.Main).launch {
            while (currentStep <= steps) {
                val volume = currentStep.toFloat() / steps
                mediaPlayer?.setVolume(volume, volume)
                delay(interval)
                currentStep++
            }
        }
    }

    fun stop() {
        mediaPlayer?.let {
            if(it.isPlaying){
                it.stop()
            }
            it.release()
        }
    }

    fun releaseAll(){
        stop()
        job?.cancel()
        job = null
    }
}
