package com.limegroup.gnutella.gui.options;

import com.limegroup.gnutella.gui.options.panes.*;
import com.limegroup.gnutella.gui.*;
import com.limegroup.gnutella.settings.ApplicationSettings;
import com.limegroup.gnutella.util.CommonUtils;
import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.awt.event.*;

/**
 * This class constructs all of the elements of the options window.  To add
 * a new option, this class should be used.  This class allows for options
 * to be added to already existing panes as well as for options to be added
 * to new panes that you can also add here.  To add a new top-level pane,
 * create a new <tt>OptionsPaneImpl</tt> and call the addOption method.
 * To add option items to that pane, add subclasses of
 * <tt>AbstractPaneItem</tt>.
 */
//2345678|012345678|012345678|012345678|012345678|012345678|012345678|012345678|
public final class OptionsConstructor {
	/**
	 * Handle to the top-level <tt>JDialog</tt window that contains all
	 * of the other GUI components.
	 */
	private final JDialog DIALOG;

	/**
	 * Constant for the default width of the options window.
	 */
	private final int OPTIONS_WIDTH = 600;

	/**
	 * Constant for the default height of the options window.
	 */
	private final int OPTIONS_HEIGHT = 460;

	/**
	 * Stored for convenience to allow using this in helper methods
	 * during construction.
	 */
	private final OptionsTreeManager TREE_MANAGER;

	/**
	 * Stored for convenience to allow using this in helper methods
	 * during construction.
	 */
	private final OptionsPaneManager PANE_MANAGER;

