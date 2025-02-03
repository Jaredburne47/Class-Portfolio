
import socket
import sys

class TCPServer:
        def __init__(self, host='127.0.0.1', port=65432):
            """
            Initializes the TCPServer instance with the specified host and port.

            Parameters:
            -----------
            host : str, optional
                The IP address on which the server listens. Defaults to '127.0.0.1' (localhost).
            port : int, optional
                The port number on which the server listens. Defaults to 65432.
            
            Attributes:
            -----------
            is_running : bool
                A flag indicating whether the server is currently running. Defaults to False.
            server_socket : socket.socket or None
                The server socket that listens for incoming connections. Defaults to None.
            """
            self.host = host
            self.port = port
            self.is_running = False
            self.server_socket = None
        def start(self):
            """
            Starts the server by creating a socket, binding it to the specified host and port,
            and setting the server to listen for incoming connections.

            Side-effects:
            -------------
            - Initializes the server socket and starts listening.
            - Prints a message indicating the server has started successfully.
            """
            self.server_socket = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
            self.server_socket.bind((self.host,self.port))
            self.server_socket.listen(5)
            self.server_socket.settimeout(1.0)
            self.is_running = True
            print(f"Server started at {self.host}:{self.port}")
        def run(self):
            """
            Runs the main server loop to accept and handle client connections.
            This method keeps the server running until the `is_running` flag is set to False.
            
            It continuously listens for new client connections and processes their requests
            until the server is stopped or interrupted.

            Side-effects:
            -------------
            - Accepts new client connections.
            - Receives and processes client requests.
            - Sends responses back to clients.
            - Handles any errors during communication or server operation.
            
            Raises:
            -------
            socket.timeout
                If no client connection is made within the specified timeout.
            Exception
                For any general server errors during connection handling.
                """
             #loop to accept and handle client connections
            while self.is_running:
                try:
                    # Accept a new client connection
                    client_socket, addr = self.server_socket.accept()
                    print(f"Connection established with {addr}")
                    # Send a welcome message to the client
                    client_socket.sendall(b"Welcome to the calculator server. Enter operation (ADD/SUB/DIV/MUL) and operands (e.g., ADD 5 10) or 'quit' to exit:\n")
                    #loop to handle client requests
                    while self.is_running:
                        try:
                            data = client_socket.recv(1024).decode()  
                            if not data or data.lower() == 'quit':  
                                break
                            response = self.process_request(data)  
                            client_socket.sendall(response.encode())  
                        except Exception as e:
                            print(f"Error: {e}")
                            break
                    client_socket.close()
                    print(f"Connection closed with {addr}")
                except socket.timeout:
                        continue
                except Exception as e:
                        print(f"Server error: {e}")
        def process_request(self, data):
            """
            Process a client request and perform the specified arithmetic operation.
            This method parses the input string, validates the operation and operands,
            performs the requested arithmetic operation, and returns the result.
            It handles addition, subtraction, multiplication, and division operations.

            Parameters:
            -----------
            data : str
                A string containing the operation and operands in the format:
                "<OPERATION> <OPERAND1> <OPERAND2>"
                e.g., "ADD 5 3" or "DIV 10 2"

            Returns:
            --------
            str
                The result of the arithmetic operation as a string, or an error message
                if the input is invalid or an error occurs during processing.

            Raises:
            -------
            ValueError
                If the operands are not valid integers.
            ZeroDivisionError
                If a division by zero is attempted.

            Examples:
            ---------
            >>> server = TCPServer()
            >>> server.process_request("ADD 5 3")
            '8'
            >>> server.process_request("DIV 10 2")
            '5.0'
            >>> server.process_request("MUL 4 6")
            '24'
            >>> server.process_request("SUB 15 7")
            '8'
            >>> server.process_request("ADD 5 abc")
            'ERROR: Operands must be valid integers'
            >>> server.process_request("DIV 10 0")
            'ERROR: Division by zero error. The second operand cannot be zero.'
            >>> server.process_request("XOR 1 2")
            'ERROR: Invalid operation. Valid operations are ADD, SUB, DIV, and MUL'
            """
            try:
                # Split data into parts and extracts operation and operands
                parts = data.strip().split()
                if len(parts) != 3:
                    return "ERROR: Invalid number of parameters. Valid format is: <OPERATION> <OPERAND1> <OPERAND2>\n"

                operation = parts[0].upper()
                number1, number2 = int(parts[1]), int(parts[2])

                # Perform the appropriate arithmetic operation based on what the user enters
                if operation == 'ADD':
                    result = number1 + number2
                elif operation == 'SUB':
                    result = number1 - number2
                elif operation == 'MUL':
                    result = number1 * number2
                elif operation == 'DIV':
                    if number2 == 0:
                        return "ERROR: Division by zero error. The second operand cannot be zero.\n"
                    result = number1 / number2
                else:
                    return "ERROR: Invalid operation. Valid operations are ADD, SUB, DIV, and MUL.\n"
                
                return f"{result}"
            except ValueError:
                return "ERROR: Operands must be valid integers.\n"
            except Exception as e:
                return f"ERROR: {str(e)}\n"
        def shutdown(self):
            """
            Shuts down the server by stopping the main loop and closing the server socket.
            This method ensures that the server stops listening for new connections and
            properly closes any open resources.

            Side-effects:
            -------------
            - Sets `is_running` to False, effectively stopping the server.
            - Closes the server socket if it is open.
            - Prints a message indicating that the server has been shut down.
            """
            # Stops the server and closes the socket
            self.is_running = False
            if self.server_socket:
                self.server_socket.close()
            print("Server shut down.")

if __name__ == "__main__":

    server = TCPServer()

    try:
        server.start()  
        server.run()    
    except KeyboardInterrupt:
        print("Server interrupted. Shutting down...")
    finally:
        server.shutdown()  
                                    