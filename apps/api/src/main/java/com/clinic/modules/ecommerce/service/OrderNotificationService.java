package com.clinic.modules.ecommerce.service;

import com.clinic.modules.core.email.EmailService;
import com.clinic.modules.ecommerce.model.OrderEntity;
import com.clinic.modules.ecommerce.model.OrderItemEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Handles order-related customer notifications.
 */
@Service
public class OrderNotificationService {

    private static final Logger log = LoggerFactory.getLogger(OrderNotificationService.class);
    private static final DateTimeFormatter CREATED_AT_FORMATTER =
        DateTimeFormatter.ofPattern("MMM dd, yyyy 'at' HH:mm");

    private final EmailService emailService;

    public OrderNotificationService(EmailService emailService) {
        this.emailService = emailService;
    }

    /**
     * Sends an order confirmation email to the customer.
     * Failures are logged and do not block the calling flow.
     */
    public void sendOrderConfirmationEmail(OrderEntity order) {
        if (order == null) {
            log.warn("Order confirmation email skipped: order was null");
            return;
        }

        if (!StringUtils.hasText(order.getCustomerEmail())) {
            log.info("Order confirmation email skipped for order {} because customer email is missing",
                    order.getOrderNumber());
            return;
        }

        try {
            String subject = "Your order " + order.getOrderNumber() + " has been received";
            String html = buildOrderConfirmationHtml(order);
            emailService.sendCustomEmail(order.getCustomerEmail(), subject, html);
        } catch (Exception ex) {
            log.warn("Failed to send order confirmation email for order {}: {}",
                    order.getOrderNumber(), ex.getMessage(), ex);
        }
    }

    private String buildOrderConfirmationHtml(OrderEntity order) {
        String currency = StringUtils.hasText(order.getCurrency()) ? order.getCurrency() : "USD";
        StringBuilder itemsBuilder = new StringBuilder();

        if (order.getItems() != null) {
            for (OrderItemEntity item : order.getItems()) {
                itemsBuilder.append("<div class=\"item-row\">")
                    .append("<div class=\"item-details\">")
                    .append("<div class=\"item-name\">").append(escapeHtml(item.getDisplayName())).append("</div>")
                    .append("<div class=\"item-meta\">Qty ")
                    .append(item.getQuantity() != null ? item.getQuantity() : 0)
                    .append(" • ").append(formatMoney(item.getUnitPrice(), currency)).append(" each</div>")
                    .append("</div>")
                    .append("<div class=\"item-price\">")
                    .append(formatMoney(item.getTotalPrice(), currency))
                    .append("</div>")
                    .append("</div>");
            }
        }

        String subtotal = formatMoney(order.getSubtotal(), currency);
        String tax = formatMoney(order.getTaxAmount(), currency);
        String shipping = formatMoney(order.getShippingAmount(), currency);
        String total = formatMoney(order.getTotalAmount(), currency);
        LocalDateTime createdAt = order.getCreatedAt();
        String createdAtFormatted = createdAt != null ? createdAt.format(CREATED_AT_FORMATTER) : "";

        return "<!DOCTYPE html>" +
            "<html><head><meta charset=\"UTF-8\"><meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">" +
            "<style>" +
            "body{margin:0;padding:0;font-family:Arial,sans-serif;background:#f6f9fb;color:#0f172a;}" +
            ".container{max-width:640px;margin:0 auto;padding:20px;}" +
            ".card{background:#fff;border-radius:14px;box-shadow:0 14px 38px rgba(15,23,42,0.1);" +
            "border:1px solid #e2e8f0;overflow:hidden;}" +
            ".header{background:linear-gradient(120deg,#2563eb,#0ea5e9);color:#fff;padding:26px 24px;}" +
            ".header h1{margin:0;font-size:22px;}" +
            ".header p{margin:6px 0 0 0;color:rgba(255,255,255,0.9);font-size:14px;}" +
            ".content{padding:24px;}" +
            ".pill{display:inline-block;padding:6px 12px;border-radius:999px;background:#e0f2fe;color:#0ea5e9;" +
            "font-weight:700;font-size:12px;letter-spacing:0.3px;margin-top:10px;}" +
            ".section-title{margin:0 0 12px 0;font-size:16px;}" +
            ".items{border:1px solid #e2e8f0;border-radius:12px;background:#f8fafc;padding:14px;}" +
            ".item-row{display:flex;justify-content:space-between;align-items:flex-start;padding:10px 0;" +
            "border-bottom:1px solid #e2e8f0;}" +
            ".item-row:last-child{border-bottom:none;}" +
            ".item-details{max-width:75%;}" +
            ".item-name{font-weight:700;font-size:14px;margin-bottom:4px;}" +
            ".item-meta{color:#475569;font-size:12px;}" +
            ".item-price{font-weight:700;font-size:14px;}" +
            ".totals{margin-top:18px;}" +
            ".total-row{display:flex;justify-content:space-between;margin:6px 0;font-size:14px;}" +
            ".total-row.total{font-size:16px;font-weight:800;color:#0f172a;}" +
            ".footer{margin-top:22px;font-size:12px;color:#64748b;}" +
            "</style></head><body>" +
            "<div class=\"container\"><div class=\"card\">" +
            "<div class=\"header\">" +
            "<h1>Thanks for your order</h1>" +
            "<p>Order " + escapeHtml(order.getOrderNumber()) + (StringUtils.hasText(createdAtFormatted) ? " · " + createdAtFormatted : "") + "</p>" +
            "<div class=\"pill\">Status: " + escapeHtml(order.getStatus().name().replace('_', ' ')) + "</div>" +
            "</div>" +
            "<div class=\"content\">" +
            "<h2 class=\"section-title\">Hi " + escapeHtml(order.getCustomerName()) + ", your order was created successfully.</h2>" +
            "<p style=\"margin:0 0 16px 0;color:#475569;font-size:14px;\">We are preparing your items. You'll get another email when it is on the way or once payment is confirmed.</p>" +
            "<div class=\"items\">" + itemsBuilder + "</div>" +
            "<div class=\"totals\">" +
            "<div class=\"total-row\"><span>Subtotal</span><span>" + subtotal + "</span></div>" +
            "<div class=\"total-row\"><span>Tax</span><span>" + tax + "</span></div>" +
            "<div class=\"total-row\"><span>Shipping</span><span>" + shipping + "</span></div>" +
            "<div class=\"total-row total\"><span>Total</span><span>" + total + "</span></div>" +
            "</div>" +
            "<p style=\"margin:18px 0 0 0;font-size:13px;color:#475569;\">Billing address: "
            + escapeHtml(order.getFullBillingAddress()) + "</p>" +
            "<div class=\"footer\">This is an automated message about your order. If you have questions, reply to this email.</div>" +
            "</div></div></div></body></html>";
    }

    private String formatMoney(BigDecimal amount, String currency) {
        if (amount == null) {
            return "0.00 " + currency;
        }
        return amount.setScale(2, RoundingMode.HALF_UP).toPlainString() + " " + currency;
    }

    private String escapeHtml(String value) {
        if (value == null) {
            return "";
        }
        return value
            .replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")
            .replace("\"", "&quot;");
    }
}
