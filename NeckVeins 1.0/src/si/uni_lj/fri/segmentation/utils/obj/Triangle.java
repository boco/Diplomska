package si.uni_lj.fri.segmentation.utils.obj;

public class Triangle {
	public Vertex v1;
	public Vertex v2;
	public Vertex v3;
	public double[] normal;
	public int normalIndex;
	public int label;

	public Triangle(Vertex v1, Vertex v2, Vertex v3) {
		this.v1 = v1;
		this.v2 = v2;
		this.v3 = v3;
	}

	public String toString() {
		return "f " + v1.index + "//" + v1.normalIndex + " " + v2.index + "//" + v2.normalIndex + " " + v3.index + "//"
				+ v3.normalIndex + "\n";
	}

	public String vertices() {
		return v1.toString() + "\n" + v2.toString() + "\n" + v3.toString() + "\n";
	}

	public String normal() {
		return "vn " + normal[0] + " " + normal[1] + " " + normal[2] + "\n";
	}

	public boolean isLabeled() {
		return label != 0;
	}
}
