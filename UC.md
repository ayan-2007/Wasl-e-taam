# Use Case Specification — FoodBridge System

---

## Use Case 01 — Post Food Donation

| Element | Description |
|---|---|
| **Use Case Name** | Post Food Donation |
| **Scope** | FoodBridge — Smart Food Surplus Redistribution Platform |
| **Level** | User Goal |
| **Primary Actor** | Donor |
| **Stakeholders & Interests** | **Donor:** Wants to quickly list surplus food to avoid waste. **Recipient:** Wants to discover available donations in real time. **Admin:** Wants all donations verified before going live. **Volunteer:** Wants to see new pickup tasks. |
| **Preconditions** | Donor is registered and logged in. |
| **Postconditions** | A new Donation object is created, verified, and set to AVAILABLE state. All observers are notified. |
| **Main Success Scenario** | 1. Donor selects "Post Donation" from the menu.<br>2. Donor enters food type, quantity, unit, expiry time, and location.<br>3. System calls DonationFactory.create() to produce the correct Donation subtype.<br>4. System passes the donation through the Verification Chain (ExpiryCheck → QuantityCheck → LocationCheck → Approval).<br>5. System sets donation state to AVAILABLE.<br>6. System calls notifyAll(DONATION_POSTED).<br>7. Observers fire: RecipientNotifier alerts eligible recipients, ImpactDashboard updates, AuditLogger records the event, ExpiryWatcher begins monitoring.<br>8. System confirms success to Donor. |
| **Extensions** | 3a. Invalid food type entered: System rejects with "Unknown donation type."<br>4a. Verification chain fails at any handler: System returns specific rejection reason (e.g., "Donation already expired") and donation is not created.<br>6a. Observer throws exception: System logs the error and continues; donation is still posted. |
| **Special Requirements** | Text-based console UI. All input validated before factory invocation. |
| **Technology & Data Variations** | Console I/O via Java Scanner. In-memory repository (Phase 1). Donation types: PERISHABLE, DRY_FOOD, PREPARED_MEAL. |

---

## Use Case 02 — Claim a Donation

| Element | Description |
|---|---|
| **Use Case Name** | Claim a Donation |
| **Scope** | FoodBridge — Smart Food Surplus Redistribution Platform |
| **Level** | User Goal |
| **Primary Actor** | Recipient |
| **Stakeholders & Interests** | **Recipient:** Wants to secure food for their beneficiaries. **Donor:** Wants donation claimed quickly before expiry. **Volunteer:** Wants a pickup task assigned. **Admin:** Wants claims to be traceable and undoable. |
| **Preconditions** | Donation status is AVAILABLE. Recipient is registered and logged in. |
| **Postconditions** | Donation transitions to CLAIMED state. A ClaimDonationCommand is recorded in the invoker stack. VolunteerDispatcher triggers assignment. |
| **Main Success Scenario** | 1. Recipient browses available donations and selects one.<br>2. System creates a ClaimDonationCommand(donation, recipient).<br>3. CommandInvoker executes the command.<br>4. Command calls donation.claim(recipient).<br>5. DonationState transitions from AVAILABLE to CLAIMED.<br>6. CommandInvoker pushes the command onto the undo stack.<br>7. System calls notifyAll(DONATION_CLAIMED).<br>8. VolunteerDispatcher assigns a nearby volunteer.<br>9. System confirms the claim to Recipient. |
| **Extensions** | 1a. No donations available: System displays "No donations currently available."<br>4a. IllegalStateException thrown: Donation is not in AVAILABLE state — System displays "This donation cannot be claimed."<br>5a. Recipient has exceeded capacity: System rejects with "Recipient at full capacity." |
| **Special Requirements** | Claim must be undoable via Command pattern. Capacity limits must be enforced. |
| **Technology & Data Variations** | Console I/O. ClaimDonationCommand implements DonationCommand. State transition validated by DonationState implementation. |

---

## Use Case 03 — Auto-Match to Recipient

