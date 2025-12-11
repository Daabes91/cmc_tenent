import type { Carousel, CarouselItem, Page, Product, ProductImage, ProductPage, Category } from "~/types/ecommerce";

export function useEcommerceService() {
  const api = useAdminApi();

  const listCarousels = (tenantId: number, params: Record<string, any> = {}) => {
    const search = new URLSearchParams();
    Object.entries(params).forEach(([key, value]) => {
      if (value !== undefined && value !== null && value !== "") {
        search.set(key, String(value));
      }
    });
    const qs = search.toString();
    return api.request<Page<Carousel>>(
      `/admin/tenants/${tenantId}/carousels${qs ? `?${qs}` : ""}`
    );
  };

  const getCarousel = (tenantId: number, carouselId: number) =>
    api.request<Carousel>(`/admin/tenants/${tenantId}/carousels/${carouselId}`);

  const listProducts = (tenantId: number, params: Record<string, any> = {}) => {
    const search = new URLSearchParams();
    Object.entries(params).forEach(([key, value]) => {
      if (value !== undefined && value !== null && value !== "") {
        search.set(key, String(value));
      }
    });
    const qs = search.toString();
    return api.request<ProductPage>(`/admin/tenants/${tenantId}/products${qs ? `?${qs}` : ""}`);
  };

  const getProduct = (tenantId: number, productId: number) =>
    api.request<Product>(`/admin/tenants/${tenantId}/products/${productId}`);

  const createProduct = (tenantId: number, payload: Partial<Product> & { images?: string[] }) =>
    api.request<Product>(`/admin/tenants/${tenantId}/products`, {
      method: "POST",
      body: payload,
    });

  const updateProduct = (tenantId: number, productId: number, payload: Partial<Product>) =>
    api.request<Product>(`/admin/tenants/${tenantId}/products/${productId}`, {
      method: "PUT",
      body: payload,
    });

  const createCarousel = (tenantId: number, payload: Partial<Carousel>) =>
    api.request<Carousel>(`/admin/tenants/${tenantId}/carousels`, {
      method: "POST",
      body: payload,
    });

  const updateCarousel = (tenantId: number, carouselId: number, payload: Partial<Carousel>) =>
    api.request<Carousel>(`/admin/tenants/${tenantId}/carousels/${carouselId}`, {
      method: "PUT",
      body: payload,
    });

  const addCarouselItem = (tenantId: number, carouselId: number, payload: Partial<CarouselItem>) =>
    api.request<CarouselItem>(`/admin/tenants/${tenantId}/carousels/${carouselId}/items`, {
      method: "POST",
      body: payload,
    });

  const deleteCarousel = (tenantId: number, carouselId: number) =>
    api.request(`/admin/tenants/${tenantId}/carousels/${carouselId}`, {
      method: "DELETE",
    });

  const listCarouselItems = (tenantId: number, carouselId: number, params: Record<string, any> = {}) => {
    const search = new URLSearchParams();
    Object.entries(params).forEach(([key, value]) => {
      if (value !== undefined && value !== null && value !== "") {
        search.set(key, String(value));
      }
    });
    const qs = search.toString();
    return api.request<Page<CarouselItem>>(
      `/admin/tenants/${tenantId}/carousels/${carouselId}/items${qs ? `?${qs}` : ""}`
    );
  };

  const updateCarouselItem = (tenantId: number, carouselId: number, itemId: number, payload: Partial<CarouselItem>) =>
    api.request<CarouselItem>(`/admin/tenants/${tenantId}/carousels/${carouselId}/items/${itemId}`, {
      method: "PUT",
      body: payload,
    });

  const deleteCarouselItem = (tenantId: number, carouselId: number, itemId: number) =>
    api.request(`/admin/tenants/${tenantId}/carousels/${carouselId}/items/${itemId}`, {
      method: "DELETE",
    });

  const listCategories = (tenantId: number, params: Record<string, any> = {}) => {
    const search = new URLSearchParams({ size: "200" });
    Object.entries(params).forEach(([key, value]) => {
      if (value !== undefined && value !== null && value !== "") {
        search.set(key, String(value));
      }
    });
    const qs = search.toString();
    return api.request<Page<Category>>(`/admin/tenants/${tenantId}/categories${qs ? `?${qs}` : ""}`);
  };

  const listProductImages = (tenantId: number, productId: number) =>
    api.request<ProductImage[]>(`/admin/tenants/${tenantId}/products/${productId}/images`);

  const addProductImage = (
    tenantId: number,
    productId: number,
    payload: Partial<ProductImage>
  ) =>
    api.request<ProductImage>(`/admin/tenants/${tenantId}/products/${productId}/images`, {
      method: "POST",
      body: payload,
    });

  const updateProductImage = (
    tenantId: number,
    productId: number,
    imageId: number,
    payload: Partial<ProductImage>
  ) =>
    api.request<ProductImage>(`/admin/tenants/${tenantId}/products/${productId}/images/${imageId}`, {
      method: "PUT",
      body: payload,
    });

  const deleteProductImage = (tenantId: number, productId: number, imageId: number) =>
    api.request(`/admin/tenants/${tenantId}/products/${productId}/images/${imageId}`, {
      method: "DELETE",
    });

  const setMainProductImage = (tenantId: number, productId: number, imageId: number) =>
    api.request<ProductImage>(`/admin/tenants/${tenantId}/products/${productId}/images/${imageId}/set-main`, {
      method: "PUT",
    });

  return {
    listCarousels,
    getCarousel,
    listProducts,
    getProduct,
    createProduct,
    updateProduct,
    createCarousel,
    updateCarousel,
    deleteCarousel,
    addCarouselItem,
    listCarouselItems,
    updateCarouselItem,
    deleteCarouselItem,
    listCategories,
    listProductImages,
    addProductImage,
    updateProductImage,
    deleteProductImage,
    setMainProductImage,
  };
}
