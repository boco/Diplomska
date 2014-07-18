package si.uni_lj.fri.veins3D.gui.render.models;

import java.util.HashSet;
import java.util.LinkedHashSet;

public class PointNeighbours {
	HashSet<Integer> faces;
	HashSet<Integer> points;
	// These points are used for changing the original points' positions
	LinkedHashSet<Integer> pointsOriginal;

	public PointNeighbours() {
		faces = new HashSet<Integer>();
		points = new HashSet<Integer>();
		pointsOriginal = new LinkedHashSet<Integer>();
	}

}
