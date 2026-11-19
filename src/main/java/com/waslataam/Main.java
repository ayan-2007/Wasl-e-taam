package com.waslataam;

import com.waslataam.auth.AdminPolicy;
import com.waslataam.auth.AuthService;
import com.waslataam.chain.ApprovalHandler;
import com.waslataam.chain.ExpiryCheckHandler;
import com.waslataam.chain.LocationCheckHandler;
import com.waslataam.chain.QuantityCheckHandler;
import com.waslataam.chain.VerificationHandler;
import com.waslataam.coordination.DonationCoordinator;
import com.waslataam.coordination.VolunteerDispatcher;
import com.waslataam.command.AuditTrail;
import com.waslataam.command.CommandInvoker;
import com.waslataam.demo.DemoDataSeeder;
import com.waslataam.factory.DonationFactory;
import com.waslataam.factory.UserFactory;
import com.waslataam.observer.AuditLogger;
import com.waslataam.observer.ExpiryWatcher;
import com.waslataam.observer.ImpactDashboardObserver;
import com.waslataam.observer.RecipientNotifier;
import com.waslataam.repository.AccountRepository;
import com.waslataam.repository.Database;
import com.waslataam.repository.DonationRepository;
import com.waslataam.repository.InMemoryDonationRepo;
import com.waslataam.repository.SqliteAccountRepository;
import com.waslataam.repository.SqliteUserRepository;
import com.waslataam.repository.UserRepository;
import com.waslataam.strategy.MatchingEngine;
import com.waslataam.strategy.NearestFirstStrategy;
import com.waslataam.ui.util.SceneFactory;
import com.waslataam.viewmodel.DashboardViewModel;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class Main extends Application {
    private static DonationCoordinator staticCoordinator;
    private static AuthService staticAuthService;
    private DonationCoordinator coordinator;
    private ExpiryWatcher expiryWatcher;

    public static DonationCoordinator getCoordinator() {
        return staticCoordinator;
    }

    public static AuthService getAuthService() {
        return staticAuthService;
    }

    @Override
    public void start(Stage primaryStage) throws IOException {
        // Initialize dependencies (Dependency Injection)
        initializeDependencies();

        // Load login view
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/waslataam/ui/resources/fxml/LoginView.fxml"));
        Parent root = loader.load();

        Scene scene = SceneFactory.create(root, 1080, 720);

        primaryStage.setTitle("Wasl-e-Ta'am - Smart Food Surplus Redistribution");
        primaryStage.setScene(scene);
        primaryStage.setMinWidth(960);
        primaryStage.setMinHeight(640);
        primaryStage.centerOnScreen();
        primaryStage.show();
    }

    private void initializeDependencies() {
        // Database (users, credentials, administrator allowlist)
        Database database = Database.defaultDatabase();
        database.initialize();

        // Repositories
        DonationRepository donationRepository = new InMemoryDonationRepo();
        UserRepository userRepository = new SqliteUserRepository(database);
        AccountRepository accountRepository = new SqliteAccountRepository(database);

        // Factories
        DonationFactory donationFactory = new DonationFactory();
        UserFactory userFactory = new UserFactory();

        // Authentication
        AdminPolicy adminPolicy = new AdminPolicy(accountRepository);
        staticAuthService = new AuthService(accountRepository, userRepository, userFactory, adminPolicy);

        // Verification Chain (Chain of Responsibility)
        VerificationHandler expiryCheck = new ExpiryCheckHandler();
        VerificationHandler quantityCheck = new QuantityCheckHandler();
        VerificationHandler locationCheck = new LocationCheckHandler();
        VerificationHandler approval = new ApprovalHandler();

        expiryCheck.setNext(quantityCheck).setNext(locationCheck).setNext(approval);

        // Command Pattern
        CommandInvoker commandInvoker = new CommandInvoker();
        AuditTrail auditTrail = new AuditTrail();

        // Strategy Pattern
        MatchingEngine matchingEngine = new MatchingEngine(userRepository);
        matchingEngine.setStrategy(new NearestFirstStrategy());

        // Observer Pattern
        DashboardViewModel dashboardViewModel = DashboardViewModel.getInstance();
        
        RecipientNotifier recipientNotifier = new RecipientNotifier();
        AuditLogger auditLogger = new AuditLogger(auditTrail);
        ImpactDashboardObserver impactDashboardObserver = new ImpactDashboardObserver(dashboardViewModel);
        ExpiryWatcher expiryWatcher = new ExpiryWatcher(donationRepository, 30); // Check every 30 seconds

        // Coordinator (central hub)
        coordinator = new DonationCoordinator(
                donationRepository,
                userRepository,
                donationFactory,
                expiryCheck,
                commandInvoker,
                auditTrail,
                matchingEngine
        );
        staticCoordinator = coordinator;

        // Register observers
        coordinator.addObserver(recipientNotifier);
        coordinator.addObserver(auditLogger);
        coordinator.addObserver(impactDashboardObserver);
        coordinator.addObserver(expiryWatcher);

        // Seed demo accounts and a full lifecycle of demo donations.
        DemoDataSeeder seeder = new DemoDataSeeder(
                userRepository,
                accountRepository,
                userFactory,
                donationRepository,
                donationFactory,
                auditTrail,
                dashboardViewModel,
                commandInvoker
        );
        seeder.seed();

        // Start expiry watcher
        expiryWatcher.start();
        this.expiryWatcher = expiryWatcher;

        // Volunteer Dispatcher
        VolunteerDispatcher volunteerDispatcher = new VolunteerDispatcher(userRepository);
    }

    @Override
    public void stop() {
        if (expiryWatcher != null) {
            expiryWatcher.stop();
        }
        try {
            super.stop();
        } catch (Exception e) {
            // Ignore exception during shutdown
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}