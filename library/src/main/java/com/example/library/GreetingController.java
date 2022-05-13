package com.example.library;

import com.example.library.accessingdatah2.AppUser;
import com.example.library.accessingdatah2.Book;
import com.example.library.accessingdatah2.UserLibrary;
import com.example.library.repos.BookRepository;
import com.example.library.repos.UserLibraryRepository;
import com.example.library.repos.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.io.Console;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Controller
public class GreetingController {
    @Autowired
    private UserRepository userRepo;

    @Autowired
    private BookRepository bookRepo;

    @Autowired
    private UserLibraryRepository ulRepo;

    private Integer userAu = null;

    @Value("${upload.path}")
    private String uploadPath;

    @GetMapping("/main")
    public String main(Map<String, Object> model) {
        if(userAu == null){
            return "redirect:login";
        }
/*
        if(userAu != 1){
            return "redirect:login";
        }
*/
        Iterable<Book> books = bookRepo.findAll();
        model.put("books", books);
        return "main";
    }

    @GetMapping("/findBook")
    public String findBook(Map<String, Object> model) {
        if(userAu == null){
            return "redirect:login";
        }
        return "findBook";
    }

    @GetMapping("/usersBooks")
    public String usersBooks(Map<String, Object> model) {
        if(userAu == null){
            return "redirect:login";
        }
        List<UserLibrary> ulList = ulRepo.findByUserID(userAu);
        List<Integer> userBooks = ulList.stream()
                .map(UserLibrary::getBookID)
                .collect(Collectors.toList());
;
        Iterable<Book> books = bookRepo.findAllById(userBooks);
        model.put("books", books);

        return "usersBooks";
    }

    @PostMapping("addToUserLibrary")
    public String addToUserLibrary(@RequestParam Integer bookId, Map<String, Object> model){
        if(userAu == null){
            return "redirect:login";
        }
        UserLibrary userLibrary = new UserLibrary(userAu, bookId);
        Iterable<UserLibrary> userLibraries = ulRepo.findAll();
        for (UserLibrary ul:userLibraries){
            if(ul.getBookID()==userLibrary.getBookID() && ul.getUserID()==userLibrary.getUserID()){
                return "findBook";
            }
        }
        ulRepo.save(userLibrary);
        return "findBook";
    }

    @PostMapping("addBook")
    public String addBook(@RequestParam String title, @RequestParam String author, @RequestParam("filename") MultipartFile filename, Map<String, Object> model) throws IOException {
        Book book;
        if(author.length() == 0 || title.length() == 0){
            return "redirect:main";
        }
        if(filename.isEmpty() == false){
            File uploadDir = new File(uploadPath);
            if(!uploadDir.exists()){
                uploadDir.mkdir();
            }
            String uuidFile = UUID.randomUUID().toString();
            String resultFile = uuidFile + "." + filename.getOriginalFilename();
            filename.transferTo(new File(uploadPath+ "/" + resultFile));
            book = new Book(title, author, resultFile);
            bookRepo.save(book);
            Iterable<Book> books = bookRepo.findAll();
            model.put("books", books);
            return "redirect:main";
        }

        return "redirect:main";
    }

    @PostMapping("filterBook")
    public String filterBook(@RequestParam String filter, Map<String, Object> model){
        List<Book> books = new ArrayList<>();

        List<Book> booksTitle =  bookRepo.findByTitleContaining(filter);
        List<Book> booksAuthor =  bookRepo.findByAuthorContaining(filter);

        List<UserLibrary> userLibraryList = ulRepo.findByUserID(userAu);

        if(booksTitle.isEmpty() != true && booksAuthor.isEmpty() != true){
            books.addAll(booksTitle);
            for (Book bookA: booksAuthor){
                int check = 0;
                for(Book bookT: booksTitle){
                    if(bookA.getId() == bookT.getId()){
                        check = 1;
                        break;
                    }
                }
                if(check == 0) books.add(bookA);
            }
        }
        else{
            books.addAll(booksTitle);
            books.addAll(booksAuthor);
        }

        if(userLibraryList.isEmpty() == false){
            List<Book> res = new ArrayList<>();
            for(Book book: books){
                int check = 0;
                 for(UserLibrary ul: userLibraryList){
                     if (book.getId()==ul.getBookID()) {
                         check = 1;
                         break;
                     }
                 }
                if(check == 0) res.add(book);
            }
            model.put("books", res);
            return "findBook";
        }

        model.put("books", books);
        return "findBook";
    }

    @PostMapping("filterBookByID")
    public String filterBookByID(@RequestParam String filter, Map<String, Object> model){
        if(filter==""){
            return "redirect:main";
        }

        try {
            Integer.valueOf(filter);
        }
        catch (NumberFormatException ex){
            return "redirect:main";
        }
        Integer id = Integer.valueOf(filter);



        if(bookRepo.existsById(id)){
            Iterable<UserLibrary> ul = ulRepo.findByBookID(id);
            for(UserLibrary user:ul){
                ulRepo.deleteById(user.getId());
            }
            bookRepo.deleteById(id);
            return "redirect:main";

        }
        return "redirect:main";
    }

    @GetMapping("/login")
    public String authorization(Map<String, Object> model) {
        return "login";
    }

    @GetMapping("/registration")
    public String registration(Map<String, Object> model) {
        return "registration";
    }

    @PostMapping("authorization")
    public String authorization(@RequestParam String login, @RequestParam String password, Map<String, Object> model){
        String message;
        List<AppUser> users = userRepo.findByLogin(login);
        userAu = null;

        if(users.isEmpty()){
            message = "Этот логин не существует";
            model.put("message", message);
            return "login";
        }

        if(login.equals("admin")){
            List<AppUser> res = userRepo.findByLogin(login);
            if (res.get(0).getPassword().equals(password)){
                userAu =  users.get(0).getId();
                return "redirect:main";
            }
            message = "Неверный пароль";
            model.put("message", message);
            return "login";
        }

        if(users.get(0).getPassword().equals(password)){
            userAu =  users.get(0).getId();
            return "redirect:userPage";
        }
        else{
            message = "Неверный пароль";
            model.put("message", message);
            return "login";
        }

    }

    @PostMapping("check")
    public String check(@RequestParam String login, @RequestParam String password, Map<String, Object> model){
        String message;
        if(login=="" || password==""){
            message = "Заполните все поля";
            model.put("message", message);
            return "registration";
        }
        List<AppUser> users = userRepo.findByLogin(login);
        if(!users.isEmpty()){
            message = "Такой логин уже существует";
            model.put("message", message);
            return "registration";
        }

        AppUser user = new AppUser(login, password);
        userRepo.save(user);
        return "redirect:login";

    }

    @GetMapping("/userPage")
    public String userPage (Map<String, Object> model) {
        if(userAu == null){
            return "redirect:login";
        }

        String userLogin = userRepo.findById(userAu).get().getLogin();
        model.put("userLogin", userLogin);

        return "userPage";
    }
}
