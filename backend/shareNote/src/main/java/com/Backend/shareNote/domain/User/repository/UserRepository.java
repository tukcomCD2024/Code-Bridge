package com.Backend.shareNote.domain.User.repository;

import com.Backend.shareNote.domain.User.entity.Users;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import org.springframework.web.bind.annotation.RequestBody;

@Repository
public interface UserRepository extends MongoRepository<Users, String> {

}
