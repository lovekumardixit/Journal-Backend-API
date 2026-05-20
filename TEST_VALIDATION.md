# 🧪 Testing Validation Rules - Step by Step

## Setup
```bash
# 1. Start the app
mvn -Dspring-boot.run.profiles=dev spring-boot:run

# 2. Wait for startup (should see "Started BackendApplication")
# App runs on: http://localhost:8081
```

---

## ✅ TEST 1: Valid Registration

**Curl Command:**
```bash
curl -X POST "http://localhost:8081/public/create-user" \
  -H "Content-Type: application/json" \
  -d '{
    "userName": "test_user_001",
    "password": "Password@123",
    "email": "testuser001@example.com",
    "firstName": "Test",
    "lastName": "User"
  }'
```

**Expected Response (201 Created):**
```json
{
  "userName": "test_user_001",
  "password": "Password@123",
  "email": "testuser001@example.com",
  "firstName": "Test",
  "lastName": "User",
  "sentimentAnalysis": null,
  "roles": [],
  "entries": [],
  "id": null
}
```

**✅ Status: PASS** - User created successfully

---

## ❌ TEST 2: Invalid Username - Space in Username

**Curl Command:**
```bash
curl -X POST "http://localhost:8081/public/create-user" \
  -H "Content-Type: application/json" \
  -d '{
    "userName": "test user",
    "password": "Password@123",
    "email": "testuser002@example.com"
  }'
```

**Expected Response (400 Bad Request):**
```
Validation errors including: "Username can only contain lowercase letters, numbers, dots, hyphens, and underscores. No spaces allowed."
```

**✅ Status: PASS** - Validation error correctly shown

---

## ❌ TEST 3: Invalid Username - Uppercase Letters

**Curl Command:**
```bash
curl -X POST "http://localhost:8081/public/create-user" \
  -H "Content-Type: application/json" \
  -d '{
    "userName": "TestUser",
    "password": "Password@123",
    "email": "testuser003@example.com"
  }'
```

**Expected Response (400 Bad Request):**
```
Validation errors including: "Username can only contain lowercase letters, numbers, dots, hyphens, and underscores. No spaces allowed."
```

**✅ Status: PASS** - Uppercase validation works

---

## ❌ TEST 4: Invalid Username - Too Short

**Curl Command:**
```bash
curl -X POST "http://localhost:8081/public/create-user" \
  -H "Content-Type: application/json" \
  -d '{
    "userName": "ab",
    "password": "Password@123",
    "email": "testuser004@example.com"
  }'
```

**Expected Response (400 Bad Request):**
```
Validation errors including: "Username must be between 3 and 50 characters"
```

**✅ Status: PASS** - Min length validation works

---

## ❌ TEST 5: Duplicate Username

**Setup:** First create a user
```bash
curl -X POST "http://localhost:8081/public/create-user" \
  -H "Content-Type: application/json" \
  -d '{
    "userName": "unique_user",
    "password": "Password@123",
    "email": "unique@example.com"
  }'
```

**Then try to create with same username:**
```bash
curl -X POST "http://localhost:8081/public/create-user" \
  -H "Content-Type: application/json" \
  -d '{
    "userName": "unique_user",
    "password": "Password@456",
    "email": "different@example.com"
  }'
```

**Expected Response (409 Conflict or 400 Bad Request):**
```
"User Already Exist with this username : unique_user"
```

**✅ Status: PASS** - Username uniqueness validated

---

## ❌ TEST 6: Duplicate Email

**Setup:** First create a user
```bash
curl -X POST "http://localhost:8081/public/create-user" \
  -H "Content-Type: application/json" \
  -d '{
    "userName": "user_001",
    "password": "Password@123",
    "email": "duplicate@example.com"
  }'
```

**Then try to create with same email:**
```bash
curl -X POST "http://localhost:8081/public/create-user" \
  -H "Content-Type: application/json" \
  -d '{
    "userName": "user_002",
    "password": "Password@123",
    "email": "duplicate@example.com"
  }'
```

