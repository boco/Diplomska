package si.uni_lj.fri.segmentation.utils;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.ShortBuffer;

import si.uni_lj.fri.segmentation.MHDReader;
import si.uni_lj.fri.veins3D.gui.VeinsWindow;

public class FileUtils {

	/**
	 * Reads OpenCL file
	 * 
	 * @param name
	 * @return
	 */
	public static String loadText(String name) {
		if (!name.endsWith(".cls")) {
			name += ".cls";
		}
		BufferedReader br = null;
		String resultString = null;
		try {
			br = new BufferedReader(new InputStreamReader(VeinsWindow.class.getResourceAsStream(name)));
			String line = null;
			StringBuilder result = new StringBuilder();
			while ((line = br.readLine()) != null) {
				result.append(line);
				result.append("\n");
			}
			resultString = result.toString();
		} catch (NullPointerException npe) {
			System.err.println("Error retrieving OpenCL source file: ");
			npe.printStackTrace();
		} catch (IOException ioe) {
			System.err.println("Error reading OpenCL source file: ");
			ioe.printStackTrace();
		} finally {
			try {
				br.close();
			} catch (IOException ex) {
				System.err.println("Error closing OpenCL source file");
				ex.printStackTrace();
			}
		}

		return resultString;
	}

	/**
	 * Currently is reading just shorts
	 * 
	 * TODO - read different number types written in MHD
	 * 
	 * @param fileName
	 * @return
	 */
	public static float[] readFile(String fileName) {
		MHDReader.readMHD(fileName);
		ShortBuffer shorts = fastFileRead();
		return bufferAs1DMatrix(shorts);
	}

	/**
	 * Currently is reading just shorts
	 * 
	 * TODO - read different number types written in MHD
	 * 
	 * @param fileName
	 * @return
	 */
	public static float[][][] readFile3D(String fileName) {
		MHDReader.readMHD(fileName);
		ShortBuffer shorts = fastFileRead();
		return bufferAs3DMatrix(shorts);
	}

	private static ShortBuffer fastFileRead() {
		System.out.println("Reading raw file " + MHDReader.rawFile + "...");
		File file = new File(MHDReader.rawFile);
		byte[] fileData = new byte[(int) file.length()];
		try {
			DataInputStream dis = new DataInputStream(new FileInputStream(file));
			dis.readFully(fileData);
			dis.close();
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(-1);
		}
		ByteBuffer buffer = ByteBuffer.wrap(fileData);
		buffer.order(ByteOrder.LITTLE_ENDIAN);

		ShortBuffer shorts = buffer.asShortBuffer();

		// Dereference variables
		file = null;
		fileData = null;
		buffer = null;

		return shorts;
	}

	private static float[] bufferAs1DMatrix(ShortBuffer buff) {
		float[] matrix = new float[buff.capacity()];
		for (int i = 0; i < buff.capacity(); i++) {
			matrix[i] = buff.get(i);
		}
		buff = null;
		return matrix;
	}

	private static float[][][] bufferAs3DMatrix(ShortBuffer buff) {
		float[][][] matrix = new float[MHDReader.Ny][MHDReader.Ny][MHDReader.Ny];
		for (int i = 0; i < MHDReader.Nz; i++) {
			for (int j = 0; j < MHDReader.Ny; j++) {
				for (int k = 0; k < MHDReader.Nx; k++) {
					matrix[j][k][i] = buff.get();
				}
			}
		}
		buff = null;
		return matrix;
	}
}
