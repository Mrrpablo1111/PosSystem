package com.sh.sh.pos.system.service.serviceImpl.suppliersServiceImpl;

import java.util.List;

import org.springframework.stereotype.Service;

import com.sh.sh.pos.system.domain.SupplierStatus;
import com.sh.sh.pos.system.mapper.suppliersMapper.SupplierMapper;
import com.sh.sh.pos.system.model.Store;
import com.sh.sh.pos.system.model.suppliers.Supplier;
import com.sh.sh.pos.system.payload.dto.suppliersDTO.SupplierDTO;
import com.sh.sh.pos.system.repository.StoreRepository;
import com.sh.sh.pos.system.repository.suppliersRepository.SupplierRepository;
import com.sh.sh.pos.system.service.suppliersService.SupplierService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SupplierServiceImpl implements SupplierService {

    private final SupplierRepository supplierRepository;
    private final StoreRepository storeRepository;

    @Override
    public SupplierDTO create(SupplierDTO dto) {

        Store store = storeRepository.findById(dto.getStoreId())
                .orElseThrow(() -> new RuntimeException("Store not found"));

        if (supplierRepository.existsByCode(dto.getCode())) {
            throw new RuntimeException("Supplier code already exists");
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
    public SupplierDTO update(Long id, SupplierDTO dto) {

        Supplier supplier = supplierRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Supplier not found"));

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
    public SupplierDTO getById(Long id) {

        Supplier supplier = supplierRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Supplier not found"));

        return SupplierMapper.toDTO(supplier);
    }

    @Override
    public List<SupplierDTO> getByStore(Long storeId) {
        return supplierRepository.findByStoreId(storeId)
                .stream()
                .map(SupplierMapper::toDTO)
                .toList();
    }

    @Override
    public void inactive(Long id) {

        Supplier supplier = supplierRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Supplier not found"));

        supplier.setStatus(SupplierStatus.INACTIVE);

        supplierRepository.save(supplier);
    }

    @Override
    public void activate(Long id) {

        Supplier supplier = supplierRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Supplier not found"));

        supplier.setStatus(SupplierStatus.ACTIVE);

        supplierRepository.save(supplier);
    }
}
