package com.sh.sh.pos.system.mapper;

import com.sh.sh.pos.system.model.Customer;
import com.sh.sh.pos.system.model.price.PriceGroup;
import com.sh.sh.pos.system.payload.dto.CustomerDTO;

public class CustomerMapper {

    public static CustomerDTO toDTO(Customer customer) {

        return CustomerDTO.builder()
                .id(customer.getId())
                .fullName(customer.getFullName())
                .email(customer.getEmail())
                .phone(customer.getPhone())

                .priceGroupId(
                        customer.getPriceGroup() != null
                                ? customer.getPriceGroup().getId()
                                : null)

                .priceGroupName(
                        customer.getPriceGroup() != null
                                ? customer.getPriceGroup().getName()
                                : null)

                .createdAt(customer.getCreatedAt())
                .updatedAt(customer.getUpdatedAt())
                .build();
    }

    public static Customer toEntity(
            CustomerDTO dto,
            PriceGroup priceGroup) {

        return Customer.builder()
                .id(dto.getId())
                .fullName(dto.getFullName())
                .email(dto.getEmail())
                .phone(dto.getPhone())
                .priceGroup(priceGroup)
                .build();
    }
}