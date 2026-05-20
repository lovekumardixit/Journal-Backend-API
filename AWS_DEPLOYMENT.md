 # Journal Backend ko AWS par deploy karne ke note aur zaroori steps

Yeh file simple bhaasha mein batati hai ki AWS par project chalane ke liye code mein kya zaroori badlav chahiye ya nahi. Neeche mukhya points diye gaye hain.

- Jaldi jawab: Kab code change ki zaroorat nahi hai
- Production ke liye sujhav (file storage, credentials, monitoring)
- S3-based `ProfilePhotoService` ka example (Java / AWS SDK v2) — agar aap S3 use karna chahen
- Zaroori environment variables, IAM permissions aur `application-prod.yml` snippets
- Quick deployment options: ECS (Fargate), Elastic Beanstalk (Docker), EC2 + Docker

---

Saar (short answer)
- Agar aap AWS par aise run karte ho ki container/instance ko ek persistent filesystem mil jaye (jaise EBS volume ya ECS ke saath EFS mount kiya hua), to code mein koi change karne ki zaroorat nahi hai. Jo current `ProfilePhotoService` hai, woh files `uploads/profile-photos/` mein store karta hai aur tab tak sahi chalega jab tak underlying storage persistent ho.
- Lekin agar aap scalable aur durable solution chahte ho to S3 use karna recommended hai. Uska example niche diya hua hai.

1) Codebase mein kahan dekhen
- File upload logic: `src/main/java/com/love/Backend/service/ProfilePhotoService.java` (local filesystem)
- Controller endpoints: `src/main/java/com/love/Backend/controller/UserController.java`
- Swagger/OpenAPI config: `src/main/java/com/love/Backend/config/SwaggerConfig.java`

2) Option A — Code change nahi karna (tezi se)
- Agar aap chahte ho bina code badle deploy karna to yeh options use karo:
  - EC2: Docker image ko EC2 par chalao aur uploads ko attached EBS volume par store karo (container ke andar host path ko `/app/uploads` se map karo).
  - ECS + EFS: EFS filesystem banayein aur ise Fargate task mein app ke upload path par mount karein (default `app.upload.dir` = `uploads`).
  - Elastic Beanstalk: EBS volume attach karke `uploads/` directory wahan store karein.

Faayde: Code change nahi chahiye, jaldi ho jata hai.
Nuksan: Volume management infrastructure chahiye; multiple instances pe scale karne ke liye shared filesystem (EFS) zaroori hai.

3) Option B — Recommended: S3 use karo (thoda code change)
- S3 durable, scalable, CDN (CloudFront) ke saath easy; volume mounting ki tension nahi.
- Niche S3 service ka example aur wiring bataya gaya hai.

3.1) `pom.xml` mein dependency add karein
Add AWS SDK v2 S3 dependency (example):

```xml
<!-- add inside <dependencies> -->
<dependency>
  <groupId>software.amazon.awssdk</groupId>
  <artifactId>s3</artifactId>
  <version>2.20.0</version> <!-- choose the latest compatible version -->
</dependency>
```

3.2) Environment variables / IAM role
- `AWS_S3_BUCKET` — bucket ka naam
- `AWS_REGION` — region (e.g., us-east-1)
- `AWS_ACCESS_KEY_ID` aur `AWS_SECRET_ACCESS_KEY` — sirf tab zaroori jab aap IAM role use nahi kar rahe
- Optional: `AWS_S3_BASE_URL` ya CloudFront domain agar CDN se serve karoge

3.3) S3-backed service ka example
Niche Java service ka example diya gaya hai — ise `src/main/java/com/love/Backend/service/ProfilePhotoS3Service.java` mein daal sakte ho. (Code block unchanged hai taki copy-paste se use kar sako.)

