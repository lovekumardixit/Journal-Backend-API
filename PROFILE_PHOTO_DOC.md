# Profile Photo Upload - Design, Usage, Postman & S3 Integration

Checklist (what I'll cover)
- [x] How a user selects and uploads a photo from a client (web/mobile)
- [x] How to upload using Postman (step-by-step)
- [x] Where the photo is stored now (local filesystem) and how it's served
- [x] How to switch to direct S3 upload (two options: backend upload or client direct via presigned URL)
- [x] Code snippets to implement S3 support (Spring Boot)
- [x] Configuration and security considerations

---

1) How the upload works in this project (high level)

- Frontend sends a multipart/form-data POST request to the backend endpoint:
  - Endpoint: `POST /user/profile-photo/upload`
  - Field name: `file`
  - Authorization: Bearer access token (endpoint requires authenticated user)
- Backend (`ProfilePhotoService`) validates file type & size, stores file and returns a URL path.
- Static resource mapping (`WebConfig`) exposes saved files at `/uploads/**` so the returned URL can be requested by clients.

Flow:
Client (file input) → POST /user/profile-photo/upload (multipart/form-data + Authorization) → server validates & saves → returns `/uploads/profile-photos/<file>` → client displays image using that URL

---

2) Where files are stored right now (current implementation)

- Storage location: filesystem under the configured `app.upload.dir` (default `uploads`) inside a `profile-photos` subdirectory.
- Default path when running locally (from project root): `./uploads/profile-photos/<userId>_<uuid>.<ext>`
- Returned URL from `ProfilePhotoService.uploadProfilePhoto(...)` is: `/uploads/profile-photos/<filename>`
- Static resource mapping (in `WebConfig`) maps `/uploads/**` to the absolute filesystem path so the photo is publicly reachable via the backend host:
  - Example: `http://localhost:8081/uploads/profile-photos/507f1f77bcf86cd799439011_abcd-uuid.jpg`

Files are created by `ProfilePhotoService.uploadProfilePhoto(...)` which:
- validates MIME type (jpeg/png/gif/webp)
- limits size to 5 MB
- creates directory `uploads/profile-photos`
- stores file as `{userId}_{UUID}.{ext}`
- returns `/uploads/profile-photos/{storedName}`

Important notes for production:
- If you run the app in Docker, mount a host volume to retain uploaded files across container restarts.
- If running on multiple instances, local filesystem approach will not be shared between instances — use shared storage (S3) or central file server.

---

3) How the client selects and uploads a photo

Web (HTML) example (simple):
```html
<form id="photoForm" enctype="multipart/form-data">
  <input type="file" name="file" accept="image/*" />
  <button type="submit">Upload</button>
</form>

<script>
  const form = document.getElementById('photoForm');
  form.addEventListener('submit', async (e) => {
    e.preventDefault();
    const data = new FormData(form);
    const token = localStorage.getItem('accessToken'); // your auth token
    const resp = await fetch('/user/profile-photo/upload', {
      method: 'POST',
      headers: {
        'Authorization': 'Bearer ' + token
      },
      body: data
    });
    const res = await resp.json();
    console.log(res);
  });
</script>
```

Mobile (Android / iOS): use native file pickers to get the image binary and send as multipart/form-data POST to same endpoint with Authorization header.

---

4) Using Postman to upload a profile photo (step-by-step)

1. Open Postman.
2. Select POST and set URL to (example): `http://localhost:8081/user/profile-photo/upload`
3. Add Authorization header: `Authorization: Bearer <ACCESS_TOKEN>` (the endpoint expects the authenticated user from security context).
4. In the "Body" tab, choose `form-data`.
5. Add a new key with name `file`, change type to `File` (not Text), and choose an image file from your disk.
6. Send the request.

Expected response:
- HTTP 200 and a JSON body (in `UserController.uploadProfilePhoto` the code returns a HashMap with message and photoUrl). Example response:
  ```json
  {
    "message": "Profile photo uploaded successfully",
    "photoUrl": "/uploads/profile-photos/<filename>.jpg"
  }
  ```

After that, you can view the image in browser:
```
http://localhost:8081/uploads/profile-photos/<filename>.jpg
```

Notes:
- If you are using MongoDB Atlas or a remote server, the backend host might be different; use the backend's base URL.
- Ensure the `Authorization` header contains a valid token; otherwise the controller will not find the authenticated user.

---

5) CURL example (quick test)

```bash
curl -X POST "http://localhost:8081/user/profile-photo/upload" \
  -H "Authorization: Bearer <ACCESS_TOKEN>" \
  -F "file=@/path/to/photo.jpg"
```

---

6) Switch to S3 storage — options & recommended approach

When running multiple backend instances or in production, prefer S3 (or similar object storage). Two main approaches:

A) Backend uploads the file to S3 (replace local save with S3 upload)
- Client behavior unchanged — POST to backend endpoint with file.
- Backend receives file and, instead of writing to disk, uploads to S3 and returns S3 public URL (or a proxied path).
- Simple for clients; easier to control validation and naming in backend.

B) Client uploads directly to S3 using a presigned upload URL (recommended for large scale / lower backend bandwidth)
- Client requests an upload URL from backend (small request, authenticated).
- Backend generates a presigned PUT or POST URL (short lived) and returns it.
- Client PUTs the file directly to S3 using the presigned URL.
- After upload, client notifies backend (or backend can infer filename) to save the file URL in user profile.
- Advantages: reduced backend bandwidth & latency, better scalability.

I'll provide code snippets for both options below (Spring Boot + AWS SDK v2).

---

7) Add AWS S3 dependency (pom.xml)

