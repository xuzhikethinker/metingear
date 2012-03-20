package uk.ac.ebi.metingear.preference;

import org.apache.log4j.Logger;

import javax.swing.*;

/**
 * PreferenceFrame - 24.02.2012 <br/>
 * <p/>
 * Class descriptions.
 *
 * @author johnmay
 * @author $Author$ (this version)
 * @version $Rev$
 */
public class PreferenceFrame extends JFrame {

    private static final Logger LOGGER = Logger.getLogger(PreferenceFrame.class);
    
    public PreferenceFrame(){
        setContentPane(new PreferencePanel(this));
    }

}
