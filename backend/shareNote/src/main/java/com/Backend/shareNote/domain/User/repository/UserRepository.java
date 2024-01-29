package com.Backend.shareNote.domain.User.repository;

import com.Backend.shareNote.domain.User.entity.Users;
import org.apache.catalina.User;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Optional;

@Repository
public interface UserRepository extends MongoRepository<Users, String> {
    Optional<Users> findByLoginId(String loginId);
}
