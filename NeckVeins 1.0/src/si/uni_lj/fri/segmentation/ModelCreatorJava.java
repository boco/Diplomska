package si.uni_lj.fri.segmentation;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;

import si.uni_lj.fri.segmentation.utils.FileUtils;
import si.uni_lj.fri.segmentation.utils.Gauss3D;
import si.uni_lj.fri.segmentation.utils.Graytresh;
import si.uni_lj.fri.segmentation.utils.Kockanje;
import si.uni_lj.fri.segmentation.utils.MarchingCubes;

public class ModelCreatorJava {

	public static Object[] createModel(String fileName, double sigma, double threshold) {
		float[][][] ctMatrix = FileUtils.readFile3D(fileName);
		
		/*for (int i = 0; i < ctMatrix.length; i++) {
			for (int j = 0; j < ctMatrix[0].length; j++) {
				for (int k = 0; k < ctMatrix[0][0].length; k++) {
					System.out.print(ctMatrix[i][j][k]+" ");
				}
				System.out.println("test 2");
			}
			System.out.println("test 1");
		}*/
		
		//execGauss(ctMatrix, sigma);
		double isolevel = execFindTreshold(ctMatrix, threshold);
		execNormalization(ctMatrix, execFindMax(ctMatrix));
		System.out.println("Threshold je: "+threshold);
		System.out.println("Isolevel je: "+isolevel);
		//float[] vertices = execMarchingCubes(ctMatrix, isolevel);
		float[] vertices = execKocke(ctMatrix, isolevel);
		//System.out.println(vertices.length / 9);
		
		/*for (int i = 0; i < vertices.length; i++) {
			System.out.print(vertices[i]+" ");
		}
		System.out.println();*/
		
		//tukaj uredi treshold
		int[] nTriangles = new int[] { vertices.length / 9 };		
		return new Object[] { IntBuffer.wrap(nTriangles), FloatBuffer.wrap(vertices), 0, (float) isolevel };
	}

	private static void execNormalization(float[][][] ctMatrix, double max) {
		System.out.println("Values normalization...");
		for (int i = 0; i < ctMatrix.length; i++) {
			for (int j = 0; j < ctMatrix[0].length; j++) {
				for (int k = 0; k < ctMatrix[0][0].length; k++) {
					//System.out.println(ctMatrix[i][j][k]);
					ctMatrix[i][j][k] /= max;
				}
			}
		}
	}

	private static void execGauss(float[][][] ctMatrix, double sigma) {
		System.out.println("Gauss Java implementation...");
		if (sigma > 0)
			Gauss3D.gauss3D(ctMatrix, sigma);
	}

	private static double execFindMax(float[][][] ctMatrix) {
		double max = 0;
		for (int i = 0; i < ctMatrix.length; i++) {
			for (int j = 0; j < ctMatrix[0].length; j++) {
				for (int k = 0; k < ctMatrix[0][0].length; k++) {
					if (ctMatrix[i][j][k] > max)
						max = ctMatrix[i][j][k];
				}
			}
		}
		return max;
		//return 1700;
	}

	private static double execFindTreshold(float[][][] ctMatrix, double threshold) {
		System.out.println("Thresholding...");
		if (threshold < 0) {
			return Graytresh.graytresh(ctMatrix)[0];
		} else {
			return threshold;
		}
	}

	private static float[] execMarchingCubes(float[][][] ctMatrix, double isolevel) {
		System.out.println("Marching cubes...");
		ArrayList<Float> vertices = MarchingCubes.marchingCubes(ctMatrix, (float) isolevel);
		float[] v = new float[vertices.size()];
		for (int i = 0; i < vertices.size(); i++)
			v[i] = vertices.get(i);
		return v;
	}
	
	private static float[] execKocke(float[][][] ctMatrix, double isolevel) {
		System.out.println("Kockanje...");
		ArrayList<Float> vertices = Kockanje.kocke(ctMatrix, (float) isolevel);
		float[] v = new float[vertices.size()];
		for (int i = 0; i < vertices.size(); i++)
			v[i] = vertices.get(i);
		return v;
	}
}
