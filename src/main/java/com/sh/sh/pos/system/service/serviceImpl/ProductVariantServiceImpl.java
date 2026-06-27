package com.sh.sh.pos.system.service.serviceImpl;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import com.sh.sh.pos.system.mapper.ProductVariantMapper;
import com.sh.sh.pos.system.model.products.Product;
import com.sh.sh.pos.system.model.products.ProductOptionValue;
import com.sh.sh.pos.system.model.products.ProductVariant;
import com.sh.sh.pos.system.model.products.ProductVariantValue;
import com.sh.sh.pos.system.payload.dto.productDTO.ProductVariantDTO;
import com.sh.sh.pos.system.payload.dto.productDTO.ProductVariantValueDTO;
import com.sh.sh.pos.system.repository.ProductOptionValueRepository;
import com.sh.sh.pos.system.repository.ProductRepository;
import com.sh.sh.pos.system.repository.ProductVariantRepository;
import com.sh.sh.pos.system.repository.ProductVariantValueRepository;
import com.sh.sh.pos.system.service.ProductService.ProductVariantService;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ProductVariantServiceImpl implements ProductVariantService {
    private final ProductRepository productRepository;
    private final ProductVariantRepository variantRepository;
    private final ProductOptionValueRepository optionValueRepository;
    private final ProductVariantValueRepository variantValueRepository;

    @Transactional
    @Override
    public ProductVariantDTO create(ProductVariantDTO dto) {
        if (variantRepository.existsBySku(dto.getSku())) {
            throw new IllegalArgumentException("SKU already exists.");
        }

        if (dto.getBarcode() != null &&
                variantRepository.existsByBarcode(dto.getBarcode())) {

            throw new IllegalArgumentException("Barcode already exists.");
        }

        Product product = productRepository.findById(dto.getProductId())
                .orElseThrow(() -> new EntityNotFoundException("Product not found"));

        ProductVariant variant = ProductVariantMapper.toEntity(dto, product);
        List<ProductVariantValue> variantValues = new ArrayList<>();

        if (dto.getValues() != null && !dto.getValues().isEmpty()) {

            for (ProductVariantValueDTO valueDTO : dto.getValues()) {

                ProductOptionValue optionValue = optionValueRepository.findById(valueDTO.getOptionValueId())
                        .orElseThrow(() -> new EntityNotFoundException("Option value not found"));
                if (!optionValue.getOption().getProduct().getId().equals(product.getId())) {
                    throw new IllegalArgumentException("Option value does not belong to this product.");
                }

                ProductVariantValue variantValue = new ProductVariantValue();

                variantValue.setVariant(variant);
                variantValue.setOptionValue(optionValue);

                variantValues.add(variantValue);

            }
        }
        variant.setValues(variantValues);
        ProductVariant saved = variantRepository.save(variant);

        return ProductVariantMapper.toDTO(saved);
    }

    @Transactional
    @Override
    public ProductVariantDTO update(Long id, ProductVariantDTO dto) {
        ProductVariant variant = variantRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Variant not found"));

        variant.setName(dto.getName());
        variant.setSku(dto.getSku());
        variant.setBarcode(dto.getBarcode());
        variant.setItemCode(dto.getItemCode());
        variant.setCostPrice(dto.getCostPrice());
        variant.setSellingPrice(dto.getSellingPrice());
        variant.setActive(dto.getActive());

        variant.getValues().clear();

        variant = variantRepository.save(variant);

        variantValueRepository.deleteByVariantId(variant.getId());

        if (!variant.getSku().equals(dto.getSku())
                && variantRepository.existsBySku(dto.getSku())) {
            throw new IllegalArgumentException("SKU already exists.");
        }

        if (dto.getBarcode() != null
                && !dto.getBarcode().equals(variant.getBarcode())
                && variantRepository.existsByBarcode(dto.getBarcode())) {
            throw new IllegalArgumentException("Barcode already exists.");
        }

        if (dto.getValues() != null) {

            for (ProductVariantValueDTO valueDTO : dto.getValues()) {

                ProductOptionValue optionValue = optionValueRepository.findById(valueDTO.getOptionValueId())
                        .orElseThrow(() -> new EntityNotFoundException("Option value not found"));

                if (!optionValue.getOption()
                        .getProduct()
                        .getId()
                        .equals(variant.getProduct().getId())) {

                    throw new IllegalArgumentException(
                            "Option value does not belong to this product.");
                }
                ProductVariantValue variantValue = new ProductVariantValue();

                variantValue.setVariant(variant);
                variantValue.setOptionValue(optionValue);

                variant.getValues().add(variantValue);
            }
        }

        return ProductVariantMapper.toDTO(variantRepository.save(variant));
    }

    @Override
    public ProductVariantDTO get(Long id) {
        ProductVariant variant = variantRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Variant not found"));

        return ProductVariantMapper.toDTO(variant);
    }

    @Override
    public List<ProductVariantDTO> getByProduct(Long productId) {
        productRepository.findById(productId)
                .orElseThrow(() -> new EntityNotFoundException("Product not found"));

        return variantRepository.findByProductId(productId)
                .stream()
                .map(ProductVariantMapper::toDTO)
                .toList();
    }

    @Transactional
    @Override
    public void delete(Long id) {
        ProductVariant variant = variantRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Variant not found"));
        variantValueRepository.deleteByVariantId(id);
        variantRepository.delete(variant);
    }

}
