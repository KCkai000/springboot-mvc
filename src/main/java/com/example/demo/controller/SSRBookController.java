package com.example.demo.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.example.demo.exception.BookException;
import com.example.demo.model.Book;
import com.example.demo.service.BookService;

@Controller
@RequestMapping("/ssr/book")
public class SSRBookController {
	
	@Autowired
	private BookService bookService;
	
	//查詢所有書籍
	@GetMapping
	public String findAllBooks(Model model) {
		List<Book> books = bookService.findAllBooks();
		model.addAttribute("books", books);
		return "book-list";  //這段就是在以前servlet裡面那段 REQUESTdISPECHER.FORWORD的那段導向
	}
	
	//新增書籍
	@PostMapping("/add")
	public String addBook(Book book, Model model) {
		try {
			bookService.addBook(book);			
		}catch(BookException e) {
			model.addAttribute("message", "新增錯誤:" + e.getMessage());
			return "error";
		}
		return "redirect:/ssr/book"; //redirect是告訴瀏覽器 自己再重新導到標的jsp
		//會重導回列表是因為新增後可以直接反映給使用者看
	}
	
	//刪除書籍
	@GetMapping("/delete/{id}")
	public String deleteBook(@PathVariable Integer id, Model model) {
		try {
			bookService.deleteBook(id);			
		}catch(BookException e){
			model.addAttribute("message", "刪除錯誤:" + e.getMessage());
			return "error";
		}
		return "redirect:/ssr/book";
	}
	
	//編輯書籍
	@PutMapping("/update")
	public String updateBook(@PathVariable Integer id, Model model) {
		try {
			bookService.updateBook(id, null);
		}catch(BookException e) {
			model.addAttribute("message", "更新錯誤:" + e.getMessage());
			return "error";
		}
		return "redirect:/ssr/book";		
	}
}
