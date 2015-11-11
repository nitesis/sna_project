package ch.fhnw.sna.examples.dbpedia.model;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

public class AuthorGraph {

	private Map<String, Author> uriToArtist = Maps.newHashMap();
	private Map<String, Set<String>> associations = Maps.newHashMap();

	public void addArtist(Author artist) {
		uriToArtist.put(artist.getUri(), artist);
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

	public boolean containsArtist(String uri) {
		return uriToArtist.containsKey(uri);
	}

	public Collection<Author> getArtists() {
		return uriToArtist.values();
	}

	public void addArtistIfNotExists(String uri, String label) {
		if (!uriToArtist.containsKey(uri)){
			uriToArtist.put(uri, new Author(uri, label));
		}
		
	}

}
