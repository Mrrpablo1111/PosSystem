package com.sh.sh.pos.system.service.serviceimpl.productvariantserviceimpl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.sh.sh.pos.system.exception.ResourceNotFoundException;
import com.sh.sh.pos.system.exception.UserException;
import com.sh.sh.pos.system.model.products.Product;
import com.sh.sh.pos.system.model.products.ProductVariant;
import com.sh.sh.pos.system.payload.dto.productdto.ProductVariantDTO;
import com.sh.sh.pos.system.repository.productoptionvaluerepository.ProductOptionValueRepository;
import com.sh.sh.pos.system.repository.productrepository.ProductRepository;
import com.sh.sh.pos.system.repository.productvariantrepository.ProductVariantRepository;
import com.sh.sh.pos.system.repository.productvariantvaluerepository.ProductVariantValueRepository;

@ExtendWith(MockitoExtension.class)
@DisplayName("ProductVariantServiceImpl — uniqueness validation and CRUD")
class ProductVariantServiceImplTest {

    @Mock private ProductRepository productRepository;
    @Mock private ProductVariantRepository variantRepository;
    @Mock private ProductOptionValueRepository optionValueRepository;
    @Mock private ProductVariantValueRepository variantValueRepository;

    @InjectMocks
    private ProductVariantServiceImpl variantService;

    private Product product;
    private ProductVariant variant;

    @BeforeEach
    void setUp() {
        product = new Product();
        product.setId(5L);
        product.setName("T-Shirt");

        variant = new ProductVariant();
        variant.setId(1L);
        variant.setName("T-Shirt / Red / M");
        variant.setSku("TS-RED-M");
        variant.setBarcode("885000111222");
        variant.setCostPrice(new BigDecimal("3.00"));
        variant.setSellingPrice(new BigDecimal("6.50"));
        variant.setActive(true);
        variant.setProduct(product);
        variant.setValues(new ArrayList<>());
    }

    private ProductVariantDTO dto(String sku, String barcode) {
        ProductVariantDTO dto = new ProductVariantDTO();
        dto.setProductId(5L);
        dto.setName("T-Shirt / Red / M");
        dto.setSku(sku);
        dto.setBarcode(barcode);
        dto.setCostPrice(new BigDecimal("3.00"));
        dto.setSellingPrice(new BigDecimal("6.50"));
        dto.setActive(true);
        return dto;
    }

    // ── create: uniqueness ────────────────────────────────────────────────

    @Test
    @DisplayName("create → duplicate SKU → UserException, nothing saved ❌")
    void create_duplicateSku_rejected() {
        when(variantRepository.existsBySku("TS-RED-M")).thenReturn(true);

        assertThatThrownBy(() -> variantService.create(dto("TS-RED-M", "885000111222")))
                .isInstanceOf(UserException.class)
                .hasMessageContaining("SKU already exists");

        verify(variantRepository, never()).save(any());
    }

    @Test
    @DisplayName("create → duplicate barcode → UserException ❌")
    void create_duplicateBarcode_rejected() {
        when(variantRepository.existsBySku("TS-NEW")).thenReturn(false);
        when(variantRepository.existsByBarcode("885000111222")).thenReturn(true);

        assertThatThrownBy(() -> variantService.create(dto("TS-NEW", "885000111222")))
                .isInstanceOf(UserException.class)
                .hasMessageContaining("Barcode already exists");

        verify(variantRepository, never()).save(any());
    }

    @Test
    @DisplayName("create → null barcode → barcode check skipped, saved ✅")
    void create_nullBarcode_allowed() {
        when(variantRepository.existsBySku("TS-NEW")).thenReturn(false);
        when(productRepository.findById(5L)).thenReturn(Optional.of(product));
        when(variantRepository.save(any(ProductVariant.class))).thenReturn(variant);

        assertThat(variantService.create(dto("TS-NEW", null))).isNotNull();

        verify(variantRepository, never()).existsByBarcode(any());
    }

