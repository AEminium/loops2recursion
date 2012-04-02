
public class SimpleFor {
	public static void main(String[] args) {
		int M = 20;
		int N = 10;
		
		int[][] matrix = new int[M][N];
		
		int k=0;
		while(k < 10) {
			System.out.println(k++);
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
