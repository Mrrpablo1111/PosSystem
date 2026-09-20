package com.sh.sh.pos.system.service.serviceimpl.productoptionserviceimpl;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sh.sh.pos.system.exception.ResourceNotFoundException;
import com.sh.sh.pos.system.mapper.productoptionmapper.ProductOptionMapper;
import com.sh.sh.pos.system.model.products.Product;
import com.sh.sh.pos.system.model.products.ProductOption;
import com.sh.sh.pos.system.payload.dto.productdto.ProductOptionDTO;
import com.sh.sh.pos.system.repository.productoptionrepository.ProductOptionRepository;
import com.sh.sh.pos.system.repository.productrepository.ProductRepository;
import com.sh.sh.pos.system.service.productservice.ProductOptionService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ProductOptionServiceImpl implements ProductOptionService {

    private final ProductOptionRepository optionRepository;
    private final ProductRepository productRepository;

    @Override
    @Transactional
    public ProductOptionDTO create(ProductOptionDTO productOptionDTO) {
        Product product = productRepository.findById(productOptionDTO.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Product not found with id: " + productOptionDTO.getProductId()));

        ProductOption option = ProductOptionMapper.toEntity(productOptionDTO, product);

        return ProductOptionMapper.toDTO(optionRepository.save(option));
    }

    @Override
    @Transactional
    public ProductOptionDTO update(Long id, ProductOptionDTO productOptionDTO) {
        ProductOption option = findOptionOrThrow(id);

        option.setName(productOptionDTO.getName());

        return ProductOptionMapper.toDTO(optionRepository.save(option));
    }

    @Override
    @Transactional
    public void delete(Long id) {
        optionRepository.delete(findOptionOrThrow(id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductOptionDTO> findByProduct(Long productId) {
        return optionRepository.findByProductId(productId)
                .stream()
                .map(ProductOptionMapper::toDTO)
                .toList();
    }

    // ── Helpers ───────────────────────────────────────────────────────────

    private ProductOption findOptionOrThrow(Long id) {
        return optionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Option not found with id: " + id));
    }
}