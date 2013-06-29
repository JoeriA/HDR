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
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.geom.Ellipse2D;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import javax.swing.JFrame;

/**
 * Main class
 * 
 * @author Joeri
 * 
 */
public class Main {

    /**
     * @param args
     */
    public static void main(String[] args) {

	// Do you want to create a new sample? If false, observations.txt will
	// be read (containing observations of last created sample).
	boolean newSample = true;
	// Amount of generated observations (only when creating new sample)
	int nrOfObs = 10000;
	// Percentage of observations to be excluded from the hdr
	double alpha = 0.1;
	// Which graph method to use?
	// 0 is simple, does not ensure connected graph without holes
	// 1 is top-down approach
	// 2 is bottom-up approach
	int method = 1;
	// Set parameters of normal distribution
	double[][] parameters = new double[2][2];
	// muX
	parameters[0][0] = 0.0;
	// sigmaX
	parameters[0][1] = 2.0;
	// muY
	parameters[1][0] = 0.0;
	// sigmaY
	parameters[1][1] = 1.0;
	// Independent?
	boolean independent = true;
	// Draw theoretical?
	boolean theoreticalDrawing = true;

	// Create frame for printing results
	final JFrame frame = new JFrame("Voronoi");
	frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	frame.setBackground(Color.white);
	Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
	frame.setSize(screenSize.width, screenSize.height - 40);
	frame.setExtendedState(Frame.MAXIMIZED_BOTH);

	// Generate observations
	double[][] observations;
	if (newSample) {
	    SampleNormal gen = new SampleNormal(nrOfObs, parameters,
		    independent);
	    gen.writeObservations("Observations");
	    observations = gen.getObservations();
	} else {
	    observations = readData("Observations");
	}

	long startTimeVoronoi = System.currentTimeMillis();

	// Create delaunay triangulation of observations
	Watson w = new Watson(observations);

	// Calculate area of voronoi cells
	w.calcVoronoi();

	long endTimeVoronoi = System.currentTimeMillis();

	Rectangle dimensions = w.getDimensions();

	frame.setVisible(true);
	// Draw results
	final Drawing results = new Drawing(w.getVertices(), w.getEdges());
	results.setDimensions(dimensions);
	frame.add(results);
	frame.addComponentListener(new ComponentAdapter() {
	    public void componentResized(ComponentEvent e) {
		results.setBounds(frame.getBounds());
		results.repaint();
	    }
	});

	long startTimeGraph = System.currentTimeMillis();

	// Create graph
	GraphTheory graph = new GraphTheory(w.getVertices());
	if (method == 0) {
	    graph.simple((int) (alpha * observations.length));
	} else if (method == 1) {
	    // Top-down approach
	    graph.topDown((int) (alpha * observations.length));
	} else if (method == 2) {
	    // Bottom-up approach
	    graph.bottomUp((int) (observations.length * (1.0 - alpha)));
	}

	long endTimeGraph = System.currentTimeMillis();

	// Redraw results, with hdr
	double theoreticalArea = 0.0;
	int nrInEllipse = 0;
	if (theoreticalDrawing) {
	    // Compare with theoretical region
	    // Calculate c2 of ellipse
	    double c2 = Math.sqrt(-2.0 * Math.log(alpha));
	    // Calculate centre and with and height
	    double width = parameters[0][1] * c2;
	    double height = parameters[1][1] * c2;
	    theoreticalArea = Math.PI * width * height;
	    Ellipse2D.Double ellipse = new Ellipse2D.Double(parameters[0][0]
		    - width, parameters[1][0] - height, width * 2.0,
		    height * 2.0);
	    for (Vertex v : w.getVertices()) {
		if (v.isInHDR() && ellipse.contains(v.x(), v.y())) {
		    nrInEllipse += v.getNrDuplicates() + 1;
		}
	    }
	    double[][] theoretical = new double[2][2];
	    theoretical[0][0] = parameters[0][0];
	    theoretical[1][0] = parameters[1][0];
	    theoretical[0][1] = width;
	    theoretical[1][1] = height;
	    results.drawTheoretical(theoretical);
	}

	results.drawHDR(true);
	frame.repaint();

	// Print results
	System.out.println("Results of " + (1 - alpha) + "% HDR of " + nrOfObs
		+ " observations");
	System.out.println("Voronoi time in milliseconds: "
		+ (endTimeVoronoi - startTimeVoronoi));
	System.out.println("Graph time in milliseconds: "
		+ (endTimeGraph - startTimeGraph));
	System.out.println("Initial area: " + w.getArea());
	System.out.println("HDR area: " + graph.getArea());
	if (theoreticalDrawing) {
	    System.out.println("Theoretical HDR area: " + theoreticalArea);
	    System.out
		    .println("Vertices in theoretical area and HDR: "
			    + (100 * nrInEllipse / ((1.0 - alpha) * observations.length))
			    + "%");
	}
    }

    /**
     * Read coordinates of observations from file. Each observation should be on
     * an own line, with two coordinates seperated with a tab
     * 
     * @param fileName
     *            the name of the .txt file (without .txt)
     * @return double array with observations as rows, with x-coord in first
     *         column and y-coord in second column
     */
    private static double[][] readData(String fileName) {
	ArrayList<double[]> file = new ArrayList<double[]>();
	BufferedReader rdr = null;
	try {
	    rdr = new BufferedReader(new FileReader(fileName + ".txt"));

	    String s = null;

	    do {
		s = rdr.readLine();
		if (s != null) {
		    String[] split = s.split("\t");
		    int length = split.length;
		    double[] tmp = new double[length];
		    for (int i = 0; i < length; i++) {
			tmp[i] = Double.parseDouble(split[i]);
		    }
		    file.add(tmp);
		}
	    } while (s != null);
	} catch (FileNotFoundException e) {
	    System.out.println("File not found");
	} catch (IOException e) {
	    System.out.println("Error reading file" + e.getMessage());
	} finally {
	    try {
		if (rdr != null) {
		    rdr.close();
		}
	    } catch (IOException e) {
	    }
	}
	double[][] returnval = new double[file.size()][file.get(0).length];
	for (int j = 0; j < file.size(); j++) {
	    returnval[j] = file.get(j);
	}
	return returnval;
    }
}
