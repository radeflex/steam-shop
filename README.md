# 🎮 Steam Shop

A fullstack web application for buying and selling Steam accounts — with a user storefront, shopping cart, payment system, admin panel, and email confirmation flow.

![Java](https://img.shields.io/badge/Java-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-6DB33F?style=for-the-badge&logo=spring-boot&logoColor=white)
![React](https://img.shields.io/badge/React-20232A?style=for-the-badge&logo=react&logoColor=61DAFB)
![Docker](https://img.shields.io/badge/Docker-2496ED?style=for-the-badge&logo=docker&logoColor=white)

---

## 📋 Table of Contents

- [About](#about)
- [Features](#features)
- [Tech Stack](#tech-stack)
- [Architecture](#architecture)
- [Project Structure](#project-structure)
- [Getting Started](#getting-started)

---

## About

Steam Shop is a fullstack e-commerce platform for purchasing Steam accounts. It features JWT-based authentication with email confirmation, a shopping cart and payment flow, a purchase history tracker, an admin dashboard, and a notification system.

---

## Features

### 👤 User
- Register and login with **email confirmation**
- Browse available Steam accounts (products) with images
- Add items to **shopping cart** and checkout
- Full **payment flow via [Yookassa](https://yoomoney.ru)** with status tracking
- View purchase history
- View and edit personal profile

### 🛠️ Admin Panel
- Manage products (create, edit, delete) with image upload
- Manage Steam account inventory (`AccountStatus` tracking)
- View and manage all registered users
- Create and send typed **notifications** to users (with read tracking)

### 🔐 Security
- **JWT authentication** (`JwtService` + filter chain)
- Role-based access control (`UserRole`: USER / ADMIN)
- Email confirmation tokens (`EmailConfirmation` entity)

---

## Tech Stack

### Backend
- **Java + Spring Boot** — REST API
- **Spring Security + JWT** — authentication & authorization
- **Spring Data JPA** — data access layer (repositories)
- **Spring Data Redis** — data caching
- **MailService** — email sending (confirmation, notifications)
- **Maven** — build tool

### Frontend
- **React** (Create React App) — SPA
- **Context API** — global auth & cart state
- **Nginx** — production static file serving

### DevOps
- **Docker + Docker Compose** — containerization
- **GitHub Actions** — CI/CD pipeline

---

## Architecture

The backend follows a clean layered architecture:

```
Controller → Service → Repository → Database
```

| Layer | Responsibility |
|---|---|
| `http/controller` | REST endpoints, request/response handling |
| `http/filter` | JWT filter, request preprocessing |
| `http/handler` | Exception & error handling |
| `service` | Business logic |
| `repository` | JPA data access |
| `entity` | JPA domain models |
| `dto` | Data transfer objects |
| `mapper` | Entity ↔ DTO conversion |
| `configuration` | Spring Security, beans config |
| `props` | Typed config properties |
| `utils` | Utility helpers |

---

## Project Structure

```
steam-shop/
├── backend/
│   └── src/main/java/by/radeflex/steamshop/
│       ├── configuration/
│       ├── dto/
│       ├── entity/
│       │   ├── User.java / UserRole.java
│       │   ├── Account.java / AccountStatus.java
│       │   ├── Product.java
│       │   ├── UserProduct.java / UserProductHistory.java
│       │   ├── Payment.java / PaymentItem.java
│       │   ├── PaymentStatus.java / PaymentType.java / PaymentSource.java
│       │   ├── Notification.java / NotificationRead.java / NotificationType.java
│       │   └── EmailConfirmation.java
│       ├── http/
│       │   ├── controller/
│       │   │   ├── AuthController.java
│       │   │   ├── UserController.java
│       │   │   ├── ProductController.java
│       │   │   ├── AccountController.java
│       │   │   ├── CartController.java
│       │   │   ├── PaymentController.java
│       │   │   ├── NotificationController.java
│       │   │   ├── EmailConfirmationController.java
│       │   │   └── ImageController.java
│       │   ├── filter/
│       │   └── handler/
│       ├── mapper/
│       ├── props/
│       ├── repository/
│       │   ├── UserRepository.java
│       │   ├── AccountRepository.java
│       │   ├── ProductRepository.java
│       │   ├── UserProductRepository.java / UserProductHistoryRepository.java
│       │   ├── PaymentRepository.java / PaymentItemRepository.java
│       │   ├── NotificationRepository.java / NotificationReadRepository.java
│       │   └── EmailConfirmationRepository.java
│       ├── service/
│       │   ├── AuthService.java / JwtService.java
│       │   ├── UserService.java / CurrentUserService.java
│       │   ├── ProductService.java / AccountService.java
│       │   ├── CartService.java / PaymentService.java
│       │   ├── NotificationService.java
│       │   ├── EmailConfirmationService.java
│       │   ├── MailService.java
│       │   └── ImageService.java
│       ├── utils/
│       └── SteamShopApplication.java
│
├── frontend/
│   └── src/
│       ├── api/
│       ├── components/
│       │   ├── AppNavbar.jsx / ProductCard.jsx / CartCard.jsx
│       │   └── admin/
│       ├── context/
│       ├── layouts/
│       └── pages/
│           ├── ProductsPage.jsx / CartPage.jsx
│           ├── LoginPage.jsx / RegisterPage.jsx
│           ├── ConfirmEmailPage.jsx
│           ├── CurrentUserPage.jsx / UserEditPage.jsx
│           ├── status/
│           └── admin/
│               ├── AccountsPage.jsx
│               ├── ProductsPage.jsx / ProductFormPage.jsx
│               └── NotificationsPage.jsx / NotificationCreatePage.jsx
│
├── .github/workflows/
└── docker-compose.yml
```

---

## Getting Started

### Prerequisites

- [Docker](https://www.docker.com/) & Docker Compose
- [Java 17+](https://adoptium.net/) *(for local backend dev)*
- [Node.js 18+](https://nodejs.org/) *(for local frontend dev)*

### Run with Docker

```bash
git clone https://github.com/radeflex/steam-shop.git
cd steam-shop
docker compose up -d // needs .env configuration, see docker-compose.yml
```

- **Frontend** → http://localhost:3000
- **Backend API** → http://localhost:8443

```bash
docker compose down   # to stop
```

### Run Locally

**Backend:**
```bash
cd backend
./mvnw spring-boot:run
```

**Frontend:**
```bash
cd frontend
npm install
npm start
```

---

## Author

Made by [@radeflex](https://github.com/radeflex)
