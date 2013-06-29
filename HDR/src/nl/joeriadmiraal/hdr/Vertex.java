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
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Class to create a point in 2D space
 * 
 * @author Joeri
 * 
 */
public class Vertex {

	private double[] coords = new double[2];
	private Set<Integer> edges = new HashSet<Integer>();
	private List<Vertex> voronoi = new ArrayList<Vertex>();
	private Set<Integer> neighbours = new HashSet<Integer>();
	private boolean bound;
	private boolean inHDR;
	private double area;
	private int id;
	private int duplicates;

	/**
	 * Create a point in 2D space
	 * 
	 * @param xCoord
	 *            x-coordinate
	 * @param yCoord
	 *            y-coordinate
	 */
	public Vertex(double xCoord, double yCoord) {
		coords[0] = xCoord;
		coords[1] = yCoord;
		bound = false;
		duplicates = 0;
	}

	/**
	 * Set id number of this vertex
	 * 
	 * @param id
	 *            id number of this vertex
	 */
	public void setID(int id) {
		this.id = id;
	}

	/**
	 * Set state of hdr, true if vertex is in hdr
	 * 
	 * @param inHDR
	 *            state of hdr, true if vertex is in hdr
	 */
	public void setInHDR(boolean inHDR) {
		this.inHDR = inHDR;
	}

	/**
	 * Get state of hdr, true if vertex is in hdr
	 * 
	 * @return state of hdr, true if vertex is in hdr
	 */
	public boolean isInHDR() {
		return inHDR;
	}

	/**
	 * Increase amount of duplicates
	 */
	public void addDuplicate() {
		duplicates += 1;
	}

	/**
	 * Get the amount of duplicates of this vertex
	 * 
	 * @return the amount of duplicates of this vertex
	 */
	public int getNrDuplicates() {
		return duplicates;
	}

	/**
	 * Get id number of this vertex
	 * 
	 * @return id number of this vertex
	 */
	public int getID() {
		return id;
	}

	/**
	 * Add vertex to neighbour of this vertex
	 * 
	 * @param i
	 *            id of neighbour vertex to be added
	 */
	public void addNeighbour(int i) {
		neighbours.add(i);
	}

	/**
	 * Remove vertex as neighbour
	 * 
	 * @param i
	 *            id of neighbour to be removed
	 */
	public void removeNeighbour(int i) {
		neighbours.remove(i);
	}

	/**
	 * Get the id's of neighbours
	 * 
	 * @return list of id's of neighbours
	 */
	public Set<Integer> getNeighbours() {
		return neighbours;
	}

	/**
	 * Add Voronoi vertex to this vertex
	 * 
	 * @param v
	 *            vertex of Voronoi cell of this vertex
	 */
	public void addVoronoiEdge(QuadEdge e) {
		Vertex temp;
		// Check if this edge has 1 or 2 triangles
		if (e.getTriangles().size() == 1) {
			// If it has 1, it is a bound
			bound = true;
			// Add centre of triangle to Voronoi
			temp = e.getTriangles().get(0).getCentre();
			if (!voronoi.contains(temp)) {
				voronoi.add(temp);
			}
		} else if (e.getTriangles().size() == 2) {
			// Add both centres of triangles to Voronoi
			for (int i = 0; i < 2; i++) {
				temp = e.getTriangles().get(i).getCentre();
				if (!voronoi.contains(temp)) {
					voronoi.add(temp);
				}
			}
		}
	}

	/**
	 * Get set of edges connected to this vertex
	 * 
	 * @return set of edges connected to this vertex
	 */
	public Set<Integer> getEdges() {
		return edges;
	}

	/**
	 * Get x-coordinate of this vertex
	 * 
	 * @return x-coordinate of this vertex
	 */
	public double x() {
		return coords[0];
	}

	/**
	 * Get y-coordinate of this vertex
	 * 
	 * @return y-coordinate of this vertex
	 */
	public double y() {
		return coords[1];
	}

	/**
	 * Remove edge with id hash from this vertex
	 * 
	 * @param hash
	 *            id of edge to be removed
	 */
	public void removeEdge(int hash) {
		edges.remove(hash);
	}

	/**
	 * Add edge with id hash to this vertex
	 * 
	 * @param hash
	 *            id of edge to be added
	 */
	public void addEdge(int hash) {
		if (edges.contains(hash))
			throw new IllegalArgumentException(
					"Vertex already contains this edge");
		edges.add(hash);
	}

	/**
	 * Set this cell to a bound or not
	 * 
	 * @param bound
	 *            true if this cell is a bound
	 */
	public void setBound(boolean bound) {
		this.bound = bound;
	}

	/**
	 * Check if this cell is a bound
	 * 
	 * @return true if this cell is a bound
	 */
	public boolean isBound() {
		return bound;
	}

	/**
	 * Get the area of this Voronoi cell
	 * 
	 * @return the area of this Voronoi cell
	 */
	public double getArea() {
		// Calculate area if not already done
		if (area == 0)
			calcArea();
		return area;
	}

	/**
	 * Set area size (used for boundary cells)
	 * 
	 * @param newArea
	 *            size of new area
	 */
	public void setArea(double newArea) {
		area = newArea;
	}

	/**
	 * Calculate the area of this Voronoi cell
	 */
	public void calcArea() {
		if (bound) {
			area = Double.POSITIVE_INFINITY;
		} else {
			// Sort vertices in clockwise order
			sortVoronoiCell();

			// Calculate area
			double sum = 0.0;
			Vertex current;
			Vertex previous;
			for (int i = 0; i < voronoi.size(); i++) {
				current = voronoi.get(i);
				if (i == 0) {
					previous = voronoi.get(voronoi.size() - 1);
				} else {
					previous = voronoi.get(i - 1);
				}
				sum += (previous.x() + current.x())
						* (previous.y() - current.y());
			}
			area = (sum / 2.0) / (1 + duplicates);
		}
	}

	private void sortVoronoiCell() {
		// Get a vertex of the cell for comparison
		final double mx = coords[0];
		final double my = coords[1];
		// Sort vertices, using relative angle to centre
		Collections.sort(voronoi, new Comparator<Vertex>() {
			public int compare(Vertex a, Vertex b) {
				// This function sorts vertices in clockwise order, starting at
				// 12 o'clock
				// Check if one part is in left part and other in right part (of
				// clock)
				if (a.x() >= mx && b.x() < mx) {
					return -1;
				}
				if (a.x() <= mx && b.x() > mx) {
					return 1;
				}
				if (a.x() == mx && b.x() == mx) {
					if (a.y() > b.y()) {
						return -1;
					} else {
						return 1;
					}
				}
				// Otherwise, compute the cross product of vectors
				double det = (a.x() - mx) * (b.y() - my) - (b.x() - mx)
						* (a.y() - my);
				if (det > 0) {
					return 1;
				} else {
					return -1;
				}
			}
		});
	}

	/**
	 * Get Voronoi vertices
	 * 
	 * @return Voronoi vertices
	 */
	public List<Vertex> getVoronoiCell() {
		return voronoi;
	}
}
