package com.love.Backend.service;

import com.love.Backend.dto.request.EntryRequest;
import com.love.Backend.dto.request.EntryUpdateRequest;
import com.love.Backend.dto.response.EntryResponse;
import com.love.Backend.dto.response.SentimentSummaryResponse;
import com.love.Backend.entity.User;
import com.love.Backend.entity.entry;
import com.love.Backend.enums.Sentiment;
import com.love.Backend.exception.ForbiddenException;
import com.love.Backend.exception.ResourceNotFoundException;
import com.love.Backend.repository.BackendEntryRepo;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Component 
public class BackendEntryService {

    @Autowired 
    private BackendEntryRepo backendEntryRepo; 

    @Autowired 
    private UserEntryService userService; 

    @Autowired 
    private MongoTemplate mongoTemplate; 

    @Autowired 
    private AuditLogService auditLogService; 

    @Autowired 
    private FileStorageService fileStorageService; 

    @Transactional 
    public EntryResponse createEntry(EntryRequest request, String userName) { 
        User user = getUserOrThrow(userName); 
        entry newEntry = new entry(); 
        newEntry.setTitle(request.getTitle()); 
        newEntry.setContent(request.getContent()); 
        newEntry.setSentiment(request.getSentiment() != null ? request.getSentiment() : Sentiment.ANXIOUS); 
        newEntry.setDate(LocalDateTime.now()); 
        entry saved = backendEntryRepo.save(newEntry); 
        saved = saved != null ? saved : newEntry; 
        ensureEntryList(user); 
        user.getEntries().add(saved); 
        userService.saveUser(user); 
        recordAudit(userName, "CREATE_ENTRY", saved.getId()); 
        return toResponse(saved); 
    }

    @Transactional 
    public boolean deleteById(ObjectId id, String userName) { 
        User user = getUserOrThrow(userName); 
        ensureEntryList(user);
        boolean removed = user.getEntries().removeIf(x -> x.getId().equals(id)); // Here only Entry reference is deleting of user
        if (!removed) {
            return false; 
        }
        userService.saveUser(user); 
        backendEntryRepo.deleteById(id); //Here actual entry has been deleting
        recordAudit(userName, "DELETE_ENTRY", id); 
        return true; 
    }

    public EntryResponse updateEntry(ObjectId id, EntryUpdateRequest request, String userName) { 
        User user = getUserOrThrow(userName); 
        ensureUserOwnsEntry(user, id); 
        entry old = backendEntryRepo.findById(id).orElseThrow(() -> new ResourceNotFoundException("Entry not found")); 
        if (request.getTitle() != null && !request.getTitle().isBlank()) { 
            old.setTitle(request.getTitle()); 
        }
        if (request.getContent() != null && !request.getContent().isBlank()) { 
            old.setContent(request.getContent()); 
        }
        if (request.getSentiment() != null) { 
            old.setSentiment(request.getSentiment()); 
        }
        entry saved = backendEntryRepo.save(old); 
        saved = saved != null ? saved : old; 
        recordAudit(userName, "UPDATE_ENTRY", saved.getId()); 
        return toResponse(saved); 
    }

    public List<EntryResponse> getAllEntriesOfUser(String userName) { 
        User user = getUserOrThrow(userName); 
        ensureEntryList(user); 
        return user.getEntries().stream().map(this::toResponse).toList(); 
    }

    public Page<EntryResponse> searchEntries(String userName, Sentiment sentiment, String keyword, LocalDateTime from, LocalDateTime to, Pageable pageable) { 
        User user = getUserOrThrow(userName); 
        ensureEntryList(user); 
        List<ObjectId> entryIds = user.getEntries().stream().map(entry::getId).toList(); 
        Query query = new Query(); 
        query.addCriteria(Criteria.where("_id").in(entryIds)); 
        if (sentiment != null) { 
            query.addCriteria(Criteria.where("sentiment").is(sentiment)); 
        }
        if (keyword != null && !keyword.isBlank()) { 
            query.addCriteria(new Criteria().orOperator(Criteria.where("title").regex(keyword, "i"), Criteria.where("content").regex(keyword, "i"))); 
        }
        if (from != null || to != null) { 
            Criteria dateCriteria = Criteria.where("date"); 
            if (from != null) { 
                dateCriteria.gte(from); 
            }
            if (to != null) { 
                dateCriteria.lte(to); 
            }
            query.addCriteria(dateCriteria); 
        }
        long total = mongoTemplate.count(query, entry.class); 
        if (pageable.isPaged()) { 
            query.with(pageable); 
        }
        List<EntryResponse> content = mongoTemplate.find(query, entry.class).stream().map(this::toResponse).toList(); 
        return new PageImpl<>(content, pageable, total); 
    }

