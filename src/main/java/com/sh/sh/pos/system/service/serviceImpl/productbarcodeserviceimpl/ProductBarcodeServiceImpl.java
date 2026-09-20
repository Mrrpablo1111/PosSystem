package com.sh.sh.pos.system.service.serviceimpl.productbarcodeserviceimpl;


import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sh.sh.pos.system.exception.ResourceNotFoundException;
import com.sh.sh.pos.system.exception.UserException;
import com.sh.sh.pos.system.mapper.productbarcodemapper.ProductBarcodeMapper;
import com.sh.sh.pos.system.model.products.Product;
import com.sh.sh.pos.system.model.products.ProductBarcode;
import com.sh.sh.pos.system.model.products.ProductVariant;
import com.sh.sh.pos.system.payload.dto.productdto.BarcodeLookupDTO;
import com.sh.sh.pos.system.payload.dto.productdto.ProductBarcodeDTO;
import com.sh.sh.pos.system.repository.inventoryrepository.InventoryRepository;
import com.sh.sh.pos.system.repository.productbarcoderepository.ProductBarcodeRepository;
import com.sh.sh.pos.system.repository.productrepository.ProductRepository;
import com.sh.sh.pos.system.repository.productvariantrepository.ProductVariantRepository;
import com.sh.sh.pos.system.service.productservice.ProductBarcodeService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ProductBarcodeServiceImpl implements ProductBarcodeService {

    private final ProductBarcodeRepository barcodeRepository;
    private final ProductRepository productRepository;
    private final ProductVariantRepository variantRepository;
    private final InventoryRepository inventoryRepository;

    // ── Writes ────────────────────────────────────────────────────────────

    @Override
    @Transactional
    public ProductBarcodeDTO create(ProductBarcodeDTO dto) {
        if (dto.getBarcode() == null || dto.getBarcode().isBlank()) {
            throw new UserException("Barcode is required.");
        }

        Product product = findProductOrThrow(dto.getProductId());
        ProductVariant variant = resolveVariant(dto.getVariantId(), product);

        // Uniqueness across the whole registry AND the legacy columns
        validateBarcodeUnique(dto.getBarcode());

        ProductBarcode entity = ProductBarcodeMapper.toEntity(dto, product, variant);

        if (Boolean.TRUE.equals(entity.getPrimary())) {
            clearPrimaryFlag(product.getId(), dto.getVariantId());
        }

        return ProductBarcodeMapper.toDTO(barcodeRepository.save(entity));
    }

    @Override
    @Transactional
    public ProductBarcodeDTO update(Long id, ProductBarcodeDTO dto) {
        ProductBarcode entity = findBarcodeOrThrow(id);

        if (dto.getBarcode() != null && !dto.getBarcode().equals(entity.getBarcode())) {
            validateBarcodeUnique(dto.getBarcode());
            entity.setBarcode(dto.getBarcode());
        }
        if (dto.getSource() != null) {
            entity.setSource(ProductBarcodeMapper.parseSource(dto.getSource()));
        }
        if (dto.getNote() != null) entity.setNote(dto.getNote());
        if (dto.getActive() != null) entity.setActive(dto.getActive());

        if (Boolean.TRUE.equals(dto.getPrimary()) && !Boolean.TRUE.equals(entity.getPrimary())) {
            clearPrimaryFlag(entity.getProduct().getId(),
                    entity.getVariant() != null ? entity.getVariant().getId() : null);
            entity.setPrimary(true);
        } else if (Boolean.FALSE.equals(dto.getPrimary())) {
            entity.setPrimary(false);
        }

        return ProductBarcodeMapper.toDTO(barcodeRepository.save(entity));
    }

    @Override
    @Transactional
    public void delete(Long id) {
        barcodeRepository.delete(findBarcodeOrThrow(id));
    }

    // ── Reads ─────────────────────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public List<ProductBarcodeDTO> getByProduct(Long productId) {
        return ProductBarcodeMapper.toDTOList(
                barcodeRepository.findByProductIdOrderByPrimaryDescCreatedAtAsc(productId));
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductBarcodeDTO> getByVariant(Long variantId) {
        return ProductBarcodeMapper.toDTOList(barcodeRepository.findByVariantId(variantId));
    }

    /**
     * Register scan resolution, in priority order:
     *   1. the barcode registry (this module)
     *   2. product_variants.barcode (legacy per-variant column)
     *   3. products.barcode (legacy product column)
     */
    @Override
    @Transactional(readOnly = true)
    public BarcodeLookupDTO lookup(String barcode) {
        // 1 — registry
        var registered = barcodeRepository.findByBarcodeAndActiveTrue(barcode);
        if (registered.isPresent()) {
            ProductBarcode pb = registered.get();
            return buildLookup(barcode, "PRODUCT_BARCODE",
                    pb.getSource() != null ? pb.getSource().name() : null,
                    pb.getProduct(), pb.getVariant());
        }

        // 2 — variant column
        var variant = variantRepository.findByBarcode(barcode);
        if (variant.isPresent()) {
            ProductVariant v = variant.get();
            return buildLookup(barcode, "VARIANT", null, v.getProduct(), v);
        }

        // 3 — product column
        Product product = productRepository.findByBarcode(barcode)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No product found for barcode: " + barcode));
        return buildLookup(barcode, "PRODUCT", null, product, null);
    }

    // ── Helpers ───────────────────────────────────────────────────────────

    private BarcodeLookupDTO buildLookup(String barcode, String resolvedFrom,
                                         String barcodeSource,
                                         Product product, ProductVariant variant) {
        return BarcodeLookupDTO.builder()
                .barcode(barcode)
                .resolvedFrom(resolvedFrom)
                .barcodeSource(barcodeSource)
                .productId(product.getId())
                .productName(product.getName())
                .variantId(variant != null ? variant.getId() : null)
                .variantName(variant != null ? variant.getName() : null)
                .sku(variant != null ? variant.getSku() : product.getSku())
                .sellingPrice(variant != null && variant.getSellingPrice() != null
                        ? variant.getSellingPrice() : product.getSellingPrice())
                .costPrice(variant != null && variant.getCostPrice() != null
                        ? variant.getCostPrice() : product.getCostPrice())
                .image(product.getImage())
                .currentStock(inventoryRepository.sumQuantityForItem(
                        product.getId(), variant != null ? variant.getId() : null))
                .build();
    }

    private void validateBarcodeUnique(String barcode) {
        if (barcodeRepository.existsByBarcode(barcode)
                || variantRepository.existsByBarcode(barcode)
                || productRepository.existsByBarcode(barcode)) {
            throw new UserException("Barcode '" + barcode + "' is already in use.");
        }
    }

    /** Only one primary per product/variant scope. */
    private void clearPrimaryFlag(Long productId, Long variantId) {
        List<ProductBarcode> siblings = variantId != null
                ? barcodeRepository.findByProductIdAndVariantId(productId, variantId)
                : barcodeRepository.findByProductIdOrderByPrimaryDescCreatedAtAsc(productId);
        siblings.stream()
                .filter(b -> Boolean.TRUE.equals(b.getPrimary()))
                .forEach(b -> { b.setPrimary(false); barcodeRepository.save(b); });
    }

    private ProductVariant resolveVariant(Long variantId, Product product) {
        if (variantId == null) return null;
        ProductVariant variant = variantRepository.findById(variantId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Product variant not found with id: " + variantId));
        if (!variant.getProduct().getId().equals(product.getId())) {
            throw new UserException("Variant " + variantId
                    + " does not belong to product " + product.getId() + ".");
        }
        return variant;
    }

    private Product findProductOrThrow(Long id) {
        if (id == null) throw new UserException("productId is required.");
        return productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));
    }

    private ProductBarcode findBarcodeOrThrow(Long id) {
        return barcodeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Product barcode not found with id: " + id));
    }
}
