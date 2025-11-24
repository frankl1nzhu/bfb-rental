# 🚗 BFB Rental Management System

A robust Car Rental Management System built withSpring Boot, designed to handle complex business rules, conflict detection, and automated state management.

## 🛠 Tech Stack

* **Backend:** Java 17, Spring Boot 3 (Web, Data JPA, Validation).
* **Frontend:** Thymeleaf, Bootstrap 5 (Responsive UI).
* **Database:** H2 In-Memory Database (for easy testing).
* **Tools:** Swagger UI (API Docs), Lombok, Maven.

## ✨ Key Features

### 1. Core Management (CRUD)

* **Clients:** Unique identification logic.
* **Vehicles:** Stock management with pricing and status tracking.
* **Contracts:** Rental agreements linking clients and vehicles.

### 2. Advanced Business Logic

* **Collision Detection:** Prevents double-booking of the same vehicle for overlapping dates.
* **Smart Pricing:** Automatic calculation based on daily rates and duration.
* **Automated Scheduler:** Automatically promotes contracts to `EN_COURS` (In Progress) when the start date arrives.

### 3. Architecture & Patterns

* **Observer Pattern:** When a vehicle is reported as **Broken**, the system automatically detects and cancels all pending contracts for that car via Spring Events.
* **Chain Reaction Logic:** If a contract is marked as **Late**, the system checks for upcoming conflicts and cancels the next booking if the car won't be available in time.

### 4. Developer Tools

* **Data Generator:** One-click generation of random Clients, Vehicles, and Contracts for stress testing.
* **Scenario Builder:** Pre-built button to generate specific "Conflict/Late" scenarios for demonstration purposes.

## 🚀 How to Run

**Deployment website** : https://bfb-rental.onrender.com/

1. **Clone the repository**
   ```bash
   git clone https://github.com/frankl1nzhu/bfb-rental.git
   ```
2. **Run the application**
   ```bash
   mvn spring-boot:run
   ```
3. **Access the Application**
   * **Web UI:** [http://localhost:8080](http://localhost:8080)
   * **Swagger API:** [http://localhost:8080/swagger-ui/index.html](http://localhost:8080/swagger-ui/index.html)
   * **H2 Console:** [http://localhost:8080/h2-console](http://localhost:8080/h2-console)

## 🧪 Demo Scenarios

**Scenario A: The Breakdown (Observer Pattern)**

1. Go to **Véhicules** and pick a car.
2. Click **⚠️Déclarer une panne**.
3. Go to **Contracts**: Observe that pending contracts for this car are now `ANNULÉ`.

**Scenario B: The Late Return (Chain Reaction)**

1. Go to **Contrats**.
2. Click **Démo Conflit**.
3. Identify the contract marked `EN_COURS` (ended yesterday) and the one `EN_ATTENTE` (starts tomorrow).
4. Click **⏳ Marquer comme en retard** on the first contract.
5. Observe the second contract automatically switching to `ANNULÉ`.
