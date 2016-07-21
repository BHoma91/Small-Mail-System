/*
 * Ben Homa
 * Karl Capili
 * Ciera Jones
 */

package server;


import java.util.ArrayList;

public class Chat 
{
	ArrayList<String> conversation = new ArrayList<String>(); //array list of strings that make up a conversation in a group
	String chatName;
	
	public Chat(String chatName, ArrayList<String> conversation) //chat constructor
	{
		this.chatName = chatName;
		this.conversation = conversation;
	}
	
	public void setchatName(String chatName)
	{
		this.chatName = chatName;
	}
	
	public String getChatName()
	{
		return chatName;
	}
	
	public void setConversation(ArrayList<String> conversation)
	{
		this.conversation = conversation;
	}
	
	public ArrayList<String> getConversation()
	{
		return conversation;
	}
}
