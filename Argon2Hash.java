import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Base64;

import org.bouncycastle.crypto.generators.Argon2BytesGenerator;
import org.bouncycastle.crypto.params.Argon2Parameters;

public class Argon2Hash {

    private static Integer iterations = 2;
    private static Integer memLimit = 262144;
    private static Integer hashLength = 32;
    private static Integer parallelism = 8;

    private static Base64.Encoder enc64 = Base64.getEncoder();
    private static Base64.Decoder dec64 = Base64.getDecoder();

    private Argon2Parameters.Builder builder;


    public Argon2Hash(String saltIn) {

        byte[] salt = dec64.decode(saltIn);

        this.builder = new Argon2Parameters.Builder(Argon2Parameters.ARGON2_id)
            .withVersion(Argon2Parameters.ARGON2_VERSION_13)
            .withIterations(iterations)
            .withMemoryAsKB(memLimit)
            .withParallelism(parallelism)
            .withSalt(salt); 
    }

    public static String generateSalt() {

        String saltOut;
        SecureRandom secureRandom = new SecureRandom();
        byte[] salt = new byte[16];

        secureRandom.nextBytes(salt);
        saltOut = enc64.encodeToString(salt);
            
        return saltOut;
    }

    public String generateHash(String saltIn, String passwordIn) {

        String hashedPasswordStr;

        byte[] hashedPassword = new byte[hashLength];
        
        Argon2BytesGenerator generate = new Argon2BytesGenerator();

        generate.init(this.builder.build());
        generate.generateBytes(passwordIn.getBytes(StandardCharsets.UTF_8), hashedPassword, 0, hashedPassword.length);
        hashedPasswordStr = enc64.encodeToString(hashedPassword);

        return hashedPasswordStr;

    }

    public Boolean checkHash(String hashedPasswordIn, String passwordIn) {

        byte[] hashedPassword = dec64.decode(hashedPasswordIn);
        byte[] testHash       = new byte[hashLength];

        Argon2BytesGenerator verifier = new Argon2BytesGenerator();

        verifier.init(this.builder.build());
        verifier.generateBytes(passwordIn.getBytes(StandardCharsets.UTF_8), testHash, 0, testHash.length);

        if (Arrays.equals(testHash, hashedPassword) == true) { return true; }
        else { return false; }

    }
}
