package ch.fhnw.sna.examples.dbpedia;

//import static ch.fhnw.sna.util.GenreDetectionUtil.isBlues;
//import static ch.fhnw.sna.util.GenreDetectionUtil.isCountry;
//import static ch.fhnw.sna.util.GenreDetectionUtil.isElectronic;
//import static ch.fhnw.sna.util.GenreDetectionUtil.isFolk;
//import static ch.fhnw.sna.util.GenreDetectionUtil.isHipHop;
//import static ch.fhnw.sna.util.GenreDetectionUtil.isJazz;
//import static ch.fhnw.sna.util.GenreDetectionUtil.isLatin;
//import static ch.fhnw.sna.util.GenreDetectionUtil.isPop;
//import static ch.fhnw.sna.util.GenreDetectionUtil.isRock;
//import static ch.fhnw.sna.util.GenreDetectionUtil.isSoul;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Collection;
import java.util.Iterator;
import java.util.Set;

import org.apache.jena.datatypes.DatatypeFormatException;
import org.apache.jena.datatypes.xsd.XSDDatatype;
import org.apache.jena.datatypes.xsd.XSDDateTime;
import org.apache.jena.datatypes.xsd.impl.XSDDateType;
import org.apache.jena.datatypes.xsd.impl.XSDYearType;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Sets;

import ch.fhnw.sna.examples.dbpedia.model.Author;
import ch.fhnw.sna.examples.dbpedia.model.AuthorGraph;

/**
 * Fetches the Music Artist Association Network from dbpedia
 * 
 */
// Hier werden die eigentlichen Abfragen gemacht
public class AuthorFetcher {

	private static final String DBPEDIA_SPARQL_ENDPOINT = "http://dbpedia.org/sparql";
	private static final DateTimeFormatter ACTIVE_YEAR_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

	private static final Logger LOG = LoggerFactory.getLogger(AuthorFetcher.class);

	public AuthorGraph fetch() {
		AuthorGraph graph = new AuthorGraph();
		LOG.info("Start fetching Author Network");
		fetchAssociations(graph);
		LOG.info("Finished fetching Author Network");
		LOG.info("Start fetching node attributs");
		enrichNodeInformation(graph);
		LOG.info("Finished fetching node attributes");
		return graph;
	}

	private void fetchAssociations(AuthorGraph graph) {
		final int LIMIT = Integer.MAX_VALUE; // Means no limit
		boolean hasMoreResults = true;
		int currentOffset = 0;
		int fetchedTotal = 0;
		while (hasMoreResults && fetchedTotal < LIMIT) {
			//Hier schon eine Query-Anpassung für unser Autorenprojekt
			String influenceQuery = "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> \n"
					 + "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> \n"
					 + "PREFIX dbpedia-owl: <http://dbpedia.org/ontology/> \n" +
					 "select ?P ?Q where {"+
					 "{?Q <http://dbpedia.org/property/influencedBy> ?P."+
					 "?P <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://dbpedia.org/class/yago/Writer110794014>."+
					 "?Q <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://dbpedia.org/class/yago/Writer110794014>}"+
					 "UNION \n"+
					 "{?P <http://dbpedia.org/property/influenced> ?Q."+
					 "?P <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://dbpedia.org/class/yago/Writer110794014>."+
					 "?Q <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://dbpedia.org/class/yago/Writer110794014>}"+

					 
					 "} LIMIT 1000 OFFSET "+ currentOffset;
				

			LOG.debug("Querying: {}", influenceQuery);
			System.out.println(influenceQuery);
			Query query = QueryFactory.create(influenceQuery);
			int resultCounter = 0;
			try (QueryExecution qexec = QueryExecutionFactory.sparqlService(DBPEDIA_SPARQL_ENDPOINT, query)) {
				ResultSet results = qexec.execSelect();

				while (results.hasNext()) {
					++resultCounter;
					try{
					QuerySolution sol = results.next();
					String fromUri = sol.getResource("P").getURI();
					String toUri = sol.getResource("Q").getURI();
					graph.addAuthorIfNotExists(fromUri, fromUri);
					graph.addAuthorIfNotExists(toUri, toUri);
					graph.addAssociation(fromUri, toUri);
					}catch(Exception e){
						LOG.error("Fehler beim sammeln...", e);
					}
				}
			}
			LOG.debug("Fetches {} new results.", resultCounter);
			fetchedTotal += resultCounter;
			currentOffset += 1000;
			if (resultCounter < 1000) {
				hasMoreResults = false;
			}
		}
	}
	
	
	
	

	private void enrichNodeInformation(AuthorGraph graph) {
		for (Author a : graph.getAuthors()){
			enrichSingleAuthor(a);
		}
	}

