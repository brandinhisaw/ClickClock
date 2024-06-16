package com.example.clickclock

import java.time.Duration
import java.time.LocalDate
import java.time.LocalTime

data class PunchCard(val id: Int, val date: LocalDate, val punchIn: LocalTime, var punchOut: LocalTime?, var duration: Duration?)
