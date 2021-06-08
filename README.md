# SOLUTIF : Solusi Lalu Lintas Alternatif

An application for monitoring street condition or road traffic condition such as road potholes, fallen trees, etc.

![](assets/Thumbnail.png)

## Table of Contents

- [Introduction](#introduction)

- [Quick Steps](#quick-steps)

- [Features & Screenshots](#features--screenshots)

- [Tech Stack](#tech-stack)

- [Dependencies](#dependencies)

- [Download the App](#download-the-app)

  

## Introduction

SOLUTIF is an application for monitoring street condition or road traffic condition such as road potholes, fallen trees, etc. The application will be used by the transportation service officer that works in the road. They submit the condition report, and the officer who responsible to solve the problem will go to the place that been reported before and solve the problem. After the problem solved, the reported condition will be checked as solved on the reported log. This system expected to reduce traffic congestion that can happen because of the road traffic problem.

Using the MVVM, Repository pattern, Dependency Injection, and Modularization architecture to make code easier to read and maintain, and use several Firebase services such as Authentication to simplify user management, Cloud Firestore to get the latest data quickly, and Storage to manage the storage of each photo report.

For third-party libraries this project uses the Mapbox API, besides being easy to use, the API can also be used for free (Pay-as-you-go). The API is used to display the map to make it more interactive.



## Quick Steps

Start by adding the required dependencies such as Firebase services, Kotlin Coroutines, Mapbox, and others. After that proceed with integrating and activating several Firebase services in the project, such as Authentication, Cloud Firestore, and Storage. Once integrated, the next step is to create designs and layouts for Splash Screen, registration, login, create navigation for the homepage, and also profile layouts.

Once the layout creation process is complete, it's time to create a function for each activity so that it can connect to the firebase service. Before that, we need to convert this Task into a coroutine function that will wait for the query answer and emit a Result object with the appropriate data.

So what we gonna do is to create an extension function for the Task object, called await(), what will return:

- **Result.Success:** the data if the query was successful
- **Result.Error:** the error message if an error happened
- **Result.Canceled:** an eventual error message if the query was canceled.

After that, we will create a data model for each data that we will receive and send, such as User data class and Report data class.

We are going to declare all the methods of our repository in an interface. Then we do an implementation for our application with the real Firebase queries.

Before we apply this method we will create some member variables to initialize which firebase function to use. Here we use KTX, thus shortening the syntax we will write. An example would be like this:

```kotlin
// Without KTX
private val firestore = FirebaseFirestore.getInstance()
// With KTX
private val firestoreInstance = Firebase.firestore
```

Jika fungsi firebase yang dibutuhkan sudah di inisialisasi, saatnya mengimplementasikan fungsi yang telah dibuat pada repository.

If you've read about MVVM with Android, usually in the Repository there is a Model and Remote Data Source for managing data. But this time will be different, with Cloud Firestore, we can remove the last two parts of it. This is because Firestore provides its own local cache. This means we can remove the model and remote data source and combine them in a single repository class.

Once the function is implemented in the Repository, it's time to create a ViewModel which will be responsible to get the data from the repository and notify the user interface about the changes and vice versa. Once all the ViewModels for each required activity have been created, it's time for us to add dependency injection, here we will use Coin as it is more concise and straightforward than others.

When displaying a list of reports, we usually use the RecyclerView, but here we use the Firebase Firestore UI library so that the data is easy to manage and the UI will be more interactive, for example, if data is deleted or new data is created, the page will automatically update. Besides being interactive, the library is also very easy to use.

After all business processes have been completed, we will change the project that was previously monolith (one module) into many modules. In this case, we will add a new module called Core, this modularization process serves to clearly divide the code according to its purpose and speed up build times.

And last but not least, we added Obfuscation to avoid reverse engineering and reduce the size of the application.

## Features & Screenshots

| Name          | Screenshot                                      | Description                                                  |
| ------------- | ----------------------------------------------- | ------------------------------------------------------------ |
| Splash Screen | <img src="assets/splashscreen.jpg" width="360"> | Just an ordinary Splash Screen with a logo in the center of the screen. |
| Login         | <img src="assets/login.jpg" width="360">        | Login with your registered email and password. This feature is used for authentication and distinguishing the roles of reporter and operator. |
| Register      | <img src="assets/register.jpg" width="360">     | To register a user in order to login. User can choose to be "Operator" or "Reporter |
| Home/Maps     | <img src="assets/maps.jpg" width="360">         | The page that will appear after the user logs in. The map shows where the report was reported. If one of the reports is pressed, it will immediately be directed to the detail feature. |
| Reports       | <img src="assets/reports.jpg" width="360">      | Displays all reports in the list, if one of the reports is pressed, it will immediately be directed to the detail page. Report data will automatically change when new reports are added or deleted. |
| Details       | <img src="assets/details.jpg" width="360">      | The page that displays the report details, there are photos, descriptions, completion status, and the location of the report is made in the form of a map.<br/>If the logged in user is an operator, then that user can go to the location using navigation and can change the completed status. |
| Navigation    | <img src="assets/navigation.jpg" width="360">   | A navigation feature that will direct the user to where the report is reported. This feature can only be used by operator. |
| Profile       | <img src="assets/profile.jpg" width="360">      | To display the data of users who are currently logged in and there is a button to logout. |



## Tech Stack

- MVVM (Model-View-ViewModel) Architecture Pattern
- Modularization (core module)
- Dependency Injection with Koin
- ViewBinding
- Obfuscation with Proguard



## Dependencies

- [Mapbox](https://docs.mapbox.com/help/tutorials/first-steps-android-sdk/)
- [Glide](https://github.com/bumptech/glide)
- [AndroidX](https://mvnrepository.com/artifact/androidx)
- [Cardview](https://developer.android.com/jetpack/androidx/releases/cardview)
- [Firebase](https://firebase.google.com/)
  - [Authentication](https://firebase.google.com/docs/auth/android/start)
  - [Cloud Firestore](https://firebase.google.com/docs/firestore/quickstart)
    - [Firestore UI](https://firebaseopensource.com/projects/firebase/firebaseui-android/firestore/readme/)
  - [Cloud Storage](https://firebase.google.com/docs/storage/android/start)
- [Lifecycle & LiveData](https://developer.android.com/jetpack/androidx/releases/lifecycle)
- [Koin](https://github.com/InsertKoinIO/koin)
- [Leak Canary](https://github.com/square/leakcanary)



## Download the App

[Download](https://drive.google.com/file/d/1QH2CZOG1MheUGE1aztkUSa_f0Liy0kG-/view?usp=sharing)