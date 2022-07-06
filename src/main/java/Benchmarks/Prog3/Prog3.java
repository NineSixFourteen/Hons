package Benchmarks.Prog3;

import java.util.Random;

public class Prog3 {


    public static void main(String[] args) {
        long start = System.currentTimeMillis();
        Random r = new Random();
        int[] arr = new int[30000];
        for(int i = 0 ; i < 30000;i++){
            arr[i] = r.nextInt() ;
        }
        arr = bubbleSort(arr);
        long end = System.currentTimeMillis();
        long diff = end - start ;
        System.out.println(diff);
    }

    static int[] bubbleSort(int arr[]) {
        int n = arr.length;
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n - i - 1; j++) {
                if (arr[j] > arr[j + 1]) {
                    int temp = arr[j];
                    arr[j] = arr[j + 1];
                    arr[j + 1] = temp;
                }
            }
        }
        return arr ;
    }

}
