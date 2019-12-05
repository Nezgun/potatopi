def ip_search():
    f = open("inet.txt", "r")
    skip = 0
    while(1):
        line = f.readline()
        if("inet" in line and skip == 2):
            target = line.split()
            f.close()
            return target[1]
        elif("inet" in line):    
            skip += 1

f = open("inet_address.txt", "w+")
f.write(ip_search)
f.close()