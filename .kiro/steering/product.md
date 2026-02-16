---
inclusion: always
---

# Foodshare Product Overview

Foodshare is an enterprise-grade iOS food sharing platform that connects people with surplus food to those who need it, reducing waste and building community. The app is inspired by Olio's food-sharing model with Airbnb's premium design aesthetic.

## Core Features

- **Food Listings**: Users can create, browse, search, and claim food listings with photos, descriptions, categories, locations, and expiry dates
- **Real-Time Chat**: In-app messaging for coordinating pickup details between donors and recipients
- **Location-Based Discovery**: Find food listings nearby using geospatial queries with map visualization
- **User Profiles & Ratings**: Build trust through ratings, reviews, and reputation tracking
- **Authentication**: Secure signup/login with email/password and OAuth (Apple, Google)

## Listing Categories

The iOS app supports 12 listing categories matching the web platform (using singular raw values to match database `post_type` column):

| Category | Description |
|----------|-------------|
| `food` | Surplus food items |
| `thing` | Non-food items to share |
| `borrow` | Items available for borrowing |
| `wanted` | Items users are looking for |
| `foodbank` | Food banks |
| `fridge` | Community fridges |
| `zerowaste` | Zero waste initiatives |
| `vegan` | Vegan-specific listings |
| `organisation` | Businesses, charities, community groups |
| `volunteer` | Volunteer opportunities |
| `challenge` | Community challenges |
| `forum` | Community discussions |

Categories are grouped for different contexts:
- **Feed filters**: food, thing, borrow, wanted, foodbank, fridge, zerowaste, vegan
- **Creatable**: food, thing, borrow, wanted, zerowaste, vegan
- **Community resources**: foodbank, fridge, organisation, volunteer

See `Core/Models/ListingCategory.swift` for the full implementation.

## Target Users

- **Food Donors**: Environmentally conscious individuals, families with excess groceries, small businesses
- **Food Recipients**: Budget-conscious families, students, community-minded people
- **Community Organizations**: Food banks, community centers, nonprofit food rescue groups

## Design Philosophy

The app features a "Liquid Glass v26" design system with glassmorphism aesthetics:
- Frosted glass effects with semi-transparent backgrounds
- Premium, modern UI inspired by Airbnb
- Native iOS materials (.ultraThinMaterial, .thinMaterial)
- Clean, minimal interface emphasizing food photography

## Success Metrics

- Claim Success Rate: >80% of listings successfully transferred
- User Retention (30-day): >40%
- Average Rating: >4.5 stars
- App Store Rating: >4.7 stars

## Platform

- iOS 17.0+ (iPhone and iPad)
- Swift 6.2 with SwiftUI
- Supabase backend with PostgreSQL, PostGIS, and Edge Functions
