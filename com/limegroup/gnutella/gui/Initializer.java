package com.limegroup.gnutella.gui;

import com.limegroup.gnutella.*;
import com.limegroup.gnutella.bugs.BugManager;
import com.limegroup.gnutella.browser.ExternalControl;
import com.limegroup.gnutella.settings.StartupSettings;
import com.limegroup.gnutella.util.SystemUtils;
import com.limegroup.gnutella.util.CommonUtils;
import com.limegroup.gnutella.util.I18NConvert;
import com.limegroup.gnutella.gui.init.*;
import com.limegroup.gnutella.gui.notify.NotifyUserProxy;

import java.lang.reflect.*;
import javax.swing.UIManager;

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
            // Register GURL to receive AppleEvents, such as magnet links.
            GURLHandler.getInstance().register();
            // Register Cocoa handlers for the menus.
            if(CommonUtils.isJava14OrLater())
                CocoaApplicationEventHandler.instance().register();
            // Raise the number of allowed concurrent open files to 1024.
            SystemUtils.setOpenFileLimit(1024);
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
            Thread.yield();
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
        if (args.length > 0 && !args[0].equals("-startup")) {
            arg = ExternalControl.preprocessArgs(args);
            ExternalControl.checkForActiveLimeWire(arg);
            ExternalControl.enqueueMagnetRequest(arg);
        } else if (!StartupSettings.ALLOW_MULTIPLE_INSTANCES.getValue()) {
            // if we don't want multiple instances, we need to check if
            // limewire is already active.
            ExternalControl.checkForActiveLimeWire();
        }
        
        Initializer.setOSXSystemProperties();
        
        ResourceManager.instance();
        
        // Show the splash screen if we're not starting automatically on 
        // system startup
        SplashWindow splash = null;
        if(!isStartup) {
            splash = new SplashWindow();
            
            SplashWindow.setStatusText(
                GUIMediator.getStringResource("SPLASH_STATUS_USER_SETTINGS"));
            
            // TODO: Initialize SettingsHandler things?
            
            SplashWindow.setStatusText(
                GUIMediator.getStringResource("SPLASH_STATUS_SHARED_FILES"));
        }
        
        //Initialize the bug manager
        //(make sure the BugSettings class can be loaded first)
        BugManager.instance();
        
        // Run through the initialization sequence -- this must always be
        // called before GUIMediator constructs the LibraryTree!
        new SetupManager().createIfNeeded();
        
        // Make sure the save directory is valid.
        SaveDirectoryHandler.handleSaveDirectory();
        
        SplashWindow.setStatusText(
            GUIMediator.getStringResource("SPLASH_STATUS_INTERFACE"));
        
        // Construct the frontend
        GUIMediator mediator = GUIMediator.instance();
        
        SplashWindow.setStatusText(
            GUIMediator.getStringResource("SPLASH_STATUS_CORE_COMPONENTS"));
        
        // Construct gui callback class
        ActivityCallback ac = new VisualConnectionCallback();
        
        // Construct the RouterService object, which functions as the
        // backend initializer as well as the interface from the GUI to the
        // front end.
        RouterService routerService = new RouterService(ac);
        
        // Notify GUIMediator of the RouterService interface class to the
        // backend.
        mediator.setRouterService(routerService);
        
        // Touch the I18N stuff to ensure it loads properly.
        I18NConvert.instance();
        
        // Start the backend threads.  Note that the GUI is not yet visible,
        // but it needs to be constructed at this point  
        routerService.start();
        
        // Instruct the gui to perform tasks that can only be performed
        // after the backend has been constructed.
        mediator.startTimer();
        
        // Create the user desktop notifier object.
        // This must be done before the GUI is made visible,
        // otherwise the user can close it and not see the
        // tray icon.
        NotifyUserProxy notifyProxy = NotifyUserProxy.instance();
        // Hide the notifier after it has been initialized.
        notifyProxy.hideNotify();
        
        // Hide the splash screen and recycle its memory.
        if(splash != null) {
            splash.dispose();
        }
        
        GUIMediator.allowVisibility();
        
        // Make the GUI visible.
        if(!isStartup) {
            GUIMediator.setAppVisible(true);
        } else {
            GUIMediator.startupHidden();
        }
        
        // Activate a download for magnet URL locally if one exists
        ExternalControl.runQueuedMagnetRequest();
		
		if (CommonUtils.isJava14OrLater() && 
			com.limegroup.gnutella.settings.iTunesSettings.DAAP_SUPPORT_ENABLED.getValue()) {
			
			try {
				com.limegroup.gnutella.gui.DaapMediator.instance().startServer();
				com.limegroup.gnutella.gui.DaapMediator.instance().registerService();
			} catch (java.io.IOException err) {
				ErrorService.error(err);
			}
		}
    }
    
    /**
     * Sets the startup property to be true.
     */
    static void setStartup() {
        isStartup = true;
    }
    
    /**
     * Uses reflection to set system properties on OS X.  Reflection is
     * necessary because the static System.setProperty(String, String) method
     * is a Java2 method.
     */
    static void setOSXSystemProperties() {
        if (!CommonUtils.isMacOSX())
            return;
        
        try {
            Method setPropertyMethod = System.class.getDeclaredMethod(
                "setProperty",
                new Class[] { String.class, String.class });
            if (CommonUtils.isJava14OrLater()) {
                UIManager.put("ProgressBar.repaintInterval", new Integer(500));
                setPropertyMethod.invoke(null, new String[] {
                    "apple.laf.useScreenMenuBar", "true"});
            } else {
                setPropertyMethod.invoke(null, new String[] {
                    "com.apple.macos.useScreenMenuBar", "true"});
                setPropertyMethod.invoke(null, new String[] {
                    "com.apple.mrj.application.apple.menu.about.name",
                    "LimeWire"});
                setPropertyMethod.invoke(null, new String[] {
                    "com.apple.macos.use-file-dialog-packages", "true"});
            }
        } catch (IllegalAccessException e1) {
            // nothing we can do
        } catch (InvocationTargetException e1) {
            // nothing we can do
        } catch (SecurityException e) {
            // nothing we can do
        } catch (NoSuchMethodException e) {
            // nothing we can do
        }
        
        // any other exception is unexpected and will be propagated to
        // ErrorService
    }
}
