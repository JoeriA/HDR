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

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.geom.Ellipse2D;
import java.util.List;
import java.util.Map;

import javax.swing.JPanel;

/**
 * Construction to draw points and lines
 * 
 * @author Joeri
 * 
 */
public class Drawing extends JPanel {

	private static final long serialVersionUID = 1L;
	private List<Vertex> points;
	private Map<Integer, QuadEdge> lines;
	private boolean hdr = false;
	private double[][] theoretical;
	private Rectangle dimensions;
	private double scale, offsetX, offsetY;
	private Rectangle bounds;

	/**
	 * Draw points and lines
	 * 
	 * @param points
	 *            points to be drawed
	 * @param lines
	 *            lines to be drawed
	 */
	public Drawing(List<Vertex> points, Map<Integer, QuadEdge> lines) {
		this.points = points;
		this.lines = lines;
	}

	/**
	 * Set whether to draw the HDR or not
	 * 
	 * @param draw
	 *            true for drawing the HDR
	 */
	public void drawHDR(boolean draw) {
		hdr = draw;
	}

	/**
	 * Draw a symmetric theoretical prediction region
	 * 
	 * @param theoretical
	 *            x, y, width and height of the ellipse
	 */
	public void drawTheoretical(double[][] theoretical) {
		this.theoretical = theoretical;
	}

	/**
	 * Set dimensions of observations
	 * 
	 * @param dimensions
	 *            dimensions of observations
	 */
	public void setDimensions(Rectangle dimensions) {
		this.dimensions = dimensions;
	}

	/**
	 * Set dimensions of screen
	 * 
	 * @param bounds
	 *            dimensions of screen
	 */
	public void setBounds(Rectangle bounds) {
		this.bounds = bounds;
	}

	/**
	 * Draw
	 */
	public void paintComponent(Graphics g) {
		double scaleX = bounds.getWidth()
				/ dimensions.getWidth();
		double scaleY = bounds.getHeight()
				/ dimensions.getHeight();
		if (scaleY > scaleX) {
			scale = scaleX;
			offsetX = dimensions.getMinX();
			offsetY = dimensions.getMinY() * (scaleY / scaleX);
		} else {
			scale = scaleY;
			offsetX = dimensions.getMinX() * (scaleX / scaleY);
			offsetY = dimensions.getMinY();
		}

		g.setColor(Color.lightGray);
		// Draw HDR points
		if (hdr) {
			for (Vertex v : points) {
				// Only paint cell if it is in hdr
				if (v.isInHDR()) {
					Polygon p = new Polygon();
					for (Vertex vVor : v.getVoronoiCell()) {
						p.addPoint(transformX(vVor.x()), transformY(vVor.y()));
					}
					g.fillPolygon(p);
				}
			}
		}

		g.setColor(Color.gray);
		// Draw Delaunay lines
		if (lines != null) {
			for (QuadEdge e : lines.values()) {
				Vertex[] line = e.getEdge();
				int x1 = transformX(line[0].x());
				int y1 = transformY(line[0].y());
				int x2 = transformX(line[1].x());
				int y2 = transformY(line[1].y());
				//g.drawLine(x1, y1, x2, y2);
			}
		}

		g.setColor(Color.black);
		// Draw Voronoi lines
		if (lines != null) {
			for (QuadEdge e : lines.values()) {
				Vertex[] line = e.getVoronoiEdge();
				if (line != null) {
					int x1 = transformX(line[0].x());
					int y1 = transformY(line[0].y());
					int x2 = transformX(line[1].x());
					int y2 = transformY(line[1].y());
					g.drawLine(x1, y1, x2, y2);
				}
			}
		}

		g.setColor(Color.black);
		// Draw points
		if (points != null) {
			for (Vertex v : points) {
				int x = transformX(v.x());
				int y = transformY(v.y());
				g.fillRect(x - 1, y - 1, 2, 2);

			}
		}
		g.setColor(Color.red);
		// Draw theoretical circle
		if (theoretical != null) {
			Graphics2D g2 = (Graphics2D) g;
			double width = theoretical[0][1] * scale;
			double heigth = theoretical[1][1] * scale;
			double x = (theoretical[0][0] - offsetX) * scale - width;
			double y = bounds.getHeight() - (theoretical[1][0] - offsetY) * scale - heigth;
			g2.draw(new Ellipse2D.Double(x, y, width * 2.0, heigth * 2.0));
		}
	}

	private int transformX(Double d) {
		double transformed = (d - offsetX) * scale;
		return (int) Math.round(transformed);
	}

	private int transformY(Double d) {
		double transformed = bounds.getHeight() - (d - offsetY) * scale;
		return (int) Math.round(transformed);
	}

}