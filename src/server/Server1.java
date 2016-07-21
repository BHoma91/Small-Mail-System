/*
 * Ben Homa
 * Karl Capili
 * Ciera Jones
 */

package server;

import java.io.BufferedReader;
import java.io.PrintStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.net.Inet4Address;
import java.net.ServerSocket;

public class Server1
{
	// The server socket.
	private static ServerSocket svc = null;
	// The client socket.
	private static Socket clientSocket = null;
	// This chat server can accept up to MAX clients' connections.
	private static final int MAX = 25;
	private static final clientThread[] threads = new clientThread[MAX];

	public static void main(String args[])
	{
		// The default port number.
		int portno = 12345;
		if (args.length < 1)
		{
			System.out.println(">> Usage: java [-p portno] Server");
		} else
		{
			portno = Integer.parseInt(args[1]); //if port number is specified by user
		}
		System.out.println(">> Server Running on port number: " + portno);
		try
		{
			svc = new ServerSocket(portno,5); //creates server socket
		} catch (IOException e)
		{
			System.out.println(e);
		}

		while (true)
		{
			/*Handles threads */
			try
			{
				clientSocket = svc.accept();
				int i = 0;
				for (i = 0; i < MAX; i++)
				{
					if (threads[i] == null)
					{
						(threads[i] = new clientThread(clientSocket, threads)).start();
						break;
					}
				}
				if (i == MAX) //error when there are too many clients connected to the server
				{
					PrintStream output = new PrintStream(clientSocket.getOutputStream());
					output.println(">> Server queue full");
					output.close();
					clientSocket.close();
				}
			} catch (IOException e)
			{
				System.out.println(e);
			}
		}
	}
}

class clientThread extends Thread
{
	private String clientName = null;
	private BufferedReader input = null;
	private PrintStream output = null;
	private Socket clientSocket = null;
	private final clientThread[] threads;
	private int MAX;
	private static ArrayList<Chat> chatNames = new ArrayList<Chat>();

	static String chatName = "";
	String command = "";

	public clientThread(Socket clientSocket, clientThread[] threads)
	{
		this.clientSocket = clientSocket;
		this.threads = threads;
		MAX = threads.length;
	}

	public void run()
	{
		int MAX = this.MAX;
		clientThread[] threads = this.threads;

		try
		{
			/* Create input and output streams for this client.*/
			input = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
			output = new PrintStream(clientSocket.getOutputStream());

			/*User Information*/
			String name;
			String timeStamp = "";
			String ipAddress = "";

			/*Conversations in the specific chats*/
			ArrayList<String> conversation = new ArrayList<String>();
			Chat newChat = null;

			command = input.readLine().trim();

			if(command.equals("post")) //code that handles if post is the client that calls server
			{
				chatName = input.readLine().trim();
				name = System.getProperty("user.name");

				/* Welcome the new the client. */
				output.println(">> Hello " + name + " Enter Text Below: ");

				synchronized(this) //code is synchronized to prevent issues like race conditions
				{
					for(int i = 0; i < MAX; i++)
					{
						if(threads[i] != null && threads[i] == this)
						{
							clientName = chatName;
							break;
						}
					}
				}
				/* Start the conversation. */
				while(true)
				{
					Boolean T = true;
					Boolean flag = false;
					for(Chat chat: chatNames) //loop checks if a chat specified by input is already created
					{
						if(chat.chatName.equals(chatName))
						{
							conversation = chat.conversation; //if chat already exists conversation is added
							T = false;
						}
					}

					if(T == true) //if chat is new it is created
					{
						newChat = new Chat(chatName, conversation);
					}

					String line = input.readLine();
					String convoString = "";

					synchronized(this)
					{
						for (int i = 0; i < MAX; i++)
						{
							if (threads[i] != null && threads[i].clientName != null) //code handles creating the strings that are placed into the conversations
							{
								ipAddress = Inet4Address.getLocalHost().getHostAddress();
								timeStamp = new SimpleDateFormat("HH:mm:ss MM/dd/yyyy").format(Calendar.getInstance().getTime());
								if(!chatNames.isEmpty())
								{
									for(int j = 0; j < chatNames.size(); j++)
									{
										if(chatNames.get(j) != null)
										{
											if(chatNames.get(j).chatName.equals(chatName))
											{
												convoString = ("\n>> " + line);
												chatNames.get(j).conversation.add(convoString);
												flag = true;
											}
										}
									}
									if (flag != true){
									convoString = ("[" + name + "]" + ipAddress + " " + timeStamp + "\n>> " + line);
									conversation.add(convoString);
									newChat.setConversation(conversation);
									chatNames.add(newChat);
									}
								} else {
									convoString = ("[" + name + "]" + ipAddress + " " + timeStamp + "\n>> " + line);
									conversation.add(convoString);
									newChat.setConversation(conversation);
									chatNames.add(newChat);
								}
								break;
							}
						}
					}
					break;

				}
				output.println(">> Message Sent. Press Enter Key To Exit.");
				output.println(">> ***Goodbye " + name + "***");

				/*
				 * Close the output stream, close the input stream, close the socket.
				 */
				input.close();
				output.close();
				clientSocket.close();
			}
			else if (command.equals("get")) //code that handles if get client is calling the server
			{
				boolean t = true;
				boolean found = false;
				chatName = input.readLine().trim();
				for (int i = 0; i < MAX; i++) {
					if (threads[i] == this) {
						threads[i] = null;
					}
				}

				for(int i = 0; i < MAX; i++) //loop looks for chat name in data structure that corresponds to the chat name that user is looking for
				{
					if(threads[i] != null)
					{
						if(threads[i].clientName.equals(chatName))
						{
							for(int j = 0; j < chatNames.size(); j++)
							{
								if(chatNames.get(j) != null)
								{
									if(chatNames.get(j).chatName.equals(chatName))
									{
										output.println(chatNames.get(j).conversation); //prints conversation
										t = false;
										found = true;
									}
								}
							}
						}
					}
					if(t == false)
					{
						break;
					}
				}
				if(found == false)
				{
					output.println("ERROR: Group Name Does Not Exist");
				}

				output.println("Press Enter Key To Exit");
				/*Close sockets*/
				input.close();
				output.close();
				clientSocket.close();
			}
		} catch (IOException e)
		{
			System.out.println(e);
		}
	}
}