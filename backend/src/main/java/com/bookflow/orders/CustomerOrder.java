package com.bookflow.orders;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.PrePersist;
import javax.persistence.Table;

import com.bookflow.customers.Customer;

@Entity
@Table(name = "customer_orders")
public class CustomerOrder {

    // 订单主键，表名不用 orders，避免和部分数据库关键字冲突。
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 下单顾客，多个订单可以属于同一个顾客。
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    // 订单项跟随订单一起保存。
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItem> items = new ArrayList<>();

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal totalAmount;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    protected CustomerOrder() {
    }

    public CustomerOrder(Customer customer, BigDecimal totalAmount) {
        this.customer = customer;
        this.totalAmount = totalAmount;
    }

    @PrePersist
    void onCreate() {
        createdAt = Instant.now();
    }

    public void addItem(OrderItem item) {
        items.add(item);
        item.setOrder(this);
    }

    public Long getId() {
        return id;
    }

    public Customer getCustomer() {
        return customer;
    }

    public List<OrderItem> getItems() {
        return items;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
