package com.love.Backend.entity;


import com.love.Backend.enums.Sentiment;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;


import java.time.LocalDateTime;
@Getter
@Setter
@Document(collection = "entries")
@Schema(description = "Entry entity representing a user entry or journal entry")
public class entry {
    @Id
    @Schema(description = "Unique identifier for the entry", example = "507f1f77bcf86cd799439011")
    private ObjectId id;
    @NonNull
    @Schema(description = "Title of the entry", example = "My Day")
    private String title;
    @Schema(description = "Content of the entry", example = "Today was a great day...")
    private String content;
    @Schema(description = "Date and time when the entry was created", example = "2023-10-01T12:00:00")
    private LocalDateTime date;
    @Schema(description = "Sentiment associated with the entry", example = "HAPPY")
    private Sentiment sentiment;
    @Schema(description = "Optional attachment URL/path for this entry", example = "/uploads/file.png")
    private String attachmentUrl;



}
