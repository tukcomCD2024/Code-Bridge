package com.Backend.shareNote.domain.User.repository;

import com.Backend.shareNote.domain.User.entity.Refresh;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface RefreshRepository extends MongoRepository<Refresh,String> {
    Boolean existsByRefresh(String refresh);
    @Transactional
    void deleteByRefresh(String refresh);

    Refresh findByUserId(String userId);
}
