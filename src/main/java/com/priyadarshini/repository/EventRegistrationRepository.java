package com.priyadarshini.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.priyadarshini.model.EventRegistration;

@Repository
public interface EventRegistrationRepository extends JpaRepository<EventRegistration, Long> {
    
    // You don't need to write any code inside here right now!
    // JpaRepository automatically gives you methods like save(), findAll(), findById(), and deleteById().

}