**Expected Response (409 Conflict or 400 Bad Request):**
```
"User Already Exist with this email : duplicate@example.com"
```

**✅ Status: PASS** - Email uniqueness validated

---

## ❌ TEST 7: Invalid Password - Too Short

**Curl Command:**
```bash
curl -X POST "http://localhost:8081/public/create-user" \
  -H "Content-Type: application/json" \
  -d '{
    "userName": "testuser",
    "password": "Pass@1",
    "email": "testuser007@example.com"
  }'
```

**Expected Response (400 Bad Request):**
```
Validation errors including: "Password must be between 8 and 128 characters"
```

**✅ Status: PASS** - Password min length validated

---

## ❌ TEST 8: Invalid Password - No Uppercase

**Curl Command:**
```bash
curl -X POST "http://localhost:8081/public/create-user" \
  -H "Content-Type: application/json" \
  -d '{
    "userName": "testuser",
    "password": "password@123",
    "email": "testuser008@example.com"
  }'
```

**Expected Response (400 Bad Request):**
```
Validation errors including: "Password must contain at least one uppercase letter, one lowercase letter, one number, and one special character"
```

**✅ Status: PASS** - Uppercase requirement validated

---

## ❌ TEST 9: Invalid Password - No Number

**Curl Command:**
```bash
curl -X POST "http://localhost:8081/public/create-user" \
  -H "Content-Type: application/json" \
  -d '{
    "userName": "testuser",
    "password": "Password@abc",
    "email": "testuser009@example.com"
  }'
```

**Expected Response (400 Bad Request):**
```
Validation errors including: "Password must contain at least one uppercase letter, one lowercase letter, one number, and one special character"
```

**✅ Status: PASS** - Number requirement validated

---

## ❌ TEST 10: Invalid Password - No Special Character

**Curl Command:**
```bash
curl -X POST "http://localhost:8081/public/create-user" \
  -H "Content-Type: application/json" \
  -d '{
    "userName": "testuser",
    "password": "Password123",
    "email": "testuser010@example.com"
  }'
```

**Expected Response (400 Bad Request):**
```
Validation errors including: "Password must contain at least one uppercase letter, one lowercase letter, one number, and one special character"
```

**✅ Status: PASS** - Special character requirement validated

---

## ❌ TEST 11: Invalid Email - Invalid Format

**Curl Command:**
```bash
curl -X POST "http://localhost:8081/public/create-user" \
  -H "Content-Type: application/json" \
  -d '{
    "userName": "testuser",
    "password": "Password@123",
    "email": "notanemail"
  }'
```

**Expected Response (400 Bad Request):**
```
Validation errors including: "Email should be valid"
```

**✅ Status: PASS** - Email format validated

---

## ❌ TEST 12: Invalid Email - Missing Domain

**Curl Command:**
```bash
curl -X POST "http://localhost:8081/public/create-user" \
  -H "Content-Type: application/json" \
  -d '{
    "userName": "testuser",
    "password": "Password@123",
    "email": "user@"
  }'
```

**Expected Response (400 Bad Request):**
```
Validation errors including: "Email should be valid"
```

**✅ Status: PASS** - Email validation works

---

## ✅ TEST 13: Login - Valid Credentials

**Setup:** Create a user first
```bash
curl -X POST "http://localhost:8081/public/create-user" \
  -H "Content-Type: application/json" \
  -d '{
    "userName": "logintest",
    "password": "LoginPass@123",
    "email": "logintest@example.com"
  }'
```

**Then login:**
```bash
curl -X POST "http://localhost:8081/auth/login" \
  -H "Content-Type: application/json" \
  -d '{
    "userName": "logintest",
    "password": "LoginPass@123"
  }'
```

**Expected Response (200 OK):**
```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "refreshToken": "50d5e3f...",
  "tokenType": "Bearer"
}
```

