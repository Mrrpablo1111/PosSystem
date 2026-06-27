package com.sh.sh.pos.system.service.serviceImpl.stockServiceImpl;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import com.sh.sh.pos.system.domain.StockRequestStatus;
import com.sh.sh.pos.system.mapper.stockMapper.StockRequestMapper;
import com.sh.sh.pos.system.model.Branch;
import com.sh.sh.pos.system.model.User;
import com.sh.sh.pos.system.model.products.Product;
import com.sh.sh.pos.system.model.products.ProductVariant;
import com.sh.sh.pos.system.model.stocks.StockRequest;
import com.sh.sh.pos.system.model.stocks.StockRequestItem;
import com.sh.sh.pos.system.payload.dto.stockDTO.StockRequestDTO;
import com.sh.sh.pos.system.payload.dto.stockDTO.StockRequestItemDTO;
import com.sh.sh.pos.system.repository.BranchRepository;
import com.sh.sh.pos.system.repository.ProductRepository;
import com.sh.sh.pos.system.repository.ProductVariantRepository;
import com.sh.sh.pos.system.repository.stockRepository.StockRequestRepository;
import com.sh.sh.pos.system.service.stockService.StockRequestService;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class StockRequestServiceImpl implements StockRequestService {

    private final StockRequestRepository requestRepository;
    private final BranchRepository branchRepository;
    private final ProductRepository productRepository;
    private final ProductVariantRepository variantRepository;
    private final StockTransferServiceImpl transferService;

    @Override
    @Transactional
    public StockRequestDTO create(StockRequestDTO dto, User user) {
        Branch fromBranch = branchRepository.findById(dto.getFromBranchId())
                .orElseThrow(() -> new EntityNotFoundException("From branch not found"));

        Branch toBranch = branchRepository.findById(dto.getToBranchId())
                .orElseThrow(() -> new EntityNotFoundException("To branch not found"));

        StockRequest request = StockRequestMapper.toEntity(dto, fromBranch, toBranch);
        request.setRequestedBy(user);
        request.setStatus(StockRequestStatus.PENDING);

        List<StockRequestItem> items = new ArrayList<>();

        if (dto.getItems() != null) {
            for (StockRequestItemDTO itemDTO : dto.getItems()) {
                Product product = productRepository.findById(itemDTO.getProductId())
                        .orElseThrow(() -> new EntityNotFoundException("Product not found"));

                ProductVariant variant = null;
                if (itemDTO.getVariantId() != null) {
                    variant = variantRepository.findById(itemDTO.getVariantId())
                            .orElseThrow(() -> new EntityNotFoundException("Variant not found"));
                }

                items.add(StockRequestItem.builder()
                        .request(request)
                        .product(product)
                        .variant(variant)
                        .quantity(itemDTO.getQuantity())
                        .build());
            }
        }

        request.setItems(items);

        return StockRequestMapper.toDTO(requestRepository.save(request));
    }

    @Override
    @Transactional
    public StockRequestDTO approve(Long id, User user) {
        StockRequest request = requestRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Request not found"));

        request.setStatus(StockRequestStatus.APPROVED);
        request.setApprovedBy(user);
        request.setApprovedAt(LocalDateTime.now());

        return StockRequestMapper.toDTO(requestRepository.save(request));
    }

    @Override
    @Transactional
    public StockRequestDTO reject(Long id, User user) {
        StockRequest request = requestRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Request not found"));

        request.setStatus(StockRequestStatus.REJECTED);

        return StockRequestMapper.toDTO(requestRepository.save(request));
    }

    @Override
    public StockRequestDTO get(Long id) {
        return requestRepository.findById(id)
                .map(StockRequestMapper::toDTO)
                .orElseThrow(() -> new EntityNotFoundException("Request not found"));
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
        StockRequest request = requestRepository.findById(requestId)
                .orElseThrow(() -> new EntityNotFoundException("Request not found"));

        if (request.getStatus() != StockRequestStatus.APPROVED) {
            throw new IllegalArgumentException("Only approved request can be converted to transfer");
        }

        request.setStatus(StockRequestStatus.CONVERTED_TO_TRANSFER);

        return StockRequestMapper.toDTO(requestRepository.save(request));

    }

}
