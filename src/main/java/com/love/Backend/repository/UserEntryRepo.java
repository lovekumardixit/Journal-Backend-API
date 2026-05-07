package com.love.Backend.repository;

import com.love.Backend.dto.request.UserRequestDTO;
import com.love.Backend.entity.User;
import com.love.Backend.entity.User;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
@Component
public interface UserEntryRepo extends MongoRepository<User, ObjectId>, UserCustomRepo {

   User findByUserName(String userName);
   boolean existsByUserName(String userName);

}
