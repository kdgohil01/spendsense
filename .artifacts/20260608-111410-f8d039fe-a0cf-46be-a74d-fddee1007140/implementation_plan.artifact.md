# Implementation Plan - Fix Sync Hang and Status Update

The app's sync process hangs indefinitely when the Cloud Firestore API is disabled or the network is unstable, because the Firestore `await()` call retries indefinitely without a timeout. This also prevents the sync status from updating in the UI.

## Proposed Changes

### Data Layer

#### [SyncManager.kt](file:///K:/1.Projects/SpendSense/app/src/main/java/com/spendsense/app/data/SyncManager.kt)

- Wrap Firestore `await()` calls in `withTimeout` to prevent indefinite hangs.
- Add missing imports for `withTimeout`.
- Improve the `restoreFromCloud` logic to safely handle potential nulls or malformed data from Firestore.

```kotlin
    suspend fun syncWithCloud() = withContext(Dispatchers.IO) {
        // ...
        withTimeout(15000) { // 15-second timeout for upload
            firestore.collection("users").document(userId)
                .collection("expenses")
                .document("backup")
                .set(mapOf("data" to payload))
                .await()
        }
        // ...
    }
```

---

### UI Layer

#### [SpendSenseViewModel.kt](file:///K:/1.Projects/SpendSense/app/src/main/java/com/spendsense/app/ui/SpendSenseViewModel.kt)

- Clean up code by adding imports for `SyncManager` and `FirebaseUser`, removing full-path references.
- Ensure that sync errors (including timeouts) are captured and the `_isSyncing` state is always reset (already handled by `finally`, but will now be triggered by the timeout).

---

## Verification Plan

### Automated Tests
- I will attempt to run existing unit tests if any are relevant.
- I will check if there are tests for `SyncManager` or `SpendSenseViewModel`.

### Manual Verification
- I will use `read_logcat` after the changes to ensure that when a sync is triggered, it either succeeds or hits the timeout and logs the failure, instead of hanging.
- I will verify that `_lastSyncTime` updates in the UI (by checking the UI state if possible, or by looking at logs).
- I will verify that the "circle animation" (CircularProgressIndicator) stops after the timeout.
