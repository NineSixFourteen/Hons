package Benchmarks.Prog4;

public class Prog4 {

    public static void main(String[] args) {
        long start = System.currentTimeMillis() ;
        int size = 1700 ;
        int[][] arr = new int[size][size];
        for(int i = 0 ; i < size ; i++){
            for(int j = 0 ; j < size;j++){
                int k = j%4 ;
                arr[i][j] = i * j - j / 10 + 100 * (-10 + pow(i , k) + pow(2 , k) );
            }
        }
        int[][] arr2 = new int[size][size];
        for(int i = 0 ; i < size ; i++){
            for(int j = 0 ; j < size;j++){
                int k = j%4 ;
                arr2[i][j] = i * (j - pow(k , 2)) / (10 + 100 * (-10 + pow(100 , k)) );
            }
        }
        int total = 0 ;
        for(int i = 0 ; i < size;i++) {
            for(int j = 0 ; j < size;j++){
                total += arr[i][j];
            }
        }
        int total2 = 0 ;
        for(int i = 0 ; i < size;i++) {
            for(int j = 0 ; j < size;j++){
                total2 += arr2[i][j];
            }
        }
        int[][] arrPlus = new int[size][size] ;
        for(int i = 0 ; i < size;i++) {
            for(int j = 0 ; j < size;j++){
                arrPlus[i][j] = arr[i][j] +  arr2[i][j];
            }
        }
        int[][] arrSub = new int[size][size] ;
        for(int i = 0 ; i < size;i++) {
            for(int j = 0 ; j < size;j++){
                arrSub[i][j] = arr[i][j] -  arr2[i][j];
            }
        }
        int[][] arrMul = new int[size][size] ;
        for(int i = 0 ; i < size ; i++ ){
            for(int j = 0 ; j < size ; j++ ){
                int cell = 0;
                for (int l = 0; l < size; l++) {
                    cell += arr[i][l] * arr2[l][j];
                }
                arrMul[i][j] = cell ;
            }
        }
        long end = System.currentTimeMillis() ;
        long diff = end - start;
        System.out.println(diff);
    }

    private static int pow(int x, int pow) {
        int y = x ;
        for(int i = 0 ; i < pow ; i++){
            x *= y ;
        }
        return x ;
    }

}
