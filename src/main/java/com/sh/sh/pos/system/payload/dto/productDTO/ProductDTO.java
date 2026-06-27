package com.sh.sh.pos.system.payload.dto.productDTO;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;


import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ProductDTO {

	private Long id;
	private Long storeId;
	private String name;
	private String sku;
	private String description;
	private BigDecimal mrp;
	private BigDecimal sellingPrice;
	private String brand;
	private String image;
	private Boolean active;
	private Double weight;
	private Long supplierId;
	private String supplierName;
	@NotNull(message = "Cost price is required")
	@Min(value = 0, message = "Cost price must be >= 0")
	private BigDecimal costPrice;

	@NotBlank(message = "Unit is required")
	private String unit;

	private Integer defaultReorderLevel;
	private String barcode;

	private Long categoryId;
	private String categoryName;

	private LocalDateTime createdAt;
	private LocalDateTime updatedAt;
	private List<ProductOptionDTO> options;

	private List<ProductVariantDTO> variants;

}
