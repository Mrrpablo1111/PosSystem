package com.sh.sh.pos.system.service.serviceImpl;

import java.util.List;

import org.springframework.stereotype.Service;

import com.sh.sh.pos.system.payload.dto.CategoryDTO;
import com.sh.sh.pos.system.repository.CategoryRepository;
import com.sh.sh.pos.system.service.CategoryService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CategroyServiceImpl implements CategoryService{
	
	private final CategoryRepository category
	
	@Override
	public CategoryDTO createCategory(CategoryDTO dto) {
		
		return null;
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
