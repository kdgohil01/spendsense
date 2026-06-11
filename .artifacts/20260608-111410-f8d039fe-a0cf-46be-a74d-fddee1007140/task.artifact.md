# Task Management

- [x] Research sync implementation and identify the hang issue
- [/] Fix sync hang and improve error handling
    - [ ] Update `SyncManager.kt` with timeouts and resilient data restoration
    - [ ] Clean up `SpendSenseViewModel.kt` imports and ensure error propagation
- [ ] Verify the fix
    - [ ] Check logs for timeout/error messages
    - [ ] Verify `isSyncing` state resets correctly
