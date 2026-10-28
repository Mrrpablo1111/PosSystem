package com.sh.sh.pos.system.security;


import java.util.Arrays;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.sh.sh.pos.system.domain.UserRole;

/**
 * Single source of truth for what each role is allowed to do.
 * <p>
 * Controllers check PERMISSIONS (e.g. hasAuthority('GOODS_RECEIPT_APPROVE')),
 * never roles — so tightening or loosening access means editing this file
 * only, without touching any controller.
 * <p>
 * Wire-up (in your UserDetailsService / JWT filter):
 * <p>
 * List<GrantedAuthority> authorities = new ArrayList<>();
 * authorities.add(new SimpleGrantedAuthority(user.getRole().name()));
 * RolePermissions.permissionsFor(user.getRole())
 * .forEach(p -> authorities.add(new SimpleGrantedAuthority(p)));
 */
public final class RolePermissions {

    private RolePermissions() {
    }

    // ═══════════════════════════════════════════════════════════════════════
    // Permission constants — grouped by module
    // ═══════════════════════════════════════════════════════════════════════

    // ── Catalog ───────────────────────────────────────────────────────────
    public static final String PRODUCT_VIEW = "PRODUCT_VIEW";
    public static final String PRODUCT_CREATE = "PRODUCT_CREATE";
    public static final String PRODUCT_UPDATE = "PRODUCT_UPDATE";
    public static final String PRODUCT_DELETE = "PRODUCT_DELETE";

    public static final String CATEGORY_VIEW = "CATEGORY_VIEW";
    public static final String CATEGORY_MANAGE = "CATEGORY_MANAGE";   // create/update/delete

    // ── Partners ──────────────────────────────────────────────────────────
    public static final String SUPPLIER_VIEW = "SUPPLIER_VIEW";
    public static final String SUPPLIER_MANAGE = "SUPPLIER_MANAGE";   // create/update/block/activate

    // ── Locations ─────────────────────────────────────────────────────────
    public static final String WAREHOUSE_VIEW = "WAREHOUSE_VIEW";
    public static final String WAREHOUSE_MANAGE = "WAREHOUSE_MANAGE";
    public static final String BRANCH_VIEW = "BRANCH_VIEW";
    public static final String BRANCH_MANAGE = "BRANCH_MANAGE";

    // ── Stock ─────────────────────────────────────────────────────────────
    public static final String INVENTORY_VIEW = "INVENTORY_VIEW";   // includes transaction ledger reads
    public static final String INVENTORY_MANAGE = "INVENTORY_MANAGE"; // create rows / settings
    public static final String INVENTORY_ADJUST = "INVENTORY_ADJUST"; // manual quantity corrections

    public static final String PRODUCT_BATCH_VIEW = "PRODUCT_BATCH_VIEW";
    public static final String PRODUCT_BATCH_MANAGE = "PRODUCT_BATCH_MANAGE";

    // ── Purchasing ────────────────────────────────────────────────────────
    public static final String PURCHASE_ORDER_VIEW = "PURCHASE_ORDER_VIEW";
    public static final String PURCHASE_ORDER_CREATE = "PURCHASE_ORDER_CREATE";
    public static final String PURCHASE_ORDER_UPDATE = "PURCHASE_ORDER_UPDATE";
    public static final String PURCHASE_ORDER_DELETE = "PURCHASE_ORDER_DELETE";
    public static final String PURCHASE_ORDER_APPROVE = "PURCHASE_ORDER_APPROVE";
    public static final String PURCHASE_ORDER_SEND = "PURCHASE_ORDER_SEND";

    public static final String GOODS_RECEIPT_VIEW = "GOODS_RECEIPT_VIEW";
    public static final String GOODS_RECEIPT_CREATE = "GOODS_RECEIPT_CREATE";
    public static final String GOODS_RECEIPT_UPDATE = "GOODS_RECEIPT_UPDATE";
    public static final String GOODS_RECEIPT_APPROVE = "GOODS_RECEIPT_APPROVE";
    public static final String GOODS_RECEIPT_DELETE = "GOODS_RECEIPT_DELETE";
    public static final String GOODS_RECEIPT_REPORT = "GOODS_RECEIPT_REPORT";

    public static final String PURCHASE_INVOICE_VIEW = "PURCHASE_INVOICE_VIEW";
    public static final String PURCHASE_INVOICE_CREATE = "PURCHASE_INVOICE_CREATE";
    public static final String PURCHASE_INVOICE_UPDATE = "PURCHASE_INVOICE_UPDATE";
    public static final String PURCHASE_INVOICE_DELETE = "PURCHASE_INVOICE_DELETE";
    public static final String PURCHASE_INVOICE_POST = "PURCHASE_INVOICE_POST";

