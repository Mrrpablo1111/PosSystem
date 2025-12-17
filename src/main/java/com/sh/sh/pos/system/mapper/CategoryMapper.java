package com.sh.sh.pos.system.mapper;

import com.sh.sh.pos.system.model.Category;
import com.sh.sh.pos.system.payload.dto.CategoryDTO;

public class CategoryMapper {

	public static CategoryDTO toDTO(Category category) {
		// TODO Auto-generated method stub
		return CategoryDTO.builder()
				.id(category.getId())
				.name(category.getName())
				.storeId(category.getStore()!=null?category.getStore().getId():null)
				.build();
	}

}