| Element | Description |
|---|---|
| **Use Case Name** | Auto-Match to Recipient |
| **Scope** | FoodBridge — Smart Food Surplus Redistribution Platform |
| **Level** | Subfunction |
| **Primary Actor** | System (Time-based trigger) |
| **Stakeholders & Interests** | **Recipient:** Wants to be matched fairly. **Donor:** Wants donation matched before expiry. **Admin:** Wants configurable matching logic. |
| **Preconditions** | A donation has been posted and is AVAILABLE. At least one recipient is registered. |
| **Postconditions** | The best-matched recipient is notified. If no match is found, timeout logic applies. |
| **Main Success Scenario** | 1. Donation is posted and set to AVAILABLE.<br>2. MatchingEngine retrieves the active MatchingStrategy.<br>3. MatchingEngine calls strategy.findBestMatch(donation, recipientList).<br>4. Strategy returns the highest-ranked recipient.<br>5. RecipientNotifier alerts the matched recipient.<br>6. If the recipient does not claim within a timeout window, the next-ranked recipient is notified. |
| **Extensions** | 3a. No recipients registered: System logs "No recipients available for matching."<br>4a. All recipients at capacity: System skips auto-match, donation remains AVAILABLE for manual claim.<br>5a. Timeout expires on all recipients: Donation remains AVAILABLE with "Manual claim only" flag. |
| **Special Requirements** | Matching strategy must be swappable at runtime without restart. |
| **Technology & Data Variations** | Strategy implementations: NearestFirstStrategy, ExpiryFirstStrategy, CapacityBasedStrategy. Active strategy set by Admin via MatchingEngine.setStrategy(). |

---

## Use Case 04 — Cancel a Claim

| Element | Description |
|---|---|
| **Use Case Name** | Cancel a Claim |
| **Scope** | FoodBridge — Smart Food Surplus Redistribution Platform |
| **Level** | User Goal |
| **Primary Actor** | Recipient |
| **Stakeholders & Interests** | **Recipient:** Wants to release a donation they cannot collect. **Donor:** Wants donation re-listed quickly. **Other Recipients:** Want a chance to claim the released donation. **Admin:** Wants cancellation traceable. |
| **Preconditions** | Donation status is CLAIMED by this Recipient. |
| **Postconditions** | Donation reverts to AVAILABLE state. A CancelClaimCommand is recorded. Other recipients are re-notified. |
| **Main Success Scenario** | 1. Recipient selects "Cancel Claim" on a previously claimed donation.<br>2. System creates a CancelClaimCommand(donation).<br>3. CommandInvoker executes the command.<br>4. Command calls donation.cancel().<br>5. DonationState transitions from CLAIMED to AVAILABLE.<br>6. System calls notifyAll(DONATION_AVAILABLE).<br>7. Other recipients are notified of the newly available donation. |
| **Extensions** | 4a. IllegalStateException: Donation is IN_TRANSIT or already DELIVERED — System displays "Cannot cancel — donation is already in progress or delivered." |
| **Special Requirements** | Cancel must be undoable (re-claim via new ClaimDonationCommand). |
| **Technology & Data Variations** | CancelClaimCommand implements DonationCommand with undo() that re-claims the donation. |

---

## Use Case 05 — Volunteer Pickup & Delivery

| Element | Description |
|---|---|
| **Use Case Name** | Volunteer Pickup & Delivery |
| **Scope** | FoodBridge — Smart Food Surplus Redistribution Platform |
| **Level** | User Goal |
| **Primary Actor** | Volunteer |
| **Stakeholders & Interests** | **Volunteer:** Wants clear pickup and delivery instructions. **Donor:** Wants confirmation of pickup. **Recipient:** Wants timely delivery. **Admin:** Wants delivery completion recorded. |
| **Preconditions** | Donation status is CLAIMED. Volunteer is assigned and logged in. |
| **Postconditions** | Donation transitions through IN_TRANSIT to DELIVERED. ImpactDashboard updates. |
| **Main Success Scenario** | 1. Volunteer views assigned tasks and selects a pickup.<br>2. Volunteer confirms pickup at donor location.<br>3. System transitions state from CLAIMED to IN_TRANSIT.<br>4. Volunteer travels to recipient location.<br>5. Volunteer confirms delivery.<br>6. System creates MarkDeliveredCommand and executes it.<br>7. DonationState transitions from IN_TRANSIT to DELIVERED.<br>8. System calls notifyAll(DONATION_DELIVERED).<br>9. ImpactDashboard updates: kg saved, meals served, CO₂ avoided.<br>10. System confirms delivery completion to Volunteer. |
| **Extensions** | 3a. Donation expired during transit: ExpiryWatcher fires, state transitions to EXPIRED — Volunteer is notified "Donation expired, return to donor."<br>5a. Delivery confirmation fails: Volunteer retries; system validates state is still IN_TRANSIT. |
| **Special Requirements** | Delivery cannot be undone (terminal state). All delivery events must be audited. |
| **Technology & Data Variations** | MarkDeliveredCommand implements DonationCommand (undo() throws UnsupportedOperationException). |

