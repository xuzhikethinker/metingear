/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.ebi.mnb.view;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.SwingUtilities;
import junit.framework.TestCase;
import org.junit.Test;
import uk.ac.ebi.chemet.entities.reaction.Reversibility;
import uk.ac.ebi.core.Compartment;
import uk.ac.ebi.core.MetabolicReaction;
import uk.ac.ebi.core.Metabolite;
import uk.ac.ebi.core.reaction.MetaboliteParticipant;
import uk.ac.ebi.resource.chemical.BasicChemicalIdentifier;
import uk.ac.ebi.resource.reaction.BasicReactionIdentifier;

/**
 *
 * @author johnmay
 */
public class ReactionRendererTest {

    @Test
    public void testRenderUniporterReaction() throws IOException {

        Metabolite atp = new Metabolite(BasicChemicalIdentifier.nextIdentifier(), "atp", "ATP");
        Metabolite alanine = new Metabolite(BasicChemicalIdentifier.nextIdentifier(), "dala", "D-Alanine");

        final MetabolicReaction rxn = new MetabolicReaction(BasicReactionIdentifier.nextIdentifier(), "up",
                                                            "uniportTest");

        rxn.addReactant(new MetaboliteParticipant(atp, Compartment.CYTOPLASM));
        rxn.addReactant(new MetaboliteParticipant(alanine, Compartment.EXTRACELLULA));

        rxn.addProduct(new MetaboliteParticipant(atp, Compartment.CYTOPLASM));
        rxn.addProduct(new MetaboliteParticipant(alanine, Compartment.CYTOPLASM));

        rxn.setReversibility(Reversibility.IRREVERSIBLE_LEFT_TO_RIGHT);

        final ReactionRenderer renderer = new ReactionRenderer();


        File f = File.createTempFile("testImage", ".png");
        ImageIO.write((BufferedImage) renderer.renderTransportReaction(rxn).getImage(), "png", f);
        System.out.println(f);

    }

    public void testGetReaction() {
    }

    public void testDrawMolecule() {
    }

    public void testDrawPlus() {
    }

    public void testDrawArrow() {
    }

    public void testMain() throws Exception {
    }
}