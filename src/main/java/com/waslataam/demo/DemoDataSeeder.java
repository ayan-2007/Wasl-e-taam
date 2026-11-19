package com.waslataam.demo;

import com.waslataam.auth.PasswordHasher;
import com.waslataam.command.AuditTrail;
import com.waslataam.command.ClaimDonationCommand;
import com.waslataam.command.CommandInvoker;
import com.waslataam.domain.Donation;
import com.waslataam.domain.DonationStatus;
import com.waslataam.domain.DonationType;
import com.waslataam.domain.Donor;
import com.waslataam.domain.Location;
import com.waslataam.domain.Recipient;
import com.waslataam.domain.User;
import com.waslataam.domain.UserRole;
import com.waslataam.domain.VehicleType;
import com.waslataam.domain.Volunteer;
import com.waslataam.factory.DonationFactory;
import com.waslataam.factory.DonationParams;
import com.waslataam.factory.UserFactory;
import com.waslataam.factory.UserParams;
import com.waslataam.repository.AccountRecord;
import com.waslataam.repository.AccountRepository;
import com.waslataam.repository.DonationRepository;
import com.waslataam.repository.UserRepository;
import com.waslataam.state.AvailableState;
import com.waslataam.state.ClaimedState;
import com.waslataam.state.DeliveredState;
import com.waslataam.state.ExpiredState;
import com.waslataam.state.InTransitState;
import com.waslataam.state.PendingReviewState;
import com.waslataam.state.RejectedState;
import com.waslataam.viewmodel.DashboardViewModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * Populates the application with a coherent demo dataset:
 *
 * <ul>
 *   <li>eight accounts (two per role, including one full and one limited admin),</li>
 *   <li>thirteen donations covering all seven lifecycle states,</li>
 *   <li>an audit trail describing how each donation reached its state, and</li>
 *   <li>impact metrics and one recorded claim command so admin undo is usable.</li>
 * </ul>
 *
 * <p>Accounts are only inserted when missing, so locally registered users and
 * changed passwords survive restarts. Donations live in the in-memory
 * repository and are recreated on each launch.</p>
 */
public class DemoDataSeeder {
    private static final Logger log = LoggerFactory.getLogger(DemoDataSeeder.class);

    private static final Location DONOR_A_LOCATION =
            new Location(24.8138, 67.0300, "Amina's Kitchen, Clifton Block 5, Karachi", "Clifton");
    private static final Location DONOR_B_LOCATION =
            new Location(24.8607, 67.0100, "Karachi Bakery House, Saddar, Karachi", "Saddar");
    private static final Location RECIPIENT_A_LOCATION =
            new Location(24.8826, 67.0650, "Sadaf Welfare Trust, Gulshan-e-Iqbal, Karachi", "Gulshan");
    private static final Location RECIPIENT_B_LOCATION =
            new Location(24.9056, 67.0822, "Al-Noor Shelter Home, North Nazimabad, Karachi", "North Nazimabad");

    private final UserRepository users;
    private final AccountRepository accounts;
    private final UserFactory userFactory;
    private final DonationRepository donations;
    private final DonationFactory donationFactory;
    private final AuditTrail auditTrail;
    private final DashboardViewModel dashboard;
    private final CommandInvoker commandInvoker;

    public DemoDataSeeder(UserRepository users,
                          AccountRepository accounts,
                          UserFactory userFactory,
                          DonationRepository donations,
                          DonationFactory donationFactory,
                          AuditTrail auditTrail,
                          DashboardViewModel dashboard,
                          CommandInvoker commandInvoker) {
        this.users = users;
        this.accounts = accounts;
        this.userFactory = userFactory;
        this.donations = donations;
        this.donationFactory = donationFactory;
        this.auditTrail = auditTrail;
        this.dashboard = dashboard;
        this.commandInvoker = commandInvoker;
    }

    public void seed() {
        SeedUsers seed = seedUsers();
        if (donations.findAll().isEmpty()) {
            seedDonations(seed);
        }
        recalculateDashboard();
    }

    // ------------------------------------------------------------------ users

