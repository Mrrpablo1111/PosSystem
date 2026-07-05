package com.sh.sh.pos.system.service.serviceimpl.suppliersserviceimpl;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sh.sh.pos.system.domain.SupplierStatus;
import com.sh.sh.pos.system.exception.ResourceNotFoundException;
import com.sh.sh.pos.system.exception.UserException;
import com.sh.sh.pos.system.mapper.suppliersmapper.SupplierMapper;
import com.sh.sh.pos.system.model.store.Store;
import com.sh.sh.pos.system.model.suppliers.Supplier;
import com.sh.sh.pos.system.payload.dto.suppliersdto.SupplierDTO;
import com.sh.sh.pos.system.repository.storerepository.StoreRepository;
import com.sh.sh.pos.system.repository.suppliersrepository.SupplierRepository;
import com.sh.sh.pos.system.service.suppliersservice.SupplierService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SupplierServiceImpl implements SupplierService {

    private final SupplierRepository supplierRepository;
    private final StoreRepository storeRepository;

    @Override
    @Transactional
    public SupplierDTO create(SupplierDTO dto) {

        Store store = storeRepository.findById(dto.getStoreId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Store not found with id: " + dto.getStoreId()));

        if (supplierRepository.existsByCode(dto.getCode())) {
            throw new UserException("Supplier code already exists: " + dto.getCode());
        }

        Supplier supplier = Supplier.builder()
                .code(dto.getCode())
                .name(dto.getName())
                .phone(dto.getPhone())
                .email(dto.getEmail())
                .address(dto.getAddress())
                .taxNumber(dto.getTaxNumber())
                .contactPerson(dto.getContactPerson())
                .country(dto.getCountry())
                .paymentTermDays(dto.getPaymentTermDays())
                .creditLimit(dto.getCreditLimit())
                .status(SupplierStatus.ACTIVE)
                .store(store)
                .build();

        return SupplierMapper.toDTO(supplierRepository.save(supplier));
    }

    @Override
    @Transactional
    public SupplierDTO update(Long id, SupplierDTO dto) {

        Supplier supplier = findSupplierOrThrow(id);

        supplier.setName(dto.getName());
        supplier.setPhone(dto.getPhone());
        supplier.setEmail(dto.getEmail());
        supplier.setAddress(dto.getAddress());
        supplier.setTaxNumber(dto.getTaxNumber());
        supplier.setContactPerson(dto.getContactPerson());
        supplier.setCountry(dto.getCountry());
        supplier.setPaymentTermDays(dto.getPaymentTermDays());
        supplier.setCreditLimit(dto.getCreditLimit());

        return SupplierMapper.toDTO(supplierRepository.save(supplier));
    }

    @Override
    @Transactional(readOnly = true)
    public SupplierDTO getById(Long id) {
        return SupplierMapper.toDTO(findSupplierOrThrow(id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<SupplierDTO> getByStore(Long storeId) {
        return supplierRepository.findByStoreId(storeId)
                .stream()
                .map(SupplierMapper::toDTO)
                .toList();
    }

    @Override
    @Transactional
    public void inactive(Long id) {
        setStatus(id, SupplierStatus.INACTIVE);
    }

    @Override
    @Transactional
    public void activate(Long id) {
        setStatus(id, SupplierStatus.ACTIVE);
    }

    // ── Helpers ───────────────────────────────────────────────────────────

    private Supplier findSupplierOrThrow(Long id) {
        return supplierRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Supplier not found with id: " + id));
    }

    private void setStatus(Long id, SupplierStatus status) {
        Supplier supplier = findSupplierOrThrow(id);
        supplier.setStatus(status);
        supplierRepository.save(supplier);
    }
}