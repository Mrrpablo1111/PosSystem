package com.sh.sh.pos.system.service.serviceImpl;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;

import com.sh.sh.pos.system.domain.StockMovementType;
import com.sh.sh.pos.system.mapper.StockMovementMapper;
import com.sh.sh.pos.system.model.Branch;
import com.sh.sh.pos.system.model.StockMovement;
import com.sh.sh.pos.system.model.products.Product;
import com.sh.sh.pos.system.model.products.ProductBatch;
import com.sh.sh.pos.system.model.products.ProductVariant;
import com.sh.sh.pos.system.payload.dto.StockMovementDTO;
import com.sh.sh.pos.system.repository.BranchRepository;
import com.sh.sh.pos.system.repository.ProductBatchRepository;
import com.sh.sh.pos.system.repository.ProductRepository;
import com.sh.sh.pos.system.repository.ProductVariantRepository;
import com.sh.sh.pos.system.repository.StockMovementRepository;
import com.sh.sh.pos.system.service.StockMovementService;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class StockMovementServiceImpl implements StockMovementService {
    private final BranchRepository branchRepository;
    private final ProductRepository productRepository;
    private final ProductVariantRepository variantRepository;
    private final ProductBatchRepository productBatchRepository;
    private final StockMovementRepository movementRepository;

    @Transactional
    @Override
    public StockMovementDTO create(StockMovementDTO dto) {
        Branch branch = branchRepository.findById(dto.getBranchId()).orElseThrow(
            ()-> new EntityNotFoundException("Branch not found")
        );
        Product product = productRepository.findById(dto.getProductId()).orElseThrow(
            ()-> new EntityNotFoundException("Product not found")
        );
        ProductVariant variant= null;
        if(dto.getBatchId() != null){
            variant = variantRepository.findById(dto.getVariantId())
                    .orElseThrow(()-> new EntityNotFoundException("Variant not found"));

        } 

        ProductBatch batch = null;
        if(dto.getBatchId() != null){
            batch = productBatchRepository.findById(dto.getBatchId())
                    .orElseThrow(()-> new EntityNotFoundException("Batch not found"));
        }
         StockMovement movement = StockMovementMapper.toEntity(
                dto,
                branch,
                product,
                variant,
                batch
        );

        return StockMovementMapper.toDTO(movementRepository.save(movement));
    }

    @Override
    public StockMovementDTO get(Long id) {
        StockMovement movement = movementRepository.findById(id).orElseThrow(
            ()-> new EntityNotFoundException("Stock movement not found"));

            return StockMovementMapper.toDTO(movement);
        
    }

    @Override
    public List<StockMovementDTO> getAll() {
        return movementRepository.findAll().stream().map(StockMovementMapper::toDTO).toList();
    }

    @Override
    public List<StockMovementDTO> getByBranch(Long branchId) {
       return movementRepository.findByBranch_Id(branchId)
                .stream()
                .map(StockMovementMapper::toDTO)
                .toList();
    }

    @Override
    public List<StockMovementDTO> getByProduct(Long productId) {
        return movementRepository.findByProduct_Id(productId)
                .stream()
                .map(StockMovementMapper::toDTO)
                .toList();
    }

    @Override
    public List<StockMovementDTO> getByVariant(Long variantId) {
        return movementRepository.findByVariant_Id(variantId)
                .stream()
                .map(StockMovementMapper::toDTO)
                .toList();
    }

    @Override
    public List<StockMovementDTO> getByBatch(Long batchId) {
        return movementRepository.findByBatch_Id(batchId)
                .stream()
                .map(StockMovementMapper::toDTO)
                .toList();
    }

    @Override
    public List<StockMovementDTO> getByType(StockMovementType movementType) {
        return movementRepository.findByMovementType(movementType)
                .stream()
                .map(StockMovementMapper::toDTO)
                .toList();
    }

    @Override
    public List<StockMovementDTO> getByDateRange(LocalDateTime start, LocalDateTime end) {
        return movementRepository.findByCreatedAtBetween(start, end)
                .stream()
                .map(StockMovementMapper::toDTO)
                .toList();
    }
    
}
