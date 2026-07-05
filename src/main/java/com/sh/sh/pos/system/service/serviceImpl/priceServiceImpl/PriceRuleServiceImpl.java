package com.sh.sh.pos.system.service.serviceimpl.priceserviceimpl;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sh.sh.pos.system.exception.ResourceNotFoundException;
import com.sh.sh.pos.system.exception.UserException;
import com.sh.sh.pos.system.mapper.pricemapper.PriceRuleMapper;
import com.sh.sh.pos.system.model.branch.Branch;
import com.sh.sh.pos.system.model.price.PriceGroup;
import com.sh.sh.pos.system.model.price.PriceRule;
import com.sh.sh.pos.system.model.products.Product;
import com.sh.sh.pos.system.model.products.ProductVariant;
import com.sh.sh.pos.system.payload.dto.pricedto.PriceRuleDTO;
import com.sh.sh.pos.system.repository.branchrepository.BranchRepository;
import com.sh.sh.pos.system.repository.pricerepository.PriceGroupRepository;
import com.sh.sh.pos.system.repository.pricerepository.PriceRuleRepository;
import com.sh.sh.pos.system.repository.productrepository.ProductRepository;
import com.sh.sh.pos.system.repository.productvariantrepository.ProductVariantRepository;
import com.sh.sh.pos.system.service.priceservice.PriceRuleService;

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
    @Transactional
    public PriceRuleDTO create(PriceRuleDTO dto) {
        Product product = productRepository.findById(dto.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Product not found with id: " + dto.getProductId()));

        ProductVariant variant = null;
        if (dto.getVariantId() != null) {
            variant = variantRepository.findById(dto.getVariantId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Variant not found with id: " + dto.getVariantId()));
        }

        PriceGroup group = priceGroupRepository.findById(dto.getPriceGroupId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Price group not found with id: " + dto.getPriceGroupId()));

        Branch branch = null;
        if (dto.getBranchId() != null) {
            branch = branchRepository.findById(dto.getBranchId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Branch not found with id: " + dto.getBranchId()));
        }

        validatePricingMethod(dto);

        PriceRule rule = PriceRuleMapper.toEntity(dto, product, variant, group, branch);

        return PriceRuleMapper.toDTO(priceRuleRepository.save(rule));
    }

    @Override
    @Transactional
    public PriceRuleDTO update(Long id, PriceRuleDTO dto) {
        PriceRule rule = findPriceRuleOrThrow(id);

        validatePricingMethod(dto);

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

        return PriceRuleMapper.toDTO(priceRuleRepository.save(rule));
    }

    @Override
    @Transactional(readOnly = true)
    public PriceRuleDTO get(Long id) {
        return PriceRuleMapper.toDTO(findPriceRuleOrThrow(id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<PriceRuleDTO> getAll() {
        return priceRuleRepository.findAll()
                .stream()
                .map(PriceRuleMapper::toDTO)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<PriceRuleDTO> getByProduct(Long productId) {
        return priceRuleRepository.findByProductId(productId)
                .stream()
                .map(PriceRuleMapper::toDTO)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<PriceRuleDTO> getByPriceGroup(Long priceGroupId) {
        return priceRuleRepository.findByPriceGroupId(priceGroupId)
                .stream()
                .map(PriceRuleMapper::toDTO)
                .toList();
    }

    @Override
    @Transactional
    public void delete(Long id) {
        priceRuleRepository.delete(findPriceRuleOrThrow(id));
    }

    // ── Helpers ───────────────────────────────────────────────────────────

    private PriceRule findPriceRuleOrThrow(Long id) {
        return priceRuleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Price rule not found with id: " + id));
    }

    private void validatePricingMethod(PriceRuleDTO dto) {
        int count = 0;

        if (dto.getFixedPrice() != null) {
            count++;
        }
        if (dto.getDiscountPercent() != null) {
            count++;
        }
        if (dto.getMarkupPercent() != null) {
            count++;
        }

        if (count != 1) {
            throw new UserException("Exactly one pricing method must be provided "
                    + "(fixedPrice, discountPercent, or markupPercent)");
        }
    }
}