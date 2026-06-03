package com.claivent.demo6;

import com.claivent.demo6.model.DbUser;
import com.claivent.demo6.repository.DbUserRepository;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;

@Service
public class DbUserServices {

    private final DbUserRepository dbUserRepository;

    public DbUserServices(DbUserRepository dbUserRepository) {
        this.dbUserRepository = dbUserRepository;
    }

    public List<DbUser> getAllUsers() {
        return dbUserRepository.findAll();
    }

    public DbUser addUser(DbUser user) {
        return dbUserRepository.save(user);
    }

    public boolean deleteUser(Long id) {
        if (!dbUserRepository.existsById(id)) return false;
        dbUserRepository.deleteById(id);
        return true;
    }

    public Optional<DbUser> modifyUser(Long id, DbUser patch) {
        return dbUserRepository.findById(id).map(user -> {
            if (patch.getName() != null)  user.setName(patch.getName());
            if (patch.getEmail() != null) user.setEmail(patch.getEmail());
            return dbUserRepository.save(user);
        });
    }
}
