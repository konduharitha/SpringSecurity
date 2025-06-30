package com.app.SpringSecEx.service;

import com.app.SpringSecEx.model.UserPrincipal;
import com.app.SpringSecEx.model.Users;
import com.app.SpringSecEx.repo.UserRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class MyUserServiceDetails implements UserDetailsService {

    @Autowired
    private UserRepo repo;
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        Users users = repo.findByUsername(username);
            if(users == null){
                System.out.println("User not found");
                throw new UsernameNotFoundException("User not found");
            }
                return  new UserPrincipal(users);
    }
}
