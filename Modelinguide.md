# Modeling Guide — FoodBridge Use Case & Sequence Diagrams

## Overview

This guide provides the specification for drawing a **UML Use Case Diagram** for the FoodBridge Smart Food Surplus Redistribution Platform, plus copy-paste-ready **PlantUML prompts for a detailed design-level sequence diagram** for every use case. Together they capture the functional scope, the actors, and the object interactions that implement each scenario.

The use case diagram shows *what* the system does; the sequence diagrams in section **g** show *how the controllers, domain objects, and design-pattern collaborators realize each use case*. Both use the same 12 canonical UC IDs defined in section **c**.

---

## a. System Boundary (Overall Scope)

Draw a **rectangle** (the system boundary box) labeled:

```
FoodBridge — Smart Food Surplus Redistribution Platform
```

Everything inside this rectangle represents software functionality. All actors (stick figures) are placed **outside** the rectangle. All use cases (ovals) are placed **inside** the rectangle.

The scope of the system includes:
- Donation posting, deletion, search/filtering, verification, and lifecycle management
- Recipient claiming and cancellation
- Volunteer pickup and delivery workflows
- Smart auto-matching with swappable algorithms
- Real-time impact dashboard
- Expiry monitoring and auto-handling
- Admin controls: strategy switching, undo, audit reporting

---

## b. Actors

Place the following **4 primary actors** outside the system boundary box, connected to their use cases via solid lines (association relationships).

| Actor | Description | Notation |
|---|---|---|
| **Donor** | Restaurants, hotels, grocery stores, or individuals who post surplus food | Stick figure + label "Donor" |
| **Recipient** | NGOs, food banks, shelters, orphanages who claim donations | Stick figure + label "Recipient" |
| **Volunteer** | Logistics volunteers who pick up and deliver donations | Stick figure + label "Volunteer" |
| **Admin** | Platform administrator who manages strategies, undo, and audit reports | Stick figure + label "Admin" |

Additionally, include **1 secondary actor**:

| Actor | Description | Notation |
|---|---|---|
| **System** (or **Time**) | Represents automatic time-triggered behavior (auto-matching, expiry handling) | Stick figure + label "System" or "Clock" |

---

## c. Use Cases

Draw the following **12 use case ovals** inside the system boundary. Each oval contains the UC name.

| UC | Use Case | Primary Actor | Summary |
|---|---|---|---|
| **UC-01** | Post Food Donation | Donor | Donor submits surplus food; the system creates, verifies, and publishes the donation. |
| **UC-02** | Delete a Donation | Donor | Donor removes one of their own donations that has not moved into an in-progress or terminal delivery state. |
| **UC-03** | View Impact Dashboard | Any User | Any authenticated user views live impact metrics and top donors/recipients. |
| **UC-04** | Claim a Donation | Recipient | Recipient selects and claims an available donation. |
| **UC-05** | Cancel a Claim | Recipient | Recipient releases a previously claimed donation. |
| **UC-06** | Search and Filter Food Donations | Recipient | Recipient narrows the available donation list by type, status, urgency, or search text. |
| **UC-07** | Volunteer Pickup and Delivery | Volunteer | Volunteer confirms pickup at the donor and delivery to the recipient. |
| **UC-08** | Switch Matching Strategy | Admin | Admin changes the active matching algorithm at runtime. |
| **UC-09** | Undo Incorrect Action | Admin | Admin reverses the last command from the history stack. |
| **UC-10** | Generate Audit Report | Admin | Admin views a chronological report of all system actions. |
| **UC-11** | Auto-Match to Recipient | System / Time | System automatically ranks and notifies the best-matched recipient. |
| **UC-12** | Handle Expired Donation | System / Time | System detects and transitions expired donations. |

The IDs above are the canonical IDs used throughout this guide. They must match the labels in the use case diagram (section **f**) and the sequence diagrams (section **g**).

---

## d. Diagram Layout & Relationships

Below is a recommended layout for arranging elements:

```
┌─────────────────────────────────────────────────────────────────────────┐
│  FoodBridge — Smart Food Surplus Redistribution Platform                │
│                                                                         │
│  ┌──────────────────┐  ┌──────────────────┐  ┌──────────────────┐       │
│  │ UC-01 Post Food  │  │ UC-02 Delete a   │  │ UC-03 View Impact│       │
│  │ Donation         │  │ Donation         │  │ Dashboard        │       │
│  └──────────────────┘  └──────────────────┘  └──────────────────┘       │
│  ┌──────────────────┐  ┌──────────────────┐  ┌──────────────────┐       │
│  │ UC-04 Claim a    │  │ UC-05 Cancel a   │  │ UC-06 Search and │       │
│  │ Donation         │  │ Claim            │  │ Filter Donations │       │
│  └──────────────────┘  └──────────────────┘  └──────────────────┘       │
│  ┌──────────────────┐  ┌──────────────────┐  ┌──────────────────┐       │
│  │ UC-07 Volunteer  │  │ UC-08 Switch     │  │ UC-09 Undo       │       │
│  │ Pickup & Delivery│  │ Matching Strategy│  │ Incorrect Action │       │
│  └──────────────────┘  └──────────────────┘  └──────────────────┘       │
│  ┌──────────────────┐  ┌──────────────────┐  ┌──────────────────┐       │
│  │ UC-10 Generate   │  │ UC-11 Auto-Match │  │ UC-12 Handle     │       │
│  │ Audit Report     │  │ to Recipient     │  │ Expired Donation │       │
│  └──────────────────┘  └──────────────────┘  └──────────────────┘       │
└─────────────────────────────────────────────────────────────────────────┘
      Donor         Recipient        Volunteer        Admin        System
```

