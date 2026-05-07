package com.love.Backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.love.Backend.dto.request.UserRequestDTO;
import com.love.Backend.kafka.producer.RegisterProducer;
import com.love.Backend.service.UserEntryService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class PublicControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserEntryService userService;

    @MockBean
    private RegisterProducer kafkaProducerService;

    @Test
    public void testCreateUser_Success() throws Exception {
        UserRequestDTO dto = new UserRequestDTO();
        dto.setUserName("newuser");
        dto.setPassword("password");
        dto.setEmail("newuser@example.com");

        when(userService.saveNewUser(dto)).thenReturn(dto);

        mockMvc.perform(post("/public/create-user")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto))
                        .with(csrf()))
                .andExpect(status().isCreated());
    }

    @Test
    public void testCreateUser_UserExists() throws Exception {
        UserRequestDTO dto = new UserRequestDTO();
        dto.setUserName("existinguser");

        when(userService.saveNewUser(dto)).thenReturn(null);

        mockMvc.perform(post("/public/create-user")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto))
                        .with(csrf()))
                .andExpect(status().isCreated()); 
                
                
                
    }
}
