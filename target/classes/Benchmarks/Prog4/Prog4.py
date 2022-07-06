import numpy as np
import time

def pow(x, pow):
    y = x
    for i in range(pow):
        x *= y
    return x

start_time = time.time()
size = 1700
a = np.empty([size, size])
b = np.empty([size, size])

for i in range(size):
    for j in range(size):
         k = j%4 ;
         a[i][j] = i * j - j / 10 + 100 * (-10 + pow(i , k) + pow(2 , k) )


for i in range(size):
    for j in range(size):
         k = j%4 ;
         b[i][j] = i * (j - pow(k , 2)) / (10 + 100 * (-10 + pow(100 , k)) )

total = np.sum(a)
total = np.sum(b)
arrPlus = np.add(a, b)
arrMin = np.subtract(a, b)
arrMul = np.multiply(a, b)
print((time.time() - start_time) * 1000)

