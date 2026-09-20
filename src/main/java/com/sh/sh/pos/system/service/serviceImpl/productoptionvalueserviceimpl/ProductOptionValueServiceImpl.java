package com.sh.sh.pos.system.service.serviceimpl.productoptionvalueserviceimpl;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sh.sh.pos.system.exception.ResourceNotFoundException;
import com.sh.sh.pos.system.exception.UserException;
import com.sh.sh.pos.system.mapper.productoptionvaluemapper.ProductOptionValueMapper;
import com.sh.sh.pos.system.model.products.ProductOption;
import com.sh.sh.pos.system.model.products.ProductOptionValue;
import com.sh.sh.pos.system.payload.dto.productdto.ProductOptionValueDTO;
import com.sh.sh.pos.system.repository.productoptionrepository.ProductOptionRepository;
import com.sh.sh.pos.system.repository.productoptionvaluerepository.ProductOptionValueRepository;
import com.sh.sh.pos.system.service.productservice.ProductOptionValueService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ProductOptionValueServiceImpl implements ProductOptionValueService {

    private static final String VALUE_EXISTS = "Value already exists.";

    private final ProductOptionRepository optionRepository;
    private final ProductOptionValueRepository valueRepository;

    @Override
    @Transactional
    public ProductOptionValueDTO create(ProductOptionValueDTO dto) {
        ProductOption option = findOptionOrThrow(dto.getOptionId());

        if (valueRepository.existsByOptionIdAndValue(dto.getOptionId(), dto.getValue())) {
            throw new UserException(VALUE_EXISTS);
        }

        ProductOptionValue value = ProductOptionValueMapper.toEntity(dto, option);

        return ProductOptionValueMapper.toDTO(valueRepository.save(value));
    }

    @Override
    @Transactional
    public ProductOptionValueDTO update(Long id, ProductOptionValueDTO dto) {
        ProductOptionValue value = findValueOrThrow(id);

        if (!value.getValue().equalsIgnoreCase(dto.getValue())
                && valueRepository.existsByOptionIdAndValue(dto.getOptionId(), dto.getValue())) {
            throw new UserException(VALUE_EXISTS);
        }

        value.setValue(dto.getValue());

        // If the option changes
        if (dto.getOptionId() != null
                && !value.getOption().getId().equals(dto.getOptionId())) {
            value.setOption(findOptionOrThrow(dto.getOptionId()));
        }

        return ProductOptionValueMapper.toDTO(valueRepository.save(value));
    }

    @Override
    @Transactional
    public void delete(Long id) {
        valueRepository.delete(findValueOrThrow(id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductOptionValueDTO> findByOption(Long optionId) {
        return valueRepository.findByOptionId(optionId)
                .stream()
                .map(ProductOptionValueMapper::toDTO)
                .toList();
    }

    // ── Helpers ───────────────────────────────────────────────────────────

    private ProductOption findOptionOrThrow(Long optionId) {
        return optionRepository.findById(optionId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Option not found with id: " + optionId));
    }

    private ProductOptionValue findValueOrThrow(Long id) {
        return valueRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Option value not found with id: " + id));
    }
}