package si.uni_lj.fri.segmentation.utils;

import java.util.LinkedHashMap;

import si.uni_lj.fri.segmentation.utils.obj.Triangle;
import si.uni_lj.fri.segmentation.utils.obj.Vertex;

public class TrianglesLabelHelper {
	private Triangle[] triangles;
	private LinkedHashMap<Vertex, Vertex> vertTriMap;
	private boolean areLabeled;

	public TrianglesLabelHelper(int nTriangles) {
		triangles = new Triangle[nTriangles];
		vertTriMap = new LinkedHashMap<Vertex, Vertex>(nTriangles);
		areLabeled = false;
	}

	public Triangle[] getTriangles() {
		return triangles;
	}

	public LinkedHashMap<Vertex, Vertex> getVertTriMap() {
		return vertTriMap;
	}

	public void resetLabels() {
		if (areLabeled) {
			for (Triangle t : triangles)
				t.label = 0;
			for (Vertex v : vertTriMap.values())
				v.isLabeled = false;
			areLabeled = false;
		}
	}

	public void flagLabeled() {
		areLabeled = true;
	}

	public boolean areLabeled() {
		return areLabeled;
	}

}