---

## Use Case 06 — View Impact Dashboard

| Element | Description |
|---|---|
| **Use Case Name** | View Impact Dashboard |
| **Scope** | FoodBridge — Smart Food Surplus Redistribution Platform |
| **Level** | User Goal |
| **Primary Actor** | Any User (Donor, Recipient, Volunteer, Admin) |
| **Stakeholders & Interests** | **All Users:** Want to see the social impact metrics. **Admin:** Wants to demonstrate platform effectiveness. |
| **Preconditions** | System has processed at least one completed transaction. |
| **Postconditions** | Dashboard displays current live metrics. No system state changes. |
| **Main Success Scenario** | 1. User selects "View Impact Dashboard" from the menu.<br>2. System queries the ImpactDashboard observer.<br>3. System displays: total kg of food saved, total meals served, total CO₂ avoided, top donors list, top recipients list, active donations count.<br>4. System returns to main menu. |
| **Extensions** | 1a. No transactions recorded: System displays "No data yet — start by posting a donation!" |
| **Special Requirements** | Dashboard must reflect real-time data. Read-only operation — no side effects. |
| **Technology & Data Variations** | ImpactDashboard implements SystemObserver. Data aggregated from all observed events. |

---

## Use Case 07 — Handle Expired Donation

| Element | Description |
|---|---|
| **Use Case Name** | Handle Expired Donation |
| **Scope** | FoodBridge — Smart Food Surplus Redistribution Platform |
| **Level** | Subfunction |
| **Primary Actor** | System (ExpiryWatcher — time-based trigger) |
| **Stakeholders & Interests** | **Donor:** Wants to be notified when their donation expires. **Recipient:** Wants expired items removed from available listings. **Admin:** Wants expiry events logged. |
| **Preconditions** | A donation exists in AVAILABLE state past its expiry time. |
| **Postconditions** | Donation transitions to EXPIRED state. Donor is notified. Expired donation removed from active pool. |
| **Main Success Scenario** | 1. ExpiryWatcher Observer periodically checks all AVAILABLE donations.<br>2. ExpiryWatcher detects a donation past its expiryTime.<br>3. ExpiryWatcher triggers state transition: AVAILABLE → EXPIRED.<br>4. System calls notifyAll(DONATION_EXPIRED).<br>5. Donor is notified that their donation has expired.<br>6. Expired donation is removed from the active listing pool.<br>7. AuditLogger records the expiry event. |
| **Extensions** | 2a. Donation already claimed before expiry: Watcher skips it — only AVAILABLE donations are eligible for expiry.<br>2b. Multiple donations expire simultaneously: Each is processed independently; watcher iterates the full list. |
| **Special Requirements** | Time-based polling mechanism required. Expired state is terminal — no transitions out of it. |
| **Technology & Data Variations** | ExpiryWatcher implements SystemObserver. Uses system clock (LocalDateTime.now()) for expiry comparison. Polling interval configured at startup. |

---

## Use Case 08 — Switch Matching Strategy

