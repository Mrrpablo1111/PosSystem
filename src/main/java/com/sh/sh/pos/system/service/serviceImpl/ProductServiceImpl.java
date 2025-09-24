package com.sh.sh.pos.system.service.serviceImpl;

import java.util.List;

import org.springframework.stereotype.Service;

import com.sh.sh.pos.system.model.Store;
import com.sh.sh.pos.system.model.User;
import com.sh.sh.pos.system.payload.dto.ProductDTO;
import com.sh.sh.pos.system.repository.ProductRepository;
import com.sh.sh.pos.system.repository.StoreRepository;
import com.sh.sh.pos.system.service.ProductService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor 
public class ProductServiceImpl implements ProductService {
	
	private final ProductRepository productRepository;

	private final StoreRepository storeRepository;
	@Override
	public ProductDTO createProduct(ProductDTO productDTO, User user) throws Exception {
		Store store = storeRepository.findById(productDTO.getStoreId()).orElseThrow(
				() -> new Exception("Store not found ")
				)
		return null;
	} 

	@Override
	public ProductDTO updateProduct(Long id, ProductDTO productDTO, User user) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void deleteProduct(Long id, User user) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public List<ProductDTO> getProductByStoreId(Long storeId) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<ProductDTO> searchBykeyword(Long storeId, String keyword) {
		// TODO Auto-generated method stub
		return null;
	}

}