Add the AWS SDK S3 module (AWS SDK v2 recommended):

```xml
<dependency>
  <groupId>software.amazon.awssdk</groupId>
  <artifactId>s3</artifactId>
  <version>2.20.0</version>
</dependency>
```

(choose a recent stable version)

---

8) Option A: Backend uploads to S3 — sample `ProfilePhotoService` method

```java
// imports omitted for brevity
@Service
public class ProfilePhotoServiceS3 {

    private final S3Client s3;
    private final String bucket;
    private static final List<String> ALLOWED_MIME_TYPES = Arrays.asList("image/jpeg","image/png","image/gif","image/webp");
    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024;

    public ProfilePhotoServiceS3(@Value("${aws.s3.bucket}") String bucket) {
        this.s3 = S3Client.builder().build();
        this.bucket = bucket;
    }

    public String uploadToS3(MultipartFile file, String userId) throws IOException {
        if (file == null || file.isEmpty()) throw new BadRequestException("file required");
        if (file.getSize() > MAX_FILE_SIZE) throw new BadRequestException("max 5MB");
        String contentType = file.getContentType();
        if (!ALLOWED_MIME_TYPES.contains(contentType)) throw new BadRequestException("invalid type");

        String extension = getFileExtension(file.getOriginalFilename());
        String key = "profile-photos/" + userId + "_" + UUID.randomUUID() + "." + extension;

        PutObjectRequest putReq = PutObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .contentType(contentType)
                .acl(ObjectCannedACL.PUBLIC_READ) // or keep private and serve via signed URLs
                .build();

        s3.putObject(putReq, RequestBody.fromBytes(file.getBytes()));

        // Return public URL (if ACL public) or S3 path
        return "https://" + bucket + ".s3.amazonaws.com/" + key;
    }

    private String getFileExtension(String filename) {
        if (filename == null || filename.lastIndexOf('.') == -1) return "jpg";
        return filename.substring(filename.lastIndexOf('.') + 1);
    }
}
```

Notes:
- Make sure your AWS credentials are available to the app (environment variables, EC2 role, or default provider chain).
- Consider keeping objects private and use presigned GET URLs for access.
- Set proper bucket CORS if client directly accesses S3 URLs from browser.

---

9) Option B: Generate presigned PUT URL (client uploads directly)

Backend: generate presigned URL and return it.

```java
@Service
public class S3PresignService {
    private final S3Presigner presigner;
    private final String bucket;

    public S3PresignService(@Value("${aws.s3.bucket}") String bucket) {
        this.presigner = S3Presigner.builder().build();
        this.bucket = bucket;
    }

    public String generatePresignedUrl(String userId, String filename, String contentType) {
        String key = "profile-photos/" + userId + "_" + filename;
        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .contentType(contentType)
                .build();

        PutObjectPresignRequest presignRequest = PutObjectPresignRequest.builder()
                .signatureDuration(Duration.ofMinutes(10))
                .putObjectRequest(putObjectRequest)
                .build();

        PresignedPutObjectRequest presigned = presigner.presignPutObject(presignRequest);
        return presigned.url().toString();
    }
}
```

Client usage (Javascript / fetch):

```javascript
// 1. Request presigned URL from backend (authenticated)
const { url, key } = await fetch('/api/s3/presign?filename=photo.jpg&contentType=image/jpeg', {headers:{Authorization:'Bearer ...'}}).then(r=>r.json());

// 2. Upload directly to S3 with PUT
await fetch(url, { method: 'PUT', headers: {'Content-Type': 'image/jpeg'}, body: file });

// 3. Inform backend to save `key` on user profile: POST /user/photo/saved { key }
```

Advantages:
- Client uploads directly to S3; backend bandwidth is minimal.
- Works well for large files or high upload volume.

CORS:
- Configure bucket CORS to allow PUT from your frontend origin when using browser uploads.

---

10) Configuration entries to add to `application-dev.yml` or production config

```yaml
aws:
  s3:
    bucket: your-bucket-name
    region: us-east-1

# Optionally put credentials as environment variables or IAM role for the host.
# AWS_ACCESS_KEY_ID, AWS_SECRET_ACCESS_KEY
```

11) Security & production considerations

- Validate file types and sizes (already implemented)
- Scan files for malware if needed
- Use private S3 objects and presigned GET URLs to control access
- Use HTTPS when returning S3 URLs
- If storing locally behind a reverse proxy, ensure proper access controls and path traversal protections (already some checks exist)
- Backup strategy & retention policy for uploads

---

12) Docker & deployment notes

- If using local filesystem storage in Docker, map a persistent volume:

```yaml
# docker-compose snippet
volumes:
  uploads-data:

services:
  backend:
    image: your-backend-image
    volumes:
      - uploads-data:/app/uploads
```

- If using S3, no shared volume needed; set AWS credentials in environment variables or attach an appropriate IAM role to the host.

---

13) Quick action items for you (TL;DR)

- To test right now with Postman: follow the Postman steps above and upload to `http://localhost:8081/user/profile-photo/upload` with `file` field and Authorization header.
- To move to S3 quickly: add AWS SDK dependency, implement `uploadToS3(...)` and swap logic in `ProfilePhotoService`.
- To scale uploads: prefer presigned URL method (Option B).

---

If you want, I can:
- Add the S3 dependency to `pom.xml` and implement the S3-enabled `ProfilePhotoService` (either direct or presigned approach) and update `UserController` to expose a presign endpoint.
- Add example Postman collection file for the upload request.

Which of these would you like me to implement next? (A: backend S3 upload, B: presigned URL flow, C: Postman collection, D: none - just docs)
