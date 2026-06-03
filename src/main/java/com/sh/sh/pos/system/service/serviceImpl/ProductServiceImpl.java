package com.sh.sh.pos.system.service.serviceImpl;

import java.nio.file.AccessDeniedException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.sh.sh.pos.system.domain.UserRole;
import com.sh.sh.pos.system.mapper.ProductMapper;
import com.sh.sh.pos.system.model.Category;
import com.sh.sh.pos.system.model.Product;
import com.sh.sh.pos.system.model.Store;
import com.sh.sh.pos.system.model.User;
import com.sh.sh.pos.system.payload.dto.ProductDTO;
import com.sh.sh.pos.system.repository.CategoryRepository;
import com.sh.sh.pos.system.repository.ProductRepository;
import com.sh.sh.pos.system.repository.StoreRepository;
import com.sh.sh.pos.system.service.ProductService;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

	private final ProductRepository productRepository;

	private final StoreRepository storeRepository;

	private final CategoryRepository categoryRepository;

	@Override
	public ProductDTO createProduct(ProductDTO productDTO, User user) throws AccessDeniedException {
		Store store = storeRepository.findById(productDTO.getStoreId()).orElseThrow(
				() -> new EntityNotFoundException("Store not found "));

		Category category = categoryRepository.findById(productDTO.getCategoryId()).orElseThrow(
				() -> new EntityNotFoundException("Category not found"));
		Product product = ProductMapper.toEntity(productDTO, store, category);
		Product savedProduct = productRepository.save(product);
		return ProductMapper.toDTO(savedProduct);
	}

	@Override
	public ProductDTO updateProduct(Long id, ProductDTO productDTO, User user) throws AccessDeniedException {
		Product product = productRepository.findById(id).orElseThrow(
				() -> new EntityNotFoundException("product not found"));
		checkAuthority(product.getStore(), user);

		product.setName(productDTO.getName());
		product.setDescription(productDTO.getDescription());
		product.setSku(productDTO.getSku());
		product.setImage(productDTO.getImage());
		product.setMrp(productDTO.getMrp());
		product.setSellingPrice(productDTO.getSellingPrice());
		product.setBrand(productDTO.getBrand());
		product.setUpdatedAt(LocalDateTime.now());
		if (productDTO.getCategoryId() != null) {
			Category category = categoryRepository.findById(productDTO.getCategoryId()).orElseThrow(
					() -> new EntityNotFoundException("category not found"));
			product.setCategory(category);
		}
		return ProductMapper.toDTO(productRepository.save(product));
	}

	@Override
	public void deleteProduct(Long id, User user) throws AccessDeniedException {
		Product product = productRepository.findById(id).orElseThrow(
				() -> new EntityNotFoundException("product not found"));
			
		checkAuthority(product.getStore(), user);	
		productRepository.deleteById(id);

	}

	@Override
	public List<ProductDTO> getProductByStoreId(Long storeId) {


		return productRepository.findByStoreId(storeId)
				.stream()
				.map(ProductMapper::toDTO)
				.collect(Collectors.toList());
	}

	@Override
	public List<ProductDTO> searchBykeyword(Long storeId, String keyword) {
		return productRepository.searchByKeyword(storeId, keyword)
				.stream()
				.map(ProductMapper::toDTO)
				.collect(Collectors.toList());
	}

	@Override
	public ProductDTO getProductById(Long id) {
		Product product = productRepository.findById(id).orElseThrow(()->new EntityNotFoundException("Product not found"));
		return ProductMapper.toDTO(product);
	}

	public void checkAuthority(Store store, User user) throws AccessDeniedException {

		if (user.getRole() == UserRole.ROLE_STORE_MANAGER
				&& user.getStore().getId().equals(store.getId())) {
			return;
		}

		if (user.getRole() == UserRole.ROLE_STORE_ADMIN
				&& store.getStoreAdmin().getId().equals(user.getId())) {
			return;
		}

		throw new AccessDeniedException("You are not authorized to manage this store.");

	}

}