	private void enrichSingleAuthor(Author author) {
		LOG.info("Enrich artist {}", author.getLabel());
		String queryString = buildActorQuery(author);

		Query query = QueryFactory.create(queryString);
		Set<String> relatives = Sets.newHashSet();


		try (QueryExecution qexec = QueryExecutionFactory.sparqlService(DBPEDIA_SPARQL_ENDPOINT, query)) {

			ResultSet results = qexec.execSelect();
			while (results.hasNext()) {
				QuerySolution sol = results.next();
				Iterator<String> it = sol.varNames();
				while (it.hasNext()) {
					String key = it.next();
					switch (key) {
					case "deathDate":
						Literal activeYears = sol.getLiteral(key);
						extractDeathDate(author, activeYears);
						break;
					case "birthdate":
						Literal birthdate = sol.getLiteral(key);
						extractBirthdate(author, birthdate);
						break;

					case "relative":
						Resource relative = sol.getResource(key);
						relatives.add(relative.getLocalName());
						break;

					case "spouse":
						Resource spouse = sol.getResource(key);
						author.setSpouse(spouse.getLocalName());
						break;

					default:
						throw new IllegalStateException("Unknown key: " + key);
					}
				}
			}
		}

	}

	private void extractDeathDate(Author artist, Literal deathDate) {
		try {
		if (deathDate.getDatatype() instanceof XSDDateType) {
			XSDDateTime time = (XSDDateTime) deathDate.getValue();
			LocalDate birthDate = LocalDate.of(time.getYears(), time.getMonths(), time.getDays());
			artist.setDeathYear(birthDate);
		} else if (XSDDatatype.XSDgYear.equals(deathDate.getDatatype())) {
			int year = Integer.parseInt(deathDate.getValue().toString());
			artist.setDeathYear(LocalDate.of(year, 1, 1));
		
		} else if (deathDate.getDatatype() instanceof XSDYearType) {
			LocalDate birthDate = LocalDate.parse(deathDate.getLexicalForm(), ACTIVE_YEAR_FORMATTER);
			artist.setDeathYear(LocalDate.of(birthDate.getYear(), 1, 1));
		}
		else {
			LOG.error("Unknown birthdate type: " + deathDate.getDatatype());
		}
		} catch (DateTimeParseException ex) {
			LOG.warn("Could not extract time from: "+deathDate);
		} catch (DatatypeFormatException ex){
			LOG.warn("Could not extract time from: "+deathDate);
		}
	}
	
	
	private void extractBirthdate(Author artist, Literal birthdate) {
		try {
		if (birthdate.getDatatype() instanceof XSDDateType) {
			XSDDateTime time = (XSDDateTime) birthdate.getValue();
			LocalDate birthDate = LocalDate.of(time.getYears(), time.getMonths(), time.getDays());
			artist.setbirthYear(birthDate);
		} else if (XSDDatatype.XSDgYear.equals(birthdate.getDatatype())) {
			int year = Integer.parseInt(birthdate.getValue().toString());
			artist.setbirthYear(LocalDate.of(year, 1, 1));
		
		} else if (birthdate.getDatatype() instanceof XSDYearType) {
			LocalDate birthDate = LocalDate.parse(birthdate.getLexicalForm(), ACTIVE_YEAR_FORMATTER);
			artist.setbirthYear(LocalDate.of(birthDate.getYear(), 1, 1));
		}
		else {
			LOG.error("Unknown birthdate type: " + birthdate.getDatatype());
		}
		} catch (DateTimeParseException ex) {
			LOG.warn("Could not extract time from: "+birthdate);
		} catch (DatatypeFormatException ex){
			LOG.warn("Could not extract time from: "+birthdate);
		}
	}

//	private void extractActiveYears(Author artist, Literal years) {
//		int year =-1;
//			if (XSDDatatype.XSDgYear.equals(years.getDatatype())){
//				year = Integer.parseInt(years.getValue().toString());
//			} else {
//				LOG.warn("Could not extract time datatype: "+years);
//			}
//		if (year > -1){
//			artist.setActiveYears(LocalDate.now().getYear() - year);
//		}
//	}

//
//
//	private void extractArtistSex(Author artist, Set<String> subjects) {
//		int maleCounter = 0;
//		int femaleCounter = 0;
//		for (String subject : subjects) {
//			if (subject.contains("male")) {
//				++maleCounter;
//			}
//			if (subject.contains("female")) {
//				++femaleCounter;
//			}
//		}
//		if (femaleCounter > 0) {
//			artist.setSex("female");
//		} else if (maleCounter > 0) {
//			artist.setSex("male");
//		} else {
//			LOG.debug("No sex found for: " + artist);
//		}
//	}

	private String buildActorQuery(Author author) {
		String authorUri = author.getUri();
		
		String query = "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> \n"
				 + "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> \n"
				 + "PREFIX dbpedia-owl: <http://dbpedia.org/ontology/> \n" +
				 "select * where { \n"+
				 "{<"+ authorUri+"> <http://dbpedia.org/ontology/deathDate> ?deathDate } \n"+
				 "UNION \n"+
				 "{<" + authorUri+ "> dbpedia-owl:birthDate ?birthdate} \n"+
				 "UNION \n"+
				 "{<" + authorUri+ "> dbpedia-owl:spouse ?spouse.\n"
				 		+ "?spouse <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://dbpedia.org/class/yago/Writer110794014>\n}"+
				 "UNION \n"+
				 "{<" + authorUri+ "> dbpedia-owl:relative ?relative}\n"+
				 "UNION \n"+
				 "{<" + authorUri+ "> dbpedia-owl:relative ?relative}"+
				 "}";
		
		
		LOG.debug("Querying: {}", query);
		return query;
	}
}
