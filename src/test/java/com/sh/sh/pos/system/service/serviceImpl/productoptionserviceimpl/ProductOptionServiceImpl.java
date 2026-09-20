package com.sh.sh.pos.system.service.serviceimpl.productoptionserviceimpl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.sh.sh.pos.system.exception.ResourceNotFoundException;
import com.sh.sh.pos.system.model.products.Product;
import com.sh.sh.pos.system.model.products.ProductOption;
import com.sh.sh.pos.system.payload.dto.productdto.ProductOptionDTO;
import com.sh.sh.pos.system.repository.productoptionrepository.ProductOptionRepository;
import com.sh.sh.pos.system.repository.productrepository.ProductRepository;

@ExtendWith(MockitoExtension.class)
@DisplayName("ProductOptionServiceImpl — CRUD and lookups")
class ProductOptionServiceImplTest {

    @Mock private ProductOptionRepository optionRepository;
    @Mock private ProductRepository productRepository;

    @InjectMocks
    private ProductOptionServiceImpl optionService;

    private Product product;
    private ProductOption option;

    @BeforeEach
    void setUp() {
        product = new Product();
        product.setId(5L);
        product.setName("T-Shirt");

        option = newOption(1L, "Size");
    }

    private ProductOption newOption(Long id, String name) {
        ProductOption o = new ProductOption();
        o.setId(id);
        o.setName(name);
        o.setProduct(product);
        o.setValues(List.of());
        return o;
    }

    private ProductOptionDTO dto(String name) {
        ProductOptionDTO dto = new ProductOptionDTO();
        dto.setProductId(5L);
        dto.setName(name);
        return dto;
    }

    // ── create ────────────────────────────────────────────────────────────

    @Test
    @DisplayName("create → valid product → option saved ✅")
    void create_valid_saved() {
        when(productRepository.findById(5L)).thenReturn(Optional.of(product));
        when(optionRepository.save(any(ProductOption.class))).thenReturn(option);

        ProductOptionDTO result = optionService.create(dto("Size"));

        assertThat(result).isNotNull();
        verify(optionRepository).save(any(ProductOption.class));
    }

    @Test
    @DisplayName("create → unknown product id → 404, nothing saved ❌")
    void create_productNotFound() {
        when(productRepository.findById(5L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> optionService.create(dto("Size")))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Product not found");

        verify(optionRepository, never()).save(any());
    }

    // ── update ────────────────────────────────────────────────────────────

    @Test
    @DisplayName("update → renames the option ✅")
    void update_renames() {
        when(optionRepository.findById(1L)).thenReturn(Optional.of(option));
        when(optionRepository.save(any(ProductOption.class))).thenReturn(option);

        optionService.update(1L, dto("Color"));

        assertThat(option.getName()).isEqualTo("Color");
        verify(optionRepository).save(option);
    }

    @Test
    @DisplayName("update → unknown id → 404 ❌")
    void update_notFound() {
        when(optionRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> optionService.update(99L, dto("Color")))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Option not found");

        verify(optionRepository, never()).save(any());
    }

    // ── delete ────────────────────────────────────────────────────────────

    @Test
    @DisplayName("delete → deletes the loaded entity ✅")
    void delete_found() {
        when(optionRepository.findById(1L)).thenReturn(Optional.of(option));

        optionService.delete(1L);

        verify(optionRepository).delete(option);
    }

    @Test
    @DisplayName("delete → unknown id → 404, nothing deleted ❌")
    void delete_notFound() {
        when(optionRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> optionService.delete(99L))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(optionRepository, never()).delete(any());
    }

    // ── findByProduct ─────────────────────────────────────────────────────

    @Test
    @DisplayName("findByProduct → maps all options to DTOs ✅")
    void findByProduct_mapsAll() {
        ProductOption second = newOption(2L, "Color");

        when(optionRepository.findByProductId(5L)).thenReturn(List.of(option, second));

        List<ProductOptionDTO> result = optionService.findByProduct(5L);

        assertThat(result).hasSize(2);
    }

    @Test
    @DisplayName("findByProduct → no options → empty list, not null ✅")
    void findByProduct_empty() {
        when(optionRepository.findByProductId(5L)).thenReturn(List.of());

        assertThat(optionService.findByProduct(5L)).isEmpty();
    }
}