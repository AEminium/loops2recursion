
public class SimpleFor {
	public static void main(String[] args) {
		int M = 20;
		int N = 10;
		
		int[][] matrix = new int[M][N];
		
		int k=0;
		while(k < 10) {
			int a = 1;
			System.out.println(k++);
			matrix[0][0] = a;
		}
		
		
		for (int i=0;i<N;i++) {
			System.out.println("Hello " + i);
		}
		
		
		for(int i=0; i<M; i++) {
			for(int j=0; j<N; j++) {
				if (i == j) {
					matrix[i][j] = 1;
				} else {
					matrix[i][j] = 0;
				}
			}			
		}
		
		System.out.println("M:" + matrix[5][5]);
	}
}
