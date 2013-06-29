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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GraphTheory {

	Map<Integer, Vertex> verticesMap = new HashMap<Integer, Vertex>();
	List<Vertex> verticesSet;
	double area;

	/**
	 * Create graph of vertices with Voronoi data
	 * 
	 * @param unsortedVertices
	 *            all vertices/cells in the Voronoi diagram
	 */
	public GraphTheory(List<Vertex> unsortedVertices) {
		// Put vertices in map, to find neighbours quickly and to (new!) list
		for (Vertex v : unsortedVertices) {
			verticesMap.put(v.getID(), v);
		}
		verticesSet = unsortedVertices;
	}

	/**
	 * Compute HDR, including only the 1-alpha smallest cells. Does not ensure
	 * connected graph without holes.
	 * 
	 * @param nrToRemove
	 *            number of nodes to remove
	 */
	public void simple(int nrToRemove) {
		Vertex temp;
		// Loop through all vertices
		for (int i = 0; i < verticesSet.size(); i++) {
			temp = verticesSet.get(i);
			// If there are more vertices to remove, remove cell
			if (temp.getNrDuplicates() < nrToRemove) {
				temp.setInHDR(false);
				nrToRemove -= temp.getNrDuplicates() + 1;
			} else {
				temp.setInHDR(true);
			}
		}
	}

	/**
	 * Calculate the HDR containing 1-alpha vertices with top-down approach
	 * 
	 * @param alpha
	 *            number of vertices to be deleted
	 */
	public void topDown(int nrToRemove) {
		// Sort vertices
		sortVertices(true);

		// Begin with all nodes in the HDR
		for (Vertex v : verticesSet) {
			v.setInHDR(true);
		}

		// Remove cells
		int cellNr;
		Vertex check, toRemove;
		while (nrToRemove > 0) {
			toRemove = null;
			cellNr = 0;
			// Find boundary cell with biggest area
			while (toRemove == null) {
				check = verticesSet.get(cellNr);
				// Check if selected vertex is in hdr and does not exceed
				// nrToRemove
				if (check.isInHDR() && check.getNrDuplicates() < nrToRemove) {
					if (check.isBound()) {
						// If vertex is bound, it is always ok to remove
						toRemove = check;
					} else if (checkSwitches(check)) {
						// If vertex has 2 switches, it is ok to remove
						toRemove = check;
					}
				}
				cellNr++;
			}
			// Remove this cell
			toRemove.setInHDR(false);
			nrToRemove -= toRemove.getNrDuplicates() + 1;
		}
	}

	/**
	 * Compute the HDR with the bottom-up approach with nrToAdd observations
	 * 
	 * @param nrToAdd
	 *            the number of observations to be included in the HDR
	 */
	public void bottomUp(int nrToAdd) {
		// Sort vertices
		sortVertices(false);

		// Set all nodes not in HDR
		for (Vertex v : verticesSet) {
			v.setInHDR(false);
			v.setBound(false);
		}

		int cellNr;
		Vertex check, toAdd;

		// First add smallest cell to HDR
		toAdd = verticesSet.get(0);
		toAdd.setInHDR(true);
		nrToAdd -= toAdd.getNrDuplicates() + 1;

		// Find node with lowest value to add to HDR
		while (nrToAdd > 0) {
			toAdd = null;
			cellNr = 0;
			while (toAdd == null) {
				check = verticesSet.get(cellNr);
				// Check if cell is not yet in HDR, has 2 switches and does not
				// exceed nrToAdd
				if (!check.isInHDR() && check.getNrDuplicates() < nrToAdd
						&& checkSwitches(check)) {
					toAdd = check;
				} else {
					cellNr++;
				}
			}
			// Add this node to the HDR
			toAdd.setInHDR(true);
			nrToAdd -= toAdd.getNrDuplicates() + 1;
		}
	}

	/**
	 * Get the amount of switches this node has. Switch is when a to connected
	 * neighbours are not both inside or outside HDR. When there are 2 switches,
	 * removal does not create a hole and HDR remains connected
	 * 
	 * @param toCheck
	 *            node to be added or removed
	 * @return true if number of switches exceeds two.
	 */
	private boolean checkSwitches(Vertex toCheck) {
		// Get clockwise sorted neighbours
		List<Vertex> sortedNeighbours = getSortedNeighbours(toCheck);
		int switches = 0;
		// Look how many switches there are
		for (int i = 1; i < sortedNeighbours.size(); i++) {
			// A switch is when two adjacent neighbours are not in the same
			// state (in or out hdr)
			if ((!sortedNeighbours.get(i).isInHDR() && sortedNeighbours.get(
					i - 1).isInHDR())
					|| (sortedNeighbours.get(i).isInHDR() && !sortedNeighbours
							.get(i - 1).isInHDR())) {
				switches += 1;
			}
		}
		// Note that loop does not include check between first and last vertex.
		// Not necessairy as number of switches is always even. So when number
		// of switches is 1 or 2, removal does not result in holes or
		// non-connected HDR.
		if (switches > 2 || switches < 1) {
			return false;
		}
		return true;
	}

	/**
	 * Get clockwise sorted neighbours of toCheck
	 * 
	 * @param toCheck
	 *            node to get sorted neighbours of
	 * @return clockwise sorted neighbours of toCheck
	 */
	private List<Vertex> getSortedNeighbours(Vertex toCheck) {
		// Get neighbours
		List<Vertex> neighbours = new ArrayList<Vertex>();
		for (int neighbour : toCheck.getNeighbours()) {
			neighbours.add(verticesMap.get(neighbour));
		}
		// Get a vertex of the cell for comparison
		final double mx = toCheck.x();
		final double my = toCheck.y();
		// Sort vertices, using relative angle to centre
		Collections.sort(neighbours, new Comparator<Vertex>() {
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
		return neighbours;
	}

	/**
	 * Get area of HDR
	 * 
	 * @return area of HDR
	 */
	public double getArea() {
		double area = 0.0;
		// Get areas of all voronoi cells in hdr
		for (Vertex v : verticesSet) {
			if (v.isInHDR()) {
				area += v.getArea() * (1 + v.getNrDuplicates());
			}
		}
		return area;
	}

	/**
	 * Sort a list of vertices on Voronoi area
	 * 
	 * @param highToLow
	 *            true if sort from high to low, low to high otherwise
	 * @return sorted list of vertices (area)
	 */
	private void sortVertices(boolean highToLow) {
		// Create comparator on Voronoi cell area
		if (highToLow) {
			// Sort from high to low
			Collections.sort(verticesSet, new Comparator<Vertex>() {
				public int compare(Vertex a, Vertex b) {
					return new Double(b.getArea()).compareTo(new Double(a
							.getArea()));
				}
			});
		} else {
			// Sort from low to high
			Collections.sort(verticesSet, new Comparator<Vertex>() {
				public int compare(Vertex a, Vertex b) {
					return new Double(a.getArea()).compareTo(new Double(b
							.getArea()));
				}
			});
		}
	}
}
