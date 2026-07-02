package com.example.habittra.ui.addHabit

import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import com.example.habittra.R
import com.example.habittra.data.local.AppDatabase
import com.example.habittra.data.repository.HabitRepository

class AddHabitActivity : AppCompatActivity() {

    private var selectedColor = "#FFD700"

    // The 6 color options shown in the picker
    private val palette = listOf(
        "#FFD700", // yellow
        "#FF6B6B", // red
        "#4CAF50", // green
        "#2196F3", // blue
        "#FF9800", // orange
        "#9C27B0"  // purple
    )

    private lateinit var viewModel: AddHabitViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_habit)

        val repository = HabitRepository(AppDatabase.getInstance(this).habitDao())
        viewModel = ViewModelProvider(this, AddHabitViewModelFactory(repository))[AddHabitViewModel::class.java]

        setupColorPicker()

        findViewById<Button>(R.id.btnSave).setOnClickListener {
            val name = findViewById<EditText>(R.id.etHabitName).text.toString().trim()
            if (name.isEmpty()) {
                Toast.makeText(this, "Please enter a habit name", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            viewModel.addHabit(name, selectedColor)
            finish() // go back to HomeActivity; Room Flow auto-updates the list
        }
    }

    private fun setupColorPicker() {
        val row = findViewById<LinearLayout>(R.id.colorPickerRow)
        val dp  = resources.displayMetrics.density
        val size = (40 * dp).toInt()

        palette.forEach { hex ->
            val circle = View(this).apply {
                layoutParams = LinearLayout.LayoutParams(size, size).also {
                    it.marginEnd = (12 * dp).toInt()
                }
                background = GradientDrawable().apply {
                    shape = GradientDrawable.OVAL
                    setColor(Color.parseColor(hex))
                }
                setOnClickListener { selectColor(hex) }
            }
            row.addView(circle)
        }

        selectColor(palette[0]) // yellow selected by default
    }

    private fun selectColor(colorHex: String) {
        selectedColor = colorHex
        val row = findViewById<LinearLayout>(R.id.colorPickerRow)
        val dp  = resources.displayMetrics.density

        // Redraw all circles — selected one gets a white ring
        palette.forEachIndexed { i, hex ->
            val circle = row.getChildAt(i) ?: return@forEachIndexed
            circle.background = GradientDrawable().apply {
                shape = GradientDrawable.OVAL
                setColor(Color.parseColor(hex))
                if (hex == colorHex) {
                    setStroke((3 * dp).toInt(), Color.WHITE)
                }
            }
        }
    }
}