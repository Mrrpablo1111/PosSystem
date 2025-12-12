package com.sh.sh.pos.system.service.serviceImpl;

import java.util.List;

import org.springframework.stereotype.Service;

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
		return CategoryMapper.toDTO(category);
		
	}

	@Override
	public List<CategoryDTO> getCategoriesByStore(Long storeId) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public CategoryDTO updateCategory(Long id, CategoryDTO dto) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void deleteCategory(Long id) {
		// TODO Auto-generated method stub
	}

}
