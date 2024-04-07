package com.example.Authentication;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;
public class PasswordHashing {

    // Method to generate a random string to be used as a salt
    public static String generateSalt() {
        SecureRandom secureRandom = new SecureRandom();
        byte[] saltBytes = new byte[16]; // You can adjust the length of the salt as needed
        secureRandom.nextBytes(saltBytes);
        return Base64.getEncoder().encodeToString(saltBytes);
    }


    // Method to hash a password with salt using SHA-256
    public static String hashPassword(String password, String salt) {
        try {
            MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
            byte[] saltBytes = Base64.getDecoder().decode(salt); // Convert salt string to byte array
            messageDigest.update(saltBytes);
            byte[] hashedPasswordBytes = messageDigest.digest(password.getBytes());
            return Base64.getEncoder().encodeToString(hashedPasswordBytes);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null;
        }
    }

}
