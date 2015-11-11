package ch.fhnw.sna.examples.dbpedia;

import ch.fhnw.sna.examples.dbpedia.model.AuthorGraph;

/**
 * Main class for Music artist fetcher
 *
 */
public class AuthorFetcher_Main {

	public static void main(String[] args) {
		String FILE = "MusicArtist-associations.gexf";
		
		AuthorGraph graph = new AuthorFetcher().fetch();
		new AuthorGephiExport(FILE).export(graph);
	}
}
