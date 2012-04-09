
public class IntegratedFor {
	public static void main(String[] args) {
		int i=0;
		
		int[] a = new int[2];
		a[1] = 1;
		a[(i=i+1)] = 2;
		
		for(i=0; i<10; i++) {
			i = 20;
			System.out.println("i: " + i);
		}
		
		System.out.println("final i:" + i);
	}
}
