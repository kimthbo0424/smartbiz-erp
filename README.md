# SmartBiz ERP

Spring Boot–based ERP system for small business inventory and business management.

---

## Project Overview

SmartBiz ERP is a team-based backend project focused on designing and implementing
ERP-style domains such as product, client, and inventory management.

The project was developed to practice backend application design,
domain modeling, and data consistency handling using Spring Boot.

---

## Tech Stack

- Java (JDK 21)
- Spring Boot
- Spring Data JPA
- Gradle
- MySQL
- Redis
- Thymeleaf
- Spring Security

---

## Team

- Team size: 3 developers
- Backend-focused team project
- Responsibilities divided by domain

---

## Duration

- Development period: 2025.11.10 ~ 2026.01.05

---

## Goals & Outcome

### Goals
- Design ERP-style backend architecture
- Implement core business domains with clear responsibilities
- Ensure data consistency using transaction management

### Outcome
- Successfully implemented product, client, and inventory domains
- Applied DTO-based API design and service-layer business logic
- Achieved stable inventory flow handling with transactional consistency

---

## My Contribution

In this team of three, I was responsible for the following backend domains:
- **Product**: product management APIs and domain logic
- **Client**: client (customer/supplier) management and status handling
- **Inventory**: inventory flow management including in/out, movement, and adjustment

My contributions included:
- Designing service-layer business logic for assigned domains  
  (담당 도메인의 서비스 레이어 비즈니스 로직 설계)
- Managing entity persistence while separating API DTOs from domain entities  
  (API DTO와 도메인 엔티티를 분리하여 영속성 로직을 관리)
- Managing transactions and ensuring data consistency across inventory operations  
  (재고 관련 트랜잭션을 관리하고 데이터 정합성을 보장)
- Coordinating API request/response structures and domain responsibilities with team members  
  (팀원들과 API 요청/응답 구조 및 도메인 책임 범위를 조율)

---

## Project Structure
```
src
└─ main
├─ java
│ └─ com.smartbiz.erp
│ ├─ controller
│ ├─ service
│ ├─ repository
│ └─ entity
└─ resources
├─ templates
└─ application.properties

```

---

## 💡 What I Learned

- REST API 설계 원칙
- 트랜잭션 처리와 데이터 정합성
- JPA 연관관계 설계
- Redis 캐시 적용
- 인덱스 설계