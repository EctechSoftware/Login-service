package com.ectech.login.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.ectech.login.entities.UserEntity;

@Repository
public interface UserRepository extends JpaRepository<UserEntity, Integer> {

    @Query(value = "SELECT u FROM usuarios u WHERE u.name = :name AND u.password = :password")
    public UserEntity findOneByNameAndPassword(@Param("name") String name, @Param("password") String password);

    @Query(value = "SELECT u FROM User u WHERE u.userName=:username")
    public UserEntity findOneByUserName(@Param("username") String username);
}
