package com.sh.sh.pos.system.service.security;


import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import com.sh.sh.pos.system.domain.UserRole;
import com.sh.sh.pos.system.security.RolePermissions;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class RolePermissionsTest {

    // ── The spec rule that matters most ──────────────────────────────────

    @Test
    @DisplayName("only Admin / Store Admin / Warehouse Manager can approve goods receipts")
    void onlyManagersCanApproveGoodsReceipts() {
        assertThat(RolePermissions.hasPermission(UserRole.ROLE_ADMIN, RolePermissions.GOODS_RECEIPT_APPROVE)).isTrue();
        assertThat(RolePermissions.hasPermission(UserRole.ROLE_STORE_ADMIN, RolePermissions.GOODS_RECEIPT_APPROVE)).isTrue();
        assertThat(RolePermissions.hasPermission(UserRole.ROLE_WAREHOUSE_MANAGER, RolePermissions.GOODS_RECEIPT_APPROVE)).isTrue();

        assertThat(RolePermissions.hasPermission(UserRole.ROLE_WAREHOUSE_STAFF, RolePermissions.GOODS_RECEIPT_APPROVE)).isFalse();
        assertThat(RolePermissions.hasPermission(UserRole.ROLE_STORE_MANAGER, RolePermissions.GOODS_RECEIPT_APPROVE)).isFalse();
        assertThat(RolePermissions.hasPermission(UserRole.ROLE_BRANCH_ADMIN, RolePermissions.GOODS_RECEIPT_APPROVE)).isFalse();
        assertThat(RolePermissions.hasPermission(UserRole.ROLE_BRANCH_MANAGER, RolePermissions.GOODS_RECEIPT_APPROVE)).isFalse();
        assertThat(RolePermissions.hasPermission(UserRole.ROLE_BRANCH_CASHIER, RolePermissions.GOODS_RECEIPT_APPROVE)).isFalse();
        assertThat(RolePermissions.hasPermission(UserRole.ROLE_CUSTOMER, RolePermissions.GOODS_RECEIPT_APPROVE)).isFalse();
    }

    @Test
    @DisplayName("warehouse staff can draft receipts but not delete them")
    void staffCanDraftNotDelete() {
        var staff = RolePermissions.permissionsFor(UserRole.ROLE_WAREHOUSE_STAFF);
        assertThat(staff).contains(
                RolePermissions.GOODS_RECEIPT_VIEW,
                RolePermissions.GOODS_RECEIPT_CREATE,
                RolePermissions.GOODS_RECEIPT_UPDATE);
        assertThat(staff).doesNotContain(
                RolePermissions.GOODS_RECEIPT_DELETE,
                RolePermissions.GOODS_RECEIPT_APPROVE);
    }

    @Test
    @DisplayName("store manager approves POs but not goods receipts")
    void storeManagerPoApprovalOnly() {
        var sm = RolePermissions.permissionsFor(UserRole.ROLE_STORE_MANAGER);
        assertThat(sm).contains(RolePermissions.PURCHASE_ORDER_APPROVE, RolePermissions.PURCHASE_ORDER_SEND);
        assertThat(sm).doesNotContain(RolePermissions.GOODS_RECEIPT_APPROVE);
    }

    @Test
    @DisplayName("cashier is sales-only — no purchasing, no user management")
    void cashierScope() {
        var cashier = RolePermissions.permissionsFor(UserRole.ROLE_BRANCH_CASHIER);
        assertThat(cashier).contains(RolePermissions.SALE_CREATE, RolePermissions.PRODUCT_VIEW);
        assertThat(cashier).doesNotContain(
                RolePermissions.SALE_REFUND,               // refunds need manager
                RolePermissions.GOODS_RECEIPT_VIEW,
                RolePermissions.USER_MANAGE,
                RolePermissions.INVENTORY_ADJUST);
    }

    @Test
    @DisplayName("customer gets nothing")
    void customerEmpty() {
        assertThat(RolePermissions.permissionsFor(UserRole.ROLE_CUSTOMER)).isEmpty();
    }

    @Test
    @DisplayName("legacy STORE_MANAGER authority still granted where inventory endpoints need it")
    void legacyAuthorityPresent() {
        assertThat(RolePermissions.permissionsFor(UserRole.ROLE_ADMIN))
                .contains(RolePermissions.LEGACY_STORE_MANAGER);
        assertThat(RolePermissions.permissionsFor(UserRole.ROLE_BRANCH_MANAGER))
                .contains(RolePermissions.LEGACY_STORE_MANAGER);
    }

    // ── Structural safety nets ────────────────────────────────────────────

    @ParameterizedTest
    @EnumSource(UserRole.class)
    @DisplayName("every role resolves to a non-null set (no missing map entries)")
    void everyRoleResolves(UserRole role) {
        assertThat(RolePermissions.permissionsFor(role)).isNotNull();
    }

    @Test
    @DisplayName("permission sets are immutable")
    void setsAreImmutable() {
        var perms = RolePermissions.permissionsFor(UserRole.ROLE_ADMIN);
        assertThrows(UnsupportedOperationException.class, () -> perms.add("HACK"));
    }

    @Test
    @DisplayName("admin has strictly more permissions than staff (sanity ordering)")
    void adminSupersetOfStaff() {
        assertThat(RolePermissions.permissionsFor(UserRole.ROLE_ADMIN))
                .containsAll(RolePermissions.permissionsFor(UserRole.ROLE_WAREHOUSE_STAFF));
    }
}
