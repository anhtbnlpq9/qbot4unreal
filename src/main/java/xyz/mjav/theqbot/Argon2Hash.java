package xyz.mjav.theqbot;

import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Base64;

import org.bouncycastle.crypto.generators.Argon2BytesGenerator;
import org.bouncycastle.crypto.params.Argon2Parameters;

public final class Argon2Hash {

    /*
     * Parameters for argon2id
     */
    private static final Integer iterations   = 2; /* Performs 2 iterations */
    private static final Integer memLimit     = 262144;  /* Using 256MB of RAM */
    private static final Integer hashLength   = 32; /* Generated hashes are 32 bytes long */
    private static final Integer parallelism  = 8; /* 8 threads */


    private static final Base64.Encoder enc64 = Base64.getEncoder();
    private static final Base64.Decoder dec64 = Base64.getDecoder();

    private final Argon2Parameters.Builder builder;


    /**
     * Constructor for the class
     * @param saltIn input salt
     */
    public Argon2Hash(String saltIn) {

        byte[] salt = dec64.decode(saltIn);

        this.builder = new Argon2Parameters.Builder(Argon2Parameters.ARGON2_id)
            .withVersion(Argon2Parameters.ARGON2_VERSION_13)
            .withIterations(iterations)
            .withMemoryAsKB(memLimit)
            .withParallelism(parallelism)
            .withSalt(salt);
    }

    /**
     * Generates a random salt (16 bytes)
     * @return 16 bytes random salt
     */
    public static String generateSalt() {

        String saltOut;
        SecureRandom secureRandom = new SecureRandom();
        byte[] salt = new byte[16];

        secureRandom.nextBytes(salt);
        saltOut = enc64.encodeToString(salt);

        return saltOut;
    }

    /**
     * Generates a password hash using the class static parameters and the salt stored at instance creation
     * @param passwordIn input password
     * @return hashed password
     */
    public String generateHash(String passwordIn) {

        String hashedPasswordStr;

        byte[] hashedPassword = new byte[hashLength];

        Argon2BytesGenerator generate = new Argon2BytesGenerator();

        generate.init(this.builder.build());
        generate.generateBytes(passwordIn.getBytes(StandardCharsets.UTF_8), hashedPassword, 0, hashedPassword.length);
        hashedPasswordStr = enc64.encodeToString(hashedPassword);

        return hashedPasswordStr;

    }

    /**
     * Checks if the input password and hashed password match using the class static parameters and the salt stored at instance creation
     * @param hashedPasswordIn input hashed password
     * @param passwordIn input password
     * @return whether the password and hash match or not
     */
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
