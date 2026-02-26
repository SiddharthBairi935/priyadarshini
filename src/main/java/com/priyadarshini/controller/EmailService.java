package com.priyadarshini.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import jakarta.mail.internet.MimeMessage;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    // ==========================================
    // --- 1. ATTENDEE CONFIRMATION EMAIL ---
    // ==========================================
    public void sendConfirmationEmail(String toEmail, String name, String uniqueId, boolean isPaid) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(toEmail);
            helper.setSubject("Registration Confirmed. Welcome to Priyadarshini");

            String htmlMsg = buildEmailHeader()
                    + "<h2 style='color: #E83E8C; margin-top: 0; font-size: 22px;'>Dear " + name + ",</h2>"
                    + "<p style='font-size: 16px; color: #444;'>Thank you for registering for Priyadarshini. We’re delighted to have you with us.</p>"
                    + "<p style='font-size: 16px; color: #444;'>This is to confirm that you are successfully registered.</p>"
                    
                    + "<div style='background-color: #fdf0f5; border-left: 4px solid #E83E8C; padding: 20px; margin: 30px 0; border-radius: 6px;'>"
                    + "<p style='margin: 0; font-size: 13px; color: #666; text-transform: uppercase; letter-spacing: 1px;'>Your Registration ID</p>"
                    + "<p style='margin: 5px 0 0 0; font-size: 26px; font-weight: bold; color: #E83E8C; letter-spacing: 2px;'>" + uniqueId + "</p>"
                    + "</div>"
                    
                    + "<p style='font-size: 15px; color: #666; font-style: italic;'>Please keep this ID for future reference at the event.</p>";

            if (isPaid) {
                htmlMsg += "<p style='font-size: 16px; color: #28a745; font-weight: bold; background: #e6f4ea; padding: 10px; border-radius: 4px;'>✓ Your payment of ₹2000 has been successfully verified.</p>";
            }

            htmlMsg += buildEmailFooter();

            helper.setText(htmlMsg, true);
            mailSender.send(message);

        } catch (Exception e) {
            System.err.println("Failed to send attendee email: " + e.getMessage());
        }
    }

    // ==========================================
    // --- 2. SPONSOR APPROVAL EMAIL ---
    // ==========================================
    public void sendSponsorApprovalEmail(String toEmail, String businessName, String sponsorshipType, String sponsorId) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(toEmail);
            helper.setSubject("Sponsorship Confirmed. Welcome to Priyadarshini");

            String htmlMsg = buildEmailHeader()
                    + "<h2 style='color: #D4AF37; margin-top: 0; font-size: 22px;'>Dear " + businessName + ",</h2>"
                    + "<p style='font-size: 16px; color: #444;'>Thank you for partnering with Priyadarshini as a <strong>" + sponsorshipType + "</strong>. We’re delighted to have your brand with us.</p>"
                    + "<p style='font-size: 16px; color: #444;'>This is to confirm that your sponsorship is successfully approved and your logo is now live on our directory.</p>"
                    
                    + "<div style='background-color: #fcfaf2; border-left: 4px solid #D4AF37; padding: 20px; margin: 30px 0; border-radius: 6px;'>"
                    + "<p style='margin: 0; font-size: 13px; color: #666; text-transform: uppercase; letter-spacing: 1px;'>Your Sponsor ID</p>"
                    + "<p style='margin: 5px 0 0 0; font-size: 26px; font-weight: bold; color: #D4AF37; letter-spacing: 2px;'>" + sponsorId + "</p>"
                    + "</div>"
                    
                    + "<p style='font-size: 15px; color: #666; font-style: italic;'>Please keep this ID for future reference and communications.</p>"
                    + buildEmailFooter();

            helper.setText(htmlMsg, true);
            mailSender.send(message);

        } catch (Exception e) {
            System.err.println("Failed to send sponsor email: " + e.getMessage());
        }
    }

    // ==========================================
    // --- 3. BULK CUSTOM EMAIL SENDER ---
    // ==========================================
    // NEW: Now accepts an ID Label (e.g., "Registration ID") and the ID Value
    public void sendCustomEmail(String toEmail, String name, String idLabel, String idValue, String customMessage) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(toEmail);
            helper.setSubject("Message from Priyadarshini Organizing Committee");

            String htmlMsg = buildEmailHeader()
                    + "<h2 style='color: #E83E8C; margin-top: 0;'>Dear " + name + ",</h2>"
                    
                    // NEW: Dynamically injected ID Box for custom emails
                    + "<div style='background-color: #f9f9f9; border-left: 4px solid #6F42C1; padding: 15px 20px; margin: 20px 0; border-radius: 4px;'>"
                    + "<p style='margin: 0; font-size: 12px; color: #666; text-transform: uppercase; letter-spacing: 1px;'>" + idLabel + "</p>"
                    + "<p style='margin: 5px 0 0 0; font-size: 20px; font-weight: bold; color: #333; letter-spacing: 1px;'>" + idValue + "</p>"
                    + "</div>"
                    
                    + "<div style='font-size: 16px; color: #444; line-height: 1.6; white-space: pre-wrap;'>" + customMessage + "</div>"
                    + buildEmailFooter();

            helper.setText(htmlMsg, true);
            mailSender.send(message);

        } catch (Exception e) {
            System.err.println("Failed to send custom email to " + toEmail + ": " + e.getMessage());
        }
    }


    // ==========================================
    // --- PRIVATE HELPER METHODS (THE "MASTER TEMPLATE") ---
    // ==========================================
    
    private String buildEmailHeader() {
        return "<div style='font-family: \"Segoe UI\", Tahoma, Geneva, Verdana, sans-serif; background-color: #f4f4f4; padding: 40px 10px;'>"
             + "<div style='max-width: 600px; margin: 0 auto; background-color: #ffffff; border-radius: 12px; overflow: hidden; box-shadow: 0 8px 25px rgba(0,0,0,0.05);'>"
             
             // Top Sponsor Banner
             + "<div style='text-align: center; padding: 20px; background-color: #fafafa; border-bottom: 1px solid #eeeeee;'>"
             + "<p style='margin: 0 0 5px 0; font-size: 11px; color: #888; text-transform: uppercase; letter-spacing: 1px;'>Title Sponsor</p>"
             + "<img src='https://www.oxita.in/wp-content/themes/twentyfifteen/images/oxita-logo.png' alt='Oxita' style='max-height: 35px; margin-bottom: 15px;'/>"
             + "<p style='margin: 0 0 5px 0; font-size: 11px; color: #888; text-transform: uppercase; letter-spacing: 1px;'>Co-Powered By</p>"
             + "<div style='display: flex; justify-content: center; align-items: center;'>"
             
             // FIX: Using reliable, direct public links for Tally and Kedia
             + "<img src='https://raw.githubusercontent.com/SiddharthBairi935/Image/main/Screenshot%202026-02-25%20095054.png' alt='Tally Solutions' style='max-height: 25px; margin: 0 15px;'/>"
             + "<img src='https://raw.githubusercontent.com/SiddharthBairi935/Image/main/KediaLogo.webp' alt='Kedia Capital' style='max-height: 30px; margin: 0 15px;'/>"
             + "</div>"
             + "</div>"
             
             // Main Event Logo 
             // IMPORTANT: You must host your final logo somewhere stable (not ImgBB) and paste the link here!
             + "<div style='text-align: center; padding: 30px 20px; border-bottom: 3px solid #E83E8C;'>"
             + "<img src='https://raw.githubusercontent.com/SiddharthBairi935/Image/main/logo.png' alt='Priyadarshini Logo' style='max-height: 80px; width: auto; display: block; margin: 0 auto;'/>"
             + "</div>"
             
             // Start of Body Content
             + "<div style='padding: 40px 30px; line-height: 1.6;'>";
    }

    private String buildEmailFooter() {
        return "<p style='font-size: 16px; color: #444; margin-top: 30px;'>If you have any questions or need assistance, feel free to reach out to us at <a href='mailto:priyadarshini080326@gmail.com' style='color: #E83E8C; text-decoration: none; font-weight: bold;'>priyadarshini080326@gmail.com</a>.</p>"
             + "<p style='font-size: 16px; color: #444;'>We truly appreciate your trust and look forward to your participation.</p>"
             
             + "<div style='margin-top: 40px; padding-top: 20px; border-top: 1px solid #eeeeee;'>"
             + "<p style='margin: 0; font-size: 16px; color: #333;'>Warm regards,</p>"
             + "<p style='margin: 5px 0 0 0; font-size: 16px; color: #E83E8C; font-weight: 600;'>Priyadarshini Organizing Committee</p>"
             + "</div>"
             
             // Close Body & Container
             + "</div></div></div>";
    }
}