package com.bookflow.books;

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
class BookControllerTest {

    // MockMvc 可以在不启动真实浏览器或服务器的情况下测试 REST API。
    @Autowired
    private MockMvc mockMvc;

    // 测试中直接操作 Repository，用于准备和清理测试数据。
    @Autowired
    private BookRepository bookRepository;

    @BeforeEach
    void setUp() {
        // 每个测试前清空数据库，保证测试之间互不影响。
        bookRepository.deleteAll();
    }

    @Test
    void createBookReturnsCreatedBook() throws Exception {
        // 创建图书成功时应返回 201、Location header 和完整响应体。
        mockMvc.perform(post("/api/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"Clean Code\",\"author\":\"Robert C. Martin\",\"isbn\":\"9780132350884\",\"price\":42.50,\"stockQuantity\":8}"))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", notNullValue()))
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.title", is("Clean Code")))
                .andExpect(jsonPath("$.stockQuantity", is(8)));
    }

    @Test
    void updateBookChangesInventoryFields() throws Exception {
        // 先创建一条数据，再通过 PUT 更新图书和库存字段。
        Book book = bookRepository.save(new Book("Old Title", "Old Author", "111", java.math.BigDecimal.TEN, 2));

        mockMvc.perform(put("/api/books/" + book.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"Refactoring\",\"author\":\"Martin Fowler\",\"isbn\":\"9780201485677\",\"price\":55.00,\"stockQuantity\":4}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title", is("Refactoring")))
                .andExpect(jsonPath("$.stockQuantity", is(4)));
    }

    @Test
    void deleteBookRemovesBook() throws Exception {
        // 删除后再次查询同一个 id，应该得到 404。
        Book book = bookRepository.save(new Book("Domain-Driven Design", "Eric Evans", "9780321125217", java.math.BigDecimal.valueOf(64), 3));

        mockMvc.perform(delete("/api/books/" + book.getId()))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/books/" + book.getId()))
                .andExpect(status().isNotFound());
    }

    @Test
    void invalidBookReturnsValidationError() throws Exception {
        // 非法请求体会触发 @Valid 校验，并由全局异常处理器返回 400。
        mockMvc.perform(post("/api/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"\",\"author\":\"\",\"isbn\":\"\",\"price\":-1,\"stockQuantity\":-2}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", is("Request validation failed.")));
    }
}
