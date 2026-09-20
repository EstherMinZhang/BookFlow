package com.bookflow.books;

import java.util.Optional;

import javax.persistence.LockModeType;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;

// 图书数据访问层：Spring Data JPA 会自动实现常见 CRUD 方法。
public interface BookRepository extends JpaRepository<Book, Long> {

    // 创建图书时检查 ISBN 是否已经存在，用于返回 409 Conflict。
    boolean existsByIsbn(String isbn);

    // 更新图书时根据 ISBN 查找已有记录，防止改成其他图书正在使用的 ISBN。
    Optional<Book> findByIsbn(String isbn);

    // 结账扣库存时锁定图书行，避免并发订单同时扣减同一份库存。
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<Book> findLockedById(Long id);
}
