package com.rbc.ldapapi.service;

import com.rbc.ldapapi.configuration.LdapApiConfiguration;
import com.rbc.ldapapi.model.User;
import org.apache.directory.api.ldap.model.entry.DefaultEntry;
import org.apache.directory.api.ldap.model.entry.DefaultModification;
import org.apache.directory.api.ldap.model.entry.Modification;
import org.apache.directory.api.ldap.model.entry.ModificationOperation;
import org.apache.directory.api.ldap.model.exception.LdapException;
import org.apache.directory.ldap.client.api.LdapNetworkConnection;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.UUID;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    protected LdapApiConfiguration configuration;

    private LdapNetworkConnection connection;

    @Override
    public User create(User user) throws LdapException {

        String dn = user.getDN(configuration.ldapUsersOU);


        connection().add(
                new DefaultEntry(
                        dn, // The Dn
                        "ObjectClass: top",
                        "ObjectClass: person",
                        "ObjectClass: organizationalPerson",
                        "ObjectClass: inetOrgPerson",
                        "cn", user.getFirstName(), // Note : there is no ':' when using a variable
                        "sn", user.getLastName() ,
                        "userPassword", user.getPassword(),
                        "uid", user.getUserId().toString()));

        return user;
    }

    public void updatePassword(UUID userId, String newPassword) throws LdapException {
        String dn = User.getDN(userId, configuration.ldapUsersOU);

        Modification updatePassword = new DefaultModification(ModificationOperation.REPLACE_ATTRIBUTE, "userPassword", newPassword);

        connection().modify(dn, updatePassword);

    }

    public Boolean isValidPassword(UUID userId, String password){

        Boolean isValid = true;

        LdapNetworkConnection testConnection = new LdapNetworkConnection( configuration.ldapServer, configuration.ldapPort );

         try{

             testConnection.startTls();

             testConnection.bind(User.getDN(userId, configuration.ldapUsersOU), password);

         } catch (LdapException e) {
             isValid = Boolean.FALSE;
         }

         finally {
             try {
                 testConnection.close();
             } catch (IOException e) {
                 e.printStackTrace();
             }
         }

         return  isValid;
    }

    @Override
    public LdapNetworkConnection connection() throws LdapException {

        if(connection == null){
            connection = new LdapNetworkConnection( configuration.ldapServer, configuration.ldapPort );

            connection.startTls();

            connection.bind(configuration.ldapBindName, configuration.ldapBindCredentials);
        }

        return connection;
    }
}
