/*
 * Copyright (c) 2013, Joeri Admiraal
 *
 * Permission to use, copy, modify, and/or distribute this software for any purpose
 * with or without fee is hereby granted, provided that the above copyright notice
 * and this permission notice appear in all copies.
 *
 * THE SOFTWARE IS PROVIDED "AS IS" AND THE AUTHOR DISCLAIMS ALL WARRANTIES WITH REGARD
 * TO THIS SOFTWARE INCLUDING ALL IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS.
 * IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY SPECIAL, DIRECT, INDIRECT, OR
 * CONSEQUENTIAL DAMAGES OR ANY DAMAGES WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR
 * PROFITS, WHETHER IN AN ACTION OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS ACTION,
 * ARISING OUT OF OR IN CONNECTION WITH THE USE OR PERFORMANCE OF THIS SOFTWARE.
 */

package nl.joeriadmiraal.hdr;

import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Implementation of Bowyer-Watson algorithm for Delaunay triangulation
 * 
 * @author Joeri
 * 
 */
public class Watson {

	private List<Vertex> vertices;
	private List<Vertex> stVertices = new ArrayList<Vertex>(3);
	private List<Triangle> triangles = new ArrayList<Triangle>();
	private Map<Integer, QuadEdge> edges = new HashMap<Integer, QuadEdge>();
	private int nrEdges = 0;
	private Rectangle bounds = new Rectangle();

	/**
	 * Create Delaunay triangulation for observations
	 * 
	 * @param observations
	 *            observations to create delaunay triangulation
	 */
	public Watson(double[][] observations) {

		int nrOfVertices = observations.length;
		vertices = new ArrayList<Vertex>(nrOfVertices);
		Map<Double, Map<Double, Vertex>> coords = new HashMap<Double, Map<Double, Vertex>>();

		// Add observations to list of vertices (and also calculate minimum and
		// maximum y-coordinates for later)
		Vertex temp;
		boolean xExists;
		for (int i = 0; i < nrOfVertices; i++) {
			xExists = false;
			// Check if vertex is a duplicate
			// First check x-coordinate
			if (coords.containsKey(observations[i][0])) {
				xExists = true;
				// Then check y-coordinate
				if (coords.get(observations[i][0]).containsKey(
						observations[i][1])) {
					// If vertex exists, add duplicate
					coords.get(observations[i][0]).get(observations[i][1])
							.addDuplicate();
					System.out.println("Duplicate");
					break;
				}
			}
			temp = new Vertex(observations[i][0], observations[i][1]);
			temp.setID(i);
			vertices.add(temp);
			bounds.add(temp.x(), temp.y());
			// Add vertex to temporary lists to check for duplicates
			// If x-coordinate does not exists, create new map
			if (!xExists) {
				coords.put(observations[i][0], new HashMap<Double, Vertex>());
			}
			// Add vertex to map
			coords.get(observations[i][0]).put(observations[i][1], temp);
		}

		// Sort observations on x-coordinate
		Collections.sort(vertices, new Comparator<Vertex>() {
			public int compare(Vertex a, Vertex b) {
				return new Double(a.x()).compareTo(new Double(b.x()));
			}
		});

		// Create super triangle (inspired by Sierpinski)
		createSuperTriangle();

		// Add points one by one
		for (Vertex v : vertices) {
			addPoint(v);
		}

		// Remove super triangle
		removeST();
	}

