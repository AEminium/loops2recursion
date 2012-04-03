import java.util.Random;


public class ForFFT {
	public static Complex[] sequentialFFT(Complex[] x) {
        int N = x.length;

        // base case
        if (N == 1) return new Complex[] { x[0] };

        // radix 2 Cooley-Tukey FFT
        if (N % 2 != 0) { throw new RuntimeException("N is not a power of 2"); }

        // fft of even terms
        Complex[] even = new Complex[N/2];
        for (int k = 0; k < N/2; k++) {
            even[k] = x[2*k];
        }
        Complex[] q = sequentialFFT(even);

        // fft of odd terms
        Complex[] odd  = even;  // reuse the array
        for (int k = 0; k < N/2; k++) {
            odd[k] = x[2*k + 1];
        }
        Complex[] r = sequentialFFT(odd);

        // combine
        Complex[] y = new Complex[N];
        for (int k = 0; k < N/2; k++) {
            double kth = -2 * k * Math.PI / N;
            Complex wk = new Complex(Math.cos(kth), Math.sin(kth));
            y[k]       = q[k].plus(wk.times(r[k]));
            y[k + N/2] = q[k].minus(wk.times(r[k]));
        }
        return y;
    }
	
	public static Complex[] createRandomComplexArray(int n, long seed)
	{
		Random r = new Random(seed);
		Complex[] x = new Complex[n];

		int i = 0;
        while(i < n)
		{
            x[i] = new Complex(2*r.nextDouble() - 1, 0);
			++i;
		}

        return x;
	}

	public static void main(String [] args)
	{
		Complex[] input = createRandomComplexArray(1024, 524288);
		Complex[] output = sequentialFFT(input);

		int i = 0;
		while (i < (output).length)
		{
	        System.out.println(output[i]);
			++i;
		}
	}
}
