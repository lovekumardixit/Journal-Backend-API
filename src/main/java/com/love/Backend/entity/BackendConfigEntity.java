package com.love.Backend.entity;


import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "backend_config")
@Data
@NoArgsConstructor
public class BackendConfigEntity {

    private String key;
    private String value;
}
