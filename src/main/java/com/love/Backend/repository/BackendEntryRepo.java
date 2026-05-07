package com.love.Backend.repository;

import com.love.Backend.entity.entry;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface BackendEntryRepo extends MongoRepository<entry, ObjectId>, UserCustomRepo {

    List<entry> findByTitle(String title);
    List<entry> findByContent(String content);
}
