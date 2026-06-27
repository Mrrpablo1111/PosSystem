package com.sh.sh.pos.system.service.serviceImpl.priceServiceImpl;

import java.util.List;

import org.springframework.stereotype.Service;

import com.sh.sh.pos.system.mapper.priceMapper.PriceRuleMapper;
import com.sh.sh.pos.system.model.Branch;
import com.sh.sh.pos.system.model.price.PriceGroup;
import com.sh.sh.pos.system.model.price.PriceRule;
import com.sh.sh.pos.system.model.products.Product;
import com.sh.sh.pos.system.model.products.ProductVariant;
import com.sh.sh.pos.system.payload.dto.priceDTO.PriceRuleDTO;
import com.sh.sh.pos.system.repository.BranchRepository;
import com.sh.sh.pos.system.repository.ProductRepository;
import com.sh.sh.pos.system.repository.ProductVariantRepository;
import com.sh.sh.pos.system.repository.priceRepository.PriceGroupRepository;
import com.sh.sh.pos.system.repository.priceRepository.PriceRuleRepository;
import com.sh.sh.pos.system.service.priceService.PriceRuleService;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PriceRuleServiceImpl implements PriceRuleService {
    private final PriceRuleRepository priceRuleRepository;
    private final ProductRepository productRepository;
    private final ProductVariantRepository variantRepository;
    private final PriceGroupRepository priceGroupRepository;
    private final BranchRepository branchRepository;

    @Override
    public PriceRuleDTO create(PriceRuleDTO dto) {
        Product product = productRepository.findById(dto.getProductId())
                .orElseThrow(() -> new EntityNotFoundException("Product not found"));

        ProductVariant variant = null;
        if (dto.getVariantId() != null) {
            variant = variantRepository.findById(dto.getVariantId())
                    .orElseThrow(() -> new EntityNotFoundException("Variant not found"));
        }

        PriceGroup group = priceGroupRepository.findById(dto.getPriceGroupId())
                .orElseThrow(() -> new EntityNotFoundException("Price group not found"));

        Branch branch = null;
        if (dto.getBranchId() != null) {
            branch = branchRepository.findById(dto.getBranchId())
                    .orElseThrow(() -> new EntityNotFoundException("Branch not found"));
        }

        validatePricingMethod(dto);

        PriceRule rule = PriceRuleMapper.toEntity(dto, product, variant, group, branch);

        return PriceRuleMapper.toDTO(priceRuleRepository.save(rule));
    }

    @Override
    public PriceRuleDTO update(Long id, PriceRuleDTO dto) {
        PriceRule rule = priceRuleRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Price rule not found"));

        rule.setFixedPrice(dto.getFixedPrice());
        rule.setDiscountPercent(dto.getDiscountPercent());
        rule.setMarkupPercent(dto.getMarkupPercent());
        rule.setMinQuantity(dto.getMinQuantity());
        rule.setStartDate(dto.getStartDate());
        rule.setEndDate(dto.getEndDate());
        rule.setPriority(dto.getPriority());

        if (dto.getActive() != null) {
            rule.setActive(dto.getActive());
        }
        validatePricingMethod(dto);
        return PriceRuleMapper.toDTO(priceRuleRepository.save(rule));
    }

    @Override
    public PriceRuleDTO get(Long id) {
        return priceRuleRepository.findById(id)
                .map(PriceRuleMapper::toDTO)
                .orElseThrow(() -> new EntityNotFoundException("Price rule not found"));
    }

    @Override
    public List<PriceRuleDTO> getAll() {
        return priceRuleRepository.findAll()
                .stream()
                .map(PriceRuleMapper::toDTO)
                .toList();
    }

    @Override
    public List<PriceRuleDTO> getByProduct(Long productId) {
        return priceRuleRepository.findByProduct_Id(productId)
                .stream()
                .map(PriceRuleMapper::toDTO)
                .toList();
    }

    @Override
    public List<PriceRuleDTO> getByPriceGroup(Long priceGroupId) {
        return priceRuleRepository.findByPriceGroup_Id(priceGroupId)
                .stream()
                .map(PriceRuleMapper::toDTO)
                .toList();
    }

    @Override
    public void delete(Long id) {
        PriceRule rule = priceRuleRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Price rule not found"));

        priceRuleRepository.delete(rule);
    }

    private void validatePricingMethod(PriceRuleDTO dto) {
        int count = 0;

        if (dto.getFixedPrice() != null)
            count++;

        if (dto.getDiscountPercent() != null)
            count++;

        if (dto.getMarkupPercent() != null)
            count++;

        if (count != 1) {
            throw new IllegalArgumentException(
                    "Exactly one pricing method must be provided");
        }
    }
}
