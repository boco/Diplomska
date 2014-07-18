package si.uni_lj.fri.segmentation.utils;

import java.util.ArrayList;

public class Kockanje {

	public static ArrayList<Float> kocke(float[][][] CTMatrix, float isolevel) {
		ArrayList<Float> vertices = new ArrayList<Float>();
		Vertex[] cubesVertices = new Vertex[8];
		
		for (int i = 0; i < cubesVertices.length; i++)
			cubesVertices[i] = new Vertex(0, 0, 0);
		
		int indeksi[] =  { 
			0, 2, 1, 2, 3, 1, 
			1, 3, 4, 3, 5, 4, 
			4, 5, 6, 5, 7, 6, 
			6, 7, 0, 7, 2, 0, 
			6, 0, 4, 0, 1, 4,
			2, 7, 3, 7, 5, 3 
		};

		int pogoj;
		
		/*for (int z = 0; z < CTMatrix[0][0].length - 1; z++) {
			for (int y = 0; y < CTMatrix.length - 1; y++) {
				for (int x = 0; x < CTMatrix[0].length - 1; x++) {*/
		for (int z = 0; z < 60; z++) {
			for (int y = 0; y < 60; y++) {
				for (int x = 0; x < 60; x++) {
					int left = x;
					int right = x + 1;
					int top = y;
					int bottom = y + 1;
					int back = z + 1;
					int front = z;

					pogoj = 0;
					
					cubesVertices[7].setVertex(left, top, back, CTMatrix[top][left][back]); //0
					cubesVertices[5].setVertex(right, top, back, CTMatrix[top][right][back]); //1
					cubesVertices[4].setVertex(right, bottom, back, CTMatrix[bottom][right][back]); //2
					cubesVertices[6].setVertex(left, bottom, back, CTMatrix[bottom][left][back]); //3
					cubesVertices[2].setVertex(left, top, front, CTMatrix[top][left][front]); //4
					cubesVertices[3].setVertex(right, top, front, CTMatrix[top][right][front]); //5
					cubesVertices[1].setVertex(right, bottom, front, CTMatrix[bottom][right][front]); //6
					cubesVertices[0].setVertex(left, bottom, front, CTMatrix[bottom][left][front]); //7
					
					//System.out.println("isolevel: "+isolevel+", "+cubesVertices[0].value);
					
					for(int pog = 0; pog < cubesVertices.length; pog++){
						if(cubesVertices[pog].value < isolevel){
							pogoj++;
						}
					}
					
					if(pogoj == 0){
						for (int n = 0; n < 36 ; n +=3) {
							Vertex first = cubesVertices[indeksi[n]];
							//System.out.println(first.value);
							Vertex second = cubesVertices[indeksi[n + 1]];
							Vertex third = cubesVertices[indeksi[n + 2]];
							vertices.add(first.x);
							vertices.add(first.y);
							vertices.add(first.z);
							vertices.add(second.x);
							vertices.add(second.y);
							vertices.add(second.z);
							vertices.add(third.x);
							vertices.add(third.y);
							vertices.add(third.z);
						}
					}
				}
			}
		}
				
		//vertices = enaKocka(indeksi);
		
		return vertices;
	}
	
	public static ArrayList<Float> enaKocka(int[] indeksi) {
		ArrayList<Float> vertices = new ArrayList<Float>();
		Vertex[] cubesVertices = new Vertex[8];
		
		//indeksi
		cubesVertices[7].setVertex(0, 	0, 		0, 500); //7
		cubesVertices[5].setVertex(200, 0, 		0, 500); //5
		cubesVertices[4].setVertex(200, 200, 	0, 500); //4
		cubesVertices[6].setVertex(0, 	200, 	0, 500); //6
		cubesVertices[2].setVertex(0, 	0, 		200, 500); //2
		cubesVertices[3].setVertex(200, 0, 		200, 500); //3
		cubesVertices[1].setVertex(200, 200, 	200, 500); //1
		cubesVertices[0].setVertex(0, 	200, 	200, 500); //0
		
		for (int n = 0; n < 36 ; n +=3) {
			Vertex first = cubesVertices[indeksi[n]];
			//System.out.println(first.getVertex());
			Vertex second = cubesVertices[indeksi[n + 1]];
			Vertex third = cubesVertices[indeksi[n + 2]];
			vertices.add(first.x);
			vertices.add(first.y);
			vertices.add(first.z);
			vertices.add(second.x);
			vertices.add(second.y);
			vertices.add(second.z);
			vertices.add(third.x);
			vertices.add(third.y);
			vertices.add(third.z);
		}
		
		return vertices;
	}
	
	public static class Vertex {
		public float x, y, z, value;

		public Vertex(float x, float y, float z) {
			this.x = x;
			this.y = y;
			this.z = z;
		}

		public void setVertex(float x, float y, float z, float value) {
			this.x = x;
			this.y = y;
			this.z = z;
			this.value = value;
		}
		
		public String getVertex() {
			String deli = "X: "+x+", Y: "+y+", Z: "+z;
			return deli;
		}
	}
}
