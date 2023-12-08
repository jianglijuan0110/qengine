package qengine.program;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Stream;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.eclipse.rdf4j.query.algebra.StatementPattern;
import org.eclipse.rdf4j.query.algebra.helpers.StatementPatternCollector;
import org.eclipse.rdf4j.query.parser.ParsedQuery;
import org.eclipse.rdf4j.query.parser.sparql.SPARQLParser;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.RDFParser;
import org.eclipse.rdf4j.rio.Rio;
import com.opencsv.CSVWriter;



/**
 * Programme simple lisant un fichier de requête et un fichier de données.
 * 
 * <p>
 * Les entrées sont données ici de manière statique,
 * à vous de programmer les entrées par passage d'arguments en ligne de commande comme demandé dans l'énoncé.
 * </p>
 * 
 * <p>
 * Le présent programme se contente de vous montrer la voie pour lire les triples et requêtes
 * depuis les fichiers ; ce sera à vous d'adapter/réécrire le code pour finalement utiliser les requêtes et interroger les données.
 * On ne s'attend pas forcémment à ce que vous gardiez la même structure de code, vous pouvez tout réécrire.
 * </p>
 * 
 * @author Olivier Rodriguez <olivier.rodriguez1@umontpellier.fr>
 */
final class Main {
	static final String baseURI = null;

	/**
	 * Votre répertoire de travail où vont se trouver les fichiers à lire
	 */
	static final String workingDir = "data/";

	/**
	 * Fichier contenant les requêtes sparql
	 */
	static String queryFile = "";

	/**
	 * Fichier contenant des données rdf
	 */
	static String dataFile = "";
	
	private static final MainRDFHandler rdfHandler = new MainRDFHandler();
	
    private static String outputPath = "";
    private boolean useJena = true;
    private String warmPercentage = "";
    private boolean shuffle = true;

	// ========================================================================

	/**
	 * Méthode utilisée ici lors du parsing de requête sparql pour agir sur l'objet obtenu.
	 */
	public static void processAQuery(ParsedQuery query) {
		List<StatementPattern> patterns = StatementPatternCollector.process(query.getTupleExpr());

	    // Variables pour collecter les informations nécessaires
	    List<String> listSubjects = new ArrayList<>();

	    for (StatementPattern pattern : patterns) {
	        String predicate = pattern.getPredicateVar().getValue().stringValue();
	        String object = pattern.getObjectVar().getValue().stringValue();

	        System.out.println("Pattern: " + pattern);
	        System.out.println("Object of the pattern: " + object);

	        // Utilisation de l'ordre POS pour rechercher le sujet
	        String subject = rdfHandler.findSubject(predicate, object);
	        
	        listSubjects.add(subject);

	        System.out.println("-------------------");
	    }
	    
	    // Afficher la liste des sujets
	    System.out.println("Subject found: " + listSubjects);

	    System.out.println();
	    System.out.println("##################################################");
	    System.out.println();
	
	}
	
	
	/**
	 * Entrée du programme
	 */
	public static void main(String[] args) throws Exception {
		
		// Définir les options de la ligne de commande
        Options options = new Options();
        options.addOption("queries", true, "Chemin vers le dossier des requêtes");
        options.addOption("data", true, "Chemin vers le fichier de données");
        options.addOption("output", true, "Chemin vers le dossier de sortie");
        options.addOption("Jena", false, "Active la vérification Jena");
        options.addOption("warm", true, "Pourcentage d'échantillon pour le chauffage du système");
        options.addOption("shuffle", false, "Permutation aléatoire des requêtes");

        CommandLineParser parser = new DefaultParser();

        try {
            // Analyser les arguments de la ligne de commande
            CommandLine cmd = parser.parse(options, args);

            // Récupérer les valeurs des options
            String queriesPath = cmd.getOptionValue("queries");
            String dataPath = cmd.getOptionValue("data");
            String outputPath = cmd.getOptionValue("output");
            boolean useJena = cmd.hasOption("Jena");
            String warmPercentage = cmd.getOptionValue("warm");
            boolean shuffle = cmd.hasOption("shuffle");

            // Vérifier l'existence des chemins spécifiés
            if (queriesPath == null || dataPath == null || outputPath == null) {
                System.out.println("Les chemins des requêtes, des données et de la sortie sont obligatoires.");
                return;
            }
            
            dataFile = dataPath;
            queryFile = queriesPath;
            
           /* if (cmd.hasOption("Jena")) {
                // Activer la vérification Jena
            	System.out.println("Vérification Jena activée.");
            }*/

            /*if (cmd.hasOption("warm")) {
                // Utiliser l'échantillon pour chauffer le système
            }

            if (cmd.hasOption("shuffle")) {
                // Considérer une permutation aléatoire des requêtes
            }*/
            
    		parseData();
    		parseQueries();

            // Exportez les résultats dans un fichier CSV
            // ...
    		exportToCSV();

        } catch (ParseException e) {
            // Gestion des erreurs d'analyse des arguments
            e.printStackTrace();
            System.err.println("Erreur lors de l'analyse des arguments : " + e.getMessage());
        }

	}
		/*
public void warmUpSystemWithQueries(List<String> queries, double percentage) {
    // Assurez-vous que warmPercentage est compris entre 0 et 100
    if (percentage < 0 || percentage > 100) {
        throw new IllegalArgumentException("Le pourcentage d'échantillon doit être compris entre 0 et 100.");
    }

    // Calculer le nombre d'échantillons à exécuter
    int totalQueries = queries.size();
    int warmUpCount = (int) (totalQueries * (percentage / 100));

    // Créer une liste aléatoire d'indices pour sélectionner les échantillons
    List<Integer> randomIndices = new ArrayList<>();
    for (int i = 0; i < totalQueries; i++) {
        randomIndices.add(i);
    }
    Collections.shuffle(randomIndices);

    // Sélectionner les échantillons et les exécuter
    for (int i = 0; i < warmUpCount; i++) {
        int index = randomIndices.get(i);
        String query = queries.get(index);

        // Exécuter la requête SPARQL (à adapter en fonction de votre logique)
        // ...
    }
}*/
	
