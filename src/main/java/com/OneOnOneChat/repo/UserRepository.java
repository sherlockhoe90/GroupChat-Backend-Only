package com.OneOnOneChat.repo;

import com.OneOnOneChat.entity.Status;
import com.OneOnOneChat.entity.User;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface UserRepository extends MongoRepository<User,String> {

    List<User> findAllByStatus(Status status);
}
