package qengine.program;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public class Dictionary2 {
	private Map<Integer, String> idToElementMap;
    private Map<String, List<Triple<Integer, Integer, Integer>>> tripleIndex;
    private int currentId;

    public Dictionary2() {
        this.idToElementMap = new HashMap<>();
        this.tripleIndex = new HashMap<>();
        this.currentId = 0;
    }

    public void addTriple(String subject, String predicate, String object) {
        int subjectId = addElementToDictionary(subject);
        int predicateId = addElementToDictionary(predicate);
        int objectId = addElementToDictionary(object);

        updateTripleIndex(subjectId, predicateId, objectId);
    }

    private int addElementToDictionary(String element) {
        // Vérifie si l'élément existe déjà dans la map
        for (Map.Entry<Integer, String> entry : idToElementMap.entrySet()) {
            if (Objects.equals(element, entry.getValue())) {
                return entry.getKey(); // Retourne la clé associée à l'élément
            }
        }
        // Si l'élément n'existe pas, ajoute un nouvel élément avec une nouvelle clé
        this.currentId++;
        idToElementMap.put(this.currentId, element);
        return this.currentId;
    }


    private void updateTripleIndex(int subjectId, int predicateId, int objectId) {
        updateIndex(subjectId, predicateId, objectId, "SPO");
        updateIndex(predicateId, objectId, subjectId, "POS");
    }

    private void updateIndex(int first, int second, int third, String order) {
        tripleIndex
                .computeIfAbsent(order, k -> new ArrayList<>())
                .add(new Triple<Integer, Integer, Integer>(first, second, third));
    }
    
    public void displayElements() {
        System.out.println("ID to Element Map:");
        for (Map.Entry<Integer, String> entry : idToElementMap.entrySet()) {
            System.out.println(entry.getKey() + ": " + entry.getValue());
        }

        System.out.println("\nTriple Index:");
        for (Map.Entry<String, List<Triple<Integer, Integer, Integer>>> entry : tripleIndex.entrySet()) {
            String order = entry.getKey();
            List<Triple<Integer, Integer, Integer>> tripleList = entry.getValue();

            System.out.println(order + ":");

            for (Triple<Integer, Integer, Integer> triple : tripleList) {
                System.out.println("(" + triple.getFirst() +
                                   ", " + triple.getSecond() +
                                   ", " + triple.getThird() + ")");
            }
        }
    }
    
    public Set<String> findSubjects(String order, String predicate, String object) {
        Set<String> results = new HashSet<>();

        // Vérifier si l'ordre spécifié existe dans le tripleIndex
        if (tripleIndex.containsKey(order)) {
            // Récupérer la liste de triplets associée à l'ordre spécifié
            List<Triple<Integer, Integer, Integer>> tripleList = tripleIndex.get(order);
            
            if(order.equals("POS")) {
            	// Parcourir les triplets
                for (Triple<Integer, Integer, Integer> triple : tripleList) {
                    // Vérifier si le prédicat et l'objet correspondent aux valeurs spécifiées
                    if (idToElementMap.get(triple.getFirst()).equals(predicate) &&
                        idToElementMap.get(triple.getSecond()).equals(object)) {
                        // Ajouter le sujet correspondant à l'ensemble des résultats
                        results.add(idToElementMap.get(triple.getThird()));
                    }
                }
        	}
        	if(order.equals("OPS")) {
        		// Parcourir les triplets
                for (Triple<Integer, Integer, Integer> triple : tripleList) {
                    // Vérifier si le prédicat et l'objet correspondent aux valeurs spécifiées
                    if (idToElementMap.get(triple.getSecond()).equals(predicate) &&
                        idToElementMap.get(triple.getFirst()).equals(object)) {
                        // Ajouter le sujet correspondant à l'ensemble des résultats
                        results.add(idToElementMap.get(triple.getThird()));
                    }
                }
        	}
        }

        return results;
    }


    

    public static void main(String[] args) {
        Dictionary2 dictionary = new Dictionary2();
        
        dictionary.addTriple("http://db.uwaterloo.ca/~galuc/wsdbm/User0", "http://schema.org/birthDate", "1988-09-24");
        dictionary.addTriple("http://db.uwaterloo.ca/~galuc/wsdbm/User0", "http://db.uwaterloo.ca/~galuc/wsdbm/userId", "9764726");
        dictionary.addTriple("http://db.uwaterloo.ca/~galuc/wsdbm/User1","http://db.uwaterloo.ca/~galuc/wsdbm/userId", "2536508");
        dictionary.addTriple("http://db.uwaterloo.ca/~galuc/wsdbm/User2","http://db.uwaterloo.ca/~galuc/wsdbm/userId","5196173");
		
		dictionary.displayElements();
		
		System.out.println("---------------------------------------");
		System.out.println(dictionary.findSubjects("POS", "http://db.uwaterloo.ca/~galuc/wsdbm/userId", "5196173"));
    }
}
