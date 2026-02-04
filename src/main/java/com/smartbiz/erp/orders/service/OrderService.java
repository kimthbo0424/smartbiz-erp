package com.smartbiz.erp.orders.service;

import com.smartbiz.erp.entity.Category;
import com.smartbiz.erp.entity.Client;
import com.smartbiz.erp.entity.enums.ClientType;
import com.smartbiz.erp.repository.ClientRepository;
import com.smartbiz.erp.repository.CategoryRepository;
import com.smartbiz.erp.orders.domain.Order;
import com.smartbiz.erp.orders.domain.OrderItem;
import com.smartbiz.erp.orders.domain.OrderStatus;
import com.smartbiz.erp.orders.dto.OrderCreateForm;
import com.smartbiz.erp.orders.dto.OrderDetailView;
import com.smartbiz.erp.orders.dto.OrderEditForm;
import com.smartbiz.erp.orders.dto.OrderItemForm;
import com.smartbiz.erp.orders.dto.OrderItemLineView;
import com.smartbiz.erp.orders.dto.OrderSearchQuery;
import com.smartbiz.erp.orders.dto.OrderSummaryView;
import com.smartbiz.erp.orders.repository.OrderRepository;
import com.smartbiz.erp.entity.Product;
import com.smartbiz.erp.entity.enums.ProductStatus;
import com.smartbiz.erp.repository.ProductRepository;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final ClientRepository clientRepository;
    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;

    private static final DateTimeFormatter DAY_FMT = DateTimeFormatter.ofPattern("yyyyMMdd");
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public OrderService(OrderRepository orderRepository,
                        ClientRepository clientRepository,
                        ProductRepository productRepository,
                        CategoryRepository categoryRepository) {
        this.orderRepository = orderRepository;
        this.clientRepository = clientRepository;
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
    }

    public Page<OrderSummaryView> search(OrderSearchQuery q, int page, int size) {
        String clientName = emptyToNull(q.getPartnerName());
        String managerName = emptyToNull(q.getManagerName());

        OrderStatus status = null;
        if (q.getStatus() != null && !q.getStatus().isBlank()) {
            status = OrderStatus.valueOf(q.getStatus());
        }

        Pageable pageable = PageRequest.of(Math.max(page - 1, 0), size, Sort.by(Sort.Direction.DESC, "id"));
        Page<Order> result = orderRepository.search(clientName, managerName, status, pageable);

        return result.map(o -> new OrderSummaryView(
                o.getId(),
                o.getOrderNo(),
                o.getClient().getName(),
                o.getManagerName(),
                o.getManagerPhone(),
                o.getOrderDate().toLocalDate().format(DATE_FMT),
                o.getStatus().name(),
                bdToLong(o.getSubtotalAmount()),
                bdToLong(o.getTaxAmount()),
                bdToLong(o.getDiscountAmount()),
                bdToLong(o.getTotalAmount()),
                0
        ));
    }

    @Transactional
    public Long create(OrderCreateForm form) {
        Client client = resolveClient(form.getPartnerName());
        String orderNo = generateOrderNo(LocalDate.now());

        Order order = new Order();
        order.setOrderNo(orderNo);
        order.setClient(client);
        order.setManagerName(form.getManagerName());
        order.setManagerPhone(form.getManagerPhone());
        order.setStatus(OrderStatus.PENDING);
        order.setOrderDate(parseOrderDateOrNow(form.getOrderDate()));

        // items
        order.clearItems();
        for (OrderItemForm f : safeItems(form.getItems())) {
            if (f.getProductName() == null || f.getProductName().isBlank()) continue;

            Product product = resolveProduct(f.getProductName(), f.getUnitPrice());

            OrderItem item = new OrderItem();
            item.setProduct(product);
            item.setQuantity(Math.max(f.getQty(), 1));
            item.setUnitPrice(BigDecimal.valueOf(Math.max(f.getUnitPrice(), 0)));

            BigDecimal lineSubtotal = item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity()));
            BigDecimal lineTax = lineSubtotal.multiply(new BigDecimal("0.10")).setScale(2, RoundingMode.HALF_UP);

            item.setSubtotal(lineSubtotal);
            item.setTax(lineTax);

            order.addItem(item);
        }

        // totals
        BigDecimal subtotal = order.getItems().stream().map(OrderItem::getSubtotal).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal tax = order.getItems().stream().map(OrderItem::getTax).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal discount = BigDecimal.valueOf(Math.max(form.getDiscount(), 0));

        order.setSubtotalAmount(subtotal);
        order.setTaxAmount(tax);
        order.setDiscountAmount(discount);
        order.setTotalAmount(subtotal.add(tax).subtract(discount));

        Order saved = orderRepository.save(order);
        return saved.getId();
    }

    public OrderDetailView getDetail(Long id) {
        Order o = orderRepository.findWithAllById(id)
                .orElseThrow(() -> new IllegalArgumentException("주문이 존재하지 않습니다. id=" + id));

        OrderDetailView v = new OrderDetailView();
        v.setId(o.getId());
        v.setOrderNo(o.getOrderNo());
        v.setOrderDate(o.getOrderDate().toLocalDate().format(DATE_FMT));
        v.setStatus(o.getStatus().name());
        v.setPartnerName(o.getClient().getName());
        v.setManagerName(o.getManagerName());
        v.setManagerPhone(o.getManagerPhone());
        v.setSubtotal(bdToLong(o.getSubtotalAmount()));
        v.setTax(bdToLong(o.getTaxAmount()));
        v.setDiscount(bdToLong(o.getDiscountAmount()));
        v.setTotal(bdToLong(o.getTotalAmount()));

        List<OrderItemLineView> lines = new ArrayList<>();
        for (OrderItem it : o.getItems()) {
            long amount = bdToLong(it.getSubtotal().add(it.getTax()));
            lines.add(new OrderItemLineView(
                    it.getProduct().getName(),
                    it.getQuantity(),
                    bdToLong(it.getUnitPrice()),
                    amount
            ));
        }
        v.setItems(lines);

        return v;
    }

    public OrderEditForm getEditForm(Long id) {
        Order o = orderRepository.findWithAllById(id)
                .orElseThrow(() -> new IllegalArgumentException("주문이 존재하지 않습니다. id=" + id));

        OrderEditForm f = new OrderEditForm();
        f.setId(o.getId());
        f.setOrderNo(o.getOrderNo());
        f.setPartnerName(o.getClient().getName());
        f.setManagerName(o.getManagerName());
        f.setManagerPhone(o.getManagerPhone());
        f.setOrderDate(o.getOrderDate().toLocalDate().format(DATE_FMT));
        f.setDiscount(bdToLong(o.getDiscountAmount()));
        f.setSubtotal(bdToLong(o.getSubtotalAmount()));
        f.setTax(bdToLong(o.getTaxAmount()));
        f.setTotal(bdToLong(o.getTotalAmount()));

        List<OrderItemForm> items = new ArrayList<>();
        for (OrderItem it : o.getItems()) {
            OrderItemForm line = new OrderItemForm();
            line.setProductName(it.getProduct().getName());
            line.setQty(it.getQuantity());
            line.setUnitPrice(bdToLong(it.getUnitPrice()));
            items.add(line);
        }
        if (items.isEmpty()) items.add(new OrderItemForm());
        f.setItems(items);

        return f;
    }

    @Transactional
    public void update(Long id, OrderEditForm form) {
        Order o = orderRepository.findWithAllById(id)
                .orElseThrow(() -> new IllegalArgumentException("주문이 존재하지 않습니다. id=" + id));

        // 거래처 변경도 허용(이름 기준)
        o.setClient(resolveClient(form.getPartnerName()));
        o.setManagerName(form.getManagerName());
        o.setManagerPhone(form.getManagerPhone());
        o.setOrderDate(parseOrderDateOrNow(form.getOrderDate()));

        // items: 간단히 "전부 삭제 후 재생성"
        o.getItems().clear();

        for (OrderItemForm f : safeItems(form.getItems())) {
            if (f.getProductName() == null || f.getProductName().isBlank()) continue;

            Product product = resolveProduct(f.getProductName(), f.getUnitPrice());

            OrderItem item = new OrderItem();
            item.setProduct(product);
            item.setQuantity(Math.max(f.getQty(), 1));
            item.setUnitPrice(BigDecimal.valueOf(Math.max(f.getUnitPrice(), 0)));

            BigDecimal lineSubtotal = item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity()));
            BigDecimal lineTax = lineSubtotal.multiply(new BigDecimal("0.10")).setScale(2, RoundingMode.HALF_UP);

            item.setSubtotal(lineSubtotal);
            item.setTax(lineTax);

            o.addItem(item);
        }

        BigDecimal subtotal = o.getItems().stream().map(OrderItem::getSubtotal).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal tax = o.getItems().stream().map(OrderItem::getTax).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal discount = BigDecimal.valueOf(Math.max(form.getDiscount(), 0));

        o.setSubtotalAmount(subtotal);
        o.setTaxAmount(tax);
        o.setDiscountAmount(discount);
        o.setTotalAmount(subtotal.add(tax).subtract(discount));

        orderRepository.save(o);
    }

    // ---- 상태 변경 ----

    @Transactional
    public void changeStatus(Long id, OrderStatus newStatus) {
        Order o = orderRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("주문이 존재하지 않습니다. id=" + id));

        validateTransition(o.getStatus(), newStatus);

        o.setStatus(newStatus);
        orderRepository.save(o);
    }

    private void validateTransition(OrderStatus from, OrderStatus to) {
        if (from == to) return;

        switch (from) {
            case PENDING -> {
                if (to != OrderStatus.SHIPPED && to != OrderStatus.CANCELLED) {
                    throw new IllegalStateException("PENDING 상태에서는 SHIPPED/CANCELLED로만 변경 가능합니다.");
                }
            }
            case SHIPPED -> {
                if (to != OrderStatus.RETURNED && to != OrderStatus.SETTLED) {
                    throw new IllegalStateException("SHIPPED 상태에서는 RETURNED/SETTLED로만 변경 가능합니다.");
                }
            }
            case SETTLED, CANCELLED, RETURNED -> {
                throw new IllegalStateException(from + " 상태에서는 변경할 수 없습니다.");
            }
        }
    }

    // ----------------- helpers -----------------

    private Client resolveClient(String partnerName) {
        String name = Objects.requireNonNullElse(partnerName, "").trim();
        if (name.isBlank()) throw new IllegalArgumentException("거래처명은 필수입니다.");

        return clientRepository.findByName(name).orElseGet(() -> {
            Client c = new Client();
            c.setName(name);
            c.setType(ClientType.CUSTOMER);
            c.setActive(true);
            return clientRepository.save(c);
        });
    }

    private Product resolveProduct(String productName, long unitPrice) {
        String name = Objects.requireNonNullElse(productName, "").trim();
        if (name.isBlank()) throw new IllegalArgumentException("상품명은 필수입니다.");

        return productRepository.findByName(name).orElseGet(() -> {
        	
        	// 기본 카테고리 조회 (ID = 1)
            Category defaultCategory = categoryRepository.findById(1L)
                    .orElseThrow(() -> new IllegalStateException("기본 카테고리가 존재하지 않습니다."));
        	
            Product p = new Product();
            p.setName(name);
            p.setCategory(defaultCategory); // "기본" 카테고리 id를 1로 유지하려면 초기 데이터로 맞춰두는 게 좋음
            p.setSku("SKU-" + UUID.randomUUID().toString().replace("-", "").substring(0, 10));
            p.setUnitPrice(BigDecimal.valueOf(Math.max(unitPrice, 0)));
            p.setCostPrice(BigDecimal.ZERO);
            p.setStatus(ProductStatus.ACTIVE);
            return productRepository.save(p);
        });
    }

    private String generateOrderNo(LocalDate day) {
        String prefix = "ORD-" + day.format(DAY_FMT) + "-";
        Optional<Order> last = orderRepository.findTopByOrderNoStartingWithOrderByOrderNoDesc(prefix);

        int next = 1;
        if (last.isPresent()) {
            String no = last.get().getOrderNo(); // ORD-yyyymmdd-001
            String tail = no.substring(no.length() - 3);
            next = Integer.parseInt(tail) + 1;
        }
        return prefix + String.format("%03d", next);
    }

    private LocalDateTime parseOrderDateOrNow(String yyyyMmDd) {
        if (yyyyMmDd == null || yyyyMmDd.isBlank()) return LocalDateTime.now();
        LocalDate d = LocalDate.parse(yyyyMmDd, DATE_FMT);
        return d.atStartOfDay();
    }

    private static String emptyToNull(String s) {
        if (s == null) return null;
        String t = s.trim();
        return t.isEmpty() ? null : t;
    }

    private static List<OrderItemForm> safeItems(List<OrderItemForm> items) {
        return items == null ? List.of() : items;
    }

    private static long bdToLong(BigDecimal bd) {
        if (bd == null) return 0;
        return bd.setScale(0, RoundingMode.HALF_UP).longValue();
    }
}