    public SentimentSummaryResponse getSentimentSummary(String userName, int days) { 
        LocalDateTime from = LocalDateTime.now().minusDays(days); 
        Page<EntryResponse> entries = searchEntries(userName, null, null, from, null, Pageable.unpaged()); 
        Map<Sentiment, Long> counts = new EnumMap<>(Sentiment.class); 
        for (Sentiment sentiment : Sentiment.values()) { 
            long count = entries.getContent().stream().filter(e -> sentiment.equals(e.getSentiment())).count(); 
            counts.put(sentiment, count); 
        }
        return new SentimentSummaryResponse(entries.getTotalElements(), counts); 
    }

    public EntryResponse attachFile(ObjectId id, MultipartFile file, String userName) { 
        User user = getUserOrThrow(userName); 
        ensureUserOwnsEntry(user, id); 
        entry old = backendEntryRepo.findById(id).orElseThrow(() -> new ResourceNotFoundException("Entry not found")); 
        String attachmentUrl = fileStorageService.store(file); 
        old.setAttachmentUrl(attachmentUrl); 
        entry saved = backendEntryRepo.save(old); 
        saved = saved != null ? saved : old; 
        recordAudit(userName, "ATTACH_FILE", saved.getId()); 
        return toResponse(saved); 
    }

    public void saveEntry(entry myEntry) { 
        backendEntryRepo.save(myEntry); 
    }

    public List<entry> getAll() { 
        return backendEntryRepo.findAll(); 
    }

    public Optional<entry> findById(ObjectId id) { 
        return backendEntryRepo.findById(id); 
    }

    public List<entry> findByTitle(String title) { 
        return backendEntryRepo.findByTitle(title); 
    }

    public List<entry> findByContent(String content) { 
        return backendEntryRepo.findByContent(content); 
    }

    public List<entry> getEntriesBySentiment(Sentiment sentiment) { 
        return backendEntryRepo.findBySentiment(sentiment); 
    }

    public Page<entry> getAllEntriesPaginated(Pageable pageable) { 
        return backendEntryRepo.findAll(pageable); 
    }

    private User getUserOrThrow(String userName) { 
        User user = userService.findByName(userName); 
        if (user == null) { 
            throw new ResourceNotFoundException("User not found"); 
        }
        return user; 
    }

    private void ensureUserOwnsEntry(User user, ObjectId id) { 
        ensureEntryList(user); 
        boolean ownsEntry = user.getEntries().stream().anyMatch(x -> x.getId().equals(id)); 
        if (!ownsEntry) { 
            throw new ForbiddenException("You are not allowed to access this entry"); 
        }
    }

    private void ensureEntryList(User user) { 
        if (user.getEntries() == null) { 
            user.setEntries(new ArrayList<>()); 
        } else if (!(user.getEntries() instanceof ArrayList<?>)) { 
            user.setEntries(new ArrayList<>(user.getEntries())); 
        }
    }

    private void recordAudit(String userName, String action, ObjectId id) { 
        if (auditLogService != null && id != null) { 
            auditLogService.record(userName, action, "ENTRY", id.toHexString()); 
        }
    }

    private EntryResponse toResponse(entry saved) { 
        return EntryResponse.builder() 
                .id(saved.getId() != null ? saved.getId().toHexString() : null) 
                .title(saved.getTitle()) 
                .content(saved.getContent()) 
                .date(saved.getDate()) 
                .sentiment(saved.getSentiment()) 
                .attachmentUrl(saved.getAttachmentUrl()) 
                .build(); 
    }
}
