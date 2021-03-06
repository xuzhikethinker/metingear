/*
 * Copyright (c) 2013. EMBL, European Bioinformatics Institute
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package mnb.io.tabular;

import mnb.io.resolve.EntryReconciler;
import mnb.io.tabular.parser.ReactionParser;
import mnb.io.tabular.preparse.PreparsedMetabolite;
import mnb.io.tabular.preparse.PreparsedSheet;
import org.apache.log4j.Logger;
import uk.ac.ebi.mdk.domain.identifier.basic.BasicChemicalIdentifier;
import uk.ac.ebi.mdk.domain.entity.Metabolite;
import uk.ac.ebi.mdk.domain.entity.EntityFactory;

import java.util.*;
import java.util.regex.Matcher;


/**
 *          EntityResolver – 2011.08.29 <br>
 *          Class description
 * @version $Rev$ : Last Changed $Date$
 * @author  johnmay
 * @author  $Author$ (this version)
 */
public class ExcelEntityResolver implements EntityResolver {

    private static final Logger LOGGER = Logger.getLogger(ExcelEntityResolver.class);

    private List<PreparsedMetabolite> entities = new ArrayList();

    private Map<String, PreparsedMetabolite> entityMap = new HashMap(); // abbreviation -> entitiy

    private Map<String, Metabolite> nonReconciled = new HashMap();

    private Properties p;

    private PreparsedSheet sheet;

    private EntryReconciler reconciler;

    private EntityFactory factory;


    public ExcelEntityResolver(PreparsedSheet sheet,
                               EntryReconciler reconciler,
                               EntityFactory factory) {
        this.sheet = sheet;
        this.reconciler = reconciler;
        this.factory = factory;
        load();
    }


    private final void load() {

        double found = 0;
        double total = 0;

        sheet.reset();
        while (sheet.hasNext()) {
            PreparsedMetabolite entity = (PreparsedMetabolite) sheet.next();

            Matcher compartment =
                    ReactionParser.COMPARTMENT_PATTERN.matcher(entity.getAbbreviation());

            // if contains a compartment identifier remove it
            if (compartment.find()) {
                String abbreviation = compartment.replaceAll("");
                // only put if we don't have the slot already
                if (!entityMap.containsKey(abbreviation.trim())) {
                    entityMap.put(abbreviation.trim(), entity);
                }
            } else {
                entityMap.put(entity.getAbbreviation().trim(), entity);
            }

        }
        sheet.reset();
    }


    public PreparsedMetabolite getEntity(String abbreviation) {
        return entityMap.get(abbreviation);
    }


    public Metabolite getNonReconciledMetabolite(String name) {

        if (nonReconciled.containsKey(name)) {
            return nonReconciled.get(name);
        }

        Metabolite m = factory.newInstance(Metabolite.class,
                                           BasicChemicalIdentifier.nextIdentifier(),
                                           name,
                                           name);

        nonReconciled.put(name, m);

        return m;
    }


    /**
     * 
     * @param abbreviation
     * @return
     */
    public Metabolite getReconciledMetabolite(String abbreviation) {

        // new resolution
        if (resolved.containsKey(abbreviation) == false) {
            Metabolite entry = (Metabolite) reconciler.resolve(getEntity(abbreviation));
            resolved.put(abbreviation, entry);
        }

        return resolved.get(abbreviation);

    }

    private Map<String, Metabolite> resolved = new HashMap<String, Metabolite>();
}
