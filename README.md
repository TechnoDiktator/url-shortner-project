# URL Shortener Application 🔗

A full-stack URL shortener application built with Spring Boot and JWT authentication. Shorten long URLs, track analytics, and manage your links with ease!

## 🚀 Features

- **URL Shortening**: Convert long URLs into short, shareable links
- **User Authentication**: Secure JWT-based authentication (HS512)
- **Analytics Dashboard**: Track clicks, IP addresses, user agents, and referrers
- **Click Statistics**: View total clicks by date range
- **User Management**: Each user can manage their own shortened URLs
- **RESTful API**: Clean and well-documented API endpoints

## 🛠️ Tech Stack

- **Backend**: Spring Boot 3.4.11
- **Security**: Spring Security + JWT (HS512)
- **Database**: MySQL 8.0
- **ORM**: Hibernate/JPA
- **Build Tool**: Maven
- **Java Version**: 21

## 📋 Prerequisites

- Java 21 or higher
- Maven 3.6+
- MySQL 8.0+
- Postman/cURL (for API testing)

## ⚙️ Configuration

### Database Setup

1. Create a MySQL database:
```sql
CREATE DATABASE url_shortener;
```

2. Update `application.properties`:
```properties
server.port=8081

# JWT Configuration - Must be at least 512 bits for HS512
jwt.secret=YOUR_SECURE_512_BIT_SECRET_KEY_HERE
jwt.expiration=86400000

# Database Configuration
spring.datasource.url=jdbc:mysql://localhost:3306/url_shortener
spring.datasource.username=root
spring.datasource.password=your_password
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.MySQLDialect
```

### Generate a Secure JWT Secret

```bash
# Generate a 512-bit (64 bytes) base64 encoded key
openssl rand -base64 64 | tr -d '\n' && echo
```

## 🏃 Running the Application

```bash
# Clone the repository
git clone <your-repo-url>
cd url-shortner-project/url-shortner-sb

# Build the project
mvn clean install

# Run the application
mvn spring-boot:run
```

The application will start on `http://localhost:8081`

## 📚 API Documentation

### Authentication Endpoints (Public)

#### Register New User
```bash
POST /api/auth/public/register
Content-Type: application/json

{
  "username": "johndoe",
  "email": "john@example.com",
  "password": "securePassword123"
}
```

**Response:**
```json
{
  "message": "User registered successfully"
}
```

#### Login
```bash
POST /api/auth/public/login
Content-Type: application/json

{
  "username": "johndoe",
  "password": "securePassword123"
}
```

**Response:**
```json
{
  "token": "eyJhbGciOiJIUzUxMiJ9...",
  "username": "johndoe",
  "email": "john@example.com",
  "roles": ["ROLE_USER"]
}
```

### URL Shortener Endpoints (Authenticated)

All endpoints below require the `Authorization: Bearer <token>` header.

#### Create Short URL
```bash
POST /api/url/shorten
Authorization: Bearer <token>
Content-Type: application/json

{
  "originalUrl": "https://www.example.com/very/long/url/path"
}
```

**Response:**
```json
{
  "id": 1,
  "originalUrl": "https://www.example.com/very/long/url/path",
  "shortUrl": "Abc12XyZ",
  "clickCount": 0,
  "createdDate": "2025-10-30T23:15:00",
  "username": "johndoe"
}
```

#### Get All User URLs
```bash
GET /api/url/myurls
Authorization: Bearer <token>
```

**Response:**
```json
[
  {
    "id": 1,
    "originalUrl": "https://example.com",
    "shortUrl": "Lqn7mPXB",
    "clickCount": 15,
    "createdDate": "2025-10-15T14:30:00",
    "username": "johndoe"
  },
  {
    "id": 2,
    "originalUrl": "https://another-example.com",
    "shortUrl": "p4qYGKpb",
    "clickCount": 8,
    "createdDate": "2025-10-20T10:00:00",
    "username": "johndoe"
  }
]
```

#### Get URL Analytics
```bash
GET /api/url/analytics/{shortUrl}?startDate=2025-10-01T00:00:00&endDate=2025-10-30T23:59:59
Authorization: Bearer <token>
```

**Example:**
```bash
curl -X GET "http://localhost:8081/api/url/analytics/Lqn7mPXB?startDate=2025-10-01T00:00:00&endDate=2025-10-30T23:59:59" \
  -H "Authorization: Bearer <token>"
```

**Response:**
```json
[
  {
    "id": 1,
    "shortUrl": "Lqn7mPXB",
    "clickedAt": "2025-10-15T14:30:00",
    "ipAddress": "192.168.1.100",
    "userAgent": "Mozilla/5.0...",
    "referer": "https://google.com"
  },
  {
    "id": 2,
    "shortUrl": "Lqn7mPXB",
    "clickedAt": "2025-10-16T09:15:00",
    "ipAddress": "192.168.1.101",
    "userAgent": "Chrome/120.0...",
    "referer": "https://twitter.com"
  }
]
```

