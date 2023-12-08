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

        System.out.println("Triple added: (" + subjectId + ", " + predicateId + ", " + objectId + ")");
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
    
    public void displayDictionary() {
        System.out.println("Dictionary Contents:");
        for (Map.Entry<Integer, String> entry : idToElementMap.entrySet()) {
            System.out.println("ID: " + entry.getKey() + ", Element: " + entry.getValue());
        }
    }

    public void displayTripleIndex(String order) {
        System.out.println("Triple Index for " + order + ":");
        if (tripleIndex.containsKey(order)) {
            tripleIndex.get(order).forEach((first, map) ->
                    map.forEach((second, third) ->
                            System.out.println("(" + elementToIdMap.get(first) + ", " +
                                    elementToIdMap.get(second) + ", " +
                                    third + ")"
                            )
                    )
            );
        }
    }
    
    
	public String findSubject(String order, String predicate, String object) {
        // Ensure that the order is valid
        /*if (!order.equals("POS") && !order.equals("OPS")) {
            throw new IllegalArgumentException("Invalid order: " + order);
        }*/
        String result = "";
        // Get the subject ID from the triple index
        Map<String, Map<String, Integer>> predicateMap = tripleIndex.get(order);
        if (predicateMap != null) {
            Map<String, Integer> objectMap = predicateMap.get(predicate);
            if (objectMap != null) {
                Integer subjectId = objectMap.get(object);
                if (subjectId != null) {
                    // Return the corresponding element from the idToElementMap
                    result =  idToElementMap.get(subjectId);
                }
                else result = "le triplet n'existe pas";
            }
        }

        // If no match is found, return null or throw an exceptin based on your use case
        return result;
    }

}