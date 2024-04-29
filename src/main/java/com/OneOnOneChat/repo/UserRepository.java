package com.OneOnOneChat.repo;

import com.OneOnOneChat.entity.Status;
import com.OneOnOneChat.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserRepository extends JpaRepository<User,Long> {

    List<User> findAllByStatus(Status status);
    boolean existsById(Long id);
}
