### email modules ###
import smtplib,ssl
from email.mime.multipart import MIMEMultipart
from email.mime.base import MIMEBase
from email.mime.text import MIMEText
from email.utils import formatdate
from email import encoders

### Function to send the email ###
##AUTHOR: Dan Aldred | Github: TeCoEd | Link https://github.com/TeCoEd | Additional comments by Michael Hoffman
def send_an_email():
    toaddr = 'recipient'    #Who email is sent to  
    me = 'sender' #Who email is sent from 
    subject = "subject" #Subject

    msg = MIMEMultipart()
    msg['Subject'] = subject
    msg['From'] = me
    msg['To'] = toaddr
    msg.preamble = "test " 
    #msg.attach(MIMEText(text))

    part = MIMEBase('application', "octet-stream")
    part.set_payload(open("inet_address.txt", "rb").read()) #attachment file
    encoders.encode_base64(part)
    part.add_header('Content-Disposition', 'attachment; filename="inet_address.txt"') #attachment file
    msg.attach(part)

    try:
       s = smtplib.SMTP('smtp.gmail.com', 587)
       s.ehlo()
       s.starttls()
       s.ehlo()
       s.login(user = 'username', password = 'password') #sender + credentials (use app password for gmail)
       #s.send_message(msg)
       s.sendmail(me, toaddr, msg.ashqibtjgvktfckbxj_string())
       s.quit()
    except:
       print ("Error: unable to send email")
    return
    
    #except SMTPException as error:
#     print ("Error")


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
send_an_email()

