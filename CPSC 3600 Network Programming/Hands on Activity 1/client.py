import socket
import sys

class TCPClient:
    def __init__(self,host='127.0.0.1', port=65432):
        """
        Initializes the TCPClient instance with the specified server host and port.

        Parameters:
        -----------
        host : str, optional
            The IP address of the server to connect to. Defaults to '127.0.0.1' (localhost).
        port : int, optional
            The port number of the server to connect to. Defaults to 65432.

        Attributes:
        -----------
        is_running : bool
            A flag indicating whether the client is currently running. Defaults to False.
        client_socket : socket.socket or None
            The client socket used for communication with the server. Defaults to None.
        """
        self.host = host
        self.port = port
        self.is_running = False
        self.client_socket = None
    def start(self):
        """
        Starts the client by creating a socket and connecting it to the server.
        If the connection fails, it prints an error message and sets `is_running` to False.

        Raises:
        -------
        Exception
            If there is an error during socket creation or connection.
        
        Side-effects:
        -------------
        - Initializes the client socket and connects it to the server.
        - Sets `is_running` to True if the connection is successful, False otherwise.
        """
        try:
            self.client_socket = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
            self.client_socket.connect((self.host,self.port))
            self.is_running = True
        except Exception as e:
            print(f"Failed to connect to the server: {e}")
            self.is_running = False
    def run(self):
        """
        Runs the main client loop to interact with the server.
        This method continuously sends user input to the server and receives responses
        until the user types 'quit' or the connection is interrupted.

        Side-effects:
        -------------
        - Receives and displays the server's welcome message.
        - Prompts the user for input, sends it to the server, and displays the server's response.
        - Handles any errors during communication or connection loss.
        
        Raises:
        -------
        ConnectionResetError
            If the connection to the server is lost unexpectedly.
        Exception
            For any other errors that occur during communication.
        """
        try:
            # Receive and display the welcome message from the server
            welcome_message = self.client_socket.recv(1024).decode()
            print(welcome_message)

            # loop to interact with the server
            while self.is_running:
                # Prompt the user for input
                user_input = input("Enter operation (ADD/SUB/DIV/MUL) and operands or 'quit' to exit: ").strip()
                
                # Send user input to the server
                self.client_socket.sendall(user_input.encode())

                if user_input.lower() == 'quit':  # Exit condition
                    break

                # Receive and display the server's response
                response = self.client_socket.recv(1024).decode()
                print(f"Server response: {response}")
        
        except ConnectionResetError:
            print("Connection to the server was lost.")
        except Exception as e:
            print(f"Error during communication: {e}")
        finally:
            self.shutdown()
    def shutdown(self):
        """
        Shuts down the client by closing the connection to the server.
        This method ensures that the client stops running and properly closes the socket.

        Side-effects:
        -------------
        - Sets `is_running` to False, stopping the client loop.
        - Closes the client socket if it is open.
        - Prints a message indicating that the client socket has been closed.
        """
        self.is_running = False
        if self.client_socket:
            self.client_socket.close()
            print("Client socket closed.")

if __name__ == "__main__":

    client= TCPClient()

    client.start()

    client.run()
    
    client.shutdown()
