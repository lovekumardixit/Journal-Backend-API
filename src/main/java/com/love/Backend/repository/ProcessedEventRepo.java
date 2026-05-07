package com.love.Backend.repository;

import com.love.Backend.entity.ProcessedEvent;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProcessedEventRepo extends MongoRepository<ProcessedEvent, String> {

}
