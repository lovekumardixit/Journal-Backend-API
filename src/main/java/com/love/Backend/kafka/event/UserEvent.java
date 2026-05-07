package com.love.Backend.kafka.event;


import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.bson.types.ObjectId;

@Data
@Getter
@Setter
public class UserEvent {

    private ObjectId id;
    private String userName;
    private String email;
}
