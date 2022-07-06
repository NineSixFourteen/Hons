using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace Prog1{
    class Program{
        static void Main(string[] args) {
            var watch = System.Diagnostics.Stopwatch.StartNew();
            int[,,,,,] arr = new int[10,10,10,10,10,10] ;
            for(int i = 0 ; i < 1000000; i++){
                int j = i / 100000;
                int k = (i % 100000) / 10000 ;
                int l = ((i % 100000) % 10000) / 1000 ;
                int m = (((i % 100000) % 10000) % 1000) / 100 ;
                int n = (((i % 100000) % 10000) % 1000) % 100 / 10 ;
                int o = (((i % 100000) % 10000) % 1000) % 100  % 10 ;
                arr[j,k,l,m,n,o] = i ;
            }
            for(int i = 0 ; i < 1000000; i++){
                int j = i / 100000;
                int k = (i % 100000) / 10000 ;
                int l = ((i % 100000) % 10000) / 1000 ;
                int m = (((i % 100000) % 10000) % 1000) / 100 ;
                int n = (((i % 100000) % 10000) % 1000) % 100 / 10 ;
                int o = (((i % 100000) % 10000) % 1000) % 100  % 10 ;
                fizzBuzz(arr[j,k,l,m,n,o]);
            }
            watch.Stop();
            Console.WriteLine(watch.ElapsedMilliseconds);
        }

        static String fizzBuzz(int number) {
            if (number % 15 == 0) {
                return "fizzbuzz";
            } else if (number % 5 == 0) {
                return "buzz";
            } else if (number % 3 == 0) {
                return "fizz";
            }
            return "" + number;
        }
    }

}