	// ========================================================================

	/**
	 * Traite chaque requête lue dans {@link #queryFile} avec {@link #processAQuery(ParsedQuery)}.
	 */
	private static void parseQueries() throws FileNotFoundException, IOException {
		/**
		 * Try-with-resources
		 * 
		 * @see <a href="https://docs.oracle.com/javase/tutorial/essential/exceptions/tryResourceClose.html">Try-with-resources</a>
		 */
		/*
		 * On utilise un stream pour lire les lignes une par une, sans avoir à toutes les stocker
		 * entièrement dans une collection.
		 */
		try (Stream<String> lineStream = Files.lines(Paths.get(queryFile))) {
			SPARQLParser sparqlParser = new SPARQLParser();
			Iterator<String> lineIterator = lineStream.iterator();
			StringBuilder queryString = new StringBuilder();

			while (lineIterator.hasNext())
			/*
			 * On stocke plusieurs lignes jusqu'à ce que l'une d'entre elles se termine par un '}'
			 * On considère alors que c'est la fin d'une requête
			 */
			{
				String line = lineIterator.next();
				queryString.append(line);

				if (line.trim().endsWith("}")) {
					ParsedQuery query = sparqlParser.parseQuery(queryString.toString(), baseURI);

					processAQuery(query); // Traitement de la requête, à adapter/réécrire pour votre programme

					queryString.setLength(0); // Reset le buffer de la requête en chaine vide
				}
			}
		}
	}

	/**
	 * Traite chaque triple lu dans {@link #dataFile} avec {@link MainRDFHandler}.
	 */
	private static void parseData() throws FileNotFoundException, IOException {

		try (Reader dataReader = new FileReader(dataFile)) {
			RDFParser rdfParser = Rio.createParser(RDFFormat.NTRIPLES);
			// On va parser des données au format ntriples
			//MainRDFHandler rdfHandler = new MainRDFHandler();

			// Set the RDF handler to the parser
			rdfParser.setRDFHandler(rdfHandler);

			// Parsing and processing each triple by the handler
			rdfParser.parse(dataReader, baseURI);
			
			System.out.println();
			System.out.println("##################################################");
			// Display the dictionary after processing all statements
			rdfHandler.displayDictionary();
			
			System.out.println();
			System.out.println("##################################################");
			// Display the indexes
			rdfHandler.displayIndex();
		}

	}
	
	private static void exportToCSV() {
        // Create a list to store your data (replace this with your actual data)
        List<String[]> data = new ArrayList<>();
        data.add(new String[]{"Column1", "Column2", "Column3"}); 

        // Add your actual data to the list
        // For example, you might have data from your program that you want to export
        // data.add(new String[]{"value1", "value2", "value3"});

        // Specify the CSV file path
        String csvFilePath = outputPath + "/output.csv"; 

        // Create a FileWriter with the specified CSV file path
        try (CSVWriter writer = new CSVWriter(new FileWriter(csvFilePath))) {

            // Write the data to the CSV file
            writer.writeAll(data);

            // Print a message indicating successful export
            System.out.println("Results exported to CSV: " + csvFilePath);

        } catch (IOException e) {
            // Handle IOException, e.g., by printing an error message
            System.err.println("Error exporting to CSV: " + e.getMessage());
        }
    }
	
}