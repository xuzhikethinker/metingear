
/**
 * DocumentFactory.java
 *
 * 2011.09.28
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
package uk.ac.ebi.search;

import java.io.IOException;
import java.util.AbstractMap;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;
import org.apache.log4j.Logger;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopScoreDocCollector;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.util.Version;
import uk.ac.ebi.annotation.crossreference.CrossReference;
import uk.ac.ebi.core.AnnotatedEntity;
import uk.ac.ebi.core.MetabolicReaction;
import uk.ac.ebi.core.Metabolite;
import uk.ac.ebi.core.Reconstruction;
import uk.ac.ebi.resource.chemical.ChEBIIdentifier;


/**
 *          DocumentFactory – 2011.09.28 <br>
 *          Class description
 * @version $Rev$ : Last Changed $Date$
 * @author  johnmay
 * @author  $Author$ (this version)
 */
public class DocumentFactory {

    private static final Logger LOGGER = Logger.getLogger(DocumentFactory.class);


    public static Map<UUID, AnnotatedEntity> write(IndexWriter writer, Reconstruction recon)
      throws
      CorruptIndexException, IOException {

        Map<UUID, AnnotatedEntity> documents = new HashMap();
        
        for( Metabolite m : recon.getMetabolites() ) {
            Document doc = getDocument(m);
            UUID uuid = UUID.randomUUID();
            doc.add(new Field("uuid", uuid.toString(), Field.Store.YES, Field.Index.NOT_ANALYZED));
            documents.put(uuid, m);
            writer.addDocument(doc);
        }
        for( MetabolicReaction rxn : recon.getReactions() ) {
            Document doc = getDocument(rxn);
            UUID uuid = UUID.randomUUID();
            doc.add(new Field("uuid", uuid.toString(), Field.Store.YES, Field.Index.NOT_ANALYZED));
            documents.put(uuid, rxn);
            writer.addDocument(doc);
        }

        return documents;

    }


    /**
     * Returns a searchable lucene document for a metabolite
     * @param metabolite
     * @return
     */
    public static Document getDocument(MetabolicReaction reaction) {

        Document document = getDocument(new Document(),
                                        (AnnotatedEntity) reaction);

        // add metabolite data
        for( Metabolite m : reaction.getAllReactionMolecules() ) {
            document = getDocument(document, m);
        }

        return document;

    }


    /**
     * Returns a searchable lucene document for a metabolite
     * @param metabolite
     * @return
     */
    public static Document getDocument(Metabolite metabolite) {

        Document entry = getDocument(new Document(),
                                     (AnnotatedEntity) metabolite);


        return entry;

    }


    /**
     * Returns a searchable lucene document for an AnnotatedEntity
     * @param entity
     * @return
     */
    public static Document getDocument(Document document,
                                       AnnotatedEntity entity) {


        document.add(new Field(FieldType.ACCESSION.getName(), entity.getAccession(),
                               Field.Store.YES,
                               Field.Index.ANALYZED));
        if( entity.getName() != null ) {
            document.add(new Field(FieldType.NAME.getName(),
                                   entity.getName(),
                                   Field.Store.YES, Field.Index.ANALYZED));
        }
        if( entity.getAbbreviation() != null ) {
            document.add(new Field(FieldType.ABBREVIATION.getName(),
                                   entity.getAbbreviation(),
                                   Field.Store.YES, Field.Index.ANALYZED));
        }
        // index annotations

        // xref
        for( CrossReference xref : entity.getAnnotationsExtending(CrossReference.class) ) {
            document.add(new Field(FieldType.CROSS_REFERENCE.getName(), xref.getIdentifier().
              getAccession(), Field.Store.YES, Field.Index.ANALYZED));
        }


        return document;

    }


}

