package com.bookflow.orders;

import org.springframework.data.jpa.repository.JpaRepository;

// 订单数据访问层。
public interface OrderRepository extends JpaRepository<CustomerOrder, Long> {
}
