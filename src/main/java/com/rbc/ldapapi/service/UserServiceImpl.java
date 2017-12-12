package com.rbc.ldapapi.service;

import com.rbc.ldapapi.configuration.LdapApiConfiguration;
import com.rbc.ldapapi.model.User;
import org.apache.directory.api.ldap.model.cursor.CursorException;
import org.apache.directory.api.ldap.model.cursor.EntryCursor;
import org.apache.directory.api.ldap.model.entry.*;
import org.apache.directory.api.ldap.model.exception.LdapException;
import org.apache.directory.api.ldap.model.exception.LdapInvalidAttributeValueException;
import org.apache.directory.api.ldap.model.ldif.LdifEntry;
import org.apache.directory.api.ldap.model.ldif.LdifReader;
import org.apache.directory.api.ldap.model.ldif.LdifUtils;
import org.apache.directory.api.ldap.model.message.*;
import org.apache.directory.api.ldap.model.message.controls.ManageDsaITImpl;
import org.apache.directory.api.ldap.model.name.Dn;
import org.apache.directory.ldap.client.api.LdapNetworkConnection;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
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

    @Override
    public void exportTree(Writer writer) throws CursorException, LdapException, IOException {
        EntryCursor cursor = connection().search(new Dn(configuration.ldapUsersOU), "(ObjectClass=*)", SearchScope.SUBTREE, "*", "+");

        StringBuilder st = new StringBuilder();

        Integer count = 0;


        while (cursor.next()) {
            if(count % 1000==0){
                System.out.println(String.format("[%s] exported %d users", LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss.SSS")), count));
            }
            Entry entry = cursor.get();
            String ss = LdifUtils.convertToLdif(entry);
            writer.write(ss + "\n");
            count++;
        }

        writer.close();
    }

    @Override
    public void importTree(InputStream is, String[] attributeNames ){

        LdifReader entries = null;
        try {
            entries = new LdifReader(is);
        } catch (LdapException e) {
            e.printStackTrace();
        }

        Integer count=0;

        for (LdifEntry ldifEntry : entries) {
            Entry entry = ldifEntry.getEntry();

            ModifyRequest modRequrest = new ModifyRequestImpl();
            modRequrest.setName(entry.getDn());

            for (String attributeName: attributeNames) {
                Attribute attribute = entry.get(attributeName);

                try {
                    if(attribute.isHumanReadable()) {
                        modRequrest.replace(attributeName, attribute.getString());
                    }
                    else
                    {
                        modRequrest.replace(attributeName, attribute.getBytes());
                    }
                } catch (LdapInvalidAttributeValueException e) {
                    e.printStackTrace();
                }
            }

            try {
                ModifyResponse res = connection().modify(modRequrest);
                ModifyResponse res2 = res;
            } catch (LdapException e) {
                e.printStackTrace();
            }

            if(count % 100==0){
                System.out.println(String.format("[%s] imported %d users", LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss.SSS")), count));
            }

            count++;
        }
    }

    @Override
    public void updateTree(InputStream is, Map<String, String> updatedValues) {
        LdifReader entries = null;
        try {
            entries = new LdifReader(is);
        } catch (LdapException e) {
            e.printStackTrace();
        }

        Integer count = 0;

        for (LdifEntry ldifEntry : entries) {
            Entry entry = ldifEntry.getEntry();

            ModifyRequest modRequrest = new ModifyRequestImpl();
            modRequrest.setName(entry.getDn());

            for (Map.Entry<String,String> updateEntry: updatedValues.entrySet()) {
                modRequrest.replace(updateEntry.getKey(), updateEntry.getValue());
            }

            try {
                ModifyResponse res = connection().modify(modRequrest);

                ModifyResponse mr2 = res;

            } catch (LdapException e) {
                e.printStackTrace();
            }

            if(count % 100==0){
                System.out.println(String.format("[%s] updated %d users", LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss.SSS")), count));
            }

            count++;
        }
    }
}
