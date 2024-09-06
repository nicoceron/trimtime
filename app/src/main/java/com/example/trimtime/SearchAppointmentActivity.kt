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
import com.google.firebase.database.*
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class SearchAppointmentActivity : AppCompatActivity() {
    lateinit var binding: ActivitySearchAppointmentBinding
    private lateinit var database: DatabaseReference

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        // Initialize view binding
        binding = ActivitySearchAppointmentBinding.inflate(layoutInflater)
        val view = binding.root
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(view)

        // Initialize Firebase Database reference
        database = FirebaseDatabase.getInstance().reference.child("appointments")

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
        // Fetch the available dates from Firebase
        database.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val dateList = mutableListOf<String>()
                // Get all date nodes
                snapshot.children.forEach { dateSnapshot ->
                    dateList.add(dateSnapshot.key ?: "")
                }
                // Populate spinner with dates
                val adapter = ArrayAdapter(this@SearchAppointmentActivity, android.R.layout.simple_spinner_item, dateList)
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                binding.datePicker.adapter = adapter
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle potential errors
            }
        })
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun fetchAppointments(date: String) {
        // Fetch appointments for the selected date from Firebase
        val dateRef = database.child(date)
        dateRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                // Clear existing rows in the table except the header
                binding.table.removeViews(1, binding.table.childCount - 1)

                // Loop through each child under the selected date
                snapshot.children.forEach { appointmentSnapshot ->
                    // Parse each child as a User object
                    val user = appointmentSnapshot.getValue(User::class.java)
                    user?.let {
                        // Add a row for each appointment
                        addTableRow(it)
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle potential errors
            }
        })
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
