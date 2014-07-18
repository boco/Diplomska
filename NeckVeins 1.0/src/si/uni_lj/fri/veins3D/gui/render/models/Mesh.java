package si.uni_lj.fri.veins3D.gui.render.models;

import static org.lwjgl.opengl.ARBBufferObject.GL_STATIC_DRAW_ARB;
import static org.lwjgl.opengl.ARBBufferObject.glBindBufferARB;
import static org.lwjgl.opengl.ARBBufferObject.glBufferDataARB;
import static org.lwjgl.opengl.ARBBufferObject.glDeleteBuffersARB;
import static org.lwjgl.opengl.ARBBufferObject.glGenBuffersARB;
import static org.lwjgl.opengl.ARBVertexBufferObject.GL_ARRAY_BUFFER_ARB;
import static org.lwjgl.opengl.ARBVertexBufferObject.GL_ELEMENT_ARRAY_BUFFER_ARB;
import static org.lwjgl.opengl.GL11.GL_FLOAT;
import static org.lwjgl.opengl.GL11.GL_NORMAL_ARRAY;
import static org.lwjgl.opengl.GL11.GL_TRIANGLES;
import static org.lwjgl.opengl.GL11.GL_UNSIGNED_INT;
import static org.lwjgl.opengl.GL11.GL_VERTEX_ARRAY;
import static org.lwjgl.opengl.GL11.glDrawElements;
import static org.lwjgl.opengl.GL11.glEnableClientState;
import static org.lwjgl.opengl.GL11.glNormalPointer;
import static org.lwjgl.opengl.GL11.glVertexPointer;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;

import si.uni_lj.fri.veins3D.math.Vector;

/**
 * The VBO class I made holds ID for indices to faces (and also their
 * count/amount) and ID to vertices and normals. It also remembers the
 * belongings to groups of the actual VBO it refers to. During the parsing of
 * the .obj file, each object of the VBO class briefly holds an array of
 * integers as faces (in my case: 3 integers = 1 face), it than writes and
 * actual VBO into the graphics card.
 * 
 * @author Simon �agar
 * @since 0.2
 * @version 0.2
 */
public class Mesh {
	ArrayList<Float> vertices;
	ArrayList<Float> normals;
	ArrayList<Integer> faces;
	ArrayList<String> groups;// groups this VBO belongs to

	int maxSubDepth;// maximal number of subdivisions able to render
	ArrayList<Integer> verticesAndNormalsIDs;
	ArrayList<Integer> verticesCounters;
	ArrayList<Integer> indicesIDs;
	ArrayList<Integer> facesCounters;

	public Mesh(ArrayList<String> groups, ArrayList<Integer> faces, ArrayList<Float> vertices) {
		maxSubDepth = -1;
		verticesAndNormalsIDs = new ArrayList<Integer>();
		verticesCounters = new ArrayList<Integer>();
		indicesIDs = new ArrayList<Integer>();
		facesCounters = new ArrayList<Integer>();
		this.groups = groups;
		this.faces = faces;
		this.vertices = vertices;
	}

