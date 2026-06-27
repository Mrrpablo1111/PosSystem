package com.sh.sh.pos.system.service.serviceImpl.priceServiceImpl;



import java.util.List;

import org.springframework.stereotype.Service;

import com.sh.sh.pos.system.mapper.priceMapper.PriceGroupMapper;
import com.sh.sh.pos.system.model.price.PriceGroup;
import com.sh.sh.pos.system.payload.dto.priceDTO.PriceGroupDTO;
import com.sh.sh.pos.system.repository.priceRepository.PriceGroupRepository;
import com.sh.sh.pos.system.service.priceService.PriceGroupService;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PriceGroupServiceImpl implements PriceGroupService{
    private final PriceGroupRepository priceGroupRepository;

    @Override
    public PriceGroupDTO create(PriceGroupDTO dto) {
        if (priceGroupRepository.existsByNameIgnoreCase(dto.getName())) {
            throw new IllegalArgumentException("Price group already exists");
        }

        return PriceGroupMapper.toDTO(
                priceGroupRepository.save(PriceGroupMapper.toEntity(dto))
        );
    }

    @Override
    public PriceGroupDTO update(Long id, PriceGroupDTO dto) {
        PriceGroup group = priceGroupRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Price group not found"));

        group.setName(dto.getName());
        group.setDescription(dto.getDescription());

        if (dto.getActive() != null) {
            group.setActive(dto.getActive());
        }

        return PriceGroupMapper.toDTO(priceGroupRepository.save(group));
    }

    @Override
    public PriceGroupDTO get(Long id) {
        return priceGroupRepository.findById(id)
                .map(PriceGroupMapper::toDTO)
                .orElseThrow(() -> new EntityNotFoundException("Price group not found"));
    }

    @Override
    public List<PriceGroupDTO> getAll() {
        return priceGroupRepository.findAll()
                .stream()
                .map(PriceGroupMapper::toDTO)
                .toList();
    }

    @Override
    public void delete(Long id) {
        PriceGroup group = priceGroupRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Price group not found"));

        priceGroupRepository.delete(group);
    }
    
}
