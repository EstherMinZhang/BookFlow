package com.bookflow.orders;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.bookflow.books.Book;
import com.bookflow.books.BookRepository;
import com.bookflow.common.InsufficientStockException;
import com.bookflow.common.ResourceNotFoundException;
import com.bookflow.customers.Customer;
import com.bookflow.customers.CustomerService;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final BookRepository bookRepository;
    private final CustomerService customerService;

    public OrderService(OrderRepository orderRepository, BookRepository bookRepository, CustomerService customerService) {
        this.orderRepository = orderRepository;
        this.bookRepository = bookRepository;
        this.customerService = customerService;
    }

    @Transactional(readOnly = true)
    public List<OrderResponse> getOrders() {
        return orderRepository.findAll().stream()
                .map(OrderResponse::new)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public OrderResponse getOrder(Long id) {
        return new OrderResponse(findOrder(id));
    }

    @Transactional
    public OrderResponse checkout(CheckoutRequest request) {
        Customer customer = customerService.findCustomer(request.getCustomerId());
        List<OrderItem> orderItems = new ArrayList<>();
        BigDecimal total = BigDecimal.ZERO;

        for (CheckoutItemRequest itemRequest : request.getItems()) {
            Book book = bookRepository.findLockedById(itemRequest.getBookId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Book " + itemRequest.getBookId() + " was not found."));

            validateStock(book, itemRequest.getQuantity());

            book.setStockQuantity(book.getStockQuantity() - itemRequest.getQuantity());
            BigDecimal lineTotal = book.getPrice().multiply(BigDecimal.valueOf(itemRequest.getQuantity()));
            total = total.add(lineTotal);
            orderItems.add(new OrderItem(book, itemRequest.getQuantity(), book.getPrice(), lineTotal));
        }

        CustomerOrder order = new CustomerOrder(customer, total);
        for (OrderItem item : orderItems) {
            order.addItem(item);
        }

        return new OrderResponse(orderRepository.save(order));
    }

    private void validateStock(Book book, Integer requestedQuantity) {
        if (book.getStockQuantity() < requestedQuantity) {
            throw new InsufficientStockException(
                    "Book " + book.getId() + " has only " + book.getStockQuantity() + " copies in stock.");
        }
    }

    private CustomerOrder findOrder(Long id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order " + id + " was not found."));
    }
}
