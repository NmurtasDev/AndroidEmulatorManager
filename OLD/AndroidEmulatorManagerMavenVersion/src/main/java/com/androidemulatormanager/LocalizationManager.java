package com.androidemulatormanager;

import java.text.MessageFormat;
import java.util.Locale;
import java.util.ResourceBundle;

public class LocalizationManager {
    private ResourceBundle messages;
    
    public LocalizationManager() {
        initLocalization();
    }
    
    private void initLocalization() {
        Locale locale = Locale.getDefault();
        String language = locale.getLanguage();
        
        if (!"it".equals(language) && !"en".equals(language)) {
            locale = Locale.ENGLISH;
        }
        
        try {
            messages = ResourceBundle.getBundle("messages", locale);
        } catch (Exception e) {
            messages = ResourceBundle.getBundle("messages", Locale.ENGLISH);
        }
    }
    
    public String getMessage(String key, Object... params) {
        try {
            String message = messages.getString(key);
            if (params.length > 0) {
                return MessageFormat.format(message, params);
            }
            return message;
        } catch (Exception e) {
            return key;
        }
    }
}