### Association Relationships

Connect each actor to their use cases with solid lines (no arrowheads — just straight lines):

- **Donor** → UC-01 Post Food Donation, UC-02 Delete a Donation, UC-03 View Impact Dashboard
- **Recipient** → UC-03 View Impact Dashboard, UC-04 Claim a Donation, UC-05 Cancel a Claim, UC-06 Search and Filter Food Donations
- **Volunteer** → UC-03 View Impact Dashboard, UC-07 Volunteer Pickup and Delivery
- **Admin** → UC-03 View Impact Dashboard, UC-08 Switch Matching Strategy, UC-09 Undo Incorrect Action, UC-10 Generate Audit Report
- **System (Clock/Time)** → UC-11 Auto-Match to Recipient, UC-12 Handle Expired Donation
- **Any User** is represented by the four human actors connected to UC-03 View Impact Dashboard.

### Include Relationships (optional)

If using «include», the following relationships apply:

- **UC-01 Post Food Donation** «include» **UC-11 Auto-Match to Recipient** — After a donation is posted and verified, auto-matching runs automatically.
- **UC-01 Post Food Donation** «include» **UC-12 Handle Expired Donation** — Every posted donation is monitored for expiry as a built-in consequence.

If using «extend», the following applies:

- **UC-01 Post Food Donation** «extend» (ExpiryCheckHandler failure triggers a rejection path — modeled internally, not as a separate UC).

---

## e. Summary for Drawing

| Element | Count | Notes |
|---|---|---|
| System Boundary | 1 | Labeled "FoodBridge — Smart Food Surplus Redistribution Platform" |
| Primary Actors | 4 | Donor, Recipient, Volunteer, Admin |
| Secondary Actors | 1 | System / Time / Clock |
| Use Cases | 12 | As listed above |
| Associations | ~15 | Lines connecting actors to their use cases |
| Include | 2 | Post Food Donation → Auto-Match; Post Food Donation → Handle Expired |
| Extend | 0–1 | Optional |

---

## f. Tool Recommendations

Draw this diagram using any of the following tools:
- **draw.io** (recommended — free, collaborative, UML stencils built-in)
- **StarUML** (UML-native, supports all relationship types)
- **PlantUML** (text-based — code snippet below)
- **Visual Paradigm** (full-featured UML tool)

### PlantUML Code (copy-paste ready)

```plantuml
@startuml FoodBridge Use Case Diagram
left to right direction

' Aliases match the 12-use-case model used throughout this guide.

rectangle "FoodBridge\nSmart Food Surplus\nRedistribution Platform" {
  usecase "Post Food\nDonation" as UC01
  usecase "Delete a\nDonation" as UC02
  usecase "View Impact\nDashboard" as UC03
  usecase "Claim a\nDonation" as UC04
  usecase "Cancel a\nClaim" as UC05
  usecase "Search and Filter\nFood Donations" as UC06
  usecase "Volunteer Pickup\n& Delivery" as UC07
  usecase "Switch Matching\nStrategy" as UC08
  usecase "Undo Incorrect\nAction" as UC09
  usecase "Generate Audit\nReport" as UC10
  usecase "Auto-Match\nto Recipient" as UC11
  usecase "Handle Expired\nDonation" as UC12
}

actor Donor as D
actor Recipient as R
actor Volunteer as V
actor Admin as A
actor "System / Clock" as S

D --> UC01
D --> UC02
D --> UC03
R --> UC03
R --> UC04
R --> UC05
R --> UC06
V --> UC03
V --> UC07
A --> UC03
A --> UC08
A --> UC09
A --> UC10
S --> UC11
S --> UC12

UC01 ..> UC11 : <<include>>
UC01 ..> UC12 : <<include>>
@enduml
```

---

## g. Sequence Diagram Prompts (Detailed Design)

### What these prompts produce

These prompts produce **design-level sequence diagrams**, not black-box system sequence diagrams. Each diagram shows the actual collaborators from the project:

- **Presentation:** the JavaFX controller that receives the actor's action.
- **Business:** `DonationCoordinator`, commands, factories, strategies, state objects, and observers.
- **Domain:** `Donation`, `Donor`, `Recipient`, `Volunteer`.
- **Data:** `DonationRepository` and `UserRepository`.

Every diagram follows the corresponding use case and keeps the important extensions as `alt` branches or notes. They are intentionally drawn at a medium level of detail: enough to show the design patterns, but short enough to modify.

> **Implementation note:** the current UI controllers sometimes create commands directly (for example, `RecipientDashboardController.handleClaim()` and `VolunteerDashboardController.handlePickup()`). The diagrams model the intended business-layer flow through `DonationCoordinator`. If your code keeps the direct-UI wiring, remove `COORD` from that diagram and connect `UI` to `INV` directly; the rest of the message order stays the same.

### Notation used

| Element | PlantUML | Meaning |
|---|---|---|
| Actor | `actor Donor` | Human actor outside the system |
| Boundary | `boundary "PostDonationForm\nController" as UI` | JavaFX controller / view model |
| Control | `control "Donation\nCoordinator" as COORD` | Business service or design-pattern participant |
| Entity | `entity "Donation" as DONATION` | Domain object |
| Database | `database "Donation\nRepository" as DREPO` | Repository / data access |
| Synchronous call | `UI -> COORD : postDonation(params)` | Caller waits for the result |
| Return | `COORD --> UI : donationId` | Result or confirmation |
| Create | `COORD -> CMD ** : new ClaimDonationCommand(...)` | Creates a command or object |
| Self-call | `INV -> INV : history.addLast(cmd)` | Internal processing |
| Branch | `alt ... else ... end` | Success and failure paths |
| Optional | `opt ... end` | A step that may not happen |
| Loop | `loop ... end` | Iteration or polling |
| Note | `note right of ... ... end note` | Design hint; safe to delete |

