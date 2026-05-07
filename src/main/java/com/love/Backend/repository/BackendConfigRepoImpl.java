package com.love.Backend.repository;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.MongoTemplate;

import com.love.Backend.entity.BackendConfigEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;


@Repository
public class BackendConfigRepoImpl implements BackendConfigCustomRepo{

    @Autowired
    private MongoTemplate mongoTemplate;

    @Override
    public String getConfigValue(String key){
        Query query = new Query();

        query.addCriteria(Criteria.where("key").is(key));
        BackendConfigEntity config = mongoTemplate.findOne(query, BackendConfigEntity.class);

        return config !=null ? config.getValue() : null;
    }
}
