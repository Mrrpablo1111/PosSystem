package com.sh.sh.pos.system.service.serviceImpl;

import java.util.List;

import org.springframework.stereotype.Service;

import com.sh.sh.pos.system.mapper.ProductOptionValueMapper;
import com.sh.sh.pos.system.model.products.ProductOption;
import com.sh.sh.pos.system.model.products.ProductOptionValue;
import com.sh.sh.pos.system.payload.dto.productDTO.ProductOptionValueDTO;
import com.sh.sh.pos.system.repository.ProductOptionRepository;
import com.sh.sh.pos.system.repository.ProductOptionValueRepository;
import com.sh.sh.pos.system.service.ProductService.ProductOptionValueService;

import jakarta.persistence.EntityNotFoundException;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ProductOptionValueServiceImpl implements ProductOptionValueService {
    private final ProductOptionRepository optionRepository;
    private final ProductOptionValueRepository valueRepository;

    @Override
    public ProductOptionValueDTO create(ProductOptionValueDTO dto) {
        ProductOption option = optionRepository.findById(dto.getOptionId())
                .orElseThrow(() -> new EntityNotFoundException("Option not found"));

        if (valueRepository.existsByOptionIdAndValue(
                dto.getOptionId(),
                dto.getValue())) {

            throw new IllegalArgumentException("Value already exists.");
        }

        ProductOptionValue value = ProductOptionValueMapper.toEntity(dto, option);

        return ProductOptionValueMapper.toDTO(
                valueRepository.save(value));
    }

    @Override
    public ProductOptionValueDTO update(Long id, ProductOptionValueDTO dto) {
        ProductOptionValue value = valueRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Option value not found"));

        if (!value.getValue().equalsIgnoreCase(dto.getValue())
                && valueRepository.existsByOptionIdAndValue(
                        dto.getOptionId(),
                        dto.getValue())) {

            throw new IllegalArgumentException("Value already exists.");
        }

        // Update the value
        value.setValue(dto.getValue());

        // If the option changes
        if (dto.getOptionId() != null &&
                !value.getOption().getId().equals(dto.getOptionId())) {

            ProductOption option = optionRepository.findById(dto.getOptionId())
                    .orElseThrow(() -> new EntityNotFoundException("Option not found"));

            value.setOption(option);
        }

        return ProductOptionValueMapper.toDTO(
                valueRepository.save(value));
    }

    @Override
    public void delete(Long id) {
        ProductOptionValue value = valueRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Option value not found"));

        valueRepository.delete(value);
    }

    @Override
    public List<ProductOptionValueDTO> findByOption(Long optionId) {
        return valueRepository.findByOptionId(optionId)
                .stream()
                .map(ProductOptionValueMapper::toDTO)
                .toList();
    }

}
