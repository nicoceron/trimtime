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
        // Initialize view binding
        binding = ActivitySearchAppointmentBinding.inflate(layoutInflater)
        val view = binding.root
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(view)

        // Set window insets listener to manage system bars
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Populate the date spinner with available appointment dates
        populateDateSpinner()

        // Set listener for the search button
        binding.button.setOnClickListener {
            // Get the selected date from the spinner
            val selectedDate = binding.datePicker.selectedItem.toString()
            // Fetch and display appointments for the selected date
            fetchAppointments(selectedDate)
        }
    }

    private fun populateDateSpinner() {
        // Get the base directory for appointments
        val baseDir = File(getExternalFilesDir(null), "Appointments")
        // List the directories (dates) in the base directory
        val dateDirs = baseDir.listFiles()?.filter { it.isDirectory }?.map { it.name } ?: listOf()

        // Create an ArrayAdapter for the spinner
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, dateDirs)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        // Set the adapter for the spinner
        binding.datePicker.adapter = adapter
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun fetchAppointments(date: String) {
        // Get the directory for the selected date
        val baseDir = File(getExternalFilesDir(null), "Appointments")
        val dateDir = File(baseDir, date)
        // List the appointment files in the directory
        val appointmentFiles = dateDir.listFiles { _, name -> name.endsWith(".json") } ?: arrayOf()

        // Initialize Moshi JSON adapter
        val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
        val jsonAdapter = moshi.adapter(User::class.java)

        // Clear existing rows in the table except the header
        binding.table.removeViews(1, binding.table.childCount - 1)

        // Loop through each appointment file
        for (file in appointmentFiles) {
            // Read the JSON from the file
            val json = file.readText()
            // Parse the JSON to a User object
            val user = jsonAdapter.fromJson(json)

            // If the user is not null, add a row to the table
            user?.let {
                addTableRow(it)
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun addTableRow(user: User) {
        // Create a new TableRow
        val row = TableRow(this)
        // Add user ID to the row
        row.addView(createTextView(user.id.toString()))
        // Add user name to the row
        row.addView(createTextView("${user.firstName} ${user.lastName}"))

        // Format the appointment time to display only hour and minute
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
        val dateTime = LocalDateTime.parse(user.appointmentTime, formatter)
        val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")
        val timeText = dateTime.format(timeFormatter)

        // Add formatted time to the row
        row.addView(createTextView(timeText))
        // Add the row to the table
        binding.table.addView(row)
    }

    private fun createTextView(text: String): TextView {
        // Create a new TextView with the given text
        val textView = TextView(this)
        textView.text = text
        textView.textAlignment = TextView.TEXT_ALIGNMENT_CENTER
        textView.setPadding(8, 8, 8, 8)
        return textView
    }
}
