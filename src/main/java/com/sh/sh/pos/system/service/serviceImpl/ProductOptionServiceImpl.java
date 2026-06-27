package com.sh.sh.pos.system.service.serviceImpl;

import java.util.List;

import org.springframework.stereotype.Service;

import com.sh.sh.pos.system.mapper.ProductOptionMapper;
import com.sh.sh.pos.system.model.products.Product;
import com.sh.sh.pos.system.model.products.ProductOption;
import com.sh.sh.pos.system.payload.dto.productDTO.ProductOptionDTO;
import com.sh.sh.pos.system.repository.ProductOptionRepository;
import com.sh.sh.pos.system.repository.ProductRepository;
import com.sh.sh.pos.system.service.ProductService.ProductOptionService;

import jakarta.persistence.EntityNotFoundException;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ProductOptionServiceImpl implements ProductOptionService {
    private final ProductOptionRepository optionRepository;
    private final ProductRepository productRepository;

    @Override
    public ProductOptionDTO create(ProductOptionDTO productOptionDTO) {
        Product product = productRepository.findById(productOptionDTO.getProductId())
                .orElseThrow(() -> new EntityNotFoundException("Product not found"));

        ProductOption option = ProductOptionMapper.toEntity(productOptionDTO, product);

        return ProductOptionMapper.toDTO(
                optionRepository.save(option));

    }

    @Override
    public ProductOptionDTO update(Long id, ProductOptionDTO productOptionDTO) {
        ProductOption option = optionRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Option not found"));

        option.setName(productOptionDTO.getName());

        return ProductOptionMapper.toDTO(
                optionRepository.save(option));
    }

    @Override
    public void delete(Long id) {
        ProductOption option = optionRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Option not found"));

        optionRepository.delete(option);
    }

    @Override
    public List<ProductOptionDTO> findByProduct(Long productId) {
        return optionRepository.findByProductId(productId)
                .stream()
                .map(ProductOptionMapper::toDTO)
                .toList();
    }

}
