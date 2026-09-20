package com.bookflow.orders;

import static org.hamcrest.Matchers.comparesEqualTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.bookflow.books.Book;
import com.bookflow.books.BookRepository;
import com.bookflow.customers.Customer;
import com.bookflow.customers.CustomerRepository;

@SpringBootTest
@AutoConfigureMockMvc
class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private OrderRepository orderRepository;

    @BeforeEach
    void setUp() {
        orderRepository.deleteAll();
        bookRepository.deleteAll();
        customerRepository.deleteAll();
    }

    @Test
    void checkoutCreatesOrderAndDecrementsStockInOneTransaction() throws Exception {
        Customer customer = customerRepository.save(new Customer("Ada Lovelace", "ada@example.com", "604-555-0101"));
        Book book = bookRepository.save(new Book("Clean Code", "Robert C. Martin", "9780132350884",
                BigDecimal.valueOf(42.50), 8));

        mockMvc.perform(post("/api/orders/checkout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"customerId\":" + customer.getId()
                                + ",\"items\":[{\"bookId\":" + book.getId() + ",\"quantity\":3}]}"))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", notNullValue()))
                .andExpect(jsonPath("$.customerId", is(customer.getId().intValue())))
                .andExpect(jsonPath("$.totalAmount", comparesEqualTo(127.50)))
                .andExpect(jsonPath("$.items[0].bookId", is(book.getId().intValue())))
                .andExpect(jsonPath("$.items[0].quantity", is(3)));

        Book updatedBook = bookRepository.findById(book.getId()).get();
        assertEquals(5, updatedBook.getStockQuantity());
    }

    @Test
    void checkoutWithInsufficientStockReturnsConflictAndDoesNotDecrementStock() throws Exception {
        Customer customer = customerRepository.save(new Customer("Ada Lovelace", "ada@example.com", "604-555-0101"));
        Book book = bookRepository.save(new Book("Clean Code", "Robert C. Martin", "9780132350884",
                BigDecimal.valueOf(42.50), 2));

        mockMvc.perform(post("/api/orders/checkout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"customerId\":" + customer.getId()
                                + ",\"items\":[{\"bookId\":" + book.getId() + ",\"quantity\":3}]}"))
                .andExpect(status().isConflict());

        Book updatedBook = bookRepository.findById(book.getId()).get();
        assertEquals(2, updatedBook.getStockQuantity());
        assertEquals(0, orderRepository.count());
    }

    @Test
    void getMissingOrderReturnsNotFound() throws Exception {
        mockMvc.perform(get("/api/orders/999"))
                .andExpect(status().isNotFound());
    }
}
