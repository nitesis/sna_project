package author.dbpedia;

import it.uniroma1.dis.wsngroup.gexf4j.core.Edge;
import it.uniroma1.dis.wsngroup.gexf4j.core.EdgeType;
import it.uniroma1.dis.wsngroup.gexf4j.core.Gexf;
import it.uniroma1.dis.wsngroup.gexf4j.core.Graph;
import it.uniroma1.dis.wsngroup.gexf4j.core.Mode;
import it.uniroma1.dis.wsngroup.gexf4j.core.Node;
import it.uniroma1.dis.wsngroup.gexf4j.core.data.Attribute;
import it.uniroma1.dis.wsngroup.gexf4j.core.data.AttributeClass;
import it.uniroma1.dis.wsngroup.gexf4j.core.data.AttributeList;
import it.uniroma1.dis.wsngroup.gexf4j.core.data.AttributeType;
import it.uniroma1.dis.wsngroup.gexf4j.core.impl.GexfImpl;
import it.uniroma1.dis.wsngroup.gexf4j.core.impl.StaxGraphWriter;
import it.uniroma1.dis.wsngroup.gexf4j.core.impl.data.AttributeListImpl;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.time.LocalDate;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import author.model.Author;
import author.model.AuthorGraph;

/**
 * Exports the music artist graph to Gephi
 *
 */
public class AuthorGephiExport {
	private static final Logger LOG = LoggerFactory.getLogger(AuthorGephiExport.class);
	private static final String OUTPUT_FOLDER = "output/";
	
	private final String OUTPUT_FILE;

	// Node attributes
	AttributeList attrList = new AttributeListImpl(AttributeClass.NODE);


	Attribute attAge = attrList.createAttribute(AttributeType.INTEGER, "age");
	Attribute attDeath = attrList.createAttribute(AttributeType.INTEGER, "deathyear");
	Attribute attGenres = attrList.createAttribute(AttributeType.LISTSTRING,
			"genres");
	Attribute attInfluencedBy = attrList.createAttribute(AttributeType.LISTSTRING, "influencedBy");
	Attribute attInfluenced = attrList.createAttribute(AttributeType.LISTSTRING, "influenced");
	Attribute attRelatives = attrList.createAttribute(AttributeType.LISTSTRING, "relatives");
	Attribute attSpouse = attrList.createAttribute(AttributeType.STRING, "spouse");


	
	public AuthorGephiExport(String outputFile) {
		this.OUTPUT_FILE = outputFile;
	}
	
	public void export(AuthorGraph artistGraph) {
		Gexf gexf = initializeGexf();
		Graph graph = gexf.getGraph();
		LOG.info("Creating gephi Graph");
		createGraph(graph, artistGraph);
		writeGraphToFile(gexf);
		LOG.info("Finished Gephi export");
	}


	private void writeGraphToFile(Gexf gexf) {
		StaxGraphWriter graphWriter = new StaxGraphWriter();
		File f = new File(OUTPUT_FOLDER + OUTPUT_FILE);
		Writer out;
		try {
			out = new FileWriter(f, false);
			graphWriter.writeToStream(gexf, out, "UTF-8");
			LOG.info("Stored graph in file: " +f.getAbsolutePath());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void createGraph(Graph graph, AuthorGraph artistGraph) {
		LOG.info("Creating nodes");
		Map<String, Node> nodeMap = createNodes(artistGraph, graph);
		LOG.info("Creating edges");
		createEdges(artistGraph, graph, nodeMap);
	}

	private Map<String, Node> createNodes(AuthorGraph artists, Graph graph) {
		Map<String, Node> nodeMap = new HashMap<>(
				artists.getAuthors().size() * 2);

		for (Author artist : artists.getAuthors()) {
			if (!nodeMap.containsKey(artist.getUri())) {
				nodeMap.put(artist.getUri(), createSingleNode(graph, artist));
			}
		}

		return nodeMap;
	}

	private Node createSingleNode(Graph graph, Author author) {
		Node node = graph.createNode(author.getUri()).setLabel(author.getLabel());
		
//		
		if (author.getDeathYear() != null) {
			node.getAttributeValues().addValue(attDeath, String.valueOf(author.getDeathYear().getYear()));
		}
		
		if (author.getGenres() != null) {
			node.getAttributeValues().addValue(attGenres, author.getGenres());
		}
		
		if (author.getInfluencedBy() != null ) {
			node.getAttributeValues().addValue(attInfluencedBy, author.getInfluencedBy());
		}
		
		if (author.getInfluenced() != null ) {
			node.getAttributeValues().addValue(attInfluenced, author.getInfluenced());
		}
		
		if (author.getRelatives() != null ) {
			node.getAttributeValues().addValue(attRelatives, author.getRelatives());
		}
		
		if (author.getSpouse() != null ) {
			node.getAttributeValues().addValue(attSpouse, author.getSpouse());
		}
		
		return node;
	}

	
	private void createEdges(AuthorGraph artistGraph, Graph graph,
			Map<String, Node> nodeMap) {
		for (Map.Entry<String, Set<String>> from : artistGraph.getAssociations()
				.entrySet()) {
			for (String to : from.getValue()) {
				createOrUpdateEdge(graph, nodeMap, from.getKey(), to);
			}
		}
	}

	private void createOrUpdateEdge(Graph graph, Map<String, Node> nodeMap,
			String from, String to) {
		Node source = nodeMap.get(from);
		Node target = nodeMap.get(to);
		if (source == null || target == null)
			throw new IllegalStateException(
					"Source and Target must not be null. Every node must be added to network as a node before using it as a edge.");
		if (source.hasEdgeTo(to)) {
			Edge edge = getEdgeBetween(source, target);
			edge.setWeight(edge.getWeight() + 1f);

		} else if (target.hasEdgeTo(from)) {
			Edge edge = getEdgeBetween(target, source);
			edge.setWeight(edge.getWeight() + 1f);

		} else {
			source.connectTo(target).setWeight(1f);
		}

	}

	private Edge getEdgeBetween(Node source, Node target) {
		for (Edge edge : source.getEdges()) {
			if (edge.getTarget().equals(target)) {
				return edge;
			}
		}
		return null;
	}

	private Gexf initializeGexf() {
		Gexf gexf = new GexfImpl();
		gexf.getMetadata().setLastModified(Calendar.getInstance().getTime())
				.setCreator("SNA Dbpedia Graph");
		gexf.setVisualization(true);

		Graph graph = gexf.getGraph();
		graph.setDefaultEdgeType(EdgeType.DIRECTED);
		graph.setMode(Mode.STATIC);
		graph.getAttributeLists().add(attrList);
		return gexf;
	}

}
