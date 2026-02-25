package com.priyadarshini.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.priyadarshini.model.EventRegistration;
import com.priyadarshini.model.Sponsor;
import com.priyadarshini.repository.EventRegistrationRepository;
import com.priyadarshini.repository.SponsorRepository;

import jakarta.servlet.http.HttpSession;

@Controller
public class RegistrationController {
	
	@Autowired
    private EventRegistrationRepository repository;

    @Autowired
    private EmailService emailService;
    
    @Autowired
    private SponsorRepository sponsorRepository;

    @GetMapping("/register")
    public String showRegistrationForm(Model model) {
        model.addAttribute("registration", new EventRegistration());
        return "registration-form"; 
    }

    @PostMapping("/register")
    // Note the addition of ("registration") right next to @ModelAttribute below!
    public String submitRegistration(@ModelAttribute("registration") EventRegistration registration, 
                                     @RequestParam(value = "base64Screenshot", required = false) String base64Screenshot,
                                     Model model) {
        try {
            // 1. CHECK FOR DUPLICATES BEFORE SAVING
            boolean emailExists = repository.existsByEmailAddress(registration.getEmailAddress());
            boolean phoneExists = repository.existsByPhoneNumber(registration.getPhoneNumber());

            if (emailExists || phoneExists) {
                // This sends your exact custom message back to the HTML page
                model.addAttribute("errorMessage", "Email or Phone already registered!");
                return "registration-form"; 
            }

            // 2. Save the Base64 text string if it exists
            if (base64Screenshot != null && !base64Screenshot.isEmpty()) {
                registration.setPaymentScreenshot(base64Screenshot);
            }

         // 3. Set logic for Free vs Paid & Auto-Email
            boolean isMale = "Male".equalsIgnoreCase(registration.getGender());
            registration.setRegistrationFee(isMale ? 2000.0 : 0.0);

            // Generate Unique ID
            long count = repository.count() + 1;
            String dateStr = java.time.LocalDate.now().format(java.time.format.DateTimeFormatter.ofPattern("ddMM"));
            registration.setUniqueId("Priyadarshini" + dateStr + String.format("%03d", count));

            // Auto-send email IF Female/Others
            if (!isMale) {
                try {
                    emailService.sendConfirmationEmail(registration.getEmailAddress(), registration.getName(), registration.getUniqueId(), false);
                    registration.setEmailSent(true);
                } catch (Exception mailEx) {
                    System.out.println("Mail failed to send, but saving registration.");
                }
            }

            // 4. Save to Database
            repository.save(registration);
            
            // 5. REDIRECT TO THE NEW THANK YOU PAGE
            return "redirect:/success";
            
        } catch (Exception e) {
            model.addAttribute("errorMessage", "An unexpected error occurred.");
            e.printStackTrace();
            return "registration-form";
        }
    }

    // --- NEW SUCCESS PAGE ROUTE ---
    @GetMapping("/success")
    public String showSuccessPage() {
        return "registration-success";
    }

    // --- ADMIN ROUTES BELOW ---
    @GetMapping("/admin")
    public String showAdminLogin() {
        return "admin-login";
    }

    @PostMapping("/admin/login")
    public String handleAdminLogin(@RequestParam String username, 
                                   @RequestParam String password, 
                                   HttpSession session, Model model) {
        if ("admin".equals(username) && "admin123".equals(password)) {
            session.setAttribute("ADMIN_LOGGED_IN", true);
            return "redirect:/admin/dashboard";
        }
        model.addAttribute("error", "Invalid username or password!");
        return "admin-login";
    }

    @GetMapping("/admin/dashboard")
    public String showAdminDashboard(HttpSession session, Model model) {
        if (session.getAttribute("ADMIN_LOGGED_IN") == null) {
            return "redirect:/admin";
        }
        List<EventRegistration> allData = repository.findAll();
        model.addAttribute("registrations", allData);
        return "admin-dashboard";
    }

    @GetMapping("/admin/logout")
    public String adminLogout(HttpSession session) {
        session.invalidate(); 
        return "redirect:/admin";
    }
    
 // --- NEW: TOGGLE ATTENDANCE ON EVENT DAY ---
    @org.springframework.web.bind.annotation.GetMapping("/admin/toggle-presence/{id}")
    public String togglePresence(@org.springframework.web.bind.annotation.PathVariable Long id, HttpSession session) {
        // Protect the route
        if (session.getAttribute("ADMIN_LOGGED_IN") == null) {
            return "redirect:/admin";
        }
        
        // Find user, flip their status, and save!
        EventRegistration person = repository.findById(id).orElse(null);
        if (person != null) {
            person.setPresent(!person.isPresent()); 
            repository.save(person);
        }
        
        // Refresh the dashboard
        return "redirect:/admin/dashboard";
    }
    
 // --- SHOW DETAILS PAGE ---
    @GetMapping("/admin/details/{id}")
    public String showDetailsPage(@org.springframework.web.bind.annotation.PathVariable Long id, HttpSession session, Model model) {
        if (session.getAttribute("ADMIN_LOGGED_IN") == null) return "redirect:/admin";
        
        EventRegistration person = repository.findById(id).orElse(null);
        if (person == null) return "redirect:/admin/dashboard";
        
        model.addAttribute("person", person);
        return "admin-details";
    }
    
