package com.dealzsaver

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.IOException

class LoginActivity : AppCompatActivity() {

    // Define the base URL of your Flask API
    private val baseUrl = "http://45.33.102.27:5000/login"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        // Get references to the EditText fields and buttons
        val usernameEditText = findViewById<EditText>(R.id.et_username)
        val passwordEditText = findViewById<EditText>(R.id.et_password)
        val loginButton = findViewById<Button>(R.id.btn_login)
        val signUpButton = findViewById<Button>(R.id.btn_sign_up)

        // Handle the Login button click
        loginButton.setOnClickListener {
            val username = usernameEditText.text.toString()
            val password = passwordEditText.text.toString()

            // Ensure non-empty fields
            if (username.isEmpty() || password.isEmpty()) {
                Toast.makeText(this@LoginActivity, "Please enter both username and password", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Run the login process in a background thread using coroutines
            CoroutineScope(Dispatchers.IO).launch {
                val isAuthenticated = loginUser(username, password)

                // Switch back to the main thread to update the UI
                withContext(Dispatchers.Main) {
                    if (isAuthenticated) {
                        // Login successful, navigate to ProfileActivity
                        val intent = Intent(this@LoginActivity, ProfileActivity::class.java)
                        intent.putExtra("username", username)  // Pass the username to the ProfileActivity
                        startActivity(intent)
                        finish() // Close the login activity
                    } else {
                        // Login failed, show a Toast message
                        Toast.makeText(this@LoginActivity, "Invalid credentials", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }

        // Handle the Sign Up button click
        signUpButton.setOnClickListener {
            // Navigate to the SignUpActivity
            val intent = Intent(this, SignUpActivity::class.java)
            startActivity(intent)
        }
    }

    /**
     * This function handles the login process by making an HTTP POST request to the Flask API.
     *
     * @param username The entered username.
     * @param password The entered password.
     * @return Boolean indicating whether the login was successful.
     */
    private suspend fun loginUser(username: String, password: String): Boolean {
        // Create an HTTP client to send requests
        val client = OkHttpClient()

        // Create a JSON object with the login data
        val json = JSONObject()
        json.put("username", username)
        json.put("password", password)

        // Prepare the request body as JSON
        val requestBody = json.toString().toRequestBody("application/json".toMediaTypeOrNull())

        // Create an HTTP POST request to the login endpoint
        val request = Request.Builder()
            .url(baseUrl)
            .post(requestBody)
            .build()

        return try {
            // Execute the request and get the response
            val response = client.newCall(request).execute()
            if (response.isSuccessful) {
                // The request was successful
                true
            } else {
                // The request failed
                false
            }
        } catch (e: IOException) {
            // There was an error with the request
            e.printStackTrace()
            false
        }
    }
}
