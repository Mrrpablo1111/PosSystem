package com.sh.sh.pos.system.service.serviceimpl.employeeserviceimpl;

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
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.sh.sh.pos.system.domain.UserRole;
import com.sh.sh.pos.system.exception.ResourceNotFoundException;
import com.sh.sh.pos.system.exception.UserException;
import com.sh.sh.pos.system.model.branch.Branch;
import com.sh.sh.pos.system.model.store.Store;
import com.sh.sh.pos.system.model.user.User;
import com.sh.sh.pos.system.payload.dto.userdto.UserDTO;
import com.sh.sh.pos.system.repository.branchrepository.BranchRepository;
import com.sh.sh.pos.system.repository.storerepository.StoreRepository;
import com.sh.sh.pos.system.repository.userrepository.UserRepository;

@ExtendWith(MockitoExtension.class)
@DisplayName("EmployeeServiceImpl — role rules, duplicate-email upsert, CRUD")
class EmployeeServiceImplTest {

    @Mock private StoreRepository storeRepository;
    @Mock private BranchRepository branchRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private UserRepository userRepository;

    @InjectMocks
    private EmployeeServiceImpl employeeService;

    private Store store;
    private Branch branch;
    private User savedUser;

    @BeforeEach
    void setUp() {
        store = Store.builder().id(10L).brand("Test Store").build();

        branch = new Branch();
        branch.setId(7L);
        branch.setName("Main Branch");
        branch.setStore(store);

        savedUser = new User();
        savedUser.setId(50L);
        savedUser.setEmail("emp@shpos.com");
        savedUser.setRole(UserRole.ROLE_BRANCH_CASHIER);
    }

    private UserDTO storeEmployeeDto(UserRole role, Long branchId) {
        UserDTO dto = new UserDTO();
        dto.setFullName("Sok Dara");
        dto.setEmail("emp@shpos.com");
        dto.setPassword("plain-pw");
        dto.setPhone("012 345 678");
        dto.setRole(role);
        dto.setBranchId(branchId);
        return dto;
    }

    private User branchEmployee(UserRole role) {
        User u = new User();
        u.setFullName("Sok Dara");
        u.setEmail("emp@shpos.com");
        u.setPassword("plain-pw");
        u.setRole(role);
        return u;
    }

    // ── createStoreEmployee ───────────────────────────────────────────────

    @Test
    @DisplayName("createStoreEmployee → new email → saved with encoded password ✅")
    void createStoreEmployee_newEmail_saved() {
        when(storeRepository.findById(10L)).thenReturn(Optional.of(store));
        when(passwordEncoder.encode("plain-pw")).thenReturn("$2a$10$encoded");
        when(userRepository.findByEmail("emp@shpos.com")).thenReturn(null);
        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        UserDTO result = employeeService.createStoreEmployee(
                storeEmployeeDto(UserRole.ROLE_BRANCH_CASHIER, null), 10L);

        assertThat(result).isNotNull();

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        assertThat(captor.getValue().getPassword()).isEqualTo("$2a$10$encoded");
        assertThat(captor.getValue().getStore()).isEqualTo(store);
        assertThat(captor.getValue().getId()).isNull(); // new user → no id → insert
    }

    @Test
    @DisplayName("createStoreEmployee → existing email → ENTITY gets existing id (regression) ✅")
    void createStoreEmployee_existingEmail_upsertsById() {
        User existing = new User();
        existing.setId(77L);
        existing.setEmail("emp@shpos.com");

        when(storeRepository.findById(10L)).thenReturn(Optional.of(store));
        when(passwordEncoder.encode("plain-pw")).thenReturn("$2a$10$encoded");
        when(userRepository.findByEmail("emp@shpos.com")).thenReturn(existing);
        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        employeeService.createStoreEmployee(
                storeEmployeeDto(UserRole.ROLE_BRANCH_CASHIER, null), 10L);

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        // THE regression assertion: the saved ENTITY carries the existing id,
        // so JPA updates instead of violating the unique email constraint
        assertThat(captor.getValue().getId()).isEqualTo(77L);
    }

