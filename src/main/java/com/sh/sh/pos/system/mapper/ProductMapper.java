package com.sh.sh.pos.system.mapper;

import com.sh.sh.pos.system.model.Product;
import com.sh.sh.pos.system.payload.dto.ProductDTO;

public class ProductMapper {
	public ProductDTO toDTO(Product product) {
		
		return ProductDTO.builder()
				.id(product.getId())
				.name(product.getName())
				.sku(product.getSku())
				.description(product.getDescription())
				.mrp(product.getMrp())
				.sellingPrice(product.getSellingPrice())
				.brand(product.getBrand())
				.image(product.getImage())
				.storeId(product.getStore()!=null?product.getStore().getId():null)
				.createdAt(prodcut)
	}
}
