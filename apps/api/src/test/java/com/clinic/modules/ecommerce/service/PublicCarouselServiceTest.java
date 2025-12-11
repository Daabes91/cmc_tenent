package com.clinic.modules.ecommerce.service;

import com.clinic.modules.core.tenant.TenantEntity;
import com.clinic.modules.core.tenant.TenantService;
import com.clinic.modules.ecommerce.dto.PublicCarouselResponse;
import com.clinic.modules.ecommerce.model.*;
import com.clinic.modules.ecommerce.repository.CarouselRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for PublicCarouselService.
 */
@ExtendWith(MockitoExtension.class)
class PublicCarouselServiceTest {

    @Mock
    private CarouselRepository carouselRepository;

    @Mock
    private TenantService tenantService;

    @Mock
    private EcommerceFeatureService ecommerceFeatureService;

    @InjectMocks
    private PublicCarouselService publicCarouselService;

    private TenantEntity testTenant;
    private CarouselEntity testCarousel;
    private CarouselItemEntity testCarouselItem;

    @BeforeEach
    void setUp() {
        testTenant = new TenantEntity("test-tenant", "Test Tenant");
        testTenant.setSlug("test-clinic");

        testCarousel = new CarouselEntity();
        testCarousel.setId(1L);
        testCarousel.setTenant(testTenant);
        testCarousel.setName("Hero Carousel");
        testCarousel.setSlug("hero-carousel");
        testCarousel.setType(CarouselType.HERO);
        testCarousel.setPlacement("hero");
        testCarousel.setPlatform(Platform.BOTH);
        testCarousel.setIsActive(true);
        testCarousel.setMaxItems(5);
        testCarousel.setCreatedAt(LocalDateTime.now());
        testCarousel.setUpdatedAt(LocalDateTime.now());

        testCarouselItem = new CarouselItemEntity();
        testCarouselItem.setId(1L);
        testCarouselItem.setCarousel(testCarousel);
        testCarouselItem.setTenant(testTenant);
        testCarouselItem.setContentType(CarouselContentType.IMAGE);
        testCarouselItem.setTitle("Test Item");
        testCarouselItem.setSubtitle("Test Subtitle");
        testCarouselItem.setImageUrl("https://example.com/image.jpg");
        testCarouselItem.setCtaType(CallToActionType.LINK);
        testCarouselItem.setCtaText("Learn More");
        testCarouselItem.setSortOrder(1);
        testCarouselItem.setIsActive(true);
        testCarouselItem.setCreatedAt(LocalDateTime.now());
        testCarouselItem.setUpdatedAt(LocalDateTime.now());

        testCarousel.setItems(Arrays.asList(testCarouselItem));
    }

    @Test
    void resolveTenant_WithValidSlug_ShouldReturnTenant() {
        // Arrange
        when(tenantService.findActiveBySlug("test-clinic")).thenReturn(Optional.of(testTenant));
        doNothing().when(ecommerceFeatureService).validateEcommerceEnabled(1L);

        // Act
        TenantEntity result = publicCarouselService.resolveTenant("test-clinic", null);

        // Assert
        assertNotNull(result);
        assertEquals(testTenant.getId(), result.getId());
        assertEquals(testTenant.getSlug(), result.getSlug());
        verify(tenantService).findActiveBySlug("test-clinic");
        verify(ecommerceFeatureService).validateEcommerceEnabled(1L);
    }

    @Test
    void resolveTenant_WithValidDomain_ShouldReturnTenant() {
        // Arrange
        when(tenantService.findActiveBySlug(null)).thenReturn(Optional.empty());
        when(tenantService.findActiveByDomain("test-clinic.com")).thenReturn(Optional.of(testTenant));
        doNothing().when(ecommerceFeatureService).validateEcommerceEnabled(1L);

        // Act
        TenantEntity result = publicCarouselService.resolveTenant(null, "test-clinic.com");

        // Assert
        assertNotNull(result);
        assertEquals(testTenant.getId(), result.getId());
        verify(tenantService).findActiveByDomain("test-clinic.com");
        verify(ecommerceFeatureService).validateEcommerceEnabled(1L);
    }

