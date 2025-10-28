package com.ulr.shortner.security.jwt;


import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.*;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import java.io.IOException;
import java.security.Security;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;


@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter { 

    @Autowired
    private JwtUtils jwtTokenProvider;

    @Autowired
    private UserDetailsService userDetailsService;

    //what is dofilter internam ??
    //it is like a middleware
    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws java.io.IOException, ServletException {

        try {
            //Get jwt from request header
            //Validate token 
            //If Valid get uuser details
            //get user name and load user  -> set the authentication in the context
            String jwt = jwtTokenProvider.getJwtFromHeader(request);
            if (jwt != null && jwtTokenProvider.validateToken(jwt)) {
                String username = jwtTokenProvider.getUserNameFromJwtToken(jwt);

                //load the user deatils and set the authentication in the context
                UserDetails userDetails = userDetailsService.loadUserByUsername(username);

                if (userDetails != null) {
                    //set the authentication in the context
                    //Dummy implementation
                    System.out.println("User authenticated: " + userDetails.getUsername());

                    //what is the purpose of this authentication token
                    //is it used to set the authentication in the context
                    UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(
                                    userDetails,
                                    null,
                                    userDetails.getAuthorities()
                            );
                    //set the authentication in the context
                    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    //SecurityContextHolder.getContext().setAuthentication(authentication);
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                }
            }
        } catch (Exception e) {
            // TODO: handle exception
            e.printStackTrace();
        }

        //to continue the request to the next middleware in the filterchain
        filterChain.doFilter(request , response);
    }

}
