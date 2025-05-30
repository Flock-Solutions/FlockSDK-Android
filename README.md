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
        name = "Jane Doe"
    )
}
```

---

### 4. Launch the Referral Flow

Show your users a full-screen referral experience with a single call:

```kotlin
FlockSDK.openPage(context, "referrer") // or "invitee" for invitees
```

#### React to events

Subscribe to events when users enter a valid referral code, hit an error, or close the modal:

```kotlin
FlockSDK.openPage(context, "referrer", object : FlockWebViewCallback {
    override fun onClose() { /* User closed the modal */ }
    override fun onSuccess() { /* Invitee entered valid referral code */ }
    override fun onInvalid() { /* Invitee entered invalid referral code */ }
})
```

---

### 5. Control the Referral Flow from Native

You can programmatically tell the Flock modal to navigate to a different page (if it’s open):

```kotlin
FlockSDK.navigate("invitee?state=success")
```

This is particularly useful when you want to navigate to the success screen after the invitee enters a valid referral code.

```kotlin
FlockSDK.openPage(context, "invitee", object : FlockWebViewCallback {
    override fun onClose() {}
    override fun onSuccess() {
        FlockSDK.navigate("invitee?state=success")
    }
    override fun onInvalid() {}
})
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
