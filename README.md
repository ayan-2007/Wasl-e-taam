# Wasl-e-Ta'am (وصلِ طعام) - Smart Food Surplus Redistribution Platform

A JavaFX desktop application connecting food donors with recipient organizations to reduce food waste and fight hunger.

## Prerequisites

- **JDK 21** (LTS) or higher (minimum JDK 17)
- **Maven 3.9+**

## Quick Start

### Build and Run
```bash
mvn clean javafx:run
```

### Run Tests
```bash
mvn clean test
```

### Package as JAR
```bash
mvn clean package
java --module-path /path/to/javafx-sdk-21/lib --add-modules javafx.controls,javafx.fxml -jar target/wasl-e-taam-1.0.0.jar
```

## Architecture

This application follows a **3-layer architecture** with **6 Design Patterns**:

### Layers
1. **Presentation Layer** (`com.waslataam.ui.*`, `com.waslataam.viewmodel.*`)
   - JavaFX FXML views, Controllers, CSS styling
   - ViewModels with JavaFX Properties for data binding

2. **Business Layer** (`com.waslataam.domain.*`, `factory.*`, `strategy.*`, `state.*`, `command.*`, `chain.*`, `observer.*`, `coordination.*`)
   - Pure Java - zero JavaFX dependencies
   - Domain models, business logic, design patterns

3. **Data Layer** (`com.waslataam.repository.*`)
   - Repository interfaces with in-memory implementations
   - Ready for JDBC/JSON-file implementations

### Design Patterns Implemented

| Pattern | Location | Purpose |
|---------|----------|---------|
| **Observer** | `observer/` | Real-time notifications, dashboard updates, expiry watching |
| **Strategy** | `strategy/` | Swappable matching algorithms (Nearest/Expiry/Capacity First) |
| **Factory Method** | `factory/` | Dynamic creation of Donation/User subtypes |
| **State** | `state/` | 7-state donation lifecycle (Pending→Available→Claimed→InTransit→Delivered/Expired/Rejected) |
| **Command** | `command/` | Undoable actions, audit trail |
| **Chain of Responsibility** | `chain/` | Multi-step donation verification (Expiry→Quantity→Location→Approval) |

## User Roles

| Role | Capabilities |
|------|--------------|
| **Donor** | Post food donations, view donation history |
| **Recipient** | Browse available donations, claim/cancel claims |
| **Volunteer** | Confirm pickups, confirm deliveries, toggle availability |
| **Admin** | View dashboard, switch matching strategy, view audit reports, undo actions, view impact metrics |

## Use Cases (UC-01 through UC-10)

1. **UC-01**: Post Food Donation (Donor)
2. **UC-02**: Claim a Donation (Recipient)
3. **UC-03**: Auto-Match to Recipient (System)
4. **UC-04**: Cancel a Claim (Recipient)
5. **UC-05**: Volunteer Pickup & Delivery (Volunteer)
6. **UC-06**: View Impact Dashboard (Any User)
7. **UC-07**: Handle Expired Donation (System)
8. **UC-08**: Switch Matching Strategy (Admin)
9. **UC-09**: Undo Incorrect Action (Admin)
10. **UC-10**: Generate Audit Report (Admin)

## Demo Script

1. Launch app: `mvn clean javafx:run`
2. **Login as Donor**: Enter name, email, select "Donor" role
3. **Post Donation**: Click "Post New Donation" → Fill form → Submit
4. **Logout** → **Login as Recipient**: Select "Recipient" role
5. **Claim Donation**: View available donations → Click "Claim"
6. **Logout** → **Login as Volunteer**: Select "Volunteer" role
7. **Pickup & Deliver**: Click "Confirm Pickup" → "Confirm Delivery"
8. **Logout** → **Login as Admin**: Select "Admin" role
9. **View Impact Dashboard**: See live metrics update
10. **Switch Strategy**: Go to Strategy Switcher → Change algorithm
11. **View Audit Report**: See complete audit trail with undo option

## Project Structure

