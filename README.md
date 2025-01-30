## HOW TO RUN
Here’s a structured **README.md** for your Android project. Copy and paste this into a `README.md` file in your project directory.

---

# **ProductApp - Android Assignment**

### **📌 Overview**
This is an Android application built using **MVVM architecture**, **Retrofit**, **KOIN**, and **Kotlin Coroutines**. The app allows users to **fetch and add products** using a REST API.

---

## **🚀 Features**
✔ Fetch product list from an API  
✔ Add new products  
✔ Modern MVVM architecture  
✔ Uses **Retrofit** for networking  
✔ **KOIN** for Dependency Injection  
✔ **Coroutines & LiveData** for async operations  
✔ **Jetpack Components** (ViewModel, Navigation, Room)

---

## **📂 Project Structure**
```
app/
 ├── src/main/java/com/example/productapp/
 │   ├── model/            # Data classes (Product.kt)
 │   ├── repository/       # Repository (ProductRepository.kt)
 │   ├── viewmodel/        # ViewModel (ProductViewModel.kt)
 │   ├── ui/               # UI Screens (Fragments)
 │   ├── di/               # Dependency Injection (KOIN)
 │   ├── adapter/          # RecyclerView Adapter
 │   ├── network/          # Retrofit API Service
 ├── res/layout/           # XML layouts
 ├── AndroidManifest.xml
```

---

## **🛠 Tech Stack**
- **Language:** Kotlin
- **Architecture:** MVVM
- **Networking:** Retrofit + Gson
- **Dependency Injection:** KOIN
- **Asynchronous Tasks:** Kotlin Coroutines + LiveData
- **UI Components:** RecyclerView, Jetpack Navigation

---

## **📦 Dependencies**
Add these dependencies in `app/build.gradle.kts`:

```kotlin
dependencies {
    // Core
    implementation("androidx.core:core-ktx:1.9.0")
    implementation("androidx.appcompat:appcompat:1.6.1")

    // MVVM Components
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.6.2")
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.6.2")

    // Networking (Retrofit & Gson)
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")

    // Dependency Injection (KOIN)
    implementation("io.insert-koin:koin-android:3.1.2")

    // Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.6.4")

    // RecyclerView & Glide
    implementation("androidx.recyclerview:recyclerview:1.3.2")
    implementation("com.github.bumptech.glide:glide:4.12.0")

    // Room Database (For offline storage)
    implementation("androidx.room:room-runtime:2.4.2")
    kapt("androidx.room:room-compiler:2.4.2")
}
```

---

## **🔧 How to Set Up & Run the Project**

### **1️⃣ Clone the Repository**
```sh
git clone https://github.com/yourusername/ProductApp.git
cd ProductApp
```

### **2️⃣ Open in Android Studio**
- Open **Android Studio**
- Click on **Open Project**
- Select the `ProductApp` folder

### **3️⃣ Sync Gradle**
- Go to **File > Sync Project with Gradle Files**
- Wait for dependencies to download

### **4️⃣ Set up KOIN for Dependency Injection**
Create a file: 📂 `di/AppModule.kt`

```kotlin
package com.example.productapp.di

import com.example.productapp.repository.ProductRepository
import com.example.productapp.viewmodel.ProductViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val appModule = module {
    single { ProductRepository() }
    viewModel { ProductViewModel(get()) }
}
```

Initialize KOIN in **`MainApplication.kt`**:
```kotlin
package com.example.productapp

import android.app.Application
import com.example.productapp.di.appModule
import org.koin.core.context.startKoin

class MainApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            modules(appModule)
        }
    }
}
```
Don’t forget to declare `MainApplication` in `AndroidManifest.xml`:
```xml
<application
    android:name=".MainApplication"
    ...
```

### **5️⃣ Run the App**
- **Connect a device or emulator**
- Click ▶️ **Run** in Android Studio

---

## **📜 API Details**
**Base URL:**
```
https://app.getswipe.in/api/
```

| **Endpoint**  | **Method** | **Description** |
|--------------|-----------|----------------|
| `/public/get` | `GET` | Fetches a list of products |
| `/public/add` | `POST` | Adds a new product |

---

## **🎯 Next Steps**
- ✅ Implement Room Database for offline storage
- ✅ Improve UI with Material Design
- ✅ Add Pagination for large product lists

---

## **📌 Contact & Support**
If you have any questions, feel free to **open an issue** or reach out. 🚀

---

This **README** covers everything you need to build, run, and understand the project. Let me know if you need any **modifications or additional sections**! 😊

