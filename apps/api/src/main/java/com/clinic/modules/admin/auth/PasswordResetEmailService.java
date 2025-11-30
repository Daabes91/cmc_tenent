package com.clinic.modules.admin.auth;

import com.sendgrid.Method;
import com.sendgrid.Request;
import com.sendgrid.Response;
import com.sendgrid.SendGrid;
import com.sendgrid.helpers.mail.Mail;
import com.sendgrid.helpers.mail.objects.Content;
import com.sendgrid.helpers.mail.objects.Email;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;

/**
 * Service for sending password reset emails via SendGrid
 */
@Service
@Slf4j
public class PasswordResetEmailService {

    private final SendGrid sendGrid;
    private final String fromEmail;
    private final String fromName;
    private final boolean emailEnabled;
    private final String adminBaseUrl;

    public PasswordResetEmailService(
            @Value("${security.email.sendgrid-api-key}") String sendGridApiKey,
            @Value("${security.email.from-email}") String fromEmail,
            @Value("${security.email.from-name}") String fromName,
            @Value("${security.email.enabled}") boolean emailEnabled,
            @Value("${app.admin-url}") String adminBaseUrl) {
        this.sendGrid = new SendGrid(sendGridApiKey);
        this.fromEmail = fromEmail;
        this.fromName = fromName;
        this.emailEnabled = emailEnabled;
        this.adminBaseUrl = adminBaseUrl;
    }

    /**
     * Send password reset email to staff member
     * 
     * @param toEmail Recipient email address
     * @param resetToken Plain reset token
     * @param clinicName Name of the clinic
     * @param tenantSlug Tenant slug
     * @param language Language code (en or ar)
     */
    public void sendPasswordResetEmail(
            String toEmail,
            String resetToken,
            String clinicName,
            String tenantSlug,
            String language) {
        
        if (!emailEnabled) {
            log.info("Email sending disabled. Would have sent password reset email to: {}", toEmail);
            return;
        }

        try {
            String resetLink = buildResetLink(resetToken);
            String subject = buildSubject(language);
            String htmlContent = buildHtmlContent(resetLink, clinicName, tenantSlug, language);
            
            Email from = new Email(fromEmail, fromName);
            Email to = new Email(toEmail);
            Content content = new Content("text/html", htmlContent);
            Mail mail = new Mail(from, subject, to, content);

            Request request = new Request();
            request.setMethod(Method.POST);
            request.setEndpoint("mail/send");
            request.setBody(mail.build());

            Response response = sendGrid.api(request);
            
            if (response.getStatusCode() >= 200 && response.getStatusCode() < 300) {
                log.info("Password reset email sent successfully to: {}", toEmail);
            } else {
                log.error("Failed to send password reset email. Status: {}, Body: {}", 
                    response.getStatusCode(), response.getBody());
            }
        } catch (IOException e) {
            log.error("Error sending password reset email to: {}", toEmail, e);
        }
    }

    private String buildResetLink(String token) {
        return adminBaseUrl + "/reset-password?token=" + token;
    }

    private String buildSubject(String language) {
        if ("ar".equals(language)) {
            return "إعادة تعيين كلمة المرور";
        }
        return "Reset Your Password";
    }

    private String buildHtmlContent(String resetLink, String clinicName, String tenantSlug, String language) {
        if ("ar".equals(language)) {
            return buildArabicTemplate(resetLink, clinicName, tenantSlug);
        }
        return buildEnglishTemplate(resetLink, clinicName, tenantSlug);
    }

