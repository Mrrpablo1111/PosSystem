package com.sh.sh.pos.system.service.serviceimpl.storeServiceImpl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.sh.sh.pos.system.domain.StoreStatus;
import com.sh.sh.pos.system.exception.ResourceNotFoundException;
import com.sh.sh.pos.system.model.store.Store;
import com.sh.sh.pos.system.model.user.User;
import com.sh.sh.pos.system.payload.dto.storedto.StoreDTO;
import com.sh.sh.pos.system.repository.branchrepository.BranchRepository;
import com.sh.sh.pos.system.repository.storerepository.StoreRepository;
import com.sh.sh.pos.system.repository.storetyperepository.StoreTypeRepository;
import com.sh.sh.pos.system.repository.userrepository.UserRepository;
import com.sh.sh.pos.system.service.serviceimpl.storeserviceimpl.StoreServiceImpl;
import com.sh.sh.pos.system.service.userservice.UserService;

@ExtendWith(MockitoExtension.class)
@DisplayName("StoreServiceImpl — regression tests")
class StoreServiceImplTest {

    @Mock
    private StoreRepository storeRepository;
    @Mock
    private UserService userService;
    @Mock
    private BranchRepository branchRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private StoreTypeRepository storeTypeRepository;

    @InjectMocks
    private StoreServiceImpl storeService;

    private User admin;
    private Store savedStore;

    @BeforeEach
    void setUp() {
        admin = new User();
        admin.setId(1L);
        admin.setFullName("Test Admin");
        admin.setEmail("admin@test.com");

        savedStore = Store.builder()
                .id(10L)
                .brand("Test Brand")
                .status(StoreStatus.PENDING)
                .storeAdmin(admin)
                .build();
    }

    // ── REGRESSION TEST — the bug we fixed ────────────────────────────────────
    // Bug: createStore() saved the Store but never set admin.store
    // Result: users.store_id was always NULL after store creation
    // Fix: added user.setStore(saved) + userRepository.save(user)
    //
    // This test ensures the fix never silently regresses.
    // If someone removes user.setStore(saved) in the future → this test FAILS
    // immediately.

    @Test
    @DisplayName("createStore → admin.store is set after creation (regression)")
    void createStore_linksAdminStoreId() {
        // Arrange
        StoreDTO dto = StoreDTO.builder()
                .brand("Test Brand")
                .description("A test store")
                .storeTypeId(99L)
                .build();

        when(storeTypeRepository.findById(99L)).thenReturn(Optional.empty());

        // Assert admin has NO store before
        assertThat(admin.getStore()).isNull();

        // Act
        StoreDTO result = storeService.createStore(dto, admin);

        // Assert store was created correctly
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(10L);
        assertThat(result.getBrand()).isEqualTo("Test Brand");

        // THE KEY ASSERTION — admin.store must be set after createStore()
        // This is the exact bug that caused store_id = NULL in the users table
        assertThat(admin.getStore()).isNotNull();
        assertThat(admin.getStore().getId()).isEqualTo(10L);

        // Verify userRepository.save() was called with the admin
        // If this line is missing from the service → test fails
        verify(userRepository).save(admin);
    }

    // ── getStoreById ──────────────────────────────────────────────────────────

    @Test
    @DisplayName("getStoreById → found → returns DTO")
    void getStoreById_found() throws ResourceNotFoundException {
        when(storeRepository.findById(10L)).thenReturn(Optional.of(savedStore));

        StoreDTO result = storeService.getStoreById(10L);

        assertThat(result.getId()).isEqualTo(10L);
        assertThat(result.getBrand()).isEqualTo("Test Brand");
    }

    @Test
    @DisplayName("getStoreById → not found → throws ResourceNotFoundException")
    void getStoreById_notFound() {
        when(storeRepository.findById(99L)).thenReturn(Optional.empty());

        org.junit.jupiter.api.Assertions.assertThrows(
                ResourceNotFoundException.class,
                () -> storeService.getStoreById(99L));
    }

    // ── moderateStore ─────────────────────────────────────────────────────────

    @Test
    @DisplayName("moderateStore → updates status → saves correctly")
    void moderateStore_updatesStatus() throws ResourceNotFoundException {
        when(storeRepository.findById(10L)).thenReturn(Optional.of(savedStore));
        when(storeRepository.save(any(Store.class))).thenReturn(savedStore);

        StoreDTO result = storeService.moderateStore(10L, StoreStatus.ACTIVE);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(10L);
        assertThat(result.getStatus()).isEqualTo(StoreStatus.ACTIVE);
        assertThat(savedStore.getStatus()).isEqualTo(StoreStatus.ACTIVE);

        verify(storeRepository).save(savedStore);
    }
}