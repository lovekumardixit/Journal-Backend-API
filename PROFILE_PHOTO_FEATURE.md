# Profile Photo Upload Feature

## Overview
Users can now upload, update, and delete their profile photos. Profile photos are stored locally in the `uploads/profile-photos/` directory and served via HTTP.

## API Endpoints

### 1. Upload Profile Photo
**Endpoint:** `POST /api/v1/user/profile-photo/upload`

**Authentication:** Required (Bearer token)

**Request:**
- Content-Type: `multipart/form-data`
- Parameter: `file` (multipart file)

**Allowed file types:** JPEG, PNG, GIF, WebP
**Max file size:** 5 MB

**Example using curl:**
```bash
curl -X POST http://localhost:8080/api/v1/user/profile-photo/upload \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -F "file=@/path/to/your/photo.jpg"
```

**Example using PowerShell:**
```powershell
$file = "C:\path\to\photo.jpg"
$headers = @{
    "Authorization" = "Bearer YOUR_JWT_TOKEN"
}
$form = @{
    file = Get-Item -Path $file
}
Invoke-RestMethod -Uri "http://localhost:8080/api/v1/user/profile-photo/upload" `
  -Method Post `
  -Headers $headers `
  -Form $form
```

**Successful Response (200 OK):**
```json
{
  "message": "Profile photo uploaded successfully",
  "photoUrl": "/uploads/profile-photos/507f1f77bcf86cd799439011_abc123def456.jpg"
}
```

**Error Responses:**
- `400 Bad Request`: File is empty, file too large, or invalid file type
- `401 Unauthorized`: No valid JWT token provided
- `404 Not Found`: User not found

---

### 2. Get User Profile with Photo
**Endpoint:** `GET /api/v1/user/profile`

**Authentication:** Required (Bearer token)

**Example using curl:**
```bash
curl -X GET http://localhost:8080/api/v1/user/profile \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

**Successful Response (200 OK):**
```json
{
  "userName": "john_doe",
  "email": "john.doe@example.com",
  "firstName": "John",
  "lastName": "Doe",
  "roles": ["USER"],
  "profilePhotoUrl": "/uploads/profile-photos/507f1f77bcf86cd799439011_abc123def456.jpg"
}
```

---

### 3. Delete Profile Photo
**Endpoint:** `DELETE /api/v1/user/profile-photo`

**Authentication:** Required (Bearer token)

**Example using curl:**
```bash
curl -X DELETE http://localhost:8080/api/v1/user/profile-photo \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

**Successful Response (204 No Content):**
- No response body

**Error Responses:**
- `401 Unauthorized`: No valid JWT token provided
- `404 Not Found`: User not found

---

## How It Works

1. **Upload**: When a user uploads a profile photo:
   - File validation is performed (size, mime type)
   - Old photo (if exists) is automatically deleted
   - New photo is stored in `uploads/profile-photos/` directory
   - Photo URL is saved in the User entity in MongoDB
   - Photo is served via `/uploads/profile-photos/` endpoint

2. **Storage**: Photos are stored with naming pattern:
   - Format: `{userId}_{uuid}.{extension}`
   - Example: `507f1f77bcf86cd799439011_abc123def456.jpg`

3. **Access**: Photos can be accessed via HTTP:
   - `http://localhost:8080/uploads/profile-photos/507f1f77bcf86cd799439011_abc123def456.jpg`
   - Or get the URL from user profile endpoint

---

## Configuration

Add to `application.yml` (already configured):
```yaml
spring:
  servlet:
    multipart:
      max-file-size: 5MB
      max-request-size: 10MB

app:
  upload:
    dir: uploads
```

---

## Security Features

1. **File Validation**: Only image files (JPEG, PNG, GIF, WebP) allowed
2. **File Size Limit**: 5 MB max per file
3. **User Isolation**: Each user can only upload/delete their own photo
4. **Path Traversal Prevention**: File paths are validated and normalized
5. **Unique Naming**: UUID-based naming prevents filename conflicts
6. **Old Photo Cleanup**: Previous photos are automatically deleted when new photo is uploaded

---

## Example Frontend Integration (HTML/JavaScript)

```html
<form id="photoForm" enctype="multipart/form-data">
  <input type="file" id="photoInput" name="file" accept="image/*" />
  <button type="submit">Upload Photo</button>
</form>

<script>
const form = document.getElementById('photoForm');
form.addEventListener('submit', async (e) => {
  e.preventDefault();
  
  const fileInput = document.getElementById('photoInput');
  const formData = new FormData();
  formData.append('file', fileInput.files[0]);
  
  const token = localStorage.getItem('jwtToken'); // Your JWT token
  
  try {
    const response = await fetch('/api/v1/user/profile-photo/upload', {
      method: 'POST',
      headers: {
        'Authorization': `Bearer ${token}`
      },
      body: formData
    });
    
    if (response.ok) {
      const data = await response.json();
      console.log('Photo uploaded:', data.photoUrl);
      // Update profile image on page
      document.getElementById('profileImage').src = data.photoUrl;
    } else {
      console.error('Upload failed:', response.statusText);
    }
  } catch (error) {
    console.error('Error:', error);
  }
});
</script>
```

---

## Testing with Postman

1. **Authentication**: Login first to get JWT token
2. **Upload Photo**:
   - Method: `POST`
   - URL: `http://localhost:8080/api/v1/user/profile-photo/upload`
   - Headers: `Authorization: Bearer <your_jwt_token>`
   - Body: Form-data with key `file` and value as image file
3. **Get Profile**: 
   - Method: `GET`
   - URL: `http://localhost:8080/api/v1/user/profile`
   - Headers: `Authorization: Bearer <your_jwt_token>`
4. **Delete Photo**:
   - Method: `DELETE`
   - URL: `http://localhost:8080/api/v1/user/profile-photo`
   - Headers: `Authorization: Bearer <your_jwt_token>`

---

## Storage Directory Structure

```
project-root/
├── uploads/
│   ├── profile-photos/
│   │   ├── 507f1f77bcf86cd799439011_abc123def456.jpg
│   │   ├── 507f1f77bcf86cd799439012_xyz789abc123.png
│   │   └── ...
│   └── ... (other uploads)
```

---

## Notes

- Profile photos are stored locally; for production, consider using AWS S3 or similar cloud storage
- Old photos are automatically cleaned up when new photos are uploaded
- Photos are deleted when user deletes the profile photo endpoint
- Photo URLs are relative paths; prepend your domain when sharing externally

