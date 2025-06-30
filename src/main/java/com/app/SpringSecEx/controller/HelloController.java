package com.app.SpringSecEx.controller;

import com.app.SpringSecEx.model.Student;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
public class HelloController {

    private List<Student> students = new ArrayList<>(List.of(
            new Student(1,"haritha",98),
            new Student(2,"harika",99)

    ));

    @RequestMapping("/")
    public String greeting(HttpServletRequest request){
        return "Hello World" + request.getSession().getId();
    }


    @GetMapping("/students")
    public List<Student> getStudents(){
        return students;
    }

    @GetMapping("/csrf-token")
    public CsrfToken getToken(HttpServletRequest request){
        return (CsrfToken) request.getAttribute("_csrf");
    }

    @PostMapping("/students")
    public Student addStudent(@RequestBody Student student){
        students.add(student);
        return student;
    }

}
