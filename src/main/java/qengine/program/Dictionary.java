package qengine.program;

import java.util.HashMap;
import java.util.Map;

public class Dictionary {
	
	//map associating unique identifiers to elements (String): dictionary
    private Map<Integer, String> idToElementMap;
    
    //map associating elements (String) with unique identifiers (Integer).
    private Map<String, Integer> elementToIdMap;
    
    //map with keys of type String (representing the order of elements in a triple) 
    //and values ​​being nested maps that represent an index of triples.
    private Map<String, Map<String, Map<String, Integer>>> tripleIndex;
    
    private int currentId;

    public Dictionary() {
        this.idToElementMap = new HashMap<>();
        this.elementToIdMap = new HashMap<>();
        this.tripleIndex = new HashMap<>();
        this.currentId = 1;
    }

    //method for adding triplets to the map indexes
    public void addTriple(String subject, String predicate, String object) {
        // Add subject, predicate, and object to the dictionary and assign unique IDs
        int subjectId = addElementToDictionary(subject);
        int predicateId = addElementToDictionary(predicate);
        int objectId = addElementToDictionary(object);

        // Update the triple index
        updateTripleIndex(subjectId, predicateId, objectId);

        //System.out.println("Triple added: (" + subjectId + ", " + predicateId + ", " + objectId + ")");
    }

    //method for adding triplets to the dictionary
    private int addElementToDictionary(String element) {
        // Check if the element is already in the dictionary
        if (elementToIdMap.containsKey(element)) {
            return elementToIdMap.get(element);
        } else {
            // Assign a new ID and add to the dictionaries
            int id = currentId++;
            idToElementMap.put(id, element);
            elementToIdMap.put(element, id);
            return id;
        }
    }
    

    private void updateTripleIndex(int subjectId, int predicateId, int objectId) {
        // Update the triple index for different combinations of subject, predicate, and object orders
        updateIndex(subjectId, predicateId, objectId, "SPO");
        updateIndex(objectId, predicateId, subjectId, "OPS");
        updateIndex(subjectId, objectId, predicateId, "SOP");
        updateIndex(objectId, subjectId, predicateId, "OSP");
        updateIndex(predicateId, subjectId, objectId, "PSO");
        updateIndex(predicateId, objectId, subjectId, "POS");
    }

    
    private void updateIndex(int first, int second, int third, String order) {
        tripleIndex
                .computeIfAbsent(order, k -> new HashMap<>())
                .computeIfAbsent(idToElementMap.get(first), k -> new HashMap<>())
                .put(idToElementMap.get(second), third);
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
            tripleIndex.get(order).forEach((first, map) ->
                    map.forEach((second, third) ->
                            result.append("(").append(elementToIdMap.get(first)).append(", ")
                                    .append(elementToIdMap.get(second)).append(", ")
                                    .append(third).append(")\n")
                    )
            );
        }
        return result.toString();
    }
    
    
	public String findSubject(String order, String predicate, String object) {
        // Pour s'assurer de la validite de l'ordre saisie
        if (!order.equals("POS") && !order.equals("OPS")) {
            throw new IllegalArgumentException("Invalid order: " + order);
        }
        String result = "";
        // Obtenir l'id du sujet depuis l'index
        Map<String, Map<String, Integer>> predicateMap = tripleIndex.get(order);
        if (predicateMap != null) {
            Map<String, Integer> objectMap = predicateMap.get(predicate);
            if (objectMap != null) {
                Integer subjectId = objectMap.get(object);
                if (subjectId != null) {
                    // Retouner le sujet correspondans depuis idToElementMap
                    result =  idToElementMap.get(subjectId);
                }
                else result = null; //le triplet n'existe pas
            }
        }
        return result;
    }

}