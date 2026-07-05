package com.sh.sh.pos.system.service.serviceimpl.categoryserviceimpl;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sh.sh.pos.system.domain.UserRole;
import com.sh.sh.pos.system.exception.ResourceNotFoundException;
import com.sh.sh.pos.system.exception.UserException;
import com.sh.sh.pos.system.mapper.categorymapper.CategoryMapper;
import com.sh.sh.pos.system.model.category.Category;
import com.sh.sh.pos.system.model.store.Store;
import com.sh.sh.pos.system.model.user.User;
import com.sh.sh.pos.system.payload.dto.categorydto.CategoryDTO;
import com.sh.sh.pos.system.repository.categoryrepository.CategoryRepository;
import com.sh.sh.pos.system.repository.storerepository.StoreRepository;
import com.sh.sh.pos.system.service.categoryservice.CategoryService;
import com.sh.sh.pos.system.service.userservice.UserService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

	private static final String STORE_NOT_FOUND = "Store not found with id: ";

	private final CategoryRepository categoryRepository;
	private final UserService userService;
	private final StoreRepository storeRepository;

	@Override
	@Transactional
	public CategoryDTO createCategory(CategoryDTO dto) {
		User user = userService.getCurrentUser();

		Store store = storeRepository.findById(dto.getStoreId()).orElseThrow(
				() -> new ResourceNotFoundException(STORE_NOT_FOUND + dto.getStoreId()));

		checkAuthority(user, store);

		Category category = Category.builder()
				.store(store)
				.name(dto.getName())
				.build();

		return CategoryMapper.toDTO(categoryRepository.save(category));
	}

	@Override
	@Transactional(readOnly = true)
	public List<CategoryDTO> getCategoriesByStore(Long storeId) {
		return categoryRepository.findByStoreId(storeId).stream()
				.map(CategoryMapper::toDTO)
				.toList();
	}

	@Override
	@Transactional
	public CategoryDTO updateCategory(Long id, CategoryDTO dto) {
		Category category = findCategoryOrThrow(id);
		verifyAccessToCategoryStore(category);

		category.setName(dto.getName());
		return CategoryMapper.toDTO(categoryRepository.save(category));
	}

	@Override
	@Transactional
	public void deleteCategory(Long id) {
		Category category = findCategoryOrThrow(id);
		verifyAccessToCategoryStore(category);

		categoryRepository.delete(category);
	}

	// ── Helpers ───────────────────────────────────────────────────────────

	private Category findCategoryOrThrow(Long id) {
		return categoryRepository.findById(id).orElseThrow(
				() -> new ResourceNotFoundException("Category not found with id: " + id));
	}

	private void verifyAccessToCategoryStore(Category category) {
		User user = userService.getCurrentUser();
		Long storeId = category.getStore().getId();
		Store store = storeRepository.findByIdWithAdmin(storeId).orElseThrow(
				() -> new ResourceNotFoundException(STORE_NOT_FOUND + storeId));
		checkAuthority(user, store);
	}

	private void checkAuthority(User user, Store store) {
		boolean isAdminOfStore = user.getRole() == UserRole.ROLE_STORE_ADMIN
				&& store.getStoreAdmin() != null
				&& store.getStoreAdmin().getId().equals(user.getId());

		boolean isManagerOfStore = user.getRole() == UserRole.ROLE_STORE_MANAGER
				&& user.getStore() != null
				&& user.getStore().getId().equals(store.getId());

		if (!isAdminOfStore && !isManagerOfStore) {
			throw new UserException("You don't have permission to manage this category");
		}
	}
}