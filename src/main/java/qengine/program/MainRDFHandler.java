package qengine.program;

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
	
	public void handleStatement(Statement st) {
		
		rdfDictionary.addTriple(
				st.getSubject().stringValue(),
				st.getPredicate().stringValue(),
				st.getObject().stringValue()
		);
	}
	
	
	public void displayDictionary() {
		rdfDictionary.displayDictionary();
	}
	

	public void displayIndex() {
		for (String order : new String[]{"SOP", "OPS", "SPO", "OSP", "PSO", "POS"}) {
			rdfDictionary.displayTripleIndex(order);
			System.out.println("-------------------");
		}
	}
	
	
	public String findSubject(String predicate, String object){
		String order = "POS";
		return  rdfDictionary.findSubject(order, predicate, object);
	}

}