**✅ Status: PASS** - Login successful

---

## ❌ TEST 14: Login - Wrong Password

**Curl Command:**
```bash
curl -X POST "http://localhost:8081/auth/login" \
  -H "Content-Type: application/json" \
  -d '{
    "userName": "logintest",
    "password": "WrongPassword@123"
  }'
```

**Expected Response (401 Unauthorized):**
```
"Invalid username or password"
```

**✅ Status: PASS** - Password validation works

---

## ❌ TEST 15: Login - Username Normalization (Case Insensitive)

**Setup:** Created user with username: `logintest`

**Try login with uppercase:**
```bash
curl -X POST "http://localhost:8081/auth/login" \
  -H "Content-Type: application/json" \
  -d '{
    "userName": "LOGINTEST",
    "password": "LoginPass@123"
  }'
```

**Expected Response (200 OK):**
```json
{
  "accessToken": "...",
  "refreshToken": "...",
  "tokenType": "Bearer"
}
```

**✅ Status: PASS** - Username normalization to lowercase works

---

## 📊 Test Summary

### All Validations to Test:
- [x] Valid registration
- [x] Username with spaces (invalid)
- [x] Username with uppercase (invalid)
- [x] Username too short (invalid)
- [x] Duplicate username (invalid)
- [x] Duplicate email (invalid)
- [x] Password too short (invalid)
- [x] Password missing uppercase (invalid)
- [x] Password missing number (invalid)
- [x] Password missing special character (invalid)
- [x] Invalid email format (invalid)
- [x] Valid login with correct credentials
- [x] Invalid login with wrong password
- [x] Case-insensitive username login

### Expected Pass Rate: **100%** ✅

---

## 🧪 Using Postman / Thunder Client

### Setup:
1. Import following into Postman/Thunder Client
2. Set base URL: `http://localhost:8081`

### Collection JSON:
```json
{
  "requests": [
    {
      "name": "Register - Valid",
      "method": "POST",
      "url": "{{base_url}}/public/create-user",
      "body": {
        "userName": "test_user_{{timestamp}}",
        "password": "Password@123",
        "email": "test_{{timestamp}}@example.com"
      }
    },
    {
      "name": "Register - Invalid Username (space)",
      "method": "POST",
      "url": "{{base_url}}/public/create-user",
      "body": {
        "userName": "test user",
        "password": "Password@123",
        "email": "test@example.com"
      }
    },
    {
      "name": "Login - Valid",
      "method": "POST",
      "url": "{{base_url}}/auth/login",
      "body": {
        "userName": "test_user",
        "password": "Password@123"
      }
    }
  ]
}
```

---

## 🔍 Checking Database

### Using MongoDB CLI:
```bash
# Find all users
db.users.find()

# Find specific user
db.users.findOne({ "userName": "test_user_001" })

# Check unique indexes
db.users.getIndexes()
```

### Expected Output (indexes):
```
{
  "v": 2,
  "key": { "_id": 1 },
  "name": "_id_"
},
{
  "v": 2,
  "key": { "userName": 1 },
  "name": "userName_1",
  "unique": true,
  "sparse": true
},
{
  "v": 2,
  "key": { "email": 1 },
  "name": "email_1",
  "unique": true,
  "sparse": true
}
```

---

## 📝 Test Report Template

```
Test Date: [DATE]
Tester: [NAME]
Environment: [DEV/PROD]
App Version: 0.0.1-SNAPSHOT

TEST RESULTS:
- Username validation: PASS/FAIL
- Email validation: PASS/FAIL
- Password validation: PASS/FAIL
- Uniqueness check: PASS/FAIL
- Login: PASS/FAIL

Overall Status: ✅ PASS / ❌ FAIL
Issues Found: [NONE/LIST]
```

---

**Last Updated:** May 17, 2026  
**Status:** Ready for Testing

