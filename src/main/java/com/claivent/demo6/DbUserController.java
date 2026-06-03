package com.claivent.demo6;

import com.claivent.demo6.model.DbUser;
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

    private final DbUserServices dbUserServices;

    public DbUserController(DbUserServices dbUserServices) {
        this.dbUserServices = dbUserServices;
    }

    @GetMapping("/users")
    public List<DbUser> dbUsers() {
        return dbUserServices.getAllUsers();
    }

    @PostMapping("/add")
    public ResponseEntity<DbUser> addUser(@RequestBody DbUser user) {
        return ResponseEntity.status(HttpStatus.CREATED).body(dbUserServices.addUser(user));
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Map<String, String>> deleteUser(@PathVariable Long id) {
        if (!dbUserServices.deleteUser(id)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "User " + id + " not found"));
        }
        return ResponseEntity.ok(Map.of("message", "User " + id + " deleted"));
    }

    @PatchMapping("/modify/{id}")
    public ResponseEntity<DbUser> modifyUser(@PathVariable Long id, @RequestBody DbUser patch) {
        return dbUserServices.modifyUser(id, patch)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }
}
