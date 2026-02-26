package com.priyadarshini.model; // Adjust your package name if needed

import jakarta.persistence.*;

@Entity
public class Sponsor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String businessName;
    private Long contactNumber;
    private String email;
    private String businessType;
    private String sponsorshipType;
    private Double amount;
    private String businessLocation;
    
 // --- APPROVAL TRACKING ---
    private boolean approved = false; // Defaults to false (Pending Review)
    private boolean emailSent = false;

    // --- GETTERS & SETTERS ---
    public boolean isApproved() { return approved; }
    public void setApproved(boolean approved) { this.approved = approved; }
    
    public boolean isEmailSent() { return emailSent; }
    public void setEmailSent(boolean emailSent) { this.emailSent = emailSent; }

    @Lob
    @Column(columnDefinition = "LONGTEXT")
    private String logoBase64;

    // --- NEW PAYMENT FIELDS ---
    private String transactionId;

    @Lob
    @Column(columnDefinition = "LONGTEXT")
    private String paymentScreenshotBase64;

    // --- ADD THESE GETTERS & SETTERS AT THE BOTTOM ---
    public String getTransactionId() { return transactionId; }
    public void setTransactionId(String transactionId) { this.transactionId = transactionId; }

    public String getPaymentScreenshotBase64() { return paymentScreenshotBase64; }
    public void setPaymentScreenshotBase64(String paymentScreenshotBase64) { this.paymentScreenshotBase64 = paymentScreenshotBase64; }

    // --- GETTERS AND SETTERS ---
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getBusinessName() { return businessName; }
    public void setBusinessName(String businessName) { this.businessName = businessName; }

    public Long getContactNumber() { return contactNumber; }
    public void setContactNumber(Long contactNumber) { this.contactNumber = contactNumber; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getBusinessType() { return businessType; }
    public void setBusinessType(String businessType) { this.businessType = businessType; }

    public String getSponsorshipType() { return sponsorshipType; }
    public void setSponsorshipType(String sponsorshipType) { this.sponsorshipType = sponsorshipType; }

    public Double getAmount() { return amount; }
    public void setAmount(Double amount) { this.amount = amount; }

    public String getLogoBase64() { return logoBase64; }
    public void setLogoBase64(String logoBase64) { this.logoBase64 = logoBase64; }
    
    public String getBusinessLocation() { return businessLocation; }
    public void setBusinessLocation(String businessLocation) { this.businessLocation = businessLocation; }
}