package com.Backend.shareNote.domain.Oraganization.repository;

import com.Backend.shareNote.domain.Oraganization.entity.Organization;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PageRepository extends MongoRepository<Organization.Page, String> {
}
