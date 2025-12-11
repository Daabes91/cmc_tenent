package com.clinic.api;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.NumberFormat;
import java.util.Currency;
import java.util.Locale;

/**
 * Utility for consistent currency formatting across all API responses.
 * Ensures standardized currency representation with amount and currency code.
 */
public class CurrencyFormatter {

    /**
     * Standardized currency representation for API responses.
     */
    public static class CurrencyAmount {
        
        @JsonProperty("amount")
        private final BigDecimal amount;
        
        @JsonProperty("currency")
        private final String currency;
        
        @JsonProperty("formatted")
        private final String formatted;

        public CurrencyAmount(BigDecimal amount, String currency) {
            this.amount = amount != null ? amount.setScale(2, RoundingMode.HALF_UP) : BigDecimal.ZERO;
            this.currency = currency != null ? currency.toUpperCase() : "USD";
            this.formatted = formatCurrency(this.amount, this.currency);
        }

        public BigDecimal getAmount() { return amount; }
        public String getCurrency() { return currency; }
        public String getFormatted() { return formatted; }

        @Override
        public String toString() {
            return formatted;
        }
    }

    /**
     * Create a standardized currency amount.
     */
    public static CurrencyAmount of(BigDecimal amount, String currency) {
        return new CurrencyAmount(amount, currency);
    }

    /**
     * Create a standardized currency amount with default USD currency.
     */
    public static CurrencyAmount ofUSD(BigDecimal amount) {
        return new CurrencyAmount(amount, "USD");
    }

    /**
     * Format currency amount for display.
     */
    public static String formatCurrency(BigDecimal amount, String currencyCode) {
        if (amount == null) {
            return "0.00";
        }

        try {
            Currency currency = Currency.getInstance(currencyCode != null ? currencyCode : "USD");
            NumberFormat formatter = NumberFormat.getCurrencyInstance(Locale.US);
            formatter.setCurrency(currency);
            formatter.setMinimumFractionDigits(2);
            formatter.setMaximumFractionDigits(2);
            
            return formatter.format(amount);
        } catch (Exception e) {
            // Fallback to simple formatting if currency is invalid
            BigDecimal safeAmount = amount.setScale(2, RoundingMode.HALF_UP);
            return safeAmount + " " + (currencyCode != null ? currencyCode : "USD");
        }
    }

    /**
     * Validate currency code.
     */
    public static boolean isValidCurrency(String currencyCode) {
        if (currencyCode == null || currencyCode.trim().isEmpty()) {
            return false;
        }
        
        try {
            Currency.getInstance(currencyCode.toUpperCase());
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    /**
     * Get default currency code.
     */
    public static String getDefaultCurrency() {
        return "USD";
    }

    /**
     * Normalize currency code to uppercase.
     */
    public static String normalizeCurrency(String currencyCode) {
        if (currencyCode == null || currencyCode.trim().isEmpty()) {
            return getDefaultCurrency();
        }
        return currencyCode.toUpperCase().trim();
    }
}