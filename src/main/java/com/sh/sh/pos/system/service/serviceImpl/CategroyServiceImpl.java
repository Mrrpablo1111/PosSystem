package com.sh.sh.pos.system.service.serviceImpl;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.sh.sh.pos.system.domain.UserRole;
import com.sh.sh.pos.system.mapper.CategoryMapper;
import com.sh.sh.pos.system.model.Category;
import com.sh.sh.pos.system.model.Store;
import com.sh.sh.pos.system.model.User;
import com.sh.sh.pos.system.payload.dto.CategoryDTO;
import com.sh.sh.pos.system.repository.CategoryRepository;
import com.sh.sh.pos.system.repository.StoreRepository;
import com.sh.sh.pos.system.service.CategoryService;
import com.sh.sh.pos.system.service.UserService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor

public class CategroyServiceImpl implements CategoryService{
	
	private final CategoryRepository categoryRepository;
	private final UserService userService;
	private final StoreRepository storeRepository;
	@Override
	public CategoryDTO createCategory(CategoryDTO dto) throws Exception {
		User user = userService.getCurrentUser();
		
		Store store = storeRepository.findById(dto.getStoreId()).orElseThrow(
				() -> new Exception("Store not found") 
				);
		
		Category category = Category.builder()
				.store(store)
				.name(dto.getName())
				.build();
		return CategoryMapper.toDTO(categoryRepository.save(category));
		
	}

	@Override
	public List<CategoryDTO> getCategoriesByStore(Long storeId) {
		
		List<Category>categories= categoryRepository.findByStoreId(storeId);
		return categories.stream().map(CategoryMapper::toDTO).collect(Collectors.toList());
	}
 
	@Override
	public CategoryDTO updateCategory(Long id, CategoryDTO dto) throws Exception {
		
		Category category = categoryRepository.findById(id).orElseThrow(()-> new Exception("category not exists"));
		User user = userService.getCurrentUser();
		checkAuthority(user, category.getStore());
		category.setName(dto.getName());
		return CategoryMapper.toDTO(categoryRepository.save(category));
	}

	@Override
	public void deleteCategory(Long id) throws Exception {
		Category category = categoryRepository.findById(id).orElseThrow(()-> new Exception("category not exists"));
		User user = userService.getCurrentUser();
		
		checkAuthority(user, category.getStore());
		
		categoryRepository.delete(category);
		
	}
	
	private void checkAuthority(User user, Store store) throws Exception {
		boolean isAdmin = user.getRole().equals(UserRole.ROLE_STORE_ADMIN);
		boolean isManager = user.getRole().equals(UserRole.ROLE_STORE_MANAGER);
		boolean isSameStore = user.equals(store.getStoreAdmin());
		
		if(!(isAdmin && isSameStore) && !isManager) {
			throw new Exception("you don't have permission to manage this category");
		}
	}
	
	

}
