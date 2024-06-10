package com.Backend.shareNote.domain.User.repository;

import com.Backend.shareNote.domain.User.entity.Fcm;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface FcmRepository extends MongoRepository<Fcm, String> {
    Boolean existsByFcm(String fcm);
    @Transactional
    void deleteByFcm(String fcm);
    Fcm findByUserId(String userId);
}