    @Test
    @DisplayName("createStoreEmployee → branch manager without branchId → UserException ❌")
    void createStoreEmployee_managerWithoutBranch_rejected() {
        when(storeRepository.findById(10L)).thenReturn(Optional.of(store));

        assertThatThrownBy(() -> employeeService.createStoreEmployee(
                storeEmployeeDto(UserRole.ROLE_BRANCH_MANAGER, null), 10L))
                .isInstanceOf(UserException.class)
                .hasMessageContaining("Branch id is required");

        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("createStoreEmployee → branch manager with branch → manager assigned to branch ✅")
    void createStoreEmployee_manager_assignedToBranch() {
        when(storeRepository.findById(10L)).thenReturn(Optional.of(store));
        when(branchRepository.findById(7L)).thenReturn(Optional.of(branch));
        when(passwordEncoder.encode("plain-pw")).thenReturn("$2a$10$encoded");
        when(userRepository.findByEmail("emp@shpos.com")).thenReturn(null);
        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        employeeService.createStoreEmployee(
                storeEmployeeDto(UserRole.ROLE_BRANCH_MANAGER, 7L), 10L);

        assertThat(branch.getManager()).isEqualTo(savedUser);
        verify(branchRepository).save(branch);
    }

    @Test
    @DisplayName("createStoreEmployee → unknown store → 404 ❌")
    void createStoreEmployee_storeNotFound() {
        when(storeRepository.findById(10L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> employeeService.createStoreEmployee(
                storeEmployeeDto(UserRole.ROLE_BRANCH_CASHIER, null), 10L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Store not found");
    }

    // ── createBranchEmployee ──────────────────────────────────────────────

    @Test
    @DisplayName("createBranchEmployee → cashier role → saved ✅")
    void createBranchEmployee_cashier_saved() {
        when(branchRepository.findById(7L)).thenReturn(Optional.of(branch));
        when(passwordEncoder.encode("plain-pw")).thenReturn("$2a$10$encoded");
        when(userRepository.findByEmail("emp@shpos.com")).thenReturn(null);
        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        User result = employeeService.createBranchEmployee(
                branchEmployee(UserRole.ROLE_BRANCH_CASHIER), 7L);

        assertThat(result).isNotNull();
    }

    @Test
    @DisplayName("createBranchEmployee → invalid role (STORE_ADMIN) → UserException ❌")
    void createBranchEmployee_invalidRole_rejected() {
        when(branchRepository.findById(7L)).thenReturn(Optional.of(branch));

        assertThatThrownBy(() -> employeeService.createBranchEmployee(
                branchEmployee(UserRole.ROLE_STORE_ADMIN), 7L))
                .isInstanceOf(UserException.class)
                .hasMessageContaining("Invalid role");

        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("createBranchEmployee → unknown branch → 404 ❌")
    void createBranchEmployee_branchNotFound() {
        when(branchRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> employeeService.createBranchEmployee(
                branchEmployee(UserRole.ROLE_BRANCH_CASHIER), 99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Branch not found");
    }

    // ── updateEmployee ────────────────────────────────────────────────────

    @Test
    @DisplayName("updateEmployee → only non-null fields applied, password untouched ✅")
    void updateEmployee_partialUpdate() {
        User existing = new User();
        existing.setId(50L);
        existing.setFullName("Old Name");
        existing.setEmail("old@shpos.com");
        existing.setPassword("$2a$10$keepme");

        when(userRepository.findById(50L)).thenReturn(Optional.of(existing));
        when(userRepository.save(any(User.class))).thenReturn(existing);

        User changes = new User();
        changes.setFullName("New Name");
        // email, phone, role left null

        employeeService.updateEmployee(50L, changes);

        assertThat(existing.getFullName()).isEqualTo("New Name");
        assertThat(existing.getEmail()).isEqualTo("old@shpos.com");   // untouched
        assertThat(existing.getPassword()).isEqualTo("$2a$10$keepme"); // never updated here
    }

    @Test
    @DisplayName("updateEmployee → unknown id → 404 ❌")
    void updateEmployee_notFound() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> employeeService.updateEmployee(99L, new User()))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Employee not found");
    }

    // ── delete / find ─────────────────────────────────────────────────────

    @Test
    @DisplayName("deleteEmployee → deletes the loaded entity ✅")
    void deleteEmployee_found() {
        when(userRepository.findById(50L)).thenReturn(Optional.of(savedUser));

        employeeService.deleteEmployee(50L);

        verify(userRepository).delete(savedUser);
    }

    @Test
    @DisplayName("findByBranchEmployees → role filter applied ✅")
    void findByBranchEmployees_filtersByRole() {
        User cashier = new User();
        cashier.setId(1L);
        cashier.setRole(UserRole.ROLE_BRANCH_CASHIER);
        User manager = new User();
        manager.setId(2L);
        manager.setRole(UserRole.ROLE_BRANCH_MANAGER);

        when(branchRepository.findById(7L)).thenReturn(Optional.of(branch));
        when(userRepository.findByBranchId(7L)).thenReturn(List.of(cashier, manager));

        List<User> result = employeeService.findByBranchEmployees(7L, UserRole.ROLE_BRANCH_CASHIER);

        assertThat(result).containsExactly(cashier);
    }

    @Test
    @DisplayName("findByBranchEmployees → null role → all returned ✅")
    void findByBranchEmployees_nullRole_returnsAll() {
        User cashier = new User();
        cashier.setRole(UserRole.ROLE_BRANCH_CASHIER);
        User manager = new User();
        manager.setRole(UserRole.ROLE_BRANCH_MANAGER);

        when(branchRepository.findById(7L)).thenReturn(Optional.of(branch));
        when(userRepository.findByBranchId(7L)).thenReturn(List.of(cashier, manager));

        assertThat(employeeService.findByBranchEmployees(7L, null)).hasSize(2);
    }
}