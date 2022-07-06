def fizzBuzz(number):
    if number % 15 == 0:
        return "fizzbuzz"
    elif number % 5 == 0:
        return "buzz"
    elif number % 3 == 0:
        return "fizz"
    return " " + str(number)

def makeMultiList(size):
    LayerOne = list()
    for i in range(0,size):
        LayerTwo = list()
        for j in range(0,size):
            LayerThree = list()
            for k in range(0,size):
                LayerFour = list()
                for l in range(0, size):
                    LayerFive = list()
                    for m in range(0,size):
                        x = [[] for i in range(10)]
                        LayerFive.append(x)
                    LayerFour.append(LayerFive)
                LayerThree.append(LayerFour)
            LayerTwo.append(LayerThree)
        LayerOne.append(LayerTwo)
    return LayerOne

import time
start_time = time.time()

s  = makeMultiList(10)
for i in range(0,1000000):
    j = i / 100000
    k = (i % 100000) / 10000
    l = ((i % 100000) % 10000) / 1000
    m = (((i % 100000) % 10000) % 1000) / 100
    n = (((i % 100000) % 10000) % 1000) % 100 / 10
    o = (((i % 100000) % 10000) % 1000) % 100  % 10
    s[int(j)][int(k)][int(l)][int(m)][int(n)][int(o)] = i

for i in range(0,1000000):
    j = i / 100000
    k = (i % 100000) / 10000
    l = ((i % 100000) % 10000) / 1000
    m = (((i % 100000) % 10000) % 1000) / 100
    n = (((i % 100000) % 10000) % 1000) % 100 / 10
    o = (((i % 100000) % 10000) % 1000) % 100  % 10
    fizzBuzz(s[int(j)][int(k)][int(l)][int(m)][int(n)][int(o)])

print((time.time() - start_time) * 1000)

