[![](https://jitpack.io/v/Flock-Solutions/FlockSDK-Android.svg)](https://jitpack.io/#Flock-Solutions/FlockSDK-Android)

# Flock Android SDK

Flock helps your mobile app grow with powerful, plug-and-play referral experiences. We’re a SaaS platform focused on making it dead-simple to add referral flows to your Android app—no custom UI, no backend headaches. Just drop in the SDK and let your users invite, refer, and get rewarded!

---

## 🚀 Quick Start

### 1. Add Flock to Your Project

Add the SDK to your `build.gradle`:

```groovy
implementation 'com.withflock:flocksdk:YOUR_VERSION'
```

---

### 2. Initialize Flock

Call this once, early (e.g., in `Application` or your launcher Activity):

```kotlin
FlockSDK.initialize(
    publicAccessKey = "YOUR_PUBLIC_KEY",
    environment = FlockEnvironment.PRODUCTION // or FlockEnvironment.TEST
)
```

---

### 3. Identify Your User

Let Flock know who your user is, so referrals are tracked correctly:

```kotlin
CoroutineScope(Dispatchers.Main).launch {
    FlockSDK.identify(
        externalUserId = "user-123",
        email = "user@email.com",
        name = "Jane Doe",
        customProperties = mapOf(
            "tier" to "pro",
            "subscriptionType" to "annual"
        )
    )
}
```

---

### 4. Trigger Checkpoints

Trigger checkpoints to show Flock experiences at specific moments in your user journey:

```kotlin
// Simple checkpoint trigger
FlockSDK.checkpoint("refer_button_clicked").trigger(context)

// With callbacks
FlockSDK.checkpoint("refer_button_clicked")
    .onClose {
        Log.d("Flock", "Checkpoint closed")
    }
    .onSuccess {
        Log.d("Flock", "Checkpoint succeeded")
    }
    .onInvalid {
        Log.d("Flock", "Checkpoint invalid")
    }
    .trigger(context)

// Checkpoint with navigation in success callback
FlockSDK.checkpoint("user_onboarded")
    .onSuccess {
        // Navigate to success screen when invitee enters valid referral code
        FlockSDK.checkpoint("referral_succeeded").navigate().trigger(context)
    }
    .trigger(context)
```

---

## 🛠️ Why Flock?

- **No UI to build** – We handle the referral screens for you.
- **No backend to maintain** – Flock manages referral logic, tracking, and rewards.
- **Production-ready** – Full-screen, mobile-optimized, and easy to integrate.

---

## 🧩 Requirements

- Android SDK 24+
- Kotlin project

---

## 📝 License

MIT

---

## 💬 Need Help?

Open an issue or email [support@withflock.com](mailto:support@withflock.com).

---
