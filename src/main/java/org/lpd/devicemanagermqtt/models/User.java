package org.lpd.devicemanagermqtt.models;


import lombok.Data;

import java.util.HashSet;
import java.util.Set;

@Data
public class User {

  private Long id;


  private String username;


  private String email;


  private String password;


  private Set<Role> roles = new HashSet<>();


}
