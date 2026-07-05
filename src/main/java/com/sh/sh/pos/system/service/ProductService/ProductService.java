package com.sh.sh.pos.system.service.productservice;

import java.nio.file.AccessDeniedException;
import java.util.List;

import com.sh.sh.pos.system.model.user.User;
import com.sh.sh.pos.system.payload.dto.productdto.ProductDTO;

public interface ProductService {

	ProductDTO createProduct(ProductDTO productDTO, User user);

	ProductDTO updateProduct(Long id, ProductDTO productDTO, User user);

	ProductDTO getProductById(Long id);

	ProductDTO getProductByBarcode(String barcode);

	void deleteProduct(Long id, User user);

	List<ProductDTO> getProductByStoreId(Long storeId);

	List<ProductDTO> searchBykeyword(Long storeId, String keyword);
}