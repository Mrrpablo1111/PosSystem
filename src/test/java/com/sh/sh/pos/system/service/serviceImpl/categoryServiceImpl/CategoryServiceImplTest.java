package com.sh.sh.pos.system.service.serviceimpl.categoryServiceImpl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
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

import com.sh.sh.pos.system.domain.UserRole;
import com.sh.sh.pos.system.exception.UserException;
import com.sh.sh.pos.system.model.category.Category;
import com.sh.sh.pos.system.model.store.Store;
import com.sh.sh.pos.system.model.user.User;
import com.sh.sh.pos.system.payload.dto.categorydto.CategoryDTO;
import com.sh.sh.pos.system.repository.categoryrepository.CategoryRepository;
import com.sh.sh.pos.system.repository.storerepository.StoreRepository;
import com.sh.sh.pos.system.service.serviceimpl.categoryserviceimpl.CategoryServiceImpl;
import com.sh.sh.pos.system.service.userservice.UserService;

@ExtendWith(MockitoExtension.class)
@DisplayName("CategoryServiceImpl — permission checks")
class CategoryServiceImplTest {

    @Mock private CategoryRepository categoryRepository;
    @Mock private UserService         userService;
    @Mock private StoreRepository     storeRepository;

    @InjectMocks
    private CategoryServiceImpl categoryService;

    private User     storeAdmin;
    private User     storeManager;
    private User     otherAdmin;
    private User     cashier;
    private Store    store;
    private Category category;

    @BeforeEach
    void setUp() {
        storeAdmin = new User();
        storeAdmin.setId(1L);
        storeAdmin.setRole(UserRole.ROLE_STORE_ADMIN);

        storeManager = new User();
        storeManager.setId(2L);
        storeManager.setRole(UserRole.ROLE_STORE_MANAGER);

        otherAdmin = new User();
        otherAdmin.setId(3L);
        otherAdmin.setRole(UserRole.ROLE_STORE_ADMIN);

        cashier = new User();
        cashier.setId(4L);
        cashier.setRole(UserRole.ROLE_BRANCH_CASHIER);

        store = Store.builder()
                .id(10L)
                .brand("Test Store")
                .storeAdmin(storeAdmin)
                .build();

        category = Category.builder()
                .id(100L)
                .name("Beverages")
                .store(store)
                .build();

        storeAdmin.setStore(store);
        storeManager.setStore(store);
    }

    // ── createCategory uses storeRepository.findById() ────────────────────────

    @Test
    @DisplayName("createCategory → STORE_ADMIN same store → allowed ✅")
    void createCategory_storeAdmin_sameStore_allowed() {
        when(userService.getCurrentUser()).thenReturn(storeAdmin);
        when(storeRepository.findById(10L)).thenReturn(Optional.of(store));
        when(categoryRepository.save(any(Category.class))).thenReturn(category);

        CategoryDTO dto = CategoryDTO.builder().storeId(10L).name("Beverages").build();
        CategoryDTO result = categoryService.createCategory(dto);

        assertThat(result).isNotNull();
        verify(categoryRepository).save(any(Category.class));
    }

    @Test
    @DisplayName("createCategory → STORE_ADMIN different store → denied ❌")
    void createCategory_storeAdmin_differentStore_denied() {
        Store otherStore = Store.builder()
                .id(99L).brand("Other").storeAdmin(otherAdmin).build();
        otherAdmin.setStore(otherStore);

        when(userService.getCurrentUser()).thenReturn(otherAdmin);
        when(storeRepository.findById(10L)).thenReturn(Optional.of(store));

        CategoryDTO dto = CategoryDTO.builder().storeId(10L).name("Beverages").build();

        assertThatThrownBy(() -> categoryService.createCategory(dto))
                .isInstanceOf(UserException.class)
                .hasMessageContaining("permission");

        verify(categoryRepository, never()).save(any());
    }

    @Test
    @DisplayName("createCategory → STORE_MANAGER same store → allowed ✅")
    void createCategory_storeManager_allowed() {
        when(userService.getCurrentUser()).thenReturn(storeManager);
        when(storeRepository.findById(10L)).thenReturn(Optional.of(store));
        when(categoryRepository.save(any(Category.class))).thenReturn(category);

        CategoryDTO dto = CategoryDTO.builder().storeId(10L).name("Beverages").build();
        CategoryDTO result = categoryService.createCategory(dto);

        assertThat(result).isNotNull();
        verify(categoryRepository).save(any(Category.class));
    }

    @Test
    @DisplayName("createCategory → STORE_MANAGER of different store → denied ❌")
    void createCategory_managerOfDifferentStore_denied() {
        Store otherStore = Store.builder()
                .id(99L).brand("Other").storeAdmin(otherAdmin).build();
        User otherManager = new User();
        otherManager.setId(5L);
        otherManager.setRole(UserRole.ROLE_STORE_MANAGER);
        otherManager.setStore(otherStore);

        when(userService.getCurrentUser()).thenReturn(otherManager);
        when(storeRepository.findById(10L)).thenReturn(Optional.of(store));

        CategoryDTO dto = CategoryDTO.builder().storeId(10L).name("Beverages").build();

        assertThatThrownBy(() -> categoryService.createCategory(dto))
                .isInstanceOf(UserException.class)
                .hasMessageContaining("permission");

        verify(categoryRepository, never()).save(any());
    }

