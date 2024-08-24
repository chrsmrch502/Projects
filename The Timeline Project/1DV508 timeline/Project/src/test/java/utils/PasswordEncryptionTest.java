package utils;

import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class PasswordEncryptionTest {
    //Should create random salt and that we can not test, just trust
    // secure random class from Java security package
    @Test
    void getSalt() {
        String salt1 = PasswordEncryption.getSalt(60);
        String salt2 = PasswordEncryption.getSalt(2);
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            PasswordEncryption.getSalt(0);
        });
        String expectedMessage = "Salt length must be > 0 in length";
        String actualMessage = exception.getMessage();

        assertTrue(salt1.matches("^[A-Za-z0-9]{60}$")); //- trust in random from security package, just check if given length is correct and it uses alphabet
        assertTrue(salt2.matches("^[A-Za-z0-9]{2}$"));
        assertTrue(actualMessage.contains(expectedMessage)); //Checks both that exception is thrown and correct message printed
    }

    @Test
    void hash() {
       PasswordEncryption test = new PasswordEncryption();

    }

    @Test //Add same password 1000 times with diff. salt each time - should give 1000 different encrypted passwords
    void generateSecurePassword() {
        Set<String> salts = new HashSet<>(); //to hold 1000 diff salts
        Set<String> passwords = new HashSet<>(); //To hold the passwords created with the salts
        while(salts.size()<1000){
            String salt = PasswordEncryption.getSalt(30);
            salts.add(salt);
        }
        String[] saltArray = salts.toArray(new String[0]);
        String password = "#Johnny69";
        for(int i = 0; i<salts.size(); i++){
            String uniquePass = PasswordEncryption.generateSecurePassword(password,saltArray[i]);
            passwords.add(uniquePass);
        }
        assertEquals(1000, passwords.size());

    }

    @Test
    void verifyUserPassword() {
        String salt1= PasswordEncryption.getSalt(30);
        String salt2 = PasswordEncryption.getSalt(30);
        String password1 = "#Johnny69";
        String password2 = "#SanchoPancho69";
        String encrp1 = PasswordEncryption.generateSecurePassword(password1,salt1);
        //check wrong password with wrong salt
        assertFalse(PasswordEncryption.verifyUserPassword(password2,encrp1,salt2));
        //Check wrong password with right salt
        assertFalse(PasswordEncryption.verifyUserPassword(password2,encrp1,salt1));
        //check right password with wrong salt
        assertFalse(PasswordEncryption.verifyUserPassword(password1,encrp1,salt2));
        //check right password with right salt
        assertTrue(PasswordEncryption.verifyUserPassword(password1,encrp1,salt1));
    }
}