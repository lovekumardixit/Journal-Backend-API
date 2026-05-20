# 📋 User Validation Rules - Complete Guide

## Overview
Yeh document user registration aur profile update ke liye sab validation rules describe karta hai. Ye rules **best practices** follow karte hain.

---

## 1️⃣ **USERNAME Validation Rules**

### Rules:
```
✅ Min Length: 3 characters
✅ Max Length: 50 characters
✅ Allowed Characters: a-z (lowercase), 0-9 (numbers), . (dot), - (hyphen), _ (underscore)
❌ No spaces allowed
❌ No uppercase letters allowed
❌ Must be UNIQUE across system
```

### Examples:
```
Valid:        Invalid:
✅ lav_dixit  ❌ Lav Dixit (spaces & uppercase)
✅ john.doe   ❌ JOHN_DOE (uppercase)
✅ user-123   ❌ user name (space)
✅ test_2     ❌ tes (too short)
```

### Auto-Normalization:
- Usernames automatically converted to **lowercase** on registration
- Example: User types `John_Doe` → Saved as `john_doe` in database

### Database Constraint:
```
@Indexed(unique = true, sparse = true)
Username field has UNIQUE index → Duplicate usernames rejected with 400 error
```

---

## 2️⃣ **PASSWORD Validation Rules**

### Rules:
```
✅ Min Length: 8 characters
✅ Max Length: 128 characters
✅ Must contain: At least 1 UPPERCASE letter (A-Z)
✅ Must contain: At least 1 lowercase letter (a-z)
✅ Must contain: At least 1 NUMBER (0-9)
✅ Must contain: At least 1 SPECIAL character (@$!%*?&)
```

### Regex Pattern:
```
^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[@$!%*?&])[A-Za-z\d@$!%*?&]+$
```

### Examples:
```
Valid:                  Invalid:
✅ Password@123        ❌ password123 (no uppercase/special)
✅ MyPass@2024         ❌ MyPass123 (no special character)
✅ Secure!Pass1        ❌ SECURE@PASS (no lowercase)
✅ Test@Pass99         ❌ pass@123 (too short, no uppercase)
```

### Security Notes:
- Password is **hashed with BCrypt** before storing → Never stored in plain text
- On login, entered password is compared with stored hash
- Password reset requires email verification (future feature)

---

## 3️⃣ **EMAIL Validation Rules**

### Rules:
```
✅ Must be valid email format (RFC 5322 standard)
✅ Max Length: 100 characters
✅ Must be UNIQUE across system
❌ Cannot be empty or null
```

### Validation:
- Uses Jakarta `@Email` validator (built-in Spring Boot validation)
- Standard email format: `localpart@domain.extension`

### Examples:
```
Valid:                          Invalid:
✅ user@example.com            ❌ user (no @domain)
✅ john.doe@company.co.uk      ❌ user@.com (missing domain)
✅ test+tag@mail.org           ❌ user @example.com (space)
✅ firstname.lastname@site.com  ❌ @example.com (no localpart)
```

### Auto-Normalization:
- Emails automatically converted to **lowercase** on registration
- Example: User enters `John@EXAMPLE.COM` → Saved as `john@example.com`

### Database Constraint:
```
@Indexed(unique = true, sparse = true)
Email field has UNIQUE index → Duplicate emails rejected with 400 error
```

---

## 4️⃣ **OPTIONAL FIELDS Validation**

### firstName & lastName
```
✅ Optional (can be null)
✅ Max: 100 characters each
✅ Can contain: Letters, spaces, hyphens, apostrophes
```

### sentimentAnalysis
```
✅ Optional (default: null/false)
✅ Type: Boolean
✅ Values: true/false
```

---

## 5️⃣ **API Validation Errors**

### When validation FAILS, API returns:

**Example Response (400 Bad Request):**
```json
{
  "timestamp": "2024-05-17T10:30:45.123Z",
  "status": 400,
  "error": "Bad Request",
  "message": "Validation failed",
  "errors": [
    {
      "field": "userName",
      "message": "Username can only contain lowercase letters, numbers, dots, hyphens, and underscores. No spaces allowed."
    },
    {
      "field": "password",
      "message": "Password must contain at least one uppercase letter, one lowercase letter, one number, and one special character (@$!%*?&)"
    },
    {
      "field": "email",
      "message": "Email should be valid"
    }
  ]
}
```

### Common Error Codes:
```
400 Bad Request       → Validation failed (format, length, pattern)
409 Conflict          → Username or Email already exists (duplicate)
401 Unauthorized      → Invalid credentials on login
422 Unprocessable     → Email already registered (during update)
```

---

## 6️⃣ **DUPLICATE CHECK - How It Works**

### Username Duplicate Check:
```
1. User submits registration with username "john_doe"
2. System checks: existsByUserName("john_doe") → true
3. Response: 409 Conflict
   "User Already Exist with this username : john_doe"
```

### Email Duplicate Check:
```
1. User submits registration with email "john@example.com"
2. System checks: existsByEmail("john@example.com") → true
3. Response: 409 Conflict
   "User Already Exist with this email : john@example.com"
```

### Database Level:
- MongoDB ensures uniqueness with **sparse unique indexes**
- If duplicate somehow passes, MongoDB throws: `DuplicateKeyException`

---

## 7️⃣ **REST API Examples**

### Registration Request (Valid)
```bash
curl -X POST "http://localhost:8081/public/create-user" \
  -H "Content-Type: application/json" \
  -d '{
    "userName": "john_doe",
    "password": "SecurePass@123",
    "email": "john@example.com",
    "firstName": "John",
    "lastName": "Doe",
    "sentimentAnalysis": true
  }'
```

