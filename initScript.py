
#function to find the inet address from an existing inet.txt file
#

def ip_search():
    f = open("inet.txt", "r") #open inet.txt in reading mode
    while(1):
        line = f.readline()
        if("inet" in line):
            target = line.split()
            f.close()
            return target[1]

#creates or updates file "inet_address.txt" and writes the inet to it.

f = open("inet_address.txt", "w+")
f.write(ip_search())
f.close()