	/**
	 * Create super triangle around square with given coordinates
	 * 
	 * @param xMin
	 *            x-coordinate of down bar
	 * @param xMax
	 *            x-coordinate of top bar
	 * @param yMin
	 *            y-coordinate of left bar
	 * @param yMax
	 *            y-coordinate of right bar
	 */
	private void createSuperTriangle() {
		// Calculate coordinates of the three vertices of super triangle (left,
		// right and top)
		double xMin = bounds.getMinX();
		double xMax = bounds.getMaxX();
		double yMin = bounds.getMinY();
		double yMax = bounds.getMaxY();
		double xLeft = 1.5 * xMin - 0.5 * xMax;
		double xRight = -0.5 * xMin + 1.5 * xMax;
		double xTop = 0.5 * xMin + 0.5 * xMax;
		double yTop = -yMin + 2 * yMax;

		// Create vertices of super triangle
		Vertex left = new Vertex(xLeft, yMin);
		stVertices.add(left);
		Vertex right = new Vertex(xRight, yMin);
		stVertices.add(right);
		Vertex top = new Vertex(xTop, yTop);
		stVertices.add(top);

		// Create triangle
		triangles.add(new Triangle(left, top, right));

		// Create edges
		edges.put(nrEdges, new QuadEdge(left, top, null, triangles.get(0),
				nrEdges));
		nrEdges++;
		edges.put(nrEdges, new QuadEdge(top, right, null, triangles.get(0),
				nrEdges));
		nrEdges++;
		edges.put(nrEdges, new QuadEdge(right, left, null, triangles.get(0),
				nrEdges));
		nrEdges++;
	}

	/**
	 * Remove super triangle
	 */
	private void removeST() {
		Set<Integer> stEdges = new HashSet<Integer>();
		Set<Triangle> stTriangles = new HashSet<Triangle>();
		// Get all edges and triangles connected to vertices of super triangle
		for (Vertex v : stVertices) {
			for (int i : v.getEdges()) {
				stEdges.add(i);
				for (Triangle t : edges.get(i).getTriangles()) {
					stTriangles.add(t);
				}
			}
		}
		// Remove all triangles connected to vertices of super triangle
		for (Triangle t : stTriangles) {
			removeTriangle(t);
		}

		// Remove all edges connected to vertices of super triangle
		for (int i : stEdges) {
			removeEdge(i);
		}
	}

	/**
	 * Add vertex v to the Delaunay triangulation
	 * 
	 * @param v
	 *            vertex to be added
	 */
	private void addPoint(Vertex v) {
		// Find faulty triangles and get vertices of boundaries
		List<Triangle> faulty = new ArrayList<Triangle>();
		Set<Integer> bounds = new HashSet<Integer>();
		Set<Integer> toRemove = new HashSet<Integer>();

		// This uses a while loop instead
		Set<Triangle> toCheck = new HashSet<Triangle>();
		Set<Triangle> ok = new HashSet<Triangle>();
		int i = triangles.size() - 1;
		boolean found = false;
		Triangle temp;
		// Find a faulty triangle (there has to be at least one)
		while (!found) {
			temp = triangles.get(i);
			if (temp.inCircumcircle(v)) {
				toCheck.add(temp);
				found = true;
			}
			i--;
		}
		// Check neighbours of faulty triangle and their neighbours
		while (!toCheck.isEmpty()) {
			// Process next triangle
			temp = toCheck.iterator().next();
			// If it is faulty, mark edges
			if (temp.inCircumcircle(v)) {
				faulty.add(temp);
				// Find edges that should be removed (both triangles of edge
				// are
				// faulty)
				for (Integer edgeID : temp.getEdges()) {
					// If this is second triangle of edge, this is faulty
					if (bounds.contains(edgeID)) {
						toRemove.add(edgeID);
						bounds.remove(edgeID);
					}
					// If this is first triangle of edge, add to bounds
					else {
						bounds.add(edgeID);
					}
				}
				// Find neighbours of faulty triangle
				for (int edge : temp.getEdges()) {
					for (Triangle neighbour : edges.get(edge).getTriangles()) {
						// Only process if it exists
						if (neighbour != null) {
							// Check if it is not already processed
							if (!toCheck.contains(neighbour)
									&& !ok.contains(neighbour)
									&& !faulty.contains(neighbour)) {
								toCheck.add(neighbour);
							}
						}
					}
				}
				// If not faulty add to ok triangles
			} else {
				ok.add(temp);
			}
			toCheck.remove(temp);
		}

		// Remove faulty triangles
		for (Triangle t : faulty) {
			removeTriangle(t);
		}

		// Remove faulty edges
		for (Integer edgeID : toRemove) {
			removeEdge(edgeID);
		}

		// Remove already removed triangles from boundary edges
		QuadEdge bound;
		for (int edgeID : bounds) {
			bound = edges.get(edgeID);
			for (Triangle t : faulty) {
				if (bound.getTriangles().contains(t)) {
					bound.getTriangles().remove(t);
				}
			}
		}
		// Add new edges from boundary points to v
		// (use edges from bounds!)
		QuadEdge tempQE;
		QuadEdge tempQE2;
		Triangle tempT;
		Map<Vertex, QuadEdge> done = new HashMap<Vertex, QuadEdge>();
		for (int ID : bounds) {
			// Get triangle from id
			tempQE = edges.get(ID);

			// Create triangle between boundary edge and new point
			tempT = new Triangle(tempQE.getVertices().get(0), tempQE
					.getVertices().get(1), v);
			triangles.add(tempT);
			// Add triangle to edge
			tempQE.addTriangle(tempT);

			// Construct other two edges
			for (int vertexNr = 0; vertexNr < 2; vertexNr++) {
				// Check whether edge already exists
				if (!done.containsKey(tempQE.getVertices().get(vertexNr))) {
					// If not, create new edge
					tempQE2 = new QuadEdge(tempQE.getVertices().get(vertexNr),
							v, tempT, null, nrEdges);
					edges.put(nrEdges, tempQE2);
					nrEdges++;
					done.put(tempQE.getVertices().get(vertexNr), tempQE2);
				} else {
					// If it is, add triangle to existing edge
					done.get(tempQE.getVertices().get(vertexNr)).addTriangle(
							tempT);
				}
			}
		}
	}