### Prompt index

| UC | Use case | Key participants | Patterns exercised |
|---|---|---|---|
| UC-01 | Post Food Donation | UI, Coordinator, Factory, Verification Chain, repositories, observers | Factory Method, Chain of Responsibility, State, Observer |
| UC-02 | Delete a Donation | UI, Coordinator, repository, Donation, observers | Repository, Observer |
| UC-03 | View Impact Dashboard | UI, DashboardViewModel, repositories | Observer-fed ViewModel (read-only) |
| UC-04 | Claim a Donation | UI, Coordinator, Invoker, ClaimCommand, repository, Donation, State, observers | Command, State, Observer |
| UC-05 | Cancel a Claim | UI, Coordinator, Invoker, CancelCommand, repository, Donation, State, observers | Command, State, Observer |
| UC-06 | Search and Filter Food Donations | UI, DonationListViewModel, repository, Donation | ViewModel, filtering |
| UC-07 | Volunteer Pickup and Delivery | UI, Coordinator, Invoker, commands, repositories, Donation, State, Volunteer, Recipient, observers | Command, State, Observer |
| UC-08 | Switch Matching Strategy | UI, MatchingEngine, MatchingStrategy | Strategy |
| UC-09 | Undo Incorrect Action | UI, Coordinator, Invoker, Command, repository, Donation, State, AuditTrail, observers | Command, State |
| UC-10 | Generate Audit Report | UI, Coordinator, AuditTrail, AuditLogViewModel | Read-only query / audit trail |
| UC-11 | Auto-Match to Recipient | Coordinator, MatchingEngine, Strategy, UserRepository, Recipient, observers | Strategy, Observer |
| UC-12 | Handle Expired Donation | ExpiryWatcher, repository, Donation, State, Coordinator, observers | Observer, State |

### Base template

```plantuml
@startuml NewUseCase_Sequence
title New Use Case (Sequence Diagram)

actor Actor
boundary "SomeController" as UI
control "DonationCoordinator" as COORD
database "DonationRepository" as DREPO
entity "Donation" as DONATION

autonumber
Actor -> UI : userAction(...)
UI -> COORD : businessOperation(...)
COORD -> DREPO : findById(id)
DREPO --> COORD : donation
COORD -> DONATION : changeState(...)
COORD --> UI : result
UI --> Actor : confirmation
@enduml
```

Copy this block and add only the collaborators needed for the use case. Delete arrows that do not apply.

---

### UC-01 - Post Food Donation

**Prompt:** Draw a detailed sequence diagram for a Donor posting surplus food. Show the controller, `DonationCoordinator`, `DonationFactory`, the verification chain, `Donation`, the repositories, and the observer notification. Include an `alt` branch for failed verification.

```plantuml
@startuml UC-01_PostFoodDonation_Sequence
title UC-01 - Post Food Donation (Sequence Diagram)

actor Donor
boundary "PostDonationForm\nController" as UI
control "Donation\nCoordinator" as COORD
control "DonationFactory" as FACTORY
entity "Donation" as DONATION
control "Verification\nChain" as CHAIN
database "Donation\nRepository" as DREPO
database "User\nRepository" as UREPO
entity "Donor\n(domain)" as DONOR_MODEL
participant ":SystemObserver" as OBS

autonumber
Donor -> UI : submitDonation(formData)
UI -> UI : validateForm()
UI -> COORD : postDonation(params)

COORD -> FACTORY : create(params)
note right of FACTORY
  Creates PerishableDonation,
  DryFoodDonation, or
  PreparedMealDonation by type.
end note
FACTORY --> COORD : donation

COORD -> DONATION : setLocation(location)
COORD -> CHAIN : handle(donation)
note right of CHAIN
  ExpiryCheck -> QuantityCheck
  -> LocationCheck -> Approval
end note
CHAIN --> COORD : VerificationResult

alt verification passed
  COORD -> DONATION : setState(new AvailableState())
  COORD -> DREPO : save(donation)
  COORD -> UREPO : findById(donorId)
  UREPO --> COORD : donor
  COORD -> DONOR_MODEL : addDonation(donationId)
  COORD -> UREPO : update(donor)

  COORD -> OBS : onDonationPosted(event)
  note right of OBS
    RecipientNotifier, AuditLogger,
    ImpactDashboardObserver, ExpiryWatcher
  end note

  COORD --> UI : donationId, AVAILABLE
  UI --> Donor : success message
else verification failed
  COORD -> DONATION : setState(new RejectedState())
  COORD -> DREPO : save(donation)
  COORD -> OBS : onDonationRejected(event)
  COORD --> UI : IllegalStateTransitionException(reason)
  UI --> Donor : error message
end

note over COORD : Auto-match continues in UC-11.
@enduml
```

**Domain notes:** `DonationFactory` hides the three `Donation` subclasses; the `VerificationHandler` chain runs Expiry -> Quantity -> Location -> Approval; `Donation.setState(...)` delegates to the State pattern.

---

### UC-02 - Delete a Donation

**Prompt:** Draw a detailed sequence diagram for a Donor deleting one of their own donations. Show the controller, `DonationCoordinator`, `DonationRepository`, `Donation`, the ownership/status guard, and the observer notification. Include an `alt` branch for a donation that cannot be deleted.

