package com.limegroup.gnutella.gui.options.panes;

import java.io.IOException;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JCheckBox;
import javax.swing.JTextField;
import javax.swing.JRadioButton;
import javax.swing.ButtonGroup;
import com.limegroup.gnutella.gui.SizedTextField;

import com.limegroup.gnutella.gui.LabeledComponent;
import com.limegroup.gnutella.settings.DaapSettings;
import com.limegroup.gnutella.gui.DaapMediator;

public final class DaapSupportPaneItem extends AbstractPaneItem {

    
    private final String DAAP_ENABLED_LABEL = 
            "OPTIONS_ITUNES_DAAP_SUPPORT_DAAP_ENABLED_LABEL";

    private final String USE_BIO_LABEL =
            "OPTIONS_ITUNES_DAAP_SUPPORT_USE_BIO_LABEL";
    
    private final String USE_NIO_LABEL =
            "OPTIONS_ITUNES_DAAP_SUPPORT_USE_NIO_LABEL";
    
    private final String SERVICE_NAME_LABEL = 
            "OPTIONS_ITUNES_DAAP_SUPPORT_SERVICE_NAME_LABEL";


    private final JCheckBox DAAP_ENABLED = new JCheckBox();

    private final JRadioButton USE_BIO = new JRadioButton();
    private final JRadioButton USE_NIO = new JRadioButton();

    private final JTextField SERVICE_NAME = new SizedTextField();

    /**
     * The constructor constructs all of the elements of this 
     * <tt>AbstractPaneItem</tt>.
     *
     * @param key the key for this <tt>AbstractPaneItem</tt> that the
     *            superclass uses to generate locale-specific keys
     */
    public DaapSupportPaneItem(final String key) {
        super(key);
        
        LabeledComponent comp = new LabeledComponent(DAAP_ENABLED_LABEL, DAAP_ENABLED,
            LabeledComponent.LEFT_GLUE);
        add(comp.getComponent());

        ButtonGroup group = new ButtonGroup();
        group.add(USE_BIO);
        group.add(USE_NIO);
        
        comp = new LabeledComponent(USE_BIO_LABEL, USE_BIO,
            LabeledComponent.LEFT_GLUE);
        add(comp.getComponent());
        
        comp = new LabeledComponent(USE_NIO_LABEL, USE_NIO,
            LabeledComponent.LEFT_GLUE);
        add(comp.getComponent());
        
        comp = new LabeledComponent(SERVICE_NAME_LABEL, SERVICE_NAME,
            LabeledComponent.RIGHT_GLUE);
        
        add(comp.getComponent());
    }
    
    /**
     * Defines the abstract method in <tt>AbstractPaneItem</tt>.<p>
     *
     * Sets the options for the fields in this <tt>PaneItem</tt> when the 
     * window is shown.
     */
    public void initOptions() {
        DAAP_ENABLED.setSelected(DaapSettings.DAAP_ENABLED.getValue() && 
                    DaapMediator.instance().isServerRunning());
        
        USE_NIO.setSelected(DaapSettings.DAAP_USE_NIO.getValue());
        USE_BIO.setSelected(!USE_NIO.isSelected());
        
        SERVICE_NAME.setText(DaapSettings.DAAP_SERVICE_NAME.getValue());
    }
    
    /**
     * Defines the abstract method in <tt>AbstractPaneItem</tt>.<p>
     *
     * Applies the options currently set in this window, displaying an
     * error message to the user if a setting could not be applied.
     *
     * @throws IOException if the options could not be applied for some reason
     */
    public boolean applyOptions() throws IOException {

        final boolean prevEnabled = DaapSettings.DAAP_ENABLED.getValue();
        final boolean prevUseNIO = DaapSettings.DAAP_USE_NIO.getValue();
        final String prevServiceName = DaapSettings.DAAP_SERVICE_NAME.getValue();

        String serviceName = SERVICE_NAME.getText().trim();

        if (serviceName.length()==0 && DAAP_ENABLED.isSelected()) { 
            throw new IOException(); 
        }

        DaapSettings.DAAP_ENABLED.setValue(DAAP_ENABLED.isSelected());
        DaapSettings.DAAP_USE_NIO.setValue(USE_NIO.isSelected());
        DaapSettings.DAAP_SERVICE_NAME.setValue(serviceName);
        DaapSettings.DAAP_LIBRARY_NAME.setValue(serviceName);

        try {
            
            if (DAAP_ENABLED.isSelected()) {
                
                if (!prevEnabled || USE_NIO.isSelected() != prevUseNIO) {
                    DaapMediator.instance().restart();
                   
                } else if (!serviceName.equals(prevServiceName)) {
                    DaapMediator.instance().updateService();
                }
                    
            } else if (prevEnabled) {
                
                DaapMediator.instance().stop();
            }

        } catch (IOException err) {

            DaapSettings.DAAP_ENABLED.setValue(prevEnabled);
            DaapSettings.DAAP_USE_NIO.setValue(prevUseNIO);
            DaapSettings.DAAP_SERVICE_NAME.setValue(prevServiceName);
            DaapSettings.DAAP_LIBRARY_NAME.setValue(prevServiceName);

            DaapMediator.instance().stop();

            initOptions();

            throw err;
        }

        return false;
    }
}
