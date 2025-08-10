package com.example.microsponsoringbackend.service;

import com.example.microsponsoringbackend.model.User;
import com.example.microsponsoringbackend.model.Sponsor;
import com.example.microsponsoringbackend.model.companyNonProfits;
import com.example.microsponsoringbackend.model.UserType;
import com.example.microsponsoringbackend.model.Status;
import com.example.microsponsoringbackend.repository.UserRepository;
import com.example.microsponsoringbackend.repository.SponsorRepository;
import com.example.microsponsoringbackend.repository.companyNonProfitsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.util.Date;
import java.util.UUID;

@Service
public class DataInitializationService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SponsorRepository sponsorRepository;

    @Autowired
    private companyNonProfitsRepository companyNonProfitsRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    // Temporarily disabled to prevent UTF-8 corruption errors
    // Data initialization is now handled by Liquibase via dummy_data.sql
    /*
    @PostConstruct
    public void initializeData() {
        System.out.println("Initializing test data...");

        try {
            // Create Admin User
            User adminUser = new User();
            adminUser.setUserId(UUID.randomUUID());
            adminUser.setUsername("admin");
            adminUser.setEmail("admin@microsponsoring.com");
            adminUser.setPassword(passwordEncoder.encode("admin123"));
            adminUser.setFullName("System Administrator");
            adminUser.setUserType(UserType.ADMIN);
            adminUser.setStatus(Status.ACTIVE);
            adminUser.setActive(true);
            adminUser.setIsVerified(true);
            adminUser.setAcceptedConditions(true);
            adminUser.setCreatedAt(new Date());
            adminUser.setUpdatedAt(new Date());
            userRepository.save(adminUser);

        // Create Sponsor User
        User sponsorUser = new User();
        sponsorUser.setUserId(UUID.randomUUID());
        sponsorUser.setUsername("sponsor1");
        sponsorUser.setEmail("sponsor1@example.com");
        sponsorUser.setPassword(passwordEncoder.encode("sponsor123"));
        sponsorUser.setFullName("John Sponsor");
        sponsorUser.setUserType(UserType.SPONSOR);
        sponsorUser.setStatus(Status.ACTIVE);
        sponsorUser.setActive(true);
        sponsorUser.setIsVerified(true);
        sponsorUser.setAcceptedConditions(true);
        sponsorUser.setCreatedAt(new Date());
        sponsorUser.setUpdatedAt(new Date());
        userRepository.save(sponsorUser);

        // Create Organisation User
        User orgUser = new User();
        orgUser.setUserId(UUID.randomUUID());
        orgUser.setUsername("org1");
        orgUser.setEmail("org1@example.com");
        orgUser.setPassword(passwordEncoder.encode("org123"));
        orgUser.setFullName("Charity Organization");
        orgUser.setUserType(UserType.ORGANISATION_NONPROFIT);
        orgUser.setStatus(Status.ACTIVE);
        orgUser.setActive(true);
        orgUser.setIsVerified(true);
        orgUser.setAcceptedConditions(true);
        orgUser.setCreatedAt(new Date());
        orgUser.setUpdatedAt(new Date());
        userRepository.save(orgUser);

        // Create Sponsor Record
        Sponsor sponsor = new Sponsor();
        sponsor.setSponsorId(UUID.randomUUID());
        sponsor.setUser(sponsorUser);
        sponsor.setPaymentMethod("CREDIT_CARD");
        sponsor.setSponcerCat("ENVIRONMENT");
        sponsor.setTotalAmountSpent(0.0);
        sponsor.setTotalSponsorships(0);
        sponsor.setCreatedAt(new Date());
        sponsor.setUpdatedAt(new Date());
        sponsorRepository.save(sponsor);

        // Create Company/Non-Profit Record
        companyNonProfits company = new companyNonProfits();
        company.setCompanyId(UUID.randomUUID());
        company.setUser(orgUser);
        company.setActivityType("ENVIRONMENTAL_CONSERVATION");
        company.setDetails("We work to protect endangered species and their habitats.");
        company.setTotalAmountReceived(0.0);
        company.setTotalSponsorships(0);
        company.setCreatedAt(new Date());
        company.setUpdatedAt(new Date());
        companyNonProfitsRepository.save(company);

        System.out.println("Test data initialized successfully!");
        System.out.println("Admin credentials: admin@microsponsoring.com / admin123");
        System.out.println("Sponsor credentials: sponsor1@example.com / sponsor123");
        System.out.println("Organisation credentials: org1@example.com / org123");
        
        } catch (Exception e) {
            System.err.println("Error during data initialization: " + e.getMessage());
            e.printStackTrace();
        }
    }
    */
} 