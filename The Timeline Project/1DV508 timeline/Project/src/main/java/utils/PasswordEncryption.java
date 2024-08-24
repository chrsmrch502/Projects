package utils;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.util.Base64;
import java.util.Random;
//Information on cryptographic passwords : https://tools.ietf.org/html/rfc2898

//Code used from here:
// https://howtodoinjava.com/security/how-to-generate-secure-password-hash-md5-sha-pbkdf2-bcrypt-examples
// http://www.appsdeveloperblog.com/encrypt-user-password-example-java/
public class PasswordEncryption {

    private static final Random SECURE = new SecureRandom();//cryptographically strong random number generator - SecureRandom must produce non-deterministic output.
    private static final String ALPHABET = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";


    //Method to create random salt that is x char long.
    // Uses secure random class from Java security library to ensure secure, unpredictable randomness
    public static String getSalt(int length) throws IllegalArgumentException {
        if (length < 1)
            throw new IllegalArgumentException("Salt length must be > 0 in length");
        StringBuilder returnValue = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            returnValue.append(ALPHABET.charAt(SECURE.nextInt(ALPHABET.length())));
        }
        return new String(returnValue);
    }

    //The hash method t
    public static byte[] hash(char[] password, byte[] salt) {
        //https://docs.oracle.com/javase/9/docs/api/javax/crypto/spec/PBEKeySpec.html
        PBEKeySpec spec = new PBEKeySpec(password, salt, 10000, 512); //Create a 512 bi key by hashing 10000 times, making brute-force slow
        try {
            //https://docs.oracle.com/javase/7/docs/api/javax/crypto/SecretKeyFactory.html
            SecretKeyFactory skf = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA512"); //Use PBKDF2 with SHA 512 algorithm to generate byte version of 512 bit long hash
            return skf.generateSecret(spec).getEncoded(); //SecretKeyObject created form spec and encoded as bits in byte[]
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new AssertionError("Error while hashing a password" + e.getMessage());
        } finally {
            spec.clearPassword(); //clear the char array so the password is not retrieved from java memory
        }
    }

    //The method that creates the actual encrypted password by hashing the password and salt together
    public static String generateSecurePassword(String password, String salt) {
        String returnValue;
        byte[] securePassword = hash(password.toCharArray(), salt.getBytes());
        //Using Base64 byte encoding to return the byte array as string, more info https://en.wikipedia.org/wiki/Base64
        returnValue = Base64.getEncoder().encodeToString(securePassword);//Since using 512 hash, Base64 gives 88 byte long string - less column size needing in the database vs. hex encoding

        return returnValue;
    }

    //Verify takes given password, generates encrypted password with the users salt from the DB and if the outcome equals
    // the encrypted password in the DB - then it is legit
    public static boolean verifyUserPassword(String providedPassword, String securedPassword, String salt) {
        boolean returnValue;

        // Generate New secure password with the same salt
        String newSecurePassword = generateSecurePassword(providedPassword, salt);

        // Check if two passwords are equal
        returnValue = newSecurePassword.equalsIgnoreCase(securedPassword);

        return returnValue;
    }
}
