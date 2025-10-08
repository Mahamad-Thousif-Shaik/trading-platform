package com.thousif.trading.dto.response;

import com.thousif.trading.enums.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Response {

    private String token;
    private String type = "Bearer";
    private String username;
    private String email;
    private Set<Role> roles;
    private String message;

}
