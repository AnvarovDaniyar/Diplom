package com.da.authservice.mapper;

import com.da.authservice.domain.Role;
import com.da.authservice.domain.User;
import com.da.authservice.dto.AccountPayload;
import com.da.authservice.dto.AccountView;
import com.da.authservice.dto.SignUpRequest;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Component
public class UserMapperImpl implements UserMapper {

  private final PasswordEncoder passwordEncoder;

  @Override
  public User accountPayloadToUser(AccountPayload accountPayload) {
    return User.builder().userId(accountPayload.getUserId())
           .userName(accountPayload.getUserName())
           .password(passwordEncoder.encode(accountPayload.getPassword()))
           .email(accountPayload.getEmail())
           .role(Role.valueOf(accountPayload.getRole())).build();
  }

  @Override
  public User signUpRequestToUser(SignUpRequest signUpRequest) {
    return User.builder().userName(signUpRequest.getUserName())
           .password(passwordEncoder.encode(signUpRequest.getPassword()))
           .email(signUpRequest.getEmail())
           .role(Role.CUSTOMER).build();
  }

  @Override
  public AccountView userToAccountView(User user) {
    return user.getRole().name().equals("CUSTOMER") ?  new AccountView(user.getUserId(), user.getUsername(), user.getEmail(), "")
                                                    :  new AccountView(user.getUserId(), user.getUsername(), user.getEmail(), user.getRole().name());
  }

}
