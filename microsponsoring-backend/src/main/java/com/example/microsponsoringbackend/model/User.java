package com.example.microsponsoringbackend.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import com.fasterxml.jackson.annotation.JsonManagedReference;

import java.time.LocalDate;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "users")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "user_id", columnDefinition = "VARCHAR(36)")
    @org.hibernate.annotations.JdbcTypeCode(java.sql.Types.VARCHAR)
    private UUID userId;
    
    @Column
    private String email;
    
    @Column
    private String username;
    
    @Column
    private String password;
    
    @Column
    private String fullName;

    @Column
    private Boolean active=true;
    
    @Enumerated(EnumType.STRING)
    private UserType userType;
    
    @Enumerated(EnumType.STRING)
    private Status status;
    
    @Column
    private Boolean acceptedConditions;
    
    @Column
    private Date lastLogin;
    
    @Column(columnDefinition = "VARCHAR(500)")
    private String profilePicture;
    
    @Column
    private String bio;
    
    @Column
    private String location;
    
    @Column
    private String websiteUrl;
    
    @Column
    private Boolean isVerified;
    
    @Column
    private Date createdAt;
    
    @Column
    private Date updatedAt;
    
    @OneToOne(mappedBy = "user")
    @JsonManagedReference
    private companyNonProfits companyNonProfits;
    
    @OneToOne(mappedBy = "user")
    @JsonManagedReference
    private Sponsor sponsor;
    
    // Getters and Setters
    // Constructor
}