```plantuml
@startuml UC-02_DeleteDonation_Sequence
title UC-02 - Delete a Donation (Sequence Diagram)

actor Donor
boundary "DonorDashboard\nController" as UI
control "Donation\nCoordinator" as COORD
database "Donation\nRepository" as DREPO
entity "Donation" as DONATION
participant ":SystemObserver" as OBS

autonumber
Donor -> UI : clickDelete(donationId)
UI -> UI : confirmDeletion()
UI -> COORD : deleteDonation(donationId, donorId)

COORD -> DREPO : findById(donationId)
DREPO --> COORD : donation
COORD -> DONATION : getDonorId()
COORD -> DONATION : getStatus()

alt donor owns donation and status is deletable
  note right of COORD
    Deletable: PENDING_REVIEW,
    AVAILABLE, or REJECTED.
    CLAIMED, IN_TRANSIT, and
    DELIVERED must be preserved.
  end note
  COORD -> DREPO : delete(donationId)
  COORD -> OBS : onDonationDeleted(event)
  note right of OBS
    AuditLogger records DELETE_DONATION.
    RecipientNotifier removes the listing.
    ImpactDashboard refreshes active counts.
  end note
  COORD --> UI : deleted
  UI --> Donor : "Donation deleted"
else not owner or status not deletable
  COORD --> UI : error("Cannot delete this donation")
  UI --> Donor : error message
end

note over COORD
  Deletion is a hard delete in InMemoryDonationRepo.
  A soft-delete flag can replace delete() without
  changing the message flow.
  SystemEvent.Type has no DONATION_DELETED yet;
  add it if observers must receive the event.
end note
@enduml
```

**Domain notes:** ownership is checked with `Donation.getDonorId()` and the state guard uses `Donation.getStatus()`. `DonationRepository.delete(UUID)` is the existing removal operation; no `DeleteDonationCommand` exists yet, so the coordinator calls the repository directly.

---

### UC-03 - View Impact Dashboard

**Prompt:** Draw a read-only sequence diagram for viewing the impact dashboard. Show the controller, `DashboardViewModel`, both repositories, and the read-only metric/top-list queries. Add an `alt` for no completed transactions.

```plantuml
@startuml UC-03_ViewImpactDashboard_Sequence
title UC-03 - View Impact Dashboard (Sequence Diagram)

actor "Any User" as User
boundary "ImpactDashboard\nController" as UI
control "Dashboard\nViewModel" as VM
database "Donation\nRepository" as DREPO
database "User\nRepository" as UREPO

autonumber
User -> UI : openDashboard()
UI -> VM : getInstance()
VM --> UI : viewModel
UI -> VM : kgSavedProperty(), mealsServedProperty(),\nco2AvoidedProperty(), activeDonationsProperty()
UI -> UI : bind labels to properties

UI -> DREPO : findByStatus(DELIVERED)
DREPO --> UI : deliveredDonations

alt deliveredDonations not empty
  UI -> UREPO : findById(donorId / recipientId)
  UREPO --> UI : donor / recipient names
  UI --> User : metrics + top donors + top recipients
else no delivered donations
  UI --> User : "No data yet - start by posting a donation!"
end

note right of VM : Updated live by ImpactDashboardObserver\nwhen donations are delivered, cancelled, or expired.
@enduml
```

**Domain notes:** the dashboard is read-only. `DashboardViewModel` is updated by `ImpactDashboardObserver`; the controller only binds labels and builds the top-donor/top-recipient lists from delivered donations.

---

### UC-04 - Claim a Donation

**Prompt:** Draw a detailed sequence diagram for a Recipient claiming a donation. Show the controller, coordinator, `CommandInvoker`, `ClaimDonationCommand`, repository, `Donation`, `DonationState`, and observer notification. Add a note for the invalid-state extension.

```plantuml
@startuml UC-04_ClaimDonation_Sequence
title UC-04 - Claim a Donation (Sequence Diagram)

actor Recipient
boundary "RecipientDashboard\nController" as UI
control "Donation\nCoordinator" as COORD
control "CommandInvoker" as INV
control "ClaimDonation\nCommand" as CMD
database "Donation\nRepository" as DREPO
entity "Donation" as DONATION
entity "DonationState" as STATE
participant ":SystemObserver" as OBS

autonumber
Recipient -> UI : clickClaim(donationId)
UI -> COORD : claimDonation(donationId, recipientId)

COORD -> DREPO : findById(donationId)
DREPO --> COORD : donation
COORD -> CMD ** : new ClaimDonationCommand(repo, donationId, recipientId)
COORD -> INV : execute(cmd)

INV -> CMD : execute()
CMD -> DREPO : findById(donationId)
DREPO --> CMD : donation

CMD -> DONATION : claim(recipientId)
note right of DONATION
  AvailableState.claim() throws
  IllegalStateTransitionException
  if the donation is not AVAILABLE.
end note
DONATION -> STATE : claim(donation, recipientId)
STATE -> DONATION : setRecipientId(recipientId)
STATE -> DONATION : setState(new ClaimedState())
DONATION --> CMD : status = CLAIMED

CMD -> DREPO : update(donation)
CMD --> INV : success
INV -> INV : history.addLast(cmd)

COORD -> OBS : onDonationClaimed(event)
note right of OBS
  RecipientNotifier confirms the claim.
  AuditLogger records CLAIM_DONATION.
  VolunteerDispatcher can assign a pickup.
end note

COORD --> UI : claim confirmed
UI --> Recipient : success message
@enduml
```

