package com.da.authservice.mapper;

import com.da.authservice.domain.User;
import com.da.authservice.dto.AccountPayload;
import com.da.authservice.dto.AccountView;
import com.da.authservice.dto.SignUpRequest;

public interface UserMapper {

  public User accountPayloadToUser(AccountPayload accountPayload);

  public User signUpRequestToUser(SignUpRequest signUpRequest);

  public AccountView userToAccountView(User user);
}