	public void increaseMaxSubdivision() {
		// Disclaimer: This will change the location of vertices used in other
		// VBOs as well. (At the writing of the program, that's not an issue).
		ArrayList<Float> Fs = new ArrayList<Float>();
		ArrayList<PointNeighbours> allNeighbours = new ArrayList<PointNeighbours>();
		for (int i = 0; i < vertices.size() / 3; i++)
			allNeighbours.add(new PointNeighbours());

		for (int f = 0; f < faces.size() / 3; f++) {// f is quasi-index of face
													// with three points
			int a = faces.get(f * 3) - 1;// quasi-index of point a
			int b = faces.get(f * 3 + 1) - 1;// quasi-index of point b
			int c = faces.get(f * 3 + 2) - 1;// quasi-index of point c
			Fs.add((vertices.get(a * 3) + vertices.get(b * 3) + vertices.get(c * 3)) / 3);
			Fs.add((vertices.get(a * 3 + 1) + vertices.get(b * 3 + 1) + vertices.get(c * 3 + 1)) / 3);
			Fs.add((vertices.get(a * 3 + 2) + vertices.get(b * 3 + 2) + vertices.get(c * 3 + 2)) / 3);

			allNeighbours.get(a).faces.add(f);
			allNeighbours.get(b).faces.add(f);
			allNeighbours.get(c).faces.add(f);
			allNeighbours.get(a).points.add(b);
			allNeighbours.get(a).points.add(c);
			allNeighbours.get(b).points.add(a);
			allNeighbours.get(b).points.add(c);
			allNeighbours.get(c).points.add(b);
			allNeighbours.get(c).points.add(a);

			allNeighbours.get(a).pointsOriginal.add(b);
			allNeighbours.get(a).pointsOriginal.add(c);
			allNeighbours.get(b).pointsOriginal.add(c);
			allNeighbours.get(b).pointsOriginal.add(a);
			allNeighbours.get(c).pointsOriginal.add(a);
			allNeighbours.get(c).pointsOriginal.add(b);
		}

		// Create edge points
		ArrayList<FaceEdgePoints> facesEdges = new ArrayList<FaceEdgePoints>();
		for (int i = 0; i < faces.size() / 3; i++)
			facesEdges.add(new FaceEdgePoints());
		ArrayList<Float> Es = new ArrayList<Float>();
		int eIndex = 0;
		for (int point1 = 0; point1 < allNeighbours.size(); point1++) {
			PointNeighbours point1Neighbours = allNeighbours.get(point1);
			for (int point2 : point1Neighbours.points) {
				PointNeighbours point2Neighbours = allNeighbours.get(point2);
				int faceOne = -1;
				int faceTwo = -1;

				for (int face1 : point1Neighbours.faces) {
					for (int face2 : point2Neighbours.faces) {
						if (face1 == face2) {
							if (faceOne == -1)
								faceOne = face1;
							else if (faceTwo == -1)
								faceTwo = face1;
						}
					}
				}
				if (faceOne != -1 && faceTwo != -1) {
					float[] P1, P2, F1, F2;
					P1 = new float[] { vertices.get(point1 * 3), vertices.get(point1 * 3 + 1),
							vertices.get(point1 * 3 + 2) };
					P2 = new float[] { vertices.get(point2 * 3), vertices.get(point2 * 3 + 1),
							vertices.get(point2 * 3 + 2) };
					F1 = new float[] { Fs.get(faceOne * 3), Fs.get(faceOne * 3 + 1), Fs.get(faceOne * 3 + 2) };
					F2 = new float[] { Fs.get(faceTwo * 3), Fs.get(faceTwo * 3 + 1), Fs.get(faceTwo * 3 + 2) };
					Es.add((P1[0] + P2[0] + F1[0] + F2[0]) / 4);
					Es.add((P1[1] + P2[1] + F1[1] + F2[1]) / 4);
					Es.add((P1[2] + P2[2] + F1[2] + F2[2]) / 4);
					// point1Neighbours.points.remove(new Integer(point2));
					if (!point2Neighbours.points.remove(new Integer(point1))) {
						System.out.println("Removed a non-existing point");
					}
					;

					// find out when in a face the edge is
					// faceOne
					int a = faces.get(faceOne * 3) - 1;// quasi-index of point a
					int b = faces.get(faceOne * 3 + 1) - 1;// quasi-index of
															// point b
					if (a == point1 || a == point2) {
						if (b == point1 || b == point2)
							facesEdges.get(faceOne).e1 = eIndex;
						else
							facesEdges.get(faceOne).e3 = eIndex;
					} else
						facesEdges.get(faceOne).e2 = eIndex;
					// faceTwo
					a = faces.get(faceTwo * 3) - 1;// quasi-index of point a
					b = faces.get(faceTwo * 3 + 1) - 1;// quasi-index of point b
					if (a == point1 || a == point2) {
						if (b == point1 || b == point2)
							facesEdges.get(faceTwo).e1 = eIndex;
						else
							facesEdges.get(faceTwo).e3 = eIndex;
					} else
						facesEdges.get(faceTwo).e2 = eIndex;
					eIndex++;
				}
			}
		}

		ArrayList<Float> newOriginal = new ArrayList<Float>();
		// Now the extra points have been submitted
		// We need to recalculate new vertices values
		for (int point1 = 0; point1 < vertices.size() / 3; point1++) {
			PointNeighbours point1Neighbours = allNeighbours.get(point1);// still
																			// contains
																			// the
																			// faces
																			// we
																			// need
			int f = 0;
			float[] F = new float[3];
			for (int face1 : point1Neighbours.faces) {
				f++;
				F[0] += Fs.get(face1 * 3);
				F[1] += Fs.get(face1 * 3 + 1);
				F[2] += Fs.get(face1 * 3 + 2);
			}
			F[0] /= f;
			F[1] /= f;
			F[2] /= f;

			float[] R = new float[3];
			float[] P = new float[] { vertices.get(point1 * 3), vertices.get(point1 * 3 + 1),
					vertices.get(point1 * 3 + 2) };
			int e = 0;
			for (int point2 : point1Neighbours.pointsOriginal) {
				e++;
				R[0] += (vertices.get(point2 * 3) + P[0]) / 2;
				R[1] += (vertices.get(point2 * 3 + 1) + P[1]) / 2;
				R[2] += (vertices.get(point2 * 3 + 2) + P[2]) / 2;
			}
			R[0] /= e;
			R[1] /= e;
			R[2] /= e;
			if (e == f) {// This means there are as many edges as there are
							// faces. In other cases the formula can't be used
							// and points aren't moved.
				newOriginal.add((F[0] + 2 * R[0] + (f - 3) * P[0]) / f);
				newOriginal.add((F[1] + 2 * R[1] + (f - 3) * P[1]) / f);
				newOriginal.add((F[2] + 2 * R[2] + (f - 3) * P[2]) / f);
			} else {
				newOriginal.add(P[0]);
				newOriginal.add(P[1]);
				newOriginal.add(P[2]);
			}
		}

		vertices = new ArrayList<Float>();
		for (float f : newOriginal)
			vertices.add(f);
		for (float f : Fs)
			vertices.add(f);
		for (float f : Es)
			vertices.add(f);

		ArrayList<Integer> newFaces = new ArrayList<Integer>();
		// Now create new indexes for the new faces
		for (int f = 0; f < faces.size() / 3; f++) {
			FaceEdgePoints edgePoints = facesEdges.get(f);
			int e1 = newOriginal.size() / 3 + Fs.size() / 3 + edgePoints.e1;
			int e2 = newOriginal.size() / 3 + Fs.size() / 3 + edgePoints.e2;
			int e3 = newOriginal.size() / 3 + Fs.size() / 3 + edgePoints.e3;
			int a = faces.get(f * 3) - 1;// quasi-index of point a
			int b = faces.get(f * 3 + 1) - 1;// quasi-index of point b
			int c = faces.get(f * 3 + 2) - 1;// quasi-index of point c
			int fPoint = newOriginal.size() / 3 + f;
			if (edgePoints.e1 != -1) {
				newFaces.add(a);
				newFaces.add(e1);
				newFaces.add(fPoint);

				newFaces.add(b);
				newFaces.add(fPoint);
				newFaces.add(e1);
			} else {
				newFaces.add(a);
				newFaces.add(b);
				newFaces.add(fPoint);
			}
			if (edgePoints.e2 != -1) {
				newFaces.add(b);
				newFaces.add(e2);
				newFaces.add(fPoint);

				newFaces.add(c);
				newFaces.add(fPoint);
				newFaces.add(e2);

			} else {
				newFaces.add(b);
				newFaces.add(c);
				newFaces.add(fPoint);
			}
			if (edgePoints.e3 != -1) {
				newFaces.add(c);
				newFaces.add(e3);
				newFaces.add(fPoint);

				newFaces.add(a);
				newFaces.add(fPoint);
				newFaces.add(e3);

			} else {
				newFaces.add(c);
				newFaces.add(a);
				newFaces.add(fPoint);
			}
		}
		faces = new ArrayList<Integer>();
		for (int i : newFaces)
			faces.add(i + 1);
		constructVBO();
	}

