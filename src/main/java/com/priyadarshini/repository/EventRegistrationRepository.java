package com.priyadarshini.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.priyadarshini.model.EventRegistration;

public interface EventRegistrationRepository extends JpaRepository<EventRegistration, Long> {
    
    // Add these two lines to check for duplicates!
    boolean existsByEmailAddress(String emailAddress);
    boolean existsByPhoneNumber(Long phoneNumber);
    
}