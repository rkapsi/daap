package com.limegroup.gnutella.gui;

import com.limegroup.gnutella.*;
import com.limegroup.gnutella.bugs.BugManager;
import com.limegroup.gnutella.browser.ExternalControl;
import com.limegroup.gnutella.settings.StartupSettings;
import com.limegroup.gnutella.settings.DaapSettings;
import com.limegroup.gnutella.gui.DaapManager;
import com.limegroup.gnutella.util.SystemUtils;
import com.limegroup.gnutella.util.CommonUtils;
import com.limegroup.gnutella.util.I18NConvert;
import com.limegroup.gnutella.gui.init.*;
import com.limegroup.gnutella.gui.notify.NotifyUserProxy;

import javax.swing.UIManager;
import javax.swing.plaf.basic.BasicHTML;
import javax.swing.JLabel;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * This class instantiates all of the "top-level" application classes.  These
 * include the <tt>ResourceManager</tt>, the <tt>GUIMediator</tt>, the
 * update checking classes, the <tt>SettingsManager</tt>, the
 * <tt>FileManager</tt>, <tt>RouterService</tt>, etc.  <tt>GUIMediator</tt>
 * and <tt>RouterService</tt> construct the bulk of the front and back ends,
 * respectively.  This class also links together any top-level classes that
 * need to know about each other.
 */
//2345678|012345678|012345678|012345678|012345678|012345678|012345678|012345678|
public final class Initializer {

    private static final Log LOG = LogFactory.getLog(Initializer.class);
    
    /**
     * Used to determine whether or not LimeWire is running
     * from a system startup.
     */
    private static volatile boolean isStartup = false;
    
    /**
     * Suppress the default constructor to ensure that this class can never be
     * constructed.
     */
    private Initializer() {}
    