	/**
	 * Calculate Voronoi data for all vertices (area)
	 */
	public void calcVoronoi() {
		// Generate voronoi data for each point
		for (Vertex v : vertices) {
			// Get voronoi cell vertices
			for (int i : v.getEdges()) {
				v.addVoronoiEdge(edges.get(i));
			}
			// Calculate area
			v.calcArea();
		}
	}

	/**
	 * Remove edge with nr id
	 * 
	 * @param id
	 *            id of edge
	 */
	private void removeEdge(int id) {
		QuadEdge q = edges.get(id);
		// Remove connection between vertices
		q.getVertices().get(0).removeNeighbour(q.getVertices().get(1).getID());
		q.getVertices().get(1).removeNeighbour(q.getVertices().get(0).getID());
		// Remove edge id from vertices
		q.getVertices().get(0).removeEdge(id);
		q.getVertices().get(1).removeEdge(id);
		// Edge does not have to be removed from triangles, as triangles should
		// also be deleted in this case

		// Remove edge from map
		edges.remove(id);

	}

	/**
	 * Remove triangle t
	 * 
	 * @param t
	 *            triangle to be removed
	 */
	private void removeTriangle(Triangle t) {
		// Remove triangle from edges it has
		for (int i : t.getEdges()) {
			edges.get(i).removeTriangle(t);
		}
		// Remove triangle from list
		triangles.remove(t);
	}

	/**
	 * Get vertices
	 * 
	 * @return vertices
	 */
	public List<Vertex> getVertices() {
		return vertices;
	}

	/**
	 * Get edges
	 * 
	 * @return edges
	 */
	public Map<Integer, QuadEdge> getEdges() {
		return edges;
	}

	/**
	 * Get area of triangulation
	 * 
	 * @return area of triangulation
	 */
	public double getArea() {
		double area = 0.0;
		// Get all areas of all triangles
		for (Triangle t : triangles) {
			area += t.getArea();
		}
		return area;
	}

	/**
	 * Get dimensions of data set
	 * 
	 * @return dimensions of data set
	 */
	public Rectangle getDimensions() {
		return bounds;
	}
}
