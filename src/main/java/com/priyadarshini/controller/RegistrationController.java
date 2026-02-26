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
    
 // --- TOGGLE ATTENDANCE ON EVENT DAY ---
    @org.springframework.web.bind.annotation.GetMapping("/admin/toggle-presence/{id}")
    public String togglePresence(@org.springframework.web.bind.annotation.PathVariable Long id, 
                                 HttpSession session, 
                                 jakarta.servlet.http.HttpServletRequest request) {
        
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
        
        // Smart Redirect: Send them back to the exact page they came from!
        String referer = request.getHeader("Referer");
        if (referer != null && !referer.isEmpty()) {
            return "redirect:" + referer;
        }
        
        // Fallback just in case
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
    
 // --- NEW: BULK EMAIL ENDPOINT ---
    @PostMapping("/admin/bulk-email")
    @org.springframework.web.bind.annotation.ResponseBody
    public String sendBulkEmail(@RequestParam("ids") java.util.List<Long> ids, 
                                @RequestParam("message") String message, 
                                HttpSession session) {
        if (session.getAttribute("ADMIN_LOGGED_IN") == null) return "Unauthorized";
        
        int successCount = 0;
        for (Long id : ids) {
            EventRegistration person = repository.findById(id).orElse(null);
            if (person != null && person.getEmailAddress() != null) {
                emailService.sendCustomEmail(person.getEmailAddress(), person.getName(), message);
                successCount++;
            }
        }
        return "Successfully sent " + successCount + " emails!";
    }

    // --- NEW: FULL DATABASE EXPORT ENDPOINT ---
    @GetMapping("/admin/export/all-details")
    public void exportAllDetailsCSV(jakarta.servlet.http.HttpServletResponse response, HttpSession session) throws Exception {
        if (session.getAttribute("ADMIN_LOGGED_IN") == null) return;
        
        response.setContentType("text/csv");
        response.setHeader("Content-Disposition", "attachment; filename=\"Priyadarshini_Full_Database.csv\"");
        
        java.util.List<EventRegistration> allData = repository.findAll();
        java.io.PrintWriter writer = response.getWriter();
        
        // CSV Headers
        writer.println("Sys_ID,Reg_ID,Name,Phone,Gender,Email,Education,Biz_Type,Biz_Name,Biz_Location,Biz_Email,Biz_Phone,Txn_ID,Fee_Paid,Is_Present");
        
        for (EventRegistration p : allData) {
            writer.println(
                p.getId() + "," +
                (p.getUniqueId() != null ? p.getUniqueId() : "") + "," +
                "\"" + p.getName() + "\"," +
                p.getPhoneNumber() + "," +
                p.getGender() + "," +
                p.getEmailAddress() + "," +
                "\"" + p.getBackground() + "\"," +
                "\"" + p.getBusinessType() + "\"," +
                "\"" + p.getBusinessName() + "\"," +
                "\"" + p.getBusinessLocation() + "\"," +
                (p.getBusinessEmail() != null ? p.getBusinessEmail() : "") + "," +
                (p.getBusinessPhoneNumber() != null ? p.getBusinessPhoneNumber() : "") + "," +
                (p.getTransactionId() != null ? p.getTransactionId() : "") + "," +
                p.getRegistrationFee() + "," +
                p.isPresent()
            );
        }
    }
    
 // --- TEMPORARY MOCK DATA GENERATOR ---
    @org.springframework.web.bind.annotation.GetMapping("/admin/generate-mock-data")
    @org.springframework.web.bind.annotation.ResponseBody
    public String generateMockData(HttpSession session) {
        // Security check
        if (session.getAttribute("ADMIN_LOGGED_IN") == null) return "Unauthorized. Please log in first.";

        String[] names = {"Aisha Sharma", "Rahul Verma", "Priya Desai", "Vikram Singh", "Neha Gupta", "Karan Patel", "Sneha Iyer", "Arjun Reddy", "Kavya Menon", "Rohan Mehta"};
        String[] genders = {"Female", "Male", "Female", "Male", "Female", "Male", "Female", "Male", "Female", "Others"};
        String[] types = {"IT / Technology", "Finance / Consulting", "Retail / E-commerce", "Manufacturing", "Healthcare / Wellness", "Education", "Retail / E-commerce", "IT / Technology", "Education", "Healthcare / Wellness"};

        int addedCount = 0;
        
        for (int i = 0; i < names.length; i++) {
            EventRegistration reg = new EventRegistration();
            reg.setName(names[i]);
            reg.setEmailAddress("testuser" + i + "@example.com");
            reg.setPhoneNumber(9800000000L + i); // Fake phone number
            reg.setGender(genders[i]);
            reg.setBackground("Master's Degree");
            reg.setBusinessName("Mock Enterprise " + (i + 1));
            reg.setBusinessType(types[i]);
            reg.setBusinessLocation("Mumbai, Maharashtra");
            reg.setPresent(i % 2 == 0); // Makes every alternating person "Present"

            // Handle logic for Fee and TXN ID
            boolean isMale = "Male".equalsIgnoreCase(genders[i]);
            reg.setRegistrationFee(isMale ? 2000.0 : 0.0);
            if (isMale) {
                reg.setTransactionId("MOCK-TXN-" + (1000 + i));
            }

            // Generate Unique ID
            long count = repository.count() + 1;
            reg.setUniqueId("PRIYA-MOCK-" + String.format("%03d", count));

            repository.save(reg);
            addedCount++;
        }

        return "<div style='font-family: sans-serif; text-align: center; margin-top: 50px;'>"
             + "<h2 style='color: #28a745;'>Successfully generated " + addedCount + " mock attendees!</h2>"
             + "<a href='/admin/dashboard' style='padding: 10px 20px; background: #E83E8C; color: white; text-decoration: none; border-radius: 5px;'>Go Back to Dashboard</a>"
             + "</div>";
    }
    
 // ==========================================
    // --- EVENT DAY STAFF PORTAL ROUTES ---
    // ==========================================

    @GetMapping("/staff")
    public String showStaffLogin() {
        return "staff-login";
    }

    @PostMapping("/staff/login")
    public String handleStaffLogin(@RequestParam String username, 
                                   @RequestParam String password, 
                                   HttpSession session, Model model) {
        // Simple staff credentials (you can change these!)
        if ("staff".equals(username) && "event123".equals(password)) {
            session.setAttribute("STAFF_LOGGED_IN", true);
            return "redirect:/staff/dashboard";
        }
        model.addAttribute("error", "Invalid staff credentials!");
        return "staff-login";
    }

    @GetMapping("/staff/dashboard")
    public String showStaffDashboard(HttpSession session, Model model) {
        if (session.getAttribute("STAFF_LOGGED_IN") == null) {
            return "redirect:/staff";
        }
        // Staff needs to see everyone to check them in
        model.addAttribute("registrations", repository.findAll());
        return "staff-dashboard";
    }

    @GetMapping("/staff/mark-present/{id}")
    public String staffMarkPresent(@org.springframework.web.bind.annotation.PathVariable Long id, HttpSession session) {
        if (session.getAttribute("STAFF_LOGGED_IN") == null) {
            return "redirect:/staff";
        }
        
        EventRegistration person = repository.findById(id).orElse(null);
        if (person != null) {
            // ONE-WAY LOCK: Only changes it if they are currently NOT present
            if (!person.isPresent()) {
                person.setPresent(true); 
                repository.save(person);
            }
            // If they are already present, it does nothing (only Admin can undo)
        }
        
        return "redirect:/staff/dashboard";
    }

    @GetMapping("/staff/logout")
    public String staffLogout(HttpSession session) {
        session.removeAttribute("STAFF_LOGGED_IN"); 
        return "redirect:/staff";
    }
    
 // --- API FOR SPONSOR FULL DATA (For PDF) ---
    @GetMapping("/admin/api/all-sponsors")
    @org.springframework.web.bind.annotation.ResponseBody
    public List<Sponsor> getAllSponsorsApi(HttpSession session) {
        if (session.getAttribute("ADMIN_LOGGED_IN") == null) return null;
        return sponsorRepository.findAll();
    }

    // --- FULL SPONSOR EXCEL EXPORT (CSV) ---
    @GetMapping("/admin/export/all-sponsors")
    public void exportAllSponsorsCSV(jakarta.servlet.http.HttpServletResponse response, HttpSession session) throws Exception {
        if (session.getAttribute("ADMIN_LOGGED_IN") == null) return;
        
        response.setContentType("text/csv");
        response.setHeader("Content-Disposition", "attachment; filename=\"Sponsors_Full_Database.csv\"");
        
        List<Sponsor> allSponsors = sponsorRepository.findAll();
        java.io.PrintWriter writer = response.getWriter();
        
        // CSV Headers
        writer.println("ID,Business_Name,Contact_Phone,Email,Tier,Business_Type,Amount,Approved,Email_Sent");
        
        for (Sponsor s : allSponsors) {
            writer.println(
                s.getId() + "," +
                "\"" + s.getBusinessName() + "\"," +
                s.getContactNumber() + "," +
                s.getEmail() + "," +
                "\"" + s.getSponsorshipType() + "\"," +
                "\"" + s.getBusinessType() + "\"," +
                s.getAmount() + "," +
                s.isApproved() + "," +
                s.isEmailSent()
            );
        }
    }
    
 // --- TEMPORARY SPONSOR MOCK DATA GENERATOR ---
    @GetMapping("/admin/generate-mock-sponsors")
    @org.springframework.web.bind.annotation.ResponseBody
    public String generateMockSponsors(HttpSession session) {
        if (session.getAttribute("ADMIN_LOGGED_IN") == null) return "Unauthorized";

        String[] bizNames = {"Oxita Tech", "Tally Solutions", "Kedia Capital", "Reliance Retail", "Standard Chartered"};
        String[] tiers = {"Title Sponsor", "Co-Powered By", "Co-Powered By", "Sponsored By", "Sponsored By"};
        String[] types = {"IT Services", "Software", "Finance", "Retail", "Banking"};
        Double[] amounts = {50000.0, 30000.0, 30000.0, 15000.0, 15000.0};

        for (int i = 0; i < bizNames.length; i++) {
            Sponsor s = new Sponsor();
            s.setBusinessName(bizNames[i]);
            s.setSponsorshipType(tiers[i]);
            s.setBusinessType(types[i]);
            s.setAmount(amounts[i]);
            s.setContactNumber(9900000000L + i);
            s.setEmail("partner" + i + "@example.com");
            s.setApproved(i < 3); // Mark first 3 as already approved
            s.setBusinessLocation("Mumbai, India");
            
            sponsorRepository.save(s);
        }

        return "<div style='font-family:sans-serif; text-align:center; padding-top:50px; color:white; background:#170A1C; height:100vh;'>"
             + "<h2 style='color:#D4AF37;'>5 Mock Sponsors Generated!</h2>"
             + "<a href='/admin/sponsors' style='color:#E83E8C;'>Go to Sponsor Dashboard</a>"
             + "</div>";
    }
    
    // --- BULK EMAIL FOR SPONSORS ---
    @PostMapping("/admin/bulk-email-sponsors")
    @org.springframework.web.bind.annotation.ResponseBody
    public String sendBulkEmailSponsors(@RequestParam("ids") List<Long> ids, 
                                        @RequestParam("message") String message, 
                                        HttpSession session) {
        if (session.getAttribute("ADMIN_LOGGED_IN") == null) return "Unauthorized";
        
        int successCount = 0;
        for (Long id : ids) {
            Sponsor s = sponsorRepository.findById(id).orElse(null);
            if (s != null && s.getEmail() != null) {
                // Reusing the custom email method we built earlier
                emailService.sendCustomEmail(s.getEmail(), s.getBusinessName(), message);
                successCount++;
            }
        }
        return "Successfully sent " + successCount + " emails to sponsors!";
    }
    
 // --- NEW: API ENDPOINT FOR PDF FULL EXPORT ---
    @org.springframework.web.bind.annotation.GetMapping("/admin/api/all-data")
    @org.springframework.web.bind.annotation.ResponseBody
    public java.util.List<EventRegistration> getAllDataApi(HttpSession session) {
        if (session.getAttribute("ADMIN_LOGGED_IN") == null) return null;
        return repository.findAll();
    }
}