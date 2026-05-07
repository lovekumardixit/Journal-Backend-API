package com.love.Backend.repository;

import com.love.Backend.entity.User;
import com.love.Backend.entity.entry;
import com.love.Backend.enums.Sentiment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

import java.util.List;

public class UserCustomRepoImpl implements UserCustomRepo {


    @Autowired
    private MongoTemplate mongoTemplate;

    @Override
    public List<User> findUsersByAge(int age){
        Query query = new Query();
        query.addCriteria(Criteria.where("age").gt(age));

        return mongoTemplate.find(query, User.class);
    }

    @Override
    public List<entry> findBySentiment(Sentiment sentiment){

        Query query1 = new Query();
        query1.addCriteria(Criteria.where("sentiment").is(sentiment));
        return mongoTemplate.find(query1, entry.class);
    }
}
