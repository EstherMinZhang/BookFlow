package com.bookflow;

import java.math.BigDecimal;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.LockModeType;
import javax.persistence.PersistenceContext;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class StoreService {

    @PersistenceContext
    private EntityManager database;

    @Transactional(readOnly = true)
    public List<Book> books() {
        return database.createQuery("from Book", Book.class).getResultList();
    }

    @Transactional(readOnly = true)
    public Book book(Long id) { return find(Book.class, id, "Book"); }

    @Transactional
    public Book saveBook(Book book) {
        if (database.createQuery("select count(b) from Book b where b.isbn = :isbn", Long.class)
                .setParameter("isbn", book.getIsbn()).getSingleResult() > 0) {
            throw conflict("A book with ISBN " + book.getIsbn() + " already exists.");
        }
        database.persist(book);
        return book;
    }

    @Transactional
    public Book updateBook(Long id, Book input) {
        Book book = book(id);
        database.createQuery("from Book b where b.isbn = :isbn and b.id <> :id", Book.class)
                .setParameter("isbn", input.getIsbn()).setParameter("id", id).getResultStream()
                .findFirst().ifPresent(found -> { throw conflict("A book with ISBN " + input.getIsbn() + " already exists."); });
        book.setTitle(input.getTitle());
        book.setAuthor(input.getAuthor());
        book.setIsbn(input.getIsbn());
        book.setPrice(input.getPrice());
        book.setStockQuantity(input.getStockQuantity());
        return book;
    }

    @Transactional
    public void deleteBook(Long id) { database.remove(book(id)); }

    @Transactional(readOnly = true)
    public List<Customer> customers() {
        return database.createQuery("from Customer", Customer.class).getResultList();
    }

    @Transactional(readOnly = true)
    public Customer customer(Long id) { return find(Customer.class, id, "Customer"); }

    @Transactional
    public Customer saveCustomer(Customer customer) {
        if (database.createQuery("select count(c) from Customer c where c.email = :email", Long.class)
                .setParameter("email", customer.getEmail()).getSingleResult() > 0) {
            throw conflict("A customer with email " + customer.getEmail() + " already exists.");
        }
        database.persist(customer);
        return customer;
    }

    @Transactional
    public Customer updateCustomer(Long id, Customer input) {
        Customer customer = customer(id);
        database.createQuery("from Customer c where c.email = :email and c.id <> :id", Customer.class)
                .setParameter("email", input.getEmail()).setParameter("id", id).getResultStream()
                .findFirst().ifPresent(found -> { throw conflict("A customer with email " + input.getEmail() + " already exists."); });
        customer.setName(input.getName());
        customer.setEmail(input.getEmail());
        customer.setPhone(input.getPhone());
        return customer;
    }

    @Transactional
    public void deleteCustomer(Long id) { database.remove(customer(id)); }

    @Transactional(readOnly = true)
    public List<CustomerOrder> orders() {
        return database.createQuery("select distinct o from CustomerOrder o "
                + "join fetch o.customer left join fetch o.items i left join fetch i.book "
                + "order by o.createdAt", CustomerOrder.class).getResultList();
    }

    @Transactional(readOnly = true)
    public CustomerOrder order(Long id) {
        return database.createQuery("select distinct o from CustomerOrder o "
                + "join fetch o.customer left join fetch o.items i left join fetch i.book "
                + "where o.id = :id", CustomerOrder.class)
                .setParameter("id", id).getResultStream().findFirst()
                .orElseThrow(() -> notFound("Order " + id + " was not found."));
    }

    @Transactional
    public CustomerOrder checkout(CheckoutRequest request) {
        CustomerOrder order = new CustomerOrder(customer(request.getCustomerId()), BigDecimal.ZERO);
        BigDecimal total = BigDecimal.ZERO;
        for (CheckoutItemRequest item : request.getItems()) {
            Book book = database.find(Book.class, item.bookId, LockModeType.PESSIMISTIC_WRITE);
            if (book == null) throw notFound("Book " + item.bookId + " was not found.");
            if (book.getStockQuantity() < item.quantity) {
                throw conflict("Book " + book.getId() + " has only " + book.getStockQuantity() + " copies in stock.");
            }
            book.setStockQuantity(book.getStockQuantity() - item.quantity);
            BigDecimal lineTotal = book.getPrice().multiply(BigDecimal.valueOf(item.quantity));
            total = total.add(lineTotal);
            order.addItem(new OrderItem(book, item.quantity, book.getPrice(), lineTotal));
        }
        order.setTotalAmount(total);
        database.persist(order);
        return order;
    }

    private <T> T find(Class<T> type, Long id, String name) {
        T value = database.find(type, id);
        if (value == null) throw notFound(name + " " + id + " was not found.");
        return value;
    }

    private ApiException notFound(String message) { return new ApiException(HttpStatus.NOT_FOUND, message); }
    private ApiException conflict(String message) { return new ApiException(HttpStatus.CONFLICT, message); }
}
