package com.example.clickclock

import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.clickclock.databinding.ActivityLogViewBinding
import java.time.LocalDate

@RequiresApi(Build.VERSION_CODES.O)
class LogViewActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLogViewBinding
    private lateinit var db: PunchCardDatabaseHelper
    private lateinit var punchCardLogAdapter: PunchCardLogAdapter
    private lateinit var punchCardRecyclerView: View

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityLogViewBinding.inflate(layoutInflater)
        setContentView(binding.root)

        db = PunchCardDatabaseHelper(this)

        binding.closeLogButton.setOnClickListener{
            finish()
        }

        val punchCards = db.getAllPunchCards()

        if (punchCards.isEmpty())
        {
            return
        }

        // go through all punchCards for unique dates to pass later to adapter
        val uniqueDateList = mutableListOf<LocalDate>()
        for (punchCard in punchCards)
        {
            // skip dates we already added
            if (!uniqueDateList.contains(punchCard.date))
            {
                uniqueDateList.add(punchCard.date)
            }
        }

        punchCardRecyclerView = findViewById(R.id.punchCardsRecyclerView)

        punchCardLogAdapter = PunchCardLogAdapter(uniqueDateList, this)

        binding.punchCardsRecyclerView.layoutManager = LinearLayoutManager(this)
        binding.punchCardsRecyclerView.adapter = punchCardLogAdapter
    }
}