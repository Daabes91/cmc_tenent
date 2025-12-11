package com.clinic.modules.core.email;

import com.clinic.config.SecurityProperties;
import com.clinic.modules.core.settings.ClinicSettingsEntity;
import com.clinic.modules.core.settings.ClinicSettingsRepository;
import com.clinic.modules.core.tenant.TenantContextHolder;
import com.clinic.modules.core.tenant.TenantService;
import com.clinic.modules.core.tenant.TenantEntity;
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
    private final ClinicSettingsRepository clinicSettingsRepository;
    private final TenantContextHolder tenantContextHolder;
    private final TenantService tenantService;

    public EmailService(SecurityProperties securityProperties,
                        ClinicSettingsRepository clinicSettingsRepository,
                        TenantContextHolder tenantContextHolder,
                        TenantService tenantService) {
        this.securityProperties = securityProperties;
        this.clinicSettingsRepository = clinicSettingsRepository;
        this.tenantContextHolder = tenantContextHolder;
        this.tenantService = tenantService;
    }

    public void sendAppointmentConfirmation(
            String toEmail,
            String patientName,
            String doctorName,
            String serviceName,
            String appointmentDate,
            String appointmentTime,
            String consultationType,
            String confirmationLink
    ) {
        ClinicSettingsEntity settings = currentClinicSettings();
        if (!isEmailEnabled(settings)) {
            log.info("Email sending is disabled. Would have sent confirmation email to {}", toEmail);
            return;
        }

        SendGrid client = resolveSendGridClient(settings);
        if (client == null) {
            log.warn("SendGrid API key missing; skipping confirmation email to {}", toEmail);
            return;
        }

        String clinicName = resolveFromName(settings);
        String logoUrl = resolveLogoUrl(settings);
        String subject = "Appointment Confirmation - " + clinicName;
        String htmlContent = buildConfirmationEmailHtml(
                patientName, doctorName, serviceName, appointmentDate, appointmentTime, consultationType, clinicName, confirmationLink, logoUrl
        );

        sendEmail(settings, client, toEmail, subject, htmlContent);
    }

    public void sendAppointmentCancellation(
            String toEmail,
            String patientName,
            String doctorName,
            String serviceName,
            String appointmentDate,
            String appointmentTime
    ) {
        ClinicSettingsEntity settings = currentClinicSettings();
        if (!isEmailEnabled(settings)) {
            log.info("Email sending is disabled. Would have sent cancellation email to {}", toEmail);
            return;
        }

        SendGrid client = resolveSendGridClient(settings);
        if (client == null) {
            log.warn("SendGrid API key missing; skipping cancellation email to {}", toEmail);
            return;
        }

        String clinicName = resolveFromName(settings);
        String logoUrl = resolveLogoUrl(settings);
        String subject = "Appointment Cancelled - " + clinicName;
        String htmlContent = buildCancellationEmailHtml(
                patientName, doctorName, serviceName, appointmentDate, appointmentTime, clinicName, logoUrl
        );

        sendEmail(settings, client, toEmail, subject, htmlContent);
    }

    /**
     * Send a custom HTML email using current clinic settings (SendGrid).
     */
    public void sendCustomEmail(String toEmail, String subject, String htmlContent) {
        ClinicSettingsEntity settings = currentClinicSettings();
        if (!isEmailEnabled(settings)) {
            log.info("Email sending is disabled. Would have sent custom email to {}", toEmail);
            return;
        }

        SendGrid client = resolveSendGridClient(settings);
        if (client == null) {
            log.warn("SendGrid API key missing; skipping custom email to {}", toEmail);
            return;
        }

        sendEmail(settings, client, toEmail, subject, htmlContent);
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
        ClinicSettingsEntity settings = currentClinicSettings();
        if (!isEmailEnabled(settings)) {
            log.info("Email sending is disabled. Would have sent virtual consultation email to {}", toEmail);
            return;
        }

        SendGrid client = resolveSendGridClient(settings);
        if (client == null) {
            log.warn("SendGrid API key missing; skipping virtual consultation email to {}", toEmail);
            return;
        }

        String meetLink = StringUtils.hasText(meetingLink)
                ? meetingLink
                : securityProperties.email().googleMeetLink();
        String clinicName = resolveFromName(settings);
        String logoUrl = resolveLogoUrl(settings);
        String subject = "Virtual Consultation Confirmed - " + clinicName;

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
                googleCalendarUrl,
                clinicName,
                logoUrl
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
        sendEmailWithAttachment(settings, client, toEmail, subject, htmlContent, icsContent, "appointment.ics");
    }

    private void sendEmail(ClinicSettingsEntity settings, SendGrid client,
                           String toEmail, String subject, String htmlContent) {
        try {
            Email from = new Email(resolveFromEmail(settings), resolveFromName(settings));
            Email to = new Email(toEmail);
            Content content = new Content("text/html", htmlContent);
            Mail mail = new Mail(from, subject, to, content);

            Request request = new Request();
            request.setMethod(Method.POST);
            request.setEndpoint("mail/send");
            request.setBody(mail.build());

            Response response = client.api(request);

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

    private void sendEmailWithAttachment(ClinicSettingsEntity settings, SendGrid client,
                                         String toEmail, String subject, String htmlContent,
                                         String attachmentContent, String attachmentFilename) {
        try {
            Email from = new Email(resolveFromEmail(settings), resolveFromName(settings));
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

            Response response = client.api(request);

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

    private ClinicSettingsEntity currentClinicSettings() {
        try {
            Long tenantId = tenantContextHolder.requireTenantId();
            return clinicSettingsRepository.findByTenantId(tenantId).orElse(null);
        } catch (IllegalStateException ex) {
            return null;
        }
    }

    private boolean isEmailEnabled(ClinicSettingsEntity settings) {
        Boolean enabled = settings != null ? settings.getEmailEnabled() : null;
        boolean allowed = enabled != null ? enabled : securityProperties.email().enabled();
        // Allow env fallback even if tenant has no SendGrid key; key presence is checked when building the client.
        return allowed;
    }

    private SendGrid resolveSendGridClient(ClinicSettingsEntity settings) {
        String apiKey = resolveSendgridApiKey(settings);
        return StringUtils.hasText(apiKey) ? new SendGrid(apiKey) : null;
    }

    private String resolveSendgridApiKey(ClinicSettingsEntity settings) {
        String key = securityProperties.email().sendgridApiKey();
        if (StringUtils.hasText(key)) {
            return key.trim();
        }
        // Fallback to raw env var if config binding failed
        String envKey = System.getenv("SENDGRID_API_KEY");
        if (StringUtils.hasText(envKey)) {
            log.warn("SendGrid API key resolved via environment fallback. Consider setting security.email.sendgrid-api-key.");
            return envKey.trim();
        }
        return null;
    }

    private String resolveFromEmail(ClinicSettingsEntity settings) {
        String envFrom = securityProperties.email().fromEmail();
        if (StringUtils.hasText(envFrom)) {
            return envFrom.trim();
        }
        // Allow raw environment fallback when properties binding misses (as seen in prod)
        String rawEnvFrom = System.getenv("EMAIL_FROM");
        if (!StringUtils.hasText(rawEnvFrom)) {
            rawEnvFrom = System.getenv("SECURITY_EMAIL_FROM_EMAIL");
        }
        if (StringUtils.hasText(rawEnvFrom)) {
            return rawEnvFrom.trim();
        }
        return "no-reply@localhost";
    }

    private String resolveFromName(ClinicSettingsEntity settings) {
        try {
            Long tenantId = tenantContextHolder.requireTenantId();
            TenantEntity tenant = tenantService.requireTenant(tenantId);
            if (tenant != null && StringUtils.hasText(tenant.getName())) {
                return tenant.getName().trim();
            }
        } catch (Exception ignored) {
        }
        String envFromName = securityProperties.email().fromName();
        if (StringUtils.hasText(envFromName)) {
            return envFromName.trim();
        }
        String rawEnvFromName = System.getenv("EMAIL_FROM_NAME");
        if (!StringUtils.hasText(rawEnvFromName)) {
            rawEnvFromName = System.getenv("SECURITY_EMAIL_FROM_NAME");
        }
        if (StringUtils.hasText(rawEnvFromName)) {
            return rawEnvFromName.trim();
        }
        if (settings != null && StringUtils.hasText(settings.getEmailFromName())) {
            return settings.getEmailFromName().trim();
        }
        return "Clinic";
    }

    private String resolveLogoUrl(ClinicSettingsEntity settings) {
        if (settings != null && StringUtils.hasText(settings.getLogoUrl())) {
            return settings.getLogoUrl().trim();
        }
        return null;
    }

    private String buildConfirmationEmailHtml(
            String patientName,
            String doctorName,
            String serviceName,
            String appointmentDate,
            String appointmentTime,
            String consultationType,
            String clinicName,
            String confirmationLink,
            String logoUrl
    ) {
        String brandPrimary = "#00A33B";
        String brandPrimaryDark = "#0f7c30";
        String brandSoft = "#e6f6ea";
        return "<!DOCTYPE html>" +
                "<html>" +
                "<head>" +
                "<meta charset=\"UTF-8\">" +
                "<meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">" +
                "<style>" +
                "body { font-family: Arial, sans-serif; line-height: 1.6; color: #1f2937; background: #f6f9f7; }" +
                ".container { max-width: 640px; margin: 0 auto; padding: 20px; }" +
                ".header { background: linear-gradient(135deg, " + brandPrimary + " 0%, " + brandPrimaryDark + " 100%); color: white; padding: 28px 22px; text-align: center; border-radius: 14px 14px 0 0; box-shadow: 0 10px 25px rgba(0,163,59,0.25); }" +
                ".brand { display:flex; align-items:center; gap:12px; justify-content:center; }" +
                ".brand img { max-height:48px; width:auto; border-radius:10px; background:white; padding:6px; }" +
                ".content { background: white; padding: 30px 22px; border-radius: 0 0 14px 14px; border:1px solid #e2e8f0; box-shadow: 0 10px 30px rgba(15,124,48,0.08); }" +
                ".appointment-details { background: " + brandSoft + "; padding: 20px; border-radius: 12px; margin: 20px 0; border: 1px solid " + brandPrimary + "1a; }" +
                ".detail-row { margin: 12px 0; }" +
                ".detail-label { font-weight: 700; color: #111827; }" +
                ".detail-value { color: #374151; }" +
                ".cta { text-align:center; margin: 24px 0; }" +
                ".cta a { display:inline-block; padding:14px 22px; background:" + brandPrimary + "; color:white; text-decoration:none; border-radius: 999px; font-weight:700; box-shadow: 0 12px 25px rgba(0,163,59,0.35); }" +
                ".footer { text-align: center; margin-top: 30px; padding-top: 18px; border-top: 1px solid #e5e7eb; color: #6b7280; font-size: 13px; }" +
                "</style>" +
                "</head>" +
                "<body>" +
                "<div class=\"container\">" +
                "<div class=\"header\">" +
                (logoUrl != null && !logoUrl.isBlank() ? "<div class=\"brand\"><img src=\"" + logoUrl + "\" alt=\"" + clinicName + " logo\" /></div>" : "") +
                "<h1 style=\"margin: 10px 0 0 0;\">✓ " + clinicName + "</h1>" +
                "<p style=\"margin: 8px 0 0 0;\">Your appointment has been successfully scheduled</p>" +
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
                (confirmationLink != null && !confirmationLink.isBlank()
                        ? "<p style=\"margin: 20px 0 10px 0;\">Please confirm you are coming:</p>" +
                        "<div class=\"cta\">" +
                        "<a href=\"" + confirmationLink + "\">Confirm Appointment</a>" +
                        "</div>"
                        : "") +
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
            String appointmentTime,
            String clinicName,
            String logoUrl
    ) {
        String brandPrimary = "#00A33B";
        String brandPrimaryDark = "#0f7c30";
        String brandSoft = "#fef2f2";
        return "<!DOCTYPE html>" +
                "<html>" +
                "<head>" +
                "<meta charset=\"UTF-8\">" +
                "<meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">" +
                "<style>" +
                "body { font-family: Arial, sans-serif; line-height: 1.6; color: #1f2937; background:#f7f8f9; }" +
                ".container { max-width: 640px; margin: 0 auto; padding: 20px; }" +
                ".header { background: linear-gradient(135deg, " + brandPrimaryDark + " 0%, #b91c1c 100%); color: white; padding: 28px 22px; text-align: center; border-radius: 14px 14px 0 0; box-shadow: 0 10px 25px rgba(0,0,0,0.12); }" +
                ".brand { display:flex; align-items:center; gap:12px; justify-content:center; }" +
                ".brand img { max-height:48px; width:auto; border-radius:10px; background:white; padding:6px; }" +
                ".content { background: white; padding: 30px 22px; border-radius: 0 0 14px 14px; border:1px solid #e2e8f0; box-shadow: 0 10px 30px rgba(185,28,28,0.1); }" +
                ".appointment-details { background: " + brandSoft + "; padding: 20px; border-radius: 12px; margin: 20px 0; border: 1px solid #fecaca; }" +
                ".detail-row { margin: 12px 0; }" +
                ".detail-label { font-weight: 700; color: #111827; }" +
                ".detail-value { color: #374151; }" +
                ".footer { text-align: center; margin-top: 30px; padding-top: 20px; border-top: 1px solid #e5e7eb; color: #6b7280; font-size: 13px; }" +
                "</style>" +
                "</head>" +
                "<body>" +
                "<div class=\"container\">" +
                "<div class=\"header\">" +
                (logoUrl != null && !logoUrl.isBlank() ? "<div class=\"brand\"><img src=\"" + logoUrl + "\" alt=\"" + clinicName + " logo\" /></div>" : "") +
                "<h1 style=\"margin: 10px 0 0 0;\">⚠ " + clinicName + "</h1>" +
                "<p style=\"margin: 8px 0 0 0;\">Your appointment has been cancelled</p>" +
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
            String googleCalendarUrl,
            String clinicName,
            String logoUrl
    ) {
        String brandPrimary = "#00A33B";
        String brandPrimaryDark = "#0f7c30";
        String brandSoft = "#e6f6ea";
        return "<!DOCTYPE html>" +
                "<html>" +
                "<head>" +
                "<meta charset=\"UTF-8\">" +
                "<meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">" +
                "<style>" +
                "body { font-family: Arial, sans-serif; line-height: 1.6; color: #1f2937; background:#f6f9f7; }" +
                ".container { max-width: 640px; margin: 0 auto; padding: 20px; }" +
                ".header { background: linear-gradient(135deg, " + brandPrimary + " 0%, " + brandPrimaryDark + " 100%); color: white; padding: 28px 22px; text-align: center; border-radius: 14px 14px 0 0; box-shadow: 0 10px 25px rgba(0,163,59,0.25); }" +
                ".brand { display:flex; align-items:center; gap:12px; justify-content:center; }" +
                ".brand img { max-height:48px; width:auto; border-radius:10px; background:white; padding:6px; }" +
                ".content { background: white; padding: 30px 22px; border-radius: 0 0 14px 14px; border:1px solid #e2e8f0; box-shadow: 0 10px 30px rgba(0,163,59,0.08); }" +
                ".appointment-details { background: " + brandSoft + "; padding: 20px; border-radius: 12px; margin: 20px 0; border: 1px solid " + brandPrimary + "1a; }" +
                ".detail-row { margin: 12px 0; }" +
                ".detail-label { font-weight: bold; color: #1f2937; }" +
                ".detail-value { color: #4b5563; }" +
                ".button-container { text-align: center; margin: 30px 0; }" +
                ".meet-button { display: inline-block; background: linear-gradient(135deg, " + brandPrimary + " 0%, " + brandPrimaryDark + " 100%); color: white; padding: 14px 32px; text-decoration: none; border-radius: 12px; font-weight: 700; margin: 10px; box-shadow: 0 12px 25px rgba(0,163,59,0.35); }" +
                ".calendar-button { display: inline-block; background: linear-gradient(135deg, #0ea5e9 0%, #2563eb 100%); color: white; padding: 14px 32px; text-decoration: none; border-radius: 12px; font-weight: 700; margin: 10px; box-shadow: 0 12px 25px rgba(37,99,235,0.35); }" +
                ".info-box { background: #eff6ff; border-left: 4px solid #2563eb; padding: 15px; margin: 20px 0; border-radius: 10px; }" +
                ".footer { text-align: center; margin-top: 30px; padding-top: 20px; border-top: 1px solid #e5e7eb; color: #6b7280; font-size: 14px; }" +
                "</style>" +
                "</head>" +
                "<body>" +
                "<div class=\"container\">" +
                "<div class=\"header\">" +
                (logoUrl != null && !logoUrl.isBlank() ? "<div class=\"brand\"><img src=\"" + logoUrl + "\" alt=\"" + clinicName + " logo\" /></div>" : "") +
                "<h1 style=\"margin: 10px 0 0 0;\">&#x1F4F9; " + clinicName + "</h1>" +
                "<p style=\"margin: 8px 0 0 0;\">Your online appointment has been successfully scheduled</p>" +
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
        ClinicSettingsEntity settings = currentClinicSettings();
        if (!isEmailEnabled(settings)) {
            log.info("Email sending is disabled. Would have sent staff invitation to {}", toEmail);
            return;
        }

        SendGrid client = resolveSendGridClient(settings);
        if (client == null) {
            log.warn("SendGrid API key missing; skipping staff invitation to {}", toEmail);
            return;
        }

        String clinicName = resolveFromName(settings);
        String subject = "Welcome to " + clinicName + " - Set Up Your Account";
        String htmlContent = buildStaffInvitationEmailHtml(staffName, setupUrl, validityDays, clinicName);

        sendEmail(settings, client, toEmail, subject, htmlContent);
    }

    private String buildStaffInvitationEmailHtml(
            String staffName,
            String setupUrl,
            long validityDays,
            String clinicName
    ) {
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
