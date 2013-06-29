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

import java.util.ArrayList;
import java.util.List;

/**
 * Create an edge between two vertices, with a face on each side of the vertex
 * 
 * @author Joeri
 * 
 */
public class QuadEdge {

	private List<Triangle> triangles = new ArrayList<Triangle>();
	private List<Vertex> vertices = new ArrayList<Vertex>();
	private int id;

	/**
	 * Create an edge between two vertices, with a face on each side of the
	 * vertex
	 * 
	 * @param origin
	 *            starting vertex
	 * @param destination
	 *            ending vertex
	 * @param left
	 *            face on left side
	 * @param right
	 *            face on right side
	 */
	public QuadEdge(Vertex origin, Vertex destination, Triangle left,
			Triangle right, int id) {
		this.id = id;
		// Add connection between vertices
		destination.addNeighbour(origin.getID());
		origin.addNeighbour(destination.getID());
		// Add vertices to edge and edge id to vertices
		origin.addEdge(id);
		vertices.add(origin);
		destination.addEdge(id);
		vertices.add(destination);
		// Add triangles to edge and edge id to triangles (if triangle exists)
		if (left != null) {
			left.addEdge(id);
			triangles.add(left);
		}
		if (right != null) {
			right.addEdge(id);
			triangles.add(right);
		}

	}

	/**
	 * Get the id of the edge
	 * 
	 * @return the id of the edge
	 */
	public int getID() {
		return id;
	}

	/**
	 * Add triangle t to this edge
	 * 
	 * @param t
	 *            triangle to be added
	 */
	public void addTriangle(Triangle t) {
		if (triangles.contains(null)) {
			triangles.remove(null);
		}
		t.addEdge(id);
		triangles.add(t);
	}

	/**
	 * Remove triangle t from this edge
	 * 
	 * @param t
	 *            triangle to be removed
	 */
	public void removeTriangle(Triangle t) {
		triangles.remove(t);
		// No need to remove edge from triangle, as removing an edge implies
		// removing a triangle
	}

	/**
	 * Get list of triangles connected to this edge
	 * 
	 * @return list of triangles connected to this edge
	 */
	public List<Triangle> getTriangles() {
		return triangles;
	}

	/**
	 * Get list of vertices connected to this edge
	 * 
	 * @return connected to this edge
	 */
	public List<Vertex> getVertices() {
		return vertices;
	}

	/**
	 * For drawing Delaunay edge
	 * 
	 * @param g
	 *            graphic
	 */
	public Vertex[] getEdge() {
		Vertex[] line = new Vertex[2];
		line[0] = getVertices().get(0);
		line[1] = getVertices().get(1);
		return line;
	}

	/**
	 * For drawing Voronoi edge
	 * 
	 * @param g
	 *            graphic
	 */
	public Vertex[] getVoronoiEdge() {
		Vertex[] line = null;
		if (triangles.size() == 2) {
			line = new Vertex[2];
			line[0] = triangles.get(0).getCentre();
			line[1] = triangles.get(1).getCentre();
		}
		return line;
	}
}
