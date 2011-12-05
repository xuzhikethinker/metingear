/**
 * SimulationUtil.java
 *
 * 2011.12.02
 *
 * This file is part of the CheMet library
 * 
 * The CheMet library is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * CheMet is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with CheMet.  If not, see <http://www.gnu.org/licenses/>.
 */
package uk.ac.ebi.optimise;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;
import java.util.prefs.Preferences;
import org.apache.log4j.Logger;

/**
 *          SimulationUtil - 2011.12.02 <br>
 *          Class provides utility methods for the simulation module
 * @version $Rev$ : Last Changed $Date$
 * @author  johnmay
 * @author  $Author$ (this version)
 */
public class SimulationUtil {

    private static final Logger LOGGER = Logger.getLogger(SimulationUtil.class);

    /**
     * Adds the CPLEX library path specified in the user preferences {@see setCPLEXLibraryPath(String)}
     */
    public static void setup() {

        String path = Preferences.userNodeForPackage(SimulationUtil.class).get("cplex.library.path", null);

        List<String> paths = Arrays.asList(System.getProperty("java.library.path").split(File.pathSeparator));

        if (path != null && !paths.contains(path)) {

            try {
                addLibraryPath(path);
            } catch (IOException ex) {
                LOGGER.error("Unable to add CPLEX library path " + path + " to 'java.libary.path'");
            }

        }

    }

    /**
     * Sets the CPLEX library path in the preferences and calls {@see setup()} adding this to the
     * system property library paths. Note: No paths are not removed
     */
    public static void setCPLEXLibraryPath(String path) {
        Preferences.userNodeForPackage(SimulationUtil.class).put("cplex.library.path", path);
        setup();
    }

    /**
     * Adds a path to the system property java.library.path at runtime
     * @param s
     * @throws IOException
     */
    private static void addLibraryPath(String s) throws IOException {
        try {
            // This enables the java.library.path to be modified at runtime
            // From a Sun engineer at http://forums.sun.com/thread.jspa?threadID=707176
            //
            Field field = ClassLoader.class.getDeclaredField("usr_paths");
            field.setAccessible(true);
            String[] paths = (String[]) field.get(null);
            for (int i = 0; i < paths.length; i++) {
                if (s.equals(paths[i])) {
                    return;
                }
            }
            String[] tmp = new String[paths.length + 1];
            System.arraycopy(paths, 0, tmp, 0, paths.length);
            tmp[paths.length] = s;
            field.set(null, tmp);

        } catch (IllegalAccessException e) {
            throw new IOException("Failed to get permissions to set library path");
        } catch (NoSuchFieldException e) {
            throw new IOException("Failed to get field handle to set library path");
        }
    }
}