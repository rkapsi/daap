
package com.limegroup.gnutella.settings;

/**
 * Settings for iTunes
 */
public class iTunesSettings extends LimeProps {
    
    private iTunesSettings() {}
    
    /**
     * whether or not player should be enabled.
     */
    public static BooleanSetting ITUNES_SUPPORT_ENABLED =
        FACTORY.createBooleanSetting("ITUNES_SUPPORT_ENABLED", true);

      
    /**
     * Supported file types
     */
    public static StringArraySetting ITUNES_SUPPORTED_FILE_TYPES = 
        FACTORY.createStringArraySetting("ITUNES_SUPPORTED_FILE_TYPES", 
            new String[]{".mp3", ".aif", ".aiff", ".wav", ".mp2", ".mp4", 
                        ".aac", ".mid", ".m4a", ".m4p", ".ogg"});
						
	
	// DAAP
	
	public static BooleanSetting DAAP_SUPPORT_ENABLED =
		FACTORY.createBooleanSetting("DAAP_SUPPORT_ENABLED", true);
		
	public static StringSetting DAAP_LIBRARY_NAME =
		FACTORY.createStringSetting("DAAP_LIBRARY_NAME", "LimeWire");
	
	public static IntSetting DAAP_PORT =
		FACTORY.createIntSetting("DAAP_PORT", 5214);
		
	public static StringSetting DAAP_TYPE_NAME =
		FACTORY.createStringSetting("DAAP_TYPE_NAME", "_daap._tcp.local.");
	
	//  Actually: "LimeWire" + "." + DAAP_TYPE_NAME.getValue()
	public static StringSetting DAAP_SERVICE_NAME =
		FACTORY.createStringSetting("DAAP_SERVICE_NAME", "LimeWire");
		
	public static IntSetting DAAP_WEIGHT = FACTORY.createIntSetting("DAAP_WEIGHT", 0);
	public static IntSetting DAAP_PRIORITY = FACTORY.createIntSetting("DAAP_PRIORITY", 0);
	
	public static StringSetting DAAP_SERVICE_DESCRIPTION =
		FACTORY.createStringSetting("DAAP_SERVICE_DESCRIPTION", "LimeWire");
		
	public static BooleanSetting DAAP_REQUIRES_PASSWORD =
		FACTORY.createBooleanSetting("DAAP_REQUIRES_PASSWORD", false);
		
	public static StringSetting DAAP_PASSWORD =
		FACTORY.createStringSetting("DAAP_PASSWORD", "");
}
