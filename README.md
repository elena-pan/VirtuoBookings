# VirtuoBookings

<img src="https://img.shields.io/badge/License-MIT-brightgreen.svg?style=flat-square" alt="License: MIT">

An Android app for appointment booking, real-time chat rooms, and user management. 

This project was originally developed for Women Empowerment League.

![App screens](https://user-images.githubusercontent.com/52430997/98731335-d60ae880-235a-11eb-9619-9131f4d75e0b.png)

## Features

#### Authentication
* Uses Firebase authentication for registration and login
* Different user types
* User data tracking
* Account deletion

#### Appointment Booking
* Clients can book/cancel appointments in available time slots
* Uses CalendarView library
* Service providers can view upcoming appointments
* Push notifications

#### Chat Rooms
* Option to message admin
* Uses Firebase Realtime Database and RecyclerView to update messages
* Hides client names from Service Providers for anonymity

#### User Management
* Available for admin-enabled staff accounts
* Can edit users and respond to admin messages
* View client appointment history and book/cancel appointments for clients

## Technology Stack - Uses the MVVM model
* Kotlin
* Android Jetpack
* Firebase Realtime Database
* Firebase Authentication
* Firebase Cloud Functions

