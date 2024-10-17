package com.library.backend.controller;

import com.library.backend.dto.BorrowDTO;
import com.library.backend.entity.Book;
import com.library.backend.entity.Borrow;
import com.library.backend.model.Result;
import com.library.backend.repository.BookRepository;
import com.library.backend.repository.BorrowRepository;
import com.library.backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import static com.library.backend.utils.Constant.*;

@RestController
@RequestMapping("/borrow")
public class BorrowController {

    @Autowired
    private BookRepository bookRepository;
    @Autowired
    private BorrowRepository borrowRepository;

    @Autowired
    private UserRepository userRepository;

    @GetMapping("/list")
    public Result list(Borrow borrow) {
        if (borrow.getBorrowNo() != null && !"".equals(borrow.getBorrowNo())) {
            List<Borrow> borrows = borrowRepository.findAllByBorrowNoContaining(borrow.getBorrowNo());
            List<BorrowDTO> borrowDTOS = toDTO(borrows);
            return new Result(SUCCESS_CODE, "", borrowDTOS);
        } else {
            List<Borrow> borrows = borrowRepository.findAll();
            List<BorrowDTO> borrowDTOS = toDTO(borrows);
            return new Result(SUCCESS_CODE, "", borrowDTOS);

        }
    }
    public List<BorrowDTO> toDTO(List<Borrow> borrows) {
        List<BorrowDTO> borrowDTOS = new ArrayList<>();
        for (int i = 0; i< borrows.size(); i++) {
            Borrow borrows1 = borrows.get(i);
            BorrowDTO borrowDTO = new BorrowDTO();
            borrowDTO.setBorrowNo(borrows1.getBorrowNo());
            Book book = bookRepository.findById(borrows1.getBookId());
            borrowDTO.setBookName(book.getBookName());
            borrowDTO.setReturnDate(borrows1.getReturnDate());
            borrowDTO.setStatus(borrows1.getStatus());
            borrowDTO.setBorrowDate(borrows1.getBorrowDate());
            borrowDTOS.add(borrowDTO);
            borrowDTO.setId(borrows1.getId());
        }
        return borrowDTOS;
    }

    @PostMapping("/add")
    public Result add(@RequestBody Borrow borrow) {
        try {
            borrow.setStatus("N");
            borrowRepository.save(borrow);
            return new Result(SUCCESS_CODE, "新增成功", borrow);
        } catch (Exception e) {
            return new Result(FAILE_CODE, e.toString(), borrow);
        }
    }


    @PostMapping("/addBorrow")
    public Result addBorrow(@RequestBody BorrowDTO borrowDTO) {
        try {
            Book book = bookRepository.findByBookName(borrowDTO.getBookName());
            if (book.getBorrow() > 1) {
                book.setBorrow(book.getBorrow() - 1);
                bookRepository.save(book);
                Borrow borrow = new Borrow();
                borrow.setBorrowNo(borrowDTO.getBorrowNo());
                borrow.setBookId(book.getId());
                borrow.setBorrowDate(Instant.now());
                borrow.setStatus("N");
                borrow.setReturnDate(borrowDTO.getReturnDate());
                borrowRepository.save(borrow);
                return new Result(SUCCESS_CODE, "新增成功", borrow);
            }else
                return new Result(FAILE_CODE, "库存不足，无法借阅", borrowDTO);

        } catch (Exception e) {
            return new Result(FAILE_CODE, e.toString(), borrowDTO);
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

    @PostMapping("/return")
    public Result returnBook(@RequestBody BorrowDTO borrow) {
        try {
            borrowRepository.updateInfo("Y",borrow.getId());
            Book book = bookRepository.findByBookName(borrow.getBookName());
            book.setBorrow(book.getBorrow() + 1);
            bookRepository.save(book);
            return new Result(SUCCESS_CODE, "还书成功", borrow);
        } catch (Exception e) {
            return new Result(FAILE_CODE, e.toString(), borrow);
        }
    }

    @DeleteMapping("/delete")
    public Result delete(@RequestBody Borrow borrow) {
        try {
            borrowRepository.deleteById(borrow.getId());
            return new Result(SUCCESS_CODE, "删除成功", borrow);
        } catch (Exception e) {
            return new Result(FAILE_CODE, e.toString(), borrow);
        }
    }
}
