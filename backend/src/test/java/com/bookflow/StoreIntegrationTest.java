package com.bookflow;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class StoreIntegrationTest {
    @Autowired private MockMvc mvc;
    @Autowired private ObjectMapper json;
    @Autowired private JdbcTemplate database;

    @BeforeEach
    void clearDatabase() {
        database.update("delete from order_items");
        database.update("delete from customer_orders");
        database.update("delete from books");
        database.update("delete from customers");
    }

    @Test
    void booksAndCustomersSupportCrud() throws Exception {
        long book = createBook("crud-book", 5);
        mvc.perform(get("/api/books/{id}", book)).andExpect(status().isOk())
                .andExpect(jsonPath("$.isbn").value("crud-book"));
        mvc.perform(put("/api/books/{id}", book).contentType(MediaType.APPLICATION_JSON)
                .content(bookBody("crud-book", 7))).andExpect(status().isOk())
                .andExpect(jsonPath("$.stockQuantity").value(7));
        mvc.perform(delete("/api/books/{id}", book)).andExpect(status().isNoContent());
        mvc.perform(get("/api/books/{id}", book)).andExpect(status().isNotFound());

        long customer = createCustomer();
        mvc.perform(put("/api/customers/{id}", customer).contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"Updated\",\"email\":\"updated@example.com\",\"phone\":\"123\"}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.name").value("Updated"));
        mvc.perform(get("/api/customers/{id}", customer)).andExpect(status().isOk());
        mvc.perform(delete("/api/customers/{id}", customer)).andExpect(status().isNoContent());
        mvc.perform(get("/api/customers/{id}", customer)).andExpect(status().isNotFound());
    }

    @Test
    void checkoutPersistsOrderAndDecrementsStock() throws Exception {
        long book = createBook("checkout-book", 5);
        long customer = createCustomer();
        JsonNode order = create("/api/orders/checkout", checkoutBody(customer, book, 2));
        assertThat(order.get("totalAmount").decimalValue()).isEqualByComparingTo("25.00");
        mvc.perform(get("/api/books/{id}", book)).andExpect(status().isOk())
                .andExpect(jsonPath("$.stockQuantity").value(3));
        mvc.perform(get("/api/orders/{id}", order.get("id").asLong())).andExpect(status().isOk())
                .andExpect(jsonPath("$.items[0].quantity").value(2))
                .andExpect(jsonPath("$.customer.id").value(customer));
        mvc.perform(get("/api/orders")).andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void orderWithMultipleItemsAppearsOnceInList() throws Exception {
        long first = createBook("multi-first", 5);
        long second = createBook("multi-second", 5);
        long customer = createCustomer();
        String body = "{\"customerId\":" + customer + ",\"items\":["
                + "{\"bookId\":" + first + ",\"quantity\":1},"
                + "{\"bookId\":" + second + ",\"quantity\":2}]}";
        JsonNode order = create("/api/orders/checkout", body);
        mvc.perform(get("/api/orders")).andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].items.length()").value(2))
                .andExpect(jsonPath("$[0].totalAmount").value(37.50));
        mvc.perform(get("/api/orders/{id}", order.get("id").asLong()))
                .andExpect(status().isOk()).andExpect(jsonPath("$.items.length()").value(2));
    }

    @Test
    void missingOrderReturnsNotFound() throws Exception {
        mvc.perform(get("/api/orders/{id}", Long.MAX_VALUE))
                .andExpect(status().isNotFound()).andExpect(jsonPath("$.message").isNotEmpty());
    }

    @Test
    void failedCheckoutRollsBackEarlierInventoryChanges() throws Exception {
        long first = createBook("first-book", 5);
        long second = createBook("second-book", 1);
        long customer = createCustomer();
        String body = "{\"customerId\":" + customer + ",\"items\":["
                + "{\"bookId\":" + first + ",\"quantity\":2},"
                + "{\"bookId\":" + second + ",\"quantity\":2}]}";
        mvc.perform(post("/api/orders/checkout").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isConflict()).andExpect(jsonPath("$.message").isNotEmpty());
        mvc.perform(get("/api/books/{id}", first)).andExpect(jsonPath("$.stockQuantity").value(5));
        mvc.perform(get("/api/books/{id}", second)).andExpect(jsonPath("$.stockQuantity").value(1));
        mvc.perform(get("/api/orders")).andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void rejectsInvalidRequestsAndDuplicateIsbn() throws Exception {
        mvc.perform(post("/api/books").contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.details").isArray());
        mvc.perform(post("/api/customers").contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"Test\",\"email\":\"invalid\"}"))
                .andExpect(status().isBadRequest());
        createBook("duplicate", 1);
        mvc.perform(post("/api/books").contentType(MediaType.APPLICATION_JSON).content(bookBody("duplicate", 1)))
                .andExpect(status().isConflict());
        long customer = createCustomer();
        mvc.perform(post("/api/orders/checkout").contentType(MediaType.APPLICATION_JSON)
                .content("{\"customerId\":" + customer + ",\"items\":[]}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void corsAllowsConfiguredFrontendAndRejectsOtherOrigins() throws Exception {
        mvc.perform(options("/api/books").header("Origin", "https://bookflow.example")
                .header("Access-Control-Request-Method", "POST"))
                .andExpect(status().isOk())
                .andExpect(header().string("Access-Control-Allow-Origin", "https://bookflow.example"));
        mvc.perform(options("/api/books").header("Origin", "https://untrusted.example")
                .header("Access-Control-Request-Method", "POST"))
                .andExpect(status().isForbidden());
    }

    private long createBook(String isbn, int stock) throws Exception {
        return create("/api/books", bookBody(isbn, stock)).get("id").asLong();
    }

    private long createCustomer() throws Exception {
        return create("/api/customers", "{\"name\":\"Test Customer\",\"email\":\"test@example.com\"}")
                .get("id").asLong();
    }

    private String bookBody(String isbn, int stock) {
        return "{\"title\":\"Test Book\",\"author\":\"Test Author\",\"isbn\":\"" + isbn
                + "\",\"price\":12.50,\"stockQuantity\":" + stock + "}";
    }

    private String checkoutBody(long customer, long book, int quantity) {
        return "{\"customerId\":" + customer + ",\"items\":[{\"bookId\":" + book
                + ",\"quantity\":" + quantity + "}]}";
    }

    private JsonNode create(String path, String body) throws Exception {
        String response = mvc.perform(post(path).contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        return json.readTree(response);
    }
}
