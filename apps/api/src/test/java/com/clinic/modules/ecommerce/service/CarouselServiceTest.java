package com.clinic.modules.ecommerce.service;

import com.clinic.modules.core.tenant.TenantEntity;
import com.clinic.modules.ecommerce.exception.EcommerceException;
import com.clinic.modules.ecommerce.model.*;
import com.clinic.modules.ecommerce.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for CarouselService.
 */
@ExtendWith(MockitoExtension.class)
class CarouselServiceTest {

    @Mock
    private CarouselRepository carouselRepository;

    @Mock
    private CarouselItemRepository carouselItemRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private CarouselService carouselService;

    private TenantEntity tenant;
    private CarouselEntity carousel;
    private CarouselItemEntity carouselItem;

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

        carouselItem = new CarouselItemEntity(carousel, tenant, CarouselContentType.IMAGE);
        carouselItem.setTitle("Test Item");
        carouselItem.setImageUrl("https://example.com/image.jpg");
        // Use reflection to set ID for testing
        try {
            var idField = CarouselItemEntity.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(carouselItem, 1L);
        } catch (Exception e) {
            // Ignore for test
        }
    }

    @Test
    void createCarousel_WithValidData_ShouldCreateCarousel() {
        // Given
        when(carouselRepository.existsByTenantIdAndSlug(tenant.getId(), "test-carousel")).thenReturn(false);
        when(carouselRepository.save(any(CarouselEntity.class))).thenReturn(carousel);

        // When
        CarouselEntity result = carouselService.createCarousel(
                tenant, "Test Carousel", "test-carousel", CarouselType.HERO, "HOME_PAGE", Platform.BOTH);

        // Then
        assertNotNull(result);
        assertEquals("Test Carousel", result.getName());
        assertEquals("test-carousel", result.getSlug());
        assertEquals(CarouselType.HERO, result.getType());
        assertEquals("HOME_PAGE", result.getPlacement());
        assertEquals(Platform.BOTH, result.getPlatform());
        verify(carouselRepository).save(any(CarouselEntity.class));
    }

    @Test
    void createCarousel_WithDuplicateSlug_ShouldThrowException() {
        // Given
        when(carouselRepository.existsByTenantIdAndSlug(tenant.getId(), "test-carousel")).thenReturn(true);

        // When & Then
        EcommerceException exception = assertThrows(EcommerceException.class, () ->
                carouselService.createCarousel(tenant, "Test Carousel", "test-carousel", CarouselType.HERO, "HOME_PAGE", Platform.BOTH));

        assertEquals("Carousel with slug 'test-carousel' already exists", exception.getMessage());
        verify(carouselRepository, never()).save(any(CarouselEntity.class));
    }

    @Test
    void createCarousel_WithInvalidType_ShouldThrowException() {
        // When & Then
        EcommerceException exception = assertThrows(EcommerceException.class, () ->
                carouselService.createCarousel(tenant, "Test Carousel", "test-carousel", null, "HOME_PAGE", Platform.BOTH));

        assertEquals("Carousel type is required", exception.getMessage());
    }

    @Test
    void createCarousel_WithInvalidPlatform_ShouldThrowException() {
        // When & Then
        EcommerceException exception = assertThrows(EcommerceException.class, () ->
                carouselService.createCarousel(tenant, "Test Carousel", "test-carousel", CarouselType.HERO, "HOME_PAGE", null));

        assertEquals("Platform is required", exception.getMessage());
    }

    @Test
    void createCarousel_WithInvalidPlacement_ShouldThrowException() {
        // When & Then
        EcommerceException exception = assertThrows(EcommerceException.class, () ->
                carouselService.createCarousel(tenant, "Test Carousel", "test-carousel", CarouselType.HERO, "INVALID_PLACEMENT", Platform.BOTH));

        assertEquals("Invalid placement: INVALID_PLACEMENT", exception.getMessage());
    }

    @Test
    void getCarouselByTenantAndId_WithValidId_ShouldReturnCarousel() {
        // Given
        when(carouselRepository.findByTenantIdAndId(tenant.getId(), carousel.getId())).thenReturn(Optional.of(carousel));

        // When
        CarouselEntity result = carouselService.getCarouselByTenantAndId(tenant.getId(), carousel.getId());

        // Then
        assertNotNull(result);
        assertEquals(carousel.getId(), result.getId());
        assertEquals(carousel.getName(), result.getName());
    }

    @Test
    void getCarouselByTenantAndId_WithInvalidId_ShouldThrowException() {
        // Given
        when(carouselRepository.findByTenantIdAndId(tenant.getId(), 999L)).thenReturn(Optional.empty());

        // When & Then
        EcommerceException exception = assertThrows(EcommerceException.class, () ->
                carouselService.getCarouselByTenantAndId(tenant.getId(), 999L));

        assertEquals("Carousel not found", exception.getMessage());
    }

    @Test
    void getCarouselsByTenant_ShouldReturnPagedResults() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        List<CarouselEntity> carousels = Arrays.asList(carousel);
        Page<CarouselEntity> page = new PageImpl<>(carousels, pageable, 1);
        when(carouselRepository.findByTenantId(tenant.getId(), pageable)).thenReturn(page);

        // When
        Page<CarouselEntity> result = carouselService.getCarouselsByTenant(tenant.getId(), pageable);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(carousel.getId(), result.getContent().get(0).getId());
    }

    @Test
    void addCarouselItem_WithValidImageContent_ShouldCreateItem() {
        // Given
        when(carouselRepository.findByTenantIdAndId(tenant.getId(), carousel.getId())).thenReturn(Optional.of(carousel));
        when(carouselItemRepository.getMaxSortOrderByCarouselIdAndTenantId(carousel.getId(), tenant.getId())).thenReturn(0);
        when(carouselItemRepository.save(any(CarouselItemEntity.class))).thenReturn(carouselItem);

        // When
        CarouselItemEntity result = carouselService.addCarouselItem(
                tenant.getId(), carousel.getId(), CarouselContentType.IMAGE,
                "Test Item", "Test Subtitle", "https://example.com/image.jpg",
                null, CallToActionType.NONE, null, null, null);

        // Then
        assertNotNull(result);
        assertEquals(CarouselContentType.IMAGE, result.getContentType());
        assertEquals("Test Item", result.getTitle());
        verify(carouselItemRepository).save(any(CarouselItemEntity.class));
    }

    @Test
    void addCarouselItem_WithProductContent_ShouldValidateProduct() {
        // Given
        ProductEntity product = new ProductEntity(tenant, "Test Product", "test-product");
        // Use reflection to set ID for testing
        try {
            var idField = ProductEntity.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(product, 1L);
        } catch (Exception e) {
            // Ignore for test
        }
        
        when(carouselRepository.findByTenantIdAndId(tenant.getId(), carousel.getId())).thenReturn(Optional.of(carousel));
        when(productRepository.existsByTenantIdAndId(tenant.getId(), 1L)).thenReturn(true);
        when(productRepository.findByTenantIdAndId(tenant.getId(), 1L)).thenReturn(Optional.of(product));
        when(carouselItemRepository.getMaxSortOrderByCarouselIdAndTenantId(carousel.getId(), tenant.getId())).thenReturn(0);
        when(carouselItemRepository.save(any(CarouselItemEntity.class))).thenReturn(carouselItem);

        // When
        CarouselItemEntity result = carouselService.addCarouselItem(
                tenant.getId(), carousel.getId(), CarouselContentType.PRODUCT,
                "Test Product Item", null, null,
                null, CallToActionType.ADD_TO_CART, "Add to Cart", 1L, null);

        // Then
        assertNotNull(result);
        verify(productRepository).findByTenantIdAndId(tenant.getId(), 1L);
        verify(carouselItemRepository).save(any(CarouselItemEntity.class));
    }

    @Test
    void addCarouselItem_WithInvalidContentType_ShouldThrowException() {
        // Given
        when(carouselRepository.findByTenantIdAndId(tenant.getId(), carousel.getId())).thenReturn(Optional.of(carousel));

        // When & Then
        EcommerceException exception = assertThrows(EcommerceException.class, () ->
                carouselService.addCarouselItem(tenant.getId(), carousel.getId(), null,
                        "Test Item", null, null, null, CallToActionType.NONE, null, null, null));

        assertEquals("Content type is required", exception.getMessage());
    }

    @Test
    void addCarouselItem_WithImageContentMissingUrl_ShouldThrowException() {
        // Given
        when(carouselRepository.findByTenantIdAndId(tenant.getId(), carousel.getId())).thenReturn(Optional.of(carousel));
        when(carouselItemRepository.getMaxSortOrderByCarouselIdAndTenantId(carousel.getId(), tenant.getId())).thenReturn(0);

        // When & Then
        EcommerceException exception = assertThrows(EcommerceException.class, () ->
                carouselService.addCarouselItem(tenant.getId(), carousel.getId(), CarouselContentType.IMAGE,
                        "Test Item", null, null, null, CallToActionType.NONE, null, null, null));

        assertEquals("Image URL is required for IMAGE content type", exception.getMessage());
    }

    @Test
    void deleteCarousel_WithValidId_ShouldDeleteCarousel() {
        // Given
        when(carouselRepository.findByTenantIdAndId(tenant.getId(), carousel.getId())).thenReturn(Optional.of(carousel));

        // When
        carouselService.deleteCarousel(tenant.getId(), carousel.getId());

        // Then
        verify(carouselRepository).delete(carousel);
    }

    @Test
    void reorderCarouselItems_WithValidItems_ShouldUpdateSortOrder() {
        // Given
        CarouselItemEntity item1 = new CarouselItemEntity(carousel, tenant, CarouselContentType.IMAGE);
        item1.setId(1L);
        CarouselItemEntity item2 = new CarouselItemEntity(carousel, tenant, CarouselContentType.IMAGE);
        item2.setId(2L);

        when(carouselRepository.findByTenantIdAndId(tenant.getId(), carousel.getId())).thenReturn(Optional.of(carousel));
        when(carouselItemRepository.findByIdAndTenantId(1L, tenant.getId())).thenReturn(Optional.of(item1));
        when(carouselItemRepository.findByIdAndTenantId(2L, tenant.getId())).thenReturn(Optional.of(item2));

        // When
        carouselService.reorderCarouselItems(tenant.getId(), carousel.getId(), Arrays.asList(2L, 1L));

        // Then
        verify(carouselItemRepository, times(2)).save(any(CarouselItemEntity.class));
    }
}