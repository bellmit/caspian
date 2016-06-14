package com.emc.caspian.ccs.account.authorization;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.RetentionPolicy.RUNTIME;
import static java.lang.annotation.ElementType.METHOD;

@Documented
@Retention(RUNTIME)
@Target(METHOD)
public @interface AuthorizationPolicy {

  // Supported authorization rules
  public enum Rule {
    DENY_ALL,
    ALLOW_ALL,
    DISALLOW_EDIT_DEFAULT_ACCOUNT,
    ALLOW_CLOUD_ADMIN,          // cloud admin - admin role in default domain
    ALLOW_CLOUD_MONITOR,        // cloud monitor - monitor role in default domain
    ALLOW_CLOUD_SERVICE,        // cloud service - service role in default domain
    ALLOW_ACCOUNT_ADMIN,        // account admin - admin role in primary domain of the account
    ALLOW_ACCOUNT_MONITOR,      // account monitor - monitor role in primary domain of the account
    ALLOW_SCOPED_USER,          // user with any domain or project scoped roles
    ALLOW_ACCOUNT_ADMIN_IDP,    // reserved to be used only with IDP related APIs to provide account admin authorization
    ALLOW_ACCOUNT_MONITOR_IDP,  // reserved to be used only with IDP related APIs to provide account monitor authorization
    ALLOW_DOMAIN_ADMIN,         // domain admin - admin role in any domain
    ALLOW_DOMAIN_MONITOR        // doamin monitor - monitor role in any domain    
  };

  Rule[] value();
}