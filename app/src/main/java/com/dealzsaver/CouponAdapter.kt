package com.dealzsaver

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

/**
 * Adapter class to bind the list of coupons to the RecyclerView in the ProfileActivity.
 *
 * @param coupons The list of Coupon objects to display in the RecyclerView.
 * @param onMarkAsUsedClick Lambda function that handles what happens when "Mark as Used" is clicked.
 */
class CouponAdapter(
    private val coupons: List<Coupon>, // List of coupons to display
    private val onMarkAsUsedClick: (String) -> Unit // Lambda function to handle the "Mark as Used" button click
) : RecyclerView.Adapter<CouponAdapter.CouponViewHolder>() {

    /**
     * ViewHolder class to hold references to the views in each item.
     * This helps improve performance by avoiding repeated calls to findViewById.
     */
    class CouponViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val titleTextView: TextView = itemView.findViewById(R.id.tv_coupon_title) // Reference to the TextView for the coupon title
        val descriptionTextView: TextView = itemView.findViewById(R.id.tv_coupon_description) // Reference to the TextView for the coupon description
        val markAsUsedButton: Button = itemView.findViewById(R.id.btn_mark_as_used) // Reference to the "Mark as Used" button
    }

    /**
     * This method is called when the RecyclerView needs a new ViewHolder to represent a coupon.
     * It inflates the item layout (item_coupon.xml) and returns the ViewHolder.
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CouponViewHolder {
        // Inflate the item layout for each coupon item
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_coupon, parent, false)
        return CouponViewHolder(itemView) // Return the ViewHolder
    }

    /**
     * This method binds data to the views in the ViewHolder.
     * It sets the coupon details (title, description, and validity) in each item.
     */
    override fun onBindViewHolder(holder: CouponViewHolder, position: Int) {
        // Get the current coupon from the list
        val coupon = coupons[position]

        // Bind the coupon title and description to the respective TextViews
        holder.titleTextView.text = coupon.title
        holder.descriptionTextView.text = coupon.description

        // Set the button text and state based on the coupon's validity
        if (coupon.isValid) {
            // If the coupon is valid, enable the button and set the text to "Mark as Used"
            holder.markAsUsedButton.text = "Mark as Used"
            holder.markAsUsedButton.isEnabled = true
        } else {
            // If the coupon has already been used, disable the button and set the text to "Already Used"
            holder.markAsUsedButton.text = "Already Used"
            holder.markAsUsedButton.isEnabled = false
        }

        // Set a click listener on the "Mark as Used" button to handle when the user clicks it
        holder.markAsUsedButton.setOnClickListener {
            // Call the onMarkAsUsedClick lambda function with the coupon code when the button is clicked
            onMarkAsUsedClick(coupon.couponCode)
        }
    }

    /**
     * This method returns the total number of items (coupons) in the list.
     * It is used by the RecyclerView to know how many items to display.
     */
    override fun getItemCount(): Int {
        return coupons.size // Return the size of the coupon list
    }
}
