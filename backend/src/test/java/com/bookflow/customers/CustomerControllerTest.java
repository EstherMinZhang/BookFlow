package com.bookflow.customers;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class CustomerControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private CustomerRepository customerRepository;

    @BeforeEach
    void setUp() {
        customerRepository.deleteAll();
    }

    @Test
    void createCustomerReturnsCreatedCustomer() throws Exception {
        mockMvc.perform(post("/api/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Ada Lovelace\",\"email\":\"ada@example.com\",\"phone\":\"604-555-0101\"}"))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", notNullValue()))
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.name", is("Ada Lovelace")))
                .andExpect(jsonPath("$.email", is("ada@example.com")));
    }

    @Test
    void updateCustomerChangesProfileFields() throws Exception {
        Customer customer = customerRepository.save(new Customer("Ada", "ada@example.com", "111"));

        mockMvc.perform(put("/api/customers/" + customer.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Grace Hopper\",\"email\":\"grace@example.com\",\"phone\":\"222\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is("Grace Hopper")))
                .andExpect(jsonPath("$.email", is("grace@example.com")));
    }

    @Test
    void deleteCustomerRemovesCustomer() throws Exception {
        Customer customer = customerRepository.save(new Customer("Ada", "ada@example.com", "111"));

        mockMvc.perform(delete("/api/customers/" + customer.getId()))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/customers/" + customer.getId()))
                .andExpect(status().isNotFound());
    }

    @Test
    void invalidCustomerReturnsValidationError() throws Exception {
        mockMvc.perform(post("/api/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"\",\"email\":\"not-an-email\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", is("Request validation failed.")));
    }
}
