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

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.Random;

/**
 * Generate observations with x and y coordinates, uniformly distributed
 * 
 * @author Joeri
 * 
 */
public class SampleNormal {

	private double[][] observations;
	private int amountOfObs;
	private double[][] parameters;
	boolean independent;

	/**
	 * Initialize generator
	 * 
	 * @param n
	 *            number of observations needed
	 */
	public SampleNormal(int n, double[][] parameters, boolean independent) {
		amountOfObs = n;
		this.parameters = parameters;
		this.independent = independent;
		generate();
	}

	/**
	 * Generate the random coordinates
	 */
	private void generate() {
		// place to store info
		observations = new double[amountOfObs][2];
		// random generator
		Random generator = new Random();
		// generate n normal distributed random coordinates
		for (int i = 0; i < amountOfObs; i++) {
			// Normal distribution
			observations[i][0] = parameters[0][0] + parameters[0][1]
					* generator.nextGaussian();
			observations[i][1] = parameters[1][0] + parameters[1][1]
					* generator.nextGaussian();
			if (!independent) {
				observations[i][1] += Math.abs(observations[i][0]);
			}
		}
	}

	public void writeObservations(String name) {
		// write output to file
		try {
			// Create file
			FileWriter fstream = new FileWriter(name + ".txt");
			BufferedWriter out = new BufferedWriter(fstream);

			// 7 agents, 1 queue
			for (int obs = 0; obs < amountOfObs; obs++) {
				String line = "";
				for (int i = 0; i < 2; i++) {
					line += "" + observations[obs][i] + "\t";
				}
				line += "\r\n";
				out.write(line);
			}

			// Close the output stream
			out.close();
		} catch (Exception e) {// Catch exception if any
			System.err.println("Error: " + e.getMessage());
		}
	}

	/**
	 * Get the coordinates
	 * 
	 * @return the coordinates
	 */
	public double[][] getObservations() {
		return observations;
	}
}