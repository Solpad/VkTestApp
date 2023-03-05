package com.example.vktestapp.screen.main

import android.app.Application
import android.content.ContextWrapper
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.os.Build
import android.os.Environment
import androidx.annotation.RequiresApi
import androidx.lifecycle.*
import com.example.vktestapp.model.Record
import com.example.vktestapp.repo.RecordRepo
import com.example.vktestapp.utils.Resource
import kotlinx.coroutines.launch
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*

class MainViewModel(private val recordRepo: RecordRepo, application: Application) : AndroidViewModel(application) {
    private val _recordingState: MutableLiveData<Resource<Record>> = MutableLiveData()
    val recordingState: LiveData<Resource<Record>> get() = _recordingState
    var recordBtnState = "mic"

    private val _playingState: MutableLiveData<Resource<Record>> = MutableLiveData(Resource.Stop())
    val playingState: LiveData<Resource<Record>> get() = _playingState

    private val _progress: MutableLiveData<Int> = MutableLiveData()
    val progress: LiveData<Int> get() = _progress

    private lateinit var mediaRecorder: MediaRecorder
    private lateinit var filePath: String
    private lateinit var mediaPlayer: MediaPlayer

    private var startTime = 0L
    var currPosition: Int = -1
    var sameName: MutableLiveData<Boolean> = MutableLiveData()
    var recordName: String = "name"
    var recordDuration = 0L
    var recordDurationPlay = 0

    @RequiresApi(Build.VERSION_CODES.S)
    fun startRecording() {
        viewModelScope.launch {
            _recordingState.value = Resource.Recording()
            recordAudio()
        }
    }

    @RequiresApi(Build.VERSION_CODES.S)
    private fun recordAudio() {
        filePath = getFilePath(recordName)
        mediaRecorder = MediaRecorder()
        mediaRecorder.apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.AAC_ADTS)
            setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
            setOutputFile(filePath)
        }
        try {
            mediaRecorder.prepare()
            startTime = System.currentTimeMillis()
            mediaRecorder.start()
        } catch (e: IOException) {
            _recordingState.value = Resource.Error("IOException")
            e.printStackTrace()
        } catch (e: IllegalStateException) {
            _recordingState.value = Resource.Error("IllegalStateException")
            e.printStackTrace()
        }
    }

    fun stopRecording() {
        viewModelScope.launch {
            val record = stopRecord()
            _recordingState.value = Resource.Done(record)
        }
    }

    private fun stopRecord(): Record {
        try {
            recordDuration = System.currentTimeMillis() - startTime
            mediaRecorder.stop()
        } catch (e: IOException) {
            _recordingState.value = Resource.Error("IOException on stop recording")
            e.printStackTrace()
        } catch (e: IllegalStateException) {
            _recordingState.value = Resource.Error("IllegalStateException on stop recording")
            e.printStackTrace()
        }

        val record =
            Record(
                title = recordName,
                time = generateDate(),
                filePath = filePath,
                clickListener = {},
                duration = recordDuration
            )
        saveRecord(record)
        return record
    }

    fun saveRecord(record: Record) = viewModelScope.launch {
        recordRepo.insertRecord(record)
    }

    fun getRecords() = recordRepo.getRecordsLive()

    @RequiresApi(Build.VERSION_CODES.S)
    fun getTitlesRecords(title: String) = viewModelScope.launch {
        recordRepo.getTitlesRecords().forEach {
            if (it == title) {
                sameName.postValue(false)
                return@launch
            }
        }
        recordBtnState = "stop"
        recordName = title
        startRecording()
    }

    fun deleteRecord(record: Record) = viewModelScope.launch {
        recordRepo.deleteRecord(record)
    }

    fun initMediaPlayer() {
        mediaPlayer = MediaPlayer()
        mediaPlayer.setOnCompletionListener {
            _playingState.value = Resource.Stop()
        }
    }

    private fun getFilePath(recordName: String): String {
        var contextWrapper = ContextWrapper(getApplication())
        var musDir: File? = contextWrapper.getExternalFilesDir(Environment.DIRECTORY_MUSIC)
        var file = File(musDir, "$recordName.mp3")
        return file.path
    }

    @RequiresApi(Build.VERSION_CODES.S)
    fun playRecord(filePath: String, position: Int) {
        if (mediaPlayer.isPlaying || _playingState.value is Resource.Pause) {
            mediaPlayer.stop()
            _playingState.value = Resource.Stop()
        }
        try {
            initMediaPlayer()
            mediaPlayer.apply {
                setDataSource(getFilePath(filePath))
                prepare()
                start()
            }
            currPosition = position
            recordDurationPlay = mediaPlayer.duration
            _playingState.value = Resource.Playing()

        } catch (e: java.lang.IllegalStateException) {
            _playingState.value = Resource.Error("IllegalStateException on play record")
            e.printStackTrace()
        } catch (e: IOException) {
            _playingState.value = Resource.Error("IOException on play record")
            e.printStackTrace()
        }
    }

    fun stopPlaying() {
        if (mediaPlayer.isPlaying || _playingState.value is Resource.Pause) {
            mediaPlayer.stop()
            _playingState.value = Resource.Stop()
        }
    }

    fun getProgress() {
        _progress.value = mediaPlayer.currentPosition
    }

    fun setProgress(process: Int) {
        _progress.value = process
    }

    fun resumeRecord() {
        mediaPlayer.seekTo(progress.value!!)
        mediaPlayer.start()
        if (mediaPlayer.isPlaying) {
            _playingState.value = Resource.Playing()
        }
    }

    fun pauseRecord() {
        if (mediaPlayer.isPlaying) {
            getProgress()
            mediaPlayer.pause()
        }
        _playingState.value = Resource.Pause()
    }

    private fun generateDate(): String {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val current = LocalDateTime.now()
            val formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy в HH:mm")
            current.format(formatter)
        } else {
            val date = Date()
            val formatter = SimpleDateFormat("dd.MM.yyyy в HH:mm")
            formatter.format(date)
        }
    }
}
