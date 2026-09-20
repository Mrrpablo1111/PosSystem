package com.sh.sh.pos.system.service.serviceimpl.productServiceImpl;


import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import com.sh.sh.pos.system.domain.UserRole;
import com.sh.sh.pos.system.exception.ResourceNotFoundException;
import com.sh.sh.pos.system.exception.UserException;
import com.sh.sh.pos.system.model.category.Category;
import com.sh.sh.pos.system.model.products.Product;
import com.sh.sh.pos.system.model.store.Store;
import com.sh.sh.pos.system.model.user.User;
import com.sh.sh.pos.system.payload.dto.productdto.ProductDTO;
import com.sh.sh.pos.system.repository.categoryrepository.CategoryRepository;
import com.sh.sh.pos.system.repository.productrepository.ProductRepository;
import com.sh.sh.pos.system.repository.storerepository.StoreRepository;
import com.sh.sh.pos.system.repository.suppliersrepository.SupplierRepository;
import com.sh.sh.pos.system.service.serviceimpl.productserviceimpl.ProductServiceImpl;

@ExtendWith(MockitoExtension.class)
@DisplayName("ProductServiceImpl — uniqueness, lookups, permission checks")
class ProductServiceImplTest {

    @Mock private ProductRepository  productRepository;
    @Mock private StoreRepository    storeRepository;
    @Mock private SupplierRepository supplierRepository;
    @Mock private CategoryRepository categoryRepository;

    @InjectMocks
    private ProductServiceImpl productService;

    private User     storeAdmin;
    private User     otherAdmin;
    private User     cashier;
    private Store    store;
    private Category category;
    private Product  product;

    @BeforeEach
    void setUp() {
        storeAdmin = new User();
        storeAdmin.setId(1L);
        storeAdmin.setRole(UserRole.ROLE_STORE_ADMIN);

        otherAdmin = new User();
        otherAdmin.setId(3L);
        otherAdmin.setRole(UserRole.ROLE_STORE_ADMIN);

        cashier = new User();
        cashier.setId(4L);
        cashier.setRole(UserRole.ROLE_BRANCH_CASHIER);

        store = Store.builder()
                .id(10L)
                .brand("Test Store")
                .storeAdmin(storeAdmin)
                .build();
        storeAdmin.setStore(store);

        category = Category.builder()
                .id(20L)
                .name("Beverages")
                .store(store)
                .build();

        product = Product.builder()
                .id(100L)
                .name("Coca-Cola 330ml")
                .sku("CC-330")
                .store(store)
                .category(category)
                .sellingPrice(new BigDecimal("0.75"))
                .build();
    }

    private ProductDTO sampleDto() {
        return ProductDTO.builder()
                .storeId(10L)
                .categoryId(20L)
                .name("Coca-Cola 330ml")
                .sku("CC-330")
                .barcode("885000123456")
                .sellingPrice(new BigDecimal("0.75"))
                .costPrice(new BigDecimal("0.45"))
                .unit("PIECE")
                .active(true)
                .build();
    }

    // ── createProduct ─────────────────────────────────────────────────────

    @Test
    @DisplayName("createProduct → STORE_ADMIN same store → saved ✅")
    void createProduct_storeAdmin_allowed() {
        when(productRepository.existsBySku("CC-330")).thenReturn(false);
        when(productRepository.findByBarcode("885000123456")).thenReturn(Optional.empty());
        when(storeRepository.findById(10L)).thenReturn(Optional.of(store));
        when(categoryRepository.findById(20L)).thenReturn(Optional.of(category));
        when(productRepository.save(any(Product.class))).thenReturn(product);

        ProductDTO result = productService.createProduct(sampleDto(), storeAdmin);

        assertThat(result).isNotNull();
        verify(productRepository).save(any(Product.class));
    }

    @Test
    @DisplayName("createProduct → duplicate SKU → UserException, nothing saved ❌")
    void createProduct_duplicateSku_rejected() {
        when(productRepository.existsBySku("CC-330")).thenReturn(true);

        assertThatThrownBy(() -> productService.createProduct(sampleDto(), storeAdmin))
                .isInstanceOf(UserException.class)
                .hasMessageContaining("SKU already exists");

        verify(productRepository, never()).save(any());
    }

    @Test
    @DisplayName("createProduct → duplicate barcode → UserException ❌")
    void createProduct_duplicateBarcode_rejected() {
        when(productRepository.existsBySku("CC-330")).thenReturn(false);
        when(productRepository.findByBarcode("885000123456"))
                .thenReturn(Optional.of(product));

        assertThatThrownBy(() -> productService.createProduct(sampleDto(), storeAdmin))
                .isInstanceOf(UserException.class)
                .hasMessageContaining("Barcode already exists");

        verify(productRepository, never()).save(any());
    }