| Element | Description |
|---|---|
| **Use Case Name** | Switch Matching Strategy |
| **Scope** | FoodBridge — Smart Food Surplus Redistribution Platform |
| **Level** | User Goal |
| **Primary Actor** | Admin |
| **Stakeholders & Interests** | **Admin:** Wants to adapt matching behavior to context (normal hours vs. crisis). **Donor:** Affected indirectly by new matching priorities. **Recipient:** Affected indirectly by new matching priorities. |
| **Preconditions** | Admin is logged in. |
| **Postconditions** | Active MatchingStrategy in MatchingEngine is replaced. All subsequent matches use the new algorithm. |
| **Main Success Scenario** | 1. Admin selects "Configure Matching Strategy" from the admin menu.<br>2. System displays available strategies: Nearest-First, Expiry-First, Capacity-Based.<br>3. Admin selects a new strategy.<br>4. System calls MatchingEngine.setStrategy(new ExpiryFirstStrategy()).<br>5. System confirms: "Matching strategy updated to Expiry-First."<br>6. Next donation posted is matched using the new algorithm — no restart required. |
| **Extensions** | 3a. Admin selects the currently active strategy: System displays "Already active." and returns.<br>4a. Strategy instantiation fails: System logs error and keeps current strategy. |
| **Special Requirements** | Runtime strategy switching — zero downtime. No existing donations or operations affected. |
| **Technology & Data Variations** | Strategy implementations: NearestFirstStrategy (geographic proximity), ExpiryFirstStrategy (soonest expiry), CapacityBasedStrategy (highest remaining recipient capacity). All implement MatchingStrategy interface. |

---

## Use Case 09 — Undo Incorrect Action

| Element | Description |
|---|---|
| **Use Case Name** | Undo Incorrect Action |
| **Scope** | FoodBridge — Smart Food Surplus Redistribution Platform |
| **Level** | User Goal |
| **Primary Actor** | Admin |
| **Stakeholders & Interests** | **Admin:** Wants to reverse erroneous actions (wrong claim, accidental state change). **Donor:** Wants their donation restored if incorrectly claimed. **Recipient:** Wants erroneous cancellations reversed. |
| **Preconditions** | At least one Command exists in the CommandInvoker undo stack. |
| **Postconditions** | The most recent Command is undone. System state is reverted to before the undone command. AuditTrail records the undo operation. |
| **Main Success Scenario** | 1. Admin selects "Undo Last Action" from the admin menu.<br>2. System displays the last command details (type, actor, timestamp, donation ID).<br>3. Admin confirms undo.<br>4. CommandInvoker.undo() pops the last DonationCommand from the stack.<br>5. command.undo() reverses the state transition.<br>6. System calls notifyAll with appropriate event.<br>7. AuditTrail records: "UNDO: [original command details]".<br>8. System confirms: "Last action has been undone." |
| **Extensions** | 2a. Undo stack is empty: System displays "No actions to undo."<br>5a. Terminal state command (e.g., DELIVERED) cannot be undone: System displays "Cannot undo — this action is irreversible."<br>5b. Dependent commands exist: System warns "Undoing this may affect subsequent actions. Proceed?" |
| **Special Requirements** | Not all commands support undo (e.g., MarkDeliveredCommand is terminal). Undo stack depth may be configurable. |
| **Technology & Data Variations** | CommandInvoker maintains a Deque-based history stack. Each DonationCommand implements both execute() and undo(). |

---

## Use Case 10 — Generate Audit Report

| Element | Description |
|---|---|
| **Use Case Name** | Generate Audit Report |
| **Scope** | FoodBridge — Smart Food Surplus Redistribution Platform |
| **Level** | User Goal |
| **Primary Actor** | Admin |
| **Stakeholders & Interests** | **Admin:** Wants regulatory-grade accountability. **Donor & Recipient:** Wants dispute resolution capability. **Regulators (external):** Wants proof of合规 and fair operations. |
| **Preconditions** | System has recorded at least one transaction in the AuditTrail. |
| **Postconditions** | A chronological audit report is displayed. No system state changes. |
| **Main Success Scenario** | 1. Admin selects "Generate Audit Report" from the admin menu.<br>2. Admin specifies a date range (or "all").<br>3. System queries AuditTrail for all Commands in the specified range.<br>4. System displays a chronological report: timestamp, actor name, actor role, action type (CLAIM, CANCEL, DELIVER, etc.), donation ID, donation type, quantity, outcome.<br>5. System returns to admin menu. |
| **Extensions** | 2a. No commands in the specified range: System displays "No records found for the selected period."<br>4a. Report exceeds screen length: System paginates output (page up / page down). |
| **Special Requirements** | Report must be suitable for regulatory review. All timestamps must be precise and immutable. |
| **Technology & Data Variations** | AuditTrail stores List of AuditRecord objects. Each record contains: timestamp, actorId, actorName, actionType, donationId, details. Output to console (Phase 1); file/DB export in future phases. |