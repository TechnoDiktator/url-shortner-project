# Authentication Flow Documentation

## Complete Authentication Architecture

### 1. Registration Flow

```mermaid
sequenceDiagram
    participant Client
    participant Tomcat
    participant SecurityFilter as Security Filter Chain
    participant AuthController
    participant UserService
    participant PasswordEncoder
    participant UserRepository
    participant Database

    Client->>Tomcat: POST /api/auth/public/register
    Tomcat->>SecurityFilter: Request enters filter chain
    SecurityFilter->>SecurityFilter: Check if path is public
    SecurityFilter->>AuthController: Forward to registerUser()
    AuthController->>UserService: registerUser(user)
    
    UserService->>UserRepository: existsByUsername(username)
    alt Username exists
        UserRepository-->>UserService: true
        UserService-->>AuthController: RuntimeException
        AuthController-->>Client: 400 Bad Request
    else Username available
        UserRepository-->>UserService: false
        UserService->>UserRepository: existsByEmail(email)
        alt Email exists
            UserRepository-->>UserService: true
            UserService-->>AuthController: RuntimeException
            AuthController-->>Client: 400 Bad Request
        else Email available
            UserRepository-->>UserService: false
            UserService->>PasswordEncoder: encode(plainPassword)
            PasswordEncoder-->>UserService: hashedPassword
            UserService->>UserRepository: save(user)
            UserRepository->>Database: INSERT INTO users
            Database-->>UserRepository: User saved
            UserRepository-->>UserService: User entity
            UserService-->>AuthController: Success
            AuthController-->>Client: 200 OK
        end
    end
```

### 2. Login Flow

```mermaid
sequenceDiagram
    participant Client
    participant Tomcat
    participant SecurityFilter as Security Filter Chain
    participant AuthController
    participant UserService
    participant AuthManager as Authentication Manager
    participant UserDetailsService
    participant UserRepository
    participant PasswordEncoder
    participant JwtUtils
    participant Database

    Client->>Tomcat: POST /api/auth/public/login
    Tomcat->>SecurityFilter: Request enters filter chain
    SecurityFilter->>AuthController: Forward to loginUser()
    AuthController->>UserService: authenticateUser(loginRequest)
    UserService->>AuthManager: authenticate(authToken)
    AuthManager->>UserDetailsService: loadUserByUsername(username)
    UserDetailsService->>UserRepository: findByUsername(username)
    UserRepository->>Database: SELECT FROM users
    
    alt User not found
        Database-->>UserRepository: Empty result
        UserRepository-->>UserDetailsService: Optional.empty()
        UserDetailsService-->>AuthManager: UsernameNotFoundException
        AuthManager-->>Client: 401 Unauthorized
    else User found
        Database-->>UserRepository: User data
        UserRepository-->>UserDetailsService: User entity
        UserDetailsService-->>AuthManager: UserDetails object
        AuthManager->>PasswordEncoder: matches(rawPassword, encodedPassword)
        
        alt Password does not match
            PasswordEncoder-->>AuthManager: false
            AuthManager-->>Client: 401 Unauthorized
        else Password matches
            PasswordEncoder-->>AuthManager: true
            AuthManager-->>UserService: Authentication object
            UserService->>JwtUtils: generateJwtToken(authentication)
            JwtUtils-->>UserService: JWT token string
            UserService-->>AuthController: AuthResponse DTO
            AuthController-->>Client: 200 OK with token
        end
    end
```

### 3. Protected API Request Flow

```mermaid
sequenceDiagram
    participant Client
    participant Tomcat
    participant SecurityFilter as Security Filter Chain
    participant JwtAuthFilter as JWT Filter
    participant JwtUtils
    participant UserDetailsService
    participant Controller
    participant Database

    Client->>Tomcat: GET /api/urls/my-urls with JWT
    Tomcat->>SecurityFilter: Request enters filter chain
    SecurityFilter->>JwtAuthFilter: First custom filter
    JwtAuthFilter->>JwtAuthFilter: Extract JWT from header
    
    alt No token
        JwtAuthFilter->>SecurityFilter: Continue without auth
        SecurityFilter-->>Client: 403 Forbidden
    else Token present
        JwtAuthFilter->>JwtUtils: validateJwtToken(token)
        
        alt Token invalid
            JwtUtils-->>JwtAuthFilter: false
            JwtAuthFilter->>SecurityFilter: Continue without auth
            SecurityFilter-->>Client: 403 Forbidden
        else Token valid
            JwtUtils-->>JwtAuthFilter: true
            JwtAuthFilter->>JwtUtils: getUsernameFromJwtToken(token)
            JwtUtils-->>JwtAuthFilter: username
            JwtAuthFilter->>UserDetailsService: loadUserByUsername(username)
            UserDetailsService-->>JwtAuthFilter: UserDetails object
            JwtAuthFilter->>JwtAuthFilter: Set authentication in SecurityContext
            JwtAuthFilter->>SecurityFilter: Continue filter chain
            SecurityFilter->>Controller: Forward to controller
            Controller->>Database: Process request
            Database-->>Controller: Data
            Controller-->>Client: 200 OK with response
        end
    end
```

