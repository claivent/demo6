package com.claivent.demo6;

import com.claivent.demo6.model.DbUser;
import com.claivent.demo6.repository.DbUserRepository;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
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

    @PostMapping("/add")
    public ResponseEntity<DbUser> addUser(@RequestBody DbUser user) {
        DbUser saved = dbUserRepository.save(user);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Map<String, String>> deleteUser(@PathVariable Long id) {
        if (!dbUserRepository.existsById(id)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "User " + id + " not found"));
        }
        dbUserRepository.deleteById(id);
        return ResponseEntity.ok(Map.of("message", "User " + id + " deleted"));
    }

    @PatchMapping("/modify/{id}")
    public ResponseEntity<DbUser> modifyUser(@PathVariable Long id, @RequestBody DbUser patch) {
        return dbUserRepository.findById(id).map(user -> {
            if (patch.getName() != null)  user.setName(patch.getName());
            if (patch.getEmail() != null) user.setEmail(patch.getEmail());
            return ResponseEntity.ok(dbUserRepository.save(user));
        }).orElse(ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }
}
