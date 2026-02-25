package com.priyadarshini.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;


@Entity
public class EventRegistration {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // --- NEW FIELDS FOR EVENT DAY ---
    private String uniqueId;
    private boolean present = false; // Defaults to false (not present yet)

    private boolean emailSent = false;

    // Add these at the bottom:
    public boolean isEmailSent() { return emailSent; }
    public void setEmailSent(boolean emailSent) { this.emailSent = emailSent; }

    // --- ADD THESE GETTERS AND SETTERS AT THE BOTTOM ---
    public String getUniqueId() {
        return uniqueId;
    }

    public void setUniqueId(String uniqueId) {
        this.uniqueId = uniqueId;
    }

    public boolean isPresent() {
        return present;
    }

    public void setPresent(boolean present) {
        this.present = present;
    }

    // Personal Details
    private String name;
    private Long phoneNumber; 
    private String gender;
    private String emailAddress;
    private String background; // Latest education
    
    // Business Details
    private String businessType;
    private String businessName;
    private String businessLocation;
    private String businessEmail;
    private Long businessPhoneNumber;
    private String transactionId;

    // Payment Details
    private double registrationFee; // We will calculate this based on gender later
    @jakarta.persistence.Lob
    @jakarta.persistence.Column(columnDefinition="LONGTEXT")
    private String paymentScreenshot;

    public String getPaymentScreenshot() {
        return paymentScreenshot;
    }

    public void setPaymentScreenshot(String paymentScreenshot) {
        this.paymentScreenshot = paymentScreenshot;
    }

	public EventRegistration() {
        // Default constructor required by JPA
    }

    // --- Getters and Setters ---
    // (I am only showing a few to save space, but you need them for ALL fields)

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

	public Long getPhoneNumber() {
		return phoneNumber;
	}

	public void setPhoneNumber(Long phoneNumber) {
		this.phoneNumber = phoneNumber;
	}

	public String getGender() {
		return gender;
	}

	public void setGender(String gender) {
		this.gender = gender;
	}

	public String getEmailAddress() {
		return emailAddress;
	}

	public void setEmailAddress(String emailAddress) {
		this.emailAddress = emailAddress;
	}

	public String getBackground() {
		return background;
	}

	public void setBackground(String background) {
		this.background = background;
	}

	public String getBusinessType() {
		return businessType;
	}

	public void setBusinessType(String businessType) {
		this.businessType = businessType;
	}

	public String getBusinessName() {
		return businessName;
	}

	public void setBusinessName(String businessName) {
		this.businessName = businessName;
	}

	public String getBusinessLocation() {
		return businessLocation;
	}

	public void setBusinessLocation(String businessLocation) {
		this.businessLocation = businessLocation;
	}

	public String getBusinessEmail() {
		return businessEmail;
	}

	public void setBusinessEmail(String businessEmail) {
		this.businessEmail = businessEmail;
	}

	public Long getBusinessPhoneNumber() {
		return businessPhoneNumber;
	}

	public void setBusinessPhoneNumber(Long businessPhoneNumber) {
		this.businessPhoneNumber = businessPhoneNumber;
	}

	public double getRegistrationFee() {
		return registrationFee;
	}

	public void setRegistrationFee(double registrationFee) {
		this.registrationFee = registrationFee;
	}

	public String getTransactionId() {
		return transactionId;
	}

	public void setTransactionId(String transactionId) {
		this.transactionId = transactionId;
	}
    
}
