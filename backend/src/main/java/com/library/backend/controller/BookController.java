package com.library.backend.controller;

import com.library.backend.entity.Book;
import com.library.backend.entity.Manager;
import com.library.backend.entity.User;
import com.library.backend.model.Result;
import com.library.backend.repository.BookRepository;
import com.library.backend.repository.ManagerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.DigestUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.library.backend.utils.Constant.*;

@RestController
@RequestMapping("/book")
public class BookController {

    @Autowired
    private BookRepository bookRepository;

    @GetMapping("/list")
    public Result managerLogin(Book book) {
        if (book.getBookName() != null && !"".equals(book.getBookName())) {
            List<Book> books = bookRepository.findAllByBookNameContaining(book.getBookName());
            return new Result(SUCCESS_CODE, "", books);
        } else {
            List<Book> books = bookRepository.findAll();
            return new Result(SUCCESS_CODE, "", books);

        }
    }

    @PostMapping("/add")
    public Result addBook(@RequestBody Book book) {
        try {
            Book book1 = bookRepository.findByBookName(book.getBookName());
            if (book1 != null) {
                return new Result(NAME_REPEAT, "书名重复");
            }
            bookRepository.save(book);
            return new Result(SUCCESS_CODE, "新增成功", book);
        } catch (Exception e) {

            return new Result(FAILE_CODE, e.toString(), book);
        }
    }
    @PostMapping("/update")
    public Result update(@RequestBody Book book) {
        try {
            bookRepository.save(book);
            return new Result(SUCCESS_CODE, "修改成功", book);
        } catch (Exception e) {
            return new Result(FAILE_CODE, e.toString(), book);
        }
    }


    @DeleteMapping("/delete")
    public Result delete(@RequestBody Book book) {
        try {
            bookRepository.deleteById(book.getId());
            return new Result(SUCCESS_CODE, "删除成功", book);
        } catch (Exception e) {
            return new Result(FAILE_CODE, e.toString(), book);
        }
    }
}