    private SeedUsers seedUsers() {
        Donor donorA = (Donor) upsertDemoUser("donor@wasl.demo");
        Donor donorB = (Donor) upsertDemoUser("bakery@wasl.demo");
        Recipient recipientA = (Recipient) upsertDemoUser("recipient@wasl.demo");
        Recipient recipientB = (Recipient) upsertDemoUser("shelter@wasl.demo");
        Volunteer volunteerA = (Volunteer) upsertDemoUser("volunteer@wasl.demo");
        Volunteer volunteerB = (Volunteer) upsertDemoUser("courier@wasl.demo");
        upsertDemoUser("admin@wasl.demo");
        upsertDemoUser("ops@wasl.demo");
        return new SeedUsers(donorA, donorB, recipientA, recipientB, volunteerA, volunteerB);
    }

    private User upsertDemoUser(String email) {
        DemoAccounts demo = DemoAccounts.byEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Unknown demo account: " + email));

        Optional<User> existing = users.findByEmail(email);
        if (existing.isEmpty()) {
            User user = userFactory.create(userParamsFor(demo));
            users.save(user);
            existing = Optional.of(user);
            log.info("Seeded demo user {} ({})", email, demo.role());
        }

        User user = existing.orElseThrow();
        if (accounts.findByEmail(email).isEmpty()) {
            PasswordHasher.HashedPassword hashed = PasswordHasher.hash(DemoAccounts.PASSWORD);
            accounts.save(new AccountRecord(
                    user.getId(),
                    demo.email(),
                    hashed.hash(),
                    hashed.salt(),
                    demo.isAdmin(),
                    0,
                    null,
                    null,
                    LocalDateTime.now()
            ));
        }

        if (demo.isAdmin()) {
            // The allowlist is only ever populated here (and by future admin
            // tooling), never by public sign-up.
            accounts.allowlistAdmin(demo.email(), demo.blurb());
        }
        return user;
    }

    private UserParams userParamsFor(DemoAccounts demo) {
        UserParams.Builder builder = UserParams.builder()
                .role(demo.role())
                .name(demo.name())
                .email(demo.email())
                .location(locationFor(demo.email()));

        return switch (demo.role()) {
            case DONOR -> builder.organizationName(demo.organization()).build();
            case RECIPIENT -> builder
                    .capacityKg(demo.email().equals("recipient@wasl.demo") ? 400 : 250)
                    .reliabilityScore(demo.email().equals("recipient@wasl.demo") ? 0.94 : 0.88)
                    .build();
            case VOLUNTEER -> builder
                    .vehicleType(demo.email().equals("volunteer@wasl.demo")
                            ? VehicleType.VAN
                            : VehicleType.MOTORCYCLE)
                    .build();
            case ADMIN -> builder.permissions(demo.permissions()).build();
        };
    }

    private static Location locationFor(String email) {
        return switch (email) {
            case "donor@wasl.demo" -> DONOR_A_LOCATION;
            case "bakery@wasl.demo" -> DONOR_B_LOCATION;
            case "recipient@wasl.demo" -> RECIPIENT_A_LOCATION;
            case "shelter@wasl.demo" -> RECIPIENT_B_LOCATION;
            default -> new Location(24.8607, 67.0011, "Karachi, Pakistan", "Karachi");
        };
    }

    // -------------------------------------------------------------- donations

