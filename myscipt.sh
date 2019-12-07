sudo apt-get update -y
sudo apt-get dist-upgrade -y
ip -4 addr show eth0 | grep -oP "(?<=inet ).*(?=/)" > /home/pi/Documents/potatopi/inet.txt
python /home/pi/Documents/potatopi/initScript.py