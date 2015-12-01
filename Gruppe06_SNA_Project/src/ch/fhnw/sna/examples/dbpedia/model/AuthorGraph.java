package ch.fhnw.sna.examples.dbpedia.model;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

public class AuthorGraph {

	private Map<String, Author> uriAuthor = Maps.newHashMap();
	private Map<String, Set<String>> associations = Maps.newHashMap();

	public void addAuthor(Author author) {
		uriAuthor.put(author.getUri(), author);
	}

	public void addAssociation(String fromUri, String toUri) {
		Set<String> to = associations.get(fromUri);
		if (to == null) {
			to = Sets.newHashSet();
			associations.put(fromUri, to);
		}
		to.add(toUri);
	}

	public Map<String, Set<String>> getAssociations() {
		return associations;
	}

	public boolean containsAuthor(String uri) {
		return uriAuthor.containsKey(uri);
	}

	public Collection<Author> getAuthors() {
		return uriAuthor.values();
	}

	public void addAuthorIfNotExists(String uri, String label) {
		if (!uriAuthor.containsKey(uri)){
			uriAuthor.put(uri, new Author(uri, label));
		}
		
	}

}
