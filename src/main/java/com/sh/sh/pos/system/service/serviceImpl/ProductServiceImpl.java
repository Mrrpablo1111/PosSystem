package com.sh.sh.pos.system.service.serviceImpl;

import java.nio.file.AccessDeniedException;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.sh.sh.pos.system.domain.UnitType;
import com.sh.sh.pos.system.domain.UserRole;
import com.sh.sh.pos.system.mapper.ProductMapper;
import com.sh.sh.pos.system.model.Category;
import com.sh.sh.pos.system.model.Store;
import com.sh.sh.pos.system.model.User;
import com.sh.sh.pos.system.model.products.Product;
import com.sh.sh.pos.system.model.suppliers.Supplier;
import com.sh.sh.pos.system.payload.dto.productDTO.ProductDTO;
import com.sh.sh.pos.system.repository.CategoryRepository;
import com.sh.sh.pos.system.repository.ProductRepository;
import com.sh.sh.pos.system.repository.StoreRepository;
import com.sh.sh.pos.system.repository.suppliersRepository.SupplierRepository;
import com.sh.sh.pos.system.service.ProductService.ProductService;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

	private final ProductRepository productRepository;

	private final StoreRepository storeRepository;
	private final SupplierRepository supplierRepository;

	private final CategoryRepository categoryRepository;

	@Override
	public ProductDTO createProduct(ProductDTO productDTO, User user) throws AccessDeniedException {
		validateSkuUnique(productDTO.getSku(), null);
		validateBarcodeUnique(productDTO.getBarcode(), null);

		Store store = findStore(productDTO.getStoreId());
		Category category = findCategory(productDTO.getCategoryId());
		Supplier supplier = resolveSupplier(productDTO.getSupplierId());

		Product saved = productRepository.save(
				ProductMapper.toEntity(productDTO, store, supplier, category));
		return ProductMapper.toDTO(saved);
	}

	@Override
	public ProductDTO getProductById(Long id) {
		return ProductMapper.toDTO(findProduct(id));
	}

	@Override
	public ProductDTO getProductByBarcode(String barcode) {
		Product product = productRepository.findByBarcode(barcode)
				.orElseThrow(() -> new EntityNotFoundException("Product not found for barcode: " + barcode));
		return ProductMapper.toDTO(product);
	}

	@Override
	@Transactional
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
	public ProductDTO updateProduct(Long id, ProductDTO productDTO, User user) throws AccessDeniedException {
		Product product = productRepository.findById(id).orElseThrow(
				() -> new EntityNotFoundException("product not found"));
		checkAuthority(product.getStore(), user);

		validateSkuUnique(productDTO.getSku(), id);
		validateBarcodeUnique(productDTO.getBarcode(), id);
		product.setName(productDTO.getName());
		product.setDescription(productDTO.getDescription());
		product.setSku(productDTO.getSku());
		product.setBarcode(productDTO.getBarcode());
		product.setImage(productDTO.getImage());
		product.setMrp(productDTO.getMrp());
		product.setSellingPrice(productDTO.getSellingPrice());
		product.setBrand(productDTO.getBrand());
		product.setSellingPrice(productDTO.getSellingPrice());
		product.setCostPrice(productDTO.getCostPrice());
		product.setUnit(UnitType.valueOf(productDTO.getUnit()));
		product.setDefaultReorderLevel(productDTO.getDefaultReorderLevel());
		product.setActive(productDTO.getActive());

		product.setWeight(productDTO.getWeight());

		// apply-relations
		product.setSupplier(resolveSupplier(productDTO.getSupplierId()));
		if (productDTO.getCategoryId() != null) {
			product.setCategory(findCategory(productDTO.getCategoryId()));
		}

		return ProductMapper.toDTO(productRepository.save(product));
	}

	@Override
	public void deleteProduct(Long id, User user) throws AccessDeniedException {
		Product product = findProduct(id);
		checkAuthority(product.getStore(), user);
		productRepository.deleteById(id);
	}

	public void checkAuthority(Store store, User user) throws AccessDeniedException {

		boolean isManager = user.getRole() == UserRole.ROLE_STORE_MANAGER
				&& user.getStore().getId().equals(store.getId());

		boolean isAdmin = user.getRole() == UserRole.ROLE_STORE_ADMIN
				&& store.getStoreAdmin().getId().equals(user.getId());

		if (!isManager && !isAdmin) {
			throw new AccessDeniedException("You are not authorized to manage this store.");
		}

	}

	// private-helper
	private void validateSkuUnique(String sku, Long excludeId) {
		boolean exists = excludeId == null
				? productRepository.existsBySku(sku)
				: productRepository.existsBySkuAndIdNot(sku, excludeId);
		if (exists)
			throw new IllegalArgumentException("SKU already exists: " + sku);
	}

	private Product findProduct(Long id) {
		return productRepository.findById(id)
				.orElseThrow(() -> new EntityNotFoundException("Product not found with id: " + id));
	}

	private Store findStore(Long storeId) {
		return storeRepository.findById(storeId)
				.orElseThrow(() -> new EntityNotFoundException("Store not found with id: " + storeId));
	}

	private Category findCategory(Long categoryId) {
		return categoryRepository.findById(categoryId)
				.orElseThrow(() -> new EntityNotFoundException("Category not found with id: " + categoryId));
	}

	private Supplier resolveSupplier(Long supplierId) {
		if (supplierId == null)
			return null;
		return supplierRepository.findById(supplierId)
				.orElseThrow(() -> new EntityNotFoundException("Supplier not found with id: " + supplierId));
	}

	private void validateBarcodeUnique(String barcode, Long excludeId) {
		if (barcode == null)
			return;
		boolean exists = excludeId == null
				? productRepository.findByBarcode(barcode).isPresent()
				: productRepository.existsByBarcodeAndIdNot(barcode, excludeId);
		if (exists)
			throw new IllegalArgumentException("Barcode already exists: " + barcode);
	}
}
