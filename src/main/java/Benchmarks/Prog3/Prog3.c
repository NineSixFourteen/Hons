#include <cstdio>
#include <time.h>
#include <stdlib.h>
#include <math.h>

void swap(int *xp, int *yp){
    int temp = *xp;
    *xp = *yp;
    *yp = temp;
}

void bubbleSort(int arr[], int n){
   int i, j;
   for (i = 0; i < n-1; i++)
       for (j = 0; j < n-i-1; j++)
           if (arr[j] > arr[j+1])
              swap(&arr[j], &arr[j+1]);
}

int main(){
    double time_spent = 0.0 ;
    clock_t begin = clock();
    srand(time(NULL));
    int arr[30000] ;
    for(int i = 0 ; i < 30000;i++){
        arr[i] = rand();
    }
    bubbleSort(arr , 30000) ;
    clock_t end = clock();
    time_spent += (double)(end - begin) / CLOCKS_PER_SEC;
    time_spent = time_spent * 1000 ;
    printf("%d", (int)round(time_spent));
    return 0;
}