package com.sh.sh.pos.system.service.serviceimpl.customerserviceimpl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.sh.sh.pos.system.exception.ResourceNotFoundException;
import com.sh.sh.pos.system.model.customer.Customer;
import com.sh.sh.pos.system.model.price.PriceGroup;
import com.sh.sh.pos.system.payload.dto.customerdto.CustomerDTO;
import com.sh.sh.pos.system.repository.customerrepository.CustomerRepository;
import com.sh.sh.pos.system.repository.pricerepository.PriceGroupRepository;

@ExtendWith(MockitoExtension.class)
@DisplayName("CustomerServiceImpl — price-group resolution and CRUD")
class CustomerServiceImplTest {

    @Mock private CustomerRepository customerRepository;
    @Mock private PriceGroupRepository priceGroupRepository;

    @InjectMocks
    private CustomerServiceImpl customerService;

    private Customer customer;
    private PriceGroup vipGroup;

    @BeforeEach
    void setUp() {
        vipGroup = new PriceGroup();
        vipGroup.setId(10L);
        vipGroup.setName("VIP");

        customer = new Customer();
        customer.setId(1L);
        customer.setFullName("Dara Sok");
        customer.setEmail("dara@example.com");
        customer.setPhone("012 345 678");
        customer.setPriceGroup(vipGroup);
    }

    private CustomerDTO dto(Long priceGroupId) {
        CustomerDTO dto = new CustomerDTO();
        dto.setFullName("Dara Sok");
        dto.setEmail("dara@example.com");
        dto.setPhone("012 345 678");
        dto.setPriceGroupId(priceGroupId);
        return dto;
    }

    // ── createCustomer ────────────────────────────────────────────────────

    @Test
    @DisplayName("create → without price group → saved with null group ✅")
    void create_withoutPriceGroup() {
        when(customerRepository.save(any(Customer.class))).thenReturn(customer);

        assertThat(customerService.createCustomer(dto(null))).isNotNull();

        verify(priceGroupRepository, never()).findById(any());
        verify(customerRepository).save(any(Customer.class));
    }

    @Test
    @DisplayName("create → with valid price group → group resolved and saved ✅")
    void create_withPriceGroup() {
        when(priceGroupRepository.findById(10L)).thenReturn(Optional.of(vipGroup));
        when(customerRepository.save(any(Customer.class))).thenReturn(customer);

        assertThat(customerService.createCustomer(dto(10L))).isNotNull();
        verify(priceGroupRepository).findById(10L);
    }

    @Test
    @DisplayName("create → unknown price group → 404, nothing saved ❌")
    void create_priceGroupNotFound() {
        when(priceGroupRepository.findById(10L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> customerService.createCustomer(dto(10L)))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Price group not found");

        verify(customerRepository, never()).save(any());
    }

    // ── updateCustomer ────────────────────────────────────────────────────

    @Test
    @DisplayName("update → fields applied ✅")
    void update_appliesFields() {
        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));
        when(priceGroupRepository.findById(10L)).thenReturn(Optional.of(vipGroup));
        when(customerRepository.save(any(Customer.class))).thenReturn(customer);

        CustomerDTO changes = dto(10L);
        changes.setFullName("Dara S.");

        customerService.updateCustomer(1L, changes);

        assertThat(customer.getFullName()).isEqualTo("Dara S.");
        verify(customerRepository).save(customer);
    }

    @Test
    @DisplayName("update → null priceGroupId → group cleared ✅")
    void update_clearsPriceGroup() {
        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));
        when(customerRepository.save(any(Customer.class))).thenReturn(customer);

        customerService.updateCustomer(1L, dto(null));

        assertThat(customer.getPriceGroup()).isNull();
        verify(priceGroupRepository, never()).findById(any());
    }

    @Test
    @DisplayName("update → unknown customer → 404 ❌")
    void update_notFound() {
        when(customerRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> customerService.updateCustomer(99L, dto(null)))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Customer not found");
    }

    // ── delete / get / search ─────────────────────────────────────────────

    @Test
    @DisplayName("delete → deletes the loaded entity ✅")
    void delete_found() {
        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));

        customerService.deleteCustomer(1L);

        verify(customerRepository).delete(customer);
    }

    @Test
    @DisplayName("delete → unknown id → 404, nothing deleted ❌")
    void delete_notFound() {
        when(customerRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> customerService.deleteCustomer(99L))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(customerRepository, never()).delete(any());
    }

    @Test
    @DisplayName("getCustomerById → found → DTO returned ✅")
    void getById_found() {
        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));

        assertThat(customerService.getCustomerById(1L)).isNotNull();
    }

    @Test
    @DisplayName("searchCustomers → keyword hits name or email ✅")
    void search_maps() {
        when(customerRepository
                .findByFullNameContainingIgnoreCaseOrEmailContainingIgnoreCase("dara", "dara"))
                .thenReturn(List.of(customer));

        assertThat(customerService.searchCustomers("dara")).hasSize(1);
    }

    @Test
    @DisplayName("getAllCustomers → maps all ✅")
    void getAll_maps() {
        when(customerRepository.findAll()).thenReturn(List.of(customer));

        assertThat(customerService.getAllCustomers()).hasSize(1);
    }
}