package com.sh.sh.pos.system.service.serviceimpl.productoptionvalueserviceimpl;

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
import com.sh.sh.pos.system.exception.UserException;
import com.sh.sh.pos.system.model.products.ProductOption;
import com.sh.sh.pos.system.model.products.ProductOptionValue;
import com.sh.sh.pos.system.payload.dto.productdto.ProductOptionValueDTO;
import com.sh.sh.pos.system.repository.productoptionrepository.ProductOptionRepository;
import com.sh.sh.pos.system.repository.productoptionvaluerepository.ProductOptionValueRepository;

@ExtendWith(MockitoExtension.class)
@DisplayName("ProductOptionValueServiceImpl — duplicate handling and option moves")
class ProductOptionValueServiceImplTest {

    @Mock private ProductOptionRepository optionRepository;
    @Mock private ProductOptionValueRepository valueRepository;

    @InjectMocks
    private ProductOptionValueServiceImpl valueService;

    private ProductOption sizeOption;
    private ProductOption colorOption;
    private ProductOptionValue value;

    @BeforeEach
    void setUp() {
        sizeOption = new ProductOption();
        sizeOption.setId(10L);
        sizeOption.setName("Size");

        colorOption = new ProductOption();
        colorOption.setId(20L);
        colorOption.setName("Color");

        value = new ProductOptionValue();
        value.setId(1L);
        value.setValue("Medium");
        value.setOption(sizeOption);
    }

    private ProductOptionValueDTO dto(Long optionId, String val) {
        ProductOptionValueDTO dto = new ProductOptionValueDTO();
        dto.setOptionId(optionId);
        dto.setValue(val);
        return dto;
    }

    // ── create ────────────────────────────────────────────────────────────

    @Test
    @DisplayName("create → valid → saved ✅")
    void create_valid_saved() {
        when(optionRepository.findById(10L)).thenReturn(Optional.of(sizeOption));
        when(valueRepository.existsByOptionIdAndValue(10L, "Large")).thenReturn(false);
        when(valueRepository.save(any(ProductOptionValue.class))).thenReturn(value);

        assertThat(valueService.create(dto(10L, "Large"))).isNotNull();
        verify(valueRepository).save(any(ProductOptionValue.class));
    }

    @Test
    @DisplayName("create → duplicate value on same option → UserException ❌")
    void create_duplicate_rejected() {
        when(optionRepository.findById(10L)).thenReturn(Optional.of(sizeOption));
        when(valueRepository.existsByOptionIdAndValue(10L, "Medium")).thenReturn(true);

        assertThatThrownBy(() -> valueService.create(dto(10L, "Medium")))
                .isInstanceOf(UserException.class)
                .hasMessageContaining("already exists");

        verify(valueRepository, never()).save(any());
    }

    @Test
    @DisplayName("create → unknown option → 404 ❌")
    void create_optionNotFound() {
        when(optionRepository.findById(10L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> valueService.create(dto(10L, "Large")))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Option not found");
    }

    // ── update ────────────────────────────────────────────────────────────

    @Test
    @DisplayName("update → rename to a taken value → UserException, not mutated ❌")
    void update_duplicate_rejected() {
        when(valueRepository.findById(1L)).thenReturn(Optional.of(value));
        when(valueRepository.existsByOptionIdAndValue(10L, "Small")).thenReturn(true);

        assertThatThrownBy(() -> valueService.update(1L, dto(10L, "Small")))
                .isInstanceOf(UserException.class)
                .hasMessageContaining("already exists");

        assertThat(value.getValue()).isEqualTo("Medium");
        verify(valueRepository, never()).save(any());
    }

    @Test
    @DisplayName("update → same value different case → duplicate check skipped ✅")
    void update_sameValueCaseInsensitive_allowed() {
        when(valueRepository.findById(1L)).thenReturn(Optional.of(value));
        when(valueRepository.save(any(ProductOptionValue.class))).thenReturn(value);

        valueService.update(1L, dto(10L, "MEDIUM"));

        verify(valueRepository, never()).existsByOptionIdAndValue(any(), any());
        assertThat(value.getValue()).isEqualTo("MEDIUM");
    }

    @Test
    @DisplayName("update → moving value to another option → option swapped ✅")
    void update_moveToAnotherOption() {
        when(valueRepository.findById(1L)).thenReturn(Optional.of(value));
        when(valueRepository.existsByOptionIdAndValue(20L, "Red")).thenReturn(false);
        when(optionRepository.findById(20L)).thenReturn(Optional.of(colorOption));
        when(valueRepository.save(any(ProductOptionValue.class))).thenReturn(value);

        valueService.update(1L, dto(20L, "Red"));

        assertThat(value.getOption()).isEqualTo(colorOption);
        assertThat(value.getValue()).isEqualTo("Red");
    }

    @Test
    @DisplayName("update → target option doesn't exist → 404 ❌")
    void update_targetOptionNotFound() {
        when(valueRepository.findById(1L)).thenReturn(Optional.of(value));
        when(valueRepository.existsByOptionIdAndValue(20L, "Red")).thenReturn(false);
        when(optionRepository.findById(20L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> valueService.update(1L, dto(20L, "Red")))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Option not found");
    }

    @Test
    @DisplayName("update → unknown value id → 404 ❌")
    void update_notFound() {
        when(valueRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> valueService.update(99L, dto(10L, "X")))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Option value not found");
    }

    // ── delete / find ─────────────────────────────────────────────────────

    @Test
    @DisplayName("delete → deletes the loaded entity ✅")
    void delete_found() {
        when(valueRepository.findById(1L)).thenReturn(Optional.of(value));

        valueService.delete(1L);

        verify(valueRepository).delete(value);
    }

    @Test
    @DisplayName("findByOption → maps all to DTOs ✅")
    void findByOption_mapsAll() {
        when(valueRepository.findByOptionId(10L)).thenReturn(List.of(value));

        assertThat(valueService.findByOption(10L)).hasSize(1);
    }
}