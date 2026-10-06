# ParkEase Design Patterns

ParkEase uses MVC, Service Layer and Repository architectural patterns. In addition, three object-oriented design patterns are implemented explicitly so they are easy to identify in the code, class diagram, report and viva.

## 1. Strategy Pattern — Parking Fee Calculation

### Pattern type
Behavioral

### Problem
Parking charges can be calculated in different ways depending on whether only the reservation duration is known or an actual completed parking session is available. Keeping every pricing rule inside `BillService` would make billing harder to extend.

### Lecture-style structure
- **Context:** `ParkingFeeCalculator`
- **Strategy interface:** `ParkingFeeStrategy`
- **Concrete Strategy 1:** `ReservedDurationPricingStrategy`
- **Concrete Strategy 2:** `CompletedSessionPricingStrategy`
- **Client:** `BillService`
- **Result object:** `ParkingFeeCalculation`

### Flow

`BillService -> ParkingFeeCalculator -> ParkingFeeStrategy -> ParkingFeeCalculation`

The concrete strategy can vary while the client continues to use the same strategy interface.

### Benefit
New pricing algorithms, such as peak-hour, weekend, membership or event pricing, can be added as new strategies without putting long `if/else` pricing logic inside `BillService`.

---

## 2. Observer Pattern — User Notifications

### Pattern type
Behavioral

### Problem
Reservation, parking-session, billing, refund, support and feedback modules need to notify users. Those business services should not know how notification objects are built or stored.

### Lecture-style structure
- **Subject interface:** `NotificationSubject`
  - `addObserver(...)`
  - `removeObserver(...)`
  - `notifyObservers(...)`
- **Observer interface:** `NotificationObserver`
  - `update(...)`
- **Concrete Subject:** `NotificationEventPublisher`
- **Concrete Observer:** `NotificationEventListener`
- **Update data:** `UserNotificationEvent`

### Flow

`Business Service -> NotificationEventPublisher -> notifyObservers(event) -> NotificationObserver.update(event)`

The concrete observer then uses `NotificationFactory` and `NotificationService` to create and persist the in-app notification.

### Benefit
The business modules remain loosely coupled to notification persistence. Another observer, such as an email or SMS observer, can later be added without changing reservation, payment or parking-session business rules.

---

## 3. Factory Pattern — User Creation

### Pattern type
Creational

### Problem
`UserService` needs to create different concrete user objects. If it directly uses `new RegularUser(...)` and `new AdminUser(...)`, the service becomes coupled to the concrete product classes and duplicates object-creation decisions.

### Lecture-style structure
- **Product abstraction:** `User`
- **Concrete Product 1:** `RegularUser`
- **Concrete Product 2:** `AdminUser`
- **Factory:** `UserFactory`
- **Client:** `UserService`

### Flow

`UserService -> UserFactory -> User`

Depending on the requested user type, `UserFactory` returns either a `RegularUser` or `AdminUser` while `UserService` works with the common `User` abstraction.

### Benefit
User object creation is centralized and the client no longer directly instantiates the concrete subclasses. Adding another user product type later can be handled in the factory instead of spreading object-creation code through services.

---

## Supporting Notification Factory

ParkEase also contains `NotificationFactory`, which centralizes construction and normalization of `Notification` entities. It is useful object-creation encapsulation, but the **main Factory Pattern example for the report and viva is `UserFactory`**, because its structure maps directly to Product -> Concrete Products -> Factory -> Client.

`NotificationFactory` is responsible for:
- generating notification IDs
- trimming/validating notification data
- applying field length limits
- supplying a default `SYSTEM` type
- setting the creation time
- creating notifications as unread

---

## Existing Architectural Patterns

### MVC (Model-View-Controller)
- **Model:** JPA entities such as `Reservation`, `Bill`, `ParkingSlot`, `User`
- **View:** Thymeleaf templates
- **Controller:** Spring MVC controller classes

### Service Layer
Business rules are placed in services such as `ReservationService`, `BillService`, `UserService` and `ParkingSessionService` rather than controllers.

### Repository Pattern
Repository classes isolate data-access logic from business services and delegate persistence to Spring Data `JpaRepository` interfaces.

---

## Patterns Deliberately Not Forced Into ParkEase

### Singleton
Spring already manages controller, service, repository and component beans as single application-managed objects by default. A manual `private static instance` / `getInstance()` singleton would conflict with normal Spring dependency injection, so ParkEase does not add an artificial manual Singleton only to claim another pattern.

### Decorator
Decorator could be used for dynamically stacking optional billing features, but the current ParkEase requirements do not need that additional complexity. Strategy is the clearer fit for the current fee-calculation problem.

---

## Short Viva Explanation

> ParkEase explicitly uses three design patterns from our lectures. Strategy is used for parking-fee algorithms, with `ParkingFeeStrategy` as the common strategy and two concrete pricing strategies. Observer is used for notifications, with `NotificationSubject` and `NotificationObserver`, and the concrete publisher notifies the concrete listener using `update()`. Factory is used for user creation, where `UserFactory` returns either `RegularUser` or `AdminUser` through the common `User` abstraction. We also use MVC, Service Layer and Repository patterns for the overall application architecture.

## Quick Mapping for a Class Diagram

```text
STRATEGY
BillService
    |
    v
ParkingFeeCalculator (Context)
    |
    v
ParkingFeeStrategy <<interface>>
    ^                         ^
    |                         |
ReservedDuration...     CompletedSession...

OBSERVER
NotificationSubject <<interface>>
    ^
    |
NotificationEventPublisher (Concrete Subject)
    |
    | notifyObservers(event)
    v
NotificationObserver <<interface>>
    ^
    |
NotificationEventListener (Concrete Observer)
    |
    v
NotificationFactory -> NotificationService

FACTORY
UserService (Client)
    |
    v
UserFactory
    |
    v
User <<abstract product>>
    ^                 ^
    |                 |
RegularUser       AdminUser
```
