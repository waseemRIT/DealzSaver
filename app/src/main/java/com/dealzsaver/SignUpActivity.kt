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

    // Defining the base URL of Flask API
    private val baseUrl = "http://45.33.102.27:5000/signup"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)

        // Geting references to the EditText fields and buttons
        val usernameEditText = findViewById<EditText>(R.id.et_username)
        val emailEditText = findViewById<EditText>(R.id.et_email)
        val passwordEditText = findViewById<EditText>(R.id.et_password)
        val confirmPasswordEditText = findViewById<EditText>(R.id.et_confirm_password)
        val signUpButton = findViewById<Button>(R.id.btn_sign_up)

        // Handling the sign-up button click
        signUpButton.setOnClickListener {
            val username = usernameEditText.text.toString()
            val email = emailEditText.text.toString()
            val password = passwordEditText.text.toString()
            val confirmPassword = confirmPasswordEditText.text.toString()

            // Check if the passwords match
            if (password == confirmPassword) {
                // Check if fields are not empty
                if (username.isNotEmpty() && email.isNotEmpty() && password.isNotEmpty()) {
                    // Launching a new coroutine in the IO context, which is suitable for offloading tasks that involve blocking operations,
                    // such as network requests, to avoid blocking the main thread (UI thread).
                    // And since we are sending http request it would cause a block
                    CoroutineScope(Dispatchers.IO).launch {
                        // Calling the signUpUser function, which handles the HTTP request to register the user.
                        // The function returns a Boolean indicating whether the sign-up was successful.
                        val success = signUpUser(username, email, password)

                        // Switch back to the main thread (UI thread) to update the UI components, since UI updates can only be done on the main thread.
                        withContext(Dispatchers.Main) {
                            // Checking the result of the sign-up process
                            if (success) {
                                // If the sign-up was successful, show a success message using a Toast
                                // Toast: A small popup message that is used to provide feedback to the user.
                                Toast.makeText(this@SignUpActivity, "User registered successfully!", Toast.LENGTH_SHORT).show()
                                // Log the successful navigation for debugging purposes
                                println("Navigating to ProfileActivity with username: $username")
                                // Creating an Intent to start the ProfileActivity
                                // Intent: An object used to start another activity (in this case, ProfileActivity) and pass data between activities.
                                val intent = Intent(this@SignUpActivity, ProfileActivity::class.java)
                                // Passing the username to the ProfileActivity so it can personalize the greeting
                                intent.putExtra("username", username)
                                // Starting the ProfileActivity
                                startActivity(intent)
                                // Closing the SignUpActivity to remove it from the back stack
                                finish()
                            } else {     // If the sign-up failed, show an error message using a Toast
                                Toast.makeText(this@SignUpActivity, "Sign-up failed", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                } else { // If any of the fields are empty, show a Toast message prompting the user to fill out all fields
                    Toast.makeText(this, "Please fill out all fields", Toast.LENGTH_SHORT).show()
                }
            } else { // If the passwords do not match, show a Toast message informing the user
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