    private void seedDonations(SeedUsers seed) {
        LocalDateTime now = LocalDateTime.now();
        List<Donation> created = new ArrayList<>();

        // Four available listings: urgent, normal, prepared and long-life.
        created.add(save(
                perishable(seed.donorA(), 25, DONOR_A_LOCATION, now.plusHours(3), "2–8°C, refrigerated"),
                new AvailableState()));
        created.add(save(
                dryFood(seed.donorB(), 60, DONOR_B_LOCATION, 30),
                new AvailableState()));
        created.add(save(
                preparedMeal(seed.donorA(), 40, "meals", DONOR_A_LOCATION, 40, now.minusHours(1)),
                new AvailableState()));
        created.add(save(
                perishable(seed.donorB(), 12, DONOR_B_LOCATION, now.plusHours(26), "0–4°C, chilled"),
                new AvailableState()));

        // A claimed listing that is not assigned to a volunteer yet. The claim
        // runs through the real command so the admin undo button has history.
        Donation unassignedClaim = save(
                perishable(seed.donorA(), 30, DONOR_A_LOCATION, now.plusHours(8), "2–8°C, refrigerated"),
                new AvailableState());
        created.add(unassignedClaim);
        try {
            ClaimDonationCommand claim = new ClaimDonationCommand(
                    donations, unassignedClaim.getId(), seed.recipientA().getId());
            claim.execute();
            commandInvoker.record(claim);
        } catch (Exception e) {
            log.error("Could not seed the undoable claim command", e);
        }

        // A claimed listing already assigned to Bilal (ready for pickup).
        Donation assignedClaim = dryFood(seed.donorB(), 100, DONOR_B_LOCATION, 20);
        assignedClaim.setRecipientId(seed.recipientB().getId());
        assignedClaim.setVolunteerId(seed.volunteerA().getId());
        created.add(save(assignedClaim, new ClaimedState()));

        // An in-transit delivery waiting for Bilal to confirm delivery.
        Donation inTransit = preparedMeal(
                seed.donorA(), 50, "meals", DONOR_A_LOCATION, 50, now.minusHours(1));
        inTransit.setRecipientId(seed.recipientB().getId());
        inTransit.setVolunteerId(seed.volunteerA().getId());
        created.add(save(inTransit, new InTransitState()));

        // Three delivered donations so the impact dashboard and rankings fill in.
        created.add(delivered(seed.recipientA(), seed.volunteerA(),
                dryFood(seed.donorA(), 80, DONOR_A_LOCATION, 45)));
        created.add(delivered(seed.recipientB(), seed.volunteerB(),
                perishable(seed.donorB(), 45, DONOR_B_LOCATION, now.plusHours(24), "0–4°C, chilled")));
        created.add(delivered(seed.recipientA(), seed.volunteerB(),
                preparedMeal(seed.donorA(), 30, "meals", DONOR_A_LOCATION, 30, now.minusHours(2))));

        // Terminal and review states.
        created.add(save(
                perishable(seed.donorB(), 10, DONOR_B_LOCATION, now.minusHours(2), "ambient"),
                new ExpiredState()));
        created.add(save(
                preparedMeal(seed.donorB(), 5, "meals", DONOR_B_LOCATION, 5, now.minusHours(1)),
                new RejectedState()));
        created.add(save(
                dryFood(seed.donorA(), 15, DONOR_A_LOCATION, 10),
                new PendingReviewState()));

        updateUserHistories(seed, created);
        seedAuditTrail(seed, created);
        log.info("Seeded {} demo donations", created.size());
    }

    private Donation save(Donation donation, com.waslataam.state.DonationState state) {
        donation.setState(state);
        donations.save(donation);
        return donation;
    }

    private Donation delivered(Recipient recipient, Volunteer volunteer, Donation donation) {
        donation.setRecipientId(recipient.getId());
        donation.setVolunteerId(volunteer.getId());
        donation.setState(new DeliveredState());
        donations.save(donation);
        return donation;
    }

    private Donation perishable(Donor donor, double quantity, Location location,
                                LocalDateTime expiry, String temperature) {
        return donationFactory.create(DonationParams.builder()
                .type(DonationType.PERISHABLE)
                .quantity(quantity)
                .unit("kg")
                .donorId(donor.getId())
                .location(location)
                .expiryTime(expiry)
                .temperatureRequirement(temperature)
                .build());
    }

    private Donation dryFood(Donor donor, double quantity, Location location, int shelfLifeDays) {
        return donationFactory.create(DonationParams.builder()
                .type(DonationType.DRY_FOOD)
                .quantity(quantity)
                .unit("kg")
                .donorId(donor.getId())
                .location(location)
                .shelfLifeDays(shelfLifeDays)
                .build());
    }

    private Donation preparedMeal(Donor donor, double quantity, String unit, Location location,
                                  int servings, LocalDateTime cookedAt) {
        return donationFactory.create(DonationParams.builder()
                .type(DonationType.PREPARED_MEAL)
                .quantity(quantity)
                .unit(unit)
                .donorId(donor.getId())
                .location(location)
                .servings(servings)
                .cookedAt(cookedAt)
                .build());
    }

    private void updateUserHistories(SeedUsers seed, List<Donation> created) {
        Map<UUID, Donor> donors = Map.of(
                seed.donorA().getId(), seed.donorA(),
                seed.donorB().getId(), seed.donorB());
        Map<UUID, Recipient> recipients = Map.of(
                seed.recipientA().getId(), seed.recipientA(),
                seed.recipientB().getId(), seed.recipientB());

        for (Donation donation : created) {
            Donor donor = donors.get(donation.getDonorId());
            if (donor != null) {
                donor.addDonation(donation.getId());
                users.update(donor);
            }

            if (donation.getRecipientId() == null) {
                continue;
            }
            Recipient recipient = recipients.get(donation.getRecipientId());
            if (recipient == null) {
                continue;
            }
            if (donation.getStatus() == DonationStatus.CLAIMED
                    || donation.getStatus() == DonationStatus.IN_TRANSIT) {
                recipient.addClaimedDonation(donation.getId());
                recipient.increaseLoad((int) donation.getQuantity());
            } else if (donation.getStatus() == DonationStatus.DELIVERED) {
                recipient.completeDonation(donation.getId(), (int) donation.getQuantity());
            }
            users.update(recipient);
        }
    }

