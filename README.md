Execution en ligne de commande:


"-queries", "-data" et "-output" sont obligatoires

Toutes les autres options restantes sont optionnelles.


Exemples d'exécution:

java -jar rdfengine.jar -queries /home/maguette/git/qengine/data/STAR_ALL_workload.queryset -data /home/maguette/git/qengine/data/100K.nt -output /home/maguette/Documents/M2/NoSQL/RDF

java -jar rdfengine.jar -queries /home/maguette/git/qengine/data/STAR_ALL_workload.queryset -data /home/maguette/git/qengine/data/100K.nt -output /home/maguette/Documents/M2/NoSQL/RDF -Jena

java -jar rdfengine.jar -queries /home/maguette/git/qengine/data/STAR_ALL_workload.queryset -data /home/maguette/git/qengine/data/100K.nt -output /home/maguette/Documents/M2/NoSQL/RDF -warm 80

java -jar rdfengine.jar -queries /home/maguette/git/qengine/data/STAR_ALL_workload.queryset -data /home/maguette/git/qengine/data/100K.nt -output /home/maguette/Documents/M2/NoSQL/RDF -shuffle

java -jar rdfengine.jar -queries /home/maguette/git/qengine/data/STAR_ALL_workload.queryset -data /home/maguette/git/qengine/data/100K.nt -output /home/maguette/Documents/M2/NoSQL/RDF -warm 80 -shuffle
