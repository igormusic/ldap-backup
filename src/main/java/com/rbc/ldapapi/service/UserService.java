package com.rbc.ldapapi.service;

import com.rbc.ldapapi.model.User;
import org.apache.directory.api.ldap.model.cursor.CursorException;
import org.apache.directory.api.ldap.model.exception.LdapException;
import org.apache.directory.ldap.client.api.LdapNetworkConnection;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Writer;
import java.util.Map;
import java.util.UUID;

public interface UserService {
    User create(User user) throws LdapException;
    void updatePassword(UUID userId, String newPassword) throws LdapException;
    Boolean isValidPassword(UUID userId, String password);
    LdapNetworkConnection connection() throws LdapException;
    void exportTree(Writer writer) throws CursorException, LdapException, IOException;
    void importTree(InputStream is, String[] attributeNames );
    void updateTree(InputStream is, Map<String,String> updatedValues);
}
