package com.love.Backend.dto.response;


import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class UserResponseDTO {

    private String userName;
    private String email;
    private List<String> roles = new ArrayList<>();
}