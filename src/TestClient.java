import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.net.UnknownHostException;


public class TestClient
{
    private static Socket socket;
    private static BufferedReader in;
    private static DataOutputStream out;
    
    public static void main(String args[])
    {
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
        
        while(true)
        {
            try
            {
                //Send data over socket
               String text = con.readLine();
               System.out.println("Console received: " + text);
               out.writeBytes(text + "\n");
            }
            catch(Exception e)
            {
                System.out.println(e);
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
                    String line = in.readLine();
                    System.out.println("Text recieved: " + line);
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
