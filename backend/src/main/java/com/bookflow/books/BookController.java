package com.bookflow.books;

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
@RequestMapping("/api/books")
public class BookController {

    // Controller 只负责 HTTP 输入输出，业务规则交给 Service。
    private final BookService bookService;

    public BookController(BookService bookService) {
        this.bookService = bookService;
    }

    @GetMapping
    public List<BookResponse> getBooks() {
        // 获取全部图书库存记录。
        return bookService.getBooks();
    }

    @GetMapping("/{id}")
    public BookResponse getBook(@PathVariable Long id) {
        // 根据 id 获取单本图书，不存在时由统一异常处理返回 404。
        return bookService.getBook(id);
    }

    @PostMapping
    public ResponseEntity<BookResponse> createBook(@Valid @RequestBody BookRequest request) {
        // @Valid 会触发 BookRequest 上的输入校验。
        BookResponse response = bookService.createBook(request);
        // 创建成功后返回 201 Created，并在 Location header 中放入新资源地址。
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(response.getId())
                .toUri();
        return ResponseEntity.created(location).body(response);
    }

    @PutMapping("/{id}")
    public BookResponse updateBook(@PathVariable Long id, @Valid @RequestBody BookRequest request) {
        // 更新整条图书记录，包括价格和库存数量。
        return bookService.updateBook(id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBook(@PathVariable Long id) {
        // 删除成功后返回 204 No Content。
        bookService.deleteBook(id);
        return ResponseEntity.noContent().build();
    }
}