    /**
     * Initializes all of the necessary application classes.  This begins with
     * the <tt>ResourceManager</tt> class that handles the look and feel of the
     * application as well as providing access to any string or image resources
     * used in the application.  This then constructs the <tt>GUIMediator</tt>,
     * which in turn handles the construction of the entire frontend.<p>
     *
     * The <tt>SettingsManager</tt> and the <tt>FileManager</tt> are
     * initialized early to make sure that their resources are available and
     * loading as soon as possible.  The update and init code is also kicked
     * off early to check for any available updates and to make sure that all
     * of the settings have been properly set up (going into the init setup
     * sequence otherwise).
     *
     * Finally, all of the primary backend classes are created, such as the
     * <tt>RouterService</tt> interface and <tt>StandardMessageRouter</tt>.
     * <tt>RouterService</tt> handles the creation of the other backend
     * classes.
     *
     * If this throws any exceptions, then LimeWire was not able to construct
     * properly and must be shut down.
     */
    static void initialize(String args[]) throws Throwable {
        long startMemory = 0;
        if(LOG.isTraceEnabled()) {
            startMemory = Runtime.getRuntime().totalMemory()
                        - Runtime.getRuntime().freeMemory();
            LOG.trace("START Initializer, using: " + startMemory + " memory");
        }
    
        // Set the error handler so we can receive core errors.
        ErrorService.setErrorCallback(new ErrorHandler());
        
        // Set the messaging handler so we can receive core messages
        com.limegroup.gnutella.MessageService.setCallback(new MessageHandler());
        
        // Set the default event error handler so we can receive uncaught
        // AWT errors.
        if(!CommonUtils.isJava118())
            DefaultErrorCatcher.install();
        
        // Register MacOS X specific stuff.
        if (CommonUtils.isMacOSX()) {
            LOG.trace("START registering OSX events");
            // Register GURL to receive AppleEvents, such as magnet links.
            GURLHandler.getInstance().register();
            // Register Cocoa handlers for the menus.
            if(CommonUtils.isJava14OrLater())
                CocoaApplicationEventHandler.instance().register();
            // Raise the number of allowed concurrent open files to 1024.
            SystemUtils.setOpenFileLimit(1024);
            LOG.trace("STOP registering OSX events");
        }
        
        // If this is a request to launch a pmf then just do it and exit.
        if ( args.length >= 2 && "-pmf".equals(args[0]) ) {
            PackagedMediaFileLauncher.launchFile(args[1], false); 
            return;
        }
        
        // Yield so any other events can be run to determine
        // startup status, but only if we're going to possibly
        // be starting...
        if(StartupSettings.RUN_ON_STARTUP.getValue()) {
            LOG.trace("START yield");
            Thread.yield();
            LOG.trace("STOP yield");
        }
        
        if (args.length >= 1 && "-startup".equals(args[0]))
            isStartup = true;
        
        if (isStartup) {
            // if the user doesn't want to start on system startup, exit the
            // JVM immediately
            if(!StartupSettings.RUN_ON_STARTUP.getValue())
                System.exit(0);
        }
        
        // Test for preexisting LimeWire and pass it a magnet URL if one
        // has been passed in.
        String arg = null;
        LOG.trace("START magnet check");
        if (args.length > 0 && !args[0].equals("-startup")) {
            arg = ExternalControl.preprocessArgs(args);
            ExternalControl.checkForActiveLimeWire(arg);
            ExternalControl.enqueueMagnetRequest(arg);
        } else if (!StartupSettings.ALLOW_MULTIPLE_INSTANCES.getValue()) {
            // if we don't want multiple instances, we need to check if
            // limewire is already active.
            ExternalControl.checkForActiveLimeWire();
        }
        LOG.trace("STOP magnet check");
        
        LOG.trace("START system properties");
        Initializer.setSystemProperties();
        Initializer.setOSXSystemProperties();
        LOG.trace("STOP system properties");
        
        LOG.trace("START ResourceManager");
        ResourceManager.instance();
        LOG.trace("STOP ResourceManager");
        
        // Show the splash screen if we're not starting automatically on 
        // system startup
        SplashWindow splash = null;
        if(!isStartup)
            splash = new SplashWindow();
        
        // Load up the HTML engine.
        SplashWindow.setStatusText(
            GUIMediator.getStringResource("SPLASH_STATUS_HTML_ENGINE"));
        BasicHTML.createHTMLView(new JLabel(), "<html>.</html>");

        //Initialize the bug manager
        LOG.trace("START BugManager");
        BugManager.instance();
        LOG.trace("STOP BugManager");
        
        // Run through the initialization sequence -- this must always be
        // called before GUIMediator constructs the LibraryTree!
        LOG.trace("START SetupManager");
        new SetupManager().createIfNeeded();
        LOG.trace("STOP SetupManager");
        
        // Make sure the save directory is valid.
        LOG.trace("START SaveDirectoryHandler");
        SaveDirectoryHandler.handleSaveDirectory();
        LOG.trace("STOP SaveDirectoryHandler");
        
        SplashWindow.setStatusText(
            GUIMediator.getStringResource("SPLASH_STATUS_INTERFACE"));
        
        // Construct the frontend
        LOG.trace("START GUIMediator.instance()");
        GUIMediator mediator = GUIMediator.instance();
        LOG.trace("STOP GUIMediator.instance()");
        
        SplashWindow.setStatusText(
            GUIMediator.getStringResource("SPLASH_STATUS_CORE_COMPONENTS"));
        
        // Construct gui callback class
        ActivityCallback ac = new VisualConnectionCallback();
        
        // Construct the RouterService object, which functions as the
        // backend initializer as well as the interface from the GUI to the
        // front end.
        LOG.trace("START new RouterService");
        RouterService routerService = new RouterService(ac);
        LOG.trace("STOP new RouterService");
        
        // Notify GUIMediator of the RouterService interface class to the
        // backend.
        mediator.setRouterService(routerService);
        
        // Create the user desktop notifier object.
        // This must be done before the GUI is made visible,
        // otherwise the user can close it and not see the
        // tray icon.
        LOG.trace("START NotifyUserProxy");
        NotifyUserProxy notifyProxy = NotifyUserProxy.instance();
        // Hide the notifier after it has been initialized.
        notifyProxy.hideNotify();
        LOG.trace("STOP NotifyUserProxy");
        
        // Hide the splash screen and recycle its memory.
        if(splash != null) {
            splash.dispose();
        }
        
        GUIMediator.allowVisibility();
        
        // Make the GUI visible.
        if(!isStartup) {
            LOG.trace("START setAppVisible");
            GUIMediator.setAppVisible(true);
            LOG.trace("STOP setAppVisible");
        } else {
            LOG.trace("START startupHidden");
            GUIMediator.startupHidden();
            LOG.trace("STOP startupHidden");
        }
        
        // Initialize IconManager.
        LOG.trace("START IconManager.instance()");
        GUIMediator.setSplashScreenString(
            GUIMediator.getStringResource("SPLASH_STATUS_ICONS"));
        IconManager.instance();
        LOG.trace("STOP IconManager.instance()");        
        
        // Touch the I18N stuff to ensure it loads properly.
        LOG.trace("START I18NConvert.instance()");
        GUIMediator.setSplashScreenString(
            GUIMediator.getStringResource("SPLASH_STATUS_I18N"));
        I18NConvert.instance();
        LOG.trace("STOP I18NConvert.instance()");
        
        // Start the backend threads.  Note that the GUI is not yet visible,
        // but it needs to be constructed at this point  
        LOG.trace("START RouterService");
        routerService.start();
        LOG.trace("STOP RouterService");
        
        // Instruct the gui to perform tasks that can only be performed
        // after the backend has been constructed.
        mediator.startTimer();        
        
        // Activate a download for magnet URL locally if one exists
        ExternalControl.runQueuedMagnetRequest();
        
        // Start DaapManager if Java 1.4 or later and DAAP support
        // is enabled
        if (CommonUtils.isJava14OrLater() && 
                DaapSettings.DAAP_ENABLED.getValue()) {
            
            LOG.trace("START DaapManager");
            try {
                GUIMediator.setSplashScreenString(
                        GUIMediator.getStringResource("SLPASH_STATUS_DAAP"));
                DaapManager.instance().start();
                DaapManager.instance().init();
            } catch (java.io.IOException err) {
                ErrorService.error(err);
            }
            LOG.trace("STOP DaapManager");
        }
        
        // Tell the GUI that loading is all done.
        GUIMediator.instance().loadFinished();
        
        // update the repaintInterval after the Splash is created,
        // so that the splash gets the smooth animation.
        if(CommonUtils.isMacOSX() && CommonUtils.isJava14OrLater())
            UIManager.put("ProgressBar.repaintInterval", new Integer(500));
        
        if(LOG.isTraceEnabled()) {
            long stopMemory = Runtime.getRuntime().totalMemory()
                            - Runtime.getRuntime().freeMemory();
            LOG.trace("STOP Initializer, using: " + stopMemory +
                      " memory, consumed: " + (stopMemory - startMemory));
        }
    }
    
    /**
     * Sets the startup property to be true.
     */
    static void setStartup() {
        isStartup = true;
    }
    
    /**
	 * Sets the system properties.
     */
    static void setSystemProperties() {            
        System.setProperty("http.agent", CommonUtils.getHttpServer());
    }
    
    /**
     * Sets OSX system properties.
     */
    static void setOSXSystemProperties() {
        if (!CommonUtils.isMacOSX())
            return;
            
        if(CommonUtils.isJava14OrLater()) {
            System.setProperty("apple.laf.useScreenMenuBar", "true");
        } else {
            System.setProperty("com.apple.macos.useScreenMenuBar", "true");
            System.setProperty("com.apple.mrj.application.apple.menu.about.name", "LimeWire");
            System.setProperty("com.apple.macos.use-file-dialog-packages", "true");
        }
    }
}
