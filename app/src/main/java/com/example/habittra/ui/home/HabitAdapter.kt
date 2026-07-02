package com.example.habittra.ui.home

import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.HorizontalScrollView
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.habittra.R
import com.example.habittra.data.local.Habit
import com.example.habittra.utils.DateUtils

class HabitAdapter(
    private val onToggle: (habit: Habit, isDone: Boolean) -> Unit,
    private val onDelete: (habit: Habit) -> Unit
) : ListAdapter<Habit, HabitAdapter.HabitViewHolder>(DiffCallback()) {

    private var todayCompletions: Map<Int, Boolean> = emptyMap()
    private val heatmapCache    = mutableMapOf<Int, Set<String>>()
    private val streakCache     = mutableMapOf<Int, Int>()
    private val allDates        = com.example.habittra.utils.DateUtils.last365Days()


    fun updateCompletions(newCompletions: Map<Int, Boolean>) {
        todayCompletions = newCompletions
        notifyItemRangeChanged(0, itemCount)
    }

    fun updateHeatmapData(habitId: Int, doneDates: Set<String>) {
        heatmapCache[habitId] = doneDates
        currentList.indexOfFirst { it.id == habitId }
            .takeIf { it >= 0 }?.let { notifyItemChanged(it) }
    }

    fun updateStreak(habitId: Int, streak: Int) {
        streakCache[habitId] = streak
        currentList.indexOfFirst { it.id == habitId }
            .takeIf { it >= 0 }?.let { notifyItemChanged(it) }
    }

    inner class HabitViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val colorStrip   : View              = itemView.findViewById(R.id.colorStrip)
        val tvName       : TextView          = itemView.findViewById(R.id.tvHabitName)
        val btnToggle    : View              = itemView.findViewById(R.id.btnToggle)
        val btnDelete    : ImageButton       = itemView.findViewById(R.id.btnDelete)
        val heatmap      : HeatmapView       = itemView.findViewById(R.id.heatmapView)
        val heatmapScroll: HorizontalScrollView = itemView.findViewById(R.id.heatmapScroll)
        val tvStreak    : TextView          = itemView.findViewById(R.id.tvStreak)
        val tvActiveDays: TextView          = itemView.findViewById(R.id.tvActiveDays)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HabitViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_habit, parent, false)
        return HabitViewHolder(view)
    }

    override fun onBindViewHolder(holder: HabitViewHolder, position: Int) {
        val habit  = getItem(position)
        val isDone = todayCompletions[habit.id] == true
        val color  = runCatching { Color.parseColor(habit.colorHex) }.getOrDefault(Color.GRAY)
        val dp     = holder.itemView.resources.displayMetrics.density

        holder.tvName.text   = habit.name
        holder.tvStreak.text     = "🔥 : ${streakCache[habit.id] ?: 0} days"
        holder.tvActiveDays.text = "    ✅ : ${heatmapCache[habit.id]?.size ?: 0} days"

        // Color strip
        holder.colorStrip.background = GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            cornerRadius = 12f
            setColor(color)
        }

        // Toggle button
        holder.btnToggle.background = GradientDrawable().apply {
            shape = GradientDrawable.OVAL
            if (isDone) {
                setColor(color)
            } else {
                setColor(Color.TRANSPARENT)
                setStroke((2 * dp).toInt(), Color.parseColor("#555555"))
            }
        }
        holder.btnToggle.setOnClickListener { onToggle(habit, !isDone) }


        // Delete with confirmation dialog
        holder.btnDelete.setOnClickListener {
            android.app.AlertDialog.Builder(holder.itemView.context)
                .setTitle("Delete \"${habit.name}\"?")
                .setMessage("This will permanently delete the habit and all its history.")
                .setPositiveButton("Delete") { _, _ -> onDelete(habit) }
                .setNegativeButton("Cancel", null)
                .show()
        }

        // Heatmap
        holder.heatmap.habitColor = color
        holder.heatmap.allDates   = allDates
        holder.heatmap.doneDates  = heatmapCache[habit.id] ?: emptySet()

        // Auto-scroll to today (far right)
        holder.heatmapScroll.post {
            holder.heatmapScroll.fullScroll(HorizontalScrollView.FOCUS_RIGHT)
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<Habit>() {
        override fun areItemsTheSame(old: Habit, new: Habit) = old.id == new.id
        override fun areContentsTheSame(old: Habit, new: Habit) = old == new
    }
}