	/**
	 * The constructor create all of the options windows and their
	 * components.
	 *
	 * @param treeManager the <tt>OptionsTreeManager</tt> instance to
	 *                    use for constructing the main panels and
	 *                    adding elements
	 * @param paneManager the <tt>OptionsPaneManager</tt> instance to
	 *                    use for constructing the main panels and
	 *                    adding elements
	 */
	public OptionsConstructor(final OptionsTreeManager treeManager,
			final OptionsPaneManager paneManager) {
		TREE_MANAGER = treeManager;
		PANE_MANAGER = paneManager;
		final String title = GUIMediator.getStringResource("OPTIONS_TITLE");
        final boolean shouldBeModal = !(CommonUtils.isMacOSX() && 
                                        CommonUtils.isJava14OrLater());

		DIALOG = new JDialog(new Frame(), title, shouldBeModal);

		// make the window non-resizable only for operating systems
		// where we know this will not cause a problem
		if(CommonUtils.isWindows() || CommonUtils.isMacClassic() ||
		   CommonUtils.isMacOSX()) {
			DIALOG.setResizable(false);
		}
		DIALOG.setSize(OPTIONS_WIDTH, OPTIONS_HEIGHT);

		// most Mac users expect changes to be saved when the window
		// is closed, so save them
		if(CommonUtils.isAnyMac()) {
			DIALOG.addWindowListener(new WindowAdapter() {
				public void windowClosing(WindowEvent e) {
					try {
						OptionsMediator.instance().applyOptions();
					} catch(IOException ioe) {
						// nothing we should do here.  a message should
						// have been displayed to the user with more
						// information
					}
				}
			});
		}

		PaddedPanel mainPanel = new PaddedPanel();

		Box splitBox = new Box(BoxLayout.X_AXIS);

		Component treeComponent = TREE_MANAGER.getComponent();
		Component paneComponent = PANE_MANAGER.getComponent();

		splitBox.add(treeComponent);
		splitBox.add(paneComponent);
		mainPanel.add(splitBox);

		mainPanel.add(Box.createVerticalStrut(17));
		mainPanel.add(new OptionsButtonPanel().getComponent());

		DIALOG.getContentPane().add(mainPanel);


		// Create the keys for the main panes.  These are used as
		// unique identifiers for the windows as well as for keys
		// for the locale-specific string used to display them
		final String SAVE_KEY           = "OPTIONS_SAVE_MAIN_TITLE";
		final String SHARED_KEY         = "OPTIONS_SHARED_MAIN_TITLE";
		final String SPEED_KEY          = "OPTIONS_SPEED_MAIN_TITLE";
		final String DOWNLOAD_KEY       = "OPTIONS_DOWNLOAD_MAIN_TITLE";
		final String UPLOAD_KEY         = "OPTIONS_UPLOAD_MAIN_TITLE";
		final String UPLOAD_BASIC_KEY   = "OPTIONS_UPLOAD_BASIC_MAIN_TITLE";
		final String UPLOAD_SLOTS_KEY   = "OPTIONS_UPLOAD_SLOTS_MAIN_TITLE";
		final String CONNECTIONS_KEY    = "OPTIONS_CONNECTIONS_MAIN_TITLE";
		final String SHUTDOWN_KEY       = "OPTIONS_SHUTDOWN_MAIN_TITLE";
		final String CHAT_KEY           = "OPTIONS_CHAT_MAIN_TITLE";
		final String PLAYER_KEY         = "OPTIONS_PLAYER_MAIN_TITLE";
        final String ITUNES_KEY			= "OPTIONS_ITUNES_MAIN_TITLE";
        final String ITUNES_DAAP_KEY    = "OPTIONS_ITUNES_DAAP_MAIN_TITLE";
		final String POPUPS_KEY         = "OPTIONS_POPUPS_MAIN_TITLE";
		final String BUGS_KEY           = "OPTIONS_BUGS_MAIN_TITLE";
		final String APPS_KEY           = "OPTIONS_APPS_MAIN_TITLE";
		final String SEARCH_KEY         = "OPTIONS_SEARCH_MAIN_TITLE";
		final String SEARCH_LIMIT_KEY   = "OPTIONS_SEARCH_LIMIT_MAIN_TITLE";
		final String SEARCH_QUALITY_KEY = "OPTIONS_SEARCH_QUALITY_MAIN_TITLE";
		final String SEARCH_SPEED_KEY   = "OPTIONS_SEARCH_SPEED_MAIN_TITLE";
		final String FILTERS_KEY        = "OPTIONS_FILTERS_MAIN_TITLE";
		final String RESULTS_KEY        = "OPTIONS_RESULTS_MAIN_TITLE";
		final String MESSAGES_KEY       = "OPTIONS_MESSAGES_MAIN_TITLE";
		final String ADVANCED_KEY       = "OPTIONS_ADVANCED_MAIN_TITLE";
		final String COMPRESSION_KEY    = "OPTIONS_COMPRESSION_MAIN_TITLE";
		final String PREFERENCING_KEY   = "OPTIONS_PREFERENCING_MAIN_TITLE";
		final String PORT_KEY           = "OPTIONS_PORT_MAIN_TITLE";
		final String FIREWALL_KEY       = "OPTIONS_FIREWALL_MAIN_TITLE";
        final String GUI_KEY            = "OPTIONS_GUI_MAIN_TITLE";
        final String AUTOCOMPLETE_KEY   = "OPTIONS_AUTOCOMPLETE_MAIN_TITLE"; 
        final String STARTUP_KEY        = "OPTIONS_STARTUP_MAIN_TITLE";   
        final String PROXY_KEY          = "OPTIONS_PROXY_MAIN_TITLE";

		// Create all of the panes that can be displayed to the user.
		// For each pane, create all of the corresponding pane items,
		// and insert pane in the options tree.

		final OptionsPane savingPane = new OptionsPaneImpl(SAVE_KEY);
		savingPane.add(new SaveDirPaneItem("SAVE_DIR"));
		savingPane.add(new PurgeIncompletePaneItem("PURGE_INCOMPLETE_TIME"));
		addOption(OptionsMediator.ROOT_NODE_KEY, savingPane);

		final OptionsPane sharingPane = new OptionsPaneImpl(SHARED_KEY);
		sharingPane.add(new SharedDirPaneItem("SHARED_DIRS"));
		sharingPane.add(new ExtensionsPaneItem("SHARED_EXTENSIONS"));
		addOption(OptionsMediator.ROOT_NODE_KEY, sharingPane);

		final OptionsPane speedPane = new OptionsPaneImpl(SPEED_KEY);
		speedPane.add(new SpeedPaneItem("SPEED"));
		speedPane.add(new DisableSupernodeModePaneItem("DISABLE_SUPERNODE_MODE"));
        speedPane.add(new DisableOOBSearchingPaneItem("DISABLE_OOB_SEARCHING"));
		addOption(OptionsMediator.ROOT_NODE_KEY, speedPane);

		final OptionsPane downloadPane = new OptionsPaneImpl(DOWNLOAD_KEY);
		downloadPane.add(new MaximumDownloadsPaneItem("DOWNLOAD_MAX"));
		downloadPane.add(new AutoClearDownloadsPaneItem("DOWNLOAD_CLEAR"));
		addOption(OptionsMediator.ROOT_NODE_KEY, downloadPane);


		// add the upload options group
		addGroupTreeNode(OptionsMediator.ROOT_NODE_KEY, UPLOAD_KEY);

		final OptionsPane uploadBasicPane = new OptionsPaneImpl(UPLOAD_BASIC_KEY);
		uploadBasicPane.add(new AutoClearUploadsPaneItem("UPLOAD_CLEAR"));
		uploadBasicPane.add(new UploadBandwidthPaneItem("UPLOAD_BANDWIDTH"));
		uploadBasicPane.add(new PartialFileSharingPaneItem("UPLOAD_ALLOW_PARTIAL_SHARING"));
		addOption(UPLOAD_KEY, uploadBasicPane);

		final OptionsPane uploadSlotsPane = new OptionsPaneImpl(UPLOAD_SLOTS_KEY);
		uploadSlotsPane.add(new PerPersonUploadsPaneItem("UPLOAD_PER_PERSON"));
		//uploadSlotsPane.add(new SoftMaximumUploadsPaneItem("UPLOAD_SOFT_MAX"));
		uploadSlotsPane.add(new MaximumUploadsPaneItem("UPLOAD_MAX"));
		addOption(UPLOAD_KEY, uploadSlotsPane);


		final OptionsPane connectionsPane = new OptionsPaneImpl(CONNECTIONS_KEY);
		connectionsPane.add(new ConnectOnStartupPaneItem("CONNECT_ON_STARTUP"));
		//connectionsPane.add(new AutoConnectPaneItem("AUTO_CONNECT"));
		//connectionsPane.add(new AutoConnectActivePaneItem("AUTO_CONNECT_ACTIVE"));
		addOption(OptionsMediator.ROOT_NODE_KEY, connectionsPane);

		final OptionsPane shutdownPane = new OptionsPaneImpl(SHUTDOWN_KEY);
		shutdownPane.add(new ShutdownPaneItem("SHUTDOWN"));
		addOption(OptionsMediator.ROOT_NODE_KEY, shutdownPane);

		final OptionsPane chatPane = new OptionsPaneImpl(CHAT_KEY);
		chatPane.add(new ChatActivePaneItem("CHAT_ACTIVE"));
		addOption(OptionsMediator.ROOT_NODE_KEY, chatPane);

		if (!CommonUtils.isMacClassic()) {
			final OptionsPane playerPane = new OptionsPaneImpl(PLAYER_KEY);
			playerPane.add(new PlayerPreferencePaneItem("PLAYER_PREFERENCE"));
			addOption(OptionsMediator.ROOT_NODE_KEY, playerPane);
		}
		
        
		if (CommonUtils.isJava14OrLater() || CommonUtils.isMacOSX()) {
        
            addGroupTreeNode(OptionsMediator.ROOT_NODE_KEY, ITUNES_KEY);
            
            // Auto import of newly downloaded files is only 
            // available on Mac OS X
            if (CommonUtils.isMacOSX()) {
                final OptionsPane itunesPane = new OptionsPaneImpl(ITUNES_KEY);
                itunesPane.add(new iTunesPreferencePaneItem("ITUNES_PREFERENCE"));
                addOption(ITUNES_KEY, itunesPane);
            }
            
            if (CommonUtils.isJava14OrLater()) {
                final OptionsPane daapPane = new OptionsPaneImpl(ITUNES_DAAP_KEY);
                daapPane.add(new DaapSupportPaneItem("ITUNES_DAAP_PREFERENCE"));
                daapPane.add(new DaapPasswordPaneItem("ITUNES_DAAP_PASSWORD"));
                addOption(ITUNES_KEY, daapPane);
            }
        }
        
		if (CommonUtils.isUnix()) {
			final OptionsPane browserPane = new OptionsPaneImpl(APPS_KEY);
			browserPane.add(new BrowserPaneItem("BROWSER_PREFERENCE"));
			browserPane.add(new ImageViewerPaneItem("IMAGE_VIEWER_PREFERENCE"));
			browserPane.add(new VideoPlayerPaneItem("VIDEO_PLAYER_PREFERENCE"));
			browserPane.add(new AudioPlayerPaneItem("AUDIO_PLAYER_PREFERENCE"));
			addOption(OptionsMediator.ROOT_NODE_KEY, browserPane);
		}

		final OptionsPane bugsPane = new OptionsPaneImpl(BUGS_KEY);
		bugsPane.add( new BugsPaneItem("BUGS") );
		addOption(OptionsMediator.ROOT_NODE_KEY, bugsPane);
		
		addGroupTreeNode(OptionsMediator.ROOT_NODE_KEY, GUI_KEY);
		
    	final OptionsPane popupsPane = new OptionsPaneImpl(POPUPS_KEY);
		popupsPane.add( new PopupsPaneItem("POPUPS") );
		addOption(GUI_KEY, popupsPane);
        
        final OptionsPane autocompletePane = 
            new OptionsPaneImpl(AUTOCOMPLETE_KEY);
        autocompletePane.add(new AutoCompletePaneItem("AUTOCOMPLETE"));
        addOption(GUI_KEY, autocompletePane);

		// add the search options group
		addGroupTreeNode(OptionsMediator.ROOT_NODE_KEY, SEARCH_KEY);

		final OptionsPane searchLimitPane = 
            new OptionsPaneImpl(SEARCH_LIMIT_KEY);
		searchLimitPane.add(new MaximumSearchesPaneItem("SEARCH_MAX"));
		addOption(SEARCH_KEY, searchLimitPane);

		final OptionsPane searchQualityPane = 
            new OptionsPaneImpl(SEARCH_QUALITY_KEY);
		searchQualityPane.add(new SearchQualityPaneItem("SEARCH_QUALITY"));
		addOption(SEARCH_KEY, searchQualityPane);

		final OptionsPane searchSpeedPane = 
            new OptionsPaneImpl(SEARCH_SPEED_KEY);
		searchSpeedPane.add(new SearchSpeedPaneItem("SEARCH_SPEED"));
		addOption(SEARCH_KEY, searchSpeedPane);


		// add the filters options group
		addGroupTreeNode(OptionsMediator.ROOT_NODE_KEY, FILTERS_KEY);

		final OptionsPane filtersResultsPane = new OptionsPaneImpl(RESULTS_KEY);
		filtersResultsPane.add(new IgnoreResultsPaneItem("IGNORE_RESULTS"));
		filtersResultsPane.add(new IgnoreResultTypesPaneItem("IGNORE_RESULT_TYPES"));
		addOption(FILTERS_KEY, filtersResultsPane);

		final OptionsPane filtersMessagesPane = 
            new OptionsPaneImpl(MESSAGES_KEY);
		filtersMessagesPane.add(new IgnoreMessagesPaneItem("IGNORE_MESSAGES"));
		filtersMessagesPane.add(new AllowMessagesPaneItem("ALLOW_MESSAGES"));
		addOption(FILTERS_KEY, filtersMessagesPane);


		// add the advanced options group
		addGroupTreeNode(OptionsMediator.ROOT_NODE_KEY, ADVANCED_KEY);
		final OptionsPane compressionPane = 
            new OptionsPaneImpl(COMPRESSION_KEY);
		compressionPane.add(new CompressionPaneItem("COMPRESSION"));
		addOption(ADVANCED_KEY, compressionPane);

		final OptionsPane preferencingPane = 
            new OptionsPaneImpl(PREFERENCING_KEY);
		preferencingPane.add(new ConnectionPreferencingPaneItem("CONNECT_PREF"));
		addOption(ADVANCED_KEY, preferencingPane);

		final OptionsPane portPane = new OptionsPaneImpl(PORT_KEY);
		portPane.add(new PortPaneItem("PORT"));
		addOption(ADVANCED_KEY, portPane);

		final OptionsPane firewallPane = new OptionsPaneImpl(FIREWALL_KEY);
		firewallPane.add(new ForceIPPaneItem("FORCE_IP"));
		addOption(ADVANCED_KEY, firewallPane);
        
		final OptionsPane proxyPane = new OptionsPaneImpl(PROXY_KEY);
		proxyPane.add(new ProxyPaneItem("PROXY"));
		proxyPane.add(new ProxyLoginPaneItem("PROXY_LOGIN"));
		addOption(ADVANCED_KEY, proxyPane);
        
        // we currently only support running LimeWire on system startup on
        // Windows systems where LimeWire was installed using InstallShield,
        // which this checks (more or less with the LANGUAGE setting)
        if((CommonUtils.isWindows() &&
           ApplicationSettings.LANGUAGE.isDefault()) ||
           (CommonUtils.isMacOSX() && CommonUtils.isJava14OrLater() &&
            CommonUtils.isCocoaFoundationAvailable() )) {
            final OptionsPane startupPane = new OptionsPaneImpl(STARTUP_KEY);
            startupPane.add(new StartupPaneItem("STARTUP"));
            addOption(ADVANCED_KEY, startupPane);
        }
	}