    private String buildEnglishTemplate(String resetLink, String clinicName, String tenantSlug) {
        String template = """
            <!DOCTYPE html>
            <html lang="en">
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <title>Reset Your Password</title>
                <style>
                    body {
                        font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif;
                        line-height: 1.6;
                        color: #0f172a;
                        max-width: 640px;
                        margin: 0 auto;
                        padding: 24px;
                        background: linear-gradient(135deg, #f8fafc, #eef2ff);
                    }
                    .card {
                        background-color: #ffffff;
                        border-radius: 16px;
                        padding: 32px;
                        box-shadow: 0 20px 50px rgba(79, 70, 229, 0.12);
                        border: 1px solid #e2e8f0;
                    }
                    .badge {
                        display: inline-flex;
                        align-items: center;
                        gap: 8px;
                        padding: 6px 12px;
                        border-radius: 999px;
                        background: #eef2ff;
                        color: #4338ca;
                        font-weight: 600;
                        font-size: 12px;
                        letter-spacing: 0.04em;
                        text-transform: uppercase;
                    }
                    .title {
                        font-size: 24px;
                        font-weight: 800;
                        margin: 16px 0 8px;
                        color: #111827;
                    }
                    .subtitle {
                        color: #475569;
                        margin-bottom: 24px;
                        font-size: 14px;
                    }
                    .cta {
                        display: inline-block;
                        padding: 14px 28px;
                        background: linear-gradient(135deg, #6366f1, #2563eb);
                        color: #ffffff !important;
                        text-decoration: none;
                        border-radius: 12px;
                        font-weight: 700;
                        letter-spacing: 0.01em;
                        box-shadow: 0 12px 30px rgba(79, 70, 229, 0.35);
                        transition: transform 0.15s ease, box-shadow 0.15s ease;
                    }
                    .cta:hover {
                        transform: translateY(-1px);
                        box-shadow: 0 16px 36px rgba(37, 99, 235, 0.35);
                    }
                    .info, .warning {
                        border-radius: 12px;
                        padding: 14px 16px;
                        margin: 18px 0;
                        font-size: 13px;
                    }
                    .info {
                        background: #eef2ff;
                        border: 1px solid #c7d2fe;
                        color: #312e81;
                    }
                    .warning {
                        background: #fff7ed;
                        border: 1px solid #fed7aa;
                        color: #9a3412;
                    }
                    .link {
                        word-break: break-all;
                        color: #2563eb;
                        font-size: 13px;
                        text-decoration: none;
                    }
                    .meta {
                        display: grid;
                        grid-template-columns: repeat(auto-fit, minmax(180px, 1fr));
                        gap: 12px;
                        margin: 20px 0 8px;
                    }
                    .meta-item {
                        background: #f8fafc;
                        border: 1px solid #e2e8f0;
                        border-radius: 10px;
                        padding: 10px 12px;
                        font-size: 13px;
                        color: #334155;
                    }
                    .footer {
                        text-align: center;
                        color: #64748b;
                        font-size: 12px;
                        margin-top: 28px;
                    }
                    @media (max-width: 600px) {
                        body { padding: 16px; }
                        .card { padding: 24px; }
                    }
                </style>
            </head>
            <body>
                <div class="card">
                    <div class="badge">Password Reset • CLINIC_NAME</div>
                    <div class="title">Reset your password securely</div>
                    <div class="subtitle">We received a request to reset your staff account password for TENANT_SLUG. Use the secure link below to continue.</div>

                    <div style="text-align:center; margin: 24px 0;">
                        <a href="RESET_LINK" class="cta">Create new password</a>
                    </div>

                    <div class="info">
                        <strong>⏰ Link expires in 1 hour.</strong><br>
                        For your security, this link is valid for the next 60 minutes only.
                    </div>

                    <div class="meta">
                        <div class="meta-item"><strong>Requested for:</strong><br>CLINIC_NAME (TENANT_SLUG)</div>
                        <div class="meta-item"><strong>Next step:</strong><br>Choose a new password</div>
                    </div>

                    <p style="margin:16px 0 8px; font-size:13px; color:#475569;">If the button doesn’t work, copy this link into your browser:</p>
                    <p class="link">RESET_LINK</p>

                    <div class="warning">
                        <strong>Security tips:</strong><br>
                        • Don’t share this link with anyone.<br>
                        • If you didn’t request this reset, ignore this email.<br>
                        • Your password stays the same until you complete this process.
                    </div>

                    <div class="footer">
                        This is an automated message from CLINIC_NAME (TENANT_SLUG).<br>
                        Need help? Contact your system administrator.
                    </div>
                </div>
            </body>
            </html>
            """;
        
        return template
            .replace("CLINIC_NAME", clinicName)
            .replace("TENANT_SLUG", tenantSlug)
            .replace("RESET_LINK", resetLink);
    }

