package com.sh.sh.pos.system.service.serviceimpl.storeserviceimpl;

import java.util.List;
import java.util.Locale;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sh.sh.pos.system.exception.ResourceNotFoundException;
import com.sh.sh.pos.system.exceptions.UserException;
import com.sh.sh.pos.system.mapper.storemapper.StoreTypeMapper;
import com.sh.sh.pos.system.model.store.StoreType;
import com.sh.sh.pos.system.payload.dto.storedto.StoreTypeDTO;
import com.sh.sh.pos.system.repository.storetyperepository.StoreTypeRepository;
import com.sh.sh.pos.system.service.storeservice.StoreTypeService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class StoreTypeServiceImpl implements StoreTypeService {

    private final StoreTypeRepository storeTypeRepository;
    private final StoreTypeMapper storeTypeMapper;

    @Override
    public StoreTypeDTO createStoreType(StoreTypeDTO storeTypeDTO) throws UserException {
        if (storeTypeDTO == null) {
            throw new UserException("Store type information required");
        }

        String code = normalizeCode(storeTypeDTO.getCode());
        String name = normalizeName(storeTypeDTO.getName());

        if (storeTypeRepository.existsByCode(code)) {
            throw new UserException("Store type code already exists: " + code);
        }

        if (storeTypeRepository.existsByNameIgnoreCase(name)) {
            throw new UserException("Store type name already exists: " + name);
        }

        StoreType storeType = new StoreType();

        storeType.setCode(code);
        storeType.setName(name);
        storeType.setDescription(normalizeDescription(storeTypeDTO.getDescription()));

        storeType.setActive(true);

        StoreType savedStoreType = storeTypeRepository.save(storeType);
        log.info(
                "Store type created: id={}, code={}, name={}",
                savedStoreType.getId(),
                savedStoreType.getCode(),
                savedStoreType.getName());

        return storeTypeMapper.toDTO(savedStoreType);
    }

    @Override
    @Transactional(readOnly = true)
    public StoreTypeDTO getStoreTypeById(Long id) throws UserException {
        StoreType storeType = findStoreTypeOrThrow(id);
        return storeTypeMapper.toDTO(storeType);
    }

    @Override
    @Transactional(readOnly = true)
    public List<StoreTypeDTO> getAllStoreTypes() {
        return storeTypeRepository.findAllByOrderByNameAsc()
                .stream()
                .map(storeTypeMapper::toDTO)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<StoreTypeDTO> getActiveStoreTypes() {
        return storeTypeRepository.findByIsActiveTrueOrderByNameAsc().stream().map(storeTypeMapper::toDTO).toList();
    }

    @Override
    public StoreTypeDTO updateStoreType(Long id, StoreTypeDTO storeTypeDTO) throws UserException {
        if(storeTypeDTO == null){
            throw new UserException("Store inforemation is required");
        }

        StoreType existingStoreType = findStoreTypeOrThrow(id);

        String newCode = normalizeCode(storeTypeDTO.getCode());

        String newName = normalizeName(storeTypeDTO.getName());
        // Check code only if it has changed
        if (!newCode.equals(existingStoreType.getCode())
                && storeTypeRepository.existsByCode(newCode)) {

            throw new UserException(
                    "Store type code already exists: " + newCode
            );
        }

        // Check name only if it has changed
        if (!newName.equalsIgnoreCase(existingStoreType.getName())
                && storeTypeRepository.existsByNameIgnoreCase(newName)) {

            throw new UserException(
                    "Store type name already exists: " + newName
            );
        }

        existingStoreType.setCode(newCode);
        existingStoreType.setName(newName);

        existingStoreType.setDescription(
                normalizeDescription(storeTypeDTO.getDescription())
        );

        // Do not update the active status here.
        // Use activateStoreType() or deactivateStoreType().

        StoreType savedStoreType =
                storeTypeRepository.save(existingStoreType);

        log.info(
                "Store type updated: id={}, code={}",
                savedStoreType.getId(),
                savedStoreType.getCode()
        );

        return storeTypeMapper.toDTO(savedStoreType);
    }

    @Override
    public StoreTypeDTO deactivateStoreType(Long id) throws UserException {
        StoreType storeType = findStoreTypeOrThrow(id);

        if(storeType.isActive()){
            return storeTypeMapper.toDTO(storeType);
        }

        storeType.setActive(false);

        StoreType savedStoreType = storeTypeRepository.save(storeType);

        log.info("Store type deactivated: id={}, code={}", savedStoreType.getId(), savedStoreType.getCode());

        return storeTypeMapper.toDTO(savedStoreType);
    }

    @Override
    public StoreTypeDTO activateStoreType(Long id) throws UserException {
        StoreType storeType = findStoreTypeOrThrow(id);

        if(storeType.isActive()){
            return storeTypeMapper.toDTO(storeType);
        }

        storeType.setActive(true);

        StoreType savedStoreType = storeTypeRepository.save(storeType);

        log.info("Store type activated: id={}, code={}", savedStoreType.getId(), savedStoreType.getCode());

        return storeTypeMapper.toDTO(savedStoreType);
    }

    // NormalizeCode

    private String normalizeCode(String code) throws UserException {

        if (code == null || code.isBlank()) {
            throw new UserException("Store type code required");
        }

        String normalizedCode = code.trim().toUpperCase(Locale.ROOT);
        if (!normalizedCode.matches(
                "[A-Z][A-Z0-9_]*")) {

            throw new UserException(
                    "Invalid store type code. "
                            + "Use uppercase letters, numbers, and underscores. "
                            + "The code must start with a letter.");
        }
        if (normalizedCode.length() > 50) {
            throw new UserException(
                    "Store type code cannot exceed 50 characters");
        }

        return normalizedCode;
    }

    // NormalizeName

    private String normalizeName(String name) throws UserException {
        if (name == null || name.isBlank()) {
            throw new UserException("Store name is required");
        }

        String normalizedName = name.trim();

        if (normalizedName.length() > 100) {
            throw new UserException(
                    "Store type name cannot exceed 100 characters");
        }
        return normalizedName;
    }

    // NormalizeDescription
    private String normalizeDescription(String description) throws UserException {

        if (description == null || description.isBlank()) {
            return null;
        }

        String normalizeDescription = description.trim();

        if (normalizeDescription.length() > 500) {
            throw new UserException("Description cannot exceed 500 characters");
        }
        return normalizeDescription;
    }

    private StoreType findStoreTypeOrThrow(Long id) throws UserException {

        if (id == null || id <= 0) {
            throw new UserException("Store type ID must be greater then zero");
        }
        return storeTypeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Store type not found with id: " + id));

    }
}
