package com.clinic.modules.ecommerce.dto;

/**
 * Request DTO for moving a category to a different parent.
 */
public record CategoryMoveRequest(
        
        Long parentId // null to make it a root category
) {
}