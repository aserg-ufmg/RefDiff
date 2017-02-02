
package lsclipse;

public class LCS {
	
	public static String getLCS (String x, String y){
		int M = x.length();
        int N = y.length();

        // opt[i][j] = length of LCS of x[i..M] and y[j..N]
        int[][] opt = new int[M+1][N+1];

        // compute length of LCS and all subproblems via dynamic programming
        for (int i = M-1; i >= 0; i--) {
            for (int j = N-1; j >= 0; j--) {
                if (x.charAt(i) == y.charAt(j))
                    opt[i][j] = opt[i+1][j+1] + 1;
                else 
                    opt[i][j] = Math.max(opt[i+1][j], opt[i][j+1]);
            }
        }

        String output = "";
        int i = 0, j = 0;
        while(i < M && j < N) {
            if (x.charAt(i) == y.charAt(j)) {
                output = output + x.charAt(i);
                i++;
                j++;
            }
            else if (opt[i+1][j] >= opt[i][j+1]) i++;
            else                                 j++;
        }
        return output;

    }
	
	public static void allSubSequences(String x, String y){
		
	}
	public static void main(String[] args) {
        String y = "int output=super.getSpeed() + 200;   return output; } ";//"SHANGHAI";
        String x = "";//"SHAHAING";
        System.out.println("The output is: " + LCS.getLCS(x, y));
	}
}
