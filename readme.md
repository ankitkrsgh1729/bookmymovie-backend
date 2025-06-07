🎬 BookMyMovie - Complete Movie Booking System
This project is a comprehensive Spring Boot application with PostgreSQL integration that implements a complete movie ticket booking system. The application provides end-to-end functionality from user management to payment processing with real-time seat availability and dynamic pricing.

🏗️ System Architecture
Core Services Implemented:
User Management Service - Registration, authentication, and profile management
Movie Management Service - Movie catalog with search and filtering
Theater Management Service - Theater, screen, and seat layout management
Show Management Service - Movie scheduling with conflict detection and dynamic pricing
Booking Service - Complete booking workflow with payment integration
Mock Payment Service - Simulated payment gateway for multiple payment methods
🎯 Key Features
🔐 User Management
Registration & Authentication: JWT-based secure authentication with role-based access control
Rate Limiting: IP and email-based rate limiting to prevent abuse
Database Locking: PostgreSQL advisory locks for concurrent operation safety
Event-Driven Architecture: Asynchronous processing with event publishing
Profile Management: Complete user profile CRUD operations
🎭 Movie Management
Movie Catalog: Comprehensive movie information with metadata
Advanced Search: Multi-criteria search with filters (genre, language, rating, etc.)
Content Management: Rich movie details including cast, crew, trailers
Status Management: Movie lifecycle management (coming soon, now showing, etc.)
🏢 Theater Management
Theater Network: Multi-city theater management
Screen Management: Multiple screens per theater with different configurations
Seat Layouts: Dynamic seat layouts with categories (Regular, Premium, VIP, Wheelchair)
Accessibility: Wheelchair-accessible seating and facilities management
Geographic Search: Location-based theater discovery
🎬 Show Management
Smart Scheduling: Automated conflict detection and validation
Dynamic Pricing: Time-based pricing with weekend, prime-time, and holiday multipliers
Bulk Operations: Efficient bulk show creation for multiple dates
Business Rules: Intermission, cleaning time, and buffer management
Analytics: Show performance and occupancy analytics
🎫 Booking System
Real-Time Availability: Live seat availability with concurrent booking handling
Complete Booking Flow: Seat selection → Pricing → Payment → Confirmation
15-Minute Expiry: Automatic booking expiry with seat release
Payment Integration: Multiple payment methods (Card, UPI, Net Banking, Wallet)
Receipt Generation: Digital receipts with QR codes for entry
Cancellation & Refunds: Time-based refund policy with automated processing
💳 Payment Processing
Mock Payment Gateway: Realistic payment simulation with success/failure rates
Multiple Payment Methods: Credit/Debit cards, UPI, Net Banking, Digital wallets
Payment Validation: Card validation and fraud prevention simulation
Refund Management: Automated refund processing with business rules
Transaction Tracking: Complete payment audit trail
🛠️ Technical Implementation
Core Technologies:
Java 17 with Spring Boot 3.x
PostgreSQL for primary database
JWT for stateless authentication
Maven for dependency management
Lombok for boilerplate code reduction
Spring Security for authorization
Spring Data JPA for data access
Spring Scheduling for background tasks
Advanced Features:
Database Optimization: Proper indexing and query optimization
Concurrent Operations: Thread-safe booking with optimistic locking
Scheduled Tasks: Automatic cleanup of expired bookings
Event Processing: Asynchronous event handling
Validation Framework: Comprehensive input validation
Error Handling: Global exception handling with proper HTTP status codes
📊 Database Schema
Core Entities:
Users - User accounts with role-based access
Movies - Movie catalog with metadata
Theaters - Theater locations and facilities
Screens - Individual screens within theaters
Seats - Detailed seat layouts with categories
Shows - Movie schedules with pricing
Bookings - Customer bookings with payment info
BookingSeats - Seat-booking relationships
Key Relationships:
Theater → Screens (One-to-Many)
Screen → Seats (One-to-Many)
Movie + Screen → Shows (Many-to-Many through Shows)
Show + User → Bookings (Many-to-Many through Bookings)
Booking → BookingSeats → Seats (Many-to-Many)
🚀 API Endpoints
Authentication APIs:
POST /api/v1/auth/register    - User registration
POST /api/v1/auth/login      - User authentication
GET  /api/v1/auth/profile    - Get user profile
PUT  /api/v1/auth/profile    - Update user profile
Movie APIs:
GET    /api/v1/movies                 - List all movies
POST   /api/v1/movies                 - Create movie (Admin)
GET    /api/v1/movies/{id}            - Get movie details
PUT    /api/v1/movies/{id}            - Update movie (Admin)
GET    /api/v1/movies/search          - Search movies
GET    /api/v1/movies/now-showing     - Currently showing movies
Theater APIs:
GET    /api/v1/theaters               - List theaters
POST   /api/v1/theaters               - Create theater (Admin)
GET    /api/v1/theaters/{id}          - Get theater details
GET    /api/v1/theaters/city/{city}   - Theaters by city
POST   /api/v1/theaters/{id}/screens  - Add screen (Admin)
Show APIs:
GET    /api/v1/shows/movie/{movieId}        - Shows by movie
POST   /api/v1/shows                        - Create show (Admin)
POST   /api/v1/shows/bulk                   - Bulk create shows (Admin)
GET    /api/v1/shows/{id}/pricing           - Get show pricing
POST   /api/v1/shows/check-conflicts       - Check scheduling conflicts
Booking APIs:
POST   /api/v1/bookings                     - Create booking
POST   /api/v1/bookings/validate            - Validate booking request
POST   /api/v1/bookings/pricing             - Calculate pricing
POST   /api/v1/bookings/payment             - Process payment
POST   /api/v1/bookings/{ref}/confirm       - Confirm booking
GET    /api/v1/bookings/user/{userId}       - User's bookings
POST   /api/v1/bookings/cancel              - Cancel booking
GET    /api/v1/bookings/show/{showId}/seats - Seat availability
💰 Business Logic
Dynamic Pricing Engine:
Base Price: Configurable per show/seat category
Time Multipliers: Morning (1.0x), Prime time (1.8x-2.0x)
Day Multipliers: Weekdays (1.0x), Weekends (1.5x-1.6x)
Special Events: Premieres (2.5x), Holidays (2.0x)
Convenience Fee: 5% of base amount
Taxes: 18% GST on final amount
Booking Business Rules:
Seat Limits: Maximum 10 seats per booking
Expiry Time: 15 minutes to complete payment
Cancellation: 2+ hours before show for refund
Refund Tiers: 24+ hrs (100%), 2-24 hrs (80%), <2 hrs (0%)
Buffer Times: Cleaning time between shows
Payment Processing:
Success Rates: Card (95%), UPI (90%), Net Banking (85%), Wallet (92%)
Validation: Card number, CVV, expiry date validation
Failure Simulation: Realistic failure scenarios for testing
Refund Processing: Automated refund with 3-5 business day simulation
🧪 Testing & Quality Assurance
Load Testing with Apache JMeter:
bash
# Navigate to JMeter installation
cd apache-jmeter-5.6.3/bin
./jmeter.sh  # Linux/Mac

