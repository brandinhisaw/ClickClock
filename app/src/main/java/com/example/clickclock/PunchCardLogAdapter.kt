package com.example.clickclock

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@RequiresApi(Build.VERSION_CODES.O)
class PunchCardLogAdapter(private var uniqueDateList: MutableList<LocalDate>, context: Context) : RecyclerView.Adapter<PunchCardLogAdapter.DateHeaderViewHolder>() {

    private var dateFormatter = DateTimeFormatter.ofPattern("MMM dd, yyyy")
    private var timeFormatter = DateTimeFormatter.ofPattern("h:mm a")

    private var parentContext = context

    private val db: PunchCardDatabaseHelper = PunchCardDatabaseHelper(context)

    class DateHeaderViewHolder(dateHeaderView: View) : RecyclerView.ViewHolder(dateHeaderView) {
        val dateHeaderTextView: TextView = dateHeaderView.findViewById(R.id.dateHeaderTextView)
        var dateHeaderLinearLayout: LinearLayout = dateHeaderView.findViewById(R.id.dateHeaderLinearLayout)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DateHeaderViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.date_header, parent, false)
        return DateHeaderViewHolder(view)
    }

    override fun getItemCount(): Int = uniqueDateList.size

    @SuppressLint("InflateParams")
    override fun onBindViewHolder(holder: DateHeaderViewHolder, position: Int) {

        holder.dateHeaderLinearLayout.removeAllViews()

        val punchCards = db.getPunchCardsByDate(uniqueDateList[position])

        if (punchCards.isEmpty())
        {
            holder.itemView.isVisible = false
            return
        }

        holder.dateHeaderTextView.text = uniqueDateList[position].format(dateFormatter).toString()

        Log.d("PunchCards in adapter", punchCards.toString())


        for (punchCard in punchCards)
        {
            val view = LayoutInflater.from(parentContext).inflate(R.layout.punch_card, null)
            view.findViewById<TextView>(R.id.punchInTextView).text = punchCard.punchIn.format(timeFormatter).toString()
            view.findViewById<TextView>(R.id.punchOutTextView).text = punchCard.punchOut!!.format(timeFormatter).toString()

            val durationInMinutes = punchCard.duration!!.toMinutes()
            val durationString = (durationInMinutes / 60).toString() + " hours, " + (durationInMinutes % 60).toString() + " min"
            view.findViewById<TextView>(R.id.punchDurationTextView).text = durationString

            view.findViewById<ImageView>(R.id.editPunchCardImageView).setOnClickListener {
                return@setOnClickListener
            }

            view.findViewById<ImageView>(R.id.deletePunchCardImageView).setOnClickListener {
                val builder = AlertDialog.Builder(parentContext)
                builder.setTitle("Confirm Delete")
                builder.setMessage("Do you want to delete this item?")
                builder.setPositiveButton("Yes") { _, _ ->
                    db.deleteItem(punchCard.id)
                    notifyItemChanged(position)
                    Toast.makeText(parentContext, "Event Deleted", Toast.LENGTH_SHORT).show()
                }
                builder.setNegativeButton("No") { _, _ ->
                    Toast.makeText(parentContext, "Cancelled", Toast.LENGTH_SHORT).show()
                }

                builder.show()
            }

            holder.dateHeaderLinearLayout.addView(view)
        }

    }
}