package com.sh.sh.pos.system.service.serviceimpl.inventoryserviceimpl;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sh.sh.pos.system.exception.inventoryexception.InventoryNotFoundException;
import com.sh.sh.pos.system.mapper.inventorytransactionmapper.InventoryTransactionMapper;
import com.sh.sh.pos.system.model.inventory.Inventory;
import com.sh.sh.pos.system.model.inventorytransaction.InventoryTransaction;
import com.sh.sh.pos.system.model.user.User;
import com.sh.sh.pos.system.payload.dto.inventorytransactiondto.InventoryTransactionDTO;
import com.sh.sh.pos.system.repository.inventorytransactionrepository.InventoryTransactionRepository;
import com.sh.sh.pos.system.service.inventoryservice.InventoryTransactionService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class InventoryTransactionServiceImpl implements InventoryTransactionService {

    private final InventoryTransactionRepository transactionRepository;

    // ═══════════════════════════════════════════════════════════════════
    // Write — internal callers only (GoodsReceiptService, etc.)
    // ═══════════════════════════════════════════════════════════════════

    @Override
    @Transactional
    public InventoryTransaction recordTransaction(
            Inventory inventory, int beforeQuantity, int afterQuantity,
            String referenceType, Long referenceId, User performedBy, String reason) {

        InventoryTransaction transaction = InventoryTransaction.builder()
                .product(inventory.getProduct())
                .productVariant(inventory.getProductVariant())
                .warehouse(inventory.getWarehouse())
                .quantity(afterQuantity - beforeQuantity)
                .referenceType(referenceType)
                .referenceId(referenceId)
                .performedBy(performedBy)
                .note(reason)
                .build();

        return transactionRepository.save(transaction);
    }

    // ═══════════════════════════════════════════════════════════════════
    // Read — InventoryTransactionController
    // ═══════════════════════════════════════════════════════════════════

    @Override
    @Transactional(readOnly = true)
    public List<InventoryTransactionDTO> getAll(int page, int size) {
        return InventoryTransactionMapper.toDTOList(
                transactionRepository.findAllByOrderByCreatedAtDesc(PageRequest.of(page, size)).getContent());
    }

    @Override
    @Transactional(readOnly = true)
    public InventoryTransactionDTO getById(Long id) {
        return InventoryTransactionMapper.toDTO(
                transactionRepository.findById(id)
                        .orElseThrow(() -> new InventoryNotFoundException("Inventory transaction not found: " + id)));
    }

    @Override
    @Transactional(readOnly = true)
    public List<InventoryTransactionDTO> getByProduct(Long productId) {
        return InventoryTransactionMapper.toDTOList(
                transactionRepository.findByProduct_IdOrderByCreatedAtDesc(productId));
    }

    @Override
    @Transactional(readOnly = true)
    public List<InventoryTransactionDTO> getByWarehouse(Long warehouseId) {
        return InventoryTransactionMapper.toDTOList(
                transactionRepository.findByWarehouse_IdOrderByCreatedAtDesc(warehouseId));
    }

    @Override
    @Transactional(readOnly = true)
    public List<InventoryTransactionDTO> getByProductAndWarehouse(Long productId, Long warehouseId) {
        return InventoryTransactionMapper.toDTOList(
                transactionRepository.findByProduct_IdAndWarehouse_IdOrderByCreatedAtDesc(productId, warehouseId));
    }

    @Override
    @Transactional(readOnly = true)
    public List<InventoryTransactionDTO> getByReference(String referenceType, Long referenceId) {
        return InventoryTransactionMapper.toDTOList(
                transactionRepository.findByReferenceTypeAndReferenceIdOrderByCreatedAtDesc(referenceType, referenceId));
    }

    @Override
    @Transactional(readOnly = true)
    public List<InventoryTransactionDTO> getByDateRange(LocalDate from, LocalDate to) {
        // Inclusive of the whole `to` day — from 00:00:00 through 23:59:59.999
        var fromDateTime = from.atStartOfDay();
        var toDateTime = to.atTime(LocalTime.MAX);
        return InventoryTransactionMapper.toDTOList(
                transactionRepository.findByCreatedAtBetweenOrderByCreatedAtDesc(fromDateTime, toDateTime));
    }

    @Override
    @Transactional(readOnly = true)
    public Long getNetQuantity(Long productId, Long variantId, Long warehouseId) {
        return transactionRepository.sumNetQuantity(productId, variantId, warehouseId);
    }
}
