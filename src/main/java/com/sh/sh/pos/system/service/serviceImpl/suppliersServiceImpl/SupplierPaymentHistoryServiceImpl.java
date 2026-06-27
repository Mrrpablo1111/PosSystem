package com.sh.sh.pos.system.service.serviceImpl.suppliersServiceImpl;

import java.util.List;

import org.springframework.stereotype.Service;

import com.sh.sh.pos.system.mapper.suppliersMapper.SupplierPaymentHistoryMapper;
import com.sh.sh.pos.system.payload.dto.suppliersDTO.SupplierPaymentHistoryDTO;
import com.sh.sh.pos.system.repository.suppliersRepository.SupplierPaymentRepository;
import com.sh.sh.pos.system.service.suppliersService.SupplierPaymentHistoryService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SupplierPaymentHistoryServiceImpl
        implements SupplierPaymentHistoryService {

    private final SupplierPaymentRepository paymentRepository;

    @Override
    public List<SupplierPaymentHistoryDTO> getBySupplier(
            Long supplierId) {

        return paymentRepository.findBySupplierId(supplierId)
                .stream()
                .map(SupplierPaymentHistoryMapper::toDTO)
                .toList();
    }

    @Override
    public List<SupplierPaymentHistoryDTO> getByInvoice(
            Long invoiceId) {

        return paymentRepository.findByPurchaseInvoiceId(invoiceId)
                .stream()
                .map(SupplierPaymentHistoryMapper::toDTO)
                .toList();
    }
}
