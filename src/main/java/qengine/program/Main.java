package qengine.program;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;

import java.nio.file.Files;
import java.nio.file.Path;
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

import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
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
 * Modifié par Maguette Sarr <maguette.sarr@etu.umontpellier.fr>
 * et Lijuan Jiang <olivier.rodriguez1@umontpellier.fr>
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
	public static Set<String> processAQuery(ParsedQuery query) {
		List<StatementPattern> patterns = StatementPatternCollector.process(query.getTupleExpr());

	    // Variables pour collecter les informations nécessaires
	    Set<String> listSubjects = new HashSet<>(); //Set pour pas qu'il y ait pas de doublons
	    
	    int i = 0;
	    for (StatementPattern pattern : patterns) {
	    	
	    	String predicate = pattern.getPredicateVar().getValue().stringValue();
	        String object = pattern.getObjectVar().getValue().stringValue();

	        // Utilisation de l'ordre POS pour rechercher les sujets
	        Set<String> subjects = rdfHandler.findSubjects("POS",predicate, object);
	    	
	    	if(i == 0) { // Si c'est le premier motif
	    		listSubjects.addAll(subjects);
	    	}else {
                // Si ce n'est pas le premier motif, gardez seulement les sujets communs
                listSubjects.retainAll(subjects);
	    	}
	        i++;
	    }
	    return listSubjects;
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
            String exportResultsPath = cmd.getOptionValue("export_query_results");
            boolean useJena = cmd.hasOption("Jena");
            // Récupérer le pourcentage d'échantillon ou utiliser 100 par défaut
            double warmPercentage = cmd.hasOption("warm") ? Double.parseDouble(cmd.getOptionValue("warm")) : 100.0;
            // Récupérer l'option shuffle ou utiliser false par défaut
            boolean shuffle = cmd.hasOption("shuffle") ? true : false;

            // Vérifier l'existence des chemins spécifiés
            if (queriesPath == null || dataPath == null || outputPath == null) {
                System.out.println("Les chemins des requêtes, des données et de la sortie sont obligatoires.");
                return;
            }
            
            // Utiliser la classe Path pour extraire les noms des fichiers
            Path queriesPathObject = Paths.get(queriesPath);
            String queriesFileName = queriesPathObject.getFileName().toString();
            Path dataPathObject = Paths.get(dataPath);
            String dataFileName = dataPathObject.getFileName().toString();
            
            dataFile = dataPath;
            queryFile = queriesPath;
            
            // Spécifiez le chemin du fichier CSV
            String csvOutputPath = outputPath + "/output.csv";
            String csvResultsPath = exportResultsPath + "/results.csv";
            String csvJenaPath = outputPath + "/resultsJena.csv";
            
            // Créer un FileWriter avec le chemin du fichier CSV spécifié
            CSVWriter writerOutput = new CSVWriter(new FileWriter(csvOutputPath));
            writerOutput.writeNext(new String[]{"Nom du fichier de données : " + dataFileName});
            writerOutput.writeNext(new String[]{"Nom du fichier des requêtes : " + queriesFileName});
            
            parseData();
            List<Set<String>> queryResults = parseQueries(warmPercentage,shuffle);
            
            if(exportResultsPath != null) {
                CSVWriter writerResults = new CSVWriter(new FileWriter(csvResultsPath));
                writerResults.writeNext(new String[]{"Taille se la solution du système: " + String.valueOf(queryResults.size())});
                for (Set<String> s : queryResults) {
    		    	writerResults.writeNext(new String[]{s.toString()});
    		    }
        		// Fermez le writer des résultats
                writerResults.close();
                System.out.println("Resultats du système exportés en CSV: " + csvResultsPath);
            }	    
		    
		    if(useJena) {
	        	System.out.println("Vérification Jena activée");
	        	
		    	List<Set<String>> results = parseQueriesWithJena();
		    	
		    	CSVWriter writerJena = new CSVWriter(new FileWriter(csvJenaPath));
		    	writerJena.writeNext(new String[]{"Taille se la solution Jena: " + String.valueOf(results.size())});
		    	
		    	for (Set<String> s : results) {
			    	writerJena.writeNext(new String[]{s.toString()});
			    }
	    		// Fermez le writer
	            writerJena.close();
	            System.out.println("Resultats Jena exportés en CSV: " + csvJenaPath);
	            
		    	// Vérifier si les deux listes sont nulles ou ont une taille différente
		        if (results == null || queryResults == null || results.size() != queryResults.size()) {
		        	System.out.println("Correctude et complétude des résultats du système : " + false);
		        } else {
		        	boolean b = true;
		            // Parcourir les listes et comparer chaque élément
		            for (int i = 0; i < results.size(); i++) {
		                if(!queryResults.get(i).equals(results.get(i))) {
		                	b = false;
		                	break;
		                }
		            }
		            System.out.println("Correctude et complétude des résultats du système : " + b);
		        }
		    }
		    

		    // Fermez le writer de l'output
            writerOutput.close();

            
            // Afficher un message indiquant une exportation réussie
            System.out.println("Resultats exportés en CSV: " + csvOutputPath);
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
	private static List<Set<String>> parseQueries(double percentage, boolean shuffle) throws FileNotFoundException, IOException {
		List<Set<String>> resultsParseQueries = new ArrayList<>();

	    // Vérifier que warmPercentage est dans la plage valide
	    if (percentage <= 0 || percentage > 100) {
	        System.out.println("Le pourcentage doit être compris entre 0 et 100.");
	    } else {
	        // Premier "try" pour compter le nombre de requêtes
	        long queryCount = 0;
	        try (Stream<String> lineStream = Files.lines(Paths.get(queryFile))) {
	            queryCount = lineStream.filter(line -> line.trim().endsWith("}")).count();
	            //resultsParseQueries.add("Le nombre total de requêtes est : " + queryCount);
	        }

	        // Calculer le nombre d'échantillons à exécuter (partie entière inférieure)
	        int warmUpCount = (int) (queryCount * (percentage / 100));
	        //resultsParseQueries.add("Le nombre d'échantillons à exécuter est : " + warmUpCount);

	        // Deuxième "try" pour traiter la requête
	        try (Stream<String> lineStream = Files.lines(Paths.get(queryFile))) {
	            SPARQLParser sparqlParser = new SPARQLParser();
	            Iterator<String> lineIterator = lineStream.iterator();
	            StringBuilder queryString = new StringBuilder();

	            int processedCount = 0; // Compte le nombre de requêtes traitées
	            while (lineIterator.hasNext() && processedCount < warmUpCount) {
	                String line = lineIterator.next();
	                queryString.append(line);

	                if (line.trim().endsWith("}")) {
	                    ParsedQuery query = sparqlParser.parseQuery(queryString.toString(), baseURI);

	                    // Générer un nombre aléatoire entre 0 et 1
	                    double randomIndex = Math.random();

	                    // Si shuffle est activé et que le nombre aléatoire est supérieur à 0.5
	                    if (shuffle && randomIndex > 0.5) {
	                        resultsParseQueries.add(processAQuery(query));
	                        processedCount++;
	                    } else if (!shuffle) { // Si shuffle est désactivé, traiter les requêtes dans l'ordre
	                        resultsParseQueries.add(processAQuery(query));
	                        processedCount++;
	                    }
	                    queryString.setLength(0); // Reset le buffer de la requête en chaine vide
	                }
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
	
	/**
	 * Vérification Jena
	 */
	public static List<Set<String>> parseQueriesWithJena() throws FileNotFoundException, IOException {
        // Chargement des données RDF avec Jena
        Model model = ModelFactory.createDefaultModel();
        try (FileInputStream in = new FileInputStream(dataFile)) {
            model.read(in, null, "TURTLE");
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("Nombre de triplets dans le modèle : " + model.size());

        List<Set<String>> resultList = new ArrayList<>();
        StringBuilder queryString = new StringBuilder();

        try (Stream<String> lineStream = Files.lines(Paths.get(queryFile))) {
            Iterator<String> lineIterator = lineStream.iterator();

            while (lineIterator.hasNext()) {
                String line = lineIterator.next();
                queryString.append(line);

                if (line.trim().endsWith("}")) {
                    // Création de la requête SPARQL
                    String sparqlQuery = queryString.toString();

                    try {
                        // Création de l'objet Query à partir de la chaîne SPARQL
                        Query query = QueryFactory.create(sparqlQuery);

                        // Création et exécution de la requête SPARQL avec Jena
                        try (QueryExecution qexec = QueryExecutionFactory.create(query, model)) {
                            ResultSet results = qexec.execSelect();

                            // Liste pour stocker les résultats de la requête courante
                            Set<String> queryResultList = new HashSet<>();

                            // Afficher les résultats
                            while (results.hasNext()) {
                                QuerySolution soln = results.next();
                                // Récupération des valeurs pour la variable v0
                                String subject = soln.get("v0").toString();
                                // Ajouter la solution à la liste de résultats de la requête courante
                                queryResultList.add(subject);
                            }

                            // Ajouter la liste de résultats de la requête courante à la liste résultante
                            resultList.add(queryResultList);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        // Gérer les erreurs liées à la création ou l'exécution de la requête SPARQL
                        // Ajouter une liste vide à la liste résultante en cas de resultat vide
                        resultList.add(new HashSet<>());
                    }

                    // Réinitialiser le buffer de la requête en chaine vide pour la prochaine requête
                    queryString.setLength(0);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            // Gérer les erreurs liées à la lecture du fichier de requêtes
        }

        return resultList;
    }
	
}