**Response (201 Created):**
```json
{
  "userName": "john_doe",
  "email": "john@example.com",
  "password": "SecurePass@123",
  "firstName": "John",
  "lastName": "Doe",
  "sentimentAnalysis": true
}
```

### Registration Request (Invalid Username)
```bash
curl -X POST "http://localhost:8081/public/create-user" \
  -H "Content-Type: application/json" \
  -d '{
    "userName": "John Doe",
    "password": "SecurePass@123",
    "email": "john@example.com"
  }'
```

**Response (400 Bad Request):**
```json
{
  "error": "Validation failed",
  "errors": [
    {
      "field": "userName",
      "message": "Username can only contain lowercase letters, numbers, dots, hyphens, and underscores. No spaces allowed."
    }
  ]
}
```

### Login Request
```bash
curl -X POST "http://localhost:8081/auth/login" \
  -H "Content-Type: application/json" \
  -d '{
    "userName": "john_doe",
    "password": "SecurePass@123"
  }'
```

**Response (200 OK):**
```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "refreshToken": "50d5e3f...",
  "tokenType": "Bearer"
}
```

---

## 8️⃣ **SWAGGER/OpenAPI Examples**

### Test in Swagger UI:

1. Open: `http://localhost:8081/swagger-ui.html`
2. Find: **Public APIs** → **POST /public/create-user**
3. Click "Try it out"
4. Paste JSON:

```json
{
  "userName": "test_user_123",
  "password": "TestPass@2024",
  "email": "testuser@example.com",
  "firstName": "Test",
  "lastName": "User",
  "sentimentAnalysis": false
}
```

5. Click "Execute"
6. See validation errors in response if any

---

## 9️⃣ **BEST PRACTICES IMPLEMENTED**

### ✅ What we follow:
1. **Case Normalization** - Usernames & emails auto-converted to lowercase
2. **Password Complexity** - Strong password policy (uppercase + lowercase + number + special)
3. **Uniqueness Enforcement** - Both database-level (unique indexes) and application-level checks
4. **Input Validation** - Jakarta validation annotations on all DTOs
5. **Secure Storage** - Passwords hashed with BCrypt (10 rounds), never stored plain
6. **Meaningful Error Messages** - Clear feedback on what validation failed
7. **Email Verification** - Email format validation (future: email verification link)
8. **Rate Limiting** - Protected against brute force (via interceptor)

### ❌ What we DON'T do (intentionally):
- Store passwords in plain text
- Allow special characters in username
- Accept weak passwords
- Allow duplicate emails/usernames
- Log passwords anywhere

---

## 🔟 **TESTING VALIDATION - Checklist**

### ✅ Test Cases:

| Scenario | Input | Expected Result |
|----------|-------|-----------------|
| Valid registration | All fields correct | 201 Created |
| Username too short | "ab" | 400 Bad Request |
| Username with space | "john doe" | 400 Bad Request |
| Username uppercase | "JOHN_DOE" | 400 Bad Request (auto-lowercase) |
| Duplicate username | "john_doe" (exists) | 409 Conflict |
| Password too short | "Pass@1" | 400 Bad Request |
| Password no uppercase | "password@123" | 400 Bad Request |
| Password no special char | "Password123" | 400 Bad Request |
| Invalid email | "john@" | 400 Bad Request |
| Duplicate email | "john@ex.com" (exists) | 409 Conflict |
| Login success | Valid credentials | 200 OK + tokens |
| Login fail | Wrong password | 401 Unauthorized |

---

## 1️⃣1️⃣ **ENVIRONMENT VARIABLES & CONFIG**

### Production Config (`application-prod.yml`):
```yaml
app:
  validation:
    username:
      min-length: 3
      max-length: 50
      pattern: "^[a-z0-9_.-]+$"
    password:
      min-length: 8
      max-length: 128
    email:
      max-length: 100
```

### Running with Validation:
```bash
# Dev profile (validation ON)
mvn -Dspring-boot.run.profiles=dev spring-boot:run

# Prod profile (validation ON)
mvn -Dspring-boot.run.profiles=prod spring-boot:run

# Test registration
curl -X POST "http://localhost:8081/public/create-user" \
  -H "Content-Type: application/json" \
  -d '{"userName":"test_user","password":"TestPass@123","email":"test@ex.com"}'
```

---

## 1️⃣2️⃣ **TROUBLESHOOTING**

### Problem: "User Already Exist with this username"
**Solution:** Choose a different username (case-insensitive)

### Problem: "Email should be valid"
**Solution:** Use proper email format: `name@domain.com`

### Problem: "Password must contain..."
**Solution:** Add uppercase, lowercase, number, and special character

### Problem: "Username can only contain..."
**Solution:** Remove spaces, use only lowercase, numbers, dots, hyphens, underscores

### Problem: Database unique constraint violated
**Solution:** 
- Rebuild MongoDB indexes: `db.users.dropIndex("userName_1")`
- Or use new email/username

---

## 📖 **Quick Reference - Copy Paste**

### Register (Valid):
```bash
curl -X POST "http://localhost:8081/public/create-user" \
  -H "Content-Type: application/json" \
  -d '{"userName":"user_2024","password":"Strong@Pass123","email":"user@example.com","firstName":"User","lastName":"Test"}'
```

### Register (Invalid - Test):
```bash
curl -X POST "http://localhost:8081/public/create-user" \
  -H "Content-Type: application/json" \
  -d '{"userName":"USER 123","password":"weak","email":"invalid-email"}'
```

---

**Last Updated:** May 17, 2026  
**Version:** 1.0  
**Status:** ✅ Production Ready