    // ── Supplier finance ──────────────────────────────────────────────────
    public static final String SUPPLIER_PAYMENT_VIEW = "SUPPLIER_PAYMENT_VIEW";
    public static final String SUPPLIER_PAYMENT_CREATE = "SUPPLIER_PAYMENT_CREATE";
    public static final String SUPPLIER_LEDGER_VIEW = "SUPPLIER_LEDGER_VIEW";

    // ── Sales (POS front) ─────────────────────────────────────────────────
    public static final String SALE_VIEW = "SALE_VIEW";
    public static final String SALE_CREATE = "SALE_CREATE";
    public static final String SALE_REFUND = "SALE_REFUND";

    // ── System ────────────────────────────────────────────────────────────
    public static final String CURRENCY_VIEW = "CURRENCY_VIEW";
    public static final String CURRENCY_MANAGE = "CURRENCY_MANAGE";
    public static final String USER_VIEW = "USER_VIEW";
    public static final String USER_MANAGE = "USER_MANAGE";
    public static final String HR_VIEW = "HR_VIEW";           // employees / salaries
    public static final String HR_MANAGE = "HR_MANAGE";
    public static final String REPORT_VIEW = "REPORT_VIEW";       // dashboards / exports


    // ── Store management ──────────────────────────────────
    public static final String STORE_VIEW = "STORE_VIEW";
    public static final String STORE_UPDATE = "STORE_UPDATE";
    public static final String STORE_EMPLOYEE_MANAGE = "STORE_EMPLOYEE_MANAGE";
    public static final String STORE_STATUS_MANAGE = "STORE_STATUS_MANAGE";
    public static final String STORE_DELETE = "STORE_DELETE";

    /**
     * Legacy authority: InventoryController still checks
     * hasAuthority('STORE_MANAGER'). Kept so nothing breaks — remove once
     * that controller is migrated to INVENTORY_* permissions.
     */
    public static final String LEGACY_STORE_MANAGER = "STORE_MANAGER";

    // ═══════════════════════════════════════════════════════════════════════
    // Building blocks — compose role sets out of these
    // ═══════════════════════════════════════════════════════════════════════

    private static final Set<String> CATALOG_READ = Set.of(
            PRODUCT_VIEW, CATEGORY_VIEW, PRODUCT_BATCH_VIEW);

    private static final Set<String> CATALOG_MANAGE = union(CATALOG_READ, Set.of(
            PRODUCT_CREATE, PRODUCT_UPDATE, PRODUCT_DELETE,
            CATEGORY_MANAGE, PRODUCT_BATCH_MANAGE));

    private static final Set<String> STOCK_READ = Set.of(
            INVENTORY_VIEW, PRODUCT_BATCH_VIEW, WAREHOUSE_VIEW);

    private static final Set<String> STOCK_MANAGE = union(STOCK_READ, Set.of(
            INVENTORY_MANAGE, INVENTORY_ADJUST, PRODUCT_BATCH_MANAGE, LEGACY_STORE_MANAGER));

    private static final Set<String> PURCHASING_READ = Set.of(
            PURCHASE_ORDER_VIEW, GOODS_RECEIPT_VIEW, PURCHASE_INVOICE_VIEW,
            SUPPLIER_VIEW, SUPPLIER_LEDGER_VIEW, SUPPLIER_PAYMENT_VIEW);

    /**
     * Draft-level purchasing: create and edit, but no approvals/posting
     */
    private static final Set<String> PURCHASING_DRAFT = union(PURCHASING_READ, Set.of(
            PURCHASE_ORDER_CREATE, PURCHASE_ORDER_UPDATE,
            GOODS_RECEIPT_CREATE, GOODS_RECEIPT_UPDATE,
            PURCHASE_INVOICE_CREATE, PURCHASE_INVOICE_UPDATE));

    /**
     * Full purchasing: approvals, posting, deletion, payments
     */
    private static final Set<String> PURCHASING_FULL = union(PURCHASING_DRAFT, Set.of(
            PURCHASE_ORDER_APPROVE, PURCHASE_ORDER_SEND, PURCHASE_ORDER_DELETE,
            GOODS_RECEIPT_APPROVE, GOODS_RECEIPT_DELETE, GOODS_RECEIPT_REPORT,
            PURCHASE_INVOICE_POST, PURCHASE_INVOICE_DELETE,
            SUPPLIER_PAYMENT_CREATE, SUPPLIER_MANAGE));

    private static final Set<String> SALES_CASHIER = Set.of(
            SALE_VIEW, SALE_CREATE, PRODUCT_VIEW, CATEGORY_VIEW, INVENTORY_VIEW, CURRENCY_VIEW);

    private static final Set<String> SALES_FULL = union(SALES_CASHIER, Set.of(SALE_REFUND));

