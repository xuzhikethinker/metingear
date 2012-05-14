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
import net.sf.furbelow.SpinningDialWaitIndicator;
import org.apache.log4j.Logger;
import uk.ac.ebi.caf.component.factory.CheckBoxFactory;
import uk.ac.ebi.caf.component.factory.LabelFactory;
import uk.ac.ebi.caf.component.factory.PanelFactory;
import uk.ac.ebi.caf.report.ReportManager;
import uk.ac.ebi.caf.utility.TextUtility;
import uk.ac.ebi.mdk.domain.entity.Metabolite;
import uk.ac.ebi.mdk.domain.identifier.Identifier;
import uk.ac.ebi.mdk.service.DefaultServiceManager;
import uk.ac.ebi.mdk.service.ServiceManager;
import uk.ac.ebi.mdk.service.query.QueryService;
import uk.ac.ebi.mdk.service.query.name.NameService;
import uk.ac.ebi.mdk.ui.component.ResourceList;
import uk.ac.ebi.metabolomes.webservices.util.CandidateEntry;
import uk.ac.ebi.metabolomes.webservices.util.CandidateFactory;
import uk.ac.ebi.mnb.core.ControllerDialog;
import uk.ac.ebi.mnb.interfaces.SelectionController;
import uk.ac.ebi.mnb.interfaces.TargetedUpdate;
import uk.ac.ebi.reconciliation.ChemicalFingerprintEncoder;

import javax.swing.*;
import javax.swing.event.UndoableEditListener;
import java.util.ArrayList;
import java.util.List;


/**
 * AutomaticCrossReferenceDialog – 2011.09.30 <br>
 * Class description
 *
 * @author johnmay
 * @author $Author$ (this version)
 * @version $Rev$ : Last Changed $Date$
 */
public class AutomaticCrossReference
        extends ControllerDialog {

    private static final Logger LOGGER = Logger.getLogger(AutomaticCrossReference.class);

    private JCheckBox chebi = CheckBoxFactory.newCheckBox("ChEBI");

    private JCheckBox kegg = CheckBoxFactory.newCheckBox("KEGG Compound");
    private JCheckBox hmdb = CheckBoxFactory.newCheckBox("HMDB");

    private JLabel wsLabel = LabelFactory.newFormLabel("Allow web-services:", "Dramatic performance reduction on large data-sets");
    private JLabel greedyLabel = LabelFactory.newFormLabel("Greedy Mode:",
                                                           "Keep searching for hits even once a hit has been found");
    private JCheckBox ws = CheckBoxFactory.newCheckBox();
    private JCheckBox greedy = CheckBoxFactory.newCheckBox();

    private JLabel approximateLabel = LabelFactory.newFormLabel("Approximate match:",
                                                                TextUtility.html("Uses approximate word matching when searching. Only " +
                                                                                         "names with '0' differences will be transferred but <br>" +
                                                                                         "using this method may yield new matches. Note: this method " +
                                                                                         "vastly reduces speed of the search "));
    private JLabel resourceLabel = LabelFactory.newFormLabel("Resource priority:");
    private JCheckBox approximate = CheckBoxFactory.newCheckBox();
    private ResourceList resourceSelection = new ResourceList();

    private JSpinner results = new JSpinner(new SpinnerNumberModel(50, 10, 200, 10));


    public AutomaticCrossReference(JFrame frame, TargetedUpdate updater, ReportManager messages, SelectionController controller, UndoableEditListener undoableEdits) {

        super(frame, updater, messages, controller, undoableEdits, "RunDialog");


        setDefaultLayout();


    }


    @Override
    public JLabel getDescription() {
        JLabel label = super.getDescription();
        label.setText("Match name(s) to metabolic databases");
        return label;
    }


    @Override
    public JPanel getForm() {

        JPanel form = PanelFactory.createDialogPanel();

        CellConstraints cc = new CellConstraints();

        form.setLayout(new FormLayout("right:p, 4dlu, left:p:grow",
                                      "p, 4dlu, top:p, 4dlu, p, 4dlu, p"));

        form.add(approximateLabel, cc.xy(1, 1));
        form.add(approximate, cc.xy(3, 1));
        form.add(resourceLabel, cc.xy(1, 3));
        form.add(resourceSelection, cc.xy(3, 3));
        form.add(wsLabel, cc.xy(1, 5));
        form.add(ws, cc.xy(3, 5));
        form.add(greedyLabel, cc.xy(1, 7));
        form.add(greedy, cc.xy(3, 7));


        //        JLabel label = LabelFactory.newFormLabel("Method", TextUtility.html("The method to use for name matching, Generally they<br> aim to improve recall at the cost of precision"));
        //        options.add(label, cc.xy(1, 7));
        //        options.add(ComboBoxFactory.newComboBox("Direct", "Fingerprint", "N-gram"), cc.xy(3, 7));

        return form;
    }


    @Override
    public void setVisible(boolean b) {
        if (b) {

            // should be in a setup() method
            resourceSelection.getModel().clear();
            ServiceManager services = DefaultServiceManager.getInstance();
            for (Identifier identifier : services.getIdentifiers(NameService.class)) {
                // check it's actually available
                if(services.hasService(identifier, NameService.class))
                    resourceSelection.addElement(identifier);
            }
            pack();
        }
        super.setVisible(b);

    }

    @Override
    public void process() {
        // not used
    }

    @Override
    public void process(final SpinningDialWaitIndicator indicator) {

        List<Metabolite> metabolites = new ArrayList<Metabolite>(getSelection().get(Metabolite.class));

        List<CandidateFactory> factories = new ArrayList<CandidateFactory>();

        // build the candidate factories
        for (Identifier identifier : resourceSelection.getElements()) {
            NameService<?> service = DefaultServiceManager.getInstance().getService(identifier,
                                                                                    NameService.class);
            if (canUse(service)) {
                factories.add(new CandidateFactory(service,
                                                   new ChemicalFingerprintEncoder()));
            }
        }


        for (int i = 0; i < metabolites.size(); i++) {

            Metabolite m = metabolites.get(i);
            final double progress = (double) i / metabolites.size();

            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    indicator.setText(String.format("Searching... %.1f %%", progress * 100));
                }
            });

            for (CandidateFactory factory : factories) {
                Multimap<Integer, CandidateEntry> map = approximate.isSelected()
                        ? factory.getFuzzySynonymCandidates(m.getName())
                        : factory.getSynonymCandidates(m.getName());
                if (map.containsKey(0)) {
                    for (CandidateEntry entry : map.get(0)) {
                        m.addAnnotation(factory.getCrossReference(entry));
                    }
                    if (!greedy.isSelected())
                        break;
                }
                map.clear();
                map = null;
            }
        }

        factories = null; // for cleanup
        System.gc();

    }

    public boolean canUse(QueryService service) {
        QueryService.ServiceType type = service.getServiceType();
        return ws.isSelected()
                || !type.equals(QueryService.ServiceType.SOAP_WEB_SERVICE)
                && !type.equals(QueryService.ServiceType.REST_WEB_SERVICE);
    }

    @Override
    public boolean update() {
        return update(getSelection());
    }
}
