/*
 *     This file is part of Metabolic Network Builder
 * 
 *     Metabolic Network Builder is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU Lesser General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 * 
 *     Foobar is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 * 
 *     You should have received a copy of the GNU Lesser General Public License
 *     along with Foobar.  If not, see <http://www.gnu.org/licenses/>.
 */

package uk.ac.ebi.mnb.core;

import java.util.Properties;
import uk.ac.ebi.mnb.view.theme.DarkTheme;
import uk.ac.ebi.mnb.view.theme.DefaultTheme;
import uk.ac.ebi.mnb.view.theme.Theme;


/**
 * ApplicationPreferences.java
 * Class to store the application properties
 *
 * @author johnmay
 * @date May 19, 2011
 */
public class ApplicationPreferences
  extends Properties {

    private static final org.apache.log4j.Logger LOGGER =
                                                 org.apache.log4j.Logger.getLogger(
      ApplicationPreferences.class);
    private static final String PROPERTIES_FILE = "preferences";


    private static class ApplicationPreferencesHolder {

        private static ApplicationPreferences INSTANCE = new ApplicationPreferences();
    }


    public static ApplicationPreferences getInstance() {
        return ApplicationPreferencesHolder.INSTANCE;
    }


    private ApplicationPreferences() {
        put(VIEW_TOOLBAR_INSPECTOR, true);
        put(VIEW_SOURCE_METABOLITE, "Name");
        put(VIEW_SOURCE_REACTION, "Accession");
        put(VIEW_SOURCE_PRODUCT, "Name");
        put(VIEW_SOURCE_TASK, "Name");
    }


    public Theme getTheme() {
        return theme;
    }


    private Theme theme = new DefaultTheme();
    public static final String FONT_SIZE = "font.size";
    public static final String FONT_NAME = "font.name";
    public static final String VIEW_TOOLBAR_INSPECTOR = "view.toolbar.inspector";
    public static final String VIEW_SOURCE_METABOLITE = "view.source.metabolite"; // display accession, name or abbreviation?
    public static final String VIEW_SOURCE_REACTION = "view.source.reaction"; // display accession, name or abbreviation?
    public static final String VIEW_SOURCE_PRODUCT = "view.source.product"; // display accession, name or abbreviation?
    public static final String VIEW_SOURCE_TASK = "view.source.task"; // display accession, name or abbreviation?
}

