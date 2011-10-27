package uk.ac.ebi.mnb.dialog.tools;

/**
 * AutomaticCrossReferenceDialog.java
 *
 * 2011.09.30
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
import com.google.common.collect.Multimap;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import org.apache.log4j.Logger;
import uk.ac.ebi.chebi.webapps.chebiWS.model.StarsCategory;
import uk.ac.ebi.core.AbstractAnnotatedEntity;
import uk.ac.ebi.core.Metabolite;
import uk.ac.ebi.io.service.ChEBINameService;
import uk.ac.ebi.io.service.KEGGCompoundNameService;
import uk.ac.ebi.metabolomes.webservices.ChEBIWebServiceConnection;
import uk.ac.ebi.metabolomes.webservices.KeggCompoundWebServiceConnection;
import uk.ac.ebi.metabolomes.webservices.util.CandidateEntry;
import uk.ac.ebi.metabolomes.webservices.util.CandidateFactory;
import uk.ac.ebi.metabolomes.webservices.util.SynonymCandidateEntry;
import uk.ac.ebi.mnb.core.TooltipLabel;
import uk.ac.ebi.mnb.core.Utilities;
import uk.ac.ebi.mnb.interfaces.ViewController;
import uk.ac.ebi.mnb.view.DialogPanel;
import uk.ac.ebi.mnb.view.CheckBox;
import uk.ac.ebi.mnb.view.MComboBox;
import uk.ac.ebi.mnb.view.ContextDialog;
import uk.ac.ebi.reconciliation.ChemicalFingerprintEncoder;

/**
 *          AutomaticCrossReferenceDialog – 2011.09.30 <br>
 *          Class description
 * @version $Rev$ : Last Changed $Date$
 * @author  johnmay
 * @author  $Author$ (this version)
 */
public class AutomaticCrossReference
        extends ContextDialog {

    private static final Logger LOGGER = Logger.getLogger(AutomaticCrossReference.class);
    private JCheckBox chebi = new CheckBox("ChEBI");
    private JCheckBox kegg = new CheckBox("KEGG Compound");
    private JSpinner results = new JSpinner(new SpinnerNumberModel(50, 10, 200, 10));

    public AutomaticCrossReference(JFrame frame, ViewController controller) {

        super(frame, controller, "RunDialog");

        setDefaultLayout();

    }

    @Override
    public JLabel getDescription() {
        JLabel label = super.getDescription();
        label.setText("Match name(s) to chemical databases");
        return label;
    }

    @Override
    public JPanel getOptions() {

        JPanel options = new DialogPanel();

        CellConstraints cc = new CellConstraints();

        options.setLayout(new FormLayout("p, 4dlu, p", "p, 4dlu, p, 4dlu, p, 4dlu, p, 4dlu, p"));
        options.add(chebi, cc.xy(1, 1));
        options.add(kegg, cc.xy(3, 1));

        options.add(results, cc.xyw(1, 3, 3));

        options.add(new JSeparator(), cc.xyw(1, 5, 3));
        JLabel label = new TooltipLabel("Method", "<html>The method to use for name matching, Generally they<br> aim to improve recall at the cost of precision</html>", SwingConstants.RIGHT);
        options.add(label, cc.xy(1, 7));
        options.add(new MComboBox(Arrays.asList("Direct", "Fingerprint", "N-gram")), cc.xy(3, 7));

        return options;
    }

    @Override
    public void process() {
        Collection<Metabolite> metabolties = getSelection().get(Metabolite.class);

        boolean useChEBI = chebi.isSelected();
        boolean useKegg = kegg.isSelected();

        ChEBINameService.getInstance().setMaxResults((Integer) results.getValue());
        KEGGCompoundNameService.getInstance().setMaxResults((Integer) results.getValue());

        List<CandidateFactory> factories = new ArrayList<CandidateFactory>();
        if (useChEBI) {
            factories.add(new CandidateFactory(ChEBINameService.getInstance(), new ChemicalFingerprintEncoder()));
        }
        if (useKegg) {
            factories.add(new CandidateFactory(KEGGCompoundNameService.getInstance(), new ChemicalFingerprintEncoder()));
        }

        for (Metabolite metabolite : metabolties) {
            for (CandidateFactory factory : factories) {
                Multimap<Integer, CandidateEntry> map = factory.getSynonymCandidates(metabolite.getName());
                if (map.containsKey(0)) {
                    for (CandidateEntry entry : map.get(0)) {
                        metabolite.addAnnotation(factory.getCrossReference(entry));
                    }
                } else {
                    Multimap<Integer, SynonymCandidateEntry> synonymMap = factory.getFuzzySynonymCandidates(metabolite.getName());
                    if (synonymMap.containsKey(0)) {
                        for (CandidateEntry entry : synonymMap.get(0)) {
                            metabolite.addAnnotation(factory.getCrossReference(entry));
                        }
                    } else {
                        System.out.println(synonymMap);
                    }
                }
            }
        }

    }

    @Override
    public boolean update() {
        getController().update();
        return true;
    }
}
