package com.bookflow.books;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.bookflow.common.ConflictException;
import com.bookflow.common.ResourceNotFoundException;

@Service
public class BookService {

    // Service 只依赖 Repository 接口，具体数据库访问由 Spring Data JPA 生成。
    private final BookRepository bookRepository;

    public BookService(BookRepository bookRepository) {
        this.bookRepository = bookRepository;
    }

    @Transactional(readOnly = true)
    public List<BookResponse> getBooks() {
        // 查询全部图书，并统一转换为响应 DTO。
        return bookRepository.findAll().stream()
                .map(BookResponse::new)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public BookResponse getBook(Long id) {
        return new BookResponse(findBook(id));
    }

    @Transactional
    public BookResponse createBook(BookRequest request) {
        // ISBN 是唯一业务标识，重复时返回 409 Conflict。
        if (bookRepository.existsByIsbn(request.getIsbn())) {
            throw new ConflictException("A book with ISBN " + request.getIsbn() + " already exists.");
        }

        Book book = new Book(
                request.getTitle(),
                request.getAuthor(),
                request.getIsbn(),
                request.getPrice(),
                request.getStockQuantity());

        return new BookResponse(bookRepository.save(book));
    }

    @Transactional
    public BookResponse updateBook(Long id, BookRequest request) {
        Book book = findBook(id);
        // 允许图书保留自己的 ISBN，但不允许改成另一条记录的 ISBN。
        bookRepository.findByIsbn(request.getIsbn())
                .filter(existing -> !existing.getId().equals(id))
                .ifPresent(existing -> {
                    throw new ConflictException("A book with ISBN " + request.getIsbn() + " already exists.");
                });

        book.setTitle(request.getTitle());
        book.setAuthor(request.getAuthor());
        book.setIsbn(request.getIsbn());
        book.setPrice(request.getPrice());
        book.setStockQuantity(request.getStockQuantity());

        return new BookResponse(book);
    }

    @Transactional
    public void deleteBook(Long id) {
        // 删除前先查找，确保不存在时返回明确的 404。
        Book book = findBook(id);
        bookRepository.delete(book);
    }

    private Book findBook(Long id) {
        // 统一封装查找逻辑，避免 Controller/Service 到处重复 404 处理。
        return bookRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Book " + id + " was not found."));
    }
}
