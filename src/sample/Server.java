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
    public static Map<Integer,Integer> ending;
    public static int gamesStarted;

    public static void main(String[] args) throws IOException
    {
        gamesStarted = 0;
        games = new HashMap<>();
        ending = new HashMap<>();

        ServerSocket serverSocket = new ServerSocket(1700);
        taken = false;
        System.out.println("Server running");

        Runnable send = ()->{

            while (true)
            {
                try {
                    //czekamy na instrukcje
                    Socket s = serverSocket.accept();

                    //odbieramy ja
                    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(s.getInputStream()));
                    String str = bufferedReader.readLine();
                    String[] instruction;
                    //dzielimy ja do odpowiedniego formatu
                    instruction = str.split(" ");
                    int gameKey = Integer.parseInt(instruction[1]);
                    //System.out.println(str);

                    //instrukcja do zainicjowania gry
                    if (instruction[0].equals("getTurn"))
                    {
                        PrintWriter printWriter = new PrintWriter(s.getOutputStream());
                        //jesli to jest pierwszy gracz to:
                        if(!taken)
                        {
                            //towrzymy gre
                            Game g = new Game(10,-1,-1,-1,-1);
                            gamesStarted++;
                            //dodajemy gre do mapy nadajemy jej klucz tutaj jest to gamesStarded
                            games.put(gamesStarted,g);
                            ending.put(gamesStarted, 0);

                            System.out.println("Utworzono grę numer: " + gamesStarted);

                            //odsylamy czy jestes pierwszym graczem i
                            // numer klucza do klienta ktory bedzie potem uzywal tego klucza
                            printWriter.println("1 " + (gamesStarted));
                            printWriter.flush();
                            taken = true;
                        }
                        else {
                            //jesli jestes deugim graczem odsylamy podobna informacje ale juz bez zakladania nowej gry
                            printWriter.println("2 " + (gamesStarted));
                            printWriter.flush();
                            taken = false;
                        }
                    }
                    else if (instruction[0].equals("set"))
                    {
                        //klient wysyla zaaktualizowana gre na server
                        ObjectInputStream in = new ObjectInputStream(new BufferedInputStream(s.getInputStream()));
                        games.replace(gameKey, (Game) in.readObject());
                        //System.out.println(games.get(gameKey).turn);
                    }
                    else if(instruction[0].equals("get"))
                    {
                        //klient prosi o wyslanie mu aktualnej gry z servera
                        ObjectOutputStream on = new ObjectOutputStream(new BufferedOutputStream(s.getOutputStream()));
                        on.writeObject(games.get(gameKey));

                        on.flush();
                        //System.out.println(games.get(gameKey).turn);
                    }
                    else if(instruction[0].equals("endGame"))
                    {
                        //po zakonczonej rozgrywce gra jest usuwana z mapy jesli oboje graczy wyslalo komunikat
                        ending.replace(gameKey, ending.get(gameKey)+1);

                        if(ending.get(gameKey) > 1)
                        {
                            games.remove(gameKey);
                            ending.remove(gameKey);
                            System.out.println("Gra numer: " + gameKey + " zostala zakonczona");
                            System.out.println("Liczba gier pozostalych: " + games.size());
                        }
                    }

                    s.close();


                }catch(IOException | ClassNotFoundException e) {e.printStackTrace();}

            }
        };new Thread(send).start();

    }

}
