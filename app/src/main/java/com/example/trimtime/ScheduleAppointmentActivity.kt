package com.example.trimtime

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.trimtime.databinding.ActivityScheduleAppointmentBinding
import com.example.trimtime.model.User
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import java.io.File
import java.io.FileWriter
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*

class ScheduleAppointmentActivity : AppCompatActivity() {
    lateinit var binding: ActivityScheduleAppointmentBinding
    private var selectedDateTime: LocalDateTime? = null

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        // Initialize view binding
        binding = ActivityScheduleAppointmentBinding.inflate(layoutInflater)
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

        // Check and request necessary permissions
        checkPermissions()

        // Set listener for dateTime field
        binding.dateTime.setOnClickListener {
            showDateTimePicker()
        }

        // Set listener for register button
        binding.register.setOnClickListener {
            // Retrieve and trim input values
            val firstName = binding.firstName.text.toString().trim()
            val lastName = binding.lastName.text.toString().trim()
            val age = binding.age.text.toString().trim().toInt()
            val id = binding.id.text.toString().trim().toInt()

            // Format the selected date and time
            val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
            val appointmentTime = selectedDateTime?.format(formatter) ?: ""

            // Create a User object with the input values
            val user = User(id, firstName, lastName, age, appointmentTime)

            // Register the user if a date and time is selected
            selectedDateTime?.let { dateTime ->
                register(user, dateTime)
            }
        }
    }

    private fun checkPermissions() {
        // Check if storage permissions are granted
        if (checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED ||
            checkSelfPermission(android.Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            // Request storage permissions if not granted
            requestPermissions(arrayOf(android.Manifest.permission.WRITE_EXTERNAL_STORAGE, android.Manifest.permission.READ_EXTERNAL_STORAGE), 1)
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun showDateTimePicker() {
        // Get the current date and time
        val currentDateTime = Calendar.getInstance()
        // Show DatePickerDialog to select date
        DatePickerDialog(this, { _, year, month, dayOfMonth ->
            // Show TimePickerDialog to select time
            TimePickerDialog(this, { _, hourOfDay, minute ->
                // Set the selected date and time
                val selectedCalendar = Calendar.getInstance()
                selectedCalendar.set(year, month, dayOfMonth, hourOfDay, minute)
                selectedDateTime = LocalDateTime.of(year, month + 1, dayOfMonth, hourOfDay, minute)
                // Format the selected date and time and display it
                val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
                binding.dateTime.setText(selectedDateTime?.format(formatter))
            }, currentDateTime.get(Calendar.HOUR_OF_DAY), currentDateTime.get(Calendar.MINUTE), true).show()
        }, currentDateTime.get(Calendar.YEAR), currentDateTime.get(Calendar.MONTH), currentDateTime.get(Calendar.DAY_OF_MONTH)).show()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun register(user: User, dateTime: LocalDateTime) {
        // Format the date
        val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        val dateString = dateTime.format(dateFormatter)

        // Define the base directory for storing appointments
        val baseDir = File(getExternalFilesDir(null), "Appointments")
        val dateDir = File(baseDir, dateString)

        // Ensure the directory exists
        if (!dateDir.exists()) {
            val created = dateDir.mkdirs()
            if (!created) {
                Toast.makeText(this, "Failed to create directory: ${dateDir.absolutePath}", Toast.LENGTH_LONG).show()
                return
            }
        }

        // Create a JSON file for the user
        val jsonFile = File(dateDir, "${user.id}.json")
        val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
        val jsonAdapter = moshi.adapter(User::class.java)
        val json = jsonAdapter.toJson(user)
        try {
            // Write the JSON to the file
            FileWriter(jsonFile).use {
                it.write(json)
            }
            Toast.makeText(this, "Appointment registered successfully", Toast.LENGTH_LONG).show()
        } catch (e: Exception) {
            // Handle any errors that occur during file writing
            Toast.makeText(this, "Failed to write file: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }
}
