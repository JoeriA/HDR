Computing the Highest Density Region using Voronoi and Delaunay techniques
===
Highest density regions have a great use as prediction regions. There
are some methods to compute a highest density region, but need informa-
tion about the probability density function or are only suited for certain
cases. This is a new method to approximate a highest density region using
Voronoi and Delaunay techniques. First we use these techniques to create a 
graph, after which we compute the highest density region using two different
graph algorithms: one that is faster and one that is more accurate.
Compared to the symmetric prediction region, these algorithms perform very well
and also for non-convex contour shaped densities both algorithms work as expected.
