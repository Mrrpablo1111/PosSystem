package com.sh.sh.pos.system.service.serviceimpl.purchasesOrderServiceImpl;



import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import com.sh.sh.pos.system.mapper.purchasesordermapper.PurchaseOrderMapper;
import com.sh.sh.pos.system.model.branch.Branch;
import com.sh.sh.pos.system.model.currencies.Currency;
import com.sh.sh.pos.system.model.products.Product;
import com.sh.sh.pos.system.model.purchasesorder.PurchaseOrder;
import com.sh.sh.pos.system.model.purchasesorderitem.PurchaseOrderItem;
import com.sh.sh.pos.system.model.suppliers.Supplier;
import com.sh.sh.pos.system.payload.request.CreatePurchaseOrderRequest;
import com.sh.sh.pos.system.payload.request.CreatePurchaseOrderRequest.PurchaseOrderItemRequest;
import com.sh.sh.pos.system.repository.branchrepository.BranchRepository;
import com.sh.sh.pos.system.repository.currenciesrepository.CurrencyRepository;
import com.sh.sh.pos.system.repository.productrepository.ProductRepository;
import com.sh.sh.pos.system.repository.productvariantrepository.ProductVariantRepository;
import com.sh.sh.pos.system.repository.purchasesorderrepository.PurchaseOrderRepository;
import com.sh.sh.pos.system.repository.suppliersrepository.SupplierRepository;
import com.sh.sh.pos.system.service.emailservice.EmailService;
import com.sh.sh.pos.system.service.serviceimpl.purchasesorderserviceimpl.PurchaseOrderServiceImpl;

/**
 * Tests the money calculation logic in PurchaseOrderServiceImpl.
 * This is HIGH VALUE to test — wrong totals = wrong invoices = real money bugs.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("PurchaseOrderServiceImpl — calculations")
class PurchaseOrderServiceImplTest {

    @Mock private PurchaseOrderRepository  purchaseOrderRepository;
    @Mock private SupplierRepository       supplierRepository;
    @Mock private BranchRepository         branchRepository;
    @Mock private CurrencyRepository       currencyRepository;
    @Mock private ProductRepository        productRepository;
    @Mock private ProductVariantRepository variantRepository;
    @Mock private PurchaseOrderMapper      mapper;
    @Mock private EmailService             emailService;

    @InjectMocks
    private PurchaseOrderServiceImpl service;

    private Supplier supplier;
    private Branch    branch;
    private Currency  currency;
    private Product   product;

    @BeforeEach
    void setUp() {
        supplier = new Supplier();
        supplier.setId(1L);

        branch = new Branch();
        branch.setId(1L);

        currency = new Currency();
        currency.setId(1L);
        currency.setActive(true);
        currency.setSymbol("$");

        product = new Product();
        product.setId(1L);
        product.setName("Test Product");

        // FIX: only stub what EVERY test needs.
        // currencyRepository.findById() moved into individual tests
        // that actually use a non-null currencyId.
        when(supplierRepository.findById(1L)).thenReturn(Optional.of(supplier));
        when(branchRepository.findById(1L)).thenReturn(Optional.of(branch));
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
    }

    // ── Subtotal calculation ──────────────────────────────────────────────────

    @Test
    @DisplayName("subtotal = sum of all item totalCost")
    void create_calculatesSubtotalCorrectly() {
        // FIX: stub currency lookup only here, since this test uses currencyId=1
        when(currencyRepository.findById(1L)).thenReturn(Optional.of(currency));

        CreatePurchaseOrderRequest req = buildRequest(
                item(10, new BigDecimal("5.00"), BigDecimal.ZERO),
                item(3,  new BigDecimal("20.00"), BigDecimal.ZERO)
        );

        stubSaveAndMapper();

        service.create(req);

        ArgumentCaptor<PurchaseOrder> captor = ArgumentCaptor.forClass(PurchaseOrder.class);
        Mockito.verify(purchaseOrderRepository).save(captor.capture());

        BigDecimal expectedSubtotal = new BigDecimal("110.00");
        assertThat(captor.getValue().getSubtotal())
                .isEqualByComparingTo(expectedSubtotal);
    }

    // ── Discount percentage on line item ──────────────────────────────────────

    @ParameterizedTest(name = "qty={0} unitCost={1} discountPct={2} → expectedTotal={3}")
    @DisplayName("item totalCost applies discount percentage correctly")
    @CsvSource({
            "10, 5.00,  0,  50.00",
            "10, 5.00,  10, 45.00",
            "10, 5.00,  50, 25.00",
            "4,  25.00, 20, 80.00",
    })
    void itemTotalCost_appliesDiscountCorrectly(
            int qty, BigDecimal unitCost, BigDecimal discountPct, BigDecimal expectedTotal) {

        // FIX: stub currency lookup only here
        when(currencyRepository.findById(1L)).thenReturn(Optional.of(currency));

        CreatePurchaseOrderRequest req = buildRequest(item(qty, unitCost, discountPct));

        stubSaveAndMapper();

        service.create(req);

        ArgumentCaptor<PurchaseOrder> captor = ArgumentCaptor.forClass(PurchaseOrder.class);
        Mockito.verify(purchaseOrderRepository).save(captor.capture());

        PurchaseOrderItem savedItem = captor.getValue().getItems().get(0);
        assertThat(savedItem.getTotalCost()).isEqualByComparingTo(expectedTotal);
    }

    // ── Currency fallback when not provided ───────────────────────────────────

    @Test
    @DisplayName("currencyId null falls back to first active currency")
    void create_currencyIdNull_fallsBackToActiveCurrency() {
        // This test does NOT stub findById() — it should never be called
        when(currencyRepository.findByActiveTrue()).thenReturn(List.of(currency));

        CreatePurchaseOrderRequest req = buildRequest(item(1, BigDecimal.TEN, BigDecimal.ZERO));
        req.setCurrencyId(null);

        stubSaveAndMapper();

        service.create(req);

        Mockito.verify(currencyRepository).findByActiveTrue();
        Mockito.verify(currencyRepository, Mockito.never()).findById(any());
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private void stubSaveAndMapper() {
        when(purchaseOrderRepository.save(any(PurchaseOrder.class)))
                .thenAnswer(inv -> inv.getArgument(0));
        when(mapper.toEntity(any(), any(), any(), any(), any()))
                .thenAnswer(inv -> {
                    PurchaseOrder po = new PurchaseOrder();
                    po.setItems(inv.getArgument(4));
                    return po;
                });
        when(mapper.toDTO(any())).thenReturn(null);
    }

    private CreatePurchaseOrderRequest buildRequest(PurchaseOrderItemRequest... items) {
        CreatePurchaseOrderRequest req = new CreatePurchaseOrderRequest();
        req.setSupplierId(1L);
        req.setBranchId(1L);
        req.setCurrencyId(1L);
        req.setItems(List.of(items));
        return req;
    }

    private PurchaseOrderItemRequest item(int qty, BigDecimal unitCost, BigDecimal discountPct) {
        PurchaseOrderItemRequest item = new PurchaseOrderItemRequest();
        item.setProductId(1L);
        item.setRequestedQuantity(qty);
        item.setUnitCost(unitCost);
        item.setDiscountPct(discountPct);
        return item;
    }
}