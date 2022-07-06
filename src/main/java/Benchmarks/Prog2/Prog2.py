import time
start_time = time.time()
def contain(s , End):
    for x in range(len(s)):
        if(s[x] == End):
            return True
    return False

def findPath(Start , End):
    s = []
    s.append(Start)
    while(contain(s , End) == False):
        if(len(s) > 30000):
            return
        r = s.pop(0)
        s += checkRules(r)

def checkRules(s):
    a = []
    if s[-1] == 'I':
        a.append(s + 'U')
    a.append(s + s[1:])
    for i in range(len(s)- 2) :
        if s[i] == 'I' and s[i+1] == 'I' and s[i+2] == 'I':
            a.append(s[:i] + 'U' +s[i+3:])
    for i in range(0,len(s)-1):
        if s[i] == 'U' and s[i + 1] == 'U':
            a.append(s[:i] + s[i+2:])
    return a

findPath("MI", "MUIU")
findPath("MI" , "MIUIUIUIU")
findPath("MI" , "MUIIU")
findPath("MI" , "MIUIIIIUIUIIIIU")
findPath("MI" , "MUIU")
findPath("MI" , "MIUIUIUIU")
findPath("MI" , "MUIIU")
findPath("MI" , "MIIUUII")
findPath("MI" , "Notpos")
findPath("MI" , "Notpos")
print((time.time() - start_time) * 1000)