package com.sh.sh.pos.system.repository.priceRepository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.sh.sh.pos.system.model.price.PriceRule;

public interface PriceRuleRepository extends JpaRepository<PriceRule, Long> {
    List<PriceRule> findByProduct_Id(Long productId);

    List<PriceRule> findByPriceGroup_Id(Long priceGroupId);

    @Query("""
                SELECT r FROM PriceRule r
                WHERE r.active = true
                AND r.product.id = :productId
                AND r.priceGroup.id = :priceGroupId
                AND (:variantId IS NULL OR r.variant IS NULL OR r.variant.id = :variantId)
                AND (:branchId IS NULL OR r.branch IS NULL OR r.branch.id = :branchId)
                AND r.minQuantity <= :quantity
                AND (r.startDate IS NULL OR r.startDate <= :today)
                AND (r.endDate IS NULL OR r.endDate >= :today)
                ORDER BY r.priority DESC, r.minQuantity DESC
            """)
    List<PriceRule> findApplicableRules(
            Long productId,
            Long variantId,
            Long priceGroupId,
            Long branchId,
            Integer quantity,
            LocalDate today);

}
