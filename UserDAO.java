package dao;

import model.User;

import java.util.Optional;
/** Operations for login accounts. */
public interface UserDAO {

    void save(User user);

    Optional<User> findByUsername(String username);

    boolean existsByUsername(String username);

    int count();
}