**Domain notes:** `ClaimDonationCommand` captures the recipient and donation IDs; `CommandInvoker` stores it for undo; `AvailableState.claim()` sets the recipient and swaps to `ClaimedState`.

---

### UC-05 - Cancel a Claim

**Prompt:** Draw a detailed sequence diagram for a Recipient cancelling a claim. Show the command flow, the `DonationState` transition from `CLAIMED` to `AVAILABLE`, and the observer notification. Include an `alt` for a donation already in transit or delivered.

```plantuml
@startuml UC-05_CancelClaim_Sequence
title UC-05 - Cancel a Claim (Sequence Diagram)

actor Recipient
boundary "RecipientDashboard\nController" as UI
control "Donation\nCoordinator" as COORD
control "CommandInvoker" as INV
control "CancelClaim\nCommand" as CMD
database "Donation\nRepository" as DREPO
entity "Donation" as DONATION
entity "DonationState" as STATE
participant ":SystemObserver" as OBS

autonumber
Recipient -> UI : clickCancel(donationId)
UI -> COORD : cancelClaim(donationId, recipientId)

COORD -> DREPO : findById(donationId)
DREPO --> COORD : donation
COORD -> CMD ** : new CancelClaimCommand(repo, donationId, recipientId)
COORD -> INV : execute(cmd)

INV -> CMD : execute()
CMD -> DREPO : findById(donationId)
DREPO --> CMD : donation
CMD -> DONATION : cancelClaim()
DONATION -> STATE : cancel(donation)

alt status = CLAIMED
  STATE -> DONATION : setRecipientId(null)
  STATE -> DONATION : setState(new AvailableState())
  DONATION --> CMD : status = AVAILABLE

  CMD -> DREPO : update(donation)
  CMD --> INV : success
  INV -> INV : history.addLast(cmd)

  COORD -> OBS : onDonationCancelled(event)
  note right of OBS
    RecipientNotifier re-notifies other recipients.
    AuditLogger records CANCEL_CLAIM.
  end note

  COORD --> UI : cancellation confirmed
  UI --> Recipient : success message
else status = IN_TRANSIT or DELIVERED
  STATE --> DONATION : IllegalStateTransitionException
  DONATION --> CMD : exception
  CMD --> INV : exception
  INV --> COORD : exception
  COORD --> UI : error("Cannot cancel: already in progress or delivered")
  UI --> Recipient : error message
end
@enduml
```

**Domain notes:** `ClaimedState.cancel()` clears `recipientId` and installs `AvailableState`. `CancelClaimCommand.undo()` can re-claim the donation if the previous status was `CLAIMED`.

---

### UC-06 - Search and Filter Food Donations

**Prompt:** Draw a sequence diagram for a Recipient searching and filtering available donations. Show the controller, `DonationListViewModel` filters, `DonationRepository`, and `Donation`. Include a loop over the candidates and an `alt` for no matches.

```plantuml
@startuml UC-06_SearchFilterDonations_Sequence
title UC-06 - Search and Filter Food Donations (Sequence Diagram)

actor Recipient
boundary "RecipientDashboard\nController" as UI
control "DonationList\nViewModel" as VM
database "Donation\nRepository" as DREPO
entity "Donation" as DONATION

autonumber
Recipient -> UI : enterKeyword(keyword)
Recipient -> UI : selectType(type)
Recipient -> UI : toggleUrgentOnly(flag)
UI -> UI : handleFilterChange()

UI -> VM : setFilterType(type)
UI -> VM : setFilterStatus("AVAILABLE")
UI -> VM : setShowUrgentOnly(flag)

UI -> DREPO : findByStatus(AVAILABLE)
DREPO --> UI : availableDonations

loop for each donation in availableDonations
  UI -> DONATION : getType()
  UI -> DONATION : isUrgent()
  UI -> DONATION : getLocation()
  UI -> UI : matchesKeywordAndFilters(donation)
end

UI -> VM : setDonations(matchingDonations)

alt at least one match
  VM --> UI : matching DonationRow list
  UI -> UI : donationsTable.setItems(rows)
  UI --> Recipient : filtered donation list
else no matches
  VM --> UI : empty list
  UI --> Recipient : "No donations match your filters"
end

note right of VM
  Exposed filters: type, status,
  and urgent-only flag.
  Keyword search is matched against
  type and location in the target design.
end note
note over UI
  Current applyFilters() reloads the full list;
  the target design filters the fetched list as shown.
end note
@enduml
```

**Domain notes:** `DonationListViewModel` already exposes `filterType`, `filterStatus`, and `showUrgentOnly` properties. The controller owns the query and filtering loop; `DonationRow` supplies the table values. Add a text field bound to the keyword if full-text search is required.

---

### UC-07 - Volunteer Pickup and Delivery

**Prompt:** Draw one detailed sequence diagram with a pickup phase and a delivery phase. Show the volunteer controller, coordinator, commands, repositories, `Donation`, state transitions, `Volunteer`, `Recipient`, and observer notifications. Mark pickup as `CLAIMED` -> `IN_TRANSIT` and delivery as `IN_TRANSIT` -> `DELIVERED`.

