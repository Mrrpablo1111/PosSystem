package com.sh.sh.pos.system.mapper.productoptionmapper;
import com.sh.sh.pos.system.model.products.Product;
import com.sh.sh.pos.system.model.products.ProductOption;
import com.sh.sh.pos.system.payload.dto.productdto.ProductOptionDTO;

public class ProductOptionMapper {
    private ProductOptionMapper() {
    }

    public static ProductOptionDTO toDTO(ProductOption option) {
        ProductOptionDTO dto = new ProductOptionDTO();

        dto.setId(option.getId());
        dto.setName(option.getName());
        dto.setProductId(
                option.getProduct() != null
                        ? option.getProduct().getId()
                        : null);

        return dto;
    }

    public static ProductOption toEntity(ProductOptionDTO dto, Product product) {

        ProductOption option = new ProductOption();

        option.setId(dto.getId());
        option.setName(dto.getName());
        option.setProduct(product);

        return option;
    }

}