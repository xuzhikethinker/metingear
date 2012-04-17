/**
 * MergeLoci.java
 *
 * 2011.10.13
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
package uk.ac.ebi.mnb.dialog.tools;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import java.awt.event.ActionEvent;
import org.apache.log4j.Logger;
import uk.ac.ebi.annotation.Locus;
import uk.ac.ebi.core.MultimerImplementation;
import uk.ac.ebi.core.Reconstruction;
import uk.ac.ebi.core.ReconstructionManager;
import uk.ac.ebi.core.product.ProductCollection;
import uk.ac.ebi.core.reaction.ReactionList;
import uk.ac.ebi.interfaces.entities.GeneProduct;
import uk.ac.ebi.interfaces.entities.MetabolicReaction;
import uk.ac.ebi.mnb.core.ControllerAction;
import uk.ac.ebi.mnb.interfaces.MainController;


/**
 * @name    MergeLoci - 2011.10.13 <br>
 *          Class description
 * @version $Rev$ : Last Changed $Date$
 * @author  johnmay
 * @author  $Author$ (this version)
 */
public class MergeLoci extends ControllerAction {

    private static final Logger LOGGER = Logger.getLogger(MergeLoci.class);


    public MergeLoci(MainController controller) {
        super(MergeLoci.class.getSimpleName(), controller);
    }


    public void actionPerformed(ActionEvent e) {

        Reconstruction recon = ReconstructionManager.getInstance().getActive();

        Multimap<String, MetabolicReaction> monomeric = HashMultimap.create();
        Multimap<String, MetabolicReaction> multimeric = HashMultimap.create();

        ReactionList rxns = recon.getReactions();
        for (MetabolicReaction rxn : rxns) {
            for (Locus locus : rxn.getAnnotations(Locus.class)) {
                if (locus.containsMultiple()) {
                    multimeric.put(locus.toString(), rxn);
                } else {
                    monomeric.put(locus.toString(), rxn);
                }
            }
        }

        ProductCollection products = recon.getProducts();

        // monomeric
        for (String locus : monomeric.keySet()) {
            for (GeneProduct product : products.get(locus)) {
                for (MetabolicReaction rxn : monomeric.get(locus)) {
                    rxn.addModifier(product); // needs to be an add
                }
            }
        }

        // multimeric
        for (String locusAnnotation : multimeric.keySet()) {

            String[] loci = locusAnnotation.split("\\+");
            GeneProduct[] subunits = new GeneProduct[loci.length];
            for (int i = 0; i < loci.length; i++) {
                subunits[i] = products.get(loci[i]).iterator().next();
            }

            GeneProduct product = new MultimerImplementation(subunits);
            recon.getProducts().add(product);

            for (MetabolicReaction rxn : multimeric.get(locusAnnotation)) {
                rxn.addModifier(product);
            }
        }

        update();

    }
}