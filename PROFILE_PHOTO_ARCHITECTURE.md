# Profile Photo Feature - Architecture & Decision

## Feature Overview

Profile photo upload feature has been successfully implemented in the Journal Backend API.

---

## Decision: Profile Photos stored in **USERS** (not Entries)

### Why USERS?

✅ **Logical Placement:**
- Profile photo is metadata about the user, not about entries/journals
- Each user has exactly ONE profile photo (not multiple)
- Profile photo belongs to user identity/profile, not entry data

✅ **Scalability:**
- User collection: ~thousands to millions
- Entries collection: ~billions (each user has many entries)
- Storing photo URL in User entity (1 reference per user) is efficient
- If stored in entries: would duplicate photo URL across thousands of entries

✅ **Query Performance:**
- Get user profile → 1 DB query (includes photo URL)
- Display entry → No need to fetch photo (not associated with entry)

✅ **Consistency:**
- One photo per user ✓
- Easy to update/delete (single place)
- User has firstName, lastName, profilePhotoUrl, email, etc. (all profile-related)

---

## Database Schema

### User Entity (MongoDB)
```java
@Document(collection = "users")
public class User {
    @Id
    private ObjectId id;
    
    private String userName;
    private String email;
    private String password;
    
    private String firstName;        // Added
    private String lastName;         // Added
    private String profilePhotoUrl;  // Added
    
    private List<String> roles;
    private List<entry> entries;
    private Boolean sentimentAnalysis;
}
```

### Directory Structure
```
project-root/
├── uploads/
│   ├── profile-photos/
│   │   ├── 507f1f77bcf86cd799439011_uuid1234.jpg
│   │   ├── 507f1f77bcf86cd799439012_uuid5678.png
│   │   └── ... (photos keyed by userId)
│   └── ... (other upload types in future)
```

---

## API Endpoints

### Profile Photo Management

| Endpoint | Method | Purpose | Auth |
|----------|--------|---------|------|
| `/api/v1/user/profile-photo/upload` | `POST` | Upload/update profile photo | ✅ Required |
| `/api/v1/user/profile` | `GET` | Get user profile with photo URL | ✅ Required |
| `/api/v1/user/profile-photo` | `DELETE` | Delete profile photo | ✅ Required |

### Data Access Endpoints (Already Existing)

| Endpoint | Method | Purpose | Auth |
|----------|--------|---------|------|
| `/api/v1/user` | `GET` | Get all users (includes photo URLs) | ✅ Required |
| `/api/v1/user/me` | `GET` | Get current user | ✅ Required |

---

## Classes/Files Created

### New Service
📄 `src/main/java/com/love/Backend/service/ProfilePhotoService.java`
- File upload/storage logic
- File validation (size, mime-type)
- File deletion logic
- Security checks (path traversal prevention)

### Updated Files
📝 `src/main/java/com/love/Backend/entity/User.java`
- Added `profilePhotoUrl: String`
- Added `firstName: String`
- Added `lastName: String`

📝 `src/main/java/com/love/Backend/controller/UserController.java`
- Added `uploadProfilePhoto()` endpoint
- Added `getProfile()` endpoint
- Added `deleteProfilePhoto()` endpoint
- Injected `ProfilePhotoService`

📝 `src/main/java/com/love/Backend/dto/response/UserResponseDTO.java`
- Added `profilePhotoUrl`
- Added `firstName`
- Added `lastName`

📝 `src/main/java/com/love/Backend/dto/request/UserRequestDTO.java`
- Added `firstName`
- Added `lastName`

📝 `src/main/java/com/love/Backend/service/UserEntryService.java`
- Updated `fullUserUpdate()` to include new fields in response
- Updated `partialUserUpdate()` to include new fields in response

📝 `src/main/resources/application.yml`
- Added multipart upload size limits (5MB per file, 10MB total)
- Added app.upload.dir configuration

### Documentation Files
📄 `PROFILE_PHOTO_FEATURE.md` - Full feature documentation
📄 `PROFILE_PHOTO_TESTING.md` - Testing guide with examples

