package com.dealzsaver

import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.*
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.IOException

class ProfileActivity : AppCompatActivity() {

    // Define the Flask API base URL for updating coupon status
    private val baseUrl = "http://45.33.102.27:5000/update_coupon"

    // Declare RecyclerView and adapter to display the coupons
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: CouponAdapter
    private val coupons = mutableListOf<Coupon>() // List to store fetched coupons

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile) // Set the layout to activity_profile.xml

        // Reference to the RecyclerView in the layout
        recyclerView = findViewById(R.id.recyclerViewCoupons)

        // Reference to the greeting TextView in the layout
        val greetingTextView = findViewById<TextView>(R.id.tv_greeting)

        // Set up the RecyclerView with a layout manager and an empty adapter initially
        recyclerView.layoutManager = LinearLayoutManager(this)

        // Initialize the CouponAdapter with an empty list of coupons and define what happens when the "Mark as Used" button is clicked
        adapter = CouponAdapter(coupons) { couponCode ->
            markCouponAsUsed(couponCode) // Call the function to mark the coupon as used
        }
        recyclerView.adapter = adapter // Set the adapter to the RecyclerView

        // Get the username passed from the previous activity (LoginActivity or SignUpActivity)
        val username = intent.getStringExtra("username") ?: "Unknown User"

        // Display a greeting with the username in the greeting TextView
        greetingTextView.text = "Hi, $username!"

        // Fetch coupons for the user from the database through the API
        CoroutineScope(Dispatchers.IO).launch {
            val fetchedCoupons = fetchCouponsFromDatabase(username)

            withContext(Dispatchers.Main) {
                if (fetchedCoupons.isNotEmpty()) {
                    // If coupons are fetched successfully, update the coupons list and notify the adapter
                    coupons.clear()
                    coupons.addAll(fetchedCoupons)
                    adapter.notifyDataSetChanged() // Notify the adapter to refresh the view
                } else {
                    // If no coupons are available, show a message to the user
                    Toast.makeText(this@ProfileActivity, "No coupons available.", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    /**
     * This function fetches the user's coupons from the database through the Flask API.
     * It makes an HTTP GET request to retrieve the coupons for the given username.
     *
     * @param username The username of the logged-in user whose coupons will be fetched.
     * @return List of Coupon objects associated with the user.
     */
    private suspend fun fetchCouponsFromDatabase(username: String): List<Coupon> {
        val coupons = mutableListOf<Coupon>()
        val client = OkHttpClient()

        // Prepare the API request to fetch coupons
        val request = Request.Builder()
            .url("http://45.33.102.27:5000/get_coupons?username=$username")
            .build()

        return try {
            // Execute the request and get the response
            client.newCall(request).execute().use { response ->  // Ensures the response body is closed
                if (response.isSuccessful) {
                    val responseBody = response.body?.string()
                    val jsonResponse = JSONObject(responseBody)
                    val couponArray = jsonResponse.getJSONArray("coupons")

                    // Loop through the coupons and add them to the list
                    for (i in 0 until couponArray.length()) {
                        val couponJson = couponArray.getJSONObject(i)
                        val title = couponJson.getString("title")
                        val description = couponJson.getString("description")
                        val couponCode = couponJson.getString("coupon_code")
                        val isValid = couponJson.getInt("is_valid") == 1 // Convert 1/0 to true/false

                        coupons.add(Coupon(title, description, couponCode, isValid))
                    }
                }
            }
            coupons // Return the fetched coupons
        } catch (e: IOException) {
            e.printStackTrace()
            coupons // Return an empty list in case of error
        }
    }

    /**
     * This function sends an HTTP POST request to the API to mark a coupon as used.
     *
     * @param couponCode The coupon code to be marked as used.
     */
    private fun markCouponAsUsed(couponCode: String) {
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
                        refreshCoupons() // Refresh the coupons after marking as used
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

    /**
     * This function refreshes the coupons list after a coupon is marked as used.
     * It fetches the updated list of coupons and updates the RecyclerView.
     */
    private fun refreshCoupons() {
        // Get the username again to fetch the updated list of coupons
        val username = intent.getStringExtra("username") ?: return

        // Fetch the updated coupons list in the background
        CoroutineScope(Dispatchers.IO).launch {
            val updatedCoupons = fetchCouponsFromDatabase(username)
            withContext(Dispatchers.Main) {
                // Update the RecyclerView adapter with the new list of coupons
                coupons.clear()
                coupons.addAll(updatedCoupons)
                adapter.notifyDataSetChanged() // Notify the adapter to refresh the view
            }
        }
    }
}
