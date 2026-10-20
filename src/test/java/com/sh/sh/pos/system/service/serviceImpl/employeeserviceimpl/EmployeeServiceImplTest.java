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

     @Mock
    private StoreRepository storeRepository;

    @Mock
    private BranchRepository branchRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private EmployeeServiceImpl employeeService;

    private Store store;
    private Branch branch;
    private User savedUser;

    @BeforeEach
    void setUp() {

        store = Store.builder()
                .id(10L)
                .brand("Test Store")
                .build();

        branch = new Branch();
        branch.setId(7L);
        branch.setName("Main Branch");
        branch.setStore(store);

        savedUser = new User();
        savedUser.setId(50L);
        savedUser.setFullName("Sok Dara");
        savedUser.setEmail("emp@shpos.com");
        savedUser.setRole(UserRole.ROLE_BRANCH_CASHIER);
        savedUser.setStore(store);
        savedUser.setBranch(branch);
    }

    // =========================================================
    // TEST DATA
    // =========================================================

    private UserDTO storeEmployeeDto(
            UserRole role,
            Long branchId) {

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

        User user = new User();

        user.setFullName("Sok Dara");
        user.setEmail("emp@shpos.com");
        user.setPassword("plain-pw");
        user.setPhone("012 345 678");
        user.setRole(role);

        return user;
    }

    // =========================================================
    // CREATE STORE EMPLOYEE
    // =========================================================

    @Test
    @DisplayName(
            "createStoreEmployee -> new email -> employee saved with encoded password"
    )
    void createStoreEmployee_newEmail_saved() {

        // Arrange
        when(storeRepository.findById(10L))
                .thenReturn(Optional.of(store));

        when(userRepository.existsByEmail("emp@shpos.com"))
                .thenReturn(false);

        when(passwordEncoder.encode("plain-pw"))
                .thenReturn("$2a$10$encoded");

        when(userRepository.save(any(User.class)))
                .thenReturn(savedUser);

        // Act
        UserDTO result =
                employeeService.createStoreEmployee(
                        storeEmployeeDto(
                                UserRole.ROLE_BRANCH_CASHIER,
                                null
                        ),
                        10L
                );

        // Assert
        assertThat(result).isNotNull();

        ArgumentCaptor<User> captor =
                ArgumentCaptor.forClass(User.class);

        verify(userRepository).save(captor.capture());

        User capturedUser = captor.getValue();

        assertThat(capturedUser.getId())
                .isNull();

        assertThat(capturedUser.getEmail())
                .isEqualTo("emp@shpos.com");

        assertThat(capturedUser.getPassword())
                .isEqualTo("$2a$10$encoded");

        assertThat(capturedUser.getStore())
                .isEqualTo(store);

        assertThat(capturedUser.getBranch())
                .isNull();

        assertThat(capturedUser.getVerified())
        .isFalse();
    }
    

    // =========================================================
    // CREATE STORE EMPLOYEE - DUPLICATE EMAIL
    // =========================================================

    @Test
    @DisplayName(
            "createStoreEmployee -> duplicate email -> UserException"
    )
    void createStoreEmployee_duplicateEmail_rejected() {

        // Arrange
        when(storeRepository.findById(10L))
                .thenReturn(Optional.of(store));

        when(userRepository.existsByEmail("emp@shpos.com"))
                .thenReturn(true);

        // Act + Assert
        assertThatThrownBy(() ->
                employeeService.createStoreEmployee(
                        storeEmployeeDto(
                                UserRole.ROLE_BRANCH_CASHIER,
                                null
                        ),
                        10L
                )
        )
                .isInstanceOf(UserException.class)
                .hasMessageContaining(
                        "User with email already exists"
                );

        verify(userRepository, never())
                .save(any(User.class));

        verify(passwordEncoder, never())
                .encode(any());
    }

    // =========================================================
    // CREATE STORE EMPLOYEE - MANAGER WITHOUT BRANCH
    // =========================================================

    @Test
    @DisplayName(
            "createStoreEmployee -> branch manager without branchId -> rejected"
    )
    void createStoreEmployee_managerWithoutBranch_rejected() {

        // Arrange
        when(storeRepository.findById(10L))
                .thenReturn(Optional.of(store));

        // Act + Assert
        assertThatThrownBy(() ->
                employeeService.createStoreEmployee(
                        storeEmployeeDto(
                                UserRole.ROLE_BRANCH_MANAGER,
                                null
                        ),
                        10L
                )
        )
                .isInstanceOf(UserException.class)
                .hasMessageContaining(
                        "Branch id is required"
                );

        verify(userRepository, never())
                .save(any(User.class));
    }

    // =========================================================
    // CREATE STORE EMPLOYEE - MANAGER
    // =========================================================

    @Test
    @DisplayName(
            "createStoreEmployee -> branch manager -> branch manager assigned"
    )
    void createStoreEmployee_manager_assignedToBranch() {

        // Arrange
        User manager = new User();
        manager.setId(100L);
        manager.setFullName("Sok Dara");
        manager.setEmail("emp@shpos.com");
        manager.setRole(UserRole.ROLE_BRANCH_MANAGER);

        when(storeRepository.findById(10L))
                .thenReturn(Optional.of(store));

        when(branchRepository.findById(7L))
                .thenReturn(Optional.of(branch));

        when(userRepository.existsByEmail("emp@shpos.com"))
                .thenReturn(false);

        when(passwordEncoder.encode("plain-pw"))
                .thenReturn("$2a$10$encoded");

        when(userRepository.save(any(User.class)))
                .thenReturn(manager);

        // Act
        employeeService.createStoreEmployee(
                storeEmployeeDto(
                        UserRole.ROLE_BRANCH_MANAGER,
                        7L
                ),
                10L
        );

        // Assert
        assertThat(branch.getManager())
                .isEqualTo(manager);

        verify(branchRepository)
                .save(branch);
    }

    // =========================================================
    // CREATE STORE EMPLOYEE - BRANCH NOT FOUND
    // =========================================================

    @Test
    @DisplayName(
            "createStoreEmployee -> manager branch not found -> 404"
    )
    void createStoreEmployee_branchNotFound() {

        // Arrange
        when(storeRepository.findById(10L))
                .thenReturn(Optional.of(store));

        when(branchRepository.findById(7L))
                .thenReturn(Optional.empty());

        // Act + Assert
        assertThatThrownBy(() ->
                employeeService.createStoreEmployee(
                        storeEmployeeDto(
                                UserRole.ROLE_BRANCH_MANAGER,
                                7L
                        ),
                        10L
                )
        )
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining(
                        "Branch not found"
                );

        verify(userRepository, never())
                .save(any(User.class));
    }

    // =========================================================
    // CREATE STORE EMPLOYEE - STORE NOT FOUND
    // =========================================================

    @Test
    @DisplayName(
            "createStoreEmployee -> store not found -> 404"
    )
    void createStoreEmployee_storeNotFound() {

        // Arrange
        when(storeRepository.findById(10L))
                .thenReturn(Optional.empty());

        // Act + Assert
        assertThatThrownBy(() ->
                employeeService.createStoreEmployee(
                        storeEmployeeDto(
                                UserRole.ROLE_BRANCH_CASHIER,
                                null
                        ),
                        10L
                )
        )
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining(
                        "Store not found"
                );

        verify(userRepository, never())
                .save(any(User.class));
    }

    // =========================================================
    // CREATE BRANCH EMPLOYEE
    // =========================================================

    @Test
    @DisplayName(
            "createBranchEmployee -> cashier -> employee saved"
    )
    void createBranchEmployee_cashier_saved() {

        // Arrange
        when(branchRepository.findById(7L))
                .thenReturn(Optional.of(branch));

        when(userRepository.existsByEmail("emp@shpos.com"))
                .thenReturn(false);

        when(passwordEncoder.encode("plain-pw"))
                .thenReturn("$2a$10$encoded");

        when(userRepository.save(any(User.class)))
                .thenReturn(savedUser);

        // Act
        User result =
                employeeService.createBranchEmployee(
                        branchEmployee(
                                UserRole.ROLE_BRANCH_CASHIER
                        ),
                        7L
                );

        // Assert
        assertThat(result)
                .isEqualTo(savedUser);

        ArgumentCaptor<User> captor =
                ArgumentCaptor.forClass(User.class);

        verify(userRepository)
                .save(captor.capture());

        User capturedUser = captor.getValue();

        assertThat(capturedUser.getId())
                .isNull();

        assertThat(capturedUser.getBranch())
                .isEqualTo(branch);

        assertThat(capturedUser.getStore())
                .isEqualTo(store);

        assertThat(capturedUser.getPassword())
                .isEqualTo("$2a$10$encoded");

        assertThat(capturedUser.getVerified())
                .isFalse();
    }

    // =========================================================
    // CREATE BRANCH EMPLOYEE - DUPLICATE EMAIL
    // =========================================================

    @Test
    @DisplayName(
            "createBranchEmployee -> duplicate email -> rejected"
    )
    void createBranchEmployee_duplicateEmail_rejected() {

        // Arrange
        when(branchRepository.findById(7L))
                .thenReturn(Optional.of(branch));

        when(userRepository.existsByEmail("emp@shpos.com"))
                .thenReturn(true);

        // Act + Assert
        assertThatThrownBy(() ->
                employeeService.createBranchEmployee(
                        branchEmployee(
                                UserRole.ROLE_BRANCH_CASHIER
                        ),
                        7L
                )
        )
                .isInstanceOf(UserException.class)
                .hasMessageContaining(
                        "User with email already exists"
                );

        verify(userRepository, never())
                .save(any(User.class));
    }

    // =========================================================
    // CREATE BRANCH EMPLOYEE - INVALID ROLE
    // =========================================================

    @Test
    @DisplayName(
            "createBranchEmployee -> STORE_ADMIN -> rejected"
    )
    void createBranchEmployee_invalidRole_rejected() {

        // Arrange
        when(branchRepository.findById(7L))
                .thenReturn(Optional.of(branch));

        // Act + Assert
        assertThatThrownBy(() ->
                employeeService.createBranchEmployee(
                        branchEmployee(
                                UserRole.ROLE_STORE_ADMIN
                        ),
                        7L
                )
        )
                .isInstanceOf(UserException.class)
                .hasMessageContaining(
                        "Invalid branch employee role"
                );

        verify(userRepository, never())
                .save(any(User.class));
    }

    // =========================================================
    // CREATE BRANCH EMPLOYEE - BRANCH NOT FOUND
    // =========================================================

    @Test
    @DisplayName(
            "createBranchEmployee -> branch not found -> 404"
    )
    void createBranchEmployee_branchNotFound() {

        // Arrange
        when(branchRepository.findById(99L))
                .thenReturn(Optional.empty());

        // Act + Assert
        assertThatThrownBy(() ->
                employeeService.createBranchEmployee(
                        branchEmployee(
                                UserRole.ROLE_BRANCH_CASHIER
                        ),
                        99L
                )
        )
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining(
                        "Branch not found"
                );

        verify(userRepository, never())
                .save(any(User.class));
    }

    // =========================================================
    // CREATE BRANCH EMPLOYEE - MANAGER
    // =========================================================

    @Test
    @DisplayName(
            "createBranchEmployee -> manager -> manager assigned to branch"
    )
    void createBranchEmployee_manager_assigned() {

        // Arrange
        User manager = new User();

        manager.setId(100L);
        manager.setFullName("Sok Dara");
        manager.setEmail("emp@shpos.com");
        manager.setRole(
                UserRole.ROLE_BRANCH_MANAGER
        );

        when(branchRepository.findById(7L))
                .thenReturn(Optional.of(branch));

        when(userRepository.existsByEmail("emp@shpos.com"))
                .thenReturn(false);

        when(passwordEncoder.encode("plain-pw"))
                .thenReturn("$2a$10$encoded");

        when(userRepository.save(any(User.class)))
                .thenReturn(manager);

        // Act
        employeeService.createBranchEmployee(
                branchEmployee(
                        UserRole.ROLE_BRANCH_MANAGER
                ),
                7L
        );

        // Assert
        assertThat(branch.getManager())
                .isEqualTo(manager);

        verify(branchRepository)
                .save(branch);
    }

    // =========================================================
    // UPDATE EMPLOYEE - PARTIAL UPDATE
    // =========================================================

    @Test
    @DisplayName(
            "updateEmployee -> only supplied fields updated"
    )
    void updateEmployee_partialUpdate() {

        // Arrange
        User existing = new User();

        existing.setId(50L);
        existing.setFullName("Old Name");
        existing.setEmail("old@shpos.com");
        existing.setPhone("011111111");
        existing.setPassword("$2a$10$keepme");
        existing.setRole(
                UserRole.ROLE_BRANCH_CASHIER
        );

        when(userRepository.findById(50L))
                .thenReturn(Optional.of(existing));

        when(userRepository.save(any(User.class)))
                .thenReturn(existing);

        User changes = new User();

        changes.setFullName("New Name");

        // email, phone, role and password remain null

        // Act
        User result =
                employeeService.updateEmployee(
                        50L,
                        changes
                );

        // Assert
        assertThat(result)
                .isEqualTo(existing);

        assertThat(existing.getFullName())
                .isEqualTo("New Name");

        assertThat(existing.getEmail())
                .isEqualTo("old@shpos.com");

        assertThat(existing.getPhone())
                .isEqualTo("011111111");

        assertThat(existing.getPassword())
                .isEqualTo("$2a$10$keepme");
    }

    // =========================================================
    // UPDATE EMPLOYEE - EMAIL
    // =========================================================

    @Test
    @DisplayName(
            "updateEmployee -> new email -> email updated"
    )
    void updateEmployee_emailUpdated() {

        // Arrange
        User existing = new User();

        existing.setId(50L);
        existing.setEmail("old@shpos.com");
        existing.setFullName("Sok Dara");

        when(userRepository.findById(50L))
                .thenReturn(Optional.of(existing));

        when(userRepository.existsByEmail("new@shpos.com"))
                .thenReturn(false);

        when(userRepository.save(any(User.class)))
                .thenReturn(existing);

        User changes = new User();
        changes.setEmail("new@shpos.com");

        // Act
        employeeService.updateEmployee(
                50L,
                changes
        );

        // Assert
        assertThat(existing.getEmail())
                .isEqualTo("new@shpos.com");

        verify(userRepository)
                .existsByEmail("new@shpos.com");

        verify(userRepository)
                .save(existing);
    }

    // =========================================================
    // UPDATE EMPLOYEE - DUPLICATE EMAIL
    // =========================================================

    @Test
    @DisplayName(
            "updateEmployee -> duplicate email -> rejected"
    )
    void updateEmployee_duplicateEmail_rejected() {

        // Arrange
        User existing = new User();

        existing.setId(50L);
        existing.setEmail("old@shpos.com");

        when(userRepository.findById(50L))
                .thenReturn(Optional.of(existing));

        when(userRepository.existsByEmail("new@shpos.com"))
                .thenReturn(true);

        User changes = new User();
        changes.setEmail("new@shpos.com");

        // Act + Assert
        assertThatThrownBy(() ->
                employeeService.updateEmployee(
                        50L,
                        changes
                )
        )
                .isInstanceOf(UserException.class)
                .hasMessageContaining(
                        "User with email already exists"
                );

        verify(userRepository, never())
                .save(any(User.class));
    }

    // =========================================================
    // UPDATE EMPLOYEE - UNKNOWN ID
    // =========================================================

    @Test
    @DisplayName(
            "updateEmployee -> unknown employee -> 404"
    )
    void updateEmployee_notFound() {

        // Arrange
        when(userRepository.findById(99L))
                .thenReturn(Optional.empty());

        // Act + Assert
        assertThatThrownBy(() ->
                employeeService.updateEmployee(
                        99L,
                        new User()
                )
        )
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining(
                        "Employee not found"
                );

        verify(userRepository, never())
                .save(any(User.class));
    }

    // =========================================================
    // DELETE EMPLOYEE
    // =========================================================

    @Test
    @DisplayName(
            "deleteEmployee -> normal employee -> deleted"
    )
    void deleteEmployee_found() {

        // Arrange
        User employee = new User();

        employee.setId(50L);
        employee.setEmail("emp@shpos.com");
        employee.setRole(
                UserRole.ROLE_BRANCH_CASHIER
        );

        when(userRepository.findById(50L))
                .thenReturn(Optional.of(employee));

        // Act
        employeeService.deleteEmployee(50L);

        // Assert
        verify(userRepository)
                .delete(employee);
    }

    // =========================================================
    // DELETE BRANCH MANAGER
    // =========================================================

    @Test
    @DisplayName(
            "deleteEmployee -> branch manager -> manager removed from branch"
    )
    void deleteEmployee_manager_clearsBranchManager() {

        // Arrange
        User manager = new User();

        manager.setId(50L);
        manager.setEmail("manager@shpos.com");
        manager.setRole(
                UserRole.ROLE_BRANCH_MANAGER
        );
        manager.setBranch(branch);

        branch.setManager(manager);

        when(userRepository.findById(50L))
                .thenReturn(Optional.of(manager));

        // Act
        employeeService.deleteEmployee(50L);

        // Assert
        assertThat(branch.getManager())
                .isNull();

        verify(branchRepository)
                .save(branch);

        verify(userRepository)
                .delete(manager);
    }

    // =========================================================
    // DELETE STORE ADMIN - REJECT
    // =========================================================

    @Test
    @DisplayName(
            "deleteEmployee -> STORE_ADMIN -> rejected"
    )
    void deleteEmployee_storeAdmin_rejected() {

        // Arrange
        User admin = new User();

        admin.setId(50L);
        admin.setRole(
                UserRole.ROLE_STORE_ADMIN
        );

        when(userRepository.findById(50L))
                .thenReturn(Optional.of(admin));

        // Act + Assert
        assertThatThrownBy(() ->
                employeeService.deleteEmployee(50L)
        )
                .isInstanceOf(UserException.class)
                .hasMessageContaining(
                        "Store admin cannot be deleted"
                );

        verify(userRepository, never())
                .delete(any(User.class));
    }

    // =========================================================
    // DELETE SUPER ADMIN - REJECT
    // =========================================================

    @Test
    @DisplayName(
            "deleteEmployee -> SUPER_ADMIN -> rejected"
    )
    void deleteEmployee_superAdmin_rejected() {

        // Arrange
        User admin = new User();

        admin.setId(50L);
        admin.setRole(
                UserRole.ROLE_ADMIN
        );

        when(userRepository.findById(50L))
                .thenReturn(Optional.of(admin));

        // Act + Assert
        assertThatThrownBy(() ->
                employeeService.deleteEmployee(50L)
        )
                .isInstanceOf(UserException.class)
                .hasMessageContaining(
                        "Super admin cannot be deleted"
                );

        verify(userRepository, never())
                .delete(any(User.class));
    }

    // =========================================================
    // FIND STORE EMPLOYEES - ROLE
    // =========================================================

    @Test
    @DisplayName(
            "findByStoreEmployees -> cashier role -> filtered"
    )
    void findByStoreEmployees_filtersByRole() {

        // Arrange
        User cashier = new User();

        cashier.setId(1L);
        cashier.setRole(
                UserRole.ROLE_BRANCH_CASHIER
        );

        when(storeRepository.findById(10L))
                .thenReturn(Optional.of(store));

        when(userRepository.findByStoreAndRoleIn(
                store,
                List.of(
                        UserRole.ROLE_BRANCH_CASHIER
                )
        ))
                .thenReturn(List.of(cashier));

        // Act
        List<User> result =
                employeeService.findByStoreEmployees(
                        10L,
                        UserRole.ROLE_BRANCH_CASHIER
                );

        // Assert
        assertThat(result)
                .containsExactly(cashier);
    }

    // =========================================================
    // FIND STORE EMPLOYEES - ALL
    // =========================================================

    @Test
    @DisplayName(
            "findByStoreEmployees -> null role -> all employee roles"
    )
    void findByStoreEmployees_nullRole_returnsAll() {

        // Arrange
        User manager = new User();
        manager.setRole(
                UserRole.ROLE_BRANCH_MANAGER
        );

        User cashier = new User();
        cashier.setRole(
                UserRole.ROLE_BRANCH_CASHIER
        );

        List<User> employees =
                List.of(manager, cashier);

        when(storeRepository.findById(10L))
                .thenReturn(Optional.of(store));

        when(userRepository.findByStoreAndRoleIn(
                store,
                List.of(
                        UserRole.ROLE_STORE_MANAGER,
                        UserRole.ROLE_BRANCH_MANAGER,
                        UserRole.ROLE_BRANCH_CASHIER
                )
        ))
                .thenReturn(employees);

        // Act
        List<User> result =
                employeeService.findByStoreEmployees(
                        10L,
                        null
                );

        // Assert
        assertThat(result)
                .containsExactly(
                        manager,
                        cashier
                );
    }

    // =========================================================
    // FIND STORE EMPLOYEES - INVALID ROLE
    // =========================================================

    @Test
    @DisplayName(
            "findByStoreEmployees -> invalid role -> rejected"
    )
    void findByStoreEmployees_invalidRole_rejected() {

        // Arrange
        when(storeRepository.findById(10L))
                .thenReturn(Optional.of(store));

        // Act + Assert
        assertThatThrownBy(() ->
                employeeService.findByStoreEmployees(
                        10L,
                        UserRole.ROLE_ADMIN
                )
        )
                .isInstanceOf(UserException.class)
                .hasMessageContaining(
                        "Invalid employee role for store"
                );

        verify(userRepository, never())
                .findByStoreAndRoleIn(
                        any(Store.class),
                        any()
                );
    }

    // =========================================================
    // FIND BRANCH EMPLOYEES - ROLE
    // =========================================================

    @Test
    @DisplayName(
            "findByBranchEmployees -> cashier role -> filtered"
    )
    void findByBranchEmployees_filtersByRole() {

        // Arrange
        User cashier = new User();

        cashier.setId(1L);
        cashier.setRole(
                UserRole.ROLE_BRANCH_CASHIER
        );

        when(branchRepository.findById(7L))
                .thenReturn(Optional.of(branch));

        when(userRepository.findByBranchAndRoleIn(
                branch,
                List.of(
                        UserRole.ROLE_BRANCH_CASHIER
                )
        ))
                .thenReturn(List.of(cashier));

        // Act
        List<User> result =
                employeeService.findByBranchEmployees(
                        7L,
                        UserRole.ROLE_BRANCH_CASHIER
                );

        // Assert
        assertThat(result)
                .containsExactly(cashier);
    }

    // =========================================================
    // FIND BRANCH EMPLOYEES - ALL
    // =========================================================

    @Test
    @DisplayName(
            "findByBranchEmployees -> null role -> all branch employees"
    )
    void findByBranchEmployees_nullRole_returnsAll() {

        // Arrange
        User cashier = new User();

        cashier.setRole(
                UserRole.ROLE_BRANCH_CASHIER
        );

        User manager = new User();

        manager.setRole(
                UserRole.ROLE_BRANCH_MANAGER
        );

        List<User> employees =
                List.of(cashier, manager);

        when(branchRepository.findById(7L))
                .thenReturn(Optional.of(branch));

        when(userRepository.findByBranchAndRoleIn(
                branch,
                List.of(
                        UserRole.ROLE_BRANCH_CASHIER,
                        UserRole.ROLE_BRANCH_MANAGER
                )
        ))
                .thenReturn(employees);

        // Act
        List<User> result =
                employeeService.findByBranchEmployees(
                        7L,
                        null
                );

        // Assert
        assertThat(result)
                .containsExactly(
                        cashier,
                        manager
                );
    }

    // =========================================================
    // FIND BRANCH EMPLOYEES - INVALID ROLE
    // =========================================================

    @Test
    @DisplayName(
            "findByBranchEmployees -> STORE_ADMIN -> rejected"
    )
    void findByBranchEmployees_invalidRole_rejected() {

        // Arrange
        when(branchRepository.findById(7L))
                .thenReturn(Optional.of(branch));

        // Act + Assert
        assertThatThrownBy(() ->
                employeeService.findByBranchEmployees(
                        7L,
                        UserRole.ROLE_STORE_ADMIN
                )
        )
                .isInstanceOf(UserException.class)
                .hasMessageContaining(
                        "Invalid branch employee role"
                );

        verify(userRepository, never())
                .findByBranchAndRoleIn(
                        any(Branch.class),
                        any()
                );
    }

    // =========================================================
    // FIND EMPLOYEE BY ID
    // =========================================================

    @Test
    @DisplayName(
            "findEmployeeById -> existing employee -> returned"
    )
    void findEmployeeById_found() {

        // Arrange
        when(userRepository.findById(50L))
                .thenReturn(Optional.of(savedUser));

        // Act
        User result =
                employeeService.findEmployeeById(50L);

        // Assert
        assertThat(result)
                .isEqualTo(savedUser);
    }

    // =========================================================
    // FIND EMPLOYEE BY ID - NOT FOUND
    // =========================================================

    @Test
    @DisplayName(
            "findEmployeeById -> unknown id -> 404"
    )
    void findEmployeeById_notFound() {

        // Arrange
        when(userRepository.findById(99L))
                .thenReturn(Optional.empty());

        // Act + Assert
        assertThatThrownBy(() ->
                employeeService.findEmployeeById(99L)
        )
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining(
                        "Employee not found"
                );
    }
}