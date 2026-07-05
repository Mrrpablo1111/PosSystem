package com.sh.sh.pos.system.repository.categoryrepository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.sh.sh.pos.system.model.category.Category;

public interface CategoryRepository extends JpaRepository<Category, Long> {
	List<Category> findByStoreId(long storyId); 
}
