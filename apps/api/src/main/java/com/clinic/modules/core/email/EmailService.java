package com.clinic.modules.core.email;

import com.clinic.config.SecurityProperties;
import com.sendgrid.*;
import com.sendgrid.helpers.mail.Mail;
import com.sendgrid.helpers.mail.objects.Attachments;
import com.sendgrid.helpers.mail.objects.Content;
import com.sendgrid.helpers.mail.objects.Email;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Base64;

@Service
public class EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);

    private final SecurityProperties securityProperties;
    private final SendGrid sendGridClient;

    public EmailService(SecurityProperties securityProperties) {
        this.securityProperties = securityProperties;
        String apiKey = securityProperties.email().sendgridApiKey();
        this.sendGridClient = StringUtils.hasText(apiKey) ? new SendGrid(apiKey) : null;
    }

    public void sendAppointmentConfirmation(
            String toEmail,
            String patientName,
            String doctorName,
            String serviceName,
            String appointmentDate,
            String appointmentTime,
            String consultationType
    ) {
        if (!isEmailEnabled()) {
            log.info("Email sending is disabled. Would have sent confirmation email to {}", toEmail);
            return;
        }

        String subject = "Appointment Confirmation - " + securityProperties.email().fromName();
        String htmlContent = buildConfirmationEmailHtml(
                patientName, doctorName, serviceName, appointmentDate, appointmentTime, consultationType
        );

        sendEmail(toEmail, subject, htmlContent);
    }

    public void sendAppointmentCancellation(
            String toEmail,
            String patientName,
            String doctorName,
            String serviceName,
            String appointmentDate,
            String appointmentTime
    ) {
        if (!isEmailEnabled()) {
            log.info("Email sending is disabled. Would have sent cancellation email to {}", toEmail);
            return;
        }

        String subject = "Appointment Cancelled - " + securityProperties.email().fromName();
        String htmlContent = buildCancellationEmailHtml(
                patientName, doctorName, serviceName, appointmentDate, appointmentTime
        );

        sendEmail(toEmail, subject, htmlContent);
    }

    /**
     * Send virtual consultation confirmation with Google Meet link and calendar attachment.
     */
    public void sendVirtualConsultationConfirmation(
            String toEmail,
            String patientName,
            String doctorName,
            String serviceName,
            ZonedDateTime appointmentStartTime,
            ZonedDateTime appointmentEndTime,
            String meetingLink
    ) {
        if (!isEmailEnabled()) {
            log.info("Email sending is disabled. Would have sent virtual consultation email to {}", toEmail);
            return;
        }

        String meetLink = StringUtils.hasText(meetingLink)
                ? meetingLink
                : securityProperties.email().googleMeetLink();
        String subject = "Virtual Consultation Confirmed - " + securityProperties.email().fromName();

        // Format dates for display
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("EEEE, MMMM d, yyyy");
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("h:mm a");
        String formattedDate = appointmentStartTime.format(dateFormatter);
        String formattedTime = appointmentStartTime.format(timeFormatter);

        // Generate Google Calendar link
        String eventTitle = "Virtual Consultation with " + doctorName;
        String eventDetails = "Virtual consultation for " + serviceName + " with " + doctorName +
                            ".\n\nJoin via Google Meet: " + meetLink;
        String googleCalendarUrl = CalendarUtil.generateGoogleCalendarUrl(
                eventTitle,
                eventDetails,
                "Virtual",
                appointmentStartTime,
                appointmentEndTime
        );

        // Build HTML email
        String htmlContent = buildVirtualConsultationEmailHtml(
                patientName,
                doctorName,
                serviceName,
                formattedDate,
                formattedTime,
                meetLink,
                googleCalendarUrl
        );

        // Generate .ics calendar file
        String icsContent = CalendarUtil.generateIcsContent(
                eventTitle,
                eventDetails,
                "Virtual",
                appointmentStartTime,
                appointmentEndTime,
                meetLink
        );

        // Send email with calendar attachment
        sendEmailWithAttachment(toEmail, subject, htmlContent, icsContent, "appointment.ics");
    }

    private void sendEmail(String toEmail, String subject, String htmlContent) {
        try {
            Email from = new Email(
                    securityProperties.email().fromEmail(),
                    securityProperties.email().fromName()
            );
            Email to = new Email(toEmail);
            Content content = new Content("text/html", htmlContent);
            Mail mail = new Mail(from, subject, to, content);

            Request request = new Request();
            request.setMethod(Method.POST);
            request.setEndpoint("mail/send");
            request.setBody(mail.build());

            Response response = sendGridClient.api(request);

            if (response.getStatusCode() >= 200 && response.getStatusCode() < 300) {
                log.info("Email sent successfully to {} with subject: {}", toEmail, subject);
            } else {
                log.error("Failed to send email. Status: {}, Body: {}",
                        response.getStatusCode(), response.getBody());
            }
        } catch (IOException ex) {
            log.error("Error sending email to {}: {}", toEmail, ex.getMessage(), ex);
        }
    }

    private void sendEmailWithAttachment(String toEmail, String subject, String htmlContent,
                                         String attachmentContent, String attachmentFilename) {
        try {
            Email from = new Email(
                    securityProperties.email().fromEmail(),
                    securityProperties.email().fromName()
            );
            Email to = new Email(toEmail);
            Content content = new Content("text/html", htmlContent);
            Mail mail = new Mail(from, subject, to, content);

            // Add .ics file as attachment
            Attachments attachment = new Attachments();
            String base64Content = Base64.getEncoder().encodeToString(attachmentContent.getBytes());
            attachment.setContent(base64Content);
            attachment.setType("text/calendar");
            attachment.setFilename(attachmentFilename);
            attachment.setDisposition("attachment");
            mail.addAttachments(attachment);

            Request request = new Request();
            request.setMethod(Method.POST);
            request.setEndpoint("mail/send");
            request.setBody(mail.build());

            Response response = sendGridClient.api(request);

            if (response.getStatusCode() >= 200 && response.getStatusCode() < 300) {
                log.info("Email with attachment sent successfully to {} with subject: {}", toEmail, subject);
            } else {
                log.error("Failed to send email with attachment. Status: {}, Body: {}",
                        response.getStatusCode(), response.getBody());
            }
        } catch (IOException ex) {
            log.error("Error sending email with attachment to {}: {}", toEmail, ex.getMessage(), ex);
        }
    }

    private boolean isEmailEnabled() {
        return securityProperties.email().enabled() && sendGridClient != null;
    }

    private String buildConfirmationEmailHtml(
            String patientName,
            String doctorName,
            String serviceName,
            String appointmentDate,
            String appointmentTime,
            String consultationType
    ) {
        String clinicName = securityProperties.email().fromName();
        return "<!DOCTYPE html>" +
                "<html>" +
                "<head>" +
                "<meta charset=\"UTF-8\">" +
                "<meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">" +
                "<style>" +
                "body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }" +
                ".container { max-width: 600px; margin: 0 auto; padding: 20px; }" +
                ".header { background: linear-gradient(135deg, #2563eb 0%, #06b6d4 100%); color: white; padding: 30px 20px; text-align: center; border-radius: 8px 8px 0 0; }" +
                ".content { background: #f9fafb; padding: 30px 20px; border-radius: 0 0 8px 8px; }" +
                ".appointment-details { background: white; padding: 20px; border-radius: 8px; margin: 20px 0; border-left: 4px solid #2563eb; }" +
                ".detail-row { margin: 12px 0; }" +
                ".detail-label { font-weight: bold; color: #1f2937; }" +
                ".detail-value { color: #4b5563; }" +
                ".footer { text-align: center; margin-top: 30px; padding-top: 20px; border-top: 1px solid #e5e7eb; color: #6b7280; font-size: 14px; }" +
                "</style>" +
                "</head>" +
                "<body>" +
                "<div class=\"container\">" +
                "<div class=\"header\">" +
                "<h1 style=\"margin: 0;\">✓ Appointment Confirmed</h1>" +
                "<p style=\"margin: 10px 0 0 0;\">Your appointment has been successfully scheduled</p>" +
                "</div>" +
                "<div class=\"content\">" +
                "<p>Dear " + patientName + ",</p>" +
                "<p>Your appointment has been confirmed. Here are the details:</p>" +
                "<div class=\"appointment-details\">" +
                "<div class=\"detail-row\"><span class=\"detail-label\">Doctor:</span> <span class=\"detail-value\">" + doctorName + "</span></div>" +
                "<div class=\"detail-row\"><span class=\"detail-label\">Service:</span> <span class=\"detail-value\">" + serviceName + "</span></div>" +
                "<div class=\"detail-row\"><span class=\"detail-label\">Date:</span> <span class=\"detail-value\">" + appointmentDate + "</span></div>" +
                "<div class=\"detail-row\"><span class=\"detail-label\">Time:</span> <span class=\"detail-value\">" + appointmentTime + "</span></div>" +
                "<div class=\"detail-row\"><span class=\"detail-label\">Type:</span> <span class=\"detail-value\">" + consultationType + "</span></div>" +
                "</div>" +
                "<p><strong>Important Reminders:</strong></p>" +
                "<ul>" +
                "<li>Please arrive 10 minutes early for check-in</li>" +
                "<li>Bring your insurance card and valid ID</li>" +
                "<li>If you need to reschedule, please contact us at least 24 hours in advance</li>" +
                "</ul>" +
                "<p>If you have any questions, please don't hesitate to contact us.</p>" +
                "<div class=\"footer\">" +
                "<p>Thank you for choosing " + clinicName + "</p>" +
                "<p style=\"margin-top: 10px; font-size: 12px;\">This is an automated message. Please do not reply to this email.</p>" +
                "</div>" +
                "</div>" +
                "</div>" +
                "</body>" +
                "</html>";
    }

    private String buildCancellationEmailHtml(
            String patientName,
            String doctorName,
            String serviceName,
            String appointmentDate,
            String appointmentTime
    ) {
        String clinicName = securityProperties.email().fromName();
        return "<!DOCTYPE html>" +
                "<html>" +
                "<head>" +
                "<meta charset=\"UTF-8\">" +
                "<meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">" +
                "<style>" +
                "body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }" +
                ".container { max-width: 600px; margin: 0 auto; padding: 20px; }" +
                ".header { background: linear-gradient(135deg, #dc2626 0%, #ea580c 100%); color: white; padding: 30px 20px; text-align: center; border-radius: 8px 8px 0 0; }" +
                ".content { background: #f9fafb; padding: 30px 20px; border-radius: 0 0 8px 8px; }" +
                ".appointment-details { background: white; padding: 20px; border-radius: 8px; margin: 20px 0; border-left: 4px solid #dc2626; }" +
                ".detail-row { margin: 12px 0; }" +
                ".detail-label { font-weight: bold; color: #1f2937; }" +
                ".detail-value { color: #4b5563; }" +
                ".footer { text-align: center; margin-top: 30px; padding-top: 20px; border-top: 1px solid #e5e7eb; color: #6b7280; font-size: 14px; }" +
                "</style>" +
                "</head>" +
                "<body>" +
                "<div class=\"container\">" +
                "<div class=\"header\">" +
                "<h1 style=\"margin: 0;\">⚠ Appointment Cancelled</h1>" +
                "<p style=\"margin: 10px 0 0 0;\">Your appointment has been cancelled</p>" +
                "</div>" +
                "<div class=\"content\">" +
                "<p>Dear " + patientName + ",</p>" +
                "<p>This email confirms that your appointment has been cancelled:</p>" +
                "<div class=\"appointment-details\">" +
                "<div class=\"detail-row\"><span class=\"detail-label\">Doctor:</span> <span class=\"detail-value\">" + doctorName + "</span></div>" +
                "<div class=\"detail-row\"><span class=\"detail-label\">Service:</span> <span class=\"detail-value\">" + serviceName + "</span></div>" +
                "<div class=\"detail-row\"><span class=\"detail-row\"><span class=\"detail-label\">Date:</span> <span class=\"detail-value\">" + appointmentDate + "</span></div>" +
                "<div class=\"detail-row\"><span class=\"detail-label\">Time:</span> <span class=\"detail-value\">" + appointmentTime + "</span></div>" +
                "</div>" +
                "<p>If you would like to reschedule, please book a new appointment through our website or contact our office directly.</p>" +
                "<p>We hope to see you again soon!</p>" +
                "<div class=\"footer\">" +
                "<p>Thank you for choosing " + clinicName + "</p>" +
                "<p style=\"margin-top: 10px; font-size: 12px;\">This is an automated message. Please do not reply to this email.</p>" +
                "</div>" +
                "</div>" +
                "</div>" +
                "</body>" +
                "</html>";
    }

    private String buildVirtualConsultationEmailHtml(
            String patientName,
            String doctorName,
            String serviceName,
            String appointmentDate,
            String appointmentTime,
            String meetLink,
            String googleCalendarUrl
    ) {
        String clinicName = securityProperties.email().fromName();
        return "<!DOCTYPE html>" +
                "<html>" +
                "<head>" +
                "<meta charset=\"UTF-8\">" +
                "<meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">" +
                "<style>" +
                "body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }" +
                ".container { max-width: 600px; margin: 0 auto; padding: 20px; }" +
                ".header { background: linear-gradient(135deg, #10b981 0%, #059669 100%); color: white; padding: 30px 20px; text-align: center; border-radius: 8px 8px 0 0; }" +
                ".content { background: #f9fafb; padding: 30px 20px; border-radius: 0 0 8px 8px; }" +
                ".appointment-details { background: white; padding: 20px; border-radius: 8px; margin: 20px 0; border-left: 4px solid #10b981; }" +
                ".detail-row { margin: 12px 0; }" +
                ".detail-label { font-weight: bold; color: #1f2937; }" +
                ".detail-value { color: #4b5563; }" +
                ".button-container { text-align: center; margin: 30px 0; }" +
                ".meet-button { display: inline-block; background: linear-gradient(135deg, #10b981 0%, #059669 100%); color: white; padding: 14px 32px; text-decoration: none; border-radius: 6px; font-weight: bold; margin: 10px; }" +
                ".calendar-button { display: inline-block; background: linear-gradient(135deg, #3b82f6 0%, #2563eb 100%); color: white; padding: 14px 32px; text-decoration: none; border-radius: 6px; font-weight: bold; margin: 10px; }" +
                ".info-box { background: #eff6ff; border-left: 4px solid #3b82f6; padding: 15px; margin: 20px 0; border-radius: 4px; }" +
                ".footer { text-align: center; margin-top: 30px; padding-top: 20px; border-top: 1px solid #e5e7eb; color: #6b7280; font-size: 14px; }" +
                "</style>" +
                "</head>" +
                "<body>" +
                "<div class=\"container\">" +
                "<div class=\"header\">" +
                "<h1 style=\"margin: 0;\">&#x1F4F9; Virtual Consultation Confirmed</h1>" +
                "<p style=\"margin: 10px 0 0 0;\">Your online appointment has been successfully scheduled</p>" +
                "</div>" +
                "<div class=\"content\">" +
                "<p>Dear " + patientName + ",</p>" +
                "<p>Your virtual consultation has been confirmed. Here are the details:</p>" +
                "<div class=\"appointment-details\">" +
                "<div class=\"detail-row\"><span class=\"detail-label\">Doctor:</span> <span class=\"detail-value\">" + doctorName + "</span></div>" +
                "<div class=\"detail-row\"><span class=\"detail-label\">Service:</span> <span class=\"detail-value\">" + serviceName + "</span></div>" +
                "<div class=\"detail-row\"><span class=\"detail-label\">Date:</span> <span class=\"detail-value\">" + appointmentDate + "</span></div>" +
                "<div class=\"detail-row\"><span class=\"detail-label\">Time:</span> <span class=\"detail-value\">" + appointmentTime + " (Amman Time)</span></div>" +
                "<div class=\"detail-row\"><span class=\"detail-label\">Type:</span> <span class=\"detail-value\">Virtual Consultation</span></div>" +
                "</div>" +
                "<div class=\"button-container\">" +
                "<a href=\"" + meetLink + "\" class=\"meet-button\">&#x1F4F9; Join via Google Meet</a>" +
                "<a href=\"" + googleCalendarUrl + "\" class=\"calendar-button\">&#x1F4C5; Add to Google Calendar</a>" +
                "</div>" +
                "<div class=\"info-box\">" +
                "<strong>&#x1F4CC; Important Information:</strong>" +
                "<ul style=\"margin: 10px 0; padding-left: 20px;\">" +
                "<li>A calendar file (.ics) is attached to this email for easy calendar import</li>" +
                "<li>Please join the meeting 5 minutes early to test your connection</li>" +
                "<li>Make sure you have a stable internet connection and a working camera/microphone</li>" +
                "<li>The Google Meet link will be active 15 minutes before your appointment</li>" +
                "</ul>" +
                "</div>" +
                "<p><strong>Before Your Appointment:</strong></p>" +
                "<ul>" +
                "<li>Test your camera and microphone</li>" +
                "<li>Find a quiet, well-lit location</li>" +
                "<li>Have your medical records or previous test results ready if applicable</li>" +
                "<li>Prepare any questions you want to ask the doctor</li>" +
                "</ul>" +
                "<p>If you have any technical issues or questions, please don't hesitate to contact us.</p>" +
                "<div class=\"footer\">" +
                "<p>Thank you for choosing " + clinicName + "</p>" +
                "<p style=\"margin-top: 10px; font-size: 12px;\">This is an automated message. Please do not reply to this email.</p>" +
                "</div>" +
                "</div>" +
                "</div>" +
                "</body>" +
                "</html>";
    }

    /**
     * Send staff invitation email with password setup link.
     */
    public void sendStaffInvitation(
            String toEmail,
            String staffName,
            String setupUrl,
            long validityDays
    ) {
        if (!isEmailEnabled()) {
            log.info("Email sending is disabled. Would have sent staff invitation to {}", toEmail);
            return;
        }

        String subject = "Welcome to " + securityProperties.email().fromName() + " - Set Up Your Account";
        String htmlContent = buildStaffInvitationEmailHtml(staffName, setupUrl, validityDays);

        sendEmail(toEmail, subject, htmlContent);
    }

    private String buildStaffInvitationEmailHtml(
            String staffName,
            String setupUrl,
            long validityDays
    ) {
        String clinicName = securityProperties.email().fromName();
        return "<!DOCTYPE html>" +
                "<html>" +
                "<head>" +
                "<meta charset=\"UTF-8\">" +
                "<meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">" +
                "<style>" +
                "body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }" +
                ".container { max-width: 600px; margin: 0 auto; padding: 20px; }" +
                ".header { background: linear-gradient(135deg, #7c3aed 0%, #a855f7 100%); color: white; padding: 30px 20px; text-align: center; border-radius: 8px 8px 0 0; }" +
                ".content { background: #f9fafb; padding: 30px 20px; border-radius: 0 0 8px 8px; }" +
                ".button-container { text-align: center; margin: 30px 0; }" +
                ".setup-button { display: inline-block; background: linear-gradient(135deg, #7c3aed 0%, #a855f7 100%); color: white; padding: 14px 32px; text-decoration: none; border-radius: 6px; font-weight: bold; }" +
                ".info-box { background: #f3f4f6; border-left: 4px solid #7c3aed; padding: 15px; margin: 20px 0; border-radius: 4px; }" +
                ".footer { text-align: center; margin-top: 30px; padding-top: 20px; border-top: 1px solid #e5e7eb; color: #6b7280; font-size: 14px; }" +
                "</style>" +
                "</head>" +
                "<body>" +
                "<div class=\"container\">" +
                "<div class=\"header\">" +
                "<h1 style=\"margin: 0;\">&#x1F44B; Welcome to " + clinicName + "!</h1>" +
                "<p style=\"margin: 10px 0 0 0;\">You've been invited to join our team</p>" +
                "</div>" +
                "<div class=\"content\">" +
                "<p>Hello " + staffName + ",</p>" +
                "<p>You have been added as a staff member at " + clinicName + ". To get started, please set up your account password by clicking the button below:</p>" +
                "<div class=\"button-container\">" +
                "<a href=\"" + setupUrl + "\" class=\"setup-button\">&#x1F511; Set Up My Account</a>" +
                "</div>" +
                "<div class=\"info-box\">" +
                "<strong>&#x1F4CC; Important Information:</strong>" +
                "<ul style=\"margin: 10px 0; padding-left: 20px;\">" +
                "<li>This invitation link is valid for " + validityDays + " days</li>" +
                "<li>You will be able to set your own secure password</li>" +
                "<li>After setup, you can log in to the admin dashboard</li>" +
                "<li>Your permissions and access level have been configured by an administrator</li>" +
                "</ul>" +
                "</div>" +
                "<p><strong>Having trouble with the button?</strong></p>" +
                "<p>Copy and paste this link into your browser:</p>" +
                "<p style=\"background: #f3f4f6; padding: 10px; border-radius: 4px; font-size: 12px; word-break: break-all;\">" + setupUrl + "</p>" +
                "<p>If you did not expect this invitation or believe you received this email in error, please ignore it or contact us.</p>" +
                "<div class=\"footer\">" +
                "<p>Welcome to the " + clinicName + " team!</p>" +
                "<p style=\"margin-top: 10px; font-size: 12px;\">This is an automated message. Please do not reply to this email.</p>" +
                "</div>" +
                "</div>" +
                "</div>" +
                "</body>" +
                "</html>";
    }
}
