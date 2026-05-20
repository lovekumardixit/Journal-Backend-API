package com.love.Backend.repository;

import com.love.Backend.entity.FailedEvent;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface FailedEventRepository extends MongoRepository<FailedEvent, String> {

    List<FailedEvent> findBySentFalse();
}
