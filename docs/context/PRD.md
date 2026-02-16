# Product Requirements Document: Foodshare iOS App

## Document Information

- **Version**: 1.1
- **Last Updated**: February 16, 2026
- **Status**: Active
- **Author**: Foodshare Team
- **Stakeholders**: Product, Engineering, Design, Business Development

---

## Executive Summary

Foodshare is an iOS mobile application that connects people who have surplus food with those who need it, reducing food waste while building community connections. Inspired by Olio's food-sharing model with Airbnb's premium design aesthetic, Foodshare provides a beautiful, intuitive platform for listing, discovering, and claiming free food items in your local area.

The app features a modern glassmorphism design system ("Liquid Glass v26"), real-time chat functionality, location-based discovery, and enterprise-grade architecture built on Supabase backend infrastructure. Our mission is to make food sharing as seamless and delightful as booking accommodation through Airbnb.

**Target Launch**: Q1 2026
**Platform**: iOS 17+ & Android 28+ (Cross-platform via Skip Fuse)
**Backend**: Self-hosted Supabase with 35 Edge Functions

---

## 1. Product Overview

### 1.1 Purpose

**Problem Statement:**
- 40% of food produced globally goes to waste
- Many people struggle with food insecurity in their communities
- Existing food-sharing apps lack intuitive UX and modern design
- No trusted platform for peer-to-peer food sharing with real-time communication

**Solution:**
Foodshare solves these problems by providing a premium, trust-focused platform where:
- Food donors can quickly list surplus food with photos and details
- Recipients can discover available food nearby in real-time
- Users can communicate securely via in-app chat
- Community members build reputation through ratings and verified profiles

### 1.2 Target Audience

#### Primary Users

**1. Food Donors (35-55 years old)**
- Environmentally conscious individuals
- Home cooks with meal prep surplus
- Families with excess groceries
- Garden owners with abundant produce
- Small business owners (bakeries, cafes)

**2. Food Recipients (25-45 years old)**
- Budget-conscious families and students
- Environmentally conscious individuals
- Community-minded people
- People seeking variety in their diet
- Urban dwellers in food deserts

#### Secondary Users

**3. Community Organizations**
- Food banks
- Community centers
- Religious organizations
- Nonprofit food rescue groups

### 1.3 Value Proposition

**For Donors:**
- Reduce food waste guilt
- Help community members
- Build social connections
- Gain positive reputation
- Environmental impact tracking

**For Recipients:**
- Access free, quality food
- Discover diverse food options
- Save money on groceries
- Meet neighbors
- Contribute to sustainability

**Competitive Differentiators:**
- **Premium Design**: Airbnb-level polish with glassmorphism aesthetic
- **Real-Time Communication**: Instant chat for coordination
- **Trust & Safety**: Verified profiles, ratings, user guidelines
- **Fast & Intuitive**: Post listings in under 60 seconds
- **Location Intelligence**: Smart nearby discovery with maps

### 1.4 Success Metrics (KPIs)

#### Engagement Metrics
- **MAU (Monthly Active Users)**: 10,000 by Month 6
- **Daily Active Users (DAU)**: 2,000 by Month 6
- **Listings Created Per Day**: 500+ by Month 6
- **Successful Claims Per Day**: 300+ by Month 6

#### Quality Metrics
- **Claim Success Rate**: >80% of listings successfully transferred
- **User Retention (30-day)**: >40%
- **Chat Response Rate**: >70% within 1 hour
- **Average Rating**: >4.5 stars

#### Impact Metrics
- **Food Items Saved**: 50,000 items by Month 12
- **Estimated Pounds Diverted**: 100,000 lbs by Month 12
- **CO2 Emissions Prevented**: Track and display

#### Business Metrics
- **App Store Rating**: >4.7 stars
- **Crash-Free Rate**: >99.5%
- **Session Length**: >5 minutes average

---

## 2. User Personas

### Persona 1: Sarah - The Conscious Donor

**Demographics**
- Age: 38
- Location: Urban area (San Francisco)
- Occupation: Marketing Manager
- Income: $95,000/year
- Tech-savviness: High

**Background**
Sarah is a working mother of two who meal preps on Sundays but often overestimates portions. She's passionate about sustainability and hates throwing food away. She's active on social media and enjoys helping her community.

**Goals**
- Quickly share surplus food before it spoils
- Feel good about reducing food waste
- Connect with neighbors
- Track her environmental impact
- Build a positive reputation in her community

**Frustrations**
- Current donation processes take too much time
- Doesn't know who to give food to
- Concerns about food safety liability
- Wants confirmation food was actually received
- No way to track impact

**User Stories**
- As Sarah, I want to photograph and list my surplus lasagna in under 60 seconds so I don't waste time during my busy evening
- As Sarah, I want to see who claimed my food and read their profile so I feel confident about the transaction
- As Sarah, I want to track how much food I've shared over time so I can see my environmental impact
- As Sarah, I want to message recipients directly so I can coordinate pickup times
- As Sarah, I want to receive notifications when someone claims my listings so I can respond quickly

**Usage Patterns**
- Posts 2-3 listings per week (Sunday evenings, Wednesday evenings)
- Responds to messages within 30 minutes
- Checks app when receiving notifications
- Browses community impact dashboard monthly

---

### Persona 2: Marcus - The Budget-Conscious Recipient

**Demographics**
- Age: 26
- Location: Suburban area (Oakland)
- Occupation: Graduate Student
- Income: $25,000/year
- Tech-savviness: Very High

**Background**
Marcus is pursuing his Master's degree and living on a tight budget. He's environmentally conscious and appreciates variety in his diet. He's comfortable with technology and frequently uses apps for food delivery, ridesharing, and social networking.

**Goals**
- Supplement groceries to save money
- Find interesting, quality food options
- Reduce environmental footprint
- Build connections in his new neighborhood
- Quick and easy pickup coordination

**Frustrations**
- Limited food budget restricts variety
- Doesn't want to seem "needy" or judged
- Uncertainty about food quality/freshness
- Difficulty coordinating pickup times
- Competition for popular listings

