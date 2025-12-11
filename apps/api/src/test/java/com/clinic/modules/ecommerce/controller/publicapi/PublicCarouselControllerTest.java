package com.clinic.modules.ecommerce.controller.publicapi;

import com.clinic.modules.core.tenant.TenantEntity;
import com.clinic.modules.ecommerce.dto.PublicCarouselResponse;
import com.clinic.modules.ecommerce.model.CarouselType;
import com.clinic.modules.ecommerce.model.Platform;
import com.clinic.modules.ecommerce.service.PublicCarouselService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Unit tests for PublicCarouselController.
 */
@WebMvcTest(PublicCarouselController.class)
class PublicCarouselControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PublicCarouselService publicCarouselService;

    @Autowired
    private ObjectMapper objectMapper;

    private TenantEntity testTenant;
    private PublicCarouselResponse testCarouselResponse;

    @BeforeEach
    void setUp() {
        testTenant = new TenantEntity("test-tenant", "Test Tenant");
        testTenant.setSlug("test-clinic");

        testCarouselResponse = new PublicCarouselResponse(
                1L,
                "Hero Carousel",
                "hero-carousel",
                CarouselType.HERO,
                "hero",
                Platform.BOTH,
                5,
                Collections.emptyList(),
                LocalDateTime.now(),
                LocalDateTime.now()
        );
    }

    @Test
    void getCarousels_WithSlugOnly_ShouldReturnCarousels() throws Exception {
        // Arrange
        List<PublicCarouselResponse> carousels = Arrays.asList(testCarouselResponse);
        when(publicCarouselService.resolveTenant("test-clinic", null)).thenReturn(testTenant);
        when(publicCarouselService.getActiveCarousels(1L)).thenReturn(carousels);

        // Act & Assert
        mockMvc.perform(get("/public/carousels")
                        .param("slug", "test-clinic")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value("Hero Carousel"))
                .andExpect(jsonPath("$[0].slug").value("hero-carousel"))
                .andExpect(jsonPath("$[0].type").value("HERO"))
                .andExpect(jsonPath("$[0].placement").value("hero"))
                .andExpect(jsonPath("$[0].platform").value("BOTH"));

        verify(publicCarouselService).resolveTenant("test-clinic", null);
        verify(publicCarouselService).getActiveCarousels(1L);
    }

    @Test
    void getCarousels_WithPlacementAndPlatform_ShouldReturnFilteredCarousels() throws Exception {
        // Arrange
        List<PublicCarouselResponse> carousels = Arrays.asList(testCarouselResponse);
        when(publicCarouselService.resolveTenant("test-clinic", null)).thenReturn(testTenant);
        when(publicCarouselService.getCarouselsByPlacementAndPlatform(1L, "hero", Platform.WEB))
                .thenReturn(carousels);

        // Act & Assert
        mockMvc.perform(get("/public/carousels")
                        .param("slug", "test-clinic")
                        .param("placement", "hero")
                        .param("platform", "WEB")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].id").value(1));

        verify(publicCarouselService).resolveTenant("test-clinic", null);
        verify(publicCarouselService).getCarouselsByPlacementAndPlatform(1L, "hero", Platform.WEB);
    }

    @Test
    void getCarousels_WithPlacementOnly_ShouldReturnCarouselsForPlacement() throws Exception {
        // Arrange
        List<PublicCarouselResponse> carousels = Arrays.asList(testCarouselResponse);
        when(publicCarouselService.resolveTenant("test-clinic", null)).thenReturn(testTenant);
        when(publicCarouselService.getCarouselsByPlacement(1L, "hero")).thenReturn(carousels);

        // Act & Assert
        mockMvc.perform(get("/public/carousels")
                        .param("slug", "test-clinic")
                        .param("placement", "hero")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].id").value(1));

        verify(publicCarouselService).resolveTenant("test-clinic", null);
        verify(publicCarouselService).getCarouselsByPlacement(1L, "hero");
    }

    @Test
    void getCarouselsByPlacement_ShouldReturnCarouselsForSpecificPlacement() throws Exception {
        // Arrange
        List<PublicCarouselResponse> carousels = Arrays.asList(testCarouselResponse);
        when(publicCarouselService.resolveTenant("test-clinic", null)).thenReturn(testTenant);
        when(publicCarouselService.getCarouselsByPlacement(1L, "hero")).thenReturn(carousels);

        // Act & Assert
        mockMvc.perform(get("/public/carousels/placement/hero")
                        .param("slug", "test-clinic")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].id").value(1));

        verify(publicCarouselService).resolveTenant("test-clinic", null);
        verify(publicCarouselService).getCarouselsByPlacement(1L, "hero");
    }

    @Test
    void getAllActiveCarousels_ShouldReturnAllActiveCarousels() throws Exception {
        // Arrange
        List<PublicCarouselResponse> carousels = Arrays.asList(testCarouselResponse);
        when(publicCarouselService.resolveTenant("test-clinic", null)).thenReturn(testTenant);
        when(publicCarouselService.getActiveCarousels(1L)).thenReturn(carousels);

        // Act & Assert
        mockMvc.perform(get("/public/carousels/all")
                        .param("slug", "test-clinic")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].id").value(1));

        verify(publicCarouselService).resolveTenant("test-clinic", null);
        verify(publicCarouselService).getActiveCarousels(1L);
    }

    @Test
    void getCarousels_WithInvalidTenant_ShouldReturnBadRequest() throws Exception {
        // Arrange
        when(publicCarouselService.resolveTenant("invalid", null))
                .thenThrow(new IllegalArgumentException("Tenant not found"));

        // Act & Assert
        mockMvc.perform(get("/public/carousels")
                        .param("slug", "invalid")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        verify(publicCarouselService).resolveTenant("invalid", null);
    }

    @Test
    void getCarousels_WithDomainParameter_ShouldResolveTenantByDomain() throws Exception {
        // Arrange
        List<PublicCarouselResponse> carousels = Arrays.asList(testCarouselResponse);
        when(publicCarouselService.resolveTenant(null, "test-clinic.com")).thenReturn(testTenant);
        when(publicCarouselService.getActiveCarousels(1L)).thenReturn(carousels);

        // Act & Assert
        mockMvc.perform(get("/public/carousels")
                        .param("domain", "test-clinic.com")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].id").value(1));

        verify(publicCarouselService).resolveTenant(null, "test-clinic.com");
        verify(publicCarouselService).getActiveCarousels(1L);
    }
}