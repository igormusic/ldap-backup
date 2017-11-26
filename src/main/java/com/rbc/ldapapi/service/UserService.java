package com.rbc.ldapapi.service;

import com.rbc.ldapapi.model.User;
import org.apache.directory.api.ldap.model.exception.LdapException;
import org.apache.directory.ldap.client.api.LdapNetworkConnection;

import java.util.UUID;

public interface UserService {
    User create(User user) throws LdapException;
    void updatePassword(UUID userId, String newPassword) throws LdapException;
    Boolean isValidPassword(UUID userId, String password);
    LdapNetworkConnection connection() throws LdapException;


}
