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

package uk.ac.ebi.mnb.dialog.tools;

import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;
import net.sf.furbelow.SpinningDialWaitIndicator;
import org.apache.log4j.Logger;
import uk.ac.ebi.caf.component.factory.CheckBoxFactory;
import uk.ac.ebi.caf.component.factory.LabelFactory;
import uk.ac.ebi.caf.component.list.MutableJListController;
import uk.ac.ebi.caf.report.ReportManager;
import uk.ac.ebi.caf.utility.TextUtility;
import uk.ac.ebi.mdk.domain.annotation.Annotation;
import uk.ac.ebi.mdk.domain.annotation.DefaultAnnotationFactory;
import uk.ac.ebi.mdk.domain.entity.Metabolite;
import uk.ac.ebi.mdk.domain.identifier.AbstractChemicalIdentifier;
import uk.ac.ebi.mdk.domain.identifier.Identifier;
import uk.ac.ebi.mdk.domain.identifier.type.ChemicalIdentifier;
import uk.ac.ebi.mdk.domain.observation.Candidate;
import uk.ac.ebi.mdk.service.DefaultServiceManager;
import uk.ac.ebi.mdk.service.ServiceManager;
import uk.ac.ebi.mdk.service.query.QueryService;
import uk.ac.ebi.mdk.service.query.name.NameService;
import uk.ac.ebi.mdk.tool.resolve.ChemicalFingerprintEncoder;
import uk.ac.ebi.mdk.tool.resolve.NameCandidateFactory;
import uk.ac.ebi.mdk.ui.component.ResourceList;
import uk.ac.ebi.mnb.core.ControllerDialog;
import uk.ac.ebi.mnb.edit.AddAnnotationEdit;
import uk.ac.ebi.mnb.interfaces.SelectionController;
import uk.ac.ebi.mnb.interfaces.TargetedUpdate;

import javax.swing.*;
import javax.swing.event.UndoableEditListener;
import javax.swing.undo.CompoundEdit;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;


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


    private JLabel    wsLabel     = LabelFactory.newFormLabel("Allow Web services:", "Dramatic performance reduction on large data-sets");
    private JLabel    greedyLabel = LabelFactory.newFormLabel("Greedy Mode:",
                                                              "Keep searching for hits even once a hit has been found");
    private JCheckBox ws          = CheckBoxFactory.newCheckBox();
    private JCheckBox greedy      = CheckBoxFactory.newCheckBox();

    private JLabel       approximateLabel  = LabelFactory.newFormLabel("Approximate match:",
                                                                       TextUtility.html("Uses approximate word matching when searching. Only " +
                                                                                                "names with '0' differences will be transferred but <br>" +
                                                                                                "using this method may yield new matches. Note: this method " +
                                                                                                "vastly reduces speed of the search "));
    private JLabel       resourceLabel     = LabelFactory.newFormLabel("Resource priority:");
    private JCheckBox    approximate       = CheckBoxFactory.newCheckBox();
    private ResourceList resourceSelection = new ResourceList();

    public AutomaticCrossReference(JFrame frame, TargetedUpdate updater, ReportManager messages, SelectionController controller, UndoableEditListener undoableEdits) {

        super(frame, updater, messages, controller, undoableEdits, "RunDialog");


        // blend the list in
        resourceSelection.setBackground(getBackground());
        resourceSelection.setForeground(LabelFactory.newFormLabel("").getForeground());
        resourceSelection.setVisibleRowCount(6);

        setDefaultLayout();

        ws.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                updateResourceList();
            }
        });


    }


    @Override
    public JLabel getDescription() {
        JLabel label = super.getDescription();
        label.setText("Match name(s) to metabolic databases");
        return label;
    }


    @Override
    public JPanel getForm() {

        JPanel form = super.getForm();

        CellConstraints cc = new CellConstraints();

        form.setLayout(new FormLayout("right:p, 4dlu, left:p:grow",
                                      "p, 4dlu, top:p, 4dlu, p, 4dlu, p"));

        form.add(approximateLabel, cc.xy(1, 1));
        form.add(approximate, cc.xy(3, 1));
        form.add(resourceLabel, cc.xy(1, 3));
        form.add(new MutableJListController(resourceSelection).getListWithController(), cc.xy(3, 3));
        form.add(wsLabel, cc.xy(1, 5));
        form.add(ws, cc.xy(3, 5));
        form.add(greedyLabel, cc.xy(1, 7));
        form.add(greedy, cc.xy(3, 7));

        return form;
    }


    @Override
    public void setVisible(boolean b) {
        if (b) {
            updateResourceList();
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

        List<NameCandidateFactory> factories = new ArrayList<NameCandidateFactory>();

        // build the candidate factories
        for (Identifier identifier : resourceSelection.getElements()) {
            NameService<?> service = DefaultServiceManager.getInstance().getService(identifier,
                                                                                    NameService.class);
            if (isUsable(service) && isChemicalService(service)) {
                factories.add(new NameCandidateFactory(new ChemicalFingerprintEncoder(),
                                                       service));
            }
        }


        CompoundEdit edit = new CompoundEdit();

        for (int i = 0; i < metabolites.size(); i++) {

            Metabolite m = metabolites.get(i);
            final double progress = (double) i / metabolites.size();

            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    indicator.setText(String.format("Searching... %.1f %%", progress * 100));
                }
            });

            boolean found = false;

            for (NameCandidateFactory factory : factories) {

                Set<Candidate> candidates = factory.getCandidates(m.getName(), approximate.isSelected());


                for (Candidate candidate : candidates) {
                    if (candidate.getDistance() == 0) {
                        Annotation annotation = DefaultAnnotationFactory.getInstance().getCrossReference(candidate.getIdentifier());
                        edit.addEdit(new AddAnnotationEdit(m, annotation));
                        m.addAnnotation(annotation);
                        found = true;
                    }
                }

                if (found && !greedy.isSelected())
                    break;

            }
        }

        edit.end();
        addEdit(edit);

        factories = null; // for cleanup
        System.gc(); // only a suggestion ot the vm

    }

    public boolean isUsable(QueryService service) {
        QueryService.ServiceType type = service.getServiceType();
        return ws.isSelected() || !service.getServiceType().remote();
    }


    public boolean isChemicalService(QueryService service){
        return service.getIdentifier() instanceof ChemicalIdentifier;
    }

    private void updateResourceList() {
        // should be in a setup() method
        resourceSelection.getModel().clear();
        ServiceManager services = DefaultServiceManager.getInstance();
        for (Identifier identifier : services.getIdentifiers(NameService.class)) {
            // check it's actually available
            if (services.hasService(identifier, NameService.class)) {
                try{
                    QueryService service = services.getService(identifier, NameService.class);
                    if (isUsable(service) && isChemicalService(service)) {
                        resourceSelection.addElement(identifier);
                    }
                } catch (NoSuchElementException ex) {
                    // connection problems to ws
                }
            }
        }
        if (resourceSelection.getElements().size() > 1)
            resourceSelection.setSelectedIndex(0);

        pack();

    }

    @Override
    public boolean update() {
        return update(getSelection());
    }


}
