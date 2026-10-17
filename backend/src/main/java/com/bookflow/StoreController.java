package com.bookflow;

import java.net.URI;
import java.util.List;

import javax.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@RestController
@RequestMapping("/api")
public class StoreController {

    private final StoreService store;

    public StoreController(StoreService store) { this.store = store; }

    @GetMapping("/books")
    public List<Book> books() { return store.books(); }

    @GetMapping("/books/{id}")
    public Book book(@PathVariable Long id) { return store.book(id); }

    @PostMapping("/books")
    public ResponseEntity<Book> createBook(@Valid @RequestBody Book book) {
        Book saved = store.saveBook(book);
        return created(saved.getId()).body(saved);
    }

    @PutMapping("/books/{id}")
    public Book updateBook(@PathVariable Long id, @Valid @RequestBody Book book) {
        return store.updateBook(id, book);
    }

    @DeleteMapping("/books/{id}")
    public ResponseEntity<Void> deleteBook(@PathVariable Long id) {
        store.deleteBook(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/customers")
    public List<Customer> customers() { return store.customers(); }

    @GetMapping("/customers/{id}")
    public Customer customer(@PathVariable Long id) { return store.customer(id); }

    @PostMapping("/customers")
    public ResponseEntity<Customer> createCustomer(@Valid @RequestBody Customer customer) {
        Customer saved = store.saveCustomer(customer);
        return created(saved.getId()).body(saved);
    }

    @PutMapping("/customers/{id}")
    public Customer updateCustomer(@PathVariable Long id, @Valid @RequestBody Customer customer) {
        return store.updateCustomer(id, customer);
    }

    @DeleteMapping("/customers/{id}")
    public ResponseEntity<Void> deleteCustomer(@PathVariable Long id) {
        store.deleteCustomer(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/orders")
    public List<CustomerOrder> orders() { return store.orders(); }

    @GetMapping("/orders/{id}")
    public CustomerOrder order(@PathVariable Long id) { return store.order(id); }

    @PostMapping("/orders/checkout")
    public ResponseEntity<CustomerOrder> checkout(@Valid @RequestBody CheckoutRequest request) {
        CustomerOrder order = store.checkout(request);
        return created(order.getId()).body(order);
    }

    private <T> ResponseEntity.BodyBuilder created(Long id) {
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}").buildAndExpand(id).toUri();
        return ResponseEntity.created(location);
    }
}
