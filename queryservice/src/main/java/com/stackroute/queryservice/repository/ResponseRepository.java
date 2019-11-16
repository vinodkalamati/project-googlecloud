package com.stackroute.queryservice.repository;

import com.stackroute.queryservice.domain.NotFoundResponse;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ResponseRepository extends MongoRepository<NotFoundResponse, Integer> {

}
