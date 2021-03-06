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
package mnb.io.tabular.preparse;

import com.google.common.base.Joiner;
import java.util.ArrayList;
import java.util.Collection;
import mnb.io.tabular.type.EntityColumn;
import org.apache.log4j.Logger;
import uk.ac.ebi.mdk.domain.observation.Candidate;


/**
 *          PreparsedMetabolite – 2011.08.30 <br>
 *          Class to hold data of a metabolite entity prior to parsing. The class adds a collection
 *          of candidate entries which allow the parser to deside on the name
 * @version $Rev$ : Last Changed $Date$
 * @author  johnmay
 * @author  $Author$ (this version)
 */
public class PreparsedMetabolite extends PreparsedEntry {

    private static final Logger LOGGER = Logger.getLogger(PreparsedMetabolite.class);

    private Collection<Candidate> candidates = new ArrayList<Candidate>();


    public PreparsedMetabolite() {
        super(EntityColumn.class);
    }


    public String getAbbreviation() {
        return getValue(EntityColumn.ABBREVIATION);
    }


    public String[] getNames() {
        return getValues(EntityColumn.DESCRIPTION);
    }


    public String[] getKEGGXREFs() {
        return getValues(EntityColumn.KEGG_XREF);
    }


    public String getFormula() {
        return getValue(EntityColumn.FORMULA);
    }


    public boolean addCandidate(Candidate candidate) {
        return candidates.add(candidate);
    }


    public String getCharge() {
        return getValue(EntityColumn.CHARGE);
    }


    @Override
    public String toString() {
        return Joiner.on(", ").join(getNames());
    }
}
