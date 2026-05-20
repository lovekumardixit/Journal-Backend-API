package com.love.Backend.repository;

import com.love.Backend.entity.BackendConfigEntity;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface BackendConfigRepo extends MongoRepository<BackendConfigEntity, ObjectId>, BackendConfigCustomRepo {
}
