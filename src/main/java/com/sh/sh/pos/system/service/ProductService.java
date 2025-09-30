package com.sh.sh.pos.system.service;

import java.util.List;

import com.sh.sh.pos.system.model.User;
import com.sh.sh.pos.system.payload.dto.ProductDTO;

public interface ProductService {
	
	ProductDTO createProduct(ProductDTO productDTO, User user) throws Exception;
	ProductDTO updateProduct(Long id, ProductDTO productDTO, User user) throws Exception;
	
	void deleteProduct(Long id, User user) throws Exception;
	
	List<ProductDTO> getProductByStoreId(Long storeId);
	List<ProductDTO> searchBykeyword(Long storeId,   String keyword)  ;
}
