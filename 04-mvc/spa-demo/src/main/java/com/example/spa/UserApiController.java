package com.example.spa;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class UserApiController {

    @GetMapping("/users/{id}")
    public User getUser(@PathVariable int id) {
        return new User(id, "User" + id, "user" + id + "@example.com");
    }

    static class User {
        private int id;
        private String name;
        private String email;

        public User(int id, String name, String email) {
            this.id = id;
            this.name = name;
            this.email = email;
        }

        public int getId() { return id; }
        public String getName() { return name; }
        public String getEmail() { return email; }
    }
}
