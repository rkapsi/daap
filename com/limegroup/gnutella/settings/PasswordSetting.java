package com.limegroup.gnutella.settings;

import java.util.Properties;

/**
 * Class for a password setting.
 */
public final class PasswordSetting extends Setting {

    String value;

    /**
     * Creates a new <tt>PasswordSetting</tt> instance with the specified key
     * and defualt value.
     * 
     * @param key the constant key to use for the setting
     * @param defaultStr the default value to use for the setting
     */
    PasswordSetting(Properties defaultProps, Properties props, String key,
            String defaultStr) {
        super(defaultProps, props, key, defaultStr, null);
    }

    PasswordSetting(Properties defaultProps, Properties props, String key,
            String defaultStr, String simppKey) {
        super(defaultProps, props, key, defaultStr, simppKey);
    }

    /**
     * Accessor for the value of this setting.
     * 
     * @return the value of this setting
     */
    public String getValue() {
        return value;
    }

    /**
     * Mutator for this setting.
     * 
     * @param str the <tt>String</tt> to store
     */
    public void setValue(String str) {
        super.setValue(str);
    }

    /**
     * Load value from property string value
     * 
     * @param sValue property string value
     */
    protected void loadValue(String sValue) {
        value = sValue;
    }
}