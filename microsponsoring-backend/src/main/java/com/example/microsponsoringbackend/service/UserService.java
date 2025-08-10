package com.example.microsponsoringbackend.service;

import com.example.microsponsoringbackend.model.User;
import com.example.microsponsoringbackend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.example.microsponsoringbackend.dto.OrganisationProfileDTO;
import com.example.microsponsoringbackend.model.companyNonProfits;
import com.example.microsponsoringbackend.model.PageCustomizations;
import com.example.microsponsoringbackend.model.UserType;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class UserService {
    @Autowired
    private UserRepository userRepository;

    public List<User> findAll() {
        return userRepository.findAll();
    }

    public Optional<User> findById(UUID id) {
        return userRepository.findById(id);
    }

    public User save(User user) {
        return userRepository.save(user);
    }

    public void deleteById(UUID id) {
        userRepository.deleteById(id);
    }

    public boolean existsById(UUID id) {
        return userRepository.existsById(id);
    }

    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public OrganisationProfileDTO getOrganisationProfile(UUID userId) {
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isEmpty() || userOpt.get().getUserType() != UserType.ORGANISATION_NONPROFIT) {
            return null;
        }

        User user = userOpt.get();
        companyNonProfits company = user.getCompanyNonProfits();
        PageCustomizations customizations = (company != null) ? company.getPageCustomizations() : null;

        OrganisationProfileDTO dto = new OrganisationProfileDTO();

        // From User
        dto.setUserId(user.getUserId());
        dto.setUsername(user.getUsername());
        dto.setEmail(user.getEmail());
        dto.setFullName(user.getFullName());
        dto.setProfilePicture(user.getProfilePicture());
        dto.setWebsiteUrl(user.getWebsiteUrl());

        if (company != null) {
            // From companyNonProfits
            dto.setActivityType(company.getActivityType());
            dto.setDetails(company.getDetails());
            dto.setTotalSponsorships(company.getTotalSponsorships());
            dto.setCompanyId(company.getCompanyId());
        }

        if (customizations != null) {
            // From PageCustomizations
            dto.setBackgroundColor(customizations.getBackgroundColor());
            dto.setPrimaryColor(customizations.getPrimaryColor());
            dto.setSecondaryColor(customizations.getSecondaryColor());
            dto.setFontStyle(customizations.getFontStyle());
            dto.setLogoUrl(customizations.getLogoUrl());
            dto.setBannerImageUrl(customizations.getBannerImageUrl());
            dto.setBackgroundImageUrl(customizations.getBackgroundImageUrl());
        }

        return dto;
    }

    @Transactional
    public void updateOrganisationProfile(UUID userId, OrganisationProfileDTO dto) {
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isEmpty()) {
            return; // Or throw an exception
        }

        User user = userOpt.get();
        companyNonProfits company = user.getCompanyNonProfits();
        PageCustomizations customizations = (company != null) ? company.getPageCustomizations() : null;

        // Update User fields
        user.setFullName(dto.getFullName());
        user.setWebsiteUrl(dto.getWebsiteUrl());
        
        // Only update profile picture if it's a valid file URL (not base64 or full URL)
        if (dto.getProfilePicture() != null && dto.getProfilePicture().startsWith("/api/images/")) {
            user.setProfilePicture(dto.getProfilePicture());
        }

        // Update companyNonProfits fields
        if (company != null) {
            company.setActivityType(dto.getActivityType());
            company.setDetails(dto.getDetails());
        }

        // Update PageCustomizations fields
        if (customizations != null) {
            customizations.setBackgroundColor(dto.getBackgroundColor());
            customizations.setPrimaryColor(dto.getPrimaryColor());
            customizations.setSecondaryColor(dto.getSecondaryColor());
            customizations.setFontStyle(dto.getFontStyle());
            
            // Only update image URLs if they are valid file paths (not base64 or full URLs)
            if (dto.getLogoUrl() != null && dto.getLogoUrl().startsWith("/api/images/")) {
                customizations.setLogoUrl(dto.getLogoUrl());
            }
            if (dto.getBannerImageUrl() != null && dto.getBannerImageUrl().startsWith("/api/images/")) {
                customizations.setBannerImageUrl(dto.getBannerImageUrl());
            }
            if (dto.getBackgroundImageUrl() != null && dto.getBackgroundImageUrl().startsWith("/api/images/")) {
                customizations.setBackgroundImageUrl(dto.getBackgroundImageUrl());
            }
        }
        
        userRepository.save(user);
    }

    public List<User> findAllByUserType(UserType userType) {
        return userRepository.findAllByUserType(userType);
    }
} 