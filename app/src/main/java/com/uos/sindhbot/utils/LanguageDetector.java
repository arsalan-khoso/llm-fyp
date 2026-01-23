package com.uos.sindhbot.utils;

import java.util.regex.Pattern;

public class LanguageDetector {
    
    // Patterns for Urdu (Arabic script)
    private static final Pattern URDU_PATTERN = Pattern.compile(".*[\\u0600-\\u06FF\\u0750-\\u077F\\u08A0-\\u08FF\\uFB50-\\uFDFF\\uFE70-\\uFEFF].*");
    
    // Patterns for Sindhi (Arabic script with specific characters)
    private static final Pattern SINDHI_PATTERN = Pattern.compile(".*[\\u0600-\\u06FF\\u0750-\\u077F].*");
    
    public static String detectLanguage(String text) {
        if (text == null || text.trim().isEmpty()) {
            return "en";
        }
        
        // Check for Urdu/Sindhi (Arabic script)
        if (URDU_PATTERN.matcher(text).matches()) {
            // Try to distinguish between Urdu and Sindhi
            // This is a simplified detection - you may need more sophisticated logic
            if (containsSindhiSpecificChars(text)) {
                return "sd"; // Sindhi
            }
            return "ur"; // Urdu
        }
        
        // Default to English
        return "en";
    }
    
    private static boolean containsSindhiSpecificChars(String text) {
        // Sindhi-specific characters (simplified check)
        // You can expand this with actual Sindhi character ranges
        String sindhiChars = "ڄڃڇڊڋڌڍڎڏڐڑڒړڔڕږڗڙښڛڜڝڞڟڠڡڢڣڤڥڦڧڨکڪګڬڭڮگڰڱڲڳڴڵڶڷڸڹںڻڼڽھڿہۂۃۄۅۆۇۈۉۊۋیۍێۏېۑےۓ";
        for (char c : text.toCharArray()) {
            if (sindhiChars.indexOf(c) != -1) {
                return true;
            }
        }
        return false;
    }
    
    public static String getLanguageName(String code) {
        switch (code) {
            case "en":
                return "English";
            case "ur":
                return "Urdu";
            case "sd":
                return "Sindhi";
            default:
                return "English";
        }
    }
}

