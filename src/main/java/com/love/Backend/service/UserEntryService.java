package com.love.Backend.service;
import com.love.Backend.dto.request.UserRequestDTO;
import com.love.Backend.dto.response.UserResponseDTO;
import com.love.Backend.exception.UserAlreadyExistsException;
import com.love.Backend.exception.UserNotFoundException;
import com.love.Backend.repository.BackendEntryRepo;
import com.love.Backend.dto.response.UserResponseDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.love.Backend.entity.User;
import com.love.Backend.repository.UserEntryRepo;


import org.bson.types.ObjectId;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.TimeUnit;


@Component
public class UserEntryService {
    @Autowired
    private UserEntryRepo userRepo;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    private static final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public void saveUser(User user){
        userRepo.save(user);
    }

    private static final Logger logger = LoggerFactory.getLogger(UserEntryService.class);

    @CacheEvict(value = "users", key = "#dto.userName") 
    public UserResponseDTO fullUserUpdate(UserRequestDTO dto){ 
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userName = authentication.getName();
        User user = userRepo.findByUserName(userName);

        if(user == null){
            throw new RuntimeException("User not found");
        }

        
        user.setUserName(dto.getUserName());
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setSentimentAnalysis(dto.getSentimentAnalysis());
        user.setEmail(dto.getEmail());
        user.setFirstName(dto.getFirstName());
        user.setLastName(dto.getLastName());
        user.setCity(dto.getCity());

        userRepo.save(user);

        
        UserResponseDTO response = new UserResponseDTO();
        response.setUserName(user.getUserName());
        response.setEmail(user.getEmail());
        response.setRoles(user.getRoles());
        response.setProfilePhotoUrl(user.getProfilePhotoUrl());
        response.setFirstName(user.getFirstName());
        response.setLastName(user.getLastName());
        response.setCity(user.getCity());
        response.setAge(user.getAge());

        return response;
    }

    public UserResponseDTO partialUserUpdate(UserRequestDTO dto){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userName = authentication.getName();
        User user = userRepo.findByUserName(userName);
        if(user == null){
            throw new UserNotFoundException(dto.getUserName()+ " not found");
        }
        if(dto.getUserName() != null){
            user.setUserName(dto.getUserName());
        }
        if(dto.getPassword() != null){
            user.setPassword(passwordEncoder.encode(dto.getPassword()));
        }
        if(dto.getEmail() != null){
            user.setEmail(dto.getEmail());
        }
        if(dto.getSentimentAnalysis() != null){
            user.setSentimentAnalysis(dto.getSentimentAnalysis());
        }
        if(dto.getFirstName() != null){
            user.setFirstName(dto.getFirstName());
        }
        if(dto.getLastName() != null){
            user.setLastName(dto.getLastName());
        }
        if(dto.getCity() != null){
            user.setCity(dto.getCity());
        }
        userRepo.save(user);
        UserResponseDTO res = new UserResponseDTO();
        res.setUserName(user.getUserName());
        res.setEmail(user.getEmail());
        res.setRoles(user.getRoles());
        res.setProfilePhotoUrl(user.getProfilePhotoUrl());
        res.setFirstName(user.getFirstName());
        res.setLastName(user.getLastName());
        res.setCity(user.getCity());
        return res;
    }


    public boolean saveNewUser(User user) {
        try {
            
            if (userRepo.existsByUserName(user.getUserName())){
                logger.warn("User already exists with username: {}", user.getUserName());
                return false; 
            }

            
            user.setPassword(passwordEncoder.encode(user.getPassword()));

            
            user.setRoles(Arrays.asList("USER"));

            
            userRepo.save(user);

            return true;

        } catch (Exception e) {
            logger.error("Error while saving user", e);
            return false;
        }
    }

    public void saveAdmin(User user){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setRoles(Arrays.asList("USER", "ADMIN"));
        userRepo.save(user);

    }



    public List<User> getAll(){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        return userRepo.findAll();
    }
    public Optional<User> findById(ObjectId id){

        return userRepo.findById(id);
    }

    public void deleteById(ObjectId id){

         userRepo.deleteById(id);
    }

    public User findByName(String userName){
        return userRepo.findByUserName(userName);
    }



    public UserResponseDTO findByUserName(String userName){

        User user = userRepo.findByUserName(userName);

        if(user == null){
            throw new UserNotFoundException("User not found");
        }

        UserResponseDTO dto = new UserResponseDTO();
        dto.setUserName(user.getUserName());
        dto.setEmail(user.getEmail());
        dto.setRoles(user.getRoles());
        dto.setFirstName(user.getFirstName());
        dto.setLastName(user.getLastName());
        dto.setCity(user.getCity());
        dto.setAge(user.getAge());

        return dto;
    }




























































    public List<User> findUsersAboveAge(int age){
        return userRepo.findUsersByAge(age);
    }

    public UserRequestDTO saveNewUser(UserRequestDTO dto){
        // Validate username uniqueness
        if(userRepo.existsByUserName(dto.getUserName())){
            throw new UserAlreadyExistsException("User Already Exist with this username : "+dto.getUserName());
        }

        // Validate email uniqueness
        if(userRepo.existsByEmail(dto.getEmail())){
            throw new UserAlreadyExistsException("User Already Exist with this email : "+dto.getEmail());
        }

        // Normalize username to lowercase
        String normalizedUsername = dto.getUserName().toLowerCase();

        // Normalize email to lowercase
        String normalizedEmail = dto.getEmail().toLowerCase();

        User user = new User();
        user.setUserName(normalizedUsername);
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setEmail(normalizedEmail);
        user.setFirstName(dto.getFirstName());
        user.setLastName(dto.getLastName());
        user.setAge(dto.getAge());
        user.setCity(dto.getCity());
        user.setRoles(Arrays.asList("USER"));
        user.setProfilePhotoUrl(dto.getProfilePhotoUrl());

        userRepo.save(user);
        dto.setId(user.getId());
        System.out.println("saved user :" + user.getUserName());
//        System.out.println(userRepo.findAll());
        return dto;

    }


// (ensure this import exists at top of file)

    public List<UserResponseDTO> getAllUsersDTO() {
        List<User> users = getAll();

        if (users == null || users.isEmpty()) {
            return Collections.emptyList();
        }

        return users.stream().map(user -> {
            UserResponseDTO dto = new UserResponseDTO();
            dto.setUserName(user.getUserName());
            dto.setEmail(user.getEmail());
            dto.setRoles(user.getRoles());
            dto.setProfilePhotoUrl(user.getProfilePhotoUrl());
            dto.setFirstName(user.getFirstName());
            dto.setLastName(user.getLastName());
            dto.setCity(user.getCity());
            return dto;
        }).toList();
    }

}