 // --- SHOW SPONSOR FORM ---
    @GetMapping("/sponsor-register")
    public String showSponsorForm(org.springframework.ui.Model model) {
        model.addAttribute("sponsor", new Sponsor());
        
        // Check limits
        long titleCount = sponsorRepository.countBySponsorshipType("Title Sponsor");
        long coPoweredCount = sponsorRepository.countBySponsorshipType("Co-Powered By");
        
        // Pass availability to HTML
        model.addAttribute("titleAvailable", titleCount < 1);
        model.addAttribute("coPoweredAvailable", coPoweredCount < 4);
        
        return "sponsor-form";
    }

    // --- SAVE SPONSOR ---
    @PostMapping("/sponsor-register")
    public String submitSponsor(@org.springframework.web.bind.annotation.ModelAttribute Sponsor sponsor, org.springframework.web.servlet.mvc.support.RedirectAttributes redirectAttributes) {
        
        if ("Title Sponsor".equals(sponsor.getSponsorshipType())) {
            if (sponsorRepository.countBySponsorshipType("Title Sponsor") >= 1) {
                redirectAttributes.addFlashAttribute("error", "Sorry, the Title Sponsor slot is already taken!");
                return "redirect:/sponsor-register";
            }
            sponsor.setAmount(50000.0);
            
        } else if ("Co-Powered By".equals(sponsor.getSponsorshipType())) {
            if (sponsorRepository.countBySponsorshipType("Co-Powered By") >= 4) {
                redirectAttributes.addFlashAttribute("error", "Sorry, all Co-Powered slots are sold out!");
                return "redirect:/sponsor-register";
            }
            sponsor.setAmount(30000.0);
            
        } else {
            sponsor.setAmount(15000.0); // Sponsored By
        }

        sponsorRepository.save(sponsor);
        return "redirect:/sponsor-success"; // Redirects to the new public list!
    }

    // --- ADMIN MANUAL EMAIL SENDER ---
    @GetMapping("/admin/send-email/{id}")
    public String adminSendEmail(@org.springframework.web.bind.annotation.PathVariable Long id, HttpSession session) {
        if (session.getAttribute("ADMIN_LOGGED_IN") == null) return "redirect:/admin";
        
        EventRegistration person = repository.findById(id).orElse(null);
        if (person != null && !person.isEmailSent()) {
            try {
                boolean isPaid = person.getRegistrationFee() > 0;
                emailService.sendConfirmationEmail(person.getEmailAddress(), person.getName(), person.getUniqueId(), isPaid);
                person.setEmailSent(true);
                repository.save(person);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return "redirect:/admin/details/" + id;
    }
    
 // --- ADMIN DASHBOARD ---
 // --- DEDICATED SPONSOR DASHBOARD ---
    @GetMapping("/admin/sponsors")
    public String sponsorDashboard(HttpSession session, org.springframework.ui.Model model) {
        if (session.getAttribute("ADMIN_LOGGED_IN") == null) return "redirect:/admin";
        
        model.addAttribute("sponsors", sponsorRepository.findAll());
        return "sponsor-dashboard";
    }
    
    @GetMapping("/sponsors")
    public String showPublicSponsorsList(org.springframework.ui.Model model) {
        model.addAttribute("sponsors", sponsorRepository.findByApprovedTrue()); // Changed this line!
        return "sponsors-list";
    }

    // --- NEW: PUBLIC SPONSOR DETAILS ---
    @GetMapping("/sponsors/{id}")
    public String showSponsorPublicDetails(@org.springframework.web.bind.annotation.PathVariable Long id, org.springframework.ui.Model model) {
        Sponsor sponsor = sponsorRepository.findById(id).orElse(null);
        if (sponsor == null) return "redirect:/sponsors";
        
        model.addAttribute("sponsor", sponsor);
        return "sponsor-details";
    }
    
    @GetMapping("/admin/sponsor/approve/{id}")
    public String approveSponsor(@org.springframework.web.bind.annotation.PathVariable Long id, HttpSession session) {
        if (session.getAttribute("ADMIN_LOGGED_IN") == null) return "redirect:/admin";
        
        Sponsor sponsor = sponsorRepository.findById(id).orElse(null);
        if (sponsor != null && !sponsor.isApproved()) {
            // Approve them
            sponsor.setApproved(true);
            
            // Send Email
            try {
                emailService.sendSponsorApprovalEmail(sponsor.getEmail(), sponsor.getBusinessName(), sponsor.getSponsorshipType());
                sponsor.setEmailSent(true);
            } catch (Exception e) {
                e.printStackTrace();
            }
            
            sponsorRepository.save(sponsor);
        }
        return "redirect:/admin/sponsors";
    }
 // --- SHOW THE SPONSOR SUCCESS PAGE ---
    @org.springframework.web.bind.annotation.GetMapping("/sponsor-success")
    public String showSponsorSuccess() {
        return "sponsor-success";
    }
}