    private static final Set<String> SYSTEM_ADMIN = Set.of(
            BRANCH_VIEW, BRANCH_MANAGE, WAREHOUSE_MANAGE,
            CURRENCY_VIEW, CURRENCY_MANAGE,
            USER_VIEW, USER_MANAGE, HR_VIEW, HR_MANAGE, REPORT_VIEW);

    // ═══════════════════════════════════════════════════════════════════════
    // Role assignments
    // ═══════════════════════════════════════════════════════════════════════

    private static final Map<UserRole, Set<String>> MAP = new EnumMap<>(UserRole.class);

    static {
        // ── Full control over everything ──────────────────────────────────
        MAP.put(UserRole.ROLE_ADMIN, union(
                CATALOG_MANAGE, STOCK_MANAGE, PURCHASING_FULL, SALES_FULL, SYSTEM_ADMIN, Set.of(
                        STORE_VIEW,
                        STORE_UPDATE,
                        STORE_EMPLOYEE_MANAGE,
                        STORE_STATUS_MANAGE,
                        STORE_DELETE)));

        MAP.put(UserRole.ROLE_STORE_ADMIN, union(
                CATALOG_MANAGE, STOCK_MANAGE, PURCHASING_FULL, SALES_FULL, SYSTEM_ADMIN, Set.of(
                        STORE_VIEW,
                        STORE_UPDATE,
                        STORE_EMPLOYEE_MANAGE
                )));

        // ── Store manager: runs operations, approves POs, but goods-receipt
        //    approval stays with warehouse manager / store admin (your spec) ──
        MAP.put(UserRole.ROLE_STORE_MANAGER, union(
                CATALOG_MANAGE, STOCK_MANAGE, PURCHASING_DRAFT, SALES_FULL,
                Set.of(PURCHASE_ORDER_APPROVE, PURCHASE_ORDER_SEND,
                        HR_VIEW, REPORT_VIEW, BRANCH_VIEW, CURRENCY_VIEW)));

        // ── Warehouse manager: owns receiving end-to-end ──────────────────
        MAP.put(UserRole.ROLE_WAREHOUSE_MANAGER, union(
                CATALOG_READ, STOCK_MANAGE, PURCHASING_READ,
                Set.of(GOODS_RECEIPT_CREATE, GOODS_RECEIPT_UPDATE,
                        GOODS_RECEIPT_APPROVE, GOODS_RECEIPT_DELETE, GOODS_RECEIPT_REPORT,
                        REPORT_VIEW)));

        // ── Warehouse staff: drafts receipts, cannot approve or delete ────
        MAP.put(UserRole.ROLE_WAREHOUSE_STAFF, union(
                CATALOG_READ, STOCK_READ,
                Set.of(GOODS_RECEIPT_VIEW, GOODS_RECEIPT_CREATE, GOODS_RECEIPT_UPDATE,
                        PURCHASE_ORDER_VIEW, PRODUCT_BATCH_MANAGE)));

        // ── Branch admin: manages a branch's stock + drafts purchasing ────
        MAP.put(UserRole.ROLE_BRANCH_ADMIN, union(
                CATALOG_MANAGE, STOCK_MANAGE, PURCHASING_DRAFT, SALES_FULL,
                Set.of(USER_VIEW, HR_VIEW, REPORT_VIEW, BRANCH_VIEW, CURRENCY_VIEW)));

        // ── Branch manager: day-to-day, no catalog/stock deletion ─────────
        MAP.put(UserRole.ROLE_BRANCH_MANAGER, union(
                CATALOG_READ, STOCK_READ, SALES_FULL,
                Set.of(PURCHASE_ORDER_VIEW, PURCHASE_ORDER_CREATE,
                        GOODS_RECEIPT_VIEW, PURCHASE_INVOICE_VIEW,
                        INVENTORY_MANAGE, LEGACY_STORE_MANAGER,
                        HR_VIEW, REPORT_VIEW)));

        // ── Cashier: sells, looks things up ───────────────────────────────
        MAP.put(UserRole.ROLE_BRANCH_CASHIER, SALES_CASHIER);

        // ── Customer: no back-office permissions ──────────────────────────
        MAP.put(UserRole.ROLE_CUSTOMER, Set.of());
    }

    // ═══════════════════════════════════════════════════════════════════════
    // API
    // ═══════════════════════════════════════════════════════════════════════

    public static Set<String> permissionsFor(UserRole role) {
        return MAP.getOrDefault(role, Set.of());
    }

    public static boolean hasPermission(UserRole role, String permission) {
        return permissionsFor(role).contains(permission);
    }

    // ═══════════════════════════════════════════════════════════════════════
    // Private
    // ═══════════════════════════════════════════════════════════════════════

    @SafeVarargs
    private static Set<String> union(Set<String>... sets) {
        Set<String> result = new HashSet<>();
        Arrays.stream(sets).forEach(result::addAll);
        return Collections.unmodifiableSet(result);
    }
}