package com.library.backend;

import com.library.backend.entity.Book;
import com.library.backend.entity.Manager;
import com.library.backend.repository.BookRepository;
import com.library.backend.repository.ManagerRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.util.DigestUtils;

//@SpringBootTest
//class BackendApplicationTests {

//	@Autowired
//	private ManagerRepository managerRepository;
//	@Autowired
//	private BookRepository bookRepository;
//	@Test
//	void contextLoads() {
//	}
//	@Test
//	void addManager() {
//		Manager manager = new Manager();
//		manager.setPassword(DigestUtils.md5DigestAsHex("admin".getBytes()));
//		manager.setUsername("admin");
//		managerRepository.save(manager);
//	}
//	@Test
//	void addBook() {
//		Book book = new Book();
//		book.setBookName("书2");
//		book.setBorrow(10);
//		book.setPress("出版社2");
//		book.setAuthor("作者2");
//		bookRepository.save(book);
//	}
//}
