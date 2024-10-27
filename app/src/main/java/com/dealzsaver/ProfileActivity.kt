package com.dealzsaver

import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.*
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.IOException

class ProfileActivity : AppCompatActivity() {

    // Define the Flask API base URL for updating coupon status
    private val baseUrl = "http://45.33.102.27:5000/update_coupon"

    // Declare TextViews for displaying coupon details
    private lateinit var coupon1TitleTextView: TextView
    private lateinit var coupon1DescriptionTextView: TextView
    private lateinit var coupon1UsedButton: Button

    private lateinit var coupon2TitleTextView: TextView
    private lateinit var coupon2DescriptionTextView: TextView
    private lateinit var coupon2UsedButton: Button

    private lateinit var coupon3TitleTextView: TextView
    private lateinit var coupon3DescriptionTextView: TextView
    private lateinit var coupon3UsedButton: Button

    // Coupon codes for tracking
    private var coupon1Code: String = ""
    private var coupon2Code: String = ""
    private var coupon3Code: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile) // Set the layout to activity_profile.xml

        // Initialize TextViews and Buttons from the layout
        coupon1TitleTextView = findViewById(R.id.tv_coupon1_title)
        coupon1DescriptionTextView = findViewById(R.id.tv_coupon1_description)
        coupon1UsedButton = findViewById(R.id.btn_coupon1_used)

        coupon2TitleTextView = findViewById(R.id.tv_coupon2_title)
        coupon2DescriptionTextView = findViewById(R.id.tv_coupon2_description)
        coupon2UsedButton = findViewById(R.id.btn_coupon2_used)

        coupon3TitleTextView = findViewById(R.id.tv_coupon3_title)
        coupon3DescriptionTextView = findViewById(R.id.tv_coupon3_description)
        coupon3UsedButton = findViewById(R.id.btn_coupon3_used)

        // Get the username passed from the previous activity (LoginActivity or SignUpActivity)
        val username = intent.getStringExtra("username") ?: "Unknown User"
        findViewById<TextView>(R.id.tv_greeting).text = "Hi, $username!" // Update the greeting TextView

        // Fetch coupon data for the user from the server
        CoroutineScope(Dispatchers.IO).launch {
            fetchCoupons(username)
        }

        // Set onClickListeners for the "Mark as Used" buttons
        coupon1UsedButton.setOnClickListener { markCouponAsUsed(coupon1Code, coupon1UsedButton) }
        coupon2UsedButton.setOnClickListener { markCouponAsUsed(coupon2Code, coupon2UsedButton) }
        coupon3UsedButton.setOnClickListener { markCouponAsUsed(coupon3Code, coupon3UsedButton) }
    }

    /**
     * Fetches coupon data from the server and updates the TextViews.
     *
     * @param username The username of the logged-in user.
     */
    private suspend fun fetchCoupons(username: String) {
        val client = OkHttpClient()

        // Prepare the API request to fetch coupons
        val request = Request.Builder()
            .url("http://45.33.102.27:5000/get_coupons?username=$username")
            .build()

        try {
            // Execute the request and get the response
            val response = client.newCall(request).execute()
            if (response.isSuccessful) {
                val responseBody = response.body?.string()
                Log.d("ProfileActivity", "Response Body: $responseBody") // Log the response body for debugging
                val jsonResponse = JSONObject(responseBody)
                val couponArray = jsonResponse.getJSONArray("coupons")

                // Assuming three static coupons for simplicity
                if (couponArray.length() > 0) {
                    // Update the first coupon details
                    val coupon1Json = couponArray.getJSONObject(0)
                    updateCouponUI(coupon1Json, coupon1TitleTextView, coupon1DescriptionTextView, coupon1UsedButton)

                    // Update the second coupon details
                    val coupon2Json = couponArray.getJSONObject(1)
                    updateCouponUI(coupon2Json, coupon2TitleTextView, coupon2DescriptionTextView, coupon2UsedButton)

                    // Update the third coupon details
                    val coupon3Json = couponArray.getJSONObject(2)
                    updateCouponUI(coupon3Json, coupon3TitleTextView, coupon3DescriptionTextView, coupon3UsedButton)
                } else {
                    // Show a message if no coupons are available
                    withContext(Dispatchers.Main) {
                        Toast.makeText(this@ProfileActivity, "No coupons available.", Toast.LENGTH_SHORT).show()
                    }
                }
            } else {
                // Handle unsuccessful response
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@ProfileActivity, "Error fetching coupons", Toast.LENGTH_SHORT).show()
                }
            }
        } catch (e: IOException) {
            e.printStackTrace()
            withContext(Dispatchers.Main) {
                Toast.makeText(this@ProfileActivity, "Error fetching coupons", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun updateCouponUI(couponJson: JSONObject, titleTextView: TextView, descriptionTextView: TextView, button: Button) {
        // Update the UI elements with coupon details
        titleTextView.text = couponJson.getString("title")
        descriptionTextView.text = couponJson.getString("description")
        val isValid = couponJson.getInt("is_valid")

        // Check if the coupon is valid and update the button accordingly
        if (isValid == 1) {
            runOnUiThread { // Ensure UI updates are performed on the UI thread
                button.text = "Mark as Used"
                button.isEnabled = true
                button.setBackgroundColor(resources.getColor(R.color.ocean_blue)) // Change to your button color
            }
        } else {
            runOnUiThread { // Ensure UI updates are performed on the UI thread
                button.text = "Already Used"
                button.isEnabled = false
                button.setBackgroundColor(resources.getColor(R.color.gray)) // Assuming you have a gray color defined
            }
        }

        // Store coupon code for tracking
        when (titleTextView.id) {
            R.id.tv_coupon1_title -> coupon1Code = couponJson.getString("coupon_code")
            R.id.tv_coupon2_title -> coupon2Code = couponJson.getString("coupon_code")
            R.id.tv_coupon3_title -> coupon3Code = couponJson.getString("coupon_code")
        }
    }

    /**
     * Marks a coupon as used by sending a request to the server.
     *
     * @param couponCode The code of the coupon to be marked as used.
     * @param button The button associated with the coupon being marked as used.
     */
    private fun markCouponAsUsed(couponCode: String, button: Button) {
        CoroutineScope(Dispatchers.IO).launch {
            val client = OkHttpClient()

            // Create a JSON object with the coupon code to mark it as used
            val json = JSONObject()
            json.put("coupon_code", couponCode)

            // Prepare the request body as JSON
            val requestBody = json.toString().toRequestBody("application/json".toMediaTypeOrNull())

            // Create a POST request to update the coupon validity
            val request = Request.Builder()
                .url(baseUrl)
                .post(requestBody)
                .build()

            try {
                // Execute the request and handle the response
                val response = client.newCall(request).execute()
                withContext(Dispatchers.Main) {
                    if (response.isSuccessful) {
                        Toast.makeText(this@ProfileActivity, "Coupon marked as used!", Toast.LENGTH_SHORT).show()
                        // Change the button text and state after marking as used
                        button.text = "Already Used"
                        button.isEnabled = false
                        button.setBackgroundColor(resources.getColor(R.color.gray)) // Assuming you have a gray color defined
                    } else {
                        Toast.makeText(this@ProfileActivity, "Failed to mark coupon as used", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: IOException) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@ProfileActivity, "Error updating coupon", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}
