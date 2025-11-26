package com.epam.healenium.service;

import java.util.Map;

public interface SettingService {
    boolean setLogLevel(String logLevel);
    
    boolean setKeySelectorUrl(boolean enabled);
    
    boolean setCollectMetrics(boolean enabled);
    
    boolean setFindElementsAutoHealing(boolean enabled);
    
    Map<String, Object> updateSetting(String key, String value);
    
    Map<String, Object> getAllSettings();
}
