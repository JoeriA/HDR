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

import java.util.HashSet;
import java.util.Set;

/**
 * A triangle
 * 
 * @author Joeri
 * 
 */
public class Triangle {

	private Vertex[] vertices = new Vertex[3];
	private Vertex circumcentre;
	private double radius;
	private Set<Integer> edges = new HashSet<Integer>();

	/**
	 * Create a triangle
	 * 
	 * @param a
	 *            first vertex
	 * @param b
	 *            second vertex
	 * @param c
	 *            third vertex
	 */
	public Triangle(Vertex a, Vertex b, Vertex c) {
		vertices[0] = a;
		vertices[1] = b;
		vertices[2] = c;
	}

	/**
	 * Add edge with id hash to this triangle
	 * 
	 * @param hash
	 *            id of edge to be added
	 */
	public void addEdge(int hash) {
		edges.add(hash);
	}

	/**
	 * Get edges in this triangle
	 * 
	 * @return edges in this triangle
	 */
	public Set<Integer> getEdges() {
		return edges;
	}

	/**
	 * Get list of vertices
	 * 
	 * @return list of vertices
	 */
	public Vertex[] getVertices() {
		return vertices;
	}

	/**
	 * Check if given point is located in circumcircle
	 * 
	 * @param p
	 *            point to be checked
	 * @return true if point is located in circumcircle
	 */
	public boolean inCircumcircle(Vertex p) {

		// Calculate circumcentre when necessary
		if (circumcentre == null) {
			calcCircumcircle();
		}

		// Check whether distance between p and centre is smaller than radius
		boolean inCircle = false;
		if (distance(p, circumcentre) <= radius) {
			inCircle = true;
		}
		return inCircle;

	}

	/**
	 * Calculate centre and radius of circumcircle
	 */
	private void calcCircumcircle() {
		// First calculate centre of circumcircle
		Vertex a = vertices[0];
		Vertex b = vertices[1];
		Vertex c = vertices[2];

		double dA = Math.pow(a.x(), 2) + Math.pow(a.y(), 2);
		double dB = Math.pow(b.x(), 2) + Math.pow(b.y(), 2);
		double dC = Math.pow(c.x(), 2) + Math.pow(c.y(), 2);

		double d = 2 * (a.x() * (b.y() - c.y()) + b.x() * (c.y() - a.y()) + c
				.x() * (a.y() - b.y()));
		double x = ((dA) * (b.y() - c.y()) + (dB) * (c.y() - a.y()) + (dC)
				* (a.y() - b.y()))
				/ d;
		double y = ((dA) * (c.x() - b.x()) + (dB) * (a.x() - c.x()) + (dC)
				* (b.x() - a.x()))
				/ d;
		circumcentre = new Vertex(x, y);

		// Calculate radius
		radius = distance(a, circumcentre);
	}

	/**
	 * Calculate distance between points a and b
	 * 
	 * @param a
	 *            first point
	 * @param b
	 *            second point
	 * @return distance between a and b
	 */
	private double distance(Vertex a, Vertex b) {
		double dx = Math.abs(b.x() - a.x());
		double dy = Math.abs(b.y() - a.y());
		return Math.sqrt(Math.pow(dx, 2) + Math.pow(dy, 2));
	}

	/**
	 * Get centre of circumcircle (for testing)
	 * 
	 * @return centre of circumcircle
	 */
	public Vertex getCentre() {
		// Calculate circumcentre when necessary
		if (circumcentre == null) {
			calcCircumcircle();
		}
		return circumcentre;
	}

	/**
	 * Get radius of circumcircle (for testing)
	 * 
	 * @return radius of circumcircle
	 */
	public double getRadius() {
		// Calculate circumcentre when necessary
		if (circumcentre == null) {
			calcCircumcircle();
		}
		return radius;
	}

	/**
	 * Calculate area of the triangle
	 * 
	 * @return area of the triangle
	 */
	public double getArea() {
		Vertex a = vertices[0];
		Vertex b = vertices[1];
		Vertex c = vertices[2];
		double area = Math.abs((a.x() - c.x()) * (b.y() - a.y())
				- (a.x() - b.x()) * (c.y() - a.y())) * 0.5;
		return area;
	}

}
