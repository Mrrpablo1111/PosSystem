package com.sh.sh.pos.system.service.serviceimpl.branchserviceimpl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertThrows;
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

import com.sh.sh.pos.system.domain.UserRole;
import com.sh.sh.pos.system.exception.ResourceNotFoundException;
import com.sh.sh.pos.system.exception.UserException;
import com.sh.sh.pos.system.model.branch.Branch;
import com.sh.sh.pos.system.model.store.Store;
import com.sh.sh.pos.system.model.user.User;
import com.sh.sh.pos.system.payload.dto.branchdto.BranchDTO;
import com.sh.sh.pos.system.repository.branchrepository.BranchRepository;
import com.sh.sh.pos.system.repository.storerepository.StoreRepository;
import com.sh.sh.pos.system.service.userservice.UserService;

@ExtendWith(MockitoExtension.class)
@DisplayName("BranchServiceImpl — CRUD and store-scoped access")
class BranchServiceImplTest {

    @Mock
    private BranchRepository branchRepository;
    @Mock
    private StoreRepository storeRepository;
    @Mock
    private UserService userService;

    @InjectMocks
    private BranchServiceImpl branchService;

    private User storeAdmin;
    private Store store;
    private Branch branch;

    @BeforeEach
    void setUp() {
        storeAdmin = new User();
        storeAdmin.setId(1L);
        storeAdmin.setRole(UserRole.ROLE_STORE_ADMIN);

        store = Store.builder()
                .id(10L)
                .brand("Test Store")
                .storeAdmin(storeAdmin)
                .build();
        storeAdmin.setStore(store);

        branch = new Branch();
        branch.setId(100L);
        branch.setName("Main Branch");
        branch.setStore(store);
    }

    private BranchDTO dto(String name) {
        BranchDTO dto = new BranchDTO();
        dto.setName(name);
        dto.setAddress("Street 271, Phnom Penh");
        dto.setEmail("branch@shpos.com");
        dto.setPhone("012 345 678");
        return dto;
    }

    // ── createBranch ──────────────────────────────────────────────────────

    @Test
    @DisplayName("createBranch → user has a store → branch saved ✅")
    void createBranch_valid_saved() {
        when(storeRepository.findByStoreAdminId(1L)).thenReturn(Optional.of(store));
        when(branchRepository.save(any(Branch.class))).thenReturn(branch);

        BranchDTO result = branchService.createBranch(dto("Main Branch"), storeAdmin);

        assertThat(result).isNotNull();
        verify(branchRepository).save(any(Branch.class));
    }

    @Test
@DisplayName("createBranch → user has NO store → 404, nothing saved ❌")
void createBranch_noStore_rejected() {
    when(storeRepository.findByStoreAdminId(1L)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> branchService.createBranch(dto("Main Branch"), storeAdmin))
            .isInstanceOf(ResourceNotFoundException.class)
            .hasMessageContaining("No store found");

    verify(branchRepository, never()).save(any());
}
    // ── updateBranch ──────────────────────────────────────────────────────

    @Test
    @DisplayName("updateBranch → fields applied, updatedAt stamped ✅")
    void updateBranch_appliesFields() {
        when(branchRepository.findById(100L)).thenReturn(Optional.of(branch));
        when(branchRepository.save(any(Branch.class))).thenReturn(branch);

        branchService.updateBranch(100L, dto("Renamed Branch"), storeAdmin);

        assertThat(branch.getName()).isEqualTo("Renamed Branch");
        assertThat(branch.getUpdatedAt()).isNotNull();
        verify(branchRepository).save(branch);
    }

    @Test
    @DisplayName("updateBranch → unknown id → 404 ❌")
    void updateBranch_notFound() {
        when(branchRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> branchService.updateBranch(99L, dto("X"), storeAdmin))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Branch not found");
    }

    // ── deleteBranch ──────────────────────────────────────────────────────

