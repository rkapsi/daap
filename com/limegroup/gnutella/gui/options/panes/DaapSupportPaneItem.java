package com.limegroup.gnutella.gui.options.panes;

import java.io.IOException;

import javax.swing.JCheckBox;
import javax.swing.JTextField;
import com.limegroup.gnutella.gui.SizedTextField;

import com.limegroup.gnutella.gui.LabeledComponent;
import com.limegroup.gnutella.settings.iTunesSettings;
import com.limegroup.gnutella.gui.DaapMediator;

public final class DaapSupportPaneItem extends AbstractPaneItem {

    
	private final String CHECK_BOX_LABEL = 
		"OPTIONS_ITUNES_DAAP_SUPPORT_CHECKBOX_LABEL";
	
	private final String TEXTFIELD_BOX_LABEL = 
		"OPTIONS_ITUNES_DAAP_SUPPORT_SHARED_LABEL";

	/**
	 * Constant for the check box that specifies whether or not downloads 
	 * should be automatically cleared.
	 */
	private final JCheckBox CHECK_BOX = new JCheckBox();
	
	private final JTextField TEXT_FIELD = new SizedTextField();
	
	/**
	 * The constructor constructs all of the elements of this 
	 * <tt>AbstractPaneItem</tt>.
	 *
	 * @param key the key for this <tt>AbstractPaneItem</tt> that the
	 *            superclass uses to generate locale-specific keys
	 */
	public DaapSupportPaneItem(final String key) {
		super(key);
		LabeledComponent comp = new LabeledComponent(CHECK_BOX_LABEL,
													 CHECK_BOX,
													 LabeledComponent.LEFT_GLUE);
		add(comp.getComponent());
		
							comp = new LabeledComponent(TEXTFIELD_BOX_LABEL,
													 TEXT_FIELD,
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
        CHECK_BOX.setSelected(iTunesSettings.DAAP_SUPPORT_ENABLED.getValue());
		TEXT_FIELD.setText(iTunesSettings.DAAP_SERVICE_NAME.getValue());
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
        
		boolean enabledChanged = CHECK_BOX.isSelected() != iTunesSettings.DAAP_SUPPORT_ENABLED.getValue();
        
        String text = TEXT_FIELD.getText().trim();
		boolean textChanged = text.equals(iTunesSettings.DAAP_SERVICE_NAME.getValue()) == false;
        
		if (text.length()==0 && CHECK_BOX.isSelected()) { 
			throw new IOException(); 
		}
		
        iTunesSettings.DAAP_SUPPORT_ENABLED.setValue(CHECK_BOX.isSelected());
        iTunesSettings.DAAP_SERVICE_NAME.setValue(text);
        iTunesSettings.DAAP_LIBRARY_NAME.setValue(text);
            
        if (enabledChanged) {
            
            if (iTunesSettings.DAAP_SUPPORT_ENABLED.getValue()) {
                DaapMediator.instance().start();
                DaapMediator.instance().init();
            } else {
                DaapMediator.instance().stop();
            }
            
        } else if (textChanged) {
            DaapMediator.instance().updateService();
        }
        
        return false;
	}
}
