package com.example.habittra.ui.home

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.Gravity
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.content.res.AppCompatResources.getDrawable
import androidx.core.content.res.ResourcesCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.habittra.R
import com.example.habittra.data.local.AppDatabase
import com.example.habittra.data.repository.HabitRepository
import com.example.habittra.ui.addHabit.AddHabitActivity
import com.example.habittra.utils.DateUtils
import kotlinx.coroutines.launch
import kotlin.jvm.java

class HomeActivity : AppCompatActivity() {

    private lateinit var viewModel: HomeViewModel
    private lateinit var adapter: HabitAdapter
    private lateinit var tvDate: TextView

    private val dayCircles = mutableListOf<TextView>()
    private val weekDates  = DateUtils.currentWeekDates()   // stable list for whole session
    private val today      = DateUtils.todayString()
    // Change list type from TextView to DayProgressView
    private val dayViews = mutableListOf<DayProgressView>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        val repository = HabitRepository(AppDatabase.getInstance(this).habitDao())
        viewModel = ViewModelProvider(this, HomeViewModelFactory(repository))[HomeViewModel::class.java]
        tvDate = findViewById(R.id.tvDate)

        adapter = HabitAdapter(
            onToggle = { habit, isDone -> viewModel.toggleHabit(habit.id, isDone) },
            onDelete = { habit -> viewModel.deleteHabit(habit) }
        )

        findViewById<RecyclerView>(R.id.rvHabits).apply {
            layoutManager = LinearLayoutManager(this@HomeActivity)
            adapter = this@HomeActivity.adapter
        }

        setupWeekStrip()
        updateDateHeader(today)

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {

                // Observe selected date changes → update header + week strip highlight
                launch {
                    viewModel.selectedDate.collect { dateStr ->
                        updateDateHeader(dateStr)
                        refreshWeekStripHighlight(dateStr)
                    }
                }

                // Collect week completion rates → update progress rings
                launch {
                    viewModel.weekRates.collect { rates ->
                        val currentSelected = viewModel.selectedDate.value
                        refreshWeekStripHighlight(currentSelected, rates)
                    }
                }

                // Also update rings when selected date changes
                launch {
                    viewModel.selectedDate.collect { dateStr ->
                        updateDateHeader(dateStr)
                        refreshWeekStripHighlight(dateStr, viewModel.weekRates.value)
                    }
                }

                // Observe main UI state (habits list + today's completions)
                launch {
                    viewModel.uiState.collect { state ->
                        adapter.submitList(state.habits)
                        adapter.updateCompletions(state.todayCompletions)

                        // For each habit, wire up its heatmap + streak as separate flows
                        state.habits.forEach { habit ->
                            launch {
                                viewModel.getDoneDates(habit.id).collect { doneDates ->
                                    adapter.updateHeatmapData(habit.id, doneDates)
                                }
                            }
                            launch {
                                viewModel.getStreakForHabit(habit.id).collect { streak ->
                                    adapter.updateStreak(habit.id, streak)
                                }
                            }
                        }
                    }
                }
            }
        }

        findViewById<ImageButton>(R.id.btnAddHabit).setOnClickListener {
            startActivity(Intent(this, AddHabitActivity::class.java))
        }
    }

    // ---------- Week strip ----------


    private fun setupWeekStrip() {
        val weekStrip = findViewById<LinearLayout>(R.id.weekStrip)
        val dp = resources.displayMetrics.density
        dayViews.clear()
        weekStrip.removeAllViews()  // clear if called again

        weekDates.forEach { (dayName, _, dateStr) ->
            val column = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                gravity = Gravity.CENTER
                layoutParams = LinearLayout.LayoutParams(
                    0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
                isClickable = true
                isFocusable = true
                setOnClickListener { viewModel.selectDate(dateStr) }
            }

            val label = TextView(this).apply {
                text = dayName
                textSize = 11f
                typeface = ResourcesCompat.getFont(this@HomeActivity, R.font.montserrat_semibold)
                setTextColor(Color.GRAY)
                gravity = Gravity.CENTER
            }

            val size = (44 * dp).toInt()
            val dayView = DayProgressView(this).apply {
                layoutParams = LinearLayout.LayoutParams(size, size).also {
                    it.topMargin = (4 * dp).toInt()
                }
                ringColor = Color.WHITE
            }
            dayViews.add(dayView)

            column.addView(label)
            column.addView(dayView)
            weekStrip.addView(column)
        }
    }

    private fun refreshWeekStripHighlight(selectedDate: String, rates: Map<String, Float> = emptyMap()) {
        weekDates.forEachIndexed { i, (_, dayNum, dateStr) ->
            val view = dayViews.getOrNull(i) ?: return@forEachIndexed
            view.dayNumber  = dayNum
            view.isToday    = dateStr == today
            view.isDaySelected = dateStr == selectedDate
            view.progress   = rates[dateStr] ?: 0f
            view.ringColor  = Color.WHITE
        }
    }

    // ---------- Date header ----------

    private fun updateDateHeader(dateStr: String) {
        tvDate.text = DateUtils.toDisplayString(dateStr)
    }
}