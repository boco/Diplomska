package si.uni_lj.fri.segmentation.utils.obj;

import java.util.ArrayList;

public class Vertex {
	public float x;
	public float y;
	public float z;
	public int index;
	public int normalIndex;
	public double value;
	public boolean isLabeled = false;
	public ArrayList<Integer> triangles;

	public Vertex(float x, float y, float z, int index) {
		this.x = x;
		this.y = y;
		this.z = z;
		this.index = index;
		this.normalIndex = 0;
	}

	public void setVertex(float x, float y, float z, float value) {
		this.x = x;
		this.y = y;
		this.z = z;
		this.value = value;
	}

	public String toString() {
		return "v " + x + " " + y + " " + z;
	}

	public boolean isLabeled() {
		return isLabeled;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null)
			return false;
		if (this.getClass() != obj.getClass())
			return false;

		Vertex other = (Vertex) obj;
		if (this.x != other.x || this.y != other.y || this.z != other.z)
			return false;

		return true;
	}

	@Override
	public int hashCode() {
		int hashCode = (new Float(x).hashCode() >> 17) ^ (new Float(y).hashCode() ^ new Float(z).hashCode());
		return hashCode;
	}
}
