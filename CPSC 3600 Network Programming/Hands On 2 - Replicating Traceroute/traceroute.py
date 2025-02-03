import time
from scapy.all import *
from scapy.layers.inet import IP, ICMP


class TracerouteClone:

    def __init__(self):
        self.log = []

    def trace(self, hostname, num_samples = 3, max_hops = 30, timeout = 2):
        """
        Traces the route to the provided hostname

        Upon receiving a packet, routers decrease the Time To Live (TTL) field in the IP header
        by 1. If the TTL value ever reaches zero then the router will drop the packet. This is done
        to avoid routing loops where a packet gets stuck and never reaches its destination. Without
        a TTL value these packets would live forever and clog up the network. When dropping a packet
        due to a TTL reaching zero the router (unless configured not to) will send a TIME EXCEEDED
        ICMP message back to the sender in order to inform them that their packet was dropped. 

        Traceroute takes advantage of this functionality to identify the path packets take to reach
        a particular host. Traceroute sends a packet with a TTL of 1, which isdst immediately dropped 
        at the first router. Traceroute then listens for an ICMP TIME EXCEEDED message. Upon receiving 
        this message it learns 1) which router is first on the path to the host, and 2) the round trip 
        time (RTT) required to reach this router. Traceroute can then send a packet with a TTL of 2
        and learn about the second router. This continues until a packet reaches the destination.

        Of course, in order to learn that it has reached the destination Traceroute needs a response
        from the host when it receives a packet from Traceroute. Traceroute uses another ICMP message
        for this: the ECHO REPLY message. Each message sent by Traceroute is an ECHO REPLY message. 
        When the host receives this message it will reply with another one. Traceroute can then tell 
        if a particular message it recives comes from an intermediate router or the final host by
        checking the type for the ICMP message received. ECHO REPLY messages have a type code of 0 and
        TIME EXCEEDED messages have a type code of 11.

        We'll use the scapy library to craft our packets. Scapy gives you can enormous amount of control
        over packets that are sent, including specifying which headers should be used, what order
        they should be placed in, and the individual values of the header fields. You'll need to craft
        packets that contain an IPv4 header and an ICMP header. Scapy will take care of the Ethernet header 
        for us in this assignment, but you could also set that yourself.

        You can craft packets with scapy via the following syntax:
            packet = IP() / TCP() / Raw()
        This example creates a packet with an IPv4 header, followed by a TCP header, and finally followed 
        by some raw data (i.e. the payload of the packet). 
        
        It's possible to set individual parameters for each header by specifying values when creating the 
        packet, and scapy will choose intelligent default values for parameters you don't specify.
            packet = IP(src='10.0.0.7', dst='10.0.0.34') / TCP(sport=34, dport=8390, seq=483946983)

        Once you craft a packet you will need to send it using scapy, which provides several functions to 
        do so. We'll be using the sr1() function. This will send 1 packet and then receive 1 response. Other
        options include sr() which can send and receive multiple packets, and send() which (by default at 
        least) only sends packets without listening for a response. These functions operate at Layer 3 
        (i.e. they need a network layer header). Other options also exist for sending and receiving packets
        at Layer 2 (e.g. Ethernet), but we won't use them here. Note that we are NOT using the socket module
        for anything here.

        Since Scapy will need specific IP addresses instead of hostnames (e.g. clemson.edu) we need a way to 
        convert a hostname into an IP address. We will use the socket module for this. The function 
        socket.gethostbyname(hostname) will return the IP address associated with a specific hostname.
        
        You will also need to time how long it takes to get a response. You can use Python's time.time() function
        for this. It returns the current unixtime in seconds when called.

        Specific Instructions:
        1. Craft an ECHO REPLY message addressed to the host and set the TTL value to the current step (starts at 1)
        2. Send this and measure how much time passes until you receive a response (i.e. the RTT).
        3. Store the RTT for this message and the src IP address of the response using self.update_traceroute_log().
        4. Repeat steps 2-3 until you've collected the desired number of samples at this TTL value
        5. Increment the TTL value and repeat from step 1, or stop and return depending on if you've received an 
           ECHO REPLY message.  
        
        You will need to install the scapy package. You can do this using Python's pip utility. Open a terminal where 
        you can run python and enter the command 'pip install scapy'. This will install scapy into your default python 
        environment. If you recieve an error that scapy cannot be found when running your code after installing scapy 
        then VS Code is likely configured to use a different version of Python than your default system version.

        The following portions of scapy's documentation will be helpful
        * https://scapy.readthedocs.io/en/latest/installation.html
        * https://scapy.readthedocs.io/en/latest/usage.html
        * https://scapy.readthedocs.io/en/latest/api/scapy.layers.inet.html#scapy.layers.inet.IP
        * https://scapy.readthedocs.io/en/latest/api/scapy.layers.inet.html#scapy.layers.inet.ICMP

        Parameters:
            - hostname (string): The human-readable hostname (i.e. url) that you want to trace a path to.
            - num_samples (int): How many samples to collect from each step towards the destination.
            - max_hops (int): Places a maximum limit on how many steps to attempt before giving up
            - timeout (float): The maximum amount of time to wait for a response. Some machines are configured 
              not to send TIME EXCEEDED messages, so we need a timeout setting to prevent us from waiting forever
              when we reach one of those hosts. You can pass this timeout into the sr1() function. A call to 
              sr1() will return None if a timeout occurs.

        Returns:
            - list: Return a list containing the results of each sample and each TTL value. 

        Note:
            - Scapy also provides a traceroute function that will perform what we need for this assignment
              automatically. DO NOT USE THIS FUNCTION. You need to implement the functionality of traceroute
              yourself by creating and sending packets in the appropriate fashion directly.
        """
        # Clear the log from any previous results
        self.log = []

        destinationIp = socket.gethostbyname(hostname)

        for ttl in range (1, max_hops + 1):
            for _ in range(num_samples):
                
                #Craft the packet 
                packet = IP(dst=destinationIp, ttl=ttl) / ICMP()
                
                # Send the packet and wait for a response
                start_time = time.time()
                response = sr1(packet, verbose=0, timeout=timeout)
                end_time = time.time()

                if response is None:
                    self.update_traceroute_log(ttl_value = ttl)
                else:
                    # Calculate the round-trip time (RTT)
                    rtt = (end_time - start_time) * 1000  # Convert seconds to milliseconds
                    ip_address = response.src
                    self.update_traceroute_log(ttl_value = ttl,ip_address = ip_address,rtt=rtt)
                    
                    # Check if the response is an Echo Reply from the destination
                    if response.getlayer(ICMP).type == 0:  # ICMP Echo Reply type
                        return self.log
        return self.log


    def update_traceroute_log(self, ttl_value, ip_address = "Unknown", rtt = "*"):
        """
        Use the default values for ip_address and rtt if you don't get a response when you call 
        sr1() (i.e. there was a timeout). Do not modify this function.
        """
        if len(self.log) < ttl_value:
            self.log.append([])
        
        self.log[ttl_value-1].append({
            'ip': ip_address, 
            'rtt': rtt
            })


    def print_traceroute_log(self, log):
        """
        You can pass a log generated by your traceroute clone to this in order to print it nicely 
        in the terminal. Do not modify this function.
        """
        for ttl, ips in enumerate(log):
            current_ip = ""
            message = ""

            for sample in ips:
                if sample['ip'] != current_ip:
                    current_ip = sample['ip']
                    message += f"{str(ttl).rjust(2)}: {(current_ip+' ').ljust(20, '.')} "

                if sample['rtt'] != '*':
                    dur = f"{sample['rtt']:.3f}"
                    message += f"{dur.rjust(7)} ms  "
                else:
                    message += f"{'*'.rjust(7)} ms  "

            print(message)
        

if __name__ == "__main__":
    tr = TracerouteClone()
    log = tr.trace("www.google.com")
    tr.print_traceroute_log(log)