### 4. Complete Security Filter Chain

```mermaid
graph TB
    A[HTTP Request] --> B[Tomcat Server Port 8081]
    B --> C[Spring Security Filter Chain]
    
    C --> D[CORS Filter]
    D --> E[JWT Authentication Filter]
    E --> F[Username Password Auth Filter]
    F --> G[Authorization Filter]
    
    G --> H{Path Matcher}
    
    H -->|/api/auth/public/**| I[Permit All]
    H -->|/{shortUrl}| J[Permit All]
    H -->|/api/urls/**| K[Requires Authentication]
    
    I --> L[Spring MVC DispatcherServlet]
    J --> L
    
    K --> M{Authenticated?}
    M -->|No| N[403 Forbidden]
    M -->|Yes| O{Has Required Role?}
    O -->|No| N
    O -->|Yes| L
    
    L --> P[Controller Layer]
    P --> Q[Service Layer]
    Q --> R[Repository Layer]
    R --> S[(MySQL Database)]
    
    S --> R
    R --> Q
    Q --> P
    P --> L
    L --> T[HTTP Response]
```

### 5. JWT Token Structure

```mermaid
graph LR
    A[JWT Token] --> B[Header]
    A --> C[Payload]
    A --> D[Signature]
    
    B --> B1[algorithm: HS512 type: JWT]
    C --> C1[subject: username issuedAt: timestamp expiration: timestamp roles: ROLE_USER]
    D --> D1[HMACSHA512 using SECRET_KEY]
    
    style A fill:#f9f,stroke:#333,stroke-width:2px
    style B fill:#bbf,stroke:#333,stroke-width:2px
    style C fill:#bfb,stroke:#333,stroke-width:2px
    style D fill:#fbb,stroke:#333,stroke-width:2px
```

### 6. Component Architecture

```mermaid
graph TB
    subgraph Config[Configuration Layer]
        A[WebSecurityConfig]
        B[PasswordEncoder]
        C[AuthenticationManager]
    end
    
    subgraph Security[Security Layer]
        D[JwtAuthenticationFilter]
        E[JwtUtils]
        F[UserDetailsServiceImpl]
    end
    
    subgraph Controllers[Controller Layer]
        G[AuthController]
        H[URLController]
    end
    
    subgraph Services[Service Layer]
        I[UserService]
        J[URLService]
    end
    
    subgraph Repos[Repository Layer]
        K[UserRepository]
        L[URLRepository]
    end
    
    subgraph DB[Database Layer]
        M[(MySQL Database)]
    end
    
    A -->|Configures| D
    A -->|Provides| B
    A -->|Provides| C
    D -->|Uses| E
    D -->|Uses| F
    G -->|Uses| I
    H -->|Uses| J
    I -->|Uses| K
    I -->|Uses| B
    J -->|Uses| L
    F -->|Uses| K
    K -->|Accesses| M
    L -->|Accesses| M
    C -->|Uses| F
    C -->|Uses| B
```

### 7. Security Workflow Overview

```mermaid
flowchart TD
    Start([Client Request]) --> A{Public Endpoint?}
    
    A -->|Yes| B[Skip JWT Validation]
    A -->|No| C[JWT Authentication Filter]
    
    B --> D[Controller]
    
    C --> E{JWT Token Present?}
    E -->|No| F[Return 403 Forbidden]
    E -->|Yes| G{Valid Token?}
    
    G -->|No| F
    G -->|Yes| H[Extract Username]
    
    H --> I[Load UserDetails]
    I --> J[Set Authentication Context]
    J --> K{Has Required Role?}
    
    K -->|No| F
    K -->|Yes| D
    
    D --> L[Service Layer]
    L --> M[Repository Layer]
    M --> N[(Database)]
    
    N --> O[Return Response]
    O --> P[Clear Security Context]
    P --> End([Response to Client])
    
    F --> End
    
    style Start fill:#9f9,stroke:#333,stroke-width:2px
    style End fill:#f99,stroke:#333,stroke-width:2px
    style F fill:#f66,stroke:#333,stroke-width:2px
    style D fill:#9cf,stroke:#333,stroke-width:2px
    style N fill:#fc9,stroke:#333,stroke-width:2px
```

### 8. Password Hashing Process