```
src/main/java/com/waslataam/
├── Main.java                          # Application entry point
├── domain/                            # Domain models & exceptions
│   ├── Donation.java                  # Abstract base + 3 subtypes
│   ├── User.java                      # Abstract base + 4 subtypes
│   ├── Location.java                  # Value object with haversine distance
│   ├── DonationStatus.java            # 7-state enum
│   ├── UserRole.java                  # 4-role enum
│   └── exceptions/                    # Custom exceptions
├── factory/                           # Factory Method pattern
│   ├── DonationFactory.java
│   ├── UserFactory.java
│   ├── DonationParams.java
│   └── UserParams.java
├── strategy/                          # Strategy pattern
│   ├── MatchingStrategy.java
│   ├── NearestFirstStrategy.java
│   ├── ExpiryFirstStrategy.java
│   ├── CapacityBasedStrategy.java
│   └── MatchingEngine.java
├── state/                             # State pattern (7 states)
│   ├── DonationState.java
│   ├── PendingReviewState.java
│   ├── AvailableState.java
│   ├── ClaimedState.java
│   ├── InTransitState.java
│   ├── DeliveredState.java
│   ├── ExpiredState.java
│   └── RejectedState.java
├── command/                           # Command pattern
│   ├── DonationCommand.java
│   ├── ClaimDonationCommand.java
│   ├── CancelClaimCommand.java
│   ├── MarkPickedUpCommand.java
│   ├── MarkDeliveredCommand.java
│   ├── CommandInvoker.java
│   └── AuditTrail.java
├── chain/                             # Chain of Responsibility
│   ├── VerificationHandler.java
│   ├── VerificationResult.java
│   ├── ExpiryCheckHandler.java
│   ├── QuantityCheckHandler.java
│   ├── LocationCheckHandler.java
│   └── ApprovalHandler.java
├── observer/                          # Observer pattern
│   ├── SystemObserver.java
│   ├── SystemEvent.java
│   ├── RecipientNotifier.java
│   ├── AuditLogger.java
│   ├── ImpactDashboardObserver.java
│   └── ExpiryWatcher.java
├── coordination/                      # Orchestration
│   ├── DonationCoordinator.java
│   └── VolunteerDispatcher.java
├── repository/                        # Data access
│   ├── DonationRepository.java
│   ├── UserRepository.java
│   ├── InMemoryDonationRepo.java
│   └── InMemoryUserRepo.java
├── viewmodel/                         # JavaFX ViewModels
│   ├── DashboardViewModel.java
│   ├── DonationListViewModel.java
│   ├── AuditLogViewModel.java
│   └── SessionContext.java
└── ui/
    ├── resources/
    │   ├── fxml/                      # 10 FXML views
    │   └── css/theme.css              # Green/Gold theme
    └── controllers/                   # 10 Controllers
```

## Technology Stack

- **Java 21** with JPMS (module-info.java)
- **JavaFX 21** (Controls, FXML, Graphics, Base)
- **ControlsFX 11.2** for enhanced controls
- **Jackson 2.17** for JSON serialization
- **SLF4J + Logback** for logging
- **JUnit 5 + TestFX** for testing

## Configuration

- Logging: `src/main/resources/logback.xml` (console + rolling file to `logs/waslataam.log`)
- Theme: `src/main/java/com/waslataam/ui/resources/css/theme.css` (Green #2d7a3a / Gold #c9962a)
- Expiry watcher interval: 30 seconds (configurable in `Main.java`)

## Testing

```bash
# Run all tests
mvn test

# Run with coverage (requires jacoco)
mvn test jacoco:report
```

## Extending the Application

### Add New Donation Type
1. Create new class extending `Donation` in `domain/`
2. Add to `DonationType` enum
3. Add case in `DonationFactory.create()`
4. Add fields in `PostDonationForm.fxml` and controller

### Add New Matching Strategy
1. Implement `MatchingStrategy` interface
2. Add to `StrategySwitcherView.fxml` ComboBox
3. Add case in `StrategySwitcherController.handleStrategyChange()`

### Add New Verification Step
1. Extend `VerificationHandler`
2. Insert into chain in `Main.java`

### Swap Repository Implementation
1. Implement `DonationRepository` / `UserRepository` interfaces
2. Update `Main.java` to use new implementations
3. No changes needed in other layers (Dependency Inversion)

## License

This project is developed for educational purposes as part of a Software Design & Architecture course.