    private String buildArabicTemplate(String resetLink, String clinicName, String tenantSlug) {
        String template = """
            <!DOCTYPE html>
            <html lang="ar" dir="rtl">
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <title>إعادة تعيين كلمة المرور</title>
                <style>
                    body {
                        font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif;
                        line-height: 1.6;
                        color: #333;
                        max-width: 600px;
                        margin: 0 auto;
                        padding: 20px;
                        background-color: #f5f5f5;
                        direction: rtl;
                    }
                    .container {
                        background-color: #ffffff;
                        border-radius: 8px;
                        padding: 40px;
                        box-shadow: 0 2px 4px rgba(0,0,0,0.1);
                    }
                    .header {
                        text-align: center;
                        margin-bottom: 30px;
                    }
                    .header h1 {
                        color: #2563eb;
                        margin: 0;
                        font-size: 24px;
                    }
                    .content {
                        margin-bottom: 30px;
                    }
                    .button {
                        display: inline-block;
                        padding: 14px 32px;
                        background-color: #2563eb;
                        color: #ffffff !important;
                        text-decoration: none;
                        border-radius: 6px;
                        font-weight: 600;
                        text-align: center;
                        margin: 20px 0;
                    }
                    .button:hover {
                        background-color: #1d4ed8;
                    }
                    .info-box {
                        background-color: #f0f9ff;
                        border-right: 4px solid #2563eb;
                        padding: 15px;
                        margin: 20px 0;
                        border-radius: 4px;
                    }
                    .warning-box {
                        background-color: #fef3c7;
                        border-right: 4px solid #f59e0b;
                        padding: 15px;
                        margin: 20px 0;
                        border-radius: 4px;
                    }
                    .footer {
                        text-align: center;
                        color: #6b7280;
                        font-size: 14px;
                        margin-top: 30px;
                        padding-top: 20px;
                        border-top: 1px solid #e5e7eb;
                    }
                    @media only screen and (max-width: 600px) {
                        body {
                            padding: 10px;
                        }
                        .container {
                            padding: 20px;
                        }
                        .button {
                            display: block;
                            width: 100%%;
                        }
                    }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <h1>CLINIC_NAME</h1>
                        <p style="color: #6b7280; margin-top: 5px;">TENANT_SLUG</p>
                    </div>
                    
                    <div class="content">
                        <p>مرحباً،</p>
                        <p>تلقينا طلباً لإعادة تعيين كلمة المرور لحساب الموظفين الخاص بك. انقر على الزر أدناه لإنشاء كلمة مرور جديدة:</p>
                        
                        <div style="text-align: center;">
                            <a href="RESET_LINK" class="button">إعادة تعيين كلمة المرور</a>
                        </div>
                        
                        <div class="info-box">
                            <strong>⏰ تنتهي صلاحية هذا الرابط خلال ساعة واحدة</strong><br>
                            لأسباب أمنية، سيعمل رابط إعادة تعيين كلمة المرور هذا لمدة ساعة واحدة فقط من وقت الطلب.
                        </div>
                        
                        <p>إذا لم يعمل الزر، انسخ والصق هذا الرابط في متصفحك:</p>
                        <p style="word-break: break-all; color: #2563eb; font-size: 14px;">RESET_LINK</p>
                        
                        <div class="warning-box">
                            <strong>🔒 تنبيه أمني</strong><br>
                            • لا تشارك هذا الرابط مع أي شخص<br>
                            • إذا لم تطلب إعادة التعيين هذه، يرجى تجاهل هذا البريد الإلكتروني<br>
                            • ستبقى كلمة المرور الخاصة بك دون تغيير حتى تقوم بإنشاء كلمة مرور جديدة
                        </div>
                    </div>
                    
                    <div class="footer">
                        <p>هذه رسالة تلقائية من CLINIC_NAME (TENANT_SLUG).</p>
                        <p>إذا كان لديك أي أسئلة، يرجى الاتصال بمسؤول النظام.</p>
                    </div>
                </div>
            </body>
            </html>
            """;
        
        return template
            .replace("CLINIC_NAME", clinicName)
            .replace("TENANT_SLUG", tenantSlug)
            .replace("RESET_LINK", resetLink);
    }
}
