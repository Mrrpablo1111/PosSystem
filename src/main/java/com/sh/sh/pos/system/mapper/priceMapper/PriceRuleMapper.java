package com.sh.sh.pos.system.mapper.priceMapper;

import com.sh.sh.pos.system.model.Branch;
import com.sh.sh.pos.system.model.price.PriceGroup;
import com.sh.sh.pos.system.model.price.PriceRule;
import com.sh.sh.pos.system.model.products.Product;
import com.sh.sh.pos.system.model.products.ProductVariant;
import com.sh.sh.pos.system.payload.dto.priceDTO.PriceRuleDTO;

public class PriceRuleMapper {
    public static PriceRuleDTO toDTO(PriceRule rule) {
        return PriceRuleDTO.builder()
                .id(rule.getId())

                .productId(rule.getProduct() != null ? rule.getProduct().getId() : null)
                .productName(rule.getProduct() != null ? rule.getProduct().getName() : null)

                .variantId(rule.getVariant() != null ? rule.getVariant().getId() : null)
                .variantName(rule.getVariant() != null ? rule.getVariant().getName() : null)

                .priceGroupId(rule.getPriceGroup() != null ? rule.getPriceGroup().getId() : null)
                .priceGroupName(rule.getPriceGroup() != null ? rule.getPriceGroup().getName() : null)

                .branchId(rule.getBranch() != null ? rule.getBranch().getId() : null)
                .branchName(rule.getBranch() != null ? rule.getBranch().getName() : null)

                .fixedPrice(rule.getFixedPrice())
                .discountPercent(rule.getDiscountPercent())
                .markupPercent(rule.getMarkupPercent())
                .minQuantity(rule.getMinQuantity())
                .startDate(rule.getStartDate())
                .endDate(rule.getEndDate())
                .priority(rule.getPriority())
                .active(rule.getActive())
                .createdAt(rule.getCreatedAt())
                .build();
    }

    public static PriceRule toEntity(
            PriceRuleDTO dto,
            Product product,
            ProductVariant variant,
            PriceGroup priceGroup,
            Branch branch) {

        return PriceRule.builder()
                .id(dto.getId())
                .product(product)
                .variant(variant)
                .priceGroup(priceGroup)
                .branch(branch)
                .fixedPrice(dto.getFixedPrice())
                .discountPercent(dto.getDiscountPercent())
                .markupPercent(dto.getMarkupPercent())
                .minQuantity(dto.getMinQuantity() != null ? dto.getMinQuantity() : 1)
                .startDate(dto.getStartDate())
                .endDate(dto.getEndDate())
                .priority(dto.getPriority() != null ? dto.getPriority() : 1)
                .active(dto.getActive() != null ? dto.getActive() : true)
                .build();
    }
}
