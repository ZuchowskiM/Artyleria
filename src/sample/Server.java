package sample;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class Server
{
    public static Game g;

    public static void main(String[] args) throws IOException
    {

        g = new Game(10,0,0,0,0);
        ServerSocket serverSocket = new ServerSocket(1700);


        Runnable send = ()->{

            while (true)
            {
                try {

                    Socket s = serverSocket.accept();


                    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(s.getInputStream()));
                    String str = bufferedReader.readLine();
                    System.out.println(str);

                    if (str.equals("set"))
                    {
                        ObjectInputStream in = new ObjectInputStream(new BufferedInputStream(s.getInputStream()));
                        g = (Game) in.readObject();
                        System.out.println(g.turn);
                    }
                    if(str.equals("get"))
                    {
                        ObjectOutputStream on = new ObjectOutputStream(new BufferedOutputStream(s.getOutputStream()));
                        on.writeObject(g);
                        on.flush();
                        System.out.println(g.turn);
                    }




                }catch(IOException | ClassNotFoundException e) {e.printStackTrace();}

            }
        };new Thread(send).start();

//        Runnable get = ()->{
//
//            while (true)
//            {
//                try {
//                    Socket s = serverSocket.accept();
//                    ObjectOutputStream on = new ObjectOutputStream(new BufferedOutputStream(s.getOutputStream()));
//
//
//                    on.writeObject(g);
//                    on.flush();
//                    System.out.println(g.turn);
//
//
//
//                }catch(IOException e) {e.printStackTrace();}
//
//            }
//        };new Thread(get).start();

    }

}
