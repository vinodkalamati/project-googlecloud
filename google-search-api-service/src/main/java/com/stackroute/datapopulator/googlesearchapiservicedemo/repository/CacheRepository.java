package com.stackroute.datapopulator.googlesearchapiservicedemo.repository;

import com.stackroute.datapopulator.googlesearchapiservicedemo.domain.Cache;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CacheRepository extends MongoRepository<Cache, Integer> {

}
