package sample;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;


public class Server
{
    public static boolean taken;
    public static ArrayList<Game> games;
    public static void main(String[] args) throws IOException
    {
        games = new ArrayList<>();
        ServerSocket serverSocket = new ServerSocket(1700);
        taken = false;
        System.out.println("Server running");

        Runnable send = ()->{

            while (true)
            {
                try {

                    Socket s = serverSocket.accept();


                    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(s.getInputStream()));
                    String str = bufferedReader.readLine();
                    String[] instruction;
                    instruction = str.split(" ");
                    int index = Integer.parseInt(instruction[1]);
                    System.out.println(str);


                    if (instruction[0].equals("getTurn"))
                    {
                        PrintWriter printWriter = new PrintWriter(s.getOutputStream());
                        if(!taken)
                        {
                            Game g = new Game(10,0,0,0,0);
                            games.add(g);
                            System.out.println("Utworzono grę numer: " + games.size());

                            printWriter.println("1 " + (games.size() - 1));
                            printWriter.flush();
                            taken = true;
                        }
                        else {
                            printWriter.println("2 " + (games.size() - 1));
                            printWriter.flush();
                            taken = false;
                        }
                    }
                    else if (instruction[0].equals("set"))
                    {
                        ObjectInputStream in = new ObjectInputStream(new BufferedInputStream(s.getInputStream()));
                        games.set(index, (Game) in.readObject());
                        System.out.println(games.get(index).turn);
                    }
                    else if(instruction[0].equals("get"))
                    {
                        ObjectOutputStream on = new ObjectOutputStream(new BufferedOutputStream(s.getOutputStream()));
                        on.writeObject(games.get(index));
                        on.flush();
                        System.out.println(games.get(index).turn);
                    }

                    s.close();


                }catch(IOException | ClassNotFoundException e) {e.printStackTrace();}

            }
        };new Thread(send).start();

    }

}
