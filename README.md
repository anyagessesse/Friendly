# Friendly

## Table of Contents
1. [Overview](#Overview)
1. [Product Spec](#Product-Spec)
1. [Prototype](#Prototype)
2. [Schema](#Schema)

## Overview
### Description
Allows users to more easily plan when they will spend time with friends. When a user is bored/has free time and they aren’t sure which of their friends are free, they can update a status saying i“d like to spend time with someone“ and anyone they are friends with in the app can see that they are currently free. Removes the need to call each friend separately to ask if they are free.

### App Evaluation
- **Category:** Social, Lifestyle
- **Mobile:** Can conveniently make plans with friends without having to use a desktop website. Also makes use of notifications and possibly syncing to calendar.
- **Story:** Allows users to more easily get together with friends without the pressure/inconvenience of personally texting/calling people separately.
- **Market:** Anyone who enjoys spending time with friends, especially people who have many different friends in different "friend groups"/contexts.
- **Habit:** User can interact with this app daily, and even schedule out free time blocks in advance. Also user will get notifications from their friends regularly asking them to get together. Can form a habit to check the app whenever bored.
- **Scope:** Can start with the defined idea of just updating a status when bored and sending friends notifications. Can add on features such as scheduling free times in advance, connecting to your calendar, and being able to group people together by location.

## Product Spec
### 1. User Stories

**Required Must-have Stories**

- [ ] user can login with a username and password
    - [ ] user can create a new username and password if they don't already have an account
- [ ] user can see active "I'm free" statuses of their friends
- [ ] user can update status to "I'm free" which sends notifications to friends
    - [ ] User can write a persionalized message about what they want to do during that time and what city/where they are
- [ ] User can delete/hide their status once it is no longer needed
- [ ] user can search for their friends by username
- [ ] user can add and remove friends
- [ ] user can view their profile page with their friends list and profile picture
- [ ] user can use the camera to take a profile picture

**Optional Nice-to-have Stories**

- [ ] User can set what time a status will be deleted/when the user will no longer be free
- [ ] User can tap a status to see more details such as date posted, time free until, and description
- [ ] User is able to schedule free time periods in advance, and see their friends scheduled time periods
    - [ ] User can see when scheduled free time periods match up with friends (can send a notification for this)
    - [ ] User can sync with google calendar to block off busy time periods
- [ ] User can send a link to friends that allows them to become friends on the app
- [ ] User can block certain friends from seeing that they are free in that moment
- [ ] User can group together friends in categories that only the user can see from their perspective
    - [ ] User can favorite/only show notifications from certain groups/people
- [ ] User can put in their status which friends have said yes to hanging out so that users can see everyone who is going
- [ ] User can click a link/button to message/calls to make plans with a friend that is currently free

### 2. Screen Archetypes

* Login Screen
   * user can login with username and password
* Create New Account Screen
   * user can create a new account
* Main Page/Stream
    * user can see currently active (and future) statuses
* Profile
    * user can see their username and friends list
* Create Status Screen
    * user can enter text and details for a new status
    * user can update a previous status
* Search
    * user can search for other users
    * user can add/remove friends
* Status Details
    * user can see more details about the status
    * user can delete their status
* Calender (stretch)
    * user can schedule free time blocks in advance
    * user can see other friends free time blocks
* Calender Matches (stretch)
    * user can see which calendar times overlap with friends

### 3. Navigation

**Tab Navigation** (Tab to Screen)

* Main Page
* Calendar
* Profile
* Search
* Create Status Screen

**Flow Navigation** (Screen to Screen)

* Login Screen
   * Create New Account Screen
   * Main Page
* Create New Account Screen
   * Login Screen
* Main Page
    * Status Details
* Profile
    * none
* Create Status Screen
    * Main Page
* Search
    * none (back button to go back to main)
* Status Details
    * Create Status Screen (to update status)
    * Link to messages/calls to make plans
* Calendar(stretch)
    * Create Status Screen (for adding a free block)
    * Calendar Matches
* Calendar Matches (stretch)
    * Status Details?

## Prototype
https://www.figma.com/proto/iZtVHhNsfvenfRFKO2yZ7d/Friendly?node-id=2%3A0&scaling=scale-down

## Schema 

### Models
Status
Property | Type | Description
--- | --- | ---
objectId|String|unique id for each user status
updatedAt|Date|time and date when status was last updated
createdAt|Date|time and date when status was created
description|String|description of their status update
user|Pointer to user|user who posted the status

User
Property | Type | Description
--- | --- | ---
objectId|String|unique id for each user
username|String|unique username for each user
password|String|password to login to user's account
profilePic|File|image for user's profile picture
friends|List<User>|list of friends of that user

### Networking
#### List of network requests by screen
- Login
    - (Read/GET) query if user object exists
- Signup
    - (Create/POST) create a new user object
- Main Page
    - (Read/GET) query all statuses of friends
- Profile
    - (Read/GET) Query logged in user object
    - (Update/PUT) Update user profile image
    - (Read/GET) query all friends
- Create Status
    - (Create/POST) create a new status object
    - (Update/PUT) update an already existing status
- Search
    - (Read/GET) query all users to search from
    - (Udate/PUT) add a friend to friends list
- Status Details
    - (Delete) delete a status
- Calendar
    - (Read/GET) query posts in date order
