package sample;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;


public class Server
{
    public static boolean taken;
    public static Map<Integer,Game> games;
    public static int gamesStarted;

    public static void main(String[] args) throws IOException
    {
        gamesStarted = 0;
        games = new HashMap<>();
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
                    int gameKey = Integer.parseInt(instruction[1]);
                    //System.out.println(str);


                    if (instruction[0].equals("getTurn"))
                    {
                        PrintWriter printWriter = new PrintWriter(s.getOutputStream());
                        if(!taken)
                        {
                            Game g = new Game(10,-1,-1,-1,-1);
                            gamesStarted++;
                            games.put(gamesStarted,g);
                            System.out.println("Utworzono grę numer: " + gamesStarted);


                            printWriter.println("1 " + (gamesStarted));
                            printWriter.flush();
                            taken = true;
                        }
                        else {
                            printWriter.println("2 " + (gamesStarted));
                            printWriter.flush();
                            taken = false;
                        }
                    }
                    else if (instruction[0].equals("set"))
                    {
                        ObjectInputStream in = new ObjectInputStream(new BufferedInputStream(s.getInputStream()));
                        games.replace(gameKey, (Game) in.readObject());
                        //System.out.println(games.get(gameKey).turn);
                    }
                    else if(instruction[0].equals("get"))
                    {
                        ObjectOutputStream on = new ObjectOutputStream(new BufferedOutputStream(s.getOutputStream()));
                        on.writeObject(games.get(gameKey));
                        on.flush();
                        //System.out.println(games.get(gameKey).turn);
                    }
                    else if(instruction[0].equals("endGame"))
                    {
                        games.remove(gameKey);
                        System.out.println("Gra numer: " + gameKey + " zostala zakonczona");
                        System.out.println("Liczba gier pozostalych: " + games.size());
                    }

                    s.close();


                }catch(IOException | ClassNotFoundException e) {e.printStackTrace();}

            }
        };new Thread(send).start();

    }

}
