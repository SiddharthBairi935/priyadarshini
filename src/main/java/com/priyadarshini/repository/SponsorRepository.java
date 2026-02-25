package com.priyadarshini.repository;

import com.priyadarshini.model.Sponsor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SponsorRepository extends JpaRepository<Sponsor, Long> {
	long countBySponsorshipType(String sponsorshipType);
	java.util.List<Sponsor> findByApprovedTrue();
}