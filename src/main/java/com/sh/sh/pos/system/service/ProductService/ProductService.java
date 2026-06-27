package com.sh.sh.pos.system.service.ProductService;

import java.nio.file.AccessDeniedException;
import java.util.List;

import com.sh.sh.pos.system.model.User;
import com.sh.sh.pos.system.payload.dto.productDTO.ProductDTO;

public interface ProductService {
	
	ProductDTO createProduct(ProductDTO productDTO, User user) throws AccessDeniedException;
	ProductDTO updateProduct(Long id, ProductDTO productDTO, User user) throws AccessDeniedException;
	ProductDTO getProductById(Long id);
	ProductDTO getProductByBarcode(String barcode);
	
	void deleteProduct(Long id, User user) throws AccessDeniedException;
	
	List<ProductDTO> getProductByStoreId(Long storeId);
	List<ProductDTO> searchBykeyword(Long storeId,   String keyword)  ;


}
