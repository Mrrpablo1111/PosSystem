package com.sh.sh.pos.system.service.serviceimpl.productserviceimpl;

import java.util.List;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
	@Transactional(readOnly = true)
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
	@Transactional(readOnly = true)
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

		// Uniqueness is enforced whenever a non-null SKU/barcode is submitted,
		// even if the value is the same as the current record. This keeps the
		// update contract consistent with the repository validation and tests.
		if (productDTO.getSku() != null) {
			validateSkuUnique(productDTO.getSku(), id);
		}
		if (productDTO.getBarcode() != null) {
			validateBarcodeUnique(productDTO.getBarcode(), id);
		}

		// ── Partial update: null = "keep the current value" ────────────────
		// A client that omits a field never wipes it, and NOT NULL columns
		// (like active) can no longer be nulled by an incomplete payload.
		applyIfPresent(productDTO.getName(), product::setName);
		applyIfPresent(productDTO.getDescription(), product::setDescription);
		applyIfPresent(productDTO.getSku(), product::setSku);
		applyIfPresent(productDTO.getBarcode(), product::setBarcode);
		applyIfPresent(productDTO.getImage(), product::setImage);
		applyIfPresent(productDTO.getMrp(), product::setMrp);
		applyIfPresent(productDTO.getSellingPrice(), product::setSellingPrice);
		applyIfPresent(productDTO.getCostPrice(), product::setCostPrice);
		applyIfPresent(productDTO.getBrand(), product::setBrand);
		applyIfPresent(productDTO.getDefaultReorderLevel(), product::setDefaultReorderLevel);
		applyIfPresent(productDTO.getActive(), product::setActive);
		applyIfPresent(productDTO.getWeight(), product::setWeight);

		if (productDTO.getUnit() != null && !productDTO.getUnit().isBlank()) {
			try {
				product.setUnit(UnitType.valueOf(productDTO.getUnit().toUpperCase()));
			} catch (IllegalArgumentException e) {
				throw new UserException("Invalid unit type: '" + productDTO.getUnit()
						+ "'. Valid values: " + java.util.Arrays.toString(UnitType.values()));
			}
		}

		// ── Relations ──────────────────────────────────────────────────────
		// supplierId null = "leave supplier as is". If the UI ever needs a
		// "remove supplier" action, expose it explicitly (e.g. a dedicated
		// endpoint or a clearSupplier flag) rather than treating null as clear.
		if (productDTO.getSupplierId() != null) {
			product.setSupplier(resolveSupplier(productDTO.getSupplierId()));
		}
		if (productDTO.getCategoryId() != null) {
			product.setCategory(findCategory(productDTO.getCategoryId()));
		}

		return ProductMapper.toDTO(productRepository.save(product));
	}

	/** Applies a value to a setter only when the value is non-null. */
	private static <T> void applyIfPresent(T value, java.util.function.Consumer<T> setter) {
		if (value != null) {
			setter.accept(value);
		}
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