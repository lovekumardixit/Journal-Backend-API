# Profile Photo Feature - Testing Guide

## Quick Testing Steps (Using Postman / cURL / Browser)

### Prerequisites
1. Application running on `http://localhost:8080`
2. User registered and authenticated
3. JWT token obtained from login endpoint

---

## Test Scenario 1: Register a New User

**Endpoint:** `POST /api/v1/auth/register`

```bash
curl -X POST http://localhost:8080/api/v1/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "firstName": "John",
    "lastName": "Doe",
    "email": "john@example.com",
    "userName": "johndoe",
    "password": "SecurePass123@"
  }'
```

**Expected Response (200 OK):**
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "refreshToken": "...",
  "expiresIn": 3600000
}
```

Save the `token` for next steps.

---

## Test Scenario 2: Upload Profile Photo

**Endpoint:** `POST /api/v1/user/profile-photo/upload`

### Using cURL (Linux/Mac/WSL):
```bash
TOKEN="your_jwt_token_here"
curl -X POST http://localhost:8080/api/v1/user/profile-photo/upload \
  -H "Authorization: Bearer $TOKEN" \
  -F "file=@/path/to/your/photo.jpg"
```

### Using PowerShell (Windows):
```powershell
$token = "your_jwt_token_here"
$filePath = "C:\path\to\photo.jpg"
$headers = @{
    "Authorization" = "Bearer $token"
}
$form = @{
    file = Get-Item -Path $filePath
}
$response = Invoke-RestMethod -Uri "http://localhost:8080/api/v1/user/profile-photo/upload" `
    -Method Post `
    -Headers $headers `
    -Form $form
$response | ConvertTo-Json
```

### Using Postman:
1. Set method to `POST`
2. URL: `http://localhost:8080/api/v1/user/profile-photo/upload`
3. Headers tab: Add `Authorization: Bearer YOUR_JWT_TOKEN`
4. Body tab: Select `form-data`
5. Add key `file` with type `File` and select your image file
6. Click Send

**Successful Response (200 OK):**
```json
{
  "message": "Profile photo uploaded successfully",
  "photoUrl": "/uploads/profile-photos/507f1f77bcf86cd799439011_abc123def456.jpg"
}
```

**Error Response - File Too Large (400 Bad Request):**
```json
{
  "error": "Profile photo size must not exceed 5 MB"
}
```

**Error Response - Invalid File Type (400 Bad Request):**
```json
{
  "error": "Only JPEG, PNG, GIF, and WebP images are allowed. Uploaded: image/bmp"
}
```

---

## Test Scenario 3: Get User Profile with Photo

**Endpoint:** `GET /api/v1/user/profile`

```bash
TOKEN="your_jwt_token_here"
curl -X GET http://localhost:8080/api/v1/user/profile \
  -H "Authorization: Bearer $TOKEN"
```

**Expected Response (200 OK):**
```json
{
  "userName": "johndoe",
  "email": "john@example.com",
  "firstName": "John",
  "lastName": "Doe",
  "roles": ["USER"],
  "profilePhotoUrl": "/uploads/profile-photos/507f1f77bcf86cd799439011_abc123def456.jpg"
}
```

---

## Test Scenario 4: Access Profile Photo via HTTP

Once you have the `photoUrl` from the profile endpoint, you can access it directly:

```
http://localhost:8080/uploads/profile-photos/507f1f77bcf86cd799439011_abc123def456.jpg
```

**Expected:** Image displays in browser or downloads

---

## Test Scenario 5: Update Profile Photo (Replace Existing)

Repeat **Test Scenario 2** with a different image. Old photo should be automatically deleted.

```bash
TOKEN="your_jwt_token_here"
curl -X POST http://localhost:8080/api/v1/user/profile-photo/upload \
  -H "Authorization: Bearer $TOKEN" \
  -F "file=@/path/to/new_photo.png"
```

**Expected Response:**
```json
{
  "message": "Profile photo uploaded successfully",
  "photoUrl": "/uploads/profile-photos/507f1f77bcf86cd799439011_newuuid1234.png"
}
```

Verify that:
1. Old photo URL is gone from directory
2. New photo URL is returned
3. Old photo is no longer accessible via HTTP

---

## Test Scenario 6: Delete Profile Photo

**Endpoint:** `DELETE /api/v1/user/profile-photo`

```bash
TOKEN="your_jwt_token_here"
curl -X DELETE http://localhost:8080/api/v1/user/profile-photo \
  -H "Authorization: Bearer $TOKEN"
```

