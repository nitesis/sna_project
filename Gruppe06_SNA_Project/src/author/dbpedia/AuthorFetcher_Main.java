package author.dbpedia;

import author.model.AuthorGraph;

/**
 * Main class for Author fetcher
 *
 */
public class AuthorFetcher_Main {

	public static void main(String[] args) {
		String FILE = "Authors-associations.gexf";
		
		AuthorGraph graph = new AuthorFetcher().fetch();
		new AuthorGephiExport(FILE).export(graph);
	}
}
