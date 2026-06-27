package com.sh.sh.pos.system.service.serviceImpl.priceServiceImpl;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Service;

import com.sh.sh.pos.system.model.Customer;
import com.sh.sh.pos.system.model.price.PriceGroup;
import com.sh.sh.pos.system.model.price.PriceRule;
import com.sh.sh.pos.system.model.products.Product;
import com.sh.sh.pos.system.model.products.ProductVariant;
import com.sh.sh.pos.system.repository.CustomerRepository;
import com.sh.sh.pos.system.repository.ProductRepository;
import com.sh.sh.pos.system.repository.ProductVariantRepository;
import com.sh.sh.pos.system.repository.priceRepository.PriceRuleRepository;
import com.sh.sh.pos.system.service.priceService.PriceService;

import jakarta.persistence.EntityNotFoundException;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PriceServiceImpl implements PriceService{
    private final PriceRuleRepository priceRuleRepository;
    private final ProductRepository productRepository;
    private final ProductVariantRepository variantRepository;
    private final CustomerRepository customerRepository;

    @Override
    public BigDecimal resolvePrice(
            Long productId,
            Long variantId,
            Long customerId,
            Long branchId,
            Integer quantity) {

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new EntityNotFoundException("Product not found"));

        ProductVariant variant = null;
        if (variantId != null) {
            variant = variantRepository.findById(variantId)
                    .orElseThrow(() -> new EntityNotFoundException("Variant not found"));
        }

        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new EntityNotFoundException("Customer not found"));

        PriceGroup priceGroup = customer.getPriceGroup();

        if (priceGroup == null) {
            throw new IllegalArgumentException("Customer has no price group");
        }

        List<PriceRule> rules = priceRuleRepository.findApplicableRules(
                productId,
                variantId,
                priceGroup.getId(),
                branchId,
                quantity != null ? quantity : 1,
                LocalDate.now()
        );

        BigDecimal basePrice = variant != null && variant.getSellingPrice() != null
                ? variant.getSellingPrice()
                : product.getSellingPrice();

        if (rules.isEmpty()) {
            return basePrice;
        }

        PriceRule rule = rules.get(0);

        if (rule.getFixedPrice() != null) {
            return rule.getFixedPrice();
        }

        if (rule.getDiscountPercent() != null) {
            return basePrice.subtract(
                    basePrice.multiply(rule.getDiscountPercent())
                            .divide(BigDecimal.valueOf(100))
            );
        }

        if (rule.getMarkupPercent() != null) {
            BigDecimal cost = variant != null && variant.getCostPrice() != null
                    ? variant.getCostPrice()
                    : product.getCostPrice();

            return cost.add(
                    cost.multiply(rule.getMarkupPercent())
                            .divide(BigDecimal.valueOf(100))
            );
        }

        return basePrice;
    }
}
