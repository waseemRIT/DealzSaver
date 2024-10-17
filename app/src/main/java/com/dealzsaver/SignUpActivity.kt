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

class SignUpActivity : AppCompatActivity() {

    // Define the base URL of your Flask API
    private val baseUrl = "http://45.33.102.27:5000/signup"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)

        // Get references to the EditText fields and buttons
        val usernameEditText = findViewById<EditText>(R.id.et_username)
        val emailEditText = findViewById<EditText>(R.id.et_email)
        val passwordEditText = findViewById<EditText>(R.id.et_password)
        val confirmPasswordEditText = findViewById<EditText>(R.id.et_confirm_password)
        val signUpButton = findViewById<Button>(R.id.btn_sign_up)

        // Handle the sign-up button click
        signUpButton.setOnClickListener {
            val username = usernameEditText.text.toString()
            val email = emailEditText.text.toString()
            val password = passwordEditText.text.toString()
            val confirmPassword = confirmPasswordEditText.text.toString()

            // Check if the passwords match
            if (password == confirmPassword) {
                // Check if fields are not empty
                if (username.isNotEmpty() && email.isNotEmpty() && password.isNotEmpty()) {
                    // Run the sign-up process in a background thread using coroutines
                    CoroutineScope(Dispatchers.IO).launch {
                        val success = signUpUser(username, email, password)

                        // Switch back to the main thread to update the UI
                        withContext(Dispatchers.Main) {
                            if (success) {
                                Toast.makeText(this@SignUpActivity, "User registered successfully!", Toast.LENGTH_SHORT).show()
                                // Log to verify
                                println("Navigating to ProfileActivity with username: $username")
                                val intent = Intent(this@SignUpActivity, ProfileActivity::class.java)
                                intent.putExtra("username", username)  // Pass the username to the ProfileActivity
                                startActivity(intent)
                                finish() // Close the sign-up activity
                            } else {
                                Toast.makeText(this@SignUpActivity, "Sign-up failed", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                } else {
                    Toast.makeText(this, "Please fill out all fields", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show()
            }
        }
    }

    /**
     * This function handles the sign-up process by making an HTTP POST request to the Flask API.
     *
     * @param username The username entered by the user.
     * @param email The email entered by the user.
     * @param password The password entered by the user.
     * @return Boolean indicating whether the sign-up was successful.
     */
    private suspend fun signUpUser(username: String, email: String, password: String): Boolean {
        // Create an HTTP client to send requests
        val client = OkHttpClient()

        // Create a JSON object with the user data
        val json = JSONObject()
        json.put("username", username)
        json.put("email", email)
        json.put("password", password)

        // Prepare the request body as JSON
        val requestBody = json.toString().toRequestBody("application/json".toMediaTypeOrNull())

        // Create an HTTP POST request to the sign-up endpoint
        val request = Request.Builder()
            .url(baseUrl)
            .post(requestBody)
            .build()

        return try {
            // Execute the request and get the response
            val response = client.newCall(request).execute()

            // Log the response for debugging
            val responseBody = response.body?.string()
            println("API Response: $responseBody")

            if (response.isSuccessful) {
                // The request was successful
                true
            } else {
                // The request failed, log the failure
                println("Failed response: ${response.message}")
                false
            }
        } catch (e: IOException) {
            // There was an error with the request, log the error
            e.printStackTrace()
            false
        }
    }
}
