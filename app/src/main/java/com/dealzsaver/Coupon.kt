package com.dealzsaver

/**
 * Data class to represent a coupon object.
 *
 * This class holds the details of a coupon, including its title, description, coupon code,
 * and whether or not the coupon is still valid.
 *
 * @param title The title of the coupon.
 * @param description A short description of what the coupon offers.
 * @param couponCode The unique code associated with the coupon.
 * @param isValid Boolean indicating whether the coupon is still valid (true) or has been used (false).
 */
data class Coupon(
    val title: String,       // The title or name of the coupon
    val description: String, // A short description of the coupon's details
    val couponCode: String,  // The unique code associated with this coupon
    val isValid: Boolean     // True if the coupon is valid, false if it's already used
)
