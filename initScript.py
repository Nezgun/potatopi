
#function to find the inet address from an existing inet.txt file
#

def ip_search():
    f = open("inet.txt", "r") #open inet.txt in reading mode
    skip = 0 #way of skipping over undesired inets
    while(1):
        line = f.readline()
        if("inet" in line and skip == 2): #skipping undesired ones, and then will take selected.
            target = line.split()
            f.close()
            return target[1]
        elif("inet" in line):    
            skip += 1

#creates or updates file "inet_address.txt" and writes the inet to it.

f = open("inet_address.txt", "w+")
f.write(ip_search)
f.close()