# SmartBiz ERP

Spring Boot–based ERP system for small business inventory and business management.

---

## Project Overview
SmartBiz ERP is a team-based backend project developed
to practice Spring Boot application design and ERP-style domain modeling.

---

## My Role

This project was developed by a team of three, with responsibilities divided by domain.

I was responsible for designing and implementing the following backend domains:
- **Product**: product management APIs and domain logic
- **Client**: client (customer/supplier) management and status handling
- **Inventory**: inventory flow management including in/out, movement, and adjustment

My contributions included:
- Designing service-layer business logic for assigned domains
- Managing entity persistence while separating API DTOs from domain entities
- Managing transactions and ensuring data consistency across inventory operations
- Defining domain boundaries and coordinating API interfaces with other team members

---

## Tech Stack
- Java 17
- Spring Boot
- Spring Data JPA
- Gradle
- MySQL
- Redis
- Thymeleaf
- Spring Security

---

## Core Features

### Inventory Management
- Inventory status 조회
- 입고 / 출고 처리
- 재고 이동 및 조정
- 재고 취소 처리

### Product & Category
- 상품 CRUD
- 카테고리 관리
- 상품 상태 관리

### Client Management
- 거래처(Customer / Supplier) 관리
- 거래처 활성/비활성 처리

### Authentication
- 로그인 / 로그아웃
- 인증 기반 접근 제어

---

## Project Structure
```
src
 └─ main
    ├─ java
    │   └─ com.smartbiz.erp
    │       ├─ controller
    │       ├─ service
    │       ├─ repository
    │       └─ entity
    └─ resources
        ├─ templates
        └─ application.properties
```

---

## What I Learned
- REST API 설계 원칙
- 트랜잭션 처리와 데이터 정합성
- JPA 연관관계 설계
- Redis 캐시 적용