    @Test
    @DisplayName("createProduct → cashier → AccessDenied, nothing saved ❌")
    void createProduct_cashier_denied() {
        when(productRepository.existsBySku("CC-330")).thenReturn(false);
        when(productRepository.findByBarcode("885000123456")).thenReturn(Optional.empty());
        when(storeRepository.findById(10L)).thenReturn(Optional.of(store));

        assertThatThrownBy(() -> productService.createProduct(sampleDto(), cashier))
                .isInstanceOf(AccessDeniedException.class);

        verify(productRepository, never()).save(any());
    }

    @Test
    @DisplayName("createProduct → admin of different store → AccessDenied ❌")
    void createProduct_otherAdmin_denied() {
        Store otherStore = Store.builder().id(99L).brand("Other").storeAdmin(otherAdmin).build();
        otherAdmin.setStore(otherStore);

        when(productRepository.existsBySku("CC-330")).thenReturn(false);
        when(productRepository.findByBarcode("885000123456")).thenReturn(Optional.empty());
        when(storeRepository.findById(10L)).thenReturn(Optional.of(store));

        assertThatThrownBy(() -> productService.createProduct(sampleDto(), otherAdmin))
                .isInstanceOf(AccessDeniedException.class);

        verify(productRepository, never()).save(any());
    }

    @Test
    @DisplayName("createProduct → unknown store id → 404 ❌")
    void createProduct_storeNotFound() {
        when(productRepository.existsBySku("CC-330")).thenReturn(false);
        when(productRepository.findByBarcode("885000123456")).thenReturn(Optional.empty());
        when(storeRepository.findById(10L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productService.createProduct(sampleDto(), storeAdmin))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Store not found");
    }

    // ── updateProduct ─────────────────────────────────────────────────────

    @Test
    @DisplayName("updateProduct → same-store admin → fields updated ✅")
    void updateProduct_storeAdmin_allowed() {
        when(productRepository.findById(100L)).thenReturn(Optional.of(product));
        when(productRepository.existsBySkuAndIdNot("CC-330", 100L)).thenReturn(false);
        when(productRepository.existsByBarcodeAndIdNot("885000123456", 100L)).thenReturn(false);
        when(categoryRepository.findById(20L)).thenReturn(Optional.of(category));
        when(productRepository.save(any(Product.class))).thenReturn(product);

        ProductDTO dto = sampleDto();
        dto.setName("Coca-Cola Can 330ml");

        ProductDTO result = productService.updateProduct(100L, dto, storeAdmin);

        assertThat(result).isNotNull();
        assertThat(product.getName()).isEqualTo("Coca-Cola Can 330ml");
    }

    @Test
    @DisplayName("updateProduct → SKU taken by another product → UserException ❌")
    void updateProduct_skuConflict_rejected() {
        when(productRepository.findById(100L)).thenReturn(Optional.of(product));
        when(productRepository.existsBySkuAndIdNot("CC-330", 100L)).thenReturn(true);

        assertThatThrownBy(() -> productService.updateProduct(100L, sampleDto(), storeAdmin))
                .isInstanceOf(UserException.class)
                .hasMessageContaining("SKU already exists");

        verify(productRepository, never()).save(any());
    }

    @Test
    @DisplayName("updateProduct → cashier → AccessDenied ❌")
    void updateProduct_cashier_denied() {
        when(productRepository.findById(100L)).thenReturn(Optional.of(product));

        assertThatThrownBy(() -> productService.updateProduct(100L, sampleDto(), cashier))
                .isInstanceOf(AccessDeniedException.class);

        verify(productRepository, never()).save(any());
    }

    @Test
    @DisplayName("updateProduct → unknown product id → 404 ❌")
    void updateProduct_notFound() {
        when(productRepository.findById(100L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productService.updateProduct(100L, sampleDto(), storeAdmin))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Product not found");
    }

    // ── deleteProduct ─────────────────────────────────────────────────────

    @Test
    @DisplayName("deleteProduct → same-store admin → deleted ✅")
    void deleteProduct_storeAdmin_allowed() {
        when(productRepository.findById(100L)).thenReturn(Optional.of(product));

        productService.deleteProduct(100L, storeAdmin);

        verify(productRepository).delete(product);
    }

    @Test
    @DisplayName("deleteProduct → cashier → AccessDenied, NOT deleted ❌")
    void deleteProduct_cashier_denied() {
        when(productRepository.findById(100L)).thenReturn(Optional.of(product));

        assertThatThrownBy(() -> productService.deleteProduct(100L, cashier))
                .isInstanceOf(AccessDeniedException.class);

        verify(productRepository, never()).delete(any());
    }

    // ── lookups ───────────────────────────────────────────────────────────

    @Test
    @DisplayName("getProductById → found → DTO returned ✅")
    void getProductById_found() {
        when(productRepository.findById(100L)).thenReturn(Optional.of(product));

        assertThat(productService.getProductById(100L)).isNotNull();
    }

    @Test
    @DisplayName("getProductByBarcode → unknown barcode → 404 ❌")
    void getProductByBarcode_notFound() {
        when(productRepository.findByBarcode("000")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productService.getProductByBarcode("000"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("barcode");
    }
}