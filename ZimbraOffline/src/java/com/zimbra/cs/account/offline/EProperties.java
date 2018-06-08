/*
 * 
 */
package com.zimbra.cs.account.offline;

import java.util.Properties;

class EProperties extends Properties {
    private static final long serialVersionUID = -8135956477865965194L;

    @Override
    public String getProperty(String key, String defaultValue) {
        String val = super.getProperty(key, defaultValue);
        return val == null ? null : val.trim();
    }

    @Override
    public String getProperty(String key) {
        String val = super.getProperty(key);
        return val == null ? null : val.trim();
    }

    public int getPropertyAsInteger(String key, int defaultValue) {
        String val = getProperty(key);
        if (val == null || val.length() == 0)
            return defaultValue;
        try {
            return Integer.parseInt(val);
        } catch (NumberFormatException x) {
            return defaultValue;
        }
    }

    public boolean getPropertyAsBoolean(String key, boolean defaultValue) {
        String val = getProperty(key);
        if (val == null || val.length() == 0)
            return defaultValue;
        return Boolean.parseBoolean(val);
    }

    public String getNumberedProperty(String prefix, int number, String suffix) {
        return getProperty(prefix + '.' + number + '.' + suffix);
    }

    public String getNumberedProperty(String prefix, int number, String suffix,
                                      String defaultValue) {
        return getProperty(prefix + '.' + number + '.' + suffix, defaultValue);
    }

    public int getNumberedPropertyAsInteger(String prefix, int number, String suffix, int defaultValue) {
        String val = getNumberedProperty(prefix, number, suffix);
        if (val == null || val.length() == 0)
            return defaultValue;
        try {
            return Integer.parseInt(val);
        } catch (NumberFormatException x) {
            return defaultValue;
        }
    }

    public String getNumberedProperty(String prefix, int n1, String midfix, int n2) {
        return getProperty(prefix + '.' + n1 + '.' + midfix + '.' + n2);
    }
    
    public String getNumberedProperty(String prefix, int n1, String midfix, int n2, String suffix) {
        return getProperty(prefix + '.' + n1 + '.' + midfix + '.' + n2 + '.' + suffix);
    }

    public boolean getNumberedPropertyAsBoolean(String prefix, int n1, String midfix, int n2, String suffix, boolean defaultValue) {
        String val = getProperty(prefix + '.' + n1 + '.' + midfix + '.' + n2 + '.' + suffix);
        if (val == null || val.length() == 0)
            return defaultValue;
        return Boolean.parseBoolean(val);
    }
}