    // ------------------------------------------------------------------ audit

    private void seedAuditTrail(SeedUsers seed, List<Donation> created) {
        for (Donation donation : created) {
            DonationStatus status = donation.getStatus();
            DonationStatus postedAs = status == DonationStatus.AVAILABLE
                    ? DonationStatus.AVAILABLE
                    : DonationStatus.PENDING_REVIEW;

            auditTrail.log(donation.getDonorId(), "DONOR", "POST_DONATION",
                    donation.getId(), DonationStatus.PENDING_REVIEW, postedAs);

            switch (status) {
                case CLAIMED -> auditTrail.log(donation.getRecipientId(), "RECIPIENT", "CLAIM_DONATION",
                        donation.getId(), DonationStatus.AVAILABLE, DonationStatus.CLAIMED);
                case IN_TRANSIT -> {
                    auditTrail.log(donation.getRecipientId(), "RECIPIENT", "CLAIM_DONATION",
                            donation.getId(), DonationStatus.AVAILABLE, DonationStatus.CLAIMED);
                    auditTrail.log(donation.getVolunteerId(), "VOLUNTEER", "PICKUP_DONATION",
                            donation.getId(), DonationStatus.CLAIMED, DonationStatus.IN_TRANSIT);
                }
                case DELIVERED -> {
                    auditTrail.log(donation.getRecipientId(), "RECIPIENT", "CLAIM_DONATION",
                            donation.getId(), DonationStatus.AVAILABLE, DonationStatus.CLAIMED);
                    auditTrail.log(donation.getVolunteerId(), "VOLUNTEER", "PICKUP_DONATION",
                            donation.getId(), DonationStatus.CLAIMED, DonationStatus.IN_TRANSIT);
                    auditTrail.log(donation.getVolunteerId(), "VOLUNTEER", "DELIVER_DONATION",
                            donation.getId(), DonationStatus.IN_TRANSIT, DonationStatus.DELIVERED);
                }
                case EXPIRED -> auditTrail.log(seed.donorA().getId(), "SYSTEM", "EXPIRE_DONATION",
                        donation.getId(), DonationStatus.AVAILABLE, DonationStatus.EXPIRED);
                case REJECTED -> auditTrail.log(seed.donorB().getId(), "SYSTEM", "REJECT_DONATION",
                        donation.getId(), DonationStatus.PENDING_REVIEW, DonationStatus.REJECTED);
                default -> {
                    // AVAILABLE and PENDING_REVIEW already have their post entry.
                }
            }
        }
    }

    // ------------------------------------------------------------- dashboards

    private void recalculateDashboard() {
        List<Donation> all = donations.findAll();
        List<Donation> delivered = all.stream()
                .filter(donation -> donation.getStatus() == DonationStatus.DELIVERED)
                .toList();

        double kg = delivered.stream().mapToDouble(Donation::getQuantity).sum();
        int meals = delivered.stream().mapToInt(donation -> {
            if (donation instanceof com.waslataam.domain.PreparedMealDonation prepared) {
                return prepared.getServings();
            }
            return (int) (donation.getQuantity() * 2);
        }).sum();
        long active = all.stream()
                .filter(donation -> donation.getStatus().isActive())
                .count();

        dashboard.setKgSaved(kg);
        dashboard.setMealsServed(meals);
        dashboard.setCo2Avoided(kg * 2.5);
        dashboard.setActiveDonations((int) active);
        dashboard.setTotalDonors(users.findByRole(UserRole.DONOR).size());
        dashboard.setTotalRecipients(users.findByRole(UserRole.RECIPIENT).size());
        dashboard.setTotalVolunteers(users.findByRole(UserRole.VOLUNTEER).size());
    }

    private record SeedUsers(
            Donor donorA,
            Donor donorB,
            Recipient recipientA,
            Recipient recipientB,
            Volunteer volunteerA,
            Volunteer volunteerB
    ) {
    }
}
