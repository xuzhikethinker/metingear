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
package uk.ac.ebi.mnb.edit;

import uk.ac.ebi.mdk.domain.entity.AnnotatedEntity;
import uk.ac.ebi.mnb.interfaces.UndoableEntityEdit;

import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import java.util.Arrays;
import java.util.Collection;

/**
 * @name   NameEdit - 2011.10.02 <br>
 *          Allows undo/redo on abbreviation changing of an {@see AnnotatedEntity}
 * @version $Rev$ : Last Changed $Date$
 * @author  johnmay
 * @author  $Author$ (this version)
 */
public class AccessionEdit extends UndoableEntityEdit {

    private AnnotatedEntity entity;
    private String oldAccession;
    private String newAccession;

    public AccessionEdit(AnnotatedEntity entity, String newAccession) {
        this.entity = entity;
        this.newAccession = newAccession;
        this.oldAccession = entity.getAccession(); // todo use interface MetabolicEntity
    }

    @Override
    public void undo() throws CannotUndoException {
        super.undo();
        entity.getIdentifier().setAccession(oldAccession);
    }

    @Override
    public void redo() throws CannotRedoException {
        super.redo();
        entity.getIdentifier().setAccession(newAccession);
    }

    @Override
    public String getPresentationName() {
        return "Set name ";
    }   @Override
    public Collection<AnnotatedEntity> getEntities() {
        return Arrays.asList(entity);
    }
}
