package task1;
import java.security.NoSuchAlgorithmException;

import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;

public class FirstExample{
    public static void main(String... args) throws NoSuchAlgorithmException, NoSuchPaddingException {
       String data = "some data"; 
       String key = "secret key";
       Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
       Encrypt.encrypt(cipher, data, key);
    }
  }