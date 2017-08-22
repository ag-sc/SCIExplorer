# SCIExplorer
Exploration tool for ontology based data in RDF.

This project can be deployed on a Tomcat-Server. The latest version of Tomcat is recommended
The web-application is preconfigured to work correctly out-of-the-box so there is no further configuration needed.
Anyway there are some configuration-options to adjust the application for your needs.


Configuration options

Configuration-class (SCIOExplorer):
- CSV-files which are loaded from the resources-package. These files contain the ontology information (relations, classes, subclasses) to build an ontology-tree.
- RDF-file which is loaded from the resources-package. This file contains the data in RDF-triples.
- Resource URIs which are used in the RDF-triples.
- SPARQL-database interface which is used. 
- colours of the barchart bars
- ontology paths
- Pubmed URL

DetailsTableGenerator-class (SCIOExplorer/visualization):
Configure what details are shown on the details-page.
Change the 'VARIABLES' variable to configure which columns are shown.
Change the 'generateRow' method to generate a value for each of the specified columns.
