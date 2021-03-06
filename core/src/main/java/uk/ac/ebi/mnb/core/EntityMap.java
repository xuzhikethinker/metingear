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
package uk.ac.ebi.mnb.core;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import uk.ac.ebi.mdk.domain.entity.*;
import uk.ac.ebi.mdk.domain.entity.collection.EntityCollection;
import uk.ac.ebi.mdk.domain.entity.EntityFactory;

import java.util.*;


/**
 * @name    SelectionMap - 2011.10.14 <br>
 *          Class effectively wraps a guava multimap to provide some convenience
 *          methods for handling exceptions
 * @version $Rev$ : Last Changed $Date$
 * @author  johnmay
 * @author  $Author$ (this version)
 */
public class EntityMap implements EntityCollection {

    private Multimap<Class, AnnotatedEntity> map = ArrayListMultimap.create();

    private EntityFactory factory;


    public static EntityCollection singleton(EntityFactory factory, AnnotatedEntity entity){
        EntityMap map = new EntityMap(factory);
        map.add(entity);
        return map;
    }

    public EntityMap(EntityFactory factory) {
        this.factory = factory;
    }


    /**
     * @inheritDoc
     */
    public Collection<AnnotatedEntity> getEntities() {
        return map.values();
    }


    /**
     * @inheritDoc
     */
    public boolean add(AnnotatedEntity entity) {
        return map.put(factory.getEntityClass(entity.getClass()), entity);
    }


    /**
     * @inheritDoc
     */
    @Override
    public boolean remove(AnnotatedEntity entity) {
        return map.remove(factory.getEntityClass(entity.getClass()), entity);
    }


    /**
     * @inheritDoc
     */
    public boolean addAll(Collection<? extends AnnotatedEntity> entities) {

        boolean changed = false;

        for (AnnotatedEntity entity : entities) {
            changed = add(entity) || changed;
        }

        return changed;

    }


    /**
     * @inheritDoc
     */
    public EntityCollection clear() {
        map.clear();
        return this;
    }


    /**
     * @inheritDoc
     */
    public <T> Collection<T> get(Class<T> type) {
        return (Collection<T>) map.get(type);
    }


    /**
     * @inheritDoc
     */
    public Collection<GeneProduct> getGeneProducts() {

        Collection<ProteinProduct> proteins = get(ProteinProduct.class);
        Collection<RibosomalRNA> rrna = get(RibosomalRNA.class);
        Collection<TransferRNA> trna = get(TransferRNA.class);
//        Collection<TransferRNA> multimer = get(Multimer.class);

        Collection<GeneProduct> products = new ArrayList();
        products.addAll(proteins);
        products.addAll(rrna);
        products.addAll(trna);

        return products;

    }


    /**
     * @inheritDoc
     */
    public boolean hasSelection() {
        return !map.isEmpty();
    }


    /**
     * @inheritDoc
     */
    public boolean hasSelection(Class<?> type) {
        return !map.get(type).isEmpty();
    }


    /**
     * @inheritDoc
     */
    public boolean isEmpty() {
        return map.isEmpty();
    }


    /**
     * @inheritDoc
     */
    public AnnotatedEntity getFirstEntity() {

        if (map.keySet().size() == 1) {
            return map.values().iterator().next();
        }

        List<Class<? extends AnnotatedEntity>> selections = new ArrayList(map.keySet());
        Collections.sort(selections, new Comparator<Class<? extends AnnotatedEntity>>() {

            public int compare(Class<? extends AnnotatedEntity> o1, Class<? extends AnnotatedEntity> o2) {
                Integer size1 = map.get(o1).size();
                Integer size2 = map.get(o2).size();
                return size1.compareTo(size2);
            }
        });

        return map.get(selections.iterator().next()).iterator().next();

    }
}
