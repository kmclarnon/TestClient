package com.testclient;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.net.UnknownHostException;

import com.messenger.protocol.PacketHandler;
import com.messenger.protocol.packets.*;


public class TestClient
{
    private static Socket socket;
    private static BufferedReader in;
    private static DataOutputStream out;
    private static Integer authID;
    
    private static PacketHandler phandle;
    
    public static void main(String args[])
    {
        phandle = new PacketHandler();
        listenSocket();
        new Thread(new Listener(in)).start();
        run();
    }

    public static void listenSocket(){
      //Create socket connection
         try{
           socket = new Socket("127.0.0.1", 6789);
           out = new DataOutputStream(socket.getOutputStream());
           in = new BufferedReader(new InputStreamReader(
                      socket.getInputStream()));
         } catch (UnknownHostException e) {
           System.out.println("Unknown host: kq6py");
           System.exit(1);
         } catch  (IOException e) {
           System.out.println("No I/O");
           System.exit(1);
         }
      }
    
    public static void run()
    {
        BufferedReader con = new BufferedReader(new InputStreamReader(System.in));
        InputProcessor ip = new InputProcessor(con);
        
        while(true)
        {
            try
            {
               ip.processInput();
            }
            catch(Exception e)
            {
                System.out.println(e);
            }
        }
     }
    
    private static class InputProcessor
    {
        CommandProcessor cp;
        BufferedReader input;
        
        public InputProcessor(BufferedReader input)
        {
            cp = new CommandProcessor("/");
            this.input = input;
        }
        
        public void processInput()
        {
            try
            {
                // fetch input
                String text = input.readLine();
                if(cp.isCommand(text))
                {
                    System.out.println("Detected command, processing...");
                    cp.processCommand(text);
                }
                else
                {
                    Message m = new Message(-2, TestClient.authID, text);
                    out.writeBytes(m.Encode() + "\n");
                }
            }
            catch(Exception e)
            {
                System.out.println("Failed to process input");
                System.out.println(e.toString());
                e.printStackTrace();
            }
        }
        
        private static class CommandProcessor
        {
            String cmdInd;
            
            public CommandProcessor(String cmdInd)
            {
                this.cmdInd = cmdInd;
            }
            
            public Boolean isCommand(String input)
            {
                System.out.println(input.substring(0,1));
                return input.substring(0, 1).equals(this.cmdInd);
            }
            
            public void processCommand(String input)
            {
                String in[] = input.split(" ");
                System.out.println(in[0]);
                try
                {
                    if(in[0].equals("/authreq"))
                    {
                        AuthRequest p = new AuthRequest(in[1], in[2]);
                        out.writeBytes(p.Encode() + "\n");
                    }
                    else
                    {
                        System.out.println("Unknown command");
                    }
                }
                catch(Exception e)
                {
                    System.out.println(e.toString());
                }

            }
        }
        
    }
    
    private static class Listener implements Runnable
    {

        private BufferedReader in;
        
        public Listener(BufferedReader in)
        {
            this.in = in;
        }
        
        @Override
        public void run()
        {
            while(true)
            {
                try
                {
                    String input = in.readLine();
                    if(input.length() != 0)
                    {
                        Packet p = phandle.Unmarshall(input);
                        System.out.println(input);
                        switch(p.GetID())
                        {
                            case Packet.AUTH_ACCEPT:
                            {
                                TestClient.authID = ((AuthAccept)p).getAuthID();
                                System.out.println("Authorizaiton accepted by server");
                                System.out.println("AuthID recieved by server: " + TestClient.authID.toString());
                                break;
                            }
                            case Packet.AUTH_REJECT:
                            {
                                System.out.println("Authorization Rejected by server: " + ((AuthReject)p).getReason());
                                break;
                            }
                            case Packet.MESSAGE: 
                            {
                                System.out.println(((Message)p).getMessage());
                                break;
                            }
                            case Packet.UNKNOWN: 
                        }
                    }
                        
                }
                catch(IOException e)
                {
                    System.out.println("Read Failed");
                    System.exit(1);
                }
            }
            
        }
        
    }
}