    @Test
    @DisplayName("create → unknown product → 404 ❌")
    void create_productNotFound() {
        when(variantRepository.existsBySku("TS-NEW")).thenReturn(false);
        when(variantRepository.existsByBarcode("885000111222")).thenReturn(false);
        when(productRepository.findById(5L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> variantService.create(dto("TS-NEW", "885000111222")))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Product not found");
    }

    // ── update: THE regression tests for the broken validation order ─────

    @Test
    @DisplayName("update → renaming SKU onto another variant's SKU → rejected ❌ (was broken)")
    void update_skuConflict_rejected() {
        when(variantRepository.findById(1L)).thenReturn(Optional.of(variant));
        // current SKU is TS-RED-M; DTO asks for TS-RED-L which another variant owns
        when(variantRepository.existsBySku("TS-RED-L")).thenReturn(true);

        assertThatThrownBy(() -> variantService.update(1L, dto("TS-RED-L", "885000111222")))
                .isInstanceOf(UserException.class)
                .hasMessageContaining("SKU already exists");

        // the variant must NOT have been mutated before validation
        assertThat(variant.getSku()).isEqualTo("TS-RED-M");
        verify(variantRepository, never()).save(any());
    }

    @Test
    @DisplayName("update → keeping own SKU → no conflict, saved ✅")
    void update_sameSku_allowed() {
        when(variantRepository.findById(1L)).thenReturn(Optional.of(variant));
        when(variantRepository.save(any(ProductVariant.class))).thenReturn(variant);

        variantService.update(1L, dto("TS-RED-M", "885000111222"));

        // unchanged SKU → uniqueness lookup skipped entirely
        verify(variantRepository, never()).existsBySku(any());
        verify(variantRepository).save(variant);
    }

    @Test
    @DisplayName("update → barcode changed to a taken one → rejected ❌ (was broken)")
    void update_barcodeConflict_rejected() {
        when(variantRepository.findById(1L)).thenReturn(Optional.of(variant));
        when(variantRepository.existsByBarcode("885000999999")).thenReturn(true);

        assertThatThrownBy(() -> variantService.update(1L, dto("TS-RED-M", "885000999999")))
                .isInstanceOf(UserException.class)
                .hasMessageContaining("Barcode already exists");

        assertThat(variant.getBarcode()).isEqualTo("885000111222");
        verify(variantRepository, never()).save(any());
    }

    @Test
    @DisplayName("update → valid changes → fields applied, saved once ✅")
    void update_valid_applied() {
        when(variantRepository.findById(1L)).thenReturn(Optional.of(variant));
        when(variantRepository.save(any(ProductVariant.class))).thenReturn(variant);

        ProductVariantDTO changes = dto("TS-RED-M", "885000111222");
        changes.setSellingPrice(new BigDecimal("7.00"));

        variantService.update(1L, changes);

        assertThat(variant.getSellingPrice()).isEqualByComparingTo("7.00");
        verify(variantRepository).save(variant);
    }

    @Test
    @DisplayName("update → unknown id → 404 ❌")
    void update_notFound() {
        when(variantRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> variantService.update(99L, dto("X", null)))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Variant not found");
    }

    // ── get / delete ──────────────────────────────────────────────────────

    @Test
    @DisplayName("get → unknown id → 404 ❌")
    void get_notFound() {
        when(variantRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> variantService.get(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("delete → removes variant values then the variant ✅")
    void delete_cleansUpValues() {
        when(variantRepository.findById(1L)).thenReturn(Optional.of(variant));

        variantService.delete(1L);

        verify(variantValueRepository).deleteByVariantId(1L);
        verify(variantRepository).delete(variant);
    }

    @Test
    @DisplayName("getByProduct → unknown product → 404 ❌")
    void getByProduct_productNotFound() {
        when(productRepository.existsById(5L)).thenReturn(false);

        assertThatThrownBy(() -> variantService.getByProduct(5L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Product not found");
    }
}