**Expected Response (204 No Content):**
- No response body

Verify:
1. Photo URL is removed from user profile
2. GET `/api/v1/user/profile` should now return `profilePhotoUrl: null`
3. Photo file no longer accessible via HTTP (404)

---

## Test Scenario 7: Get Profile After Photo Deletion

```bash
TOKEN="your_jwt_token_here"
curl -X GET http://localhost:8080/api/v1/user/profile \
  -H "Authorization: Bearer $TOKEN"
```

**Expected Response (200 OK):**
```json
{
  "userName": "johndoe",
  "email": "john@example.com",
  "firstName": "John",
  "lastName": "Doe",
  "roles": ["USER"],
  "profilePhotoUrl": null
}
```

---

## Edge Case Tests

### Test 1: Upload Without Authentication
**Endpoint:** `POST /api/v1/user/profile-photo/upload` (without Authorization header)

**Expected Response (401 Unauthorized):**
```json
{
  "error": "Unauthorized"
}
```

### Test 2: Upload Empty File
**Expected Response (400 Bad Request):**
```json
{
  "error": "Profile photo file is required"
}
```

### Test 3: Upload File > 5 MB
**Expected Response (400 Bad Request):**
```json
{
  "error": "Profile photo size must not exceed 5 MB"
}
```

### Test 4: Upload Non-Image File (e.g., .txt, .pdf)
**Expected Response (400 Bad Request):**
```json
{
  "error": "Only JPEG, PNG, GIF, and WebP images are allowed. Uploaded: text/plain"
}
```

### Test 5: Access Deleted Photo URL
After deleting a photo, try to access the old URL:
```
http://localhost:8080/uploads/profile-photos/507f1f77bcf86cd799439011_old_uuid.jpg
```

**Expected Response (404 Not Found):**
- Static resource not found

---

## Verification Checklist

After all tests, verify:

- [x] File uploads are stored in `./uploads/profile-photos/`
- [x] Old photos are deleted when new photo is uploaded
- [x] Photos are accessible via HTTP at `/uploads/profile-photos/filename`
- [x] User profile endpoint returns correct photo URL
- [x] Photo deletion removes both file and DB reference
- [x] File size and type validation work correctly
- [x] Authentication is required for upload/delete
- [x] User can only modify their own photos (not others')

---

## Sample Test Images (for manual testing)

You can generate small test images using online tools or use these:
- JPEG: 10 KB at https://www.example.com/sample.jpg
- PNG: 5 KB at https://www.pngimg.com/download/33217
- GIF: 2 KB at https://gifimages.com/

---

## Integration with Frontend

### HTML Form Example:
```html
<form id="photoForm" enctype="multipart/form-data">
  <input type="file" id="photoFile" name="file" accept="image/*" required />
  <button type="submit">Upload Photo</button>
  <div id="message"></div>
  <img id="previewImage" style="max-width: 200px; display: none;">
</form>

<script>
document.getElementById('photoForm').addEventListener('submit', async (e) => {
  e.preventDefault();
  
  const file = document.getElementById('photoFile').files[0];
  const token = localStorage.getItem('jwtToken');
  
  const formData = new FormData();
  formData.append('file', file);
  
  try {
    const response = await fetch('/api/v1/user/profile-photo/upload', {
      method: 'POST',
      headers: { 'Authorization': `Bearer ${token}` },
      body: formData
    });
    
    if (response.ok) {
      const data = await response.json();
      document.getElementById('message').textContent = data.message;
      document.getElementById('previewImage').src = data.photoUrl;
      document.getElementById('previewImage').style.display = 'block';
    } else {
      const error = await response.json();
      document.getElementById('message').textContent = `Error: ${error.error}`;
    }
  } catch (error) {
    document.getElementById('message').textContent = `Upload failed: ${error.message}`;
  }
});
</script>
```

---

## Troubleshooting

| Issue | Solution |
|-------|----------|
| 401 Unauthorized | Check JWT token is valid and not expired |
| 404 Not Found on photo URL | Photo might have been deleted or path is incorrect |
| 413 Payload Too Large | File size exceeds 5 MB limit |
| 415 Unsupported Media Type | File type not supported (use JPEG, PNG, GIF, or WebP) |
| Photo not persisting | Check `uploads/profile-photos/` directory exists and has write permissions |
| Multiple photos for same user | By design, each user has only one profile photo (old one is deleted on upload) |