#### Get Total Clicks by Date
```bash
GET /api/url/totalClicks?startDate=2025-10-01&endDate=2025-10-30
Authorization: Bearer <token>
```

**Example:**
```bash
curl -X GET "http://localhost:8081/api/url/totalClicks?startDate=2025-10-01&endDate=2025-10-30" \
  -H "Authorization: Bearer <token>"
```

**Response:**
```json
{
  "2025-10-01": 15,
  "2025-10-02": 23,
  "2025-10-05": 18,
  "2025-10-15": 42,
  "2025-10-20": 27,
  "2025-10-30": 12
}
```

## 🧪 Testing with cURL

### Complete Test Flow

```bash
# 1. Register a new user
curl -X POST http://localhost:8081/api/auth/public/register \
  -H "Content-Type: application/json" \
  -d '{"username":"testuser","email":"test@example.com","password":"password123"}'

# 2. Login to get JWT token
TOKEN=$(curl -X POST http://localhost:8081/api/auth/public/login \
  -H "Content-Type: application/json" \
  -d '{"username":"testuser","password":"password123"}' \
  | jq -r '.token')

# 3. Create a short URL
curl -X POST http://localhost:8081/api/url/shorten \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{"originalUrl":"https://www.example.com/test"}'

# 4. Get all your URLs
curl -X GET http://localhost:8081/api/url/myurls \
  -H "Authorization: Bearer $TOKEN"

# 5. Get analytics for a specific short URL
curl -X GET "http://localhost:8081/api/url/analytics/Abc12XyZ?startDate=2025-10-01T00:00:00&endDate=2025-10-30T23:59:59" \
  -H "Authorization: Bearer $TOKEN"

# 6. Get total clicks by date
curl -X GET "http://localhost:8081/api/url/totalClicks?startDate=2025-10-01&endDate=2025-10-30" \
  -H "Authorization: Bearer $TOKEN"
```

## 📊 Database Schema

### Users Table
```sql
CREATE TABLE users (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(255) UNIQUE NOT NULL,
    email VARCHAR(255) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(50) NOT NULL
);
```

### URL Mappings Table
```sql
CREATE TABLE url_mappings (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    original_url VARCHAR(2048) NOT NULL,
    short_url VARCHAR(10) UNIQUE NOT NULL,
    click_count INT DEFAULT 0,
    created_date TIMESTAMP NOT NULL,
    user_id BIGINT NOT NULL,
    FOREIGN KEY (user_id) REFERENCES users(id)
);
```

### Click Events Table
```sql
CREATE TABLE click_events (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    url_mapping_id BIGINT NOT NULL,
    clicked_at TIMESTAMP NOT NULL,
    ip_address VARCHAR(45),
    user_agent TEXT,
    referer TEXT,
    FOREIGN KEY (url_mapping_id) REFERENCES url_mappings(id)
);
```

## 🔒 Security Features

- **JWT Authentication**: Secure token-based authentication using HS512 algorithm
- **Password Encryption**: BCrypt password hashing
- **Role-Based Access Control**: Users can only access their own URLs
- **Public/Private Endpoints**: Clear separation between authenticated and public routes

## 🐛 Troubleshooting

### Common Issues

**1. "Invalid JWT token" Error**
- Ensure you're using a fresh token (login again)
- Check that your JWT secret is at least 64 characters for HS512
- Verify the token is included in the Authorization header as `Bearer <token>`

**2. Database Connection Issues**
- Verify MySQL is running: `sudo systemctl status mysql`
- Check database credentials in `application.properties`
- Ensure database `url_shortener` exists

**3. Port Already in Use**
- Change the port in `application.properties`: `server.port=8082`
- Or kill the process using port 8081: `lsof -ti:8081 | xargs kill -9`

## 📝 Project Structure

```
url-shortner-sb/
├── src/main/java/com/ulr/shortner/
│   ├── controller/          # REST Controllers
│   ├── service/             # Business Logic
│   ├── repository/          # Database Repositories
│   ├── models/              # Entity Classes
│   ├── dtos/                # Data Transfer Objects
│   ├── security/            # JWT & Security Config
│   └── UrlShortnerSbApplication.java
├── src/main/resources/
│   └── application.properties
├── pom.xml
└── README.md
```

## 🤝 Contributing

1. Fork the repository
2. Create your feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit your changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

## 📄 License

This project is licensed under the MIT License.

## 👤 Author

Your Name - [@yourhandle](https://twitter.com/yourhandle)

Project Link: [https://github.com/yourusername/url-shortener](https://github.com/yourusername/url-shortener)

## 🙏 Acknowledgments

- Spring Boot Documentation
- JWT.io for token debugging
- MySQL Community

---

**Note**: Remember to update your JWT secret with a secure 512-bit key before deploying to production!