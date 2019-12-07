
#function to find the inet address from an existing inet.txt file
#
def n():
    with open("inet.txt", "r") as file:
        while(True): #screw you loops
            ln = file.readline() #keep reading
            if("inet" in ln and not "inet6" in ln): #make sure we aren't hitting an inet6
                l = ln.split() #if good, put into 
                if (not "127" in l[1]): #if not loopback
                    return l[1] #then output
            

#creates or updates file "inet_address.txt" and writes the inet to it.

f = open("inet_address.txt", "w+")
f.write(n())
f.close()