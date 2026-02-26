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

    public void sendConfirmationEmail(String toEmail, String name, String uniqueId, boolean isPaid) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(toEmail);
            helper.setSubject("Registration Confirmed - Priyadarshini Women's Event");

            // --- BUILD THE HTML EMAIL TEMPLATE ---
            String htmlMsg = "<div style='font-family: \"Segoe UI\", Tahoma, Geneva, Verdana, sans-serif; background-color: #f4f4f4; padding: 40px 10px;'>"
                    + "<div style='max-width: 600px; margin: 0 auto; background-color: #ffffff; border-radius: 12px; overflow: hidden; box-shadow: 0 4px 15px rgba(0,0,0,0.1);'>"
                    
                    // --- NEW SPONSORS HEADER ---
                    + "<div style='text-align: center; padding: 20px 20px 10px 20px; background-color: #fafafa; border-bottom: 1px solid #eeeeee;'>"
                    
                    // Title Sponsor (Oxita Link is already here)
                    + "<p style='margin: 0 0 5px 0; font-size: 11px; color: #888888; text-transform: uppercase; letter-spacing: 1px;'>Title Sponsor</p>"
                    + "<img src='https://www.oxita.in/wp-content/themes/twentyfifteen/images/oxita-logo.png' alt='Oxita' style='max-height: 35px; margin-bottom: 15px;'/>"
                    
                    // Co-Powered By (PASTE YOUR TALLY AND KEDIA LINKS HERE)
                    + "<p style='margin: 0 0 5px 0; font-size: 11px; color: #888888; text-transform: uppercase; letter-spacing: 1px;'>Co-Powered By</p>"
                    + "<div>"
                    + "<img src='https://i.ibb.co/5gh7kpvZ/Screenshot-2026-02-25-095054.png' alt='Tally Solutions' style='max-height: 25px; margin: 0 10px;'/>"
                    + "<img src='https://i.ibb.co/V0Kv0dbP/Kedia-Logo.webp' alt='Kedia Capital' style='max-height: 25px; margin: 0 10px;'/>"
                    + "</div>"
                    + "</div>"
                    
                    // --- MAIN EVENT LOGO (PASTE YOUR MAIN LOGO LINK HERE) ---
                    + "<div style='text-align: center; padding: 30px 20px; border-bottom: 3px solid #E83E8C;'>"
                    + "<img src='https://i.ibb.co/zdQqJsw/logo.png' alt='Priyadarshini Logo' style='max-height: 80px; width: auto; display: block; margin: 0 auto;'/>"
                    + "</div>"
                    
                    // --- BODY ---
                    + "<div style='padding: 40px 30px; color: #333333; line-height: 1.6;'>"
                    + "<h2 style='color: #E83E8C; margin-top: 0; font-size: 22px;'>Dear " + name + ",</h2>"
                    + "<p style='font-size: 16px;'>Thank you for registering for Priyadarshini.</p>"
                    + "<p style='font-size: 16px;'>This email confirms that we have successfully received your registration details. We appreciate your interest and look forward to your participation.</p>"
                    
                    // REGISTRATION ID BOX
                    + "<div style='background-color: #fdf0f5; border-left: 4px solid #E83E8C; padding: 15px 20px; margin: 30px 0; border-radius: 4px;'>"
                    + "<p style='margin: 0; font-size: 14px; color: #666; text-transform: uppercase; letter-spacing: 1px;'>Your Registration ID</p>"
                    + "<p style='margin: 5px 0 0 0; font-size: 24px; font-weight: bold; color: #E83E8C; letter-spacing: 1.5px;'>" + uniqueId + "</p>"
                    + "</div>";

            if (isPaid) {
                htmlMsg += "<p style='font-size: 16px; color: #28a745; font-weight: bold;'>✓ Your standard pass payment has been successfully verified.</p>";
            }

            // FOOTER & SIGN-OFF
            htmlMsg += "<p style='font-size: 16px;'>If you have any questions or require assistance, please feel free to contact us at <a href='mailto:priyadarshini080326@gmail.com' style='color: #E83E8C; text-decoration: none; font-weight: bold;'>priyadarshini080326@gmail.com</a>.</p>"
                    + "<p style='font-size: 16px;'>We value your trust and look forward to engaging with you.</p>"
                    
                    + "<div style='margin-top: 40px; padding-top: 20px; border-top: 1px solid #eeeeee;'>"
                    + "<p style='margin: 0; font-size: 16px; font-weight: bold; color: #333;'>Sincerely,</p>"
                    + "<p style='margin: 5px 0 0 0; font-size: 16px; color: #E83E8C; font-weight: 600;'>Priyadarshini Organizing Committee</p>"
                    + "</div>"
                    
                    + "</div>" 
                    + "</div>" 
                    + "</div>"; 

            helper.setText(htmlMsg, true);
            
            // NOTE: No attachments are added here anymore!
            
            mailSender.send(message);

        } catch (Exception e) {
            System.err.println("Failed to send email: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    public void sendSponsorApprovalEmail(String toEmail, String businessName, String sponsorshipType) {
        try {
            jakarta.mail.internet.MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(toEmail);
            helper.setSubject("Sponsorship Approved - Priyadarshini");

            String htmlMsg = "<div style='font-family: \"Segoe UI\", Tahoma, sans-serif; background-color: #f4f4f4; padding: 40px 10px;'>"
                    + "<div style='max-width: 600px; margin: 0 auto; background-color: #ffffff; border-radius: 12px; padding: 40px 30px; box-shadow: 0 4px 15px rgba(0,0,0,0.1);'>"
                    + "<h2 style='color: #D4AF37; margin-top: 0;'>Welcome, " + businessName + "!</h2>"
                    + "<p style='font-size: 16px; color: #333;'>We are thrilled to officially welcome you as a <strong>" + sponsorshipType + "</strong> for the Priyadarshini Women's Event.</p>"
                    + "<p style='font-size: 16px; color: #333;'>Your payment and details have been successfully verified by our team. Your business logo and profile are now live on our official sponsor directory.</p>"
                    + "<p style='font-size: 16px; color: #333;'>Thank you for empowering business leaders with us.</p>"
                    + "<div style='margin-top: 40px; padding-top: 20px; border-top: 1px solid #eeeeee;'>"
                    + "<p style='margin: 0; font-size: 16px; font-weight: bold; color: #333;'>Sincerely,</p>"
                    + "<p style='margin: 5px 0 0 0; font-size: 16px; color: #E83E8C; font-weight: 600;'>Priyadarshini Organizing Committee</p>"
                    + "</div></div></div>";

            helper.setText(htmlMsg, true);
            mailSender.send(message);

        } catch (Exception e) {
            System.err.println("Failed to send sponsor email: " + e.getMessage());
        }
    }
    
 // --- NEW: BULK CUSTOM EMAIL SENDER ---
    public void sendCustomEmail(String toEmail, String name, String customMessage) {
        try {
            jakarta.mail.internet.MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(toEmail);
            helper.setSubject("Message from Priyadarshini Organizing Committee");

            String htmlMsg = "<div style='font-family: \"Segoe UI\", Tahoma, sans-serif; background-color: #f4f4f4; padding: 40px 10px;'>"
                    + "<div style='max-width: 600px; margin: 0 auto; background-color: #ffffff; border-radius: 12px; padding: 40px 30px; box-shadow: 0 4px 15px rgba(0,0,0,0.1);'>"
                    + "<div style='text-align: center; padding-bottom: 20px; border-bottom: 3px solid #E83E8C;'>"
                    + "<img src='https://i.ibb.co/zdQqJsw/logo.png' alt='Priyadarshini Logo' style='max-height: 80px;'/>"
                    + "</div>"
                    + "<h2 style='color: #E83E8C; margin-top: 20px;'>Dear " + name + ",</h2>"
                    // Injects your custom message here! (Preserves line breaks)
                    + "<div style='font-size: 16px; color: #333; line-height: 1.6; white-space: pre-wrap;'>" + customMessage + "</div>"
                    + "<div style='margin-top: 40px; padding-top: 20px; border-top: 1px solid #eeeeee;'>"
                    + "<p style='margin: 0; font-size: 16px; font-weight: bold; color: #333;'>Sincerely,</p>"
                    + "<p style='margin: 5px 0 0 0; font-size: 16px; color: #E83E8C; font-weight: 600;'>Priyadarshini Organizing Committee</p>"
                    + "</div></div></div>";

            helper.setText(htmlMsg, true);
            mailSender.send(message);

        } catch (Exception e) {
            System.err.println("Failed to send custom email to " + toEmail + ": " + e.getMessage());
        }
    }
}