	public void deleteVBO() {
		for (int i : verticesAndNormalsIDs) {
			glDeleteBuffersARB(i);
		}
		for (int i : indicesIDs) {
			glDeleteBuffersARB(i);
		}
		glBindBufferARB(GL_ARRAY_BUFFER_ARB, 0);
		glBindBufferARB(GL_ELEMENT_ARRAY_BUFFER_ARB, 0);
	}

	public void constructVBO() {
		maxSubDepth++;
		verticesAndNormalsIDs.add(glGenBuffersARB());
		verticesCounters.add(vertices.size());
		indicesIDs.add(glGenBuffersARB());
		facesCounters.add(faces.size());

		glBindBufferARB(GL_ARRAY_BUFFER_ARB, verticesAndNormalsIDs.get(maxSubDepth));
		glBindBufferARB(GL_ELEMENT_ARRAY_BUFFER_ARB, indicesIDs.get(maxSubDepth));
		float[] normals = getNormals(vertices, faces);
		FloatBuffer verticesAndNormalsBuffer = BufferUtils.createFloatBuffer(vertices.size() + normals.length);
		for (float f : vertices)
			verticesAndNormalsBuffer.put(f);
		for (float f : normals)
			verticesAndNormalsBuffer.put(f);
		verticesAndNormalsBuffer.rewind();
		IntBuffer facesBuffer = BufferUtils.createIntBuffer(faces.size());
		for (int i : faces)
			facesBuffer.put(i - 1);
		facesBuffer.rewind();

		glBufferDataARB(GL_ARRAY_BUFFER_ARB, verticesAndNormalsBuffer, GL_STATIC_DRAW_ARB);
		glBufferDataARB(GL_ELEMENT_ARRAY_BUFFER_ARB, facesBuffer, GL_STATIC_DRAW_ARB);
		System.out.println("Finished building a VBO. Faces: " + (faces.size() / 3) + " Vertices: "
				+ (vertices.size() / 3));
	}