**User Stories**
- As Marcus, I want to browse available food near my apartment so I can supplement my groceries
- As Marcus, I want to filter by category (produce, baked goods, etc.) so I can find what I need
- As Marcus, I want to claim a listing with one tap and immediately chat with the donor so I can arrange pickup quickly
- As Marcus, I want to see expiry dates and photos upfront so I know food quality before claiming
- As Marcus, I want to build a good reputation through ratings so donors trust me

**Usage Patterns**
- Checks app 2-3 times daily (morning, lunch, evening)
- Claims 3-5 listings per week
- Responds to messages within 15 minutes
- Leaves ratings and thank-you messages
- Shares interesting finds on social media

---

### Persona 3: Linda - The Community Organizer

**Demographics**
- Age: 52
- Location: Mixed urban/suburban (community center)
- Occupation: Community Center Director
- Income: $65,000/year
- Tech-savviness: Medium

**Background**
Linda runs a community center that serves low-income families. She coordinates food rescue operations and volunteers. She's relationship-focused and knows most regular visitors by name.

**Goals**
- Scale food rescue operations
- Reduce logistics burden
- Connect donors directly with families
- Track community impact
- Reduce dependence on institutional donors

**Frustrations**
- Coordinating pickups/dropoffs is time-consuming
- Hard to match donors with specific recipient needs
- Lack of visibility into community food availability
- No data to report to grant funders
- Food goes to waste due to coordination failures

**User Stories**
- As Linda, I want to create an organizational account so I can facilitate food sharing at scale
- As Linda, I want to receive alerts for large quantities of food so I can coordinate community pickups
- As Linda, I want to generate reports on food saved so I can report to grant funders
- As Linda, I want to verify trusted community members so they can be designated "community heroes"

**Usage Patterns**
- Checks app 4-5 times daily during work hours
- Coordinates 10-15 claims per week on behalf of families
- Manages volunteer network through app
- Generates monthly impact reports

---

## 3. Feature Requirements

### 3.1 Authentication & Onboarding (MVP - P0)

**Priority**: P0 (Must Have for Launch)

**Description**
Users must be able to securely create accounts, sign in, and complete a welcoming onboarding experience that explains app value and builds trust.

**User Stories**
- As a new user, I want to sign up with email/password so I can quickly create an account
- As a returning user, I want to sign in with Apple ID or Google so login is convenient
- As a user, I want to verify my email address so the community trusts me
- As a user, I want to reset my password if I forget it
- As a new user, I want to see an onboarding tutorial so I understand how the app works
- As a user, I want my session to persist so I don't need to log in every time

**Acceptance Criteria**
- [ ] Email/password signup with validation (password requirements, email format)
- [ ] Email verification required before posting listings
- [ ] Sign in with Apple (Apple requirement for social login)
- [ ] Sign in with Google OAuth
- [ ] Password reset flow via email
- [ ] Onboarding carousel (3-4 screens) explaining key features
- [ ] Session persistence using Keychain
- [ ] Secure token storage with automatic refresh
- [ ] Sign out functionality
- [ ] Delete account option (GDPR compliance)

**Technical Requirements**
- Supabase Auth integration with PKCE flow
- Keychain storage for auth tokens
- Email verification via Supabase
- OAuth integration (Apple, Google)
- Async/await authentication flow

**UX Requirements**
- Glassmorphism design for auth screens
- Smooth animations between onboarding screens
- Clear error messages
- Biometric unlock for returning users (Face ID/Touch ID)

**Dependencies**
- Supabase project setup
- OAuth provider credentials (Apple, Google)
- Email templates configured in Supabase

**Mockups**: [Link to Figma - Auth Flows]

**Testing Requirements**
- Unit tests for auth logic
- Integration tests with Supabase Auth
- UI tests for complete signup/signin flows
- Security testing for token storage

---

### 3.2 Food Listings (MVP - P0)

**Priority**: P0 (Must Have for Launch)

**Description**
Core functionality enabling users to create, browse, search, and claim food listings with rich details including photos, descriptions, categories, locations, and expiry dates.

**User Stories**
- As a donor, I want to create a listing with photos, title, description, category, location, and expiry date so recipients know what I'm offering
- As a donor, I want to upload up to 5 photos so recipients can see food quality
- As a recipient, I want to browse available listings near me so I can find food I need
- As a recipient, I want to filter by category (produce, dairy, baked goods, prepared meals, pantry items) so I can narrow results
- As a recipient, I want to sort by distance, expiry date, or recency so I can prioritize listings
- As a recipient, I want to see listing details (photos, description, distance, expiry) before claiming
- As a recipient, I want to claim a listing with one tap so I can secure the food quickly
- As a donor, I want to mark my listing as completed after successful handoff
- As either user, I want to see listing status (available, claimed, completed) so I know current state

**Acceptance Criteria**

**Create Listing:**
- [ ] Form with fields: title (required), description (optional), category (required), expiry date (required)
- [ ] Image picker supporting 1-5 photos from camera or library
- [ ] Image upload to Supabase Storage with progress indicator
- [ ] Location selection: use current location or manual address entry
- [ ] Validation: title 3-100 chars, expiry date must be future, at least 1 photo
- [ ] Save as draft functionality
- [ ] Preview before publishing
- [ ] Listing creation completes in <5 seconds

**Browse Listings:**
- [ ] List view showing nearby listings (default radius: 5 miles)
- [ ] Card design with glassmorphism aesthetic
- [ ] Each card shows: primary photo, title, category icon, distance, expiry countdown
- [ ] Infinite scroll / pagination (20 listings per page)
- [ ] Pull-to-refresh for latest listings
- [ ] Empty state when no listings available
- [ ] Loading states during data fetch

**Filter & Search:**
- [ ] Filter by category (multi-select)
- [ ] Filter by distance (1, 5, 10, 25 miles)
- [ ] Filter by expiry (today, tomorrow, this week)
- [ ] Search by keyword (title/description)
- [ ] Sort by: distance, expiry (soonest first), newest
- [ ] Clear all filters button
- [ ] Filter state persists during session

