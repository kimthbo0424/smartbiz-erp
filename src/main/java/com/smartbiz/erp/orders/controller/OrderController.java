package com.smartbiz.erp.orders.controller;

import com.smartbiz.erp.common.PageView;
import com.smartbiz.erp.orders.domain.OrderStatus;
import com.smartbiz.erp.orders.dto.OrderCreateForm;
import com.smartbiz.erp.orders.dto.OrderDetailView;
import com.smartbiz.erp.orders.dto.OrderEditForm;
import com.smartbiz.erp.orders.dto.OrderItemForm;
import com.smartbiz.erp.orders.dto.OrderSearchQuery;
import com.smartbiz.erp.orders.dto.OrderSummaryView;
import com.smartbiz.erp.orders.service.OrderService;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @GetMapping
    public String list(@ModelAttribute("q") OrderSearchQuery q,
                       @RequestParam(name = "page", defaultValue = "1") int page,
                       Model model) {

        Page<OrderSummaryView> result = orderService.search(q, page, 10);

        model.addAttribute("pageTitle", "SmartBiz ERP - 주문 목록");
        model.addAttribute("orders", result.getContent());
        model.addAttribute("page", new PageView(page, result.getTotalPages()));

        return "orders/order_list";
    }

    @GetMapping("/new")
    public String newForm(Model model) {
        OrderCreateForm form = new OrderCreateForm();
        form.getItems().add(new OrderItemForm());

        model.addAttribute("pageTitle", "SmartBiz ERP - 주문 등록");
        model.addAttribute("form", form);
        return "orders/order_form";
    }

    @PostMapping
    public String create(@ModelAttribute("form") OrderCreateForm form) {
        Long id = orderService.create(form);
        return "redirect:/orders/" + id;
    }

    @GetMapping("/{id}")
    public String detail(@PathVariable("id") Long id, Model model) {
        OrderDetailView order = orderService.getDetail(id);

        model.addAttribute("pageTitle", "SmartBiz ERP - 주문 상세");
        model.addAttribute("order", order);
        return "orders/order_detail";
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable("id") Long id, Model model) {
        OrderEditForm form = orderService.getEditForm(id);

        model.addAttribute("pageTitle", "SmartBiz ERP - 주문 수정");
        model.addAttribute("form", form);
        return "orders/order_edit";
    }

    @PostMapping("/{id}/edit")
    public String update(@PathVariable("id") Long id,
                         @ModelAttribute("form") OrderEditForm form) {
        orderService.update(id, form);
        return "redirect:/orders/" + id;
    }

    // ---- 상태 변경 (템플릿의 POST action과 매핑) ----

    @PostMapping("/{id}/ship")
    public String ship(@PathVariable("id") Long id) {
        orderService.changeStatus(id, OrderStatus.SHIPPED);
        return "redirect:/orders/" + id;
    }

    @PostMapping("/{id}/cancel")
    public String cancel(@PathVariable("id") Long id) {
        orderService.changeStatus(id, OrderStatus.CANCELLED);
        return "redirect:/orders/" + id;
    }

    @PostMapping("/{id}/return")
    public String doReturn(@PathVariable("id") Long id) {
        orderService.changeStatus(id, OrderStatus.RETURNED);
        return "redirect:/orders/" + id;
    }
}
