package com.sh.sh.pos.system.service.serviceimpl.productbatchserviceimpl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
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
import com.sh.sh.pos.system.model.branch.Branch;
import com.sh.sh.pos.system.model.products.Product;
import com.sh.sh.pos.system.model.products.ProductBatch;
import com.sh.sh.pos.system.payload.dto.productdto.ProductBatchDTO;
import com.sh.sh.pos.system.repository.branchrepository.BranchRepository;
import com.sh.sh.pos.system.repository.productbatchrepository.ProductBatchRepository;
import com.sh.sh.pos.system.repository.productrepository.ProductRepository;
import com.sh.sh.pos.system.repository.productvariantrepository.ProductVariantRepository;
import com.sh.sh.pos.system.repository.suppliersrepository.SupplierRepository;
import com.sh.sh.pos.system.service.serviceimpl.productbatchserviceimpl.ProductBatchServiceImpl;

@ExtendWith(MockitoExtension.class)
@DisplayName("ProductBatchServiceImpl — quantity corrections and lookups")
class ProductBatchServiceImplTest {

    @Mock private ProductBatchRepository batchRepository;
    @Mock private ProductRepository productRepository;
    @Mock private BranchRepository branchRepository;
    @Mock private SupplierRepository supplierRepository;
    @Mock private ProductVariantRepository variantRepository;

    @InjectMocks
    private ProductBatchServiceImpl batchService;

    private ProductBatch batch;
    private Product product;
    private Branch branch;

    @BeforeEach
    void setUp() {
        product = new Product();
        product.setId(5L);
        product.setName("Coca-Cola 330ml");

        branch = new Branch();
        branch.setId(7L);
        branch.setName("Main Branch");

        // 100 received, 40 already sold → 60 remaining
        batch = new ProductBatch();
        batch.setId(1L);
        batch.setBatchNo("B-001");
        batch.setQuantity(100);
        batch.setRemainingQuantity(60);
        batch.setPurchasePrice(new BigDecimal("2.50"));
        batch.setExpiryDate(LocalDate.now().plusMonths(6));
        batch.setProduct(product);
        batch.setBranch(branch);
    }

    private ProductBatchDTO updateDto(Integer quantity) {
        ProductBatchDTO dto = new ProductBatchDTO();
        dto.setBatchNo("B-001");
        dto.setPurchasePrice(new BigDecimal("2.50"));
        dto.setExpiryDate(batch.getExpiryDate());
        dto.setQuantity(quantity);
        return dto;
    }

    // ── Quantity correction logic ─────────────────────────────────────────

    @Test
    @DisplayName("update → quantity raised 100→120 → remaining shifts 60→80 ✅")
    void update_quantityIncrease_shiftsRemaining() {
        when(batchRepository.findById(1L)).thenReturn(Optional.of(batch));
        when(batchRepository.save(any(ProductBatch.class))).thenReturn(batch);

        batchService.update(1L, updateDto(120));

        assertThat(batch.getQuantity()).isEqualTo(120);
        assertThat(batch.getRemainingQuantity()).isEqualTo(80);
    }

    @Test
    @DisplayName("update → quantity lowered 100→90 → remaining shifts 60→50 ✅")
    void update_quantityDecrease_shiftsRemaining() {
        when(batchRepository.findById(1L)).thenReturn(Optional.of(batch));
        when(batchRepository.save(any(ProductBatch.class))).thenReturn(batch);

        batchService.update(1L, updateDto(90));

        assertThat(batch.getQuantity()).isEqualTo(90);
        assertThat(batch.getRemainingQuantity()).isEqualTo(50);
    }

    @Test
    @DisplayName("update → correction bigger than remaining → floors at 0, never negative ✅")
    void update_quantityDecrease_floorsAtZero() {
        when(batchRepository.findById(1L)).thenReturn(Optional.of(batch));
        when(batchRepository.save(any(ProductBatch.class))).thenReturn(batch);

        batchService.update(1L, updateDto(30)); // delta -70, remaining was 60

        assertThat(batch.getQuantity()).isEqualTo(30);
        assertThat(batch.getRemainingQuantity()).isZero();
    }

    @Test
    @DisplayName("update → same quantity → remaining untouched ✅")
    void update_sameQuantity_noChange() {
        when(batchRepository.findById(1L)).thenReturn(Optional.of(batch));
        when(batchRepository.save(any(ProductBatch.class))).thenReturn(batch);

        batchService.update(1L, updateDto(100));

        assertThat(batch.getQuantity()).isEqualTo(100);
        assertThat(batch.getRemainingQuantity()).isEqualTo(60);
    }

    @Test
    @DisplayName("update → null quantity → remaining untouched ✅")
    void update_nullQuantity_noChange() {
        when(batchRepository.findById(1L)).thenReturn(Optional.of(batch));
        when(batchRepository.save(any(ProductBatch.class))).thenReturn(batch);

        batchService.update(1L, updateDto(null));

        assertThat(batch.getQuantity()).isEqualTo(100);
        assertThat(batch.getRemainingQuantity()).isEqualTo(60);
    }

    // ── Create ────────────────────────────────────────────────────────────

    @Test
    @DisplayName("create → valid → saved ✅")
    void create_valid_saved() {
        ProductBatchDTO dto = updateDto(100);
        dto.setProductId(5L);
        dto.setBranchId(7L);

        when(productRepository.findById(5L)).thenReturn(Optional.of(product));
        when(branchRepository.findById(7L)).thenReturn(Optional.of(branch));
        when(batchRepository.save(any(ProductBatch.class))).thenReturn(batch);

        assertThat(batchService.create(dto)).isNotNull();
        verify(batchRepository).save(any(ProductBatch.class));
    }

    @Test
    @DisplayName("create → unknown product id → 404, nothing saved ❌")
    void create_productNotFound() {
        ProductBatchDTO dto = updateDto(100);
        dto.setProductId(5L);
        dto.setBranchId(7L);

        when(productRepository.findById(5L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> batchService.create(dto))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Product not found");

        verify(batchRepository, never()).save(any());
    }

    @Test
    @DisplayName("create → unknown branch id → 404, nothing saved ❌")
    void create_branchNotFound() {
        ProductBatchDTO dto = updateDto(100);
        dto.setProductId(5L);
        dto.setBranchId(7L);

        when(productRepository.findById(5L)).thenReturn(Optional.of(product));
        when(branchRepository.findById(7L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> batchService.create(dto))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Branch not found");

        verify(batchRepository, never()).save(any());
    }

    // ── Lookups & delete ──────────────────────────────────────────────────

    @Test
    @DisplayName("get → found → DTO returned ✅")
    void get_found() {
        when(batchRepository.findById(1L)).thenReturn(Optional.of(batch));

        assertThat(batchService.get(1L)).isNotNull();
    }

    @Test
    @DisplayName("get → unknown id → 404 ❌")
    void get_notFound() {
        when(batchRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> batchService.get(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Batch not found");
    }

    @Test
    @DisplayName("getExpiringBatches → delegates with correct date window ✅")
    void getExpiringBatches_windowed() {
        when(batchRepository.findByExpiryDateBetween(any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(List.of(batch));

        assertThat(batchService.getExpiringBatches(30)).hasSize(1);
    }

    @Test
    @DisplayName("delete → deletes the loaded entity ✅")
    void delete_found() {
        when(batchRepository.findById(1L)).thenReturn(Optional.of(batch));

        batchService.delete(1L);

        verify(batchRepository).delete(batch);
    }
}