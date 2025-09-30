package com.sh.sh.pos.system.service;

import java.util.List;

import com.sh.sh.pos.system.payload.dto.CategoryDTO;

public interface CategoryService {
	CategoryDTO createCategory(CategoryDTO dto);  
	List<CategoryDTO> getCategoriesByStore(Long storeId);
	CategoryDTO updateCategory(Long id, CategoryDTO dto);
	void deleteCategory(Long id);
}