	/**
	 * Adds a parent node to the tree.  This node serves navigational
	 * purposes only, and so has no corresponding <tt>OptionsPane</tt>.
	 * This method allows for multiple tiers of parent nodes, not only
	 * top-level parents.
	 *
	 * @param parentKey the key of the parent node to add this parent
	 *                  node to
	 * @param childKey the key of the new parent node that is a child of
	 *                 the <tt>parentKey</tt> argument
	 */
	private final void addGroupTreeNode(final String parentKey,
			final String childKey) {
		TREE_MANAGER.addNode(parentKey, childKey,
			GUIMediator.getStringResource(childKey));
	}

	/**
	 * Adds the specified key and <tt>OptionsPane</tt> to current
	 * set of options.  This adds this <tt>OptionsPane</tt> to the set of
	 * <tt>OptionsPane</tt>s the user can select.
	 *
	 * @param parentKey the key of the parent node to add the new node to
	 * @param pane the new pane that also supplies the name of the node
	 *             in the tree
	 */
	private final void addOption(final String parentKey,
			final OptionsPane pane) {
		TREE_MANAGER.addNode(parentKey, pane.getName(),
			GUIMediator.getStringResource(pane.getName()));
		PANE_MANAGER.addPane(pane);
	}


	/**
	 * Makes the options window either visible or not visible depending on the
	 * boolean argument.
	 *
	 * @param visible <tt>boolean</tt> value specifying whether the options
	 *				window should be made visible or not visible
	 */
	public final void setOptionsVisible(boolean visible) {
	    if(!visible) {
	        DIALOG.dispose();
        } else {
            if(GUIMediator.isAppVisible())
    			DIALOG.setLocationRelativeTo(GUIMediator.getAppFrame());
    		else {
    			Dimension screenSize =
    			    Toolkit.getDefaultToolkit().getScreenSize();
        		Dimension dialogSize = DIALOG.getSize();
        		DIALOG.setLocation((screenSize.width - dialogSize.width)/2,
        						   (screenSize.height - dialogSize.height)/2);
    		}
    		DIALOG.show();
        }
	}	
	
	/** Returns if the Options Box is visible.
	 *  @return true if the Options Box is visible.
	 */
	public final boolean isOptionsVisible() {
		return DIALOG.isVisible();
	}

	/**
	 * Returns the main <tt>JDialog</tt> instance for the options window,
	 * allowing other components to position themselves accordingly.
	 *
	 * @return the main options <tt>JDialog</tt> window
	 */
	JDialog getMainOptionsComponent() {
		return DIALOG;
	}
}
