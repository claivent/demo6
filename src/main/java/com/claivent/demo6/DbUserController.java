package com.claivent.demo6;

import com.claivent.demo6.model.DbUser;
import com.claivent.demo6.repository.DbUserRepository;
import java.util.List;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/db")
@CrossOrigin(origins = "*")
public class DbUserController {

    private final DbUserRepository dbUserRepository;

    public DbUserController(DbUserRepository dbUserRepository) {
        this.dbUserRepository = dbUserRepository;
    }

    @GetMapping("/users")
    public List<DbUser> dbUsers() {
        return dbUserRepository.findAll();
    }
}