```java
package com.love.Backend.service;

import com.love.Backend.exception.BadRequestException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Service
@ConditionalOnProperty(name = "aws.s3.enabled", havingValue = "true")
public class ProfilePhotoS3Service {

    @Value("${aws.s3.bucket}")
    private String bucket;

    @Value("${aws.s3.base-url:}")
    private String baseUrl; // optional CloudFront or S3 website base URL

    private S3Client s3;

    private static final List<String> ALLOWED_MIME_TYPES = Arrays.asList(
            "image/jpeg", "image/png", "image/gif", "image/webp"
    );
    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024; // 5 MB

    @PostConstruct
    public void init() {
        Region region = Region.of(System.getenv().getOrDefault("AWS_REGION", "us-east-1"));
        this.s3 = S3Client.builder().region(region).build();
    }

    public String uploadProfilePhoto(MultipartFile file, String userId) {
        if (file == null || file.isEmpty()) {
            throw new BadRequestException("Profile photo file is required");
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            throw new BadRequestException("Profile photo size must not exceed 5 MB");
        }

        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_MIME_TYPES.contains(contentType)) {
            throw new BadRequestException("Only JPEG, PNG, GIF, and WebP images are allowed. Uploaded: " + contentType);
        }

        try {
            String extension = "jpg";
            String original = file.getOriginalFilename();
            if (original != null && original.contains(".")) {
                extension = original.substring(original.lastIndexOf('.') + 1);
            }
            String key = "profile-photos/" + userId + "_" + UUID.randomUUID() + "." + extension;

            PutObjectRequest putReq = PutObjectRequest.builder()
                    .bucket(bucket)
                    .key(key)
                    .contentType(contentType)
                    .acl("public-read") // optional: prefer CloudFront + signed URLs in prod
                    .build();

            s3.putObject(putReq, RequestBody.fromInputStream(file.getInputStream(), file.getSize()));

            if (baseUrl != null && !baseUrl.isBlank()) {
                return baseUrl + "/" + key;
            }
            // default to s3 public URL
            return String.format("https://%s.s3.%s.amazonaws.com/%s", bucket, s3.region().id(), key);
        } catch (IOException ex) {
            throw new BadRequestException("Failed to upload profile photo: " + ex.getMessage());
        }
    }

    public void deleteProfilePhoto(String photoUrl) {
        if (photoUrl == null || photoUrl.isBlank()) return;
        // try to extract key from URL; if you store only keys in DB you can delete directly
        String key = null;
        if (photoUrl.contains("/profile-photos/")) {
            key = photoUrl.substring(photoUrl.indexOf("profile-photos/"));
        } else if (photoUrl.startsWith("https://")) {
            // last path segment
            key = photoUrl.substring(photoUrl.indexOf(bucket) + bucket.length() + 1);
        }
        if (key == null) return;
        DeleteObjectRequest del = DeleteObjectRequest.builder().bucket(bucket).key(key).build();
        try {
            s3.deleteObject(del);
        } catch (Exception ex) {
            System.err.println("Warning: Failed to delete S3 object: " + ex.getMessage());
        }
    }
}
```

3.4) Wiring rules (Spring)
- Option 1 (recommended): `@ConditionalOnProperty` use karo — jaisa code example mein diya hai. Phir production mein `aws.s3.enabled=true` set karke S3 enable kar sakte ho.
- Option 2: agar aasan chahiye to local `ProfilePhotoService` ko remove karke S3 version rakh do.

3.5) `application-prod.yml` snippet (example)

```yaml
aws:
  s3:
    enabled: true
    bucket: my-journal-app-bucket
    base-url: https://d111111abcdef8.cloudfront.net   # optional CloudFront

# other production overrides
server:
  port: 8081

app:
  upload:
    dir: uploads   # agar aws.s3.enabled=false to yeh use hoga

spring:
  profiles: prod
```

3.6) IAM permissions (EC2/ECS task role ya IAM user ke liye)
- Kam se kam in actions ki permission chahiye hogi:
  - s3:PutObject
  - s3:GetObject
  - s3:DeleteObject
  - s3:ListBucket (optional)

4) Dusre production notes
- MongoDB: managed DB (Atlas) ya Amazon DocumentDB use karo. `MONGO_URI` environment variable se provide karo.
- JWT secret: `JWT_SECRET` environment variable mein set karo (kabhi repo mein commit mat karo).
- Mail / Kafka: credentials env vars mein do.
- CORS: agar origin restrict karna ho to `WebConfig` ya properties use karo.
- Health checks: `/actuator/health` expose karo aur load balancer health check set karo.
- Logging: CloudWatch mein logs bhejo (ECS/Beanstalk stdout se CloudWatch hota hai).

5) Packaging & deployment (short)
- Docker image build aur ECR push (PowerShell commands):

```powershell
# build
mvn clean package -DskipTests
docker build -t <aws_account>.dkr.ecr.<region>.amazonaws.com/journal-backend:latest .
# push to ECR (assume ECR repo already created)
aws ecr get-login-password --region <region> | docker login --username AWS --password-stdin <aws_account>.dkr.ecr.<region>.amazonaws.com
docker push <aws_account>.dkr.ecr.<region>.amazonaws.com/journal-backend:latest
```

- ECS Fargate: task definition mein image use karo. Agar local uploads chahiye to EFS mount karo; agar S3 use karoge to env vars aur IAM task role set karo.
- Elastic Beanstalk: Docker platform app banao aur environment vars set karo.

6) Production readiness checklist
- [ ] Decide storage: EFS (code change nahi) ya S3 (code change)
- [ ] S3 bucket create/provision karo (agar S3 choose kiya)
- [ ] IAM role mein S3 permissions add karo
- [ ] `MONGO_URI`, `JWT_SECRET`, `AWS_*` env vars runtime mein provide karo
- [ ] Health checks aur logging configure karo (CloudWatch)
- [ ] HTTPS use karo; ALB ke peeche rakh kar CloudFront optional bana sakte ho

7) Main yeh sab repository mein kar doon?
- Main S3 service ka code, `pom.xml` dependency aur `application-prod.yml` example repository mein add kar sakta hoon. Batao kaunsa option chahiye — EFS (no code change) ya S3 (code change) — aur main changes kar dunga.

---

Last updated: May 16, 2026