```plantuml
@startuml UC-07_VolunteerPickupDelivery_Sequence
title UC-07 - Volunteer Pickup and Delivery (Sequence Diagram)

actor Volunteer
boundary "VolunteerDashboard\nController" as UI
control "Donation\nCoordinator" as COORD
control "CommandInvoker" as INV
control "DonationCommand" as CMD
database "Donation\nRepository" as DREPO
database "User\nRepository" as UREPO
entity "Donation" as DONATION
entity "DonationState" as STATE
entity "Volunteer\n(domain)" as VOL
entity "Recipient\n(domain)" as REC
participant ":SystemObserver" as OBS

autonumber
note over CMD : MarkPickedUpCommand (pickup)\nMarkDeliveredCommand (delivery)

== Pickup ==
Volunteer -> UI : clickConfirmPickup(donationId)
UI -> COORD : pickupDonation(donationId, volunteerId)
COORD -> DREPO : findById(donationId)
DREPO --> COORD : donation
COORD -> CMD ** : new MarkPickedUpCommand(repo, donationId, volunteerId)
COORD -> INV : execute(cmd)

INV -> CMD : execute()
CMD -> DREPO : findById(donationId)
DREPO --> CMD : donation
CMD -> DONATION : pickUp(volunteerId)
DONATION -> STATE : pickUp(donation, volunteerId)
STATE -> DONATION : setVolunteerId(volunteerId)
STATE -> DONATION : setState(new InTransitState())
DONATION --> CMD : status = IN_TRANSIT

CMD -> DREPO : update(donation)
CMD --> INV : success
INV -> INV : history.addLast(cmd)

COORD -> UREPO : findById(volunteerId)
UREPO --> COORD : volunteer
COORD -> VOL : assignPickup(donationId)
COORD -> UREPO : update(volunteer)
COORD -> OBS : onDonationPickedUp(event)
COORD --> UI : pickup confirmed
UI --> Volunteer : status IN_TRANSIT

== Delivery ==
Volunteer -> UI : clickConfirmDelivery(donationId)
UI -> COORD : deliverDonation(donationId, volunteerId)
COORD -> DREPO : findById(donationId)
DREPO --> COORD : donation
COORD -> CMD ** : new MarkDeliveredCommand(repo, donationId, volunteerId)
COORD -> INV : execute(cmd)

INV -> CMD : execute()
CMD -> DREPO : findById(donationId)
DREPO --> CMD : donation
CMD -> DONATION : deliver()
DONATION -> STATE : deliver(donation)
STATE -> DONATION : setState(new DeliveredState())
DONATION --> CMD : status = DELIVERED

CMD -> DREPO : update(donation)
CMD --> INV : success
INV -> INV : history.addLast(cmd)

COORD -> UREPO : findById(volunteerId)
UREPO --> COORD : volunteer
COORD -> VOL : completePickup(donationId)
COORD -> UREPO : update(volunteer)

COORD -> UREPO : findById(recipientId)
UREPO --> COORD : recipient
COORD -> REC : completeDonation(donationId, quantity)
COORD -> UREPO : update(recipient)

COORD -> OBS : onDonationDelivered(event)
note right of OBS : ImpactDashboardObserver updates\nkgSaved, mealsServed, co2Avoided.
COORD --> UI : delivery confirmed
UI --> Volunteer : success message
@enduml
```

**Domain notes:** `MarkPickedUpCommand` and `MarkDeliveredCommand` are both `DonationCommand`s. The state classes enforce the legal transitions; `Volunteer.assignPickup()` and `Recipient.completeDonation()` keep the domain counters in sync.

---

### UC-08 - Switch Matching Strategy

**Prompt:** Draw a sequence diagram for switching the matching strategy. Show the controller, `MatchingEngine`, and `MatchingStrategy`. Include the three concrete strategy names, the runtime `setStrategy` call, and an `alt` for an already-active or invalid choice.

```plantuml
@startuml UC-08_SwitchMatchingStrategy_Sequence
title UC-08 - Switch Matching Strategy (Sequence Diagram)

actor Admin
boundary "StrategySwitcher\nController" as UI
control "MatchingEngine" as ENGINE
control "MatchingStrategy" as STRAT

autonumber
Admin -> UI : selectStrategy(name)
UI -> ENGINE : getAvailableStrategies()
ENGINE --> UI : ["Nearest First", "Expiry First", "Capacity Based"]
UI -> ENGINE : getCurrentStrategy()
ENGINE --> UI : currentStrategy

alt selection is valid and not already active
  UI -> STRAT ** : new SelectedStrategy()
  note right of STRAT
    NearestFirstStrategy,
    ExpiryFirstStrategy, or
    CapacityBasedStrategy.
  end note
  UI -> ENGINE : setStrategy(strategy)
  ENGINE -> ENGINE : currentStrategy = strategy
  UI --> Admin : "Matching strategy updated"
else already active or invalid
  UI --> Admin : "Already active" / error message
end

note right of ENGINE : Applies to the next match; no restart required.
@enduml
```

**Domain notes:** all strategies implement `MatchingStrategy.findBestMatch(...)` and `getName()`. `MatchingEngine` holds the current strategy, so switching is a single reference replacement.

---

### UC-09 - Undo Incorrect Action

**Prompt:** Draw a sequence diagram for undoing the last action. Show the audit controller, coordinator, `CommandInvoker`, the last `DonationCommand`, repository, `Donation`, `DonationState`, `AuditTrail`, and observers. Include an `alt` for an empty history and a note for terminal commands.

