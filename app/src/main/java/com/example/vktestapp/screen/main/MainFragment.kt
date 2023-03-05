package com.example.vktestapp.screen.main

import RecordAdapter
import android.app.Activity
import android.graphics.Canvas
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.SimpleItemAnimator
import com.example.vktestapp.MainActivity
import com.example.vktestapp.R
import com.example.vktestapp.databinding.FragmentMainBinding
import com.example.vktestapp.utils.Resource
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.fragment_main.*

class MainFragment : Fragment() {

    private lateinit var mainHandler: Handler
    private lateinit var mViewModel: MainViewModel
    private var _binding: FragmentMainBinding? = null
    private val mBinding get() = _binding!!

    private lateinit var recordAdapter: RecordAdapter


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        mViewModel = (activity as MainActivity).mainViewModel
        _binding = FragmentMainBinding.inflate(layoutInflater, container, false)
        return mBinding.root

    }

    @RequiresApi(Build.VERSION_CODES.S)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView(view)
        mainHandler = Handler(Looper.getMainLooper())
        mViewModel.initMediaPlayer()

        mViewModel.getRecords().observe(viewLifecycleOwner, Observer { records ->
            if (records.isEmpty()) {
                preview_layout.visibility = View.VISIBLE
                title_records.visibility = View.INVISIBLE
            } else {
                preview_layout.visibility = View.INVISIBLE
                title_records.visibility = View.VISIBLE
            }
            recordAdapter.notifyItemChanged(mViewModel.currPosition)

            records.forEach { record ->
                record.playStopButtonStatus = false
                record.clickListener = { position ->
                    mViewModel.playRecord(record.title, position)
                    record.playStopButtonStatus = true
                    enableSeek(true)
                    recordAdapter.notifyItemChanged(position)
                }
            }
            recordAdapter.differ.submitList(records)
        })

        mViewModel.progress.observe(viewLifecycleOwner, Observer { progress ->
            recordAdapter.setBarMaxProcess(mViewModel.recordDurationPlay)
            recordAdapter.setBarProcess(progress)
            recordAdapter.setCurrentDuration(progress)
            if (progress == 0) {
                recordAdapter.setState("stop")
            } else recordAdapter.setState("playing")
            recordAdapter.notifyItemChanged(mViewModel.currPosition)
        })

        mViewModel.playingState.observe(viewLifecycleOwner, Observer { playingState ->
            when (playingState) {
                is Resource.Playing -> {
                    val records = recordAdapter.differ.currentList
                    if (records.isNotEmpty()) {
                        records[mViewModel.currPosition].clickListener = { position ->
                            mViewModel.pauseRecord()
                            records[mViewModel.currPosition].playStopButtonStatus = false
                            enableSeek(false)
                            recordAdapter.notifyItemChanged(position)
                        }
                    }
                }
                is Resource.Pause -> {
                    val records = recordAdapter.differ.currentList
                    if (records.isNotEmpty()) {
                        records[mViewModel.currPosition].clickListener = { position ->
                            mViewModel.resumeRecord()
                            records[mViewModel.currPosition].playStopButtonStatus = true
                            enableSeek(true)
                            recordAdapter.notifyItemChanged(position)
                        }
                    }
                }
                is Resource.Stop -> {
                    val records = recordAdapter.differ.currentList
                    if (mViewModel.currPosition != -1 && records.isNotEmpty()) {
                        records[mViewModel.currPosition].playStopButtonStatus = false
                        enableSeek(false)
                        recordAdapter.notifyItemChanged(mViewModel.currPosition)
                        records[mViewModel.currPosition].clickListener = { position ->
                            mViewModel.playRecord(records[position].title, position)
                            records[position].playStopButtonStatus = true
                            enableSeek(true)
                            recordAdapter.notifyItemChanged(position)
                        }
                    }
                }
                is Resource.Error -> {
                    Toast.makeText(requireContext(), playingState.message, Toast.LENGTH_SHORT)
                        .show()
                }
                else -> {}
            }
        })


        mViewModel.recordingState.observe(viewLifecycleOwner, Observer { recordingState ->
            when (recordingState) {
                is Resource.Recording -> {
                    clearFocusOnEditText()
                    input_name_layout.visibility = View.INVISIBLE
                    recordButton.setImageResource(R.drawable.ic_stop)
                }
                is Resource.Done -> {
                    recordButton.setImageResource(R.drawable.ic_mic)
                }
                is Resource.Error -> {
                    Toast.makeText(requireContext(), recordingState.message, Toast.LENGTH_SHORT).show()
                }
                else -> {}
            }
        })

        ok.setOnClickListener {
            mViewModel.stopPlaying()
            mViewModel.getTitlesRecords(inputName.text.toString())
        }

        cancel.setOnClickListener {
            clearFocusOnEditText()
            input_name_layout.visibility = View.INVISIBLE
        }

        recordButton.setOnClickListener {
            if (mViewModel.recordBtnState == "mic") {
                inputName.text.clear()
                input_name_layout.visibility = View.VISIBLE
                focusOnEditText()
            } else if (mViewModel.recordBtnState == "stop") {
                mViewModel.recordBtnState = "mic"
                mViewModel.stopRecording()
            }
        }

        mViewModel.sameName.observe(viewLifecycleOwner, Observer { status ->
            if (!status) {
                Toast.makeText(requireContext(),"Запись с таким именем уже существует", Toast.LENGTH_SHORT).show()
                mViewModel.sameName.postValue(true)
            }
        })
    }

    private fun enableSeek(bool: Boolean) {
        val runner = object : Runnable {
            override fun run() {
                mViewModel.getProgress()
                mainHandler.postDelayed(this, 5)
            }
        }

        if (bool) mainHandler.post(runner)
        else {
            if (mViewModel.playingState.value is Resource.Stop) mViewModel.setProgress(0)
            mainHandler.removeCallbacksAndMessages(null)
        }
    }

    private fun setupRecyclerView(view: View) {
        (rvRecords.itemAnimator as SimpleItemAnimator).supportsChangeAnimations = false
        recordAdapter = RecordAdapter()
        rvRecords.apply {
            adapter = recordAdapter
            layoutManager = LinearLayoutManager(activity)
        }
        ItemTouchHelper(getItemTouchHelper(recordAdapter, view)).attachToRecyclerView(
            rvRecords
        )
    }

    private fun focusOnEditText() {
        inputName.isFocusableInTouchMode = true
        inputName.isFocusable = true
        inputName.requestFocus()
        val inputMethodManager =
            context?.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.showSoftInput(inputName, InputMethodManager.SHOW_IMPLICIT)
    }

    private fun clearFocusOnEditText() {
        val inputMethodManager =
            context?.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(view?.windowToken, 0)
    }

    private fun getItemTouchHelper(
        recordAdapter: RecordAdapter,
        view: View
    ): ItemTouchHelper.SimpleCallback {
        val itemTouchHelperCallback = object : ItemTouchHelper.SimpleCallback(
            0,
            ItemTouchHelper.LEFT
        ) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                return false
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                mViewModel.stopPlaying()
                enableSeek(false)
                val position = viewHolder.adapterPosition
                Log.d("stopper", position.toString())
                val record = recordAdapter.differ.currentList[position]
                mViewModel.deleteRecord(record)
                Snackbar.make(view, "Запись успешно удалена", Snackbar.LENGTH_LONG).apply {
                    setAction("Отмена") {
                        mViewModel.saveRecord(record)
                    }
                    show()
                }
            }

            override fun onSelectedChanged(viewHolder: RecyclerView.ViewHolder?, actionState: Int) {
                super.onSelectedChanged(viewHolder, actionState)
                if (viewHolder != null) {
                    val foregroundView: View = (viewHolder as RecordAdapter.RecordViewsHolder).viewForeground
                    getDefaultUIUtil().onSelected(foregroundView)
                }
            }

            override fun onChildDraw(
                c: Canvas,
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                dX: Float,
                dY: Float,
                actionState: Int,
                isCurrentlyActive: Boolean
            ) {
                val foregroundView: View = (viewHolder as RecordAdapter.RecordViewsHolder).viewForeground
                getDefaultUIUtil().onDraw( c, recyclerView, foregroundView, dX, dY, actionState, isCurrentlyActive)
            }

            override fun onChildDrawOver(
                c: Canvas,
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder?,
                dX: Float,
                dY: Float,
                actionState: Int,
                isCurrentlyActive: Boolean
            ) {
                val foregroundView: View = (viewHolder as RecordAdapter.RecordViewsHolder).viewForeground
                getDefaultUIUtil().onDrawOver(  c, recyclerView, foregroundView, dX, dY, actionState, isCurrentlyActive)
            }

            override fun clearView(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder
            ) {
                super.clearView(recyclerView, viewHolder)
                val foregroundView: View = (viewHolder as RecordAdapter.RecordViewsHolder).viewForeground
                getDefaultUIUtil().clearView(foregroundView)
            }
        }
        return itemTouchHelperCallback
    }

    override fun onStop() {
        super.onStop()
        mViewModel.stopPlaying()
        enableSeek(false)
    }
}