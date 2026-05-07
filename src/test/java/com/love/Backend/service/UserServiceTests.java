package com.love.Backend.service;


import com.love.Backend.entity.User;
import com.love.Backend.repository.UserEntryRepo;
import com.love.Backend.service.UserEntryService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import org.junit.jupiter.params.provider.ArgumentsSource;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest

public class UserServiceTests {

    @Autowired
    private UserEntryService entryService;
    @Autowired
    private UserEntryService userService;
    @Autowired
    private UserEntryRepo userRepo;











    @ParameterizedTest
    @ArgumentsSource(UserArgumentsProvider.class)
    public void saveNewUser(User user){ assertTrue(userService.saveNewUser(user));   }

    @Test
    void TestFindUser(){   
        User user = userRepo.findByUserName("Priyanka");

        assertNotNull(user);
    }

}
