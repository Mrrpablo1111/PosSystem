package com.sh.sh.pos.system.service.serviceimpl.productvariantserviceimpl;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import com.sh.sh.pos.system.exception.ResourceNotFoundException;
import com.sh.sh.pos.system.exception.UserException;
import com.sh.sh.pos.system.mapper.productvariantmapper.ProductVariantMapper;
import com.sh.sh.pos.system.model.products.Product;
import com.sh.sh.pos.system.model.products.ProductOptionValue;
import com.sh.sh.pos.system.model.products.ProductVariant;
import com.sh.sh.pos.system.model.products.ProductVariantValue;
import com.sh.sh.pos.system.payload.dto.productdto.ProductVariantDTO;
import com.sh.sh.pos.system.payload.dto.productdto.ProductVariantValueDTO;
import com.sh.sh.pos.system.repository.productoptionvaluerepository.ProductOptionValueRepository;
import com.sh.sh.pos.system.repository.productrepository.ProductRepository;
import com.sh.sh.pos.system.repository.productvariantrepository.ProductVariantRepository;
import com.sh.sh.pos.system.repository.productvariantvaluerepository.ProductVariantValueRepository;
import com.sh.sh.pos.system.service.productservice.ProductVariantService;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ProductVariantServiceImpl implements ProductVariantService {

    private static final String SKU_EXISTS = "SKU already exists.";
    private static final String BARCODE_EXISTS = "Barcode already exists.";

    private final ProductRepository productRepository;
    private final ProductVariantRepository variantRepository;
    private final ProductOptionValueRepository optionValueRepository;
    private final ProductVariantValueRepository variantValueRepository;

    @Transactional
    @Override
    public ProductVariantDTO create(ProductVariantDTO dto) {
        if (variantRepository.existsBySku(dto.getSku())) {
            throw new UserException(SKU_EXISTS);
        }
        if (dto.getBarcode() != null && variantRepository.existsByBarcode(dto.getBarcode())) {
            throw new UserException(BARCODE_EXISTS);
        }

        Product product = productRepository.findById(dto.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Product not found with id: " + dto.getProductId()));

        ProductVariant variant = ProductVariantMapper.toEntity(dto, product);
        variant.setValues(buildVariantValues(dto.getValues(), variant, product.getId()));

        return ProductVariantMapper.toDTO(variantRepository.save(variant));
    }

    @Transactional
    @Override
    public ProductVariantDTO update(Long id, ProductVariantDTO dto) {
        ProductVariant variant = findVariantOrThrow(id);

        // Validate uniqueness BEFORE mutating — comparisons must use current DB values
        if (!variant.getSku().equals(dto.getSku())
                && variantRepository.existsBySku(dto.getSku())) {
            throw new UserException(SKU_EXISTS);
        }
        if (dto.getBarcode() != null
                && !dto.getBarcode().equals(variant.getBarcode())
                && variantRepository.existsByBarcode(dto.getBarcode())) {
            throw new UserException(BARCODE_EXISTS);
        }

        variant.setName(dto.getName());
        variant.setSku(dto.getSku());
        variant.setBarcode(dto.getBarcode());
        variant.setItemCode(dto.getItemCode());
        variant.setCostPrice(dto.getCostPrice());
        variant.setSellingPrice(dto.getSellingPrice());
        variant.setActive(dto.getActive());

        // Replace option values
        variant.getValues().clear();
        variantValueRepository.deleteByVariantId(variant.getId());
        if (dto.getValues() != null) {
            variant.getValues().addAll(
                    buildVariantValues(dto.getValues(), variant, variant.getProduct().getId()));
        }

        return ProductVariantMapper.toDTO(variantRepository.save(variant));
    }

    @Override
    public ProductVariantDTO get(Long id) {
        return ProductVariantMapper.toDTO(findVariantOrThrow(id));
    }

    @Override
    public List<ProductVariantDTO> getByProduct(Long productId) {
        if (!productRepository.existsById(productId)) {
            throw new ResourceNotFoundException("Product not found with id: " + productId);
        }

        return variantRepository.findByProductId(productId)
                .stream()
                .map(ProductVariantMapper::toDTO)
                .toList();
    }

    @Transactional
    @Override
    public void delete(Long id) {
        ProductVariant variant = findVariantOrThrow(id);
        variantValueRepository.deleteByVariantId(id);
        variantRepository.delete(variant);
    }

    // ── Helpers ───────────────────────────────────────────────────────────

    private ProductVariant findVariantOrThrow(Long id) {
        return variantRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Variant not found with id: " + id));
    }

    /**
     * Resolves option-value ids into ProductVariantValue links, verifying each
     * option value belongs to the given product.
     */
    private List<ProductVariantValue> buildVariantValues(
            List<ProductVariantValueDTO> valueDTOs, ProductVariant variant, Long productId) {

        List<ProductVariantValue> variantValues = new ArrayList<>();
        if (valueDTOs == null || valueDTOs.isEmpty()) {
            return variantValues;
        }

        for (ProductVariantValueDTO valueDTO : valueDTOs) {
            ProductOptionValue optionValue = optionValueRepository.findById(valueDTO.getOptionValueId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Option value not found with id: " + valueDTO.getOptionValueId()));

            if (!optionValue.getOption().getProduct().getId().equals(productId)) {
                throw new UserException("Option value does not belong to this product.");
            }

            ProductVariantValue variantValue = new ProductVariantValue();
            variantValue.setVariant(variant);
            variantValue.setOptionValue(optionValue);
            variantValues.add(variantValue);
        }
        return variantValues;
    }
}