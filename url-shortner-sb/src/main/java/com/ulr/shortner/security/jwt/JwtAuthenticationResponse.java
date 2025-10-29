package com.ulr.shortner.security.jwt;

import lombok.AllArgsConstructor;
import lombok.Data;


//dto class representing JWT authentication response

@Data
@AllArgsConstructor
public class JwtAuthenticationResponse {

    private String token ;

}
