package com.ecommerce.project.service;

import com.ecommerce.project.model.User;
import com.ecommerce.project.security.request.SignupRequest;

public interface UserService {
    User register(SignupRequest signupRequest);
}
