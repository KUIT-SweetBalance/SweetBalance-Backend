package com.sweetbalance.backend.service;

import com.sweetbalance.backend.dto.SignUpDTO;
import com.sweetbalance.backend.entity.Beverage;
import com.sweetbalance.backend.entity.User;

import java.util.List;
import java.util.Optional;

public interface UserService {

    public void join(SignUpDTO signUpDTO);

    public Optional<User> findUserByUserId(Long userId);

    public Optional<User> findUserByUsername(String username);

    public List<Beverage> findBeveragesByUserId(Long userId);
}
