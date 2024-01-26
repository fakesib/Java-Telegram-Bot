package com.fakesibwork.channel_bot.repo;

import com.fakesibwork.channel_bot.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@Transactional
public interface UserRepo extends JpaRepository<User, Integer> {

    @Modifying
    @Query(value = "INSERT INTO users(id, user_id, username) VALUES(DEFAULT, ?1, ?2)", nativeQuery = true)
    void addNewUser(long userId, String userName);
}
