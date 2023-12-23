package qengine.program;

import java.util.Set;

import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.rio.helpers.AbstractRDFHandler;



/**
 * Le RDFHandler intervient lors du parsing de données et permet d'appliquer un traitement pour chaque élément lu par le parseur.
 * 
 * <p>
 * Ce qui servira surtout dans le programme est la méthode {@link #handleStatement(Statement)} qui va permettre de traiter chaque triple lu.
 * </p>
 * <p>
 * À adapter/réécrire selon vos traitements.
 * </p>
 */
public final class MainRDFHandler extends AbstractRDFHandler {
	
	private final Dictionary rdfDictionary = new Dictionary();
	private int tripletCount = 0;
	private long timeFinAjout;
	
	public void handleStatement(Statement st) {
		rdfDictionary.addTriple(
				st.getSubject().stringValue(),
				st.getPredicate().stringValue(),
				st.getObject().stringValue()
		);
		timeFinAjout = System.currentTimeMillis();
		tripletCount++;
	}
	
	public int getTripletCount() {
        return tripletCount;
    }
	
	public int getIndexCount() {
        return rdfDictionary.getIndexCount();
    }
	
	public long getDictionaryAndIndexCreationTime() {
	    //return rdfDictionary.getDictionaryAndIndexCreationTime();
		return this.timeFinAjout - rdfDictionary.getStartTime();
	}
	
	
	public String displayDictionary() {
		return rdfDictionary.displayDictionary();
	}
	

	public String displayIndex() {
	    StringBuilder result = new StringBuilder();
	    for (String order : new String[]{"SPO", "SOP", "PSO", "OPS", "POS", "OSP"}) {
	        result.append(rdfDictionary.displayTripleIndex(order)).append("\n");
	        result.append("-------------------\n");
	    }
	    return result.toString();
	}
	
	
	public Set<String> findSubjects(String order,String predicate, String object){
		return  rdfDictionary.findSubjects(order, predicate, object);
	}
	
}