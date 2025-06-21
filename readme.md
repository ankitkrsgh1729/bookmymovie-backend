# 🎬 BookMyMovie - Movie Booking System

A comprehensive Spring Boot application for movie ticket booking with real-time seat availability, dynamic pricing, and secure payment processing.

## 🚀 Features

- **User Management**: JWT authentication with role-based access control
- **Movie Catalog**: Search and filter movies by genre, language, rating
- **Theater Management**: Multi-city theaters with dynamic seat layouts
- **Smart Booking**: Real-time seat availability with 15-minute booking expiry
- **Dynamic Pricing**: Time-based pricing with weekend and prime-time multipliers
- **Payment Integration**: Mock payment gateway supporting multiple payment methods
- **Concurrency Control**: PostgreSQL advisory locks, optimistic/pessimistic locking, Redis distributed locks

## 🛠 Tech Stack

- **Backend**: Java 17, Spring Boot 3.x, Spring Security, Spring Data JPA
- **Database**: PostgreSQL with connection pooling
- **Authentication**: JWT tokens
- **Build Tool**: Maven
- **Additional**: Lombok, Validation API

## 📋 Prerequisites

- Java 17+
- PostgreSQL 13+
- Maven 3.8+
- Redis (optional, for distributed deployments)

## ⚡ Quick Start

### 1. Clone Repository
```bash
git clone https://github.com/your-username/bookmymovie.git
cd bookmymovie
```

### 2. Database Setup
```bash
# Create database
createdb bookmymovie

# Enable extensions
psql -d bookmymovie -c "CREATE EXTENSION IF NOT EXISTS pgcrypto;"
```

### 3. Configure Application
Update `src/main/resources/application.properties`:
```properties
# Database
spring.datasource.url=jdbc:postgresql://localhost:5432/bookmymovie
spring.datasource.username=your_username
spring.datasource.password=your_password

# JWT
jwt.secret=your-jwt-secret-key-minimum-256-bits
jwt.expiration=86400000

# Rate Limiting
rate.limit.ip=10
rate.limit.email=3
```

### 4. Run Application
```bash
mvn clean install
mvn spring-boot:run
```

Application starts on `http://localhost:8080`

## 🔒 Concurrency & Locking

The application implements multiple locking strategies for handling concurrent bookings:

### PostgreSQL Advisory Locks
- Application-level locking for booking processes
- Automatic cleanup when session ends
- Used for preventing double bookings

### Optimistic Locking
- JPA `@Version` annotation on entities
- Detects concurrent modifications
- Suitable for low-conflict scenarios

### Pessimistic Locking
- Database row-level locking
- `@Lock(LockModeType.PESSIMISTIC_WRITE)` on repositories
- Used for critical seat booking operations

### Redis Distributed Locks
- Cross-instance coordination for clustered deployments
- Automatic expiration with TTL
- Prevents booking conflicts across multiple servers

## 📡 API Endpoints

### Authentication
```
POST /api/v1/auth/register    - User registration
POST /api/v1/auth/login       - User login
```

### Movies
```
GET  /api/v1/movies           - List movies
GET  /api/v1/movies/search    - Search movies
GET  /api/v1/movies/{id}      - Movie details
```

### Theaters
```
GET  /api/v1/theaters         - List theaters
GET  /api/v1/theaters/city/{city} - Theaters by city
```

### Shows
```
GET  /api/v1/shows/movie/{movieId} - Shows by movie
GET  /api/v1/shows/{id}/pricing    - Show pricing
```

### Bookings
```
POST /api/v1/bookings         - Create booking
POST /api/v1/bookings/payment - Process payment
GET  /api/v1/bookings/user/{userId} - User bookings
POST /api/v1/bookings/cancel  - Cancel booking
```

## 💰 Business Rules

### Dynamic Pricing
- **Time-based**: Morning (1.0x), Afternoon (1.2x), Evening (1.8x), Night (2.0x)
- **Day-based**: Weekdays (1.0x), Saturday (1.6x), Sunday (1.5x)
- **Convenience Fee**: 5% of base amount
- **Taxes**: 18% GST on final amount

### Booking Rules
- Maximum 10 seats per booking
- 15-minute payment window
- Cancellation allowed 2+ hours before show
- Refund policy: 24+ hrs (100%), 2-24 hrs (80%), <2 hrs (0%)

## 🧪 Testing

```bash
# Run tests
mvn test

# Run with coverage
mvn test jacoco:report

# Integration tests
mvn verify -P integration-tests
```

### Load Testing
Use Apache JMeter for performance testing:
- 100 concurrent users
- Booking flow simulation
- Payment gateway testing

## 🐳 Docker Deployment

```bash
# Build image
docker build -t bookmymovie .

# Run with docker-compose
docker-compose up -d
```

## 📊 Monitoring

- **Health Check**: `/actuator/health`
- **Metrics**: `/actuator/metrics`
- **API Docs**: `/swagger-ui/index.html` (if enabled)

## 🔧 Configuration

### Environment Variables
```bash
DB_USERNAME=your_db_user
DB_PASSWORD=your_db_password
JWT_SECRET=your_jwt_secret
REDIS_URL=redis://localhost:6379
```

### Production Settings
```properties
spring.profiles.active=production
spring.jpa.hibernate.ddl-auto=validate
logging.level.com.bookmymovie=INFO
```

## 🤝 Contributing

1. Fork the repository
2. Create feature branch: `git checkout -b feature/new-feature`
3. Commit changes: `git commit -m 'Add new feature'`
4. Push to branch: `git push origin feature/new-feature`
5. Create Pull Request

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 📞 Support

- Create an [issue](https://github.com/your-username/bookmymovie/issues) for bugs
- Check [wiki](https://github.com/your-username/bookmymovie/wiki) for detailed docs
- Email: support@bookmymovie.com