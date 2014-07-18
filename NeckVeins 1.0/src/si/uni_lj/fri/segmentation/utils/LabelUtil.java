package si.uni_lj.fri.segmentation.utils;

import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedList;

import si.uni_lj.fri.segmentation.utils.obj.Triangle;
import si.uni_lj.fri.segmentation.utils.obj.Vertex;

public class LabelUtil {

	public static boolean[] getValidLabels(int nTriangles, int minTriangles, TrianglesLabelHelper helper) {
		if (!helper.areLabeled())
			labelTriangels(nTriangles, helper);
		int nLabels = findNumLabels(helper);
		int[] hist = createLabelHistogram(nLabels, helper);
		return findValidLabels(hist, minTriangles);
	}

	private static int findNumLabels(TrianglesLabelHelper helper) {
		Triangle[] tris = helper.getTriangles();
		int nLabels = 0;
		for (Triangle t : tris) {
			if (t.label > nLabels)
				nLabels = t.label;
		}
		return nLabels + 1;
	}

	private static int[] createLabelHistogram(int nLabels, TrianglesLabelHelper helper) {
		Triangle[] tris = helper.getTriangles();
		int[] hist = new int[nLabels];
		for (Triangle t : tris) {
			hist[t.label]++;
		}
		return hist;
	}

	private static boolean[] findValidLabels(int[] hist, int minTriangles) {
		boolean[] validLabels = new boolean[hist.length];
		for (int i = 0; i < hist.length; i++) {
			if (hist[i] >= minTriangles) {
				validLabels[i] = true;
			}
		}
		return validLabels;
	}

	private static void labelTriangels(int nTriangles, TrianglesLabelHelper helper) {
		helper.flagLabeled();
		Triangle[] tris = helper.getTriangles();
		LinkedList<Integer> trianglesFIFO = new LinkedList<Integer>();
		int label = 1;
		for (int i = 0; i < nTriangles; i++) {
			Triangle t = tris[i];
			if (!t.isLabeled()) {
				t.label = label;
				updateFIFO(t, trianglesFIFO);
				while (!trianglesFIFO.isEmpty()) {
					t = tris[trianglesFIFO.pop()];
					if (!t.isLabeled()) {
						t.label = label;
						updateFIFO(t, trianglesFIFO);
					}
				}
				label++;
			}
		}
	}

	private static void updateFIFO(Triangle t, LinkedList<Integer> trianglesFIFO) {
		if (!t.v1.isLabeled) {
			t.v1.isLabeled = true;
			trianglesFIFO.addAll(t.v1.triangles);
		}
		if (!t.v2.isLabeled) {
			t.v2.isLabeled = true;
			trianglesFIFO.addAll(t.v2.triangles);
		}
		if (!t.v3.isLabeled) {
			t.v3.isLabeled = true;
			trianglesFIFO.addAll(t.v3.triangles);
		}
	}

	public static void createVertexList(int nTriangles, FloatBuffer trianglesBuff, TrianglesLabelHelper helper) {
		helper.resetLabels();
		Triangle[] tris = helper.getTriangles();
		LinkedHashMap<Vertex, Vertex> map = helper.getVertTriMap();
		Vertex[] verts = new Vertex[3];
		int vertIndex = 1;
		for (int i = 0; i < nTriangles; i++) {
			for (int j = 0; j < 3; j++) {
				float x = trianglesBuff.get(i * 9 + j * 3);
				float y = trianglesBuff.get(i * 9 + j * 3 + 1);
				float z = trianglesBuff.get(i * 9 + j * 3 + 2);
				Vertex v = new Vertex(x, y, z, vertIndex);
				Vertex vert = map.get(v);
				if (vert != null) {
					vert.triangles.add(i);
					verts[j] = vert;
				} else {
					v.triangles = new ArrayList<Integer>();
					v.triangles.add(i);
					v.normalIndex = 1;
					map.put(v, v);
					vertIndex++;
					verts[j] = v;
				}
			}
			tris[i] = new Triangle(verts[0], verts[1], verts[2]);
			tris[i].normalIndex = 1;
		}
	}
}