    @Test
    @DisplayName("deleteBranch → deletes the loaded entity ✅")
    void deleteBranch_found() {
        when(branchRepository.findById(100L)).thenReturn(Optional.of(branch));

        branchService.deleteBranch(100L);

        verify(branchRepository).delete(branch);
    }

    @Test
    @DisplayName("deleteBranch → unknown id → 404, nothing deleted ❌")
    void deleteBranch_notFound() {
        when(branchRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> branchService.deleteBranch(99L))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(branchRepository, never()).delete(any());
    }

    // ── getAllBranchesByStoreId — access control ──────────────────────────

    @Test
    @DisplayName("list → store admin of the store → allowed ✅")
    void list_storeAdmin_allowed() {
        when(userService.getCurrentUser()).thenReturn(storeAdmin);
        when(storeRepository.findById(10L)).thenReturn(Optional.of(store));
        when(branchRepository.findByStoreId(10L)).thenReturn(List.of(branch));

        assertThat(branchService.getAllBranchesByStoreId(10L)).hasSize(1);
    }

    @Test
    @DisplayName("list → manager of the same store → allowed ✅")
    void list_sameStoreManager_allowed() {
        User manager = new User();
        manager.setId(2L);
        manager.setRole(UserRole.ROLE_STORE_MANAGER);
        manager.setStore(store);

        when(userService.getCurrentUser()).thenReturn(manager);
        when(storeRepository.findById(10L)).thenReturn(Optional.of(store));
        when(branchRepository.findByStoreId(10L)).thenReturn(List.of(branch));

        assertThat(branchService.getAllBranchesByStoreId(10L)).hasSize(1);
    }

    @Test
    @DisplayName("list → manager of ANOTHER store → denied ❌")
    void list_otherStoreManager_denied() {
        Store otherStore = Store.builder().id(99L).brand("Other").build();
        User otherManager = new User();
        otherManager.setId(3L);
        otherManager.setRole(UserRole.ROLE_STORE_MANAGER);
        otherManager.setStore(otherStore);

        when(userService.getCurrentUser()).thenReturn(otherManager);
        when(storeRepository.findById(10L)).thenReturn(Optional.of(store));

        assertThatThrownBy(() -> branchService.getAllBranchesByStoreId(10L))
                .isInstanceOf(UserException.class)
                .hasMessageContaining("not authorized");

        verify(branchRepository, never()).findByStoreId(any());
    }

    @Test
    @DisplayName("list → admin of ANOTHER store → denied ❌")
    void list_otherAdmin_denied() {
        User otherAdmin = new User();
        otherAdmin.setId(4L);
        otherAdmin.setRole(UserRole.ROLE_STORE_ADMIN);

        when(userService.getCurrentUser()).thenReturn(otherAdmin);
        when(storeRepository.findById(10L)).thenReturn(Optional.of(store));

        assertThatThrownBy(() -> branchService.getAllBranchesByStoreId(10L))
                .isInstanceOf(UserException.class)
                .hasMessageContaining("not authorized");
    }

    @Test
    @DisplayName("list → cashier → denied ❌")
    void list_cashier_denied() {
        User cashier = new User();
        cashier.setId(5L);
        cashier.setRole(UserRole.ROLE_BRANCH_CASHIER);

        when(userService.getCurrentUser()).thenReturn(cashier);
        when(storeRepository.findById(10L)).thenReturn(Optional.of(store));

        assertThatThrownBy(() -> branchService.getAllBranchesByStoreId(10L))
                .isInstanceOf(UserException.class);
    }

    // ── getBranchById ─────────────────────────────────────────────────────

    @Test
    @DisplayName("getBranchById → found → DTO returned ✅")
    void getById_found() {
        when(branchRepository.findById(100L)).thenReturn(Optional.of(branch));

        assertThat(branchService.getBranchById(100L)).isNotNull();
    }

    @Test
    @DisplayName("getBranchById → unknown id → 404 ❌")
    void getById_notFound() {
        when(branchRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> branchService.getBranchById(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}