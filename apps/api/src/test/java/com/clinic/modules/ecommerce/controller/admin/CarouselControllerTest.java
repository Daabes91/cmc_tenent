package com.clinic.modules.ecommerce.controller.admin;

import com.clinic.modules.core.tenant.TenantEntity;
import com.clinic.modules.core.tenant.TenantService;
import com.clinic.modules.ecommerce.model.*;
import com.clinic.modules.ecommerce.service.CarouselService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Unit tests for CarouselController.
 */
@WebMvcTest(CarouselController.class)
class CarouselControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private CarouselService carouselService;

    @MockBean
    private TenantService tenantService;

    private TenantEntity tenant;
    private CarouselEntity carousel;

    @BeforeEach
    void setUp() {
        tenant = new TenantEntity("test-tenant", "Test Tenant");
        // Use reflection to set ID for testing
        try {
            var idField = TenantEntity.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(tenant, 1L);
        } catch (Exception e) {
            // Ignore for test
        }

        carousel = new CarouselEntity(tenant, "Test Carousel", "test-carousel", CarouselType.HERO, "HOME_PAGE");
        carousel.setPlatform(Platform.BOTH);
        // Use reflection to set ID for testing
        try {
            var idField = CarouselEntity.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(carousel, 1L);
        } catch (Exception e) {
            // Ignore for test
        }
    }

    @Test
    void createCarousel_WithValidData_ShouldReturnCreatedCarousel() throws Exception {
        // Given
        when(tenantService.requireTenant(1L)).thenReturn(tenant);
        when(carouselService.createCarousel(eq(tenant), eq("Test Carousel"), eq("test-carousel"), 
                eq(CarouselType.HERO), eq("HOME_PAGE"), eq(Platform.BOTH))).thenReturn(carousel);

        String requestBody = """
                {
                    "name": "Test Carousel",
                    "slug": "test-carousel",
                    "type": "HERO",
                    "placement": "HOME_PAGE",
                    "platform": "BOTH"
                }
                """;

        // When & Then
        mockMvc.perform(post("/admin/tenants/1/carousels")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.name").value("Test Carousel"))
                .andExpect(jsonPath("$.data.slug").value("test-carousel"))
                .andExpect(jsonPath("$.data.type").value("HERO"))
                .andExpect(jsonPath("$.data.placement").value("HOME_PAGE"))
                .andExpect(jsonPath("$.data.platform").value("BOTH"));
    }

    @Test
    void createCarousel_WithInvalidData_ShouldReturnBadRequest() throws Exception {
        String requestBody = """
                {
                    "name": "",
                    "slug": "test-carousel",
                    "type": "HERO",
                    "placement": "HOME_PAGE",
                    "platform": "BOTH"
                }
                """;

        // When & Then
        mockMvc.perform(post("/admin/tenants/1/carousels")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getCarousels_ShouldReturnPagedResults() throws Exception {
        // Given
        List<CarouselEntity> carousels = Arrays.asList(carousel);
        Page<CarouselEntity> page = new PageImpl<>(carousels, PageRequest.of(0, 20), 1);
        when(carouselService.getCarouselsByTenant(eq(1L), any())).thenReturn(page);

        // When & Then
        mockMvc.perform(get("/admin/tenants/1/carousels")
                        .param("page", "0")
                        .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.totalElements").value(1));
    }

    @Test
    void getCarousel_WithValidId_ShouldReturnCarousel() throws Exception {
        // Given
        when(carouselService.getCarouselByTenantAndId(1L, 1L)).thenReturn(carousel);

        // When & Then
        mockMvc.perform(get("/admin/tenants/1/carousels/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.name").value("Test Carousel"));
    }

    @Test
    void deleteCarousel_WithValidId_ShouldReturnSuccess() throws Exception {
        // When & Then
        mockMvc.perform(delete("/admin/tenants/1/carousels/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void addCarouselItem_WithValidData_ShouldReturnCreatedItem() throws Exception {
        // Given
        CarouselItemEntity item = new CarouselItemEntity(carousel, tenant, CarouselContentType.IMAGE);
        item.setTitle("Test Item");
        item.setImageUrl("https://example.com/image.jpg");
        
        when(carouselService.addCarouselItem(eq(1L), eq(1L), eq(CarouselContentType.IMAGE),
                eq("Test Item"), eq("Test Subtitle"), eq("https://example.com/image.jpg"),
                isNull(), eq(CallToActionType.NONE), isNull(), isNull(), isNull())).thenReturn(item);

        String requestBody = """
                {
                    "contentType": "IMAGE",
                    "title": "Test Item",
                    "subtitle": "Test Subtitle",
                    "imageUrl": "https://example.com/image.jpg",
                    "ctaType": "NONE"
                }
                """;

        // When & Then
        mockMvc.perform(post("/admin/tenants/1/carousels/1/items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.contentType").value("IMAGE"))
                .andExpect(jsonPath("$.data.title").value("Test Item"));
    }
}