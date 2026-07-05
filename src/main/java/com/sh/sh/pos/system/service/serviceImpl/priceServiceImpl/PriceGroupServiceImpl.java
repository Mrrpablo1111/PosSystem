package com.sh.sh.pos.system.service.serviceimpl.priceserviceimpl;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sh.sh.pos.system.exception.ResourceNotFoundException;
import com.sh.sh.pos.system.exception.UserException;
import com.sh.sh.pos.system.mapper.pricemapper.PriceGroupMapper;
import com.sh.sh.pos.system.model.price.PriceGroup;
import com.sh.sh.pos.system.payload.dto.pricedto.PriceGroupDTO;
import com.sh.sh.pos.system.repository.pricerepository.PriceGroupRepository;
import com.sh.sh.pos.system.service.priceservice.PriceGroupService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PriceGroupServiceImpl implements PriceGroupService {

    private final PriceGroupRepository priceGroupRepository;

    @Override
    @Transactional
    public PriceGroupDTO create(PriceGroupDTO dto) {
        if (priceGroupRepository.existsByNameIgnoreCase(dto.getName())) {
            throw new UserException("Price group already exists: " + dto.getName());
        }

        return PriceGroupMapper.toDTO(
                priceGroupRepository.save(PriceGroupMapper.toEntity(dto)));
    }

    @Override
    @Transactional
    public PriceGroupDTO update(Long id, PriceGroupDTO dto) {
        PriceGroup group = findPriceGroupOrThrow(id);

        group.setName(dto.getName());
        group.setDescription(dto.getDescription());

        if (dto.getActive() != null) {
            group.setActive(dto.getActive());
        }

        return PriceGroupMapper.toDTO(priceGroupRepository.save(group));
    }

    @Override
    @Transactional(readOnly = true)
    public PriceGroupDTO get(Long id) {
        return PriceGroupMapper.toDTO(findPriceGroupOrThrow(id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<PriceGroupDTO> getAll() {
        return priceGroupRepository.findAll()
                .stream()
                .map(PriceGroupMapper::toDTO)
                .toList();
    }

    @Override
    @Transactional
    public void delete(Long id) {
        priceGroupRepository.delete(findPriceGroupOrThrow(id));
    }

    // ── Helpers ───────────────────────────────────────────────────────────

    private PriceGroup findPriceGroupOrThrow(Long id) {
        return priceGroupRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Price group not found with id: " + id));
    }
}