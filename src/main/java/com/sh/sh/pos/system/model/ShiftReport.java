package com.sh.sh.pos.system.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import com.sh.sh.pos.system.model.products.Product;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Transient;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Builder

public class ShiftReport {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	private LocalDateTime shiftStart;
	private LocalDateTime shiftEnd;

	private BigDecimal totalSales;
	private BigDecimal totalRefunds;

	private BigDecimal netSale;

	private int totalOrder;

	private BigDecimal openingCash;

	private BigDecimal closingCash;

	private BigDecimal expectedCash;

	private BigDecimal cashDifference;

	@ManyToOne
	private User cashier;

	@ManyToOne
	private Branch branch;

	@Transient
	private List<PaymentSummary> paymentSummaries;

	@OneToMany(cascade = CascadeType.ALL)
	private List<Product> topSellingProducts;

	@OneToMany(cascade = CascadeType.ALL)
	private List<Order> recentOrders;

	@OneToMany(mappedBy = "shiftReport", cascade = CascadeType.ALL)
	private List<Refund> refunds;

}
