package com.example.trimtime

import android.os.Build
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.TableRow
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.trimtime.databinding.ActivitySearchAppointmentBinding
import com.example.trimtime.model.User
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import java.io.File
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class SearchAppointmentActivity : AppCompatActivity() {
    lateinit var binding: ActivitySearchAppointmentBinding

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivitySearchAppointmentBinding.inflate(layoutInflater)
        val view = binding.root
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(view)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        populateDateSpinner()

        binding.button.setOnClickListener {
            val selectedDate = binding.datePicker.selectedItem.toString()
            fetchAppointments(selectedDate)
        }
    }

    private fun populateDateSpinner() {
        val baseDir = File(getExternalFilesDir(null), "Appointments")
        val dateDirs = baseDir.listFiles()?.filter { it.isDirectory }?.map { it.name } ?: listOf()

        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, dateDirs)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.datePicker.adapter = adapter
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun fetchAppointments(date: String) {
        val baseDir = File(getExternalFilesDir(null), "Appointments")
        val dateDir = File(baseDir, date)
        val appointmentFiles = dateDir.listFiles { _, name -> name.endsWith(".json") } ?: arrayOf()

        val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
        val jsonAdapter = moshi.adapter(User::class.java)

        binding.table.removeViews(1, binding.table.childCount - 1) // Clear existing rows except the header

        for (file in appointmentFiles) {
            val json = file.readText()
            val user = jsonAdapter.fromJson(json)

            user?.let {
                addTableRow(it)
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun addTableRow(user: User) {
        val row = TableRow(this)
        row.addView(createTextView(user.id.toString()))
        row.addView(createTextView("${user.firstName} ${user.lastName}"))

        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
        val dateTime = LocalDateTime.parse(user.appointmentTime, formatter)
        val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")
        val timeText = dateTime.format(timeFormatter)

        row.addView(createTextView(timeText)) // Display only hour and minute
        binding.table.addView(row)
    }

    private fun createTextView(text: String): TextView {
        val textView = TextView(this)
        textView.text = text
        textView.textAlignment = TextView.TEXT_ALIGNMENT_CENTER
        textView.setPadding(8, 8, 8, 8)
        return textView
    }
}