```mermaid
sequenceDiagram
    participant User
    participant System
    participant BCrypt
    participant DB as Database

    Note over User,DB: Registration Phase
    User->>System: password: myPassword123
    System->>BCrypt: encode(myPassword123)
    BCrypt->>BCrypt: Generate random salt
    BCrypt->>BCrypt: Hash password with salt
    BCrypt-->>System: hashed password
    System->>DB: Store hashed password
    
    Note over User,DB: Login Phase
    User->>System: password: myPassword123
    System->>DB: Retrieve hashed password
    DB-->>System: hashed password
    System->>BCrypt: matches(myPassword123, hashedPassword)
    BCrypt->>BCrypt: Extract salt from hash
    BCrypt->>BCrypt: Hash input with extracted salt
    BCrypt->>BCrypt: Compare hashes
    BCrypt-->>System: true or false
    
    alt Passwords match
        System-->>User: Login successful with JWT token
    else Passwords do not match
        System-->>User: 401 Unauthorized
    end
```

### 9. Error Handling Flow

```mermaid
graph TD
    A[Request] --> B{Exception Occurs?}
    
    B -->|UsernameNotFoundException| C[User not found in DB]
    B -->|BadCredentialsException| D[Invalid password]
    B -->|ExpiredJwtException| E[Token expired]
    B -->|SignatureException| F[Invalid token signature]
    B -->|RuntimeException| G[Business logic error]
    
    C --> H[401 Unauthorized]
    D --> H
    E --> I[403 Forbidden]
    F --> I
    G --> J[400 Bad Request]
    
    B -->|No Exception| K[Success Response]
    
    H --> L[Return Error Response]
    I --> L
    J --> L
    K --> M[Return Success Response]
    
    style C fill:#fcc,stroke:#333,stroke-width:2px
    style D fill:#fcc,stroke:#333,stroke-width:2px
    style E fill:#fcc,stroke:#333,stroke-width:2px
    style F fill:#fcc,stroke:#333,stroke-width:2px
    style G fill:#fcc,stroke:#333,stroke-width:2px
    style K fill:#cfc,stroke:#333,stroke-width:2px
```

## Key Security Features

### Authentication
- ✅ JWT Token-based authentication
- ✅ Stateless sessions (no server-side session storage)
- ✅ Token expiration (24 hours configurable)
- ✅ BCrypt password hashing with salt
- ✅ Username and email uniqueness validation

### Authorization
- ✅ Role-based access control (RBAC)
- ✅ Method-level security with @EnableMethodSecurity
- ✅ URL pattern-based access rules
- ✅ Public and protected endpoints

### Security Measures
- ✅ CSRF protection disabled (REST API, token-based)
- ✅ Stateless session management
- ✅ JWT signature verification (HMAC-SHA512)
- ✅ Token expiration validation
- ✅ Password strength with BCrypt (cost factor 10)

### Filter Chain Order
1. CORS Filter
2. **JWT Authentication Filter** (custom)
3. Username Password Authentication Filter
4. Authorization Filter
5. Exception Translation Filter

## Configuration Summary

**Security Endpoints:**
- **Public:**
  - POST /api/auth/public/register
  - POST /api/auth/public/login
  - GET /{shortUrl}

- **Protected:**
  - GET /api/urls/** (requires authentication)
  - POST /api/urls/** (requires authentication)
  - PUT /api/urls/** (requires authentication)
  - DELETE /api/urls/** (requires authentication)

**JWT Configuration:**
- Secret Key: Configured in application.properties
- Algorithm: HMAC-SHA512
- Expiration: 24 hours (86400000 ms)
- Header Format: "Authorization: Bearer {token}"

**Password Encoding:**
- Algorithm: BCrypt
- Strength: 10 rounds
- Salt: Auto-generated per password

**Database:**
- Type: MySQL
- Port: 3306
- Connection Pool: HikariCP

**Application:**
- Server Port: 8081
- Context Path: /

## Testing the Authentication

### 1. Register a new user
```bash
curl -X POST http://localhost:8081/api/auth/public/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser",
    "email": "test@example.com",
    "password": "password123"
  }'
```

### 2. Login
```bash
curl -X POST http://localhost:8081/api/auth/public/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser",
    "password": "password123"
  }'
```

**Response:**
```json
{
  "token": "eyJhbGciOiJIUzUxMiJ9...",
  "username": "testuser",
  "email": "test@example.com",
  "roles": ["ROLE_USER"]
}
```

### 3. Access protected endpoint
```bash
curl -X GET http://localhost:8081/api/urls/my-urls \
  -H "Authorization: Bearer eyJhbGciOiJIUzUxMiJ9..."
```

---

**Note**: This authentication system provides enterprise-grade security suitable for production use with proper secret key management and HTTPS in production.