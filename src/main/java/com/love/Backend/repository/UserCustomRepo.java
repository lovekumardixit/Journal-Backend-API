package com.love.Backend.repository;

import com.love.Backend.entity.User;
import com.love.Backend.entity.entry;
import com.love.Backend.enums.Sentiment;

import java.util.List;

public interface UserCustomRepo {

    List<User> findUsersByAge(int age);
    List<entry> findBySentiment(Sentiment sentiment);
}
