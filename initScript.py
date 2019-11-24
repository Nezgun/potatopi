def ip_search():
    f = open("inetinfo.txt", "r")
    skip = 0
    while(1):
        line = f.readline()
        if("inet" in line and skip == 2):
            target = line.split()
            return target[1]
        elif("inet" in line):    
            skip += 1

print(ip_search())