package com.rbc.ldapapi;

import com.rbc.ldapapi.configuration.LdapApiConfiguration;
import com.rbc.ldapapi.model.User;
import com.rbc.ldapapi.service.UserService;
import org.apache.directory.api.ldap.model.exception.LdapException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.UUID;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

@RunWith(SpringRunner.class)
@SpringBootTest
public class UserServiceTest {

    @Autowired
    UserService service;

    @Autowired
    LdapApiConfiguration configuration;



    @Test
    public void createUser() throws LdapException {

        User user = User.Create(UUID.randomUUID(), "Joe", "Bloggs", "itIsASecret");

        service.create( user);

        String dn = user.getDN(configuration.ldapUsersOU);

        assertThat(service.connection().exists( dn), equalTo(true));

        Boolean isValidPassword = service.isValidPassword(user.getUserId(), user.getPassword());

        assertThat(isValidPassword, equalTo(true));

        isValidPassword = service.isValidPassword(user.getUserId(), "newpassword");

        assertThat(isValidPassword, equalTo(false));

        service.updatePassword(user.getUserId(),"newpassword");

        isValidPassword = service.isValidPassword(user.getUserId(), "newpassword");

        assertThat(isValidPassword, equalTo(true));

        //service.connection().delete(dn);

    }
}
