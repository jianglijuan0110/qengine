package qengine.program;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
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
	//static final String queryFile = workingDir + "sample_query.queryset";
	static String queryFile = "";

	/**
	 * Fichier contenant des données rdf
	 */
	//static final String dataFile = workingDir + "sample_data.nt";
	static String dataFile = "";
	
	private static final MainRDFHandler rdfHandler = new MainRDFHandler();
	
	// ========================================================================

	/**
	 * Méthode utilisée ici lors du parsing de requête sparql pour agir sur l'objet obtenu.
	 */
	public static String processAQuery(ParsedQuery query) {
		List<StatementPattern> patterns = StatementPatternCollector.process(query.getTupleExpr());

	    // Variables pour collecter les informations nécessaires
	    Set<String> listSubjects = new HashSet<>(); //Set pour pas qu'il y ait de doublons
	    
	    StringBuilder result = new StringBuilder();

	    for (StatementPattern pattern : patterns) {
	        String predicate = pattern.getPredicateVar().getValue().stringValue();
	        String object = pattern.getObjectVar().getValue().stringValue();

	        result.append("Pattern: ").append(pattern).append("\n");
	        result.append("Object of the pattern: ").append(object).append("\n");

	        // Utilisation de l'ordre POS pour rechercher le sujet
	        String subject = rdfHandler.findSubject("POS",predicate, object);
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
        options.addOption("export_query_results", true, "Chemin vers le dossier de sortie des résultats des requçetes");
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
            String exportResultsPath = cmd.getOptionValue("export_query_results");
            boolean useJena = cmd.hasOption("Jena");
            // Récupérer le pourcentage d'échantillon ou utiliser 100 par défaut
            double warmPercentage = cmd.hasOption("warm") ? Double.parseDouble(cmd.getOptionValue("warm")) : 100.0;
            // Récupérer l'option shuffle ou utiliser false par défaut
            boolean shuffle = cmd.hasOption("shuffle") ? true : false;

            // Vérifier l'existence des chemins spécifiés
            if (queriesPath == null || dataPath == null || exportResultsPath == null) {
                System.out.println("Les chemins des requêtes, des données et de la sortie sont obligatoires.");
                return;
            }
            
            dataFile = dataPath;
            queryFile = queriesPath;
            
            // Spécifiez le chemin du fichier CSV
            String csvOutputPath = outputPath + "/output.csv";
            String csvResultsPath = exportResultsPath + "/results.csv";
            
            // Créer un FileWriter avec le chemin du fichier CSV spécifié
            CSVWriter writer = new CSVWriter(new FileWriter(csvResultsPath));
            
			List<String> dataResults = parseData();
		    String queryResults = parseQueries(warmPercentage,shuffle);
		    for (String s : dataResults) {
		    	writer.writeNext(new String[]{s});
		    }
		    writer.writeNext(new String[]{queryResults});

    		// Fermez le writer
            writer.close();
            
            // Afficher un message indiquant une exportation réussie
            System.out.println("Resultats exportés en CSV: \n" + csvOutputPath + "\n" + csvResultsPath);
        } catch (ParseException e) {
            // Gestion des erreurs d'analyse des arguments
            e.printStackTrace();
            System.err.println("Erreur lors de l'analyse des auguments: " + e.getMessage());
        } catch (NumberFormatException e) {
            // Si la conversion échoue
            System.err.println("Erreur de conversion : " + e.getMessage());
        } catch (IOException e) {
            System.err.println("Erreur lors de exportation en CSV: " + e.getMessage());
        }

	}
	
	// ========================================================================

	/**
	 * Traite chaque requête lue dans {@link #queryFile} avec {@link #processAQuery(ParsedQuery)}.
	 */
	private static String parseQueries(double percentage, boolean shuffle) throws FileNotFoundException, IOException {
		/**
		 * Try-with-resources
		 * 
		 * @see <a href="https://docs.oracle.com/javase/tutorial/essential/exceptions/tryResourceClose.html">Try-with-resources</a>
		 */
		/*
		 * On utilise un stream pour lire les lignes une par une, sans avoir à toutes les stocker
		 * entièrement dans une collection.
		 */
		StringBuilder resultsParseQueries = new StringBuilder();
		
		// Vérifier que warmPercentage est dans la plage valide
	    if (percentage <= 0 || percentage > 100) {
	        resultsParseQueries.append("Le pourcentage doit être compris entre 0 et 100.");
	    } else {
	    	// Premier "try" pour compter le nombre de requêtes
			long queryCount = 0;
			try (Stream<String> lineStream = Files.lines(Paths.get(queryFile))) {
		        queryCount = lineStream.filter(line -> line.trim().endsWith("}")).count();
		        resultsParseQueries.append("Le nombre total de requêtes est : ").append(queryCount).append("\n");
		    }
			
			// Calculer le nombre d'échantillons à exécuter (partie entiere inf)
			int warmUpCount = (int) (queryCount * (percentage / 100));
			resultsParseQueries.append("Le nombre d'échantillons à exécuter est : ").append(warmUpCount).append("\n");
			
			// Deuxième "try" pour taiter la requête
			try (Stream<String> lineStream = Files.lines(Paths.get(queryFile))) {
				SPARQLParser sparqlParser = new SPARQLParser();
				Iterator<String> lineIterator = lineStream.iterator();
				StringBuilder queryString = new StringBuilder();

	            int processedCount = 0; //Compte le nombre de requêtes traitées
				while (lineIterator.hasNext() && processedCount < warmUpCount)
				/*
				 * On stocke plusieurs lignes jusqu'à ce que l'une d'entre elles se termine par un '}'
				 * On considère alors que c'est la fin d'une requête
				 */
				{
					String line = lineIterator.next();
					queryString.append(line);

					if (line.trim().endsWith("}")) {
	                    ParsedQuery query = sparqlParser.parseQuery(queryString.toString(), baseURI);
	                    
	                    // Générer un nombre aléatoire entre 0 et 1
	                    double randomIndex = Math.random();
	                    
	                    // Si shuffle est activé et que le nombre aléatoire est superieur à 0.5
	                    if (shuffle && randomIndex > 0.5) {
	                    	resultsParseQueries.append(processAQuery(query)).append("\n");
	                    	processedCount++;
	                    } else if(!shuffle) { //Si shuffle est désactivé, traiter les requêtes dans l'ordre
	                    	resultsParseQueries.append(processAQuery(query)).append("\n");
	                    	processedCount++;
	                    }
	                    queryString.setLength(0); // Reset le buffer de la requête en chaine vide
	                }
				}
			}
	    }
	    //System.out.println(resultsParseQueries.toString());
	    return resultsParseQueries.toString();
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