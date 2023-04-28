package se.distansakademin;

import jdk.javadoc.doclet.Reporter;
import software.amazon.awssdk.auth.credentials.ProfileCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.cognitoidentityprovider.CognitoIdentityProviderClient;
import software.amazon.awssdk.services.cognitoidentityprovider.model.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AmazonCognito {

    // Builds and returns a Cognito Client object
    public static CognitoIdentityProviderClient getCognitoClient() {
        var credentialsProvider = ProfileCredentialsProvider.create();

        var cognitoClient = CognitoIdentityProviderClient.builder()
                .region(Region.EU_NORTH_1)
                .credentialsProvider(credentialsProvider)
                .build();

        return cognitoClient;
    }

    // Create user pool in AWS Cognito
    public static String createPool(CognitoIdentityProviderClient cognitoClient, String userPoolName) {

        try {
            var request = CreateUserPoolRequest.builder()
                    .poolName(userPoolName)
                    .build();

            CreateUserPoolResponse response = cognitoClient.createUserPool(request);

            return response.userPool().id();

        } catch (CognitoIdentityProviderException e) {
            System.err.println(e.awsErrorDetails().errorMessage());
            System.exit(1);
        }
        return "";
    }

    // Create user pool client in AWS Cognito
    public static String createPoolClient(CognitoIdentityProviderClient cognitoClient, String clientName, String userPoolId) {
        try {
            var authFlowTypes = new ExplicitAuthFlowsType[]{
                    ExplicitAuthFlowsType.ALLOW_REFRESH_TOKEN_AUTH,
                    ExplicitAuthFlowsType.ALLOW_CUSTOM_AUTH,
                    ExplicitAuthFlowsType.ALLOW_USER_SRP_AUTH,
                    ExplicitAuthFlowsType.ALLOW_ADMIN_USER_PASSWORD_AUTH,
                    ExplicitAuthFlowsType.ALLOW_USER_PASSWORD_AUTH
            };

            var request = CreateUserPoolClientRequest.builder()
                    .clientName(clientName)
                    .userPoolId(userPoolId)
                    .explicitAuthFlows(authFlowTypes)
                    .build();

            CreateUserPoolClientResponse response = cognitoClient.createUserPoolClient(request);

            return response.userPoolClient().clientId();

        } catch (CognitoIdentityProviderException e) {
            System.err.println(e.awsErrorDetails().errorMessage());
            System.exit(1);
        }

        return "";
    }


    // Register a new user
    public static void signUp(CognitoIdentityProviderClient identityProviderClient, String clientId, String username, String password, String email) {
        AttributeType userAttrs = AttributeType.builder()
                .name("email")
                .value(email)
                .build();

        List<AttributeType> userAttrsList = new ArrayList<>();
        userAttrsList.add(userAttrs);

        try {
            SignUpRequest signUpRequest = SignUpRequest.builder()
                    .userAttributes(userAttrsList)
                    .username(username)
                    .clientId(clientId)
                    .password(password)
                    .build();

            identityProviderClient.signUp(signUpRequest);

            System.out.println("User has been signed up ");

        } catch (CognitoIdentityProviderException e) {
            System.err.println(e.awsErrorDetails().errorMessage());
            System.exit(1);
        }
    }


    // Confirm registered user
    public static AdminConfirmSignUpResponse confirmUser(CognitoIdentityProviderClient cognitoClient, String username, String userPoolId) {
        try {
            var request = AdminConfirmSignUpRequest.builder()
                    .username(username)
                    .userPoolId(userPoolId)
                    .build();

            var response = cognitoClient.adminConfirmSignUp(request);

            System.out.println("Response: " + response);

            return response;
        } catch (CognitoIdentityProviderException e) {
            System.err.println(e.awsErrorDetails().errorMessage());
            System.exit(1);
        }

        return null;
    }


    // Log in with user
    public static AdminInitiateAuthResponse initiateAuth(CognitoIdentityProviderClient identityProviderClient, String clientId, String username, String password, String userPoolId) {
        try {
            Map<String, String> authParameters = new HashMap<>();
            authParameters.put("USERNAME", username);
            authParameters.put("PASSWORD", password);

            var request = AdminInitiateAuthRequest.builder()
                    .clientId(clientId)
                    .userPoolId(userPoolId)
                    .authParameters(authParameters)
                    .authFlow(AuthFlowType.ADMIN_USER_PASSWORD_AUTH)
                    .build();

            var response = identityProviderClient.adminInitiateAuth(request);

            System.out.println("Result Challenge is : " + response.challengeName());

            return response;

        } catch (CognitoIdentityProviderException e) {
            System.err.println(e.awsErrorDetails().errorMessage());
            System.exit(1);
        }

        return null;
    }

    // Create new user as admin, forces change of password
    public static void createNewUser(CognitoIdentityProviderClient cognitoClient, String userPoolId, String username, String email, String password){
        try{

            AttributeType userAttrs = AttributeType.builder()
                    .name("email")
                    .value(email)
                    .build();

            AdminCreateUserRequest userRequest = AdminCreateUserRequest.builder()
                    .userPoolId(userPoolId)
                    .username(username)
                    .temporaryPassword(password)
                    .userAttributes(userAttrs)
                    .messageAction("SUPPRESS")
                    .build() ;

            AdminCreateUserResponse response = cognitoClient.adminCreateUser(userRequest);

            System.out.println("User " + response.user().username() + "is created. Status: " + response.user().userStatus());

        } catch (CognitoIdentityProviderException e){
            System.err.println(e.awsErrorDetails().errorMessage());
            System.exit(1);
        }
    }


    // Lists all users + status
    public static void listAllUsers(CognitoIdentityProviderClient cognitoClient, String userPoolId ) {
        try {
            ListUsersRequest usersRequest = ListUsersRequest.builder()
                    .userPoolId(userPoolId)
                    .build();

            ListUsersResponse response = cognitoClient.listUsers(usersRequest);
            response.users().forEach(user -> {
                System.out.println(user.username() + " | status: " + user.userStatus());
            });

        } catch (CognitoIdentityProviderException e){
            System.err.println(e.awsErrorDetails().errorMessage());
            System.exit(1);
        }
    }



}
