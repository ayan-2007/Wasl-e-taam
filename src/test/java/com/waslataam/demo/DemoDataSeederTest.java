package com.waslataam.demo;

import com.waslataam.auth.AdminPolicy;
import com.waslataam.auth.AuthService;
import com.waslataam.command.AuditTrail;
import com.waslataam.command.CommandInvoker;
import com.waslataam.domain.Admin;
import com.waslataam.domain.AdminPermission;
import com.waslataam.domain.Donation;
import com.waslataam.domain.DonationStatus;
import com.waslataam.domain.User;
import com.waslataam.domain.UserRole;
import com.waslataam.factory.DonationFactory;
import com.waslataam.factory.UserFactory;
import com.waslataam.repository.AccountRepository;
import com.waslataam.repository.Database;
import com.waslataam.repository.DonationRepository;
import com.waslataam.repository.InMemoryDonationRepo;
import com.waslataam.repository.SqliteAccountRepository;
import com.waslataam.repository.SqliteUserRepository;
import com.waslataam.repository.UserRepository;
import com.waslataam.viewmodel.DashboardViewModel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DemoDataSeederTest {

    @TempDir
    Path tempDir;

    private UserRepository users;
    private AccountRepository accounts;
    private DonationRepository donations;
    private CommandInvoker commandInvoker;
    private AuthService authService;

    @BeforeEach
    void setUp() {
        Database database = new Database(tempDir.resolve("seed-test.db"));
        database.initialize();

        users = new SqliteUserRepository(database);
        accounts = new SqliteAccountRepository(database);
        donations = new InMemoryDonationRepo();
        commandInvoker = new CommandInvoker();

        UserFactory userFactory = new UserFactory();
        DemoDataSeeder seeder = new DemoDataSeeder(
                users,
                accounts,
                userFactory,
                donations,
                new DonationFactory(),
                new AuditTrail(),
                DashboardViewModel.getInstance(),
                commandInvoker
        );
        seeder.seed();

        authService = new AuthService(accounts, users, userFactory, new AdminPolicy(accounts));
    }

    @Test
    void seedsTwoAdministratorsWithDifferentScopes() {
        List<User> admins = users.findByRole(UserRole.ADMIN);
        assertEquals(2, admins.size());

        Admin full = (Admin) findByEmail("admin@wasl.demo");
        assertEquals(EnumSet.allOf(AdminPermission.class), full.getPermissions());

        Admin limited = (Admin) findByEmail("ops@wasl.demo");
        assertFalse(limited.hasPermission(AdminPermission.MANAGE_STRATEGIES));
        assertFalse(limited.hasPermission(AdminPermission.UNDO_ACTIONS));
        assertTrue(limited.hasPermission(AdminPermission.VIEW_AUDIT_LOG));
    }

    @Test
    void seedsDonationsAcrossEveryStatus() {
        List<Donation> all = donations.findAll();
        assertTrue(all.size() >= DonationStatus.values().length);

        Set<DonationStatus> statuses = all.stream()
                .map(Donation::getStatus)
                .collect(Collectors.toSet());
        assertEquals(EnumSet.allOf(DonationStatus.class), statuses);
    }

    @Test
    void demoAccountsCanSignInAndUndoIsAvailable() throws Exception {
        User donor = authService.login("donor@wasl.demo", DemoAccounts.PASSWORD);
        assertEquals(UserRole.DONOR, donor.getRole());

        User fullAdmin = authService.login("admin@wasl.demo", DemoAccounts.PASSWORD);
        assertTrue(fullAdmin instanceof Admin);

        User ops = authService.login("ops@wasl.demo", DemoAccounts.PASSWORD);
        assertTrue(ops instanceof Admin);

        assertTrue(commandInvoker.canUndo(),
                "Seeded claim command should give the admin an undoable action");
    }

    @Test
    void seedingTwiceDoesNotDuplicateAccounts() {
        new DemoDataSeeder(
                users,
                accounts,
                new UserFactory(),
                donations,
                new DonationFactory(),
                new AuditTrail(),
                DashboardViewModel.getInstance(),
                commandInvoker
        ).seed();

        assertEquals(2, users.findByRole(UserRole.ADMIN).size());
        assertEquals(8, users.findAll().size());
    }

    private User findByEmail(String email) {
        return users.findByEmail(email).orElseThrow();
    }
}
