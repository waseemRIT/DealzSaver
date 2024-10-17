#!/bin/bash
# sign_up.sh - Handles new user creation and coupon generation for the user.

# Database credentials
DB_HOST="127.0.0.1"
DB_USER="root"
DB_PASSWORD="root"
DB_NAME="dealsaver"

# User-provided information (passed as arguments)
USERNAME=$1
EMAIL=$2
PASSWORD=$3

# Static coupon list
COUPONS=("10% Off at Amazon" "Free Burger at Burger King" "20% Discount on Zara Purchases" \
"Buy 1 Get 1 Free Pizza at Pizza Hut" "50% Off on Movie Tickets at AMC" \
"Free Delivery on UberEats Orders" "30% Discount on Nike Sportswear" \
"Free Coffee at Starbucks" "15% Off on Electronics at Best Buy" \
"Buy 2 Get 1 Free on Books at Barnes & Noble")

DESCRIPTIONS=("Use this coupon to get 10% off your next purchase at Amazon." \
"Enjoy a free burger at Burger King with this coupon!" \
"Get 20% off all Zara items for your next shopping spree!" \
"Order one pizza at Pizza Hut and get the second one for free!" \
"Get 50% off your next movie ticket at any AMC theater." \
"Use this coupon to get free delivery on your next UberEats order." \
"Save 30% on Nike sportswear items with this exclusive coupon." \
"Enjoy a free coffee at any Starbucks location with this coupon." \
"Use this coupon to get 15% off on all electronics at Best Buy." \
"Buy two books at Barnes & Noble and get one free with this coupon!")

# Function to generate unique coupon codes
generate_coupon_code() {
  echo $(openssl rand -hex 5) # Generate a 10-character coupon code
}

# Check if a coupon code already exists in the database
coupon_code_exists() {
  local COUPON_CODE=$1
  local EXISTS=$(mysql -h $DB_HOST -u $DB_USER -p$DB_PASSWORD -sse "SELECT COUNT(*) FROM coupons WHERE coupon_code='$COUPON_CODE';")
  if [ "$EXISTS" -gt 0 ]; then
    return 0 # Code exists
  else
    return 1 # Code does not exist
  fi
}

# Insert user into the users table
mysql -h $DB_HOST -u $DB_USER -p$DB_PASSWORD $DB_NAME <<EOF
INSERT INTO users (username, email, password) VALUES ('$USERNAME', '$EMAIL', '$PASSWORD');
EOF

# Get the new user ID (Assuming auto-incremented user ID)
USER_ID=$(mysql -h $DB_HOST -u $DB_USER -p$DB_PASSWORD $DB_NAME -sse "SELECT id FROM users WHERE email='$EMAIL';")

# Insert coupons for the new user
for i in "${!COUPONS[@]}"; do
  COUPON_CODE=$(generate_coupon_code)

  # Keep generating a new coupon code if it already exists in the database
  while coupon_code_exists $COUPON_CODE; do
    COUPON_CODE=$(generate_coupon_code)
  done

  TITLE=${COUPONS[$i]}
  DESCRIPTION=${DESCRIPTIONS[$i]}

  mysql -h $DB_HOST -u $DB_USER -p$DB_PASSWORD $DB_NAME <<EOF
  INSERT INTO coupons (user_id, title, description, coupon_code, is_valid)
  VALUES ('$USER_ID', '$TITLE', '$DESCRIPTION', '$COUPON_CODE', true);
EOF
done

echo "User $USERNAME created with 10 unique coupons!"
