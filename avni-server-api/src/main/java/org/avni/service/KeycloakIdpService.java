package org.avni.service;

import org.avni.domain.User;
import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;
import org.keycloak.OAuth2Constants;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.representations.adapters.config.AdapterConfig;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.annotation.PostConstruct;
import javax.persistence.EntityNotFoundException;
import javax.ws.rs.core.Response;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service("KeycloakIdpService")
@ConditionalOnProperty(value = "keycloak.enabled", havingValue = "true")
public class KeycloakIdpService extends IdpServiceImpl {

    public static final String KEYCLOAK_ADMIN_API_CLIENT_ID = "admin-api";
    @Autowired
    private AdapterConfig adapterConfig;

    private Keycloak keycloak;

    private RealmResource realmResource;

    @Value("${avni.connectToKeycloakInDev}")
    private boolean keycloakInDevProperty;

    @Autowired
    public KeycloakIdpService(Boolean isDev) {
        super(isDev);
    }

    @PostConstruct
    public void init() {
        if (!isDev || this.idpInDev()) {
            //Is the appending "/auth" required, we cannot set getAuthServerUrl() property with the auth, as its used in KeycloakAuthService without to get certs
            keycloak = KeycloakBuilder.builder().serverUrl(adapterConfig.getAuthServerUrl())
                    .grantType(OAuth2Constants.CLIENT_CREDENTIALS).realm(adapterConfig.getRealm())
                    .clientId(KEYCLOAK_ADMIN_API_CLIENT_ID)
                    .clientSecret((String) adapterConfig.getCredentials().get("secret"))
                    .resteasyClient(new ResteasyClientBuilder().connectionPoolSize(10).build()).build();
            keycloak.tokenManager().getAccessToken();
            realmResource = keycloak.realm(adapterConfig.getRealm());
            logger.info("Initialized keycloak client");
        }
    }

    @Override
    public void createUser(User user) {
        createUserWithPassword(user, TEMPORARY_PASSWORD);
    }

    @Override
    public void createUserWithPassword(User user, String password) {
        if (isDev && !idpInDev()) {
            logger.info("Skipping keycloak create user in dev mode...");
            return;
        }
        logger.info(String.format("Initiating create keycloak-user request | username '%s' | uuid '%s'", user.getUsername(), user.getUuid()));

        UserRepresentation newUser = getUserRepresentation(user);
        Response response = realmResource.users().create(newUser);
        if(response.getStatusInfo().getFamily() == Response.Status.Family.SUCCESSFUL ||
                response.getStatusInfo().getFamily() == Response.Status.Family.INFORMATIONAL) {
            resetPassword(user, password); //Just set password using same resetPassword method
        }
        logger.info(String.format("created keycloak-user | username '%s'", user.getUsername()));
    }

    @Override
    public void updateUser(User user) {
        if (isDev && !idpInDev()) {
            logger.info("Skipping keycloak update user in dev mode...");
            return;
        }
        logger.info(String.format("Initiating update keycloak-user request | username '%s' | uuid '%s'", user.getUsername(), user.getUuid()));
        UserRepresentation userRep = getUser(user);
        updateUserRepresentation(user, userRep);
        updateThroughUserRepresentation(userRep);
        logger.info(String.format("updated keycloak-user | username '%s'", user.getUsername()));
    }

    @Override
    public void disableUser(User user) {
        enableOrDisableUser(user, false);
    }

    @Override
    public void deleteUser(User user) {
        if (isDev && !idpInDev()) {
            logger.info("Skipping keycloak delete user in dev mode...");
            return;
        }
        logger.info(String.format("Initiating keycloak delete user request | username '%s' | uuid '%s'", user.getUsername(), user.getUuid()));
        UserRepresentation userRep = getUser(user);
        Response response = realmResource.users().delete(userRep.getId());
        if(response.getStatusInfo().getFamily() == Response.Status.Family.SUCCESSFUL ||
                response.getStatusInfo().getFamily() == Response.Status.Family.INFORMATIONAL) {
            logger.info(String.format("delete keycloak-user request | username '%s'", user.getUsername()));
        }
        logger.error(String.format("Failed to delete keycloak-user request | username '%s'", user.getUsername()));
    }

    @Override
    public void enableUser(User user) {
        enableOrDisableUser(user, true);
    }

    @Override
    public void resetPassword(User user, String password) {
        if (isDev && !idpInDev()) {
            logger.info("Skipping keycloak reset password in dev mode...");
            return;
        }
        logger.info(String.format("Initiating reset password keycloak-user request | username '%s' | uuid '%s'", user.getUsername(), user.getUuid()));
        realmResource.users().get(getUser(user).getId()).resetPassword(getCredentialRepresentation(password));
        logger.info(String.format("password reset for keycloak-user | username '%s'", user.getUsername()));
    }

    @Override
    public boolean idpInDev() {
        return isDev && keycloakInDevProperty;
    }

    @Override
    public Boolean existsInIDP(User user) {
        return !realmResource.users().search(user.getUsername(), true).isEmpty();
    }

    private CredentialRepresentation getCredentialRepresentation(String password) {
        CredentialRepresentation cred = new CredentialRepresentation();
        cred.setType(CredentialRepresentation.PASSWORD);
        cred.setTemporary(false);
        if(password == null || !StringUtils.hasLength(password)) {
            password = TEMPORARY_PASSWORD;
            cred.setTemporary(true);
        }
        cred.setValue(password);
        return cred;
    }

    private UserRepresentation getUserRepresentation(User user) {
        UserRepresentation newUser = new UserRepresentation();
        updateUserRepresentation(user, newUser);
        newUser.setEmailVerified(true);
        return newUser;
    }

    private void updateUserRepresentation(User user, UserRepresentation userRep) {
        userRep.setUsername(user.getUsername());
        Map attrs = new HashMap<String, List<String>>();
        attrs.put("phone_number", Arrays.asList(user.getPhoneNumber()));
        attrs.put("phone_number_verified", Arrays.asList("true"));
        attrs.put("custom:userUUID", Arrays.asList(user.getUuid()));
        userRep.setAttributes(attrs);
        userRep.setEmail(user.getEmail());
        userRep.setEnabled(!user.isVoided());
        userRep.setFirstName(user.getName());
    }

    private void enableOrDisableUser(User user, boolean enable) {
        if (isDev && !idpInDev()) {
            logger.info("Skipping keycloak reset password in dev mode...");
            return;
        }
        logger.info(String.format("Initiating enable/disable keycloak-user request | username '%s' | uuid '%s'", user.getUsername(), user.getUuid()));
        UserRepresentation userRep = getUser(user);
        userRep.setEnabled(enable);
        updateThroughUserRepresentation(userRep);
        logger.info(String.format("enabled/disabled keycloak-user request | username '%s' | enabled '%s'", user.getUsername(), enable));
    }

    private void updateThroughUserRepresentation(UserRepresentation userRep) {
        realmResource.users().get(userRep.getId()).update(userRep);
    }

    private UserRepresentation getUser(User user) {
       return realmResource.users().search(user.getUsername(), true).stream()
                .findFirst()
                .orElseThrow(EntityNotFoundException::new);
    }
}