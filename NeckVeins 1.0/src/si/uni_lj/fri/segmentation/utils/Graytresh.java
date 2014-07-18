package si.uni_lj.fri.segmentation.utils;

import java.nio.IntBuffer;

public class Graytresh {

	/**
	 * This code modifies Otsu's algorithm implementation from
	 * http://www.labbookpages.co.uk/software/imgProc/otsuThreshold.html
	 * 
	 * @param m
	 * @return
	 */
	public static double[] graytresh(float[][][] m) {
		double maxValue = 0;
		int[] histogram = new int[256];
		int zeros = 0;
		for (int i = 0; i < m.length; i++) {
			for (int j = 0; j < m[0].length; j++) {
				for (int k = 0; k < m[0][0].length; k++) {
					if ((0xFF & (int) m[i][j][k]) == 0)
						zeros++;
					maxValue = (m[i][j][k] > maxValue) ? m[i][j][k] : maxValue;
				}
			}
		}

		int allValues = m.length * m[0].length * m[0][0].length;
		histogram = new int[256];
		histogram[0] = zeros;
		histogram[histogram.length - 1] = allValues - zeros;

		float sum = 0;
		for (int t = 0; t < histogram.length; t++)
			sum += t * histogram[t];

		float sumB = 0;
		int wB = 0;
		int wF = 0;

		float varMax = 0;
		int threshold = 0;

		for (int t = 0; t < 256; t++) {
			/* Weight Background */
			wB += histogram[t];
			if (wB == 0)
				continue;

			/* Weight Foreground */
			wF = allValues - wB;
			if (wF == 0)
				break;

			sumB += (float) (t * histogram[t]);

			/* Mean background and foreground */
			float mB = sumB / wB;
			float mF = (sum - sumB) / wF;

			/* Calculate Between Class Variance */
			float varBetween = (float) wB * (float) wF * (mB - mF) * (mB - mF);

			/* Check if new maximum found */
			if (varBetween > varMax) {
				varMax = varBetween;
				threshold = 0;
			}

			if (varBetween == varMax) {
				threshold += (t + 1);
			}
		}

		double thresh = threshold / (256.0 * 256.0);
		return new double[] { thresh, maxValue };

	}

	/**
	 * Returns threshold of 3D array and also max value. This code is based on
	 * Matlab graythresh imeplementation
	 * 
	 * @param m
	 * @return
	 */
	public static double[] graytresh(double[][][] m) {
		int zeros = 0;
		double maxValue = 0;
		for (int i = 0; i < m.length; i++) {
			for (int j = 0; j < m[0].length; j++) {
				for (int k = 0; k < m[0][0].length; k++) {
					if ((0xFF & (int) m[i][j][k]) == 0)
						zeros++;
					maxValue = (m[i][j][k] > maxValue) ? m[i][j][k] : maxValue;
				}
			}
		}

		int allValues = m.length * m[0].length * m[0][0].length;
		double[] p = new double[256];
		p[0] = zeros / (double) allValues;
		p[p.length - 1] = (allValues - zeros) / (double) allValues;

		double[] omega = new double[256];
		double[] mu = new double[256];
		for (int i = 0; i < omega.length; i++) {
			omega[i] = p[i] + ((i < 1) ? 0 : omega[i - 1]);
			mu[i] = p[i] * (i + 1) + ((i < 1) ? 0 : mu[i - 1]);
		}

		double mu_t = mu[mu.length - 1];

		double[] sigma_b_squared = new double[256];
		double max = 0;
		double threshold = 0;
		for (int i = 0; i < sigma_b_squared.length; i++) {
			sigma_b_squared[i] = ((mu_t * omega[i] - mu[i]) * (mu_t * omega[i] - mu[i])) / (omega[i] * (1 - omega[i]));
			if (sigma_b_squared[i] > max) {
				threshold = 0;
				max = sigma_b_squared[i];
			}
			if (sigma_b_squared[i] == max)
				threshold += (i + 1);
		}

		threshold /= (256 * 256);

		return new double[] { threshold, maxValue };

	}

	public static void binarize(double[][][] m) {
		double[] threshold = graytresh(m);
		double thresh = threshold[0] * threshold[1];
		for (int i = 0; i < m.length; i++) {
			for (int j = 0; j < m[0].length; j++) {
				for (int k = 0; k < m[0][0].length; k++) {
					m[i][j][k] = (m[i][j][k] >= thresh) ? 1 : 0;
				}
			}
		}
	}

	public static double[] graytresh1D(float[] m) {
		int zeros = 0;
		double maxValue = 0;
		for (int i = 0; i < m.length; i++) {
			if ((int) m[i] == 0)
				zeros++;
			maxValue = (m[i] > maxValue) ? m[i] : maxValue;
		}

		int allValues = m.length;
		double[] p = new double[256];
		p[0] = zeros / (double) allValues;
		p[p.length - 1] = (allValues - zeros) / (double) allValues;

		double[] omega = new double[256];
		double[] mu = new double[256];
		for (int i = 0; i < omega.length; i++) {
			omega[i] = p[i] + ((i < 1) ? 0 : omega[i - 1]);
			mu[i] = p[i] * (i + 1) + ((i < 1) ? 0 : mu[i - 1]);
		}

		double mu_t = mu[mu.length - 1];

		double[] sigma_b_squared = new double[256];
		double max = 0;
		double threshold = 0;
		for (int i = 0; i < sigma_b_squared.length; i++) {
			sigma_b_squared[i] = ((mu_t * omega[i] - mu[i]) * (mu_t * omega[i] - mu[i])) / (omega[i] * (1 - omega[i]));
			if (sigma_b_squared[i] > max) {
				threshold = 0;
				max = sigma_b_squared[i];
			}
			if (sigma_b_squared[i] == max)
				threshold += (i + 1);
		}

		threshold /= (256 * 256);

		return new double[] { threshold, maxValue };

	}

	public static void binarize1D(float[] m) {
		double[] threshold = graytresh1D(m);
		double thresh = threshold[0] * threshold[1];
		for (int i = 0; i < m.length; i++) {
			m[i] = (m[i] >= thresh) ? 1 : 0;
		}
	}

	public static double thresholdFromHistogram(IntBuffer buffer, int nValues) {
		double[] frequences = new double[256];
		double[] w = new double[256];
		double[] m = new double[256];
		for (int i = 0; i < frequences.length; i++) {
			frequences[i] = (float) buffer.get(i) / (float) nValues;
		}
		w[0] = frequences[0];
		for (int i = 1; i < m.length; i++) {
			w[i] = w[i - 1] + frequences[i];
			m[i] = m[i - 1] + i * frequences[i];
		}

		double mean = m[255];
		double tmpMax = 0;
		double tmpMax2 = 0;
		double threshold = 0;
		double threshold2 = 0;
		for (int i = 0; i < 256; i++) {
			double bcv = mean * w[i] - m[i];
			bcv *= bcv / (w[i] * (1 - w[i]));
			if (tmpMax < bcv) {
				tmpMax = bcv;
				threshold = i;
			}
			if (tmpMax > bcv && tmpMax2 < bcv) {
				threshold2 = i;
			}
		}
		threshold = (threshold + threshold2) / 2;

		return threshold / 256.0;
	}
}
