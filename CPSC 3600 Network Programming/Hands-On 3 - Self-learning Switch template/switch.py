from helpers import Host, Client       

class Message:
    """
    Represents a message intended for transmission across a simulated network environment. This class encapsulates
    the essential components of a network message: its source, destination, and the data payload. It is specifically
    designed for client-to-client communication via switches; switches facilitate the forwarding process based on
    the destination address without altering the message itself.

    Attributes:
        src_name (str): The name of the client sending the message.
        dst_name (str): The name of the intended recipient client of the message.
        data (str): The actual content of the message being sent.

    Methods:
        __str__: Returns a human-readable string representation of the Message instance.
    """
    def __init__(self, src_name, dst_name, data):
        self.src_name = src_name
        self.dst_name = dst_name
        self.data = data

    def __str__(self):
        return f"Message from {self.src_name} to {self.dst_name}: {self.data}"


class Switch(Host):
    """
    Inherits from the Host class and acts as a network switch in a simulated network environment. This class
    is responsible for connecting clients and switches, receiving messages from clients, updating the forwarding
    table based on source addresses, and forwarding messages either directly to the destination (if known) or by
    broadcasting to all connected clients except the sender.

    Attributes:
        name (str): Inherited from the Host class, represents the name of the switch.
        adjacent_hosts (dict): A dictionary mapping the names of directly connected hosts (both switches and clients)
                               to their instances.
        forwarding_table (dict): A dictionary acting as the forwarding table for the switch, mapping destination
                                 client names to the client instance through which the switch last received a message
                                 from that client.

    Methods:
        connect_client: Establishes a connection between this switch and a client or another switch.
        receive_from: Handles incoming messages from connected clients or switches, updates the forwarding table,
                      and forwards the message appropriately.
        broadcast: Broadcasts a given message to all connected hosts except the sender.
        print_state: Prints the current state of the switch, including its forwarding table entries.
    """
    def __init__(self, name):
        """
        Initializes a new instance of the Switch class. You do not need to modify this method

        Args:
            name (str): The name of the switch.
        """
        super().__init__(name)
        # A dictionary of adjacent hosts, indexed by name. This is populated when the connect_client function is called by the tester.
        self.adjacent_hosts = []   
        # A dictionary of host names and the adjacent switch that leads to them. 
        # You will need to update this based on how self-learning switches update their forwarding table
        self.forwarding_table = {}

    def connect_client(self, other):
        """
        Connects another host (client or switch) to this switch. This method updates the adjacent_hosts dictionary
        to include the newly connected host. You do not need to modify this method.

        Args:
            other (Host): The client or switch instance to connect to this switch.
        """
        self.adjacent_hosts.append(other)

    def receive_from(self, source, msg):
        """
        Processes a message received from a connected host. Updates the forwarding table with the source of the
        message and forwards the message to the appropriate destination based on the forwarding table. If the
        destination is unknown, the message is broadcasted to all connected hosts except the previous sender.

        You can send a message to another host using the .send_to() method inherited from the Host class in helper.py.

        Note: DO NOT attempt to use the adjacent_hosts list here. You should only consult the forwarding table. 
              The adjacent_hosts list is necessary for broadcasting messages but should not otherwise be used.
        Args:
            source (str): The name of the host from which the message was received.
            msg (Message): The message instance received from the source.
        """
        super().receive_from(source, msg)

        self.forwarding_table[msg.src_name] = source

        if msg.dst_name in self.forwarding_table:
            # Destination known: Forward the message directly to the next hop.
            next_hop = self.forwarding_table[msg.dst_name]
            self.send_to(next_hop, msg)
        else:
            self.broadcast(msg, ignore_host=source)
        
        
       
    def broadcast(self, msg, ignore_host=None):
        """
        Broadcasts a message to all hosts connected to this switch, except for the host specified by ignore_host.
        You may use the adjacent_hosts list in this function.

        Args:
            msg (Message): The message to be broadcasted.
            ignore_host (Host, optional): A host to exclude from broadcasting. Defaults to None.
        """
        for host in self.adjacent_hosts:
            if host != ignore_host:
                self.send_to(host, msg)
        
    
    def print_state(self):
        """
        Prints the current state of the switch, including the names of connected hosts and the contents of the
        forwarding table. Do not modify this function
        """
        super().print_state()
        for dest, source in self.forwarding_table.items():
            print(f"Forwarding entry: {dest} -> {source.name}")


"""
Switch connections
            Switch3 --- Switch5
               |
Switch1 --- Switch2 --- Switch4 --- Switch7
               |           |
            Switch6     Switch8

Clients per switch
Switch1: Client1, Client2,
Switch2: Client3, Client4, Client5
Switch3: Client6, Client7
Switch4: Client8
Switch5: Client9, Client10
Switch6: Client11, Client12, Client13, Client14, Client15
Switch7: Client16, Client17, Client18
Switch8: Client19, Client20
"""

def connect(switch1, switch2):
    switch1.connect_client(switch2)
    switch2.connect_client(switch1)

if __name__ == "__main__":
    switch1 = Switch("Switch 1")
    switch2 = Switch("Switch 2")
    switch3 = Switch("Switch 3")
    switch4 = Switch("Switch 4")
    switch5 = Switch("Switch 5")
    switch6 = Switch("Switch 6")
    switch7 = Switch("Switch 7")
    switch8 = Switch("Switch 8")

    client1 = Client("Client 1")
    client2 = Client("Client 2")
    client3 = Client("Client 3")
    client4 = Client("Client 4")
    client5 = Client("Client 5")
    client6 = Client("Client 6")
    client7 = Client("Client 7")
    client8 = Client("Client 8")
    client9 = Client("Client 9")
    client10 = Client("Client 10")
    client11 = Client("Client 11")
    client12 = Client("Client 12")
    client13 = Client("Client 13")
    client14 = Client("Client 14")
    client15 = Client("Client 15")
    client16 = Client("Client 16")
    client17 = Client("Client 17")
    client18 = Client("Client 18")
    client19 = Client("Client 19")
    client20 = Client("Client 20")

    connect(switch1, switch2)    
    connect(switch2, switch3)
    connect(switch2, switch4)
    connect(switch2, switch6)
    connect(switch3, switch5)    
    connect(switch4, switch7)
    connect(switch4, switch8)

    connect(switch1, client1)
    connect(switch1, client2)    
    connect(switch2, client3)
    connect(switch2, client4)
    connect(switch2, client5)
    connect(switch3, client6)
    connect(switch3, client7)
    connect(switch4, client8)
    connect(switch5, client9)
    connect(switch5, client10)
    connect(switch6, client11)
    connect(switch6, client12)
    connect(switch6, client13)
    connect(switch6, client14)
    connect(switch6, client15)
    connect(switch7, client16)
    connect(switch7, client17)
    connect(switch7, client18)
    connect(switch8, client19)
    connect(switch8, client20)

    client1.send(Message("Client 1", "Client 2", "Hello, Client 2!"))
    client2.send(Message("Client 2", "Client 1", "Hello, Client 1!"))
    client1.send(Message("Client 1", "Client 2", "How are you Client 2!"))
    client2.send(Message("Client 2", "Client 1", "How are you Client 1!"))
    client19.send(Message("Client 19", "Client 1", "I am client 19."))

    switch1.print_state()
    switch2.print_state()
    switch3.print_state()
    switch4.print_state()
    switch5.print_state()
    switch6.print_state()
    switch7.print_state()
    switch8.print_state()