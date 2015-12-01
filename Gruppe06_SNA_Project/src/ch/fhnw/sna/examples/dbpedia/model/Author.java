package ch.fhnw.sna.examples.dbpedia.model;

import java.time.LocalDate;
import java.util.Set;

import com.google.common.base.Joiner;
import com.google.common.collect.Sets;

public class Author {

	private final String uri;
	private Set<String> influencedBy = Sets.newHashSet();
	private Set<String> influenced = Sets.newHashSet();
	private String label;
	private LocalDate birthYear = null;
	private LocalDate deathYear = null;
	private Set <String> relatives = Sets.newHashSet();
	private String spouse;
	private Set<String> genres = Sets.newHashSet();
	

	public Author(String uri) {
		this.uri = uri;
	}

	public Author(String uri, String label) {
		this(uri);
		this.label = label;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((uri == null) ? 0 : uri.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Author other = (Author) obj;
		if (uri == null) {
			if (other.uri != null)
				return false;
		} else if (!uri.equals(other.uri))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "Artist [uri=" + uri + ", label=" + label + "]";
	}

	public String getUri() {
		return uri;
	}


	public void setbirthYear(LocalDate birthYear) {
		this.birthYear = birthYear;
	}

	public void addGenre(String genre) {
		genres.add(genre);
	}

	public LocalDate getbirthYear() {
		return birthYear;
	}

	public String getGenres() {
		return Joiner.on(';').join(genres);
	}

	public String getLabel() {
		return label;
	}

	public String getInfluencedBy() {
		return Joiner.on(';').join(influencedBy);
	}

	public void addInfluencedBy(String influencedBy) {
		this.influencedBy.add(influencedBy);
	}

	public String getInfluenced() {
		return Joiner.on(';').join(influenced);
	}

	public void addInfluenced(String influenced) {
		this.influenced.add(influenced);
	}

	public LocalDate getDeathYear() {
		return deathYear;
	}

	public void setDeathYear(LocalDate deathYear) {
		this.deathYear = deathYear;
	}

	public String getRelatives() {
		return Joiner.on(';').join(relatives);
	}

	public void addRelatives(String relatives) {
		this.relatives.add(relatives);
	}

	public String getSpouse() {
		return spouse;
	}

	public void setSpouse(String spouse) {
		this.spouse = spouse;
	}

}
