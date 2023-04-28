package se.distansakademin;

import software.amazon.awssdk.core.CredentialType;
import software.amazon.awssdk.services.cognitoidentityprovider.CognitoIdentityProviderClient;
import software.amazon.awssdk.services.cognitoidentityprovider.model.StopUserImportJobRequest;
import software.amazon.awssdk.services.cognitoidentityprovider.model.VerifySoftwareTokenRequest;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Main {

    private static CognitoIdentityProviderClient cognitoClient;
    private static String clientId, userPoolId, clientName;

    private static Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) {
        System.out.println("Welcome to this login experience");

        setupPrepareUserPool();

        // var linus01 = new User("linus_01", "*pifKFoW867h");
        // var accessToken = login(linus01);

        AmazonCognito.listAllUsers(cognitoClient, userPoolId);
        registerOrLogin();

        // var user = new User("linus_03", "*pifKFoW867h");
        // login(user);

        System.out.println("Done!");
    }

    // Register or login as user
    private static void registerOrLogin() {
        System.out.print("Select (l)ogin or (r)egister: ");
        var input = scanner.nextLine();

        System.out.println(input);

        User user = getUser();

        if (input.equals("l")) {
            login(user);
        } else if (input.equals("r")) {
            signUp(user);
            confirmUser(user);
        }
    }

    // Get user from input
    private static User getUser() {
        System.out.print("Enter username: ");
        var username = scanner.nextLine();

        System.out.print("Enter password: ");
        var password = scanner.nextLine();

        return new User(username, password);
    }


    // Setup & preparation
    private static void setupPrepareUserPool() {
        cognitoClient = AmazonCognito.getCognitoClient();
        userPoolId = "eu-north-1_cKirOZUz6"; // AmazonCognito.createPool(cognitoClient, "230428_cognito-user-pool");
        clientName = "amazon_cognito_client";
        clientId = "59pqfci3ajok4nguuafmr85gk8"; // AmazonCognito.createPoolClient(cognitoClient, clientName, userPoolId);
    }

    // Sign up new user
    private static void signUp(User user) {
        var username = user.getUsername();
        var password = user.getPassword();
        var email = user.getEmail();

        AmazonCognito.signUp(cognitoClient, clientId, username, password, email);
    }

    // Confirm signed up user
    private static void confirmUser(User user) {
        var username = user.getUsername();
        AmazonCognito.confirmUser(cognitoClient, username, userPoolId);
    }

    // Login as user
    private static String login(User user) {
        var username = user.getUsername();
        var password = user.getPassword();

        var loginResponse = AmazonCognito.initiateAuth(cognitoClient, clientId, username, password, userPoolId);

        var token = "";

        try {
            token = loginResponse.authenticationResult().accessToken();
            System.out.println("Login success!");
        } catch (NullPointerException e) {
            System.err.println(e.getMessage());
        }

        return token;
    }

    // Create user as admin
    private static void createUser(User user) {
        var username = user.getUsername();
        var password = user.getPassword();
        var email = user.getEmail();

        AmazonCognito.createNewUser(cognitoClient, userPoolId, username, email, password);
    }

}