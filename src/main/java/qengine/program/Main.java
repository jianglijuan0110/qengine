package qengine.program;

import java.io.FileInputStream;
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
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;
import org.apache.jena.util.FileManager;
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
	//static final String queryFile = workingDir + "sample_query.queryset";
	static String queryFile = "";

	/**
	 * Fichier contenant des données rdf
	 */
	//static final String dataFile = workingDir + "sample_data.nt";
	static String dataFile = "";
	
	private static final MainRDFHandler rdfHandler = new MainRDFHandler();
	
    private static String outputPath = "";
    private static boolean useJena = true;
    private static String warmPercentage = "";
    private static boolean shuffle = true;

	// ========================================================================

	/**
	 * Méthode utilisée ici lors du parsing de requête sparql pour agir sur l'objet obtenu.
	 */
	public static String processAQuery(ParsedQuery query) {
		List<StatementPattern> patterns = StatementPatternCollector.process(query.getTupleExpr());

	    // Variables pour collecter les informations nécessaires
	    List<String> listSubjects = new ArrayList<>();
	    
	    StringBuilder result = new StringBuilder();

	    for (StatementPattern pattern : patterns) {
	        String predicate = pattern.getPredicateVar().getValue().stringValue();
	        String object = pattern.getObjectVar().getValue().stringValue();

	        result.append("Pattern: ").append(pattern).append("\n");
	        result.append("Object of the pattern: ").append(object).append("\n");

	        // Utilisation de l'ordre POS pour rechercher le sujet
	        String subject = rdfHandler.findSubject(predicate, object);
	        listSubjects.add(subject);
	        
	        result.append("-------------------\n");
	    }
	    result.append("Subject found: ").append(listSubjects).append("\n");
	    result.append("\n##################################################\n\n");

	    return result.toString();
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
	        outputPath = cmd.getOptionValue("output");
	        useJena = cmd.hasOption("Jena");
	        warmPercentage = cmd.getOptionValue("warm");
	        shuffle = cmd.hasOption("shuffle");

	        // Vérifier l'existence des chemins spécifiés
	        if (queriesPath == null || dataPath == null || outputPath == null) {
	            System.out.println("Les chemins des requêtes, des données et de la sortie sont obligatoires.");
	            return;
	        }

	        dataFile = dataPath;
	        queryFile = queriesPath;

	        if (useJena) {
	            // Activer la vérification Jena
	            System.out.println("Vérification Jena activée.");

	            // Use Jena to read your triple data
	            Model model = ModelFactory.createDefaultModel();
	            try (FileInputStream in = new FileInputStream(dataFile)) {
	                // Assuming your data is in N-TRIPLE format, change if needed
	                model.read(in, null, "N-TRIPLE");
	            } catch (Exception e) {
	                e.printStackTrace();
	            }

	            // Specify the CSV file path
	            String csvFilePath = outputPath + "/output.csv";

	            // Create a FileWriter with the specified CSV file path
	            try (CSVWriter writer = new CSVWriter(new FileWriter(csvFilePath))) {

	                // Use Jena to perform RDF operations
	                StmtIterator iter = model.listStatements();
	                List<String[]> tripleStrings = new ArrayList<>();

	                while (iter.hasNext()) {
	                    Statement stmt = iter.nextStatement();
	                    String[] tripleString = new String[]{
	                            stmt.getSubject().toString(),
	                            stmt.getPredicate().toString(),
	                            stmt.getObject().toString()
	                    };

	                    tripleStrings.add(tripleString);

	                    // Print the triple as it is added
	                    System.out.println("Triple added: (" +
	                            stmt.getSubject().toString() + ", " +
	                            stmt.getPredicate().toString() + ", " +
	                            stmt.getObject().toString() + ")");
	                }

	                // Write triples to CSV file
	                writer.writeAll(tripleStrings);

	                // Print a message indicating successful export
	                System.out.println("Results exported to CSV: " + csvFilePath);

	            } catch (IOException e) {
	                // Handle the exception
	                e.printStackTrace();
	                System.err.println("Error exporting to CSV: " + e.getMessage());
	            }
	        }

	        // Rest of your code for warm-up and shuffle

	    } catch (ParseException e) {
	        // Gestion des erreurs d'analyse des arguments
	        e.printStackTrace();
	        System.err.println("Error exporting to CSV: " + e.getMessage());
	    }
	}

	
	// ========================================================================

	/**
	 * Traite chaque requête lue dans {@link #queryFile} avec {@link #processAQuery(ParsedQuery)}.
	 */
	private static List<String> parseQueries() throws FileNotFoundException, IOException {
		/**
		 * Try-with-resources
		 * 
		 * @see <a href="https://docs.oracle.com/javase/tutorial/essential/exceptions/tryResourceClose.html">Try-with-resources</a>
		 */
		/*
		 * On utilise un stream pour lire les lignes une par une, sans avoir à toutes les stocker
		 * entièrement dans une collection.
		 */
		List<String> resultsParseQueries = new ArrayList<>();
		
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

					//processAQuery(query); // Traitement de la requête, à adapter/réécrire pour votre programme
					resultsParseQueries.add(processAQuery(query));

					queryString.setLength(0); // Reset le buffer de la requête en chaine vide
				}
			}
		}
		return resultsParseQueries;
	}

	/**
	 * Traite chaque triple lu dans {@link #dataFile} avec {@link MainRDFHandler}.
	 */
	private static List<String> parseData() throws FileNotFoundException, IOException {
		List<String> resultsParseData = new ArrayList<>();

		try (Reader dataReader = new FileReader(dataFile)) {
			RDFParser rdfParser = Rio.createParser(RDFFormat.NTRIPLES);

			// Set the RDF handler to the parser
			rdfParser.setRDFHandler(rdfHandler);

			// Parsing and processing each triple by the handler
			rdfParser.parse(dataReader, baseURI);
			
			resultsParseData.add(rdfHandler.displayDictionary());
			resultsParseData.add(rdfHandler.displayIndex());
		}
		return resultsParseData;
	}
	
}