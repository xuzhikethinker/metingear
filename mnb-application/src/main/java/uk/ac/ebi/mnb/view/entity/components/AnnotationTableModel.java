/**
 * AnnotationTableModel.java
 *
 * 2011.12.13
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
package uk.ac.ebi.mnb.view.entity.components;

import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import javax.swing.Action;
import javax.swing.DefaultListModel;
import javax.swing.JList;
import javax.swing.table.AbstractTableModel;
import javax.swing.undo.UndoManager;
import uk.ac.ebi.interfaces.AnnotatedEntity;
import uk.ac.ebi.interfaces.Annotation;
import uk.ac.ebi.interfaces.Observation;
import uk.ac.ebi.interfaces.annotation.ObservationBasedAnnotation;
import uk.ac.ebi.mnb.core.GeneralAction;
import uk.ac.ebi.mnb.edit.DeleteAnnotation;
import uk.ac.ebi.mnb.interfaces.EntityView;
import uk.ac.ebi.mnb.main.MainView;

/**
 *          AnnotationTableModel - 2011.12.13 <br>
 *          Class description
 * @version $Rev$ : Last Changed $Date$
 * @author  johnmay
 * @author  $Author$ (this version)
 */
public class AnnotationTableModel
        extends AbstractTableModel {

    private AnnotatedEntity entity;
    private List<Annotation> annotations;
    private Class[] columns = new Class[]{Annotation.class, Annotation.class, Action.class, Action.class};
    private JList observationList;

    public AnnotationTableModel() {
        annotations = new ArrayList<Annotation>();

    }

    public void setObservationList(JList observationList) {
        this.observationList = observationList;
    }

    public AnnotatedEntity getEntity() {
        return entity;
    }

    public void setEntity(AnnotatedEntity entity) {
        this.entity = entity;
        if (entity != null) {
            setAnnotations(entity.getAnnotations());
        } else {
            annotations.clear();
        }
    }

    private void setAnnotations(Collection<Annotation> annotations) {
        this.annotations.clear();
        this.annotations.addAll(annotations);
    }

    @Override
    public void fireTableDataChanged() {
       setEntity(entity);
        super.fireTableDataChanged();
    }

    public Object getValueAt(int row,
                             int column) {

        final Annotation annotation = annotations.get(row);

        switch (column) {
            case 0:
                return annotations.get(row);
            case 1:
                return annotations.get(row);
            case 2:
                return new DeleteAnnotation(entity,
                                            annotation,
                                            MainView.getInstance().getViewController().getActiveView(),
                                            MainView.getInstance().getUndoManager());
            case 3:
                if (annotation instanceof ObservationBasedAnnotation) {
                    final ObservationBasedAnnotation oAnn = (ObservationBasedAnnotation) annotation;
                    if (!oAnn.getObservations().isEmpty()) {
                        return new GeneralAction("ShowEvidence") {

                            public void actionPerformed(ActionEvent e) {

                                System.out.println("showing evidence");
                                observationList.removeSelectionInterval(0, observationList.getModel().getSize());

                                for (Observation observation : oAnn.getObservations()) {
                                    DefaultListModel model = (DefaultListModel) observationList.getModel();
                                    int index = model.indexOf(observation);
                                    if (index != -1) {
                                        observationList.addSelectionInterval(index, index);
                                    }
                                }
                            }
                        };
                    }
                }
                return null;
        }

        return null;

    }

    public int getRowCount() {
        return annotations.size();
    }

    public int getColumnCount() {
        return columns.length;
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
        return columns[columnIndex];
    }

    @Override
    public boolean isCellEditable(int rowIndex,
                                  int columnIndex) {
        return false;
    }

    @Override
    public void setValueAt(Object aValue,
                           int rowIndex,
                           int columnIndex) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void clear() {
        annotations.clear();
    }
}