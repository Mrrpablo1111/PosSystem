package com.sh.sh.pos.system.service.categoryservice;

import java.util.List;

import com.sh.sh.pos.system.exceptions.UserException;
import com.sh.sh.pos.system.payload.dto.categorydto.CategoryDTO;

public interface CategoryService {
	CategoryDTO createCategory(CategoryDTO dto) throws UserException;  
	List<CategoryDTO> getCategoriesByStore(Long storeId);
	CategoryDTO updateCategory(Long id, CategoryDTO dto) throws UserException;
	void deleteCategory(Long id) throws UserException;
}
