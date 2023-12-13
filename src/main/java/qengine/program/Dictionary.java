package qengine.program;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public class Dictionary {
	
	//map associant des identifiants uniques à des éléments (String): dictionnaire
    private Map<Integer, String> idToElementMap;
    
    //map avec des clés de type String (représentant l'ordre des éléments dans un triplet)
    //et des valeurs étant des map imbriquées qui représentent un index de triplets
    //private Map<String, Map<String, Map<String, Integer>>> tripleIndex;
    private Map<String, List<Triple<Integer, Integer, Integer>>> tripleIndex;
    
    private int currentId;

    public Dictionary() {
        this.idToElementMap = new HashMap<>();
        this.tripleIndex = new HashMap<>();
        this.currentId = 0;
    }

    //méthode pour ajouter les triplets aux index
    public void addTriple(String subject, String predicate, String object) {
        // Ajoute le sujet, prédicat et objet au dictionnaire et les attribue des identifiants uniques
        int subjectId = addElementToDictionary(subject);
        int predicateId = addElementToDictionary(predicate);
        int objectId = addElementToDictionary(object);

        // Mettre à jour les triple index
        updateTripleIndex(subjectId, predicateId, objectId);

        //System.out.println("Triple added: (" + subjectId + ", " + predicateId + ", " + objectId + ")");
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
        // Mettre à jour les triple index pour différentes combinaisons d'ordres de sujets, de prédicats et d'objets
        updateIndex(subjectId, predicateId, objectId, "SPO");
        updateIndex(subjectId, objectId, predicateId, "SOP");
        updateIndex(predicateId, subjectId, objectId, "PSO");
        updateIndex(objectId, predicateId, subjectId, "OPS");
        updateIndex(predicateId, objectId, subjectId, "POS");
        updateIndex(objectId, subjectId, predicateId, "OSP");
    }

    private void updateIndex(int first, int second, int third, String order) {
        tripleIndex
                .computeIfAbsent(order, k -> new ArrayList<>())
                .add(new Triple<Integer, Integer, Integer>(first, second, third));
    }

    
    //DISPLAY
    
    public String displayDictionary() {
        StringBuilder result = new StringBuilder();
        result.append("\n##################################################\n\n");
        result.append("Dictionary Contents:\n");
        
        for (Map.Entry<Integer, String> entry : idToElementMap.entrySet()) {
            result.append("ID: ").append(entry.getKey()).append(", Element: ").append(entry.getValue()).append("\n");
        }
        return result.toString();
    }
    

    public String displayTripleIndex(String order) {
    	StringBuilder result = new StringBuilder();
        result.append("Triple Index for ").append(order).append(":\n");

        if (tripleIndex.containsKey(order)) {
            List<Triple<Integer, Integer, Integer>> tripleList = tripleIndex.get(order);

            for (Triple<Integer, Integer, Integer> triple : tripleList) {
                result.append("(")
                      .append(triple.getFirst())
                      .append(", ")
                      .append(triple.getSecond())
                      .append(", ")
                      .append(triple.getThird())
                      .append(")\n");
            }
        } else {
            result.append("No triples found for order: ").append(order);
        }

        return result.toString();    
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


}