	public void render(int subDepth) {
		if (maxSubDepth < subDepth)
			return;
		
		glBindBufferARB(GL_ARRAY_BUFFER_ARB, verticesAndNormalsIDs.get(subDepth));
		glBindBufferARB(GL_ELEMENT_ARRAY_BUFFER_ARB, indicesIDs.get(subDepth));

		glEnableClientState(GL_VERTEX_ARRAY);
		glVertexPointer(3, GL_FLOAT, 0, 0);
		glEnableClientState(GL_NORMAL_ARRAY);
		glNormalPointer(GL_FLOAT, 0, (4 * verticesCounters.get(subDepth)));

		glDrawElements(GL_TRIANGLES, facesCounters.get(subDepth), GL_UNSIGNED_INT, 0);

		glBindBufferARB(GL_ARRAY_BUFFER_ARB, 0);
		glBindBufferARB(GL_ELEMENT_ARRAY_BUFFER_ARB, 0);

	}

	public static float[] getNormals(ArrayList<Float> vertices, ArrayList<Integer> faces) {
		float[] normals = new float[vertices.size()];
		for (int f = 0; f < faces.size() / 3; f++) {
			int a = faces.get(f * 3) - 1;
			int b = faces.get(f * 3 + 1) - 1;
			int c = faces.get(f * 3 + 2) - 1;

			double[] A = new double[] { vertices.get(a * 3), vertices.get(1 + a * 3), vertices.get(2 + a * 3) };
			double[] B = new double[] { vertices.get(b * 3), vertices.get(1 + b * 3), vertices.get(2 + b * 3) };
			double[] C = new double[] { vertices.get(c * 3), vertices.get(1 + c * 3), vertices.get(2 + c * 3) };

			double[] AB = Vector.subtraction(B, A);
			double[] AC = Vector.subtraction(C, A);
			double[] n = Vector.crossProduct(AB, AC);
			n = Vector.normalize(n);

			normals[a * 3] += n[0];
			normals[a * 3 + 1] += n[1];
			normals[a * 3 + 2] += n[2];
			normals[b * 3] += n[0];
			normals[b * 3 + 1] += n[1];
			normals[b * 3 + 2] += n[2];
			normals[c * 3] += n[0];
			normals[c * 3 + 1] += n[1];
			normals[c * 3 + 2] += n[2];
		}
		for (int normal = 0; normal < normals.length / 3; normal++) {
			double[] n = new double[] { normals[normal * 3], normals[normal * 3 + 1], normals[normal * 3 + 2] };
			n = Vector.normalize(n);
			normals[normal * 3] = (float) n[0];
			normals[normal * 3 + 1] = (float) n[1];
			normals[normal * 3 + 2] = (float) n[2];
		}
		return normals;
	}

}
