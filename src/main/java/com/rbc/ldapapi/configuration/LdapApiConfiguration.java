package com.rbc.ldapapi.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class LdapApiConfiguration {
    @Value("${ldap.server}")
    public String ldapServer;

    @Value("${ldap.port}")
    public Integer ldapPort;

    @Value("${ldap.bind.name}")
    public String ldapBindName;

    @Value("${ldap.bind.credentials}")
    public String ldapBindCredentials;

    @Value("${ldap.users.ou}")
    public String ldapUsersOU;


}