    @Test
    @DisplayName("createCategory → BRANCH_CASHIER → denied ❌")
    void createCategory_cashier_denied() {
        when(userService.getCurrentUser()).thenReturn(cashier);
        when(storeRepository.findById(10L)).thenReturn(Optional.of(store));

        CategoryDTO dto = CategoryDTO.builder().storeId(10L).name("Beverages").build();

        assertThatThrownBy(() -> categoryService.createCategory(dto))
                .isInstanceOf(UserException.class)
                .hasMessageContaining("permission");

        verify(categoryRepository, never()).save(any());
    }

    // ── updateCategory uses storeRepository.findByIdWithAdmin() ──────────────

    @Test
    @DisplayName("updateCategory → STORE_ADMIN same store → allowed ✅")
    void updateCategory_storeAdmin_allowed() {
        when(userService.getCurrentUser()).thenReturn(storeAdmin);
        when(categoryRepository.findById(100L)).thenReturn(Optional.of(category));
        when(storeRepository.findByIdWithAdmin(10L)).thenReturn(Optional.of(store));
        when(categoryRepository.save(any(Category.class))).thenReturn(category);

        CategoryDTO dto = CategoryDTO.builder().name("Updated Name").build();
        CategoryDTO result = categoryService.updateCategory(100L, dto);

        assertThat(result).isNotNull();
        assertThat(category.getName()).isEqualTo("Updated Name");
    }

    @Test
    @DisplayName("updateCategory → STORE_MANAGER of different store → denied ❌")
    void updateCategory_managerOfDifferentStore_denied() {
        Store otherStore = Store.builder()
                .id(99L).brand("Other").storeAdmin(otherAdmin).build();
        User otherManager = new User();
        otherManager.setId(5L);
        otherManager.setRole(UserRole.ROLE_STORE_MANAGER);
        otherManager.setStore(otherStore);

        when(userService.getCurrentUser()).thenReturn(otherManager);
        when(categoryRepository.findById(100L)).thenReturn(Optional.of(category));
        when(storeRepository.findByIdWithAdmin(10L)).thenReturn(Optional.of(store));

        CategoryDTO dto = CategoryDTO.builder().name("Hack").build();

        assertThatThrownBy(() -> categoryService.updateCategory(100L, dto))
                .isInstanceOf(UserException.class)
                .hasMessageContaining("permission");

        verify(categoryRepository, never()).save(any());
    }

    @Test
    @DisplayName("updateCategory → BRANCH_CASHIER → denied ❌")
    void updateCategory_cashier_denied() {
        when(userService.getCurrentUser()).thenReturn(cashier);
        when(categoryRepository.findById(100L)).thenReturn(Optional.of(category));
        when(storeRepository.findByIdWithAdmin(10L)).thenReturn(Optional.of(store));

        CategoryDTO dto = CategoryDTO.builder().name("Hack").build();

        assertThatThrownBy(() -> categoryService.updateCategory(100L, dto))
                .isInstanceOf(UserException.class)
                .hasMessageContaining("permission");

        verify(categoryRepository, never()).save(any());
    }

    // ── deleteCategory uses storeRepository.findByIdWithAdmin() ──────────────

    @Test
    @DisplayName("deleteCategory → STORE_ADMIN same store → allowed ✅")
    void deleteCategory_storeAdmin_allowed() {
        when(userService.getCurrentUser()).thenReturn(storeAdmin);
        when(categoryRepository.findById(100L)).thenReturn(Optional.of(category));
        when(storeRepository.findByIdWithAdmin(10L)).thenReturn(Optional.of(store));

        categoryService.deleteCategory(100L);

        verify(categoryRepository).delete(category);
    }

    @Test
    @DisplayName("deleteCategory → BRANCH_CASHIER → denied + NOT deleted ❌")
    void deleteCategory_cashier_denied() {
        when(userService.getCurrentUser()).thenReturn(cashier);
        when(categoryRepository.findById(100L)).thenReturn(Optional.of(category));
        when(storeRepository.findByIdWithAdmin(10L)).thenReturn(Optional.of(store));

        assertThatThrownBy(() -> categoryService.deleteCategory(100L))
                .isInstanceOf(UserException.class)
                .hasMessageContaining("permission");

        verify(categoryRepository, never()).delete(any());
    }

    @Test
    @DisplayName("deleteCategory → STORE_ADMIN different store → denied ❌")
    void deleteCategory_otherAdmin_denied() {
        Store otherStore = Store.builder()
                .id(99L).brand("Other").storeAdmin(otherAdmin).build();
        otherAdmin.setStore(otherStore);

        when(userService.getCurrentUser()).thenReturn(otherAdmin);
        when(categoryRepository.findById(100L)).thenReturn(Optional.of(category));
        when(storeRepository.findByIdWithAdmin(10L)).thenReturn(Optional.of(store));

        assertThatThrownBy(() -> categoryService.deleteCategory(100L))
                .isInstanceOf(UserException.class)
                .hasMessageContaining("permission");

        verify(categoryRepository, never()).delete(any());
    }
}