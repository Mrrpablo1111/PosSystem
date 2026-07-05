package com.sh.sh.pos.system.mapper.categorymapper;

import com.sh.sh.pos.system.model.category.Category;
import com.sh.sh.pos.system.payload.dto.categorydto.CategoryDTO;

public class CategoryMapper {
	private CategoryMapper() {
	}
	public static CategoryDTO toDTO(Category category) {
		if (category == null) {
            return null;
        }
		return CategoryDTO.builder()
				.id(category.getId())
				.name(category.getName())
				.storeId(category.getStore() != null ? category.getStore().getId() : null)
				.build();
	}

}
