
### 6. Pixel Now Playing Integration

**Decision**: Access via NotificationListenerService to read Now Playing notifications

**Rationale**:
- Pixel's "Now Playing" displays detected songs as a notification from com.google.android.as
- The notification contains song and artist in the title field formatted as "Song Name by Artist Name"
- NotificationListenerService provides real-time updates when songs are detected
- No content provider access is available or needed
- Requires user to grant notification access permission

**Implementation Strategy**:
1. Create NotificationListenerService to listen for notifications from com.google.android.as
2. Filter for notification ID 123 (the Now Playing notification)
3. Extract title from notification extras which contains "Song Name by Artist Name"
4. Parse the title to separate song name and artist
5. Emit updates via StateFlow for reactive UI updates
6. Handle cases where notification is removed (no music detected)

**Notification Structure**:
```
Package: com.google.android.as
ID: 123
Extras:
  - android.title: "Song Name by Artist Name"
  - android.text: "Tap to see your song history"
  - android.substName: "Now Playing"
```

**Permission Required**:
- BIND_NOTIFICATION_LISTENER_SERVICE (declared in AndroidManifest)
- User must manually enable notification access in system settings:
  Settings > Apps > Special app access > Notification access

**Fallback Strategy**:
- Check if NotificationListenerService is enabled
- Display unavailable state if permission not granted
- No crash on non-Pixel devices
- Gracefully handle missing notifications

**Alternatives Considered**:
- Content provider (content://com.google.android.as): Rejected, provider does not exist or is not queryable
- Media session API: Rejected, only for actively playing media
- Third-party Shazam API: Rejected, doesn't access Pixel's local data
