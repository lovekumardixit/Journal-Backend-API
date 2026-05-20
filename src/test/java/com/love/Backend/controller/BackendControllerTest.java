package com.love.Backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.love.Backend.dto.response.EntryResponse;
import com.love.Backend.entity.User;
import com.love.Backend.entity.entry;
import com.love.Backend.service.BackendEntryService;
import com.love.Backend.service.UserEntryService;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class BackendControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private BackendEntryService entryService;

    @MockBean
    private UserEntryService userService;

    @Test
    @WithMockUser(username = "testuser")
    public void testCreateEntry() throws Exception {
        entry newEntry = new entry();
        newEntry.setTitle("Test Title");
        newEntry.setContent("Test Content");

        User user = User.builder().userName("testuser").build();
        when(userService.findByName("testuser")).thenReturn(user);

        mockMvc.perform(post("/entry")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newEntry))
                        .with(csrf()))
                .andExpect(status().isCreated());
    }

    @Test
    @WithMockUser(username = "testuser")
    public void testGetAllEntriesOfUser() throws Exception {
        User user = User.builder().userName("testuser").entries(List.of(new entry())).build();
        when(userService.findByName("testuser")).thenReturn(user);
        when(entryService.getAllEntriesOfUser("testuser")).thenReturn(List.of(EntryResponse.builder().title("Test").build()));

        mockMvc.perform(get("/entry"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "testuser")
    public void testGetEntriesByUserId_Success() throws Exception {
        ObjectId id = new ObjectId();
        User loggedUser = User.builder().id(id).userName("testuser").build();
        User targetUser = User.builder().id(id).entries(List.of(new entry())).build();

        when(userService.findByName("testuser")).thenReturn(loggedUser);
        when(userService.findById(id)).thenReturn(Optional.of(targetUser));

        mockMvc.perform(get("/entry/{id}", id.toString()))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "testuser")
    public void testGetEntriesByUserId_Forbidden() throws Exception {
        ObjectId loggedId = new ObjectId();
        ObjectId targetId = new ObjectId();
        User loggedUser = User.builder().id(loggedId).userName("testuser").build();
        User targetUser = User.builder().id(targetId).entries(List.of(new entry())).build();

        when(userService.findByName("testuser")).thenReturn(loggedUser);
        when(userService.findById(targetId)).thenReturn(Optional.of(targetUser));

        mockMvc.perform(get("/entry/{id}", targetId.toString()))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "testuser")
    public void testDeleteEntry() throws Exception {
        ObjectId id = new ObjectId();
        when(entryService.deleteById(id, "testuser")).thenReturn(true);

        mockMvc.perform(delete("/entry/id/{id}", id.toString()))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(username = "testuser")
    public void testUpdateEntry() throws Exception {
        ObjectId id = new ObjectId();
        entry updatedEntry = new entry();
        updatedEntry.setTitle("Updated Title");

        User user = User.builder().userName("testuser").entries(List.of(new entry())).build();
        when(userService.findByName("testuser")).thenReturn(user);
        when(entryService.findById(id)).thenReturn(Optional.of(new entry()));

        mockMvc.perform(put("/entry/id/{id}", id.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatedEntry))
                        .with(csrf()))
                .andExpect(status().isOk());
    }
}
