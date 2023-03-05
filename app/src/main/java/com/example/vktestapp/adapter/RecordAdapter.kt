import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.vktestapp.R
import com.example.vktestapp.model.Record


import kotlinx.android.synthetic.main.item_record.view.*

class RecordAdapter : RecyclerView.Adapter<RecordAdapter.RecordViewsHolder>() {

    inner class RecordViewsHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val viewForeground = itemView.view_card
    }

    private val differCallback = object : DiffUtil.ItemCallback<Record>() {
        override fun areItemsTheSame(oldItem: Record, newItem: Record): Boolean {
            return oldItem.filePath == newItem.filePath
        }

        override fun areContentsTheSame(oldItem: Record, newItem: Record): Boolean {
            return oldItem == newItem
        }
    }

    val differ = AsyncListDiffer(this, differCallback)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecordViewsHolder {
        val itemView =
            LayoutInflater.from(parent.context)
                .inflate(R.layout.item_record, parent, false)

        return RecordViewsHolder(itemView)
    }

    override fun onBindViewHolder(holder: RecordViewsHolder, position: Int) {
        val record = differ.currentList[position]
        holder.itemView.apply {
            nameOfRecord.text = record.title
            dateOfRecord.text = record.time

            if (state == "playing" || state == "pause")
                recordDuration.text = generDur(currentDur) + " / " + generDur(barMaxProc.toLong())
            else recordDuration.text = generDur(record.duration)

            if (barProc == 0) progressBar.visibility = View.INVISIBLE
            else progressBar.visibility = View.VISIBLE

            progressBar.max = barMaxProc
            progressBar.progress = barProc
            playStopButton.setOnClickListener { record.clickListener(position) }

            if (record.playStopButtonStatus) {
                playStopButton.supportBackgroundTintList = ContextCompat.getColorStateList(context, R.color.dark_grey)
                playStopButton.setImageResource(R.drawable.ic_pause)
            } else {
                playStopButton.supportBackgroundTintList = ContextCompat.getColorStateList(context, R.color.primary)
                playStopButton.setImageResource(R.drawable.ic_play)
            }
        }
    }
    override fun getItemCount(): Int {
        return differ.currentList.size
    }

    fun generDur(recordDuration: Long): String {
        if (recordDuration >= 3600000L)
            return (recordDuration / 3600000L).toString() + ":" + (recordDuration % 3600000L / 60000L).toString() +
                    ":" + (recordDuration % 3600000L % 60000L / 1000L).toString()
        else {
            if (recordDuration / 60000L == 0L && recordDuration % 60000L / 1000L == 0L)  return (recordDuration / 60000L).toString() +
                    ":" + "1"
            else return (recordDuration / 60000L).toString() + ":" + (recordDuration % 60000L / 1000L).toString()
        }
    }

    private var barProc = 0
    fun setBarProcess(procces: Int) { barProc = procces  }

    private var barMaxProc = 2000
    fun setBarMaxProcess(procces: Int) { barMaxProc = procces }

    private var currentDur = 0L
    fun setCurrentDuration(dur: Int) { currentDur = dur.toLong() }

    private var state = ""
    fun setState(st: String) { state = st }

}