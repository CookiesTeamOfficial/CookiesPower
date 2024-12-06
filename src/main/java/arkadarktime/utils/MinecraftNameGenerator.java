package arkadarktime.utils;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Calendar;
import java.util.Map;
import java.util.Random;

/**
 * A username generator that mutates strings into Minecraft usernames.
 *
 * @author ElectroidFilms (Optimized by ArkaDarkTime)
 */
public class MinecraftNameGenerator {

    /**
     * Mutation number constants.
     */
    private static final int MAX_MUTATIONS = 3;
    private static final int MAX_NUMBERS = 4;
    private static final int MAX_YEAR_RANGE = 15;
    private static final int MAX_UNDERSCORES = 2;

    /**
     * String comparison constants.
     */
    private static final String UNDERSCORE = "_";

    /**
     * Verification variables.
     */
    private static final String MOJANG_URL = "https://api.mojang.com/users/profiles/minecraft/";
    private static final String USERNAME_REGEX = "[a-zA-Z0-9_]{1,16}";
    private static final int MAX_USERNAME_LENGTH = 16;

    private final int minNameLength;
    private final Random random = new Random();

    /**
     * Create a new Minecraft name generator.
     *
     * @param minNameLength The minimum characters allowed.
     */
    public MinecraftNameGenerator(int minNameLength) {
        this.minNameLength = minNameLength;
    }

    /**
     * Generate a username based on the seed string provided.
     *
     * @param seed The base for generating the username.
     * @return The new username.
     */
    public String generate(String seed) {
        String username = scrambleNumbers(seed);
        int mutations = random.nextInt(MAX_MUTATIONS) + 1;
        for (int i = 0; i < mutations; i++) {
            int action = random.nextInt(8);
            switch (action) {
                case 0 -> username = addNumbers(username);
                case 1 -> username = addYear(username);
                case 2 -> username = addRandomUnderscores(username);
                case 3 -> username = addLazyUnderscore(username);
                case 4 -> username = addPhoneticReplacements(username);
                case 5 -> username = addRandomCapitalization(username);
                case 6 -> username = addLogicalCapitalization(username);
            }
        }
        return verifyUsername(username);
    }

    /**
     * Ensure that the username is valid.
     *
     * @param username The username to verify.
     * @return The verified username.
     */
    private String verifyUsername(String username) {
        if (username.length() > MAX_USERNAME_LENGTH) {
            return verifyUsername(username.substring(0, MAX_USERNAME_LENGTH));
        } else if (username.length() < minNameLength) {
            return verifyUsername(randomLetter() + username);
        } else if (!username.matches(USERNAME_REGEX)) {
            return verifyUsername(username.replaceAll("[^a-zA-Z0-9_]", ""));
        } else if (doesAlreadyExist(username)) {
            return verifyUsername(randomLetter() + username.substring(1));
        }
        return username;
    }

    /**
     * Verify that a username is not registered in Mojang's database.
     *
     * @param name The username to verify.
     * @return True if the username is unique, otherwise false.
     */
    private boolean doesAlreadyExist(String name) {
        try {
            BufferedReader in = new BufferedReader(new InputStreamReader(new URL(MOJANG_URL + name).openStream()));
            String data = in.readLine();
            in.close();
            return data != null;
        } catch ( Exception e ) {
            return false;
        }
    }

    // Example transformation methods:
    private String scrambleNumbers(String string) {
        StringBuilder builder = new StringBuilder();
        for (char c : string.toCharArray()) {
            if (Character.isDigit(c)) {
                builder.append(random.nextInt(10));
            } else {
                builder.append(c);
            }
        }
        return builder.toString();
    }

    private String addNumbers(String string) {
        int length = random.nextInt(MAX_NUMBERS) + 1;
        StringBuilder numbers = new StringBuilder();
        for (int i = 0; i < length; i++) {
            numbers.append(random.nextInt(10));
        }
        return random.nextBoolean() ? numbers + string : string + numbers;
    }

    private String addYear(String string) {
        int year = Calendar.getInstance().get(Calendar.YEAR);
        int range = year - MAX_YEAR_RANGE;
        return string + (random.nextInt((year - range)) + range);
    }

    private String addRandomUnderscores(String string) {
        StringBuilder builder = new StringBuilder(string);
        for (int i = 0; i < random.nextInt(MAX_UNDERSCORES); i++) {
            int index = random.nextInt(string.length());
            builder.insert(index, UNDERSCORE);
        }
        return builder.toString();
    }

    private String addLazyUnderscore(String string) {
        return random.nextBoolean() ? string + UNDERSCORE : UNDERSCORE + string;
    }

    private String addPhoneticReplacements(String string) {
        Map<String, String> phoneticMap = Map.of("0", "O", "S", "Z", "1", "I", "3", "E");
        StringBuilder builder = new StringBuilder();
        for (char c : string.toCharArray()) {
            String replacement = phoneticMap.get(String.valueOf(c));
            if (replacement != null) {
                builder.append(replacement);
            } else {
                builder.append(c);
            }
        }
        return builder.toString();
    }

    private String addRandomCapitalization(String string) {
        StringBuilder builder = new StringBuilder();
        for (char c : string.toCharArray()) {
            builder.append(random.nextBoolean() ? Character.toUpperCase(c) : c);
        }
        return builder.toString();
    }

    private String addLogicalCapitalization(String string) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < string.length(); i++) {
            char c = string.charAt(i);
            if (i % 2 == 0) {
                builder.append(Character.toUpperCase(c));
            } else {
                builder.append(c);
            }
        }
        return builder.toString();
    }

    private char randomLetter() {
        String alphabet = "abcdefghijklmnopqrstuvwxyz";
        return alphabet.charAt(random.nextInt(alphabet.length()));
    }

    public String generateRandomString(boolean fullRandom) {
        String characters = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        StringBuilder randomString = new StringBuilder();
        int length = fullRandom ? 3 + random.nextInt(11) : 5;
        for (int i = 0; i < length; i++) {
            int randomIndex = random.nextInt(characters.length());
            randomString.append(characters.charAt(randomIndex));
        }
        return randomString.toString();
    }

    public static void main(String[] args) {
        System.out.println("----------1----------");
        for (int i = 0; i < 30; i++) {
            MinecraftNameGenerator generator = new MinecraftNameGenerator(3);
            String generatedName = generator.generate("Kovarty");
            System.out.println("Generated Name: " + generatedName);
            System.out.println("----------2----------");

            String randomString = generator.generateRandomString(true);
            System.out.println("Random String: " + randomString);
            System.out.println("----------3----------");
        }
    }
}
