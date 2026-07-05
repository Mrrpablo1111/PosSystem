package com.sh.sh.pos.system.service.serviceimpl.productserviceimpl;

import java.util.List;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import com.sh.sh.pos.system.domain.UnitType;
import com.sh.sh.pos.system.domain.UserRole;
import com.sh.sh.pos.system.exception.ResourceNotFoundException;
import com.sh.sh.pos.system.exception.UserException;
import com.sh.sh.pos.system.mapper.productmapper.ProductMapper;
import com.sh.sh.pos.system.model.category.Category;
import com.sh.sh.pos.system.model.products.Product;
import com.sh.sh.pos.system.model.store.Store;
import com.sh.sh.pos.system.model.suppliers.Supplier;
import com.sh.sh.pos.system.model.user.User;
import com.sh.sh.pos.system.payload.dto.productdto.ProductDTO;
import com.sh.sh.pos.system.repository.categoryrepository.CategoryRepository;
import com.sh.sh.pos.system.repository.productrepository.ProductRepository;
import com.sh.sh.pos.system.repository.storerepository.StoreRepository;
import com.sh.sh.pos.system.repository.suppliersrepository.SupplierRepository;
import com.sh.sh.pos.system.service.productservice.ProductService;

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
	@Transactional
	public ProductDTO createProduct(ProductDTO productDTO, User user) {
		validateSkuUnique(productDTO.getSku(), null);
		validateBarcodeUnique(productDTO.getBarcode(), null);

		Store store = findStore(productDTO.getStoreId());
		checkAuthority(store, user);

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
				.orElseThrow(() -> new ResourceNotFoundException(
						"Product not found for barcode: " + barcode));
		return ProductMapper.toDTO(product);
	}

	@Override
	@Transactional
	public List<ProductDTO> getProductByStoreId(Long storeId) {
		return productRepository.findByStoreId(storeId)
				.stream()
				.map(ProductMapper::toDTO)
				.toList();
	}

	@Override
	public List<ProductDTO> searchBykeyword(Long storeId, String keyword) {
		return productRepository.searchByKeyword(storeId, keyword)
				.stream()
				.map(ProductMapper::toDTO)
				.toList();
	}

	@Override
	@Transactional
	public ProductDTO updateProduct(Long id, ProductDTO productDTO, User user) {
		Product product = findProduct(id);
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
		product.setCostPrice(productDTO.getCostPrice());
		product.setBrand(productDTO.getBrand());
		product.setUnit(UnitType.valueOf(productDTO.getUnit()));
		product.setDefaultReorderLevel(productDTO.getDefaultReorderLevel());
		product.setActive(productDTO.getActive());
		product.setWeight(productDTO.getWeight());

		// apply relations
		product.setSupplier(resolveSupplier(productDTO.getSupplierId()));
		if (productDTO.getCategoryId() != null) {
			product.setCategory(findCategory(productDTO.getCategoryId()));
		}

		return ProductMapper.toDTO(productRepository.save(product));
	}

	@Override
	@Transactional
	public void deleteProduct(Long id, User user) {
		Product product = findProduct(id);
		checkAuthority(product.getStore(), user);
		productRepository.delete(product);
	}

	// ── Authorization ─────────────────────────────────────────────────────

	private void checkAuthority(Store store, User user) {
		boolean isManager = user.getRole() == UserRole.ROLE_STORE_MANAGER
				&& user.getStore() != null
				&& user.getStore().getId().equals(store.getId());

		boolean isAdmin = user.getRole() == UserRole.ROLE_STORE_ADMIN
				&& store.getStoreAdmin() != null
				&& store.getStoreAdmin().getId().equals(user.getId());

		if (!isManager && !isAdmin) {
			throw new AccessDeniedException("You are not authorized to manage this store.");
		}
	}

	// ── Private helpers ───────────────────────────────────────────────────

	private void validateSkuUnique(String sku, Long excludeId) {
		boolean exists = excludeId == null
				? productRepository.existsBySku(sku)
				: productRepository.existsBySkuAndIdNot(sku, excludeId);
		if (exists) {
			throw new UserException("SKU already exists: " + sku);
		}
	}

	private void validateBarcodeUnique(String barcode, Long excludeId) {
		if (barcode == null) {
			return;
		}
		boolean exists = excludeId == null
				? productRepository.findByBarcode(barcode).isPresent()
				: productRepository.existsByBarcodeAndIdNot(barcode, excludeId);
		if (exists) {
			throw new UserException("Barcode already exists: " + barcode);
		}
	}

	private Product findProduct(Long id) {
		return productRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));
	}

	private Store findStore(Long storeId) {
		return storeRepository.findById(storeId)
				.orElseThrow(() -> new ResourceNotFoundException("Store not found with id: " + storeId));
	}

	private Category findCategory(Long categoryId) {
		return categoryRepository.findById(categoryId)
				.orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + categoryId));
	}

	private Supplier resolveSupplier(Long supplierId) {
		if (supplierId == null) {
			return null;
		}
		return supplierRepository.findById(supplierId)
				.orElseThrow(() -> new ResourceNotFoundException("Supplier not found with id: " + supplierId));
	}
}