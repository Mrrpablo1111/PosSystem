package com.sh.sh.pos.system.service.serviceImpl;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.sh.sh.pos.system.domain.UserRole;
import com.sh.sh.pos.system.exceptions.UserException;
import com.sh.sh.pos.system.mapper.CategoryMapper;
import com.sh.sh.pos.system.model.Category;
import com.sh.sh.pos.system.model.Store;
import com.sh.sh.pos.system.model.User;
import com.sh.sh.pos.system.payload.dto.CategoryDTO;
import com.sh.sh.pos.system.repository.CategoryRepository;
import com.sh.sh.pos.system.repository.StoreRepository;
import com.sh.sh.pos.system.service.CategoryService;
import com.sh.sh.pos.system.service.UserService;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor

public class CategoryServiceImpl implements CategoryService{
	
	private final CategoryRepository categoryRepository;
	private final UserService userService;
	private final StoreRepository storeRepository;
	@Override
	public CategoryDTO createCategory(CategoryDTO dto) throws UserException {
		User user = userService.getCurrentUser();
		
		Store store = storeRepository.findById(dto.getStoreId()).orElseThrow(
				() -> new EntityNotFoundException("Store not found") 
				);
		checkAuthority(user, store);
		Category category = Category.builder()
				.store(store)
				.name(dto.getName())
				.build();

		return CategoryMapper.toDTO(categoryRepository.save(category));
		
	}

	@Override
	public List<CategoryDTO> getCategoriesByStore(Long storeId) {
		
		return categoryRepository.findByStoreId(storeId).stream()
                .map(CategoryMapper::toDTO)
                .collect(Collectors.toList());
	}
 
	@Override
	public CategoryDTO updateCategory(Long id, CategoryDTO dto) throws UserException {
		
		Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Category not found"));

        User user = userService.getCurrentUser();
        checkAuthority(user, category.getStore());

        category.setName(dto.getName());
        return CategoryMapper.toDTO(categoryRepository.save(category));
	}

	@Override
	public void deleteCategory(Long id) throws UserException {
		Category category = categoryRepository.findById(id).orElseThrow(()-> new EntityNotFoundException("category not exists"));
		User user = userService.getCurrentUser();
		
		checkAuthority(user, category.getStore());
		
		categoryRepository.delete(category);
		
	}
	
	private void checkAuthority(User user, Store store) {
		boolean isAdmin = user.getRole().equals(UserRole.ROLE_STORE_ADMIN);
		boolean isManager = user.getRole().equals(UserRole.ROLE_STORE_MANAGER);
		boolean isSameStore = user.equals(store.getStoreAdmin());
		
		if(!(isAdmin && isSameStore) && !isManager) {
			throw new SecurityException("you don't have permission to manage this category");
		}
	}
	
	

}
