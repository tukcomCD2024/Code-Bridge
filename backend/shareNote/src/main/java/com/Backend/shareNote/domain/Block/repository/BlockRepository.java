package com.Backend.shareNote.domain.Block.repository;

import com.Backend.shareNote.domain.Block.entity.Block;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BlockRepository extends MongoRepository<Block, String> {
}
