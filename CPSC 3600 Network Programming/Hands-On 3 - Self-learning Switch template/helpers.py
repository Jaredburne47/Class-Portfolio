

class Host:
    def __init__(self, name):
        self.name = name
        self.received_messages = {}
        self.sent_messages = {}

    def connect_client(self, other):
        pass

    def send_to(self, other, msg):
        if other.name not in self.sent_messages:
            self.sent_messages[other.name] = []
        self.sent_messages[other.name].append(f"Sending to {other.name}: {msg}")
        other.receive_from(source = self, msg = msg)

    ## Call this from other classes when this client should receive a message
    def receive_from(self, source, msg):
        if source.name not in self.received_messages:
            self.received_messages[source.name] = []
        self.received_messages[source.name].append(msg)

    def print_state(self):
        print(f"Host: {self.name}")
        print("Received Messages:")
        for source, messages in self.received_messages.items():
            for msg in messages:
                print(f"  {msg}")
        print("Sent Messages:")
        for dest, messages in self.sent_messages.items():
            for msg in messages:
                print(f"  {msg}")


class Client(Host):
    def __init__(self, name):
        super().__init__(name)
        self.adjacent_switch = None
    
    def connect_client(self, other):
        self.adjacent_switch = other

    def send(self, msg):
        self.send_to(self.adjacent_switch, msg)

    def receive_from(self, source, msg):
        super().receive_from(source, msg)

    def print_state(self):
        super().print_state()
        print(f"Adjacent Switch: {self.adjacent_switch.name}")