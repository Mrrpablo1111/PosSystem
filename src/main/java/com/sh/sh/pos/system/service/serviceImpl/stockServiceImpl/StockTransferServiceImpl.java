package com.sh.sh.pos.system.service.serviceImpl.stockServiceImpl;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sh.sh.pos.system.domain.ReferenceType;
import com.sh.sh.pos.system.domain.StockMovementType;
import com.sh.sh.pos.system.domain.StockTransferStatus;
import com.sh.sh.pos.system.mapper.stockMapper.StockTransferMapper;
import com.sh.sh.pos.system.model.Branch;
import com.sh.sh.pos.system.model.StockMovement;

import com.sh.sh.pos.system.model.User;
import com.sh.sh.pos.system.model.products.ProductBatch;
import com.sh.sh.pos.system.model.stocks.StockTransfer;
import com.sh.sh.pos.system.model.stocks.StockTransferHistory;
import com.sh.sh.pos.system.model.stocks.StockTransferItem;
import com.sh.sh.pos.system.payload.dto.stockDTO.StockTransferDTO;
import com.sh.sh.pos.system.payload.dto.stockDTO.StockTransferItemDTO;
import com.sh.sh.pos.system.repository.BranchRepository;
import com.sh.sh.pos.system.repository.ProductBatchRepository;
import com.sh.sh.pos.system.repository.StockMovementRepository;
import com.sh.sh.pos.system.repository.stockRepository.StockTransferRepository;
import com.sh.sh.pos.system.service.stockService.StockTransferService;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class StockTransferServiceImpl implements StockTransferService {

    private final StockTransferRepository transferRepository;
    private final BranchRepository branchRepository;
    private final ProductBatchRepository batchRepository;
    private final StockMovementRepository movementRepository;

    @Override
    @Transactional
    public StockTransferDTO create(StockTransferDTO dto, User user) {

        Branch fromBranch = branchRepository.findById(dto.getFromBranchId())
                .orElseThrow(() -> new EntityNotFoundException("From branch not found"));

        Branch toBranch = branchRepository.findById(dto.getToBranchId())
                .orElseThrow(() -> new EntityNotFoundException("To branch not found"));

        StockTransfer transfer = StockTransfer.builder()
                .fromBranch(fromBranch)
                .toBranch(toBranch)
                .note(dto.getNote())
                .status(StockTransferStatus.DRAFT)
                .build();

        List<StockTransferItem> items = new ArrayList<>();

        if (dto.getItems() != null) {
            for (StockTransferItemDTO itemDTO : dto.getItems()) {

                ProductBatch batch = batchRepository.findById(itemDTO.getBatchId())
                        .orElseThrow(() -> new EntityNotFoundException("Batch not found"));

                if (!batch.getBranch().getId().equals(fromBranch.getId())) {
                    throw new IllegalArgumentException("Batch does not belong to from branch");
                }

                Integer requestedQty = itemDTO.getRequestedQty();
                Integer shippedQty = itemDTO.getShippedQty() != null
                        ? itemDTO.getShippedQty()
                        : requestedQty;

                StockTransferItem item = StockTransferItem.builder()
                        .transfer(transfer)
                        .product(batch.getProduct())
                        .variant(batch.getVariant())
                        .batch(batch)
                        .requestedQty(requestedQty)
                        .shippedQty(shippedQty)
                        .receivedQty(0)
                        .damagedQty(0)
                        .missingQty(0)
                        .build();

                items.add(item);
            }
        }

        transfer.setItems(items);
        addHistory(transfer, StockTransferStatus.DRAFT, "Transfer created", user);

        return StockTransferMapper.toDTO(transferRepository.save(transfer));
    }

    @Override
    @Transactional
    public StockTransferDTO approve(Long id, User user) {
        StockTransfer transfer = getEntity(id);

        transfer.setStatus(StockTransferStatus.APPROVED);
        addHistory(transfer, StockTransferStatus.APPROVED, "Transfer approved", user);

        return StockTransferMapper.toDTO(transferRepository.save(transfer));
    }

    @Override
    @Transactional
    public StockTransferDTO markPicking(Long id, User user) {
        StockTransfer transfer = getEntity(id);

        transfer.setStatus(StockTransferStatus.PICKING);
        addHistory(transfer, StockTransferStatus.PICKING, "Picking started", user);

        return StockTransferMapper.toDTO(transferRepository.save(transfer));
    }

    @Override
    @Transactional
    public StockTransferDTO markPacked(Long id, User user) {
        StockTransfer transfer = getEntity(id);

        transfer.setStatus(StockTransferStatus.PACKED);
        addHistory(transfer, StockTransferStatus.PACKED, "Transfer packed", user);

        return StockTransferMapper.toDTO(transferRepository.save(transfer));
    }

    @Override
    @Transactional
    public StockTransferDTO ship(Long id, StockTransferDTO shipmentDto, User user) {
        StockTransfer transfer = getEntity(id);

        transfer.setStatus(StockTransferStatus.SHIPPED);
        transfer.setCarrier(shipmentDto.getCarrier());
        transfer.setTrackingNo(shipmentDto.getTrackingNo());
        transfer.setVehicleNo(shipmentDto.getVehicleNo());
        transfer.setDriverName(shipmentDto.getDriverName());
        transfer.setDriverPhone(shipmentDto.getDriverPhone());
        transfer.setShippedAt(LocalDateTime.now());
        transfer.setShippedBy(user);

        addHistory(transfer, StockTransferStatus.SHIPPED, "Transfer shipped", user);

        return StockTransferMapper.toDTO(transferRepository.save(transfer));
    }

    @Override
    @Transactional
    public StockTransferDTO receive(Long id, StockTransferDTO receiveDto, User user) {
        StockTransfer transfer = getEntity(id);

        if (transfer.getStatus() == StockTransferStatus.RECEIVED) {
            throw new IllegalArgumentException("Transfer already received");
        }

        for (StockTransferItem item : transfer.getItems()) {

            ProductBatch fromBatch = item.getBatch();

            if (fromBatch.getQuantity() < item.getShippedQty()) {
                throw new IllegalArgumentException("Not enough stock in batch " + fromBatch.getBatchNo());
            }

            fromBatch.setQuantity(fromBatch.getQuantity() - item.getShippedQty());
            batchRepository.save(fromBatch);

            ProductBatch toBatch = createToBatch(transfer.getToBranch(), fromBatch, item.getShippedQty());
            batchRepository.save(toBatch);

            createMovement(
                    transfer.getFromBranch(),
                    item,
                    fromBatch,
                    StockMovementType.TRANSFER_OUT,
                    transfer.getId());

            createMovement(
                    transfer.getToBranch(),
                    item,
                    toBatch,
                    StockMovementType.TRANSFER_IN,
                    transfer.getId());
        }

        transfer.setStatus(StockTransferStatus.RECEIVED);
        transfer.setReceivedAt(LocalDateTime.now());
        transfer.setReceivedBy(user);

        addHistory(transfer, StockTransferStatus.RECEIVED, "Transfer received", user);

        return StockTransferMapper.toDTO(transferRepository.save(transfer));
    }

    @Override
    public StockTransferDTO get(Long id) {
        return StockTransferMapper.toDTO(getEntity(id));
    }

    @Override
    public List<StockTransferDTO> getAll() {
        return transferRepository.findAll()
                .stream()
                .map(StockTransferMapper::toDTO)
                .toList();
    }

    @Override
    public List<StockTransferDTO> getByStatus(StockTransferStatus status) {
        return transferRepository.findByStatus(status)
                .stream()
                .map(StockTransferMapper::toDTO)
                .toList();
    }

    @Override
    @Transactional
    public void cancel(Long id, User user) {
        StockTransfer transfer = getEntity(id);

        if (transfer.getStatus() == StockTransferStatus.RECEIVED) {
            throw new IllegalArgumentException("Received transfer cannot be cancelled");
        }

        transfer.setStatus(StockTransferStatus.CANCELLED);
        addHistory(transfer, StockTransferStatus.CANCELLED, "Transfer cancelled", user);

        transferRepository.save(transfer);
    }

    private StockTransfer getEntity(Long id) {
        return transferRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Transfer not found"));
    }

    private ProductBatch createToBatch(Branch toBranch, ProductBatch fromBatch, Integer qty) {

        ProductBatch batch = new ProductBatch();

        batch.setBranch(toBranch);
        batch.setProduct(fromBatch.getProduct());
        batch.setVariant(fromBatch.getVariant());
        batch.setSupplier(fromBatch.getSupplier());
        batch.setBatchNo(fromBatch.getBatchNo());
        batch.setExpiryDate(fromBatch.getExpiryDate());
        batch.setPurchasePrice(fromBatch.getPurchasePrice());
        batch.setQuantity(qty);

        return batch;
    }

    private void createMovement(
            Branch branch,
            StockTransferItem item,
            ProductBatch batch,
            StockMovementType type,
            Long transferId) {

        BigDecimal totalCost = batch.getPurchasePrice()
                .multiply(BigDecimal.valueOf(item.getShippedQty()));

        StockMovement movement = StockMovement.builder()
                .branch(branch)
                .product(item.getProduct())
                .variant(item.getVariant())
                .batch(batch)
                .movementType(type)
                .referenceType(ReferenceType.STOCK_TRANSFER)
                .referenceId(transferId)
                .quantity(item.getShippedQty())
                .unitCost(batch.getPurchasePrice())
                .totalCost(totalCost)
                .note(type.name())
                .build();

        movementRepository.save(movement);
    }

    private void addHistory(
            StockTransfer transfer,
            StockTransferStatus status,
            String remarks,
            User user) {

        if (transfer.getHistories() == null) {
            transfer.setHistories(new ArrayList<>());
        }

        StockTransferHistory history = StockTransferHistory.builder()
                .transfer(transfer)
                .status(status)
                .remarks(remarks)
                .changedBy(user)
                .build();

        transfer.getHistories().add(history);
    }
}