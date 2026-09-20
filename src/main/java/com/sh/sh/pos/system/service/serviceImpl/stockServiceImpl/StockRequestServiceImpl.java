package com.sh.sh.pos.system.service.serviceimpl.stockserviceimpl;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sh.sh.pos.system.domain.StockRequestStatus;
import com.sh.sh.pos.system.exception.ResourceNotFoundException;
import com.sh.sh.pos.system.exception.UserException;
import com.sh.sh.pos.system.mapper.stockmapper.StockRequestMapper;
import com.sh.sh.pos.system.model.branch.Branch;
import com.sh.sh.pos.system.model.products.Product;
import com.sh.sh.pos.system.model.products.ProductVariant;
import com.sh.sh.pos.system.model.stocks.StockRequest;
import com.sh.sh.pos.system.model.stocks.StockRequestItem;
import com.sh.sh.pos.system.model.user.User;
import com.sh.sh.pos.system.payload.dto.stockdto.StockRequestDTO;
import com.sh.sh.pos.system.payload.dto.stockdto.StockRequestItemDTO;
import com.sh.sh.pos.system.repository.branchrepository.BranchRepository;
import com.sh.sh.pos.system.repository.productrepository.ProductRepository;
import com.sh.sh.pos.system.repository.productvariantrepository.ProductVariantRepository;
import com.sh.sh.pos.system.repository.stockrepository.StockRequestRepository;
import com.sh.sh.pos.system.service.stockservice.StockRequestService;


import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class StockRequestServiceImpl implements StockRequestService {

    private final StockRequestRepository requestRepository;
    private final BranchRepository branchRepository;
    private final ProductRepository productRepository;
    private final ProductVariantRepository variantRepository;

    @Override
    @Transactional
    public StockRequestDTO create(StockRequestDTO dto, User user) {
        Branch fromBranch = findBranchOrThrow(dto.getFromBranchId(), "From");
        Branch toBranch = findBranchOrThrow(dto.getToBranchId(), "To");

        if (fromBranch.getId().equals(toBranch.getId())) {
            throw new UserException("From and To branches must be different");
        }

        StockRequest request = StockRequestMapper.toEntity(dto, fromBranch, toBranch);
        request.setRequestedBy(user);
        request.setStatus(StockRequestStatus.PENDING);
        request.setItems(buildItems(dto.getItems(), request));

        return StockRequestMapper.toDTO(requestRepository.save(request));
    }

    @Override
    @Transactional
    public StockRequestDTO approve(Long id, User user) {
        StockRequest request = findRequestOrThrow(id);

        request.setStatus(StockRequestStatus.APPROVED);
        request.setApprovedBy(user);
        request.setApprovedAt(LocalDateTime.now());

        return StockRequestMapper.toDTO(requestRepository.save(request));
    }

    @Override
    @Transactional
    public StockRequestDTO reject(Long id, User user) {
        StockRequest request = findRequestOrThrow(id);

        request.setStatus(StockRequestStatus.REJECTED);

        return StockRequestMapper.toDTO(requestRepository.save(request));
    }

    @Override
    public StockRequestDTO get(Long id) {
        return StockRequestMapper.toDTO(findRequestOrThrow(id));
    }

    @Override
    public List<StockRequestDTO> getAll() {
        return requestRepository.findAll()
                .stream()
                .map(StockRequestMapper::toDTO)
                .toList();
    }

    @Override
    public List<StockRequestDTO> getByStatus(StockRequestStatus status) {
        return requestRepository.findByStatus(status)
                .stream()
                .map(StockRequestMapper::toDTO)
                .toList();
    }

    @Override
    @Transactional
    public StockRequestDTO convertToTransfer(Long requestId, User user) {
        StockRequest request = findRequestOrThrow(requestId);

        if (request.getStatus() != StockRequestStatus.APPROVED) {
            throw new UserException("Only approved requests can be converted to transfer");
        }

        request.setStatus(StockRequestStatus.CONVERTED_TO_TRANSFER);

        return StockRequestMapper.toDTO(requestRepository.save(request));
    }

    // ── Helpers ───────────────────────────────────────────────────────────

    private StockRequest findRequestOrThrow(Long id) {
        return requestRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Stock request not found with id: " + id));
    }

    private Branch findBranchOrThrow(Long id, String label) {
        return branchRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        label + " branch not found with id: " + id));
    }

    private List<StockRequestItem> buildItems(List<StockRequestItemDTO> itemDTOs, StockRequest request) {
        List<StockRequestItem> items = new ArrayList<>();
        if (itemDTOs == null) {
            return items;
        }

        for (StockRequestItemDTO itemDTO : itemDTOs) {
            Product product = productRepository.findById(itemDTO.getProductId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Product not found with id: " + itemDTO.getProductId()));

            ProductVariant variant = null;
            if (itemDTO.getVariantId() != null) {
                variant = variantRepository.findById(itemDTO.getVariantId())
                        .orElseThrow(() -> new ResourceNotFoundException(
                                "Variant not found with id: " + itemDTO.getVariantId()));
            }

            items.add(StockRequestItem.builder()
                    .request(request)
                    .product(product)
                    .variant(variant)
                    .quantity(itemDTO.getQuantity())
                    .build());
        }
        return items;
    }
}