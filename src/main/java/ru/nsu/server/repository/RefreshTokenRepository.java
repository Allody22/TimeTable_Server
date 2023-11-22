package ru.nsu.server.repository;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.nsu.server.model.RefreshToken;
import ru.nsu.server.model.User;

import java.util.Optional;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    Optional<RefreshToken> findByToken(String token);

    Optional<RefreshToken> findByUser(User user);

    @Modifying
    @Query(value = "DELETE FROM refresh_token WHERE user_id = :UserId", nativeQuery = true)
    void deleteAllByUserId(@Param("UserId") Long userId);

    @Modifying
    int deleteByUser(User user);
}