**Listing Detail:**
- [ ] Full-screen photo gallery with swipe gesture
- [ ] Listing details: title, full description, category, expiry date/time
- [ ] Donor profile summary (name, photo, rating, # listings shared)
- [ ] Distance from user's location
- [ ] Map showing approximate location (privacy: show radius, not exact address)
- [ ] Status indicator (available, claimed, completed)
- [ ] Claim button (primary CTA) - only shown if available
- [ ] Share listing button
- [ ] Report listing button (abuse/inappropriate content)

**Claim Listing:**
- [ ] One-tap claim functionality
- [ ] Confirmation modal with pickup instructions
- [ ] Automatic chat thread creation with donor
- [ ] Push notification to donor
- [ ] Listing status changes to "claimed"
- [ ] Only one user can claim a listing

**Manage Listings:**
- [ ] "My Listings" tab showing user's posted listings
- [ ] "Claimed" tab showing listings user has claimed
- [ ] Edit listing (only if not claimed)
- [ ] Delete listing (with confirmation)
- [ ] Mark as completed (donor only)
- [ ] Repost completed/expired listing

**Technical Requirements**
- Supabase PostgreSQL with PostGIS for geolocation queries
- Supabase Storage for image uploads (max 5MB per image)
- Real-time subscriptions for listing status changes
- Image compression before upload
- Caching strategy for list view
- Efficient geospatial queries (< 500ms)

**Database Schema:**
```sql
CREATE TABLE food_listings (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID REFERENCES auth.users(id) ON DELETE CASCADE,
    title TEXT NOT NULL,
    description TEXT,
    category TEXT NOT NULL,
    expiry_date TIMESTAMP WITH TIME ZONE NOT NULL,
    location GEOGRAPHY(POINT, 4326) NOT NULL,
    address TEXT,
    images TEXT[] DEFAULT '{}',
    status TEXT DEFAULT 'available' CHECK (status IN ('available', 'claimed', 'completed', 'expired')),
    claimed_by UUID REFERENCES auth.users(id),
    claimed_at TIMESTAMP WITH TIME ZONE,
    completed_at TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

CREATE INDEX idx_food_listings_location ON food_listings USING GIST(location);
CREATE INDEX idx_food_listings_status ON food_listings(status);
CREATE INDEX idx_food_listings_expiry ON food_listings(expiry_date);
```

**UX Requirements**
- Glassmorphism cards for listings
- Smooth animations (card expand, image carousel)
- Haptic feedback on claim action
- Optimistic UI updates (claim reflects immediately)
- Skeleton loaders during data fetch
- High-quality image thumbnails

**Dependencies**
- Authentication (must be logged in)
- Location permissions
- Camera/photo library permissions
- Push notification permissions (for claim alerts)

**Mockups**: [Link to Figma - Listings Flows]

**Testing Requirements**
- Unit tests for listing validation, distance calculation
- Integration tests for CRUD operations with Supabase
- UI tests for complete create/browse/claim flow
- Performance tests for geospatial queries
- Load tests for image uploads

---

### 3.3 Real-Time Chat (MVP - P0)

**Priority**: P0 (Must Have for Launch)

**Description**
In-app messaging system enabling donors and recipients to coordinate pickup details, ask questions about food items, and build trust through communication.

**User Stories**
- As a recipient, I want to message the donor immediately after claiming so I can coordinate pickup
- As a donor, I want to receive messages from recipients so I can provide pickup instructions
- As either user, I want to see typing indicators so I know when the other person is responding
- As either user, I want to see message read receipts so I know my message was seen
- As either user, I want to receive push notifications for new messages so I respond quickly
- As either user, I want to see message history so I can reference previous conversations
- As either user, I want to share my availability (e.g., "Available after 6pm") quickly

**Acceptance Criteria**

**Chat Interface:**
- [ ] Conversation list showing all active chats
- [ ] Each conversation shows: other user's photo, name, last message preview, timestamp
- [ ] Unread message badges
- [ ] Swipe to delete conversation
- [ ] Search conversations by user name

**Messaging:**
- [ ] Text message sending with send button
- [ ] Message bubbles (donor vs recipient styling)
- [ ] Message timestamps (smart formatting: "Just now", "10m ago", "Yesterday", date)
- [ ] Typing indicators ("Sarah is typing...")
- [ ] Read receipts (checkmarks: sent, delivered, read)
- [ ] Auto-scroll to latest message
- [ ] Long-press message for options (copy, delete)
- [ ] Character limit: 1000 characters per message

**Quick Responses:**
- [ ] Quick response buttons: "Available now", "Available after [time]", "Address: [location]", "Still available?"
- [ ] Custom quick response creation
- [ ] Emoji picker

**Real-Time Updates:**
- [ ] New messages appear instantly without refresh
- [ ] Conversations auto-update in list view
- [ ] Push notifications for new messages (when app backgrounded)
- [ ] Local notifications for foreground messages

**Chat Context:**
- [ ] Listing details embedded in chat header (photo, title, expiry)
- [ ] Tap listing to view full details
- [ ] Related listing status visible in chat

**Technical Requirements**
- Supabase Realtime for message subscriptions
- PostgreSQL for message persistence
- Push notifications via APNs
- Efficient pagination (50 messages per page)
- Offline message queue (send when reconnected)
- Message delivery confirmation

**Database Schema:**
```sql
CREATE TABLE conversations (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    listing_id UUID REFERENCES food_listings(id) ON DELETE CASCADE,
    donor_id UUID REFERENCES auth.users(id),
    recipient_id UUID REFERENCES auth.users(id),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    UNIQUE(listing_id, donor_id, recipient_id)
);

CREATE TABLE messages (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    conversation_id UUID REFERENCES conversations(id) ON DELETE CASCADE,
    sender_id UUID REFERENCES auth.users(id),
    content TEXT NOT NULL,
    message_type TEXT DEFAULT 'text',
    read_at TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

CREATE INDEX idx_messages_conversation ON messages(conversation_id, created_at DESC);
```

**UX Requirements**
- iMessage-inspired chat bubbles with glassmorphism
- Smooth keyboard appearance animations
- Auto-focus on message input when opening chat
- Message send animation
- Haptic feedback on message send
- Typing indicator animation

**Dependencies**
- Authentication
- Food Listings (chat initiated from claim action)
- Push notification setup

**Mockups**: [Link to Figma - Chat Flows]

**Testing Requirements**
- Unit tests for message validation
- Integration tests for real-time subscriptions
- UI tests for sending/receiving messages
- Load tests for concurrent conversations
- Offline scenario testing

---

### 3.4 Location & Maps (MVP - P0)

**Priority**: P0 (Must Have for Launch)

**Description**
Location-based discovery enabling users to find food listings nearby with map visualization, distance calculations, and privacy-conscious address handling.

**User Stories**
- As a recipient, I want to see listings near my current location so I can find convenient pickups
- As a recipient, I want to view listings on a map so I can visualize locations
- As a donor, I want to share my approximate location (not exact address) for privacy
- As either user, I want to see distance to listings so I can assess convenience
- As a recipient, I want to adjust search radius so I can expand or narrow results

**Acceptance Criteria**

**Location Permissions:**
- [ ] Request "When In Use" location permission on first app launch
- [ ] Clear explanation of why location is needed
- [ ] Graceful handling if permission denied (allow manual location entry)
- [ ] Settings deeplink if user wants to enable location later

**Map View:**
- [ ] Interactive map showing available listings as pins
- [ ] Custom pin design with category icon
- [ ] Cluster pins when zoomed out
- [ ] Tap pin to show listing preview card
- [ ] User location indicator (blue dot)
- [ ] Map controls: zoom, compass, center on user
- [ ] Switch between map and list views

**Distance Calculation:**
- [x] Calculate distance from user's location to each listing
- [x] Display distance in miles (US/UK) or kilometers (international) via `DistanceUnit`
- [ ] "Near you" badge for listings < 1 mile/1.6 km
- [ ] Sort by distance functionality

**Privacy & Safety:**
- [ ] Donors' exact addresses hidden until claim
- [ ] Show approximate location (0.1 mile radius circle on map)
- [ ] Full address shared only in chat after claim
- [ ] Option for donors to choose public meetup spot instead of home

**Search Radius:**
- [ ] Default search radius: 5 km (auto-converts to ~3 mi for US/UK users)
- [x] Adjustable radius with locale-aware slider (1-50 km or 0.5-30 mi)
- [ ] "Expand search" button if no results
- [ ] Visual radius circle on map

**Technical Requirements**
- Core Location framework for GPS
- MapKit for map rendering
- PostGIS for geospatial queries in database
- Efficient spatial indexing
- Background location updates (optional, for notifications)
- Reverse geocoding for address display

**Geospatial Query Example:**
```sql
-- Find listings within 5 miles of user location
SELECT *,
       ST_Distance(location, ST_MakePoint(:user_lon, :user_lat)::geography) / 1609.34 AS distance_miles
FROM food_listings
WHERE status = 'available'
  AND ST_DWithin(
    location,
    ST_MakePoint(:user_lon, :user_lat)::geography,
    8046.72  -- 5 miles in meters
  )
ORDER BY distance_miles ASC
LIMIT 50;
```

**UX Requirements**
- Smooth map animations
- Custom map style (if possible with MapKit)
- Glassmorphic listing cards overlaying map
- Location permission prompt with context
- Loading indicator while fetching location

**Dependencies**
- Food Listings (map displays listings)
- Location permissions
- MapKit integration

**Mockups**: [Link to Figma - Map View]

**Testing Requirements**
- Unit tests for distance calculations
- Integration tests for geospatial queries
- UI tests for map interactions
- Location permission scenarios
- Performance tests with 1000+ pins

---

### 3.5 User Profiles & Ratings (P1 - Post-MVP)

**Priority**: P1 (Should Have - Phase 2)

**Description**
User profile system with ratings, reviews, and reputation building to establish trust in the community.

**User Stories**
- As a user, I want to create a profile with photo and bio so others know who I am
- As either user, I want to rate and review the other party after a transaction so reputation is tracked
- As a recipient, I want to see a donor's rating before claiming so I can trust the quality
- As a donor, I want to see my impact statistics so I feel motivated
- As a user, I want to report problematic behavior so the community stays safe

**Acceptance Criteria**
- [ ] Profile creation/editing (name, photo, bio, about me)
- [ ] Rating system (1-5 stars)
- [ ] Review text (optional, 500 chars max)
- [ ] Profile displays: avg rating, # transactions, # listings shared, food saved (lbs)
- [ ] Verified badge (email verified + 5+ transactions)
- [ ] Block user functionality
- [ ] Report user functionality
- [ ] View other users' profiles
- [ ] Privacy settings (show/hide location, bio)

**Technical Requirements**
- User profiles table
- Ratings/reviews table
- Moderation system for reports
- Image upload for profile photos
- Impact calculation (food weight estimation)

**Dependencies**
- Authentication
- Completed transactions (from listings)

---

### 3.6 Push Notifications (P1 - Post-MVP)

**Priority**: P1 (Should Have - Phase 2)

**Description**
Push notifications to keep users engaged and informed about important events.

**Notification Types**
- [ ] Listing claimed (donor)
- [ ] New message received
- [ ] Listing expiring soon (donor)
- [ ] Nearby listing posted (recipient, with category preferences)
- [ ] Transaction completed reminder
- [ ] Request for rating/review
- [ ] Weekly impact summary

**Acceptance Criteria**
- [ ] Request notification permission
- [ ] Notification preferences in settings
- [ ] Rich notifications with images
- [ ] Deep linking to relevant screens
- [ ] Notification history in app
- [ ] Badge count on app icon

**Technical Requirements**
- APNs integration
- Supabase Edge Function for notification triggers
- Token management
- Notification scheduling

---

### 3.7 In-App Notifications & Activity Feed (P2 - Future)

**Priority**: P2 (Nice to Have - Phase 3+)

**Description**
In-app notification center and activity feed showing user's interactions.

**Acceptance Criteria**
- [ ] Notification bell icon in navigation
- [ ] Unread count badge
- [ ] Activity feed showing all interactions
- [ ] Grouped notifications
- [ ] Mark as read functionality
- [ ] Clear all notifications

---

### 3.8 Community Impact Dashboard (P2 - Future)

**Priority**: P2 (Nice to Have - Phase 3+)

**Description**
Visualize community and individual impact on food waste reduction.

**Features**
- [ ] Personal impact stats (food saved, CO2 prevented, $ value)
- [ ] Community leaderboard
- [ ] Badges and achievements
- [ ] Shareable impact cards for social media
- [ ] Monthly impact reports via email (Resend)

---

## 4. Design Requirements

### 4.1 Design System - "Liquid Glass v26"

**Core Aesthetic**
- Airbnb-influenced premium design
- Glassmorphism with frosted glass effects
- Clean, minimal, modern
- High-quality food photography emphasis

**Color Palette**
```
Primary: #2ECC71 (Fresh Green)
  - Represents fresh food, sustainability, growth
  - Use for: CTAs, active states, success messages

Secondary: #3498DB (Trust Blue)
  - Represents trust, reliability
  - Use for: Navigation, links, informational elements

Accent: #F39C12 (Urgency Orange)
  - Represents urgency for expiring food
  - Use for: Expiry warnings, important alerts

Neutrals:
  - Background: Dynamic (light/dark mode)
  - Surface: .ultraThinMaterial (glassmorphism)
  - Text Primary: .primary (system)
  - Text Secondary: .secondary (system)

Semantic:
  - Success: #27AE60 (Green)
  - Warning: #F39C12 (Orange)
  - Error: #E74C3C (Red)
  - Info: #3498DB (Blue)
```

**Typography**
```
Font Family: SF Pro (iOS system font)

Styles:
- Display: SF Pro Display, Semibold, 34pt (large titles)
- Title: SF Pro Text, Semibold, 20pt (screen titles)
- Headline: SF Pro Text, Semibold, 17pt (card titles)
- Body: SF Pro Text, Regular, 16pt (main content)
- Callout: SF Pro Text, Regular, 15pt (secondary content)
- Caption: SF Pro Text, Regular, 13pt (metadata, timestamps)
```

**Spacing System**
```
xs: 4pt   (tight spacing, internal component padding)
sm: 8pt   (small gaps between related elements)
md: 16pt  (standard spacing, card padding)
lg: 24pt  (section spacing)
xl: 32pt  (major section breaks)
xxl: 48pt (screen top/bottom padding)
```

**Elevation & Shadows**
```
Subtle:
  - Shadow: color .black opacity 0.1, radius 10pt, offset (0, 4pt)
  - Use for: Floating buttons, tooltips

Medium:
  - Shadow: color .black opacity 0.15, radius 20pt, offset (0, 8pt)
  - Use for: Cards, listing cards

Strong:
  - Shadow: color .black opacity 0.2, radius 30pt, offset (0, 12pt)
  - Use for: Modals, sheets, important overlays
```

**Glass Effect Components**
- Background material: .ultraThinMaterial or .thinMaterial
- Border: white opacity 0.2, width 1pt
- Corner radius: 12pt (standard), 16pt (large cards), 20pt (modals)

### 4.2 Key Screens

**1. Onboarding**
- 3-screen carousel with illustrations
- Glassmorphic cards explaining features
- Skip button (top right)
- Animated page indicators

**2. Authentication**
- Email/password form with glassmorphic text fields
- Social login buttons (Apple, Google)
- Smooth transitions between signup/signin

**3. Home - Listings Feed**
- Tab bar navigation at bottom
- Search bar at top with filter button
- Glassmorphic listing cards in vertical scroll
- Floating action button (bottom right) to create listing

**4. Map View**
- Full-screen map
- Custom category pins
- Bottom sheet with listing preview cards
- Filter/radius controls overlaying map

**5. Listing Detail**
- Full-screen photo gallery
- Glassmorphic content card overlaying photos
- Floating claim button at bottom
- Donor profile summary

**6. Create Listing**
- Multi-step form (photos → details → location → review)
- Progress indicator at top
- Image picker with thumbnails
- Form validation with inline errors

**7. Chat**
- Conversation list with glassmorphic cards
- Chat interface with message bubbles
- Listing context in chat header
- Quick response buttons

**8. User Profile**
- Hero section with profile photo and stats
- Tab view: Active Listings, Claimed, History
- Edit profile button
- Impact statistics

### 4.3 Component Library

**Buttons**
- Primary Button: Solid green fill, white text, 50pt height
- Secondary Button: Glass effect, green border, green text
- Ghost Button: No background, green text
- Icon Button: Icon only, glass effect background

**Cards**
- Glass Card: .ultraThinMaterial, 12pt corners, white border 0.2 opacity
- Listing Card: Glass card with image, title, category, distance, expiry
- User Card: Glass card with avatar, name, rating

**Text Fields**
- Glass TextField: .thinMaterial, 12pt corners, white border
- Focus state: Green border, subtle glow

**Modals & Sheets**
- Bottom Sheet: .thinMaterial, 20pt top corners, drag indicator
- Full Modal: .regularMaterial, slide-up animation

### 4.4 Animations & Interactions

- Card tap: Scale to 0.97, haptic feedback
- List scroll: Parallax effect on images
- Button press: Scale to 0.95
- Modal present: Slide up with spring animation
- Tab switch: Cross-dissolve
- Pull to refresh: Custom animation with logo
- Loading states: Skeleton screens with shimmer effect

### 4.5 Accessibility

- VoiceOver support for all interactive elements
- Dynamic Type support (scale fonts with system settings)
- High Contrast mode alternative (solid backgrounds instead of glass)
- Minimum touch target: 44x44pt
- Color contrast ratio: WCAG AA minimum (4.5:1)
- Reduce Motion support (disable animations)
- Reduce Transparency support (solid backgrounds)

### 4.6 Design Assets

- **Figma Design File**: [Link to Figma workspace]
- **Style Guide**: [Link to design system documentation]
- **Icon Set**: SF Symbols + custom category icons
- **Illustrations**: Custom illustrations for onboarding
- **App Icon**: Designed in multiple sizes (iOS requirements)

---

## 5. Technical Requirements

### 5.1 Platform & Compatibility

**Platform**
- iOS 17.0+ (to leverage `@Observable` macro and latest SwiftUI features)
- iPhone support: iPhone SE (3rd gen) to iPhone 15 Pro Max
- iPad support: iPad (9th gen) and later (adaptive layout)
- Orientations: Portrait (primary), Landscape (supported)

**Device Testing Matrix**
- iPhone SE (3rd gen) - 4.7" - Low-end performance baseline
- iPhone 14 - 6.1" - Mid-range standard
- iPhone 15 Pro Max - 6.7" - High-end with latest features
- iPad (10th gen) - 10.9" - Tablet layout

### 5.2 Technology Stack

**Frontend (iOS App)**
- **Language**: Swift 5.9+
- **UI Framework**: SwiftUI
- **Architecture**: MVVM + Clean Architecture
- **State Management**: `@Observable` macro (iOS 17+)
- **Concurrency**: Swift async/await, actors
- **Dependency Injection**: Constructor injection + Environment objects
- **Image Loading**: Kingfisher or native AsyncImage with caching
- **Location**: Core Location
- **Maps**: MapKit

**Backend (Supabase)**
- **Database**: PostgreSQL 15+ with PostGIS extension
- **Authentication**: Supabase Auth (PKCE flow)
- **Storage**: Supabase Storage (S3-compatible)
- **Real-time**: Supabase Realtime (WebSocket subscriptions)
- **Edge Functions**: Deno/TypeScript serverless functions
- **API**: Auto-generated REST API from PostgreSQL

**Additional Services**
- **Caching**: Upstash Redis (for API response caching, rate limiting)
- **Event Streaming**: Upstash Kafka (for analytics, audit logs)
- **Email**: Resend (transactional emails, notifications)
- **Push Notifications**: Apple Push Notification Service (APNs)
- **Analytics**: (TBD - PostHog, Mixpanel, or Firebase Analytics)
- **Crash Reporting**: (TBD - Sentry or Crashlytics)

**Development Tools**
- **Version Control**: Git with GitHub
- **CI/CD**: GitHub Actions
- **Git Hooks**: Lefthook (pre-commit: lint, format; pre-push: tests)
- **Linting**: SwiftLint
- **Formatting**: SwiftFormat
- **Dependency Management**: Swift Package Manager
- **Testing**: XCTest + Swift Testing (Xcode 16)

### 5.3 Architecture

**Clean Architecture Layers**

1. **Presentation Layer**
   - SwiftUI Views
   - `@Observable` ViewModels
   - View-specific logic only

2. **Domain Layer**
   - Use Cases (business logic)
   - Repository Protocols (interfaces)
   - Domain Models (pure Swift types)

3. **Data Layer**
   - Repository Implementations
   - API Clients (Supabase SDK)
   - Data Transfer Objects (DTOs)
   - Mappers (DTO ↔ Domain Model)

4. **Infrastructure Layer**
   - Network Service
   - Database Service (Supabase client)
   - Location Service
   - Storage Service

**Folder Structure**
```
FoodshareApp/
├── App/
├── Core/
│   ├── Design/
│   ├── Networking/
│   ├── Database/
│   ├── Location/
│   └── Extensions/
├── Features/
│   ├── Authentication/
│   ├── FoodListings/
│   ├── Chat/
│   ├── UserProfile/
│   └── Map/
└── Tests/
```

### 5.4 Database Schema (Supabase PostgreSQL)

**Core Tables**

```sql
-- Users (extended from auth.users)
CREATE TABLE profiles (
    id UUID PRIMARY KEY REFERENCES auth.users(id) ON DELETE CASCADE,
    username TEXT UNIQUE,
    full_name TEXT,
    avatar_url TEXT,
    bio TEXT,
    location TEXT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

-- Food Listings
CREATE TABLE food_listings (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID REFERENCES auth.users(id) ON DELETE CASCADE,
    title TEXT NOT NULL,
    description TEXT,
    category TEXT NOT NULL,
    expiry_date TIMESTAMP WITH TIME ZONE NOT NULL,
    location GEOGRAPHY(POINT, 4326) NOT NULL,
    address TEXT,
    images TEXT[] DEFAULT '{}',
    status TEXT DEFAULT 'available' CHECK (status IN ('available', 'claimed', 'completed', 'expired')),
    claimed_by UUID REFERENCES auth.users(id),
    claimed_at TIMESTAMP WITH TIME ZONE,
    completed_at TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

-- Conversations
CREATE TABLE conversations (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    listing_id UUID REFERENCES food_listings(id) ON DELETE CASCADE,
    donor_id UUID REFERENCES auth.users(id),
    recipient_id UUID REFERENCES auth.users(id),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    UNIQUE(listing_id, donor_id, recipient_id)
);

-- Messages
CREATE TABLE messages (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    conversation_id UUID REFERENCES conversations(id) ON DELETE CASCADE,
    sender_id UUID REFERENCES auth.users(id),
    content TEXT NOT NULL CHECK (LENGTH(content) <= 1000),
    message_type TEXT DEFAULT 'text',
    read_at TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

-- Ratings
CREATE TABLE ratings (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    listing_id UUID REFERENCES food_listings(id) ON DELETE CASCADE,
    rater_id UUID REFERENCES auth.users(id),
    rated_user_id UUID REFERENCES auth.users(id),
    rating INTEGER CHECK (rating BETWEEN 1 AND 5),
    review_text TEXT CHECK (LENGTH(review_text) <= 500),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    UNIQUE(listing_id, rater_id)
);

-- Indexes
CREATE INDEX idx_food_listings_location ON food_listings USING GIST(location);
CREATE INDEX idx_food_listings_status ON food_listings(status) WHERE status = 'available';
CREATE INDEX idx_food_listings_expiry ON food_listings(expiry_date) WHERE status = 'available';
CREATE INDEX idx_messages_conversation ON messages(conversation_id, created_at DESC);
```

### 5.5 API Integration

**Supabase Swift SDK**
```swift
import Supabase

let supabase = SupabaseClient(
    supabaseURL: URL(string: "YOUR_SUPABASE_URL")!,
    supabaseKey: "YOUR_ANON_KEY",
    options: SupabaseClientOptions(
        auth: .init(
            storage: KeychainStorage(),
            flowType: .pkce
        )
    )
)
```

**Example API Calls**
```swift
// Fetch nearby listings
let listings = try await supabase
    .from("food_listings")
    .select()
    .eq("status", value: "available")
    .execute()
    .value

// Create listing
let listing = FoodListingDTO(...)
try await supabase
    .from("food_listings")
    .insert(listing)
    .execute()

// Real-time subscription
let subscription = await supabase
    .from("messages")
    .on(.insert) { message in
        // Handle new message
    }
    .subscribe()
```

### 5.6 Performance Requirements

**App Performance**
- Cold start time: < 2 seconds
- Listing list load: < 1 second
- Image load (thumbnail): < 500ms
- Image upload: < 5 seconds per image with progress
- Geospatial query: < 500ms
- Real-time message delivery: < 1 second
- Frame rate: 60 FPS (120 FPS on ProMotion devices)

**Optimization Strategies**
- Image compression before upload (max 1MB per image)
- Thumbnail generation (server-side via Supabase Storage transforms)
- List view pagination (20 items per page)
- Lazy image loading
- Database query optimization (proper indexing)
- API response caching (Upstash Redis)

### 5.7 Security Requirements

**Authentication & Authorization**
- Secure token storage (Keychain)
- PKCE flow for OAuth (prevents authorization code interception)
- Automatic token refresh
- Session timeout: 30 days
- Biometric authentication option

**Data Security**
- All API calls over HTTPS
- Row Level Security (RLS) policies on all tables
- Image upload validation (file type, size)
- Input sanitization (prevent SQL injection)
- Rate limiting (Upstash Redis)

**Privacy**
- GDPR compliance (data export, deletion)
- Privacy policy and terms of service
- Location privacy (approximate location public, exact private)
- User data encryption at rest (Supabase default)
- No third-party data sharing without consent

**Content Moderation**
- Report abuse functionality
- Automated content filtering (profanity, spam)
- Manual review process for reported content

### 5.8 Offline Support

**Offline Capabilities**
- View cached listings (last 50 loaded)
- Read conversation history
- View own profile
- Queue messages for sending when online

**Not Available Offline**
- Create/edit listings (requires image upload)
- Claim listings (requires real-time status check)
- Real-time message delivery
- Browse new listings

**Sync Strategy**
- Detect connectivity changes
- Retry failed operations when online
- Conflict resolution for queued messages

---

## 6. Non-Functional Requirements

### 6.1 Reliability

- **Availability**: 99.9% uptime (leveraging Supabase SLA)
- **Crash-Free Rate**: > 99.5%
- **Data Durability**: No data loss (PostgreSQL ACID compliance)
- **Error Handling**: Graceful degradation, user-friendly error messages

### 6.2 Scalability

- **User Growth**: Support 100,000 users by Year 1
- **Concurrent Users**: Handle 10,000 concurrent users
- **Listings**: Support 1M+ listings in database
- **Messages**: Handle 100K+ messages per day
- **Database**: Horizontal scaling via Supabase (connection pooling)

### 6.3 Maintainability

- **Code Quality**: SwiftLint/SwiftFormat enforced
- **Test Coverage**: 80%+ for business logic
- **Documentation**: Inline code comments, README, ADRs
- **Modular Architecture**: Feature-based modules for parallel development

### 6.4 Usability

- **Learnability**: New users complete first listing in < 3 minutes
- **Efficiency**: Returning users create listing in < 60 seconds
- **Error Rate**: < 5% user errors on critical flows
- **Satisfaction**: App Store rating > 4.7 stars

### 6.5 Localization

- **Initial Release**: English only (US, UK, Canada, Australia)
- **Phase 2**: Spanish (Mexico, Spain)
- **Phase 3**: French, German, Italian
- **Design Consideration**: Use i18n from start (no hardcoded strings)

### 6.6 Analytics & Monitoring

**Analytics Events**
- User signup/signin
- Listing created, viewed, claimed, completed
- Message sent
- Search performed
- Filter applied
- Location permission granted/denied
- Push notification permission granted/denied

**Monitoring**
- Crash reporting with stack traces
- Performance monitoring (API latency, screen load times)
- Error tracking (API errors, validation failures)
- User flow tracking (drop-off points)

---

## 7. Constraints & Assumptions

### 7.1 Constraints

**Technical**
- iOS 17.0+ (limits addressable market to newer devices)
- Requires active internet connection (minimal offline support)
- Location permission required for core functionality
- Camera/photo library permission required for listings

**Business**
- Budget: [To be determined]
- Timeline: 4-6 months to MVP launch
- Team size: 2-3 iOS developers, 1 designer, 1 product manager
- Initial market: United States only

**Legal**
- Food safety liability disclaimers required
- Terms of service and privacy policy
- GDPR compliance for potential EU expansion
- Age restriction: 18+ (App Store rating)

### 7.2 Assumptions

**User Behavior**
- Users willing to share approximate location
- Users willing to meet strangers for food pickup
- Users check app at least 2-3 times per week
- Users respond to messages within 1-2 hours

**Market**
- Growing awareness of food waste issues
- Users motivated by sustainability and cost savings
- Sufficient supply and demand in target cities

**Technical**
- Supabase service reliability
- Apple maintains SwiftUI/async-await APIs
- MapKit provides sufficient location features
- APNs delivers notifications reliably

---

## 8. Dependencies

### 8.1 External Dependencies

**Services**
- Supabase (database, auth, storage, realtime)
- Apple Developer Program (TestFlight, App Store)
- APNs (push notifications)
- Upstash (Redis, Kafka)
- Resend (email delivery)

**Third-Party Libraries**
- Supabase Swift SDK
- Kingfisher (image caching) - optional
- SnapshotTesting (visual regression tests) - optional

### 8.2 Internal Dependencies

**Design**
- Figma design system completion
- All screen designs approved
- Icon set finalized
- App icon designed

**Backend**
- Supabase project provisioned
- Database schema created
- Row Level Security policies implemented
- Storage buckets configured
- Edge Functions deployed

**Legal**
- Terms of Service finalized
- Privacy Policy finalized
- Food safety disclaimer written
- Content moderation policy established

---

## 9. Risks & Mitigation

| Risk | Impact | Probability | Mitigation Strategy |
|------|--------|-------------|---------------------|
| **Supabase service outage** | High | Low | Implement retry logic, cache data locally, monitor status page, have incident response plan |
| **Food safety liability** | High | Medium | Clear disclaimers, terms requiring users to follow food safety guidelines, user education, insurance |
| **Low user adoption** | High | Medium | Phased rollout starting with beta users, gather feedback, iterate on UX, incentivize early adopters |
| **Supply/demand imbalance** | Medium | High | Launch in cities with known demand, partner with food rescue orgs, incentivize donors |
| **Location privacy concerns** | Medium | Medium | Clear privacy policy, approximate locations only, let users choose public meetup spots |
| **Inappropriate content/abuse** | Medium | Medium | Report functionality, automated filtering, manual moderation, clear community guidelines |
| **Competitive pressure** | Medium | Low | Differentiate through premium UX, focus on trust/safety, build strong community |
| **Swift/iOS API changes** | Low | Medium | Use stable APIs, monitor WWDC announcements, maintain compatibility with older iOS versions where feasible |
| **Team capacity constraints** | Medium | Medium | Prioritize ruthlessly (MVP focus), consider contractors for specialized work, automate where possible |
| **App Store rejection** | High | Low | Follow guidelines carefully, test extensively, prepare detailed submission, have backup launch plan |

---

## 10. Release Strategy

### Phase 1: MVP Development (Months 1-3)

**Goals**
- Build core features (auth, listings, chat, maps)
- Establish design system
- Set up CI/CD pipeline
- Achieve 80% test coverage

**Milestones**
- Month 1: Architecture setup, design system, authentication
- Month 2: Listings CRUD, map view, location services
- Month 3: Chat, polish, testing, bug fixes

**Success Criteria**
- All P0 features functional
- < 5 critical bugs
- App Store ready (metadata, screenshots)

---

### Phase 2: Closed Beta (Month 4)

**Goals**
- Gather user feedback
- Identify and fix bugs
- Validate product-market fit
- Optimize performance

**Activities**
- TestFlight distribution to 100 beta testers
- User interviews (10-15 users)
- Analytics implementation
- Bug triage and fixes

**Success Criteria**
- > 4.0 star rating from beta testers
- < 3% crash rate
- Users complete first listing (> 70% completion rate)
- Positive qualitative feedback

---

### Phase 3: Public Beta (Month 5)

**Goals**
- Scale to 1,000 users
- Monitor performance under load
- Refine onboarding based on feedback
- Build initial community

**Activities**
- Open TestFlight beta (marketing to early adopters)
- Social media presence (Instagram, Twitter)
- Beta tester community (Discord or Slack)
- Performance optimization
- Prepare App Store assets

**Success Criteria**
- 1,000+ beta users
- > 40% 30-day retention
- 500+ listings created
- 300+ successful claims
- < 1% crash rate

---

### Phase 4: App Store Launch (Month 6)

**Goals**
- Public launch on App Store
- Drive initial user acquisition
- Establish brand presence

**Activities**
- App Store submission
- Launch marketing campaign
- Press outreach (TechCrunch, Product Hunt)
- Influencer partnerships
- Community building (social media)

**Success Criteria**
- App Store approval
- 10,000 downloads in first month
- > 4.7 App Store rating
- Featured on Product Hunt
- Press coverage (3+ articles)

---

### Phase 5: Post-Launch Iteration (Months 7-12)

**Goals**
- Implement P1 features (profiles, ratings, notifications)
- Improve based on user feedback
- Scale infrastructure
- Expand to new cities

**Activities**
- Feature releases every 2-4 weeks
- A/B testing for key flows
- User research interviews
- Performance optimization
- Community management

**Success Criteria**
- 100,000 MAU by Month 12
- > 40% 30-day retention
- 50,000 food items saved
- NPS score > 50
- Expand to 10+ cities

---

## 11. Appendix

### 11.1 Glossary

| Term | Definition |
|------|------------|
| **Listing** | A food item posted by a donor, available for claim |
| **Donor** | User who posts a food listing |
| **Recipient** | User who claims a food listing |
| **Claim** | Action of recipient securing a listing for pickup |
| **Glassmorphism** | Design aesthetic using frosted glass effects, transparency, and blur |
| **MVVM** | Model-View-ViewModel architectural pattern |
| **Clean Architecture** | Layered architecture with separation of concerns (presentation, domain, data) |
| **Real-time** | Instant updates without manual refresh (via Supabase Realtime) |
| **RLS** | Row Level Security - database-level access control in PostgreSQL |
| **PKCE** | Proof Key for Code Exchange - secure OAuth flow for mobile apps |

### 11.2 References

**Competitive Analysis**
- [Olio](https://olioex.com/) - Primary competitor
- [Too Good To Go](https://toogoodtogo.com/) - Business food rescue
- [Nextdoor](https://nextdoor.com/) - Community platform model

**Research**
- [Food Waste Statistics (USDA)](https://www.usda.gov/foodwaste)
- [Food Sharing Economics Study](https://example.com)
- [User Research Findings](https://example.com)

**Design Inspiration**
- [Airbnb Design](https://airbnb.design/)
- [Glassmorphism Design Trend](https://uxdesign.cc/glassmorphism-in-user-interfaces-1f39bb1308c9)
- [Apple Human Interface Guidelines](https://developer.apple.com/design/human-interface-guidelines/)

**Technical Documentation**
- [Supabase Documentation](https://supabase.com/docs)
- [SwiftUI Documentation](https://developer.apple.com/documentation/swiftui/)
- [Swift Concurrency](https://docs.swift.org/swift-book/LanguageGuide/Concurrency.html)

### 11.3 Related Documents

- **ADR.md** - Architecture Decision Records
- **Workflow.md** - Development workflow and processes
- **folder-structure.md** - Project organization details
- **TypeSystem.md** - Core type system documentation
- **Design System Guide** - Figma design specifications
- **API Documentation** - Supabase API reference

---

## 12. Change Log

| Version | Date | Author | Changes |
|---------|------|--------|---------|
| 1.0 | 2025-01-15 | Foodshare Team | Initial PRD for MVP |
| 1.1 | 2026-02-16 | Foodshare Team | Updated for cross-platform (Skip Fuse) |

---

## 13. Approval

| Role | Name | Signature | Date |
|------|------|-----------|------|
| Product Manager | [Name] | | |
| Engineering Lead | [Name] | | |
| Design Lead | [Name] | | |
| Business Stakeholder | [Name] | | |

---

**Document Status**: Draft - Ready for Review
