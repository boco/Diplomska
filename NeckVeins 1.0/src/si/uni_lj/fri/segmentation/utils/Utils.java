package si.uni_lj.fri.segmentation.utils;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import org.lwjgl.BufferUtils;
import org.lwjgl.opencl.CL10;
import org.lwjgl.opencl.CLCommandQueue;
import org.lwjgl.opencl.CLMem;

import si.uni_lj.fri.segmentation.MHDReader;

public class Utils {
	public static float[] getGauss1DKernel(double sigma) {
		int size = (int) (2 * Math.ceil(3 * sigma / MHDReader.dx) + 1);
		float[] kernel = new float[size];
		double sum = 0;
		for (int i = 0; i < size; i++) {
			double x = i - size / 2;
			kernel[i] = (float) ((1 / (Math.sqrt(2 * Math.PI) * sigma)) * Math.exp(-(x * x) / (2 * sigma * sigma)));
			sum += kernel[i];
		}
		for (int i = 0; i < kernel.length; i++) {
			kernel[i] /= sum;
		}

		return kernel;
	}

	public static float[] getGauss3DKernel(int size, double sigma) {
		float[] kernel = new float[size * size * size];
		double sum = 0;

		for (int z = 0; z < size; z++) {
			for (int y = 0; y < size; y++) {
				for (int x = 0; x < size; x++) {
					int index = x + y * size + z * size * size;
					double exp = Math.exp(-(x * x + y * y + z * z) / (2 * sigma * sigma));
					kernel[index] = (float) ((1 / Math.pow((Math.sqrt(2 * Math.PI) * sigma), 3)) * exp);
					sum += kernel[index];
				}
			}
		}
		for (int i = 0; i < kernel.length; i++) {
			kernel[i] /= sum;
		}
		return kernel;
	}

	public static void readBuffer(FloatBuffer buffer, int compareValue) {
		float max = 0;
		for (int i = 0; i < buffer.capacity(); i++) {
			if (buffer.get(i) > compareValue)
				System.out.println(buffer.get(i) + " ");
			if (buffer.get(i) > max)
				max = buffer.get(i);
		}
		System.out.println("max: " + max);
	}

	public static void readBuffer(IntBuffer buffer, int compareValue) {
		long sum = 0;
		for (int i = 0; i < buffer.capacity(); i++) {
			if (buffer.get(i) > compareValue)
				System.out.println("i: " + i + " value: " + buffer.get(i) + " ");
			sum += buffer.get(i);
		}
		System.out.println(buffer.capacity());
		System.out.println(sum);
	}

	public static void readCLBufferFloat(CLCommandQueue queue, CLMem memObj, int nfloats, int compareValue) {
		FloatBuffer buffer = BufferUtils.createFloatBuffer(nfloats);
		CL10.clEnqueueReadBuffer(queue, memObj, CL10.CL_TRUE, 0, buffer, null, null);
		readBuffer(buffer, compareValue);
	}

	public static void readCLBufferInt(CLCommandQueue queue, CLMem memObj, int nints, int compareValue) {
		IntBuffer buffer = BufferUtils.createIntBuffer(nints);
		CL10.clEnqueueReadBuffer(queue, memObj, CL10.CL_TRUE, 0, buffer, null, null);
		readBuffer(buffer, compareValue);
	}

	public static void histogram(CLCommandQueue queue, CLMem memObj, int size, float max) {
		FloatBuffer buffer = BufferUtils.createFloatBuffer(size);
		CL10.clEnqueueReadBuffer(queue, memObj, CL10.CL_TRUE, 0, buffer, null, null);
		int[] histogram = new int[256];

		for (int i = 0; i < buffer.capacity(); i++) {
			int index = (int) Math.round(buffer.get(i) / max * 255);
			histogram[index]++;
		}

		for (int i = 0; i < histogram.length; i++) {
			System.out.println("i: " + i + " value: " + histogram[i]);
		}
	}

	public static long startTime() {
		return System.currentTimeMillis();
	}

	public static void endTime(long startTime, String measureName) {
		long measuredTime = System.currentTimeMillis() - startTime;
		System.out.println(measureName + " time: " + measuredTime / 1000.0f + "s");
	}
}