---

## Feature Specifications

### Upload
- **Allowed formats:** JPEG, PNG, GIF, WebP
- **Max file size:** 5 MB
- **Storage location:** `./uploads/profile-photos/`
- **File naming:** `{userId}_{UUID}.{extension}`
- **Automatic cleanup:** Old photo deleted when new one uploaded

### Security
✅ JWT authentication required
✅ User can only upload for themselves (authenticated user)
✅ File path validation (prevents directory traversal)
✅ File type & size validation
✅ Unique naming (UUID prevents conflicts)

### Limitations
- One photo per user (by design)
- Photos stored locally (for production: integrate S3/Cloud Storage)
- Max 5 MB per file
- Only 4 image formats supported

---

## NOT in Entries

### Why we didn't add photos to entries:
❌ Entries are individual journal posts (many per user)
❌ Profile photo belongs to user, not to specific entry
❌ Would create data duplication
❌ Confusing UX: users would ask "whose photo is on this entry?"
❌ Performance: storing photo URL in billions of entries wasteful

### Future: Add Images/Attachments to Entries
If you want to add photos/attachments to entries later:
- Create separate `EntryAttachment` entity with foreign key to `entry`
- Add multi-file support
- Store in `uploads/entry-attachments/`

---

## API Response Examples

### Upload Success
```json
{
  "message": "Profile photo uploaded successfully",
  "photoUrl": "/uploads/profile-photos/507f1f77bcf86cd799439011_a1b2c3d4.jpg"
}
```

### Get Profile with Photo
```json
{
  "userName": "john_doe",
  "email": "john@example.com",
  "firstName": "John",
  "lastName": "Doe",
  "roles": ["USER"],
  "profilePhotoUrl": "/uploads/profile-photos/507f1f77bcf86cd799439011_a1b2c3d4.jpg"
}
```

### Profile After Photo Deletion
```json
{
  "userName": "john_doe",
  "email": "john@example.com",
  "firstName": "John",
  "lastName": "Doe",
  "roles": ["USER"],
  "profilePhotoUrl": null
}
```

---

## How to Test

1. **Register user** (with firstName/lastName):
```bash
POST /api/v1/auth/register
Body: { firstName, lastName, email, userName, password }
```

2. **Upload profile photo**:
```bash
POST /api/v1/user/profile-photo/upload
Headers: Authorization: Bearer {JWT_TOKEN}
Body: multipart/form-data with file
```

3. **Get profile with photo**:
```bash
GET /api/v1/user/profile
Headers: Authorization: Bearer {JWT_TOKEN}
```

4. **Access photo via HTTP**:
```
GET http://localhost:8080/uploads/profile-photos/{filename}
```

5. **Delete photo**:
```bash
DELETE /api/v1/user/profile-photo
Headers: Authorization: Bearer {JWT_TOKEN}
```

See `PROFILE_PHOTO_TESTING.md` for detailed test cases and examples.

---

## Future Enhancements

### Short Term
- [ ] Add AWS S3 integration (cloud storage instead of local files)
- [ ] Add image crop/resize functionality
- [ ] Add default profile picture if none uploaded
- [ ] Add profile photo in user list endpoint

### Medium Term
- [ ] Add photo approval workflow for admin
- [ ] Add photo analytics (view count, etc.)
- [ ] Support multiple photo formats (SVG, WebP with fallback)
- [ ] Add image optimization/compression on upload

### Long Term
- [ ] Add CDN support for photos
- [ ] Add photo versioning/history
- [ ] Add photo sharing/gallery features
- [ ] Entry attachments/photos (separate feature)

---

## Summary

✅ Profile photo feature implemented cleanly in User entity
✅ One photo per user (by design)
✅ Secure file upload with validation
✅ Automatic cleanup of old photos
✅ Easy to extend with S3/cloud storage
✅ Fully documented with tests

**Answer: Photos are in USERS, NOT in ENTRIES** — because profile photo is user metadata, not entry content.