    @Test
    void resolveTenant_WithInvalidSlugAndDomain_ShouldThrowException() {
        // Arrange
        when(tenantService.findActiveBySlug("invalid")).thenReturn(Optional.empty());
        when(tenantService.findActiveByDomain("invalid.com")).thenReturn(Optional.empty());

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                publicCarouselService.resolveTenant("invalid", "invalid.com"));
        
        assertTrue(exception.getMessage().contains("Tenant not found"));
    }

    @Test
    void getCarouselsByPlacementAndPlatform_ShouldReturnFilteredCarousels() {
        // Arrange
        List<CarouselEntity> carousels = Arrays.asList(testCarousel);
        when(carouselRepository.findByTenantIdAndPlacementAndPlatform(1L, "hero", Platform.WEB))
                .thenReturn(carousels);

        // Act
        List<PublicCarouselResponse> result = publicCarouselService.getCarouselsByPlacementAndPlatform(
                1L, "hero", Platform.WEB);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        
        PublicCarouselResponse response = result.get(0);
        assertEquals(testCarousel.getId(), response.getId());
        assertEquals(testCarousel.getName(), response.getName());
        assertEquals(testCarousel.getSlug(), response.getSlug());
        assertEquals(testCarousel.getType(), response.getType());
        assertEquals(testCarousel.getPlacement(), response.getPlacement());
        assertEquals(testCarousel.getPlatform(), response.getPlatform());
        
        // Verify carousel items are included
        assertNotNull(response.getItems());
        assertEquals(1, response.getItems().size());
        assertEquals(testCarouselItem.getTitle(), response.getItems().get(0).getTitle());
        
        verify(carouselRepository).findByTenantIdAndPlacementAndPlatform(1L, "hero", Platform.WEB);
    }

    @Test
    void getCarouselsByPlacement_ShouldReturnCarouselsForPlacement() {
        // Arrange
        List<CarouselEntity> carousels = Arrays.asList(testCarousel);
        when(carouselRepository.findByTenantIdAndPlacement(1L, "hero")).thenReturn(carousels);

        // Act
        List<PublicCarouselResponse> result = publicCarouselService.getCarouselsByPlacement(1L, "hero");

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testCarousel.getId(), result.get(0).getId());
        verify(carouselRepository).findByTenantIdAndPlacement(1L, "hero");
    }

    @Test
    void getActiveCarousels_ShouldReturnAllActiveCarousels() {
        // Arrange
        List<CarouselEntity> carousels = Arrays.asList(testCarousel);
        when(carouselRepository.findActiveByTenantId(1L)).thenReturn(carousels);

        // Act
        List<PublicCarouselResponse> result = publicCarouselService.getActiveCarousels(1L);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testCarousel.getId(), result.get(0).getId());
        verify(carouselRepository).findActiveByTenantId(1L);
    }

    @Test
    void convertToPublicResponse_ShouldIncludeMobileImageUrl() {
        // Arrange
        List<CarouselEntity> carousels = Arrays.asList(testCarousel);
        when(carouselRepository.findActiveByTenantId(1L)).thenReturn(carousels);

        // Act
        List<PublicCarouselResponse> result = publicCarouselService.getActiveCarousels(1L);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        
        PublicCarouselResponse response = result.get(0);
        assertNotNull(response.getItems());
        assertEquals(1, response.getItems().size());
        
        // Verify mobile image URL is generated
        String mobileImageUrl = response.getItems().get(0).getMobileImageUrl();
        assertNotNull(mobileImageUrl);
        assertTrue(mobileImageUrl.contains("mobile=true"));
        assertTrue(mobileImageUrl.contains("w=400"));
        assertTrue(mobileImageUrl.contains("h=300"));
    }
}