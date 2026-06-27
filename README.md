# E-Commerce Management System

A secure and scalable E-Commerce backend application built using Java, Spring Boot, Spring Security, JWT, MySQL, and SSLCommerz. The system provides user authentication, product management, shopping cart functionality, order processing, and secure online payment integration.

## Features

### Authentication & Authorization

* User Registration and Login
* JWT-based Authentication
* Role-Based Access Control (ADMIN, CUSTOMER)

### Product Management

* Add, Update, Delete Products
* View Product Catalog
* Category Management
* Product Search and Filtering

### Shopping Cart

* Add Products to Cart
* Update Cart Items
* Remove Products from Cart
* View Cart Summary

### Order Management

* Place Orders
* View Order History
* Track Order Status

### Payment Integration

* SSLCommerz Payment Gateway Integration
* Secure Payment Processing
* Payment Verification and Order Confirmation

### Reviews & Ratings
* Submit Product Reviews
* Rate Products
* View Customer Feedback and Ratings

### API Documentation

* Swagger/OpenAPI Documentation

## Technology Stack

* Java
* Spring Boot
* Spring Security
* JWT
* Hibernate / Spring Data JPA
* MySQL
* SSLCommerz
* Swagger/OpenAPI
* Maven

## Project Architecture

The project follows a layered architecture:

* Controller Layer
* Service Layer
* Repository Layer
* Entity Layer
* Security Layer

## Database Entities

* User
* Role
* Product
* Category
* Cart
* CartItem
* Order
* OrderItem
* Payment

## Getting Started

### Prerequisites

* Java 17+
* Maven
* MySQL

### Installation

1. Clone the repository

```bash
git clone https://github.com/Prantascode/Ecommerce-Website.git
```

2. Configure MySQL database settings in `application.properties`.

3. Build and run the project

```bash
mvn clean install
mvn spring-boot:run
```

4. Access Swagger Documentation

```text
http://localhost:8080/swagger-ui/index.html
```

## Future Improvements

* Email Notifications
* Product Reviews and Ratings
* Wishlist Functionality
* Inventory Management
* Docker Deployment

## Author

Pranta Saha
