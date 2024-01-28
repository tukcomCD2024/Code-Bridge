package com.Backend.shareNote.domain.Oraganization.repository;

import com.Backend.shareNote.domain.Oraganization.entity.Organization;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface OrganizationRepository extends MongoRepository<Organization, String> {

}
