package com.love.Backend.controller;

import com.love.Backend.dto.request.EntryRequest;
import com.love.Backend.dto.request.EntryUpdateRequest;
import com.love.Backend.dto.response.EntryResponse;
import com.love.Backend.dto.response.UserResponseDTO;
import com.love.Backend.entity.User;
import com.love.Backend.entity.entry;
import com.love.Backend.enums.Sentiment;
import com.love.Backend.exception.ResourceNotFoundException;
import com.love.Backend.service.AuditLogService;
import com.love.Backend.service.BackendEntryService;
import com.love.Backend.service.UserEntryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Tag(name = "Entry Management APIs", description = "APIs for managing authenticated user's journal entries")
@RestController
@RequestMapping("/entry")
public class BackendController {

    @Autowired
    private BackendEntryService entryService;

    @Autowired
    private UserEntryService userService;

    @Autowired
    private AuditLogService auditLogService;

    @Operation(summary = "Create new entry", description = "Create a new entry for the authenticated user")
    @ApiResponses(value = {@ApiResponse(responseCode = "201", description = "Entry created successfully"), @ApiResponse(responseCode = "400", description = "Bad request")})
    @PostMapping
    public ResponseEntity<EntryResponse> createEntry(@Valid @RequestBody EntryRequest request) {
        String userName = currentUserName();
        EntryResponse response = entryService.createEntry(request, userName);
        auditLogService.record(userName, "CREATE_ENTRY", "Entry", response.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(summary = "Get current user's entries", description = "Retrieve all entries of the authenticated user")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "Entries found"), @ApiResponse(responseCode = "404", description = "No entries found")})
    @GetMapping
    public ResponseEntity<List<EntryResponse>> getAllEntriesOfUser() {
        String userName = currentUserName();
        List<EntryResponse> entries = entryService.getAllEntriesOfUser(userName);
        if (entries.isEmpty()) {
            throw new ResourceNotFoundException("No entries found");
        }
        auditLogService.record(userName, "VIEW_ALL_ENTRIES", "Entry", "");
        return ResponseEntity.ok(entries);
    }

    @Operation(summary = "Search current user's entries", description = "Filter entries by sentiment, keyword, date range, pagination and sorting")
    @GetMapping("/search")
    public ResponseEntity<Page<EntryResponse>> searchEntries(
            @RequestParam(required = false) Sentiment sentiment,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to,
            @PageableDefault(size = 10, sort = "date") Pageable pageable) {
        String userName = currentUserName();
        Page<EntryResponse> entries = entryService.searchEntries(userName, sentiment, keyword, from, to, pageable);
        auditLogService.record(userName, "SEARCH_ENTRIES", "Entry", "sentiment=" + sentiment + ",keyword=" + keyword);
        return ResponseEntity.ok(entries);
    }

    @Operation(summary = "Get entries by user ID", description = "Retrieve entries for a specific user ID only if it belongs to authenticated user")
    @Parameter(name = "id", description = "User ID", required = true)
    @GetMapping("{id}")
    public ResponseEntity<List<entry>> getEntriesByUserId(@PathVariable ObjectId id) {
        String userName = currentUserName();
        User loggedUser = userService.findByName(userName);
        if (loggedUser == null || !loggedUser.getId().equals(id)) {
            auditLogService.record(userName, "UNAUTHORIZED_ACCESS", "Entry", id.toHexString());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        User targetUser = userService.findById(id).orElseThrow(() -> new ResourceNotFoundException("User not found"));
        auditLogService.record(userName, "VIEW_USER_ENTRIES", "Entry", id.toHexString());
        return ResponseEntity.ok(targetUser.getEntries());
    }

    @Operation(summary = "Delete entry by ID", description = "Delete an entry by ID for the authenticated user")
    @Parameter(name = "id", description = "Entry ID", required = true)
    @DeleteMapping("id/{id}")
    public ResponseEntity<Void> deleteUserById(@PathVariable ObjectId id) {
        String userName = currentUserName();
        boolean removed = entryService.deleteById(id, userName);
        if (!removed) {
            throw new ResourceNotFoundException("Entry not found");
        }
        auditLogService.record(userName, "DELETE_ENTRY", "Entry", id.toHexString());
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Update entry by ID", description = "Partially update an entry by ID for the authenticated user")
    @Parameter(name = "id", description = "Entry ID", required = true)
    @PatchMapping("id/{id}")
    public ResponseEntity<EntryResponse> patchById(@PathVariable ObjectId id, @Valid @RequestBody EntryUpdateRequest request) {
        String userName = currentUserName();
        EntryResponse response = entryService.updateEntry(id, request, userName);
        auditLogService.record(userName, "PATCH_ENTRY", "Entry", id.toHexString());
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Replace entry by ID", description = "Update an entry by ID for the authenticated user")
    @Parameter(name = "id", description = "Entry ID", required = true)
    @PutMapping("id/{id}")
    public ResponseEntity<EntryResponse> putById(@PathVariable ObjectId id, @Valid @RequestBody EntryUpdateRequest request) {
        String userName = currentUserName();
        EntryResponse response = entryService.updateEntry(id, request, userName);
        auditLogService.record(userName, "UPDATE_ENTRY", "Entry", id.toHexString());
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Upload entry attachment", description = "Upload or replace an attachment for the authenticated user's entry")
    @PostMapping(value = "id/{id}/attachment", consumes = "multipart/form-data")
    public ResponseEntity<EntryResponse> uploadAttachment(@PathVariable ObjectId id, @RequestPart("file") MultipartFile file) {
        String userName = currentUserName();
        EntryResponse response = entryService.attachFile(id, file, userName);
        auditLogService.record(userName, "UPLOAD_ATTACHMENT", "Entry", id.toHexString() + " - " + file.getOriginalFilename());
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Get all users", description = "Retrieve all users in the system")
    @ApiResponse(responseCode = "200", description = "Users retrieved successfully")
    @GetMapping("/all")
    public List<UserResponseDTO> getAll() {
        String userName = currentUserName();
        auditLogService.record(userName, "VIEW_ALL_USERS", "User", "");
        List<User> users = userService.getAll();
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

    @Operation(summary = "Get all entries paginated", description = "Retrieve all entries with pagination support")
    @ApiResponse(responseCode = "200", description = "Entries retrieved successfully")
    @GetMapping("/paginated")
    public ResponseEntity<Page<entry>> getAllEntriesPaginated(@PageableDefault(size = 10) Pageable pageable) {
        String userName = currentUserName();
        Page<entry> entries = entryService.getAllEntriesPaginated(pageable);
        auditLogService.record(userName, "VIEW_PAGINATED_ENTRIES", "Entry", "page=" + pageable.getPageNumber());
        return ResponseEntity.ok(entries);
    }

    @Operation(summary = "Get entries by title", description = "Legacy exact title search endpoint")
    @GetMapping("title/{title}")
    public ResponseEntity<List<entry>> getByName(@PathVariable String title) {
        String userName = currentUserName();
        List<entry> entries = entryService.findByTitle(title);
        if (entries.isEmpty()) {
            throw new ResourceNotFoundException("Entry not found");
        }
        auditLogService.record(userName, "SEARCH_BY_TITLE", "Entry", title);
        return ResponseEntity.ok(entries);
    }

    @Operation(summary = "Get entries by content", description = "Legacy exact content search endpoint")
    @GetMapping("content/{content}")
    public ResponseEntity<List<entry>> getByContent(@PathVariable String content) {
        String userName = currentUserName();
        List<entry> entries = entryService.findByContent(content);
        if (entries.isEmpty()) {
            throw new ResourceNotFoundException("Entry not found");
        }
        auditLogService.record(userName, "SEARCH_BY_CONTENT", "Entry", content);
        return ResponseEntity.ok(entries);
    }

    @Operation(summary = "Health-style entry controller message", description = "Small helper response for controller sanity checks")
    @GetMapping("/message")
    public Map<String, String> message() {
        return Map.of("message", "Entry controller is working");
    }

    public String currentUserName() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication.getName();
    }
}
