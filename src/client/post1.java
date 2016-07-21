/*
 * Ben Homa
 * Karl Capili
 * Ciera Jones
 */

package client;

import java.io.PrintStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;

/*
post [-h hostname] [-p port] groupname
*/

public class post1 implements Runnable
{
	// The client socket
	private static Socket cs = null;
	// The output stream
	private static PrintStream output = null;
	// The input stream
	private static BufferedReader input = null;
	private static BufferedReader input2 = null;
	private static boolean CLOSED = false;

	public static void main(String[] args)
	{
		// The default port.
		int portno = 12345;
		// The default host.
		String host = "localhost";
		String chatName = "";

		if (args.length == 1)
		{
			chatName = args[0];
		} else if (args.length == 3 && args[0].equals("-h"))
		{
			host = args[1];
			chatName = args[2];
		} else if (args.length == 3 && args[0].equals("-p"))
		{
			portno = Integer.parseInt(args[1]);
			chatName = args[2];
		} else if (args.length == 5)
		{
			host = args[1];
			portno = Integer.parseInt(args[3]);
			chatName = args[4];
		} else
		{
			System.err.println(">> USAGE: java post [-h hostname] [-p portno] groupname");
			System.exit(1);
		}

		/*
		 * Open a socket on a given host and port. Open input and output streams.
		 */
		try
		{
			cs = new Socket(host, portno);
			input = new BufferedReader(new InputStreamReader(cs.getInputStream()));
			output = new PrintStream(cs.getOutputStream());
			input2 = new BufferedReader(new InputStreamReader(System.in));

		} catch (UnknownHostException e)
		{
			System.err.println(">> ERROR: Unknown host " + host);
		} catch (IOException e)
		{
			System.err.println(">> ERROR: Could not get I/O for the connection to the host "
					+ host);
		}
		if (cs != null && output != null && input != null)
		{
			try
			{
				/* Create a thread to read from the server. */
				new Thread(new post1()).start();
				output.println("post");
				output.println(chatName);
				while (!CLOSED) {
					output.println(input2.readLine().trim());
				}
				/*
				 * Close the output stream, close the input stream, close the socket.
				 */
				output.close();
				input.close();
				cs.close();
			} catch (IOException e) {
				System.err.println(e);
			}
		}
	}

	public void run() //do the work for the threads
	{
		String responseLine;
		try
		{
			while ((responseLine = input.readLine()) != null)
			{

				System.out.println(responseLine);
				if (responseLine.compareTo(">> Message Sent") == 0)
					break;


			}
			CLOSED = true;

		} catch (IOException e)
		{
			System.err.println(e);
		}
	}
}
