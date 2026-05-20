package com.love.Backend.service;

import com.love.Backend.dto.request.EntryRequest;
import com.love.Backend.entity.User;
import com.love.Backend.entity.entry;
import com.love.Backend.enums.Sentiment;
import com.love.Backend.repository.BackendEntryRepo;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@ActiveProfiles("test")
@ExtendWith(MockitoExtension.class)
public class BackendEntryServiceTest {

    @Mock
    private BackendEntryRepo entryRepo;

    @Mock
    private UserEntryService userService;

    @InjectMocks
    private BackendEntryService entryService;

    @Test
    public void testSaveNewEntry() {
        entry newEntry = new entry();
        newEntry.setTitle("Test");
        newEntry.setContent("Sample content");
        newEntry.setSentiment(Sentiment.HAPPY);

        User user = User.builder()
                .userName("testuser")
                .password("testPasword123")
                .build();

        when(userService.findByName("testuser"))
                .thenReturn(user);

        EntryRequest request = new EntryRequest();
        request.setTitle(newEntry.getTitle());
        request.setContent(newEntry.getContent());
        request.setSentiment(newEntry.getSentiment());

        entryService.createEntry(request, "testuser");
    }

    @Test
    public void testFindByTitle() {
        List<entry> entries = List.of(new entry());
        when(entryRepo.findByTitle(anyString())).thenReturn(entries);

        List<entry> result = entryService.findByTitle("testuser");

        assertEquals(entries, result);
    }

    @Test
    public void testFindByContent() {
        List<entry> entries = List.of(new entry());
        when(entryRepo.findByContent(anyString())).thenReturn(entries);

        List<entry> result = entryService.findByContent("content");

        assertEquals(entries, result);
    }

    @Test
    public void testDeleteById_Success() {
        ObjectId id = new ObjectId();
        entry entry = new entry();
        entry.setId(id);
        User user = User.builder().userName("testuser").entries(List.of(entry)).build();

        when(userService.findByName("testuser")).thenReturn(user);
        // entryService.deleteById relies on user's entries list and backendEntryRepo.deleteById,
        // it does not call findById, so avoid unnecessary stubbing.
        boolean result = entryService.deleteById(id, "testuser");

        assertTrue(result);
        verify(entryRepo).deleteById(id);
    }

    @Test
    public void testDeleteById_NotFound() {
        ObjectId id = new ObjectId();
        User user = User.builder().userName("testuser").entries(List.of()).build();

        when(userService.findByName("testuser")).thenReturn(user);

        boolean result = entryService.deleteById(id, "testuser");

        assertFalse(result);
    }

    @Test
    public void testGetEntriesBySentiment() {
        List<entry> entries = List.of(new entry());
        when(entryRepo.findBySentiment(Sentiment.HAPPY)).thenReturn(entries);

        List<entry> result = entryService.getEntriesBySentiment(Sentiment.HAPPY);

        assertEquals(entries, result);
    }
}