# Test Configuration:
Name: Movie Booking Load Test
Number of Threads (users): 100
Ramp-up period (seconds): 60
Loop Count: 10

# HTTP Request Defaults:
Server Name or IP: localhost
Port Number: 8080

# Test Scenarios:
- User Registration: 50 concurrent users
- Movie Search: 100 concurrent users
- Booking Creation: 75 concurrent users
- Payment Processing: 50 concurrent users
  Test Data Setup:
  csv
# test-data.csv for user registration
email,firstName,lastName,phoneNumber,password
user1@test.com,John1,Doe1,9876543201,Pass123!
user2@test.com,John2,Doe2,9876543202,Pass123!
user3@test.com,John3,Doe3,9876543203,Pass123!
CSV Data Set Config:
Filename: test-data.csv
Variable Names: email,firstName,lastName,phoneNumber,password
Delimiter: , (comma)
Recycle on EOF: True
Stop thread on EOF: False
🎯 Business Metrics & Analytics
Key Performance Indicators:
Booking Conversion Rate: Seat selection to payment completion
Revenue per Show: Dynamic pricing effectiveness
Occupancy Rates: Theater utilization by time/day
Payment Success Rates: Gateway performance monitoring
Cancellation Rates: Customer behavior analysis
Popular Movies/Theaters: Business intelligence insights
Real-Time Monitoring:
Thread Pool Metrics: JVM and executor monitoring
Database Performance: Query execution times
API Response Times: Endpoint performance tracking
Concurrent Booking Conflicts: Race condition monitoring
Payment Gateway Health: Transaction success rates
🔧 Configuration
Application Properties:
yaml
# Database Configuration
spring.datasource.url=jdbc:postgresql://localhost:5432/bookmymovie
spring.datasource.username=your_username
spring.datasource.password=your_password

# JPA Configuration
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=false
spring.jpa.properties.hibernate.format_sql=true

# JWT Configuration
jwt.secret=your-jwt-secret-key
jwt.expiration=86400000

# Rate Limiting
rate.limit.ip=10
rate.limit.email=3
rate.limit.window=300000
Environment Setup:
bash
# Prerequisites
- Java 17+
- PostgreSQL 13+
- Maven 3.8+

# Database Setup
createdb bookmymovie
psql -d bookmymovie -c "CREATE EXTENSION IF NOT EXISTS pgcrypto;"

# Application Startup
mvn clean install
mvn spring-boot:run
📈 Scalability Considerations
Current Implementation Supports:
Concurrent Users: 100+ simultaneous bookings
Database Connections: Connection pooling configured
Memory Management: Optimized JPA queries with pagination
Caching Strategy: Ready for Redis integration
Horizontal Scaling: Stateless JWT authentication
Future Enhancements:
Redis Caching: Session management and frequently accessed data
Message Queues: RabbitMQ/Kafka for event processing
Microservices: Service decomposition for independent scaling
CDN Integration: Static content delivery optimization
Database Sharding: Multi-region data distribution
🎬 Getting Started
Quick Setup:
Clone Repository and install dependencies
Setup PostgreSQL and create database
Run Application with mvn spring-boot:run
Create Test Data using provided cURL commands
Test Booking Flow with Postman/cURL scripts
Sample Booking Flow:
Register user and get JWT token
Browse movies and select show
Check seat availability
Calculate pricing with fees
Create booking with 15-min expiry
Process payment through mock gateway
Receive confirmation with QR code
Ready for production deployment with comprehensive booking functionality! 🎭🎫

📞 Support & Documentation
API Documentation: Available at /swagger-ui/index.html (when enabled)
Health Checks: Available at /actuator/health
Metrics: Available at /actuator/metrics
Database Schema: Auto-generated through Hibernate DDL
