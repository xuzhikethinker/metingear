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
package uk.ac.ebi.mnb.menu.file;

import org.apache.log4j.Logger;
import uk.ac.ebi.mdk.domain.entity.DefaultEntityFactory;
import uk.ac.ebi.mdk.domain.entity.Gene;
import uk.ac.ebi.mdk.domain.entity.GeneProduct;
import uk.ac.ebi.mdk.domain.entity.Reconstruction;
import uk.ac.ebi.mdk.domain.entity.collection.Chromosome;
import uk.ac.ebi.mdk.domain.entity.collection.DefaultReconstructionManager;
import uk.ac.ebi.mdk.domain.entity.collection.ReconstructionManager;
import uk.ac.ebi.mdk.io.xml.ena.ENAXMLReader;
import uk.ac.ebi.mnb.core.ErrorMessage;
import uk.ac.ebi.mnb.core.FileChooserAction;
import uk.ac.ebi.mnb.main.MainView;

import javax.swing.*;
import javax.xml.stream.XMLStreamException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.List;
import java.util.Map;

/**
 * NewFromENA - 2011.10.17 <br> Imports ENA XML format genome/proteome
 *
 * @author johnmay
 * @author $Author$ (this version)
 * @version $Rev$ : Last Changed $Date$
 */
public class ImportENAXML extends FileChooserAction {

    private static final Logger LOGGER = Logger.getLogger(ImportENAXML.class);

    public ImportENAXML() {
        super(ImportENAXML.class.getSimpleName());
    }

    @Override
    public void activateActions() {

        getChooser().setFileSelectionMode(JFileChooser.FILES_ONLY);
        File choosenFile = getFile(showOpenDialog());

        if (choosenFile != null) {
            try {
                ENAXMLReader reader = new ENAXMLReader(DefaultEntityFactory.getInstance(), new FileInputStream(choosenFile));

                List<GeneProduct> products = reader.getProducts();

                ReconstructionManager manager = DefaultReconstructionManager.getInstance();
                Reconstruction recon = manager.active();
                for(GeneProduct p : products){
                    recon.addProduct(p);
                }

                // bit of a hack for now - add a single chromosome
                Chromosome c = recon.getGenome().chromosome(1);
                List<Gene> genes = reader.getGeneMap();
                for (Gene gene : genes) {
                    c.add(gene);
                }


                for(Map.Entry<Gene,GeneProduct> e : reader.associations()){
                    recon.associate(e.getKey(), e.getValue());
                }

                for (String warning : reader.getWarnings()) {
                    MainView.getInstance()
                            .addWarningMessage(warning);
                }

                MainView.getInstance().update();

            } catch (FileNotFoundException ex) {
                MainView.getInstance()
                        .getMessageManager()
                        .addReport(new ErrorMessage("File not found " + ex.getMessage()));
            } catch (XMLStreamException ex) {
                MainView.getInstance()
                        .getMessageManager()
                        .addReport(new ErrorMessage(ex.getMessage()));
            }
        }

    }
}
