This project is a Spring Boot application with PostgreSQL integration. It includes functionality for user registration, authentication, and profile management. Key features include:  

## User Registration:  
- Validates user input and checks for existing users.
- Implements rate limiting for IP and email to prevent abuse.
- Uses PostgreSQL advisory locks to ensure registration consistency.
- Publishes events for asynchronous processing after successful registration.

## User Authentication:  
- Validates credentials and generates JWT tokens for authenticated users.
- Ensures inactive users cannot log in.

## Thread Pool Monitoring:  
- Logs JVM thread statistics and executor thread pool metrics for monitoring.

## Database Locking:  
- Uses PostgreSQL advisory locks to manage concurrent operations safely.

## Technologies:
Java, Spring Boot, Maven, PostgreSQL, JWT for security, and Lombok for boilerplate code reduction.

## Apache JMeter:
- cd apache-jmeter-5.6.3/bin
- ./jmeter.sh  # Linux/Mac
- Name: User Registration Load Test
  Number of Threads (users): 50
  Ramp-up period (seconds): 30
  Loop Count: 5
- Add HTTP Request Defaults:
  - Server Name or IP: localhost
  - Port Number: 8080
- Add HTTP Request Sampler:
- Add view results tree listener
- test-data.csv
    - Add CSV Data Set Config:
        - Filename: test-data.csv
        - Variable Names: email, password, firstName, lastName
        - For ex: 
        - user1,John1,Doe1,1234567891,192.168.1.11
        - user2,John2,Doe2,1234567892,192.168.1.12