```plantuml
@startuml UC-09_UndoIncorrectAction_Sequence
title UC-09 - Undo Incorrect Action (Sequence Diagram)

actor Admin
boundary "AuditReport\nController" as UI
control "Donation\nCoordinator" as COORD
control "CommandInvoker" as INV
control "DonationCommand" as CMD
database "Donation\nRepository" as DREPO
entity "Donation" as DONATION
entity "DonationState" as STATE
entity "AuditTrail" as AUDIT
participant ":SystemObserver" as OBS

autonumber
Admin -> UI : clickUndo()
UI -> COORD : undoLastAction()

COORD -> INV : canUndo()
INV --> COORD : boolean

alt canUndo = true
  COORD -> INV : peekLast()
  INV --> COORD : lastCommand
  COORD -> INV : undo()

  INV -> INV : lastCommand = history.pollLast()
  INV -> CMD : undo()
  CMD -> DREPO : findById(donationId)
  DREPO --> CMD : donation
  CMD -> DONATION : undo transition
  note right of DONATION
    The concrete command reverses its own
    transition: claim, cancel, pickup, or delivery.
  end note
  DONATION -> STATE : restore previous state
  STATE -> DONATION : setState(previousState)
  DONATION --> CMD : reverted
  CMD -> DREPO : update(donation)
  CMD --> INV : success

  COORD -> AUDIT : log(actor, "UNDO", donationId)
  COORD -> OBS : notifyUndo(event)
  note right of OBS
    AuditTrail records the undo operation.
    Observers refresh their views.
  end note
  COORD --> UI : undo confirmed
  UI --> Admin : "Last action undone"
else canUndo = false
  COORD --> UI : "No actions to undo"
  UI --> Admin : error message
end

note right of CMD : Terminal commands such as MarkDeliveredCommand\nare treated as irreversible by UC-09.
note right of AUDIT : Undo logging is part of the target design;\ncurrent undoLastAction() does not yet write to AuditTrail.
@enduml
```

**Domain notes:** each concrete command implements `undo()` to reverse its own state transition. `CommandInvoker` uses a `Deque<DonationCommand>` history and `peekLast()` / `pollLast()` to expose and remove the last command.

---

### UC-10 - Generate Audit Report

**Prompt:** Draw a read-only sequence diagram for generating an audit report. Show the audit controller, coordinator, `AuditTrail`, and `AuditLogViewModel`. Include date/actor filtering and an `alt` for no records.

```plantuml
@startuml UC-10_GenerateAuditReport_Sequence
title UC-10 - Generate Audit Report (Sequence Diagram)

actor Admin
boundary "AuditReport\nController" as UI
control "Donation\nCoordinator" as COORD
entity "AuditTrail" as AUDIT
control "AuditLog\nViewModel" as VM

autonumber
Admin -> UI : generateReport(fromDate, toDate, filters)
UI -> COORD : getAuditTrail()
COORD --> UI : auditTrail

UI -> AUDIT : getEntriesSince(fromDate)
AUDIT --> UI : List<AuditEntry>
note right of AUDIT
  Optional filters:
  getEntriesByActor(actorId)
  getEntriesByDonation(donationId)
end note

UI -> VM : setEntries(entries)
VM -> VM : map AuditEntry -> AuditEntryRow

alt entries not empty
  UI --> Admin : chronological report table
  note right of Admin
    timestamp, actor, role, action,
    donationId, previous status,
    new status
  end note
else no entries
  UI --> Admin : "No records found for the selected period"
end

note right of AUDIT : Read-only; AuditLogger appends entries on every business event.
@enduml
```

**Domain notes:** `AuditTrail.AuditEntry` is an immutable record; `AuditLogViewModel` maps each entry to an `AuditEntryRow` for the JavaFX table. No domain state is changed.

---

### UC-11 - Auto-Match to Recipient

**Prompt:** Draw a detailed sequence diagram for automatic matching. Show `DonationCoordinator`, `MatchingEngine`, the active `MatchingStrategy`, `UserRepository`, `Recipient`, `Donation`, and the observer notification. Include an `alt` for no eligible recipient and a reference to the UC-04 claim flow.

```plantuml
@startuml UC-11_AutoMatch_Sequence
title UC-11 - Auto-Match to Recipient (Sequence Diagram)

actor "System / Clock" as Clock
control "Donation\nCoordinator" as COORD
database "Donation\nRepository" as DREPO
control "MatchingEngine" as MATCH
control "MatchingStrategy" as STRAT
database "User\nRepository" as UREPO
entity "Recipient" as RECIPIENT
entity "Donation" as DONATION
participant ":SystemObserver" as OBS

autonumber
Clock -> COORD : autoMatch(donationId)
COORD -> DREPO : findById(donationId)
DREPO --> COORD : donation
COORD -> MATCH : findBestMatch(donation)

MATCH -> UREPO : findByRole(RECIPIENT)
UREPO --> MATCH : candidates
note right of MATCH : currentStrategy is set by UC-08.

MATCH -> STRAT : findBestMatch(donation, candidates)
STRAT -> DONATION : getQuantity(), getLocation(), getExpiryTime()

loop for each candidate
  STRAT -> RECIPIENT : canAccept(quantity)
  STRAT -> RECIPIENT : isActive()
  note right of STRAT
    Ranking is strategy-specific:
    distance, expiry, or capacity.
  end note
end

STRAT --> MATCH : Optional<Recipient>
MATCH --> COORD : bestRecipient

alt eligible recipient found
  ref over COORD, OBS : Claim flow (UC-04)
  COORD -> OBS : onDonationClaimed(event)
  note right of OBS : RecipientNotifier alerts the matched recipient.
  COORD --> Clock : matched(recipientId)
else no eligible recipient
  note right of COORD : Donation stays AVAILABLE for manual claim.
  COORD --> Clock : noMatch
end
@enduml
```

**Domain notes:** `MatchingEngine` reads the active `MatchingStrategy`, loads recipients through `UserRepository.findByRole(RECIPIENT)`, and delegates ranking. `NearestFirstStrategy`, `ExpiryFirstStrategy`, and `CapacityBasedStrategy` are interchangeable.

---

### UC-12 - Handle Expired Donation

**Prompt:** Draw a time-triggered sequence diagram for expiry handling. Show `ExpiryWatcher`, the repository, `Donation`, the state transition to `EXPIRED`, and the observer notification. Use a `loop` for polling and an `alt` for expired versus still-fresh donations.

```plantuml
@startuml UC-12_HandleExpiredDonation_Sequence
title UC-12 - Handle Expired Donation (Sequence Diagram)

actor "System / Clock" as Clock
control "ExpiryWatcher" as EXP
database "Donation\nRepository" as DREPO
entity "Donation" as DONATION
entity "DonationState" as STATE
control "Donation\nCoordinator" as COORD
participant ":SystemObserver" as OBS

autonumber
Clock -> EXP : checkExpiries() every 30 s

EXP -> DREPO : findByStatus(AVAILABLE)
EXP -> DREPO : findByStatus(CLAIMED)
EXP -> DREPO : findByStatus(IN_TRANSIT)
DREPO --> EXP : activeDonations

loop for each active donation
  EXP -> DONATION : isExpired()

  alt donation is expired
    EXP -> DONATION : expire()
    DONATION -> STATE : expire(donation)
    STATE -> DONATION : setState(new ExpiredState())
    DONATION --> EXP : status = EXPIRED
    EXP -> DREPO : update(donation)

    EXP -> COORD : publish(DONATION_EXPIRED)
    COORD -> OBS : onDonationExpired(event)
    note right of OBS
      RecipientNotifier notifies the donor.
      AuditLogger records EXPIRE_DONATION.
      ImpactDashboard decrements active count.
    end note
  else donation is still fresh
    note right of EXP : Skip this donation.
  end
end

EXP --> Clock : check complete
note over EXP
  Current code updates the repository directly.
  The target design publishes the event through
  DonationCoordinator so all observers react.
end note
@enduml
```

**Domain notes:** `ExpiryWatcher` is a scheduled `SystemObserver`; `Donation.isExpired()` compares the domain expiry time with the clock; `ExpiredState` is terminal.

---

## h. Customizing & Rendering the Sequence Diagrams

### Mapping diagram elements to the code

| Diagram element | Source package / class |
|---|---|
| UI controllers | `com.waslataam.ui.controllers` (`PostDonationFormController`, `DonorDashboardController`, `RecipientDashboardController`, `VolunteerDashboardController`, `ImpactDashboardController`, `StrategySwitcherController`, `AuditReportController`) |
| View models | `com.waslataam.viewmodel` (`DonationListViewModel`, `DashboardViewModel`, `AuditLogViewModel`) |
| Coordinator | `com.waslataam.coordination.DonationCoordinator` |
| Commands | `com.waslataam.command` (`CommandInvoker`, `ClaimDonationCommand`, `CancelClaimCommand`, `MarkPickedUpCommand`, `MarkDeliveredCommand`, `AuditTrail`) |
| State | `com.waslataam.state` (`AvailableState`, `ClaimedState`, `InTransitState`, `DeliveredState`, `ExpiredState`, `RejectedState`) |
| Strategy | `com.waslataam.strategy` (`MatchingEngine`, `NearestFirstStrategy`, `ExpiryFirstStrategy`, `CapacityBasedStrategy`) |
| Chain | `com.waslataam.chain` (`ExpiryCheckHandler`, `QuantityCheckHandler`, `LocationCheckHandler`, `ApprovalHandler`) |
| Observers | `com.waslataam.observer` (`RecipientNotifier`, `AuditLogger`, `ImpactDashboardObserver`, `ExpiryWatcher`) |
| Repositories | `com.waslataam.repository` (`DonationRepository`, `UserRepository`) |
| Domain model | `com.waslataam.domain` (`Donation`, `Donor`, `Recipient`, `Volunteer`, `DonationState`, `Location`) |

### Common changes

| Goal | Change |
|---|---|
| Rename a class | Update the `participant`/`control`/`entity` declaration; aliases keep the arrows stable |
| Add a parameter | Edit the text after `:` on the arrow; keep names short |
| Remove a layer | Delete the lifeline and reconnect the neighboring messages |
| Collapse the observers | Keep one `:SystemObserver` lifeline and list the concrete observers in a note |
| Expand the observers | Replace `OBS` with `RecipientNotifier`, `AuditLogger`, `ImpactDashboardObserver`, and `ExpiryWatcher` lifelines |
| Expand the state | Replace `DonationState` with the concrete state used at that point (`AvailableState`, `ClaimedState`, etc.) |
| Add an alternate flow | Wrap it in `alt condition ... else ... end` |
| Show an optional step | Use `opt condition ... end` |
| Reference another use case | Use `ref over COORD, OBS : Claim flow (UC-04)` |
| Simplify to a black-box view | Keep only the actor and a single `:System` lifeline; replace internal calls with actor-to-system messages |
| Reduce width | Delete unused repositories/domain lifelines and move secondary details into notes |

### Rendering

Paste each block into one of these:

- the PlantUML extension for VS Code or IntelliJ,
- an online renderer such as `plantuml.com/plantuml`,
- the PlantUML CLI: `java -jar plantuml.jar UC-01-post-food-donation.puml`.

Each block is independent: it starts with `@startuml` and ends with `@enduml`, so you can save each one as its own `.puml` file. Suggested file names follow the pattern `UC-01-post-food-donation.puml`, `UC-02-delete-donation.puml`, and so on through `UC-12-handle-expired-donation.puml`.
