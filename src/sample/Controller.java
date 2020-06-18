package sample;

import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.SplitPane;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.*;
import java.net.Socket;


public class Controller {

    @FXML
    VBox planszaMain;

    @FXML
    SplitPane mainSlitPane;

    Label txtFldTurn;

    GridPane enemyPane;
    GridPane ourPane;

    //przechowujemy tu egzemplarz gry
    volatile Game game;

    //informacja o tym ktorym jestes graczem
    boolean playerFirst;

    Thread yourTurnT;

    //informacja o kluczu gry potrzebnej do wskazania serverowi
    int gameIndex;

    //informuje czy gra sie zakonczyla, potrzebne do przerwania odswiezania planszy
    boolean gameEnd;

    Socket s;

    Stage mainStage;


    public Controller() throws IOException, ClassNotFoundException {

        //laczymy sie z serwerem
        System.out.println("Controller() start");
        s = new Socket("127.0.0.1", 1700);

        PrintWriter printWriter = new PrintWriter(s.getOutputStream());

        //wysylamy prosbe o wskazanie nam ktorym graczem jestesmy
        printWriter.println("getTurn " + 0);
        printWriter.flush();

        //odczytujemy zwrotna instrukcje zawierajaca ktorym graczem jestesmy i klucz gry
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(s.getInputStream()));
        String str = bufferedReader.readLine();
        System.out.println(str);

        String[] instruction;
        instruction = str.split(" ");

        int YourSeat = Integer.parseInt(instruction[0]);
        gameIndex = Integer.parseInt(instruction[1]);
        gameEnd = false;

        if(YourSeat == 1)
        {
            playerFirst = true;
            System.out.println("jestem graczem 1");
        }
        else
        {
            playerFirst = false;
            System.out.println("jestem graczem 2");
        }

        //prosimy serwer o wyslanie egzemplarza gry
        printWriter = new PrintWriter(s.getOutputStream());
        printWriter.println("get " + gameIndex);
        printWriter.flush();

        ObjectInputStream in = new ObjectInputStream(new BufferedInputStream(s.getInputStream()));

        //odczytujemy go i zapisujemy w zmiennej lokalnej
        game = (Game) in.readObject();



    }

    //funcja pomocnicza, sprawdza czy mozesz wykonac ruch w zaleznosci ktorym graczem jestes
    boolean CheckYourTurn()
    {
        if(game != null)
        {
            if(playerFirst)
            {
                return game.turn;
            }
            else {
                return !game.turn;
            }
        }
        return false;

    }

    //funcja pomocnicza, sprawdza czy umiejsciles swoja haubice na planszy
    boolean checkIfGunPlaced()
    {
        if(playerFirst)
        {
            return game.ourHaubica.getPosX() != -1 && game.ourHaubica.getPosY() != -1;
        }
        else
        {
            return game.enemyHaubica.getPosX() != -1 && game.enemyHaubica.getPosY() != -1;
        }
    }

    void setMainStage(Stage stage)
    {
        this.mainStage = stage;

        mainStage.setOnHiding(windowEvent -> {

            if(!gameEnd)
            {
                gameEnd = true;

                if(playerFirst)
                    game.ourPlayerSurrender = true;
                else
                    game.enemyPlayerSurrender = true;

                try {
                    PrintWriter printWriter = new PrintWriter(new BufferedOutputStream(s.getOutputStream()));
                    // korzystamy z instrukcji set i klucza oddzielonych spacja
                    printWriter.println("set " + gameIndex);
                    printWriter.flush();

                    ObjectOutputStream on = new ObjectOutputStream(new BufferedOutputStream(s.getOutputStream()));
                    on.writeObject(game);
                    on.flush();


                    printWriter = new PrintWriter(s.getOutputStream());
                    printWriter.println("endGame " + gameIndex);
                    printWriter.flush();
                }catch (IOException e){e.printStackTrace();}

            }

        });
    }

    //funkcja inicjalizacyjna javyFX wykonuje sie po controller()
    @FXML
    public void initialize()
    {
        initializePlansze();
        refreshBoard();

        //watek odswiezajacy gre
        Runnable yourTurn = () -> {
            // musimy zadeklarowac flage jako final, dla zachowania
            // zdognosci z EDT (event dispatch thread) linijka 176
            final boolean[] flag = {true};

            while (flag[0]) {

                try {
                    //wysyla zapytanie co 250 milisekund
                    Thread.sleep(250);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                try {
                    if(!gameEnd)
                    {
                        PrintWriter printWriter = new PrintWriter(s.getOutputStream());

                        //pytamy za pomoca instrukcji i klucza
                        printWriter.println("get " + gameIndex);
                        printWriter.flush();

                        ObjectInputStream in = new ObjectInputStream(new BufferedInputStream(s.getInputStream()));
                        Game gameTemp = (Game) in.readObject();

                        if (gameTemp != null)
                            game = gameTemp;

                        //kazda funkcja aktualizajaca interfejs musi zostac wywolana w
                        // EDT więc robimy to za pomocą tej instukcji
                        Platform.runLater(() -> {
                            //refreshBoard zaaktulizuje gre ostatni raz i podniesie flage do zakonczenia watku
                            // jesli gra nie jest zakonczona flaga nie zostanie podniesiona
                            flag[0] = refreshBoard();
                        });
                    }

                } catch (IOException | ClassNotFoundException e) {
                    e.printStackTrace();
                }


            }
        };yourTurnT = new Thread(yourTurn);
        yourTurnT.setDaemon(true);
        yourTurnT.start();
    }

    //funkcja inicjalizuje interfejs uzytkownika
    private void initializePlansze()
    {

        planszaMain.setSpacing(30);

        enemyPane = new GridPane();
        enemyPane.setAlignment(Pos.CENTER);


        for (int i=0;i<game.size+1;i++)
        {
            enemyPane.getColumnConstraints().add(new ColumnConstraints(40));
            enemyPane.getRowConstraints().add(new RowConstraints((40)));
        }

        for (int i=0;i<game.size+1;i++)
        {
            for (int j=0;j<game.size+1;j++)
            {
                Label label = new Label();
                label.setPrefHeight(40);
                label.setPrefWidth(40);
                label.setAlignment(Pos.CENTER);

                Button button = new Button();
                button.setPrefHeight(40);
                button.setPrefWidth(40);
                button.setAlignment(Pos.CENTER);



                if(i==0) {
                    label.setText(String.valueOf(j));
                    enemyPane.add(label,i,j);
                }
                else if (j==0) {
                    label.setText(String.valueOf(i));
                    enemyPane.add(label, i, j);
                }
                else {


                    int finalI = i;
                    int finalJ = j;

                    //oblusga zdarzen dla przyciskow na planszy gornej(przeciwnika)
                    button.setOnAction(new EventHandler<ActionEvent>() {
                        @Override
                        public synchronized void handle(ActionEvent actionEvent) {

                            //mozesz "strzelic" jesli jest twoj ruch i umiejsciles haubice na planszy
                            if(CheckYourTurn() && checkIfGunPlaced())
                            {
                                    //po strzale zmienia kolor kwadratu na czerwony, lub czarny jestli trafi
                                    if (playerFirst) {
                                        if (game.enemyPlansza[finalI - 1][finalJ - 1] == Stan.STAN_WOLNY) {
                                            game.enemyPlansza[finalI - 1][finalJ - 1] = Stan.STAN_ZNISZCZONY;
                                            button.setStyle("-fx-background-color: red");
                                        } else if (game.enemyPlansza[finalI - 1][finalJ - 1] == Stan.STAN_ZAJETY) {
                                            game.enemyPlansza[finalI - 1][finalJ - 1] = Stan.STAN_ZNISZCZONY;
                                            game.enemyHaubica.hit();
                                            button.setStyle("-fx-background-color: black");
                                        }


                                    } else {

                                        if (game.ourPlansza[finalI - 1][finalJ - 1] == Stan.STAN_WOLNY) {
                                            game.ourPlansza[finalI - 1][finalJ - 1] = Stan.STAN_ZNISZCZONY;
                                            button.setStyle("-fx-background-color: red");
                                        } else if (game.ourPlansza[finalI - 1][finalJ - 1] == Stan.STAN_ZAJETY) {
                                            game.ourPlansza[finalI - 1][finalJ - 1] = Stan.STAN_ZNISZCZONY;
                                            game.ourHaubica.hit();
                                            button.setStyle("-fx-background-color: black");
                                        }

                                    }
                                    //konczymy swoj ruch
                                    game.changeTurn();

                                        //wysylamy zaaktulizowana gre na server
                                        try {

                                            PrintWriter printWriter = new PrintWriter(new BufferedOutputStream(s.getOutputStream()));
                                            // korzystamy z instrukcji set i klucza oddzielonych spacja
                                            printWriter.println("set " + gameIndex);
                                            printWriter.flush();

                                            ObjectOutputStream on = new ObjectOutputStream(new BufferedOutputStream(s.getOutputStream()));
                                            on.writeObject(game);
                                            on.flush();

                                        } catch (IOException e) {
                                            e.printStackTrace();
                                        }
                            }

                            }

                    });
                    enemyPane.add(button, i, j);
                }
            }
        }


        enemyPane.setGridLinesVisible(true);
        planszaMain.getChildren().add(enemyPane);

        ourPane = new GridPane();
        ourPane.setAlignment(Pos.CENTER);


        for (int i=0;i<game.size;i++)
        {
            ourPane.getColumnConstraints().add(new ColumnConstraints(40));
            ourPane.getRowConstraints().add(new RowConstraints((40)));
        }

        for (int i=0;i<game.size+1;i++)
        {
            for (int j=0;j<game.size+1;j++)
            {
                Label label = new Label();
                label.setPrefHeight(40);
                label.setPrefWidth(40);
                label.setAlignment(Pos.CENTER);

                Button button = new Button();
                button.setPrefHeight(40);
                button.setPrefWidth(40);
                button.setAlignment(Pos.CENTER);


                if(i==0) {
                    label.setText(String.valueOf(j));
                    ourPane.add(label,i,j);
                }
                else if (j==0) {
                    label.setText(String.valueOf(i));
                    ourPane.add(label, i, j);
                }
                else {
                    int finalI = i;
                    int finalJ = j;

                    //obsluga zdarzen dla przyciskow na planszy wlasnej (dolnej)
                    button.setOnAction(actionEvent -> {

                        if(CheckYourTurn())
                        {
                            if(playerFirst)
                            {
                                if(game.enemyHaubica.getPosY() == -1 && game.enemyHaubica.getPosX() == -1)
                                {
                                    game.enemyPlansza[0][0] = Stan.STAN_WOLNY;
                                    game.enemyHaubica.setPosX(finalI - 1);
                                    game.enemyHaubica.setPosY(finalJ - 1 );
                                    game.ourPlansza[finalI-1][finalJ - 1 ] = Stan.STAN_ZAJETY;
                                    button.setStyle("-fx-background-color: green");
                                }
                            }
                            else {
                                if(game.ourHaubica.getPosY() == -1 && game.ourHaubica.getPosX() == -1)
                                {
                                    game.ourPlansza[0][0] = Stan.STAN_WOLNY;
                                    game.ourHaubica.setPosX(finalI -1);
                                    game.ourHaubica.setPosY(finalJ - 1);
                                    game.enemyPlansza[finalI-1][finalJ - 1] = Stan.STAN_ZAJETY;
                                    button.setStyle("-fx-background-color: green");

                                }

                            }
                            game.changeTurn();

                            try {

                                PrintWriter printWriter = new PrintWriter(s.getOutputStream());
                                printWriter.println("set " + gameIndex);
                                printWriter.flush();

                                ObjectOutputStream on = new ObjectOutputStream(new BufferedOutputStream(s.getOutputStream()));
                                on.writeObject(game);
                                on.flush();

                            }catch (IOException e){e.printStackTrace();}
                        }
                    });

                    ourPane.add(button, i, j);
                }
            }
        }

        ourPane.setGridLinesVisible(true);

        planszaMain.getChildren().add(ourPane);

        //dodanie panelu infomrującego o tym czyja tura
        txtFldTurn = new Label("");
        txtFldTurn.setAlignment(Pos.CENTER);
        txtFldTurn.setPrefHeight(20);
        txtFldTurn.setFont(Font.font("Verdana", FontWeight.BOLD, 16));

        planszaMain.getChildren().add(txtFldTurn);
    }

    //funkcja odswiezajaca interfejs gry na podstawie egzemplarza gry
    boolean refreshBoard()
    {
            if(playerFirst)
            {
                //System.out.println("refreshBoard() player1");
                if(game.enemyPlayerSurrender)
                {
                    if(!gameEnd)
                    {
                        System.out.println("Connection lost");
                        endGame("Przeciwnik się poddał");
                    }

                    return false;
                }
                if(game.enemyHaubica.health < 1)
                {
                    //konczy gre jako wygrana strona
                    if(!gameEnd)
                    {
                        System.out.println("koniec gry wygrana");
                        endGame("Brawo!!!");
                    }

                    return false;
                }

                if(game.ourHaubica.health < 1)
                {
                    //konczy gre jako przegrana strona

                    if(!gameEnd)
                    {
                        System.out.println("koniec gry przegrana");
                        endGame("Przegrałeś :(");

                    }
                    return false;

                }

                if(game.turn)
                {
                    txtFldTurn.setText("Twój ruch");
                    txtFldTurn.setStyle("-fx-text-fill: chartreuse");
                }
                else
                {
                    txtFldTurn.setText("Tura przeciwnika");
                    txtFldTurn.setStyle("-fx-text-fill: red");
                }

                ObservableList<Node> childrens = ourPane.getChildren();
                for (int i=0;i<game.size;i++)
                {
                    for (int j=0;j<game.size;j++)
                    {

                        if(game.ourPlansza[i][j] == Stan.STAN_ZNISZCZONY) {

                            for (Node node : childrens) {
                                if(ourPane.getRowIndex(node) != null && ourPane.getColumnIndex(node) != null)
                                {
                                    if(ourPane.getRowIndex(node) == j+1 && ourPane.getColumnIndex(node) == i+1)
                                    {
                                        node.setStyle("-fx-background-color: red");
                                    }
                                }

                            }
                        }

                    }
                }

            }
            else {

                if(game.ourPlayerSurrender)
                {

                    if(!gameEnd)
                    {
                        System.out.println("Player Surrender");
                        endGame("Przeciwnik się poddał");
                    }

                    return false;
                }
                if(game.ourHaubica.health < 1)
                {

                    if(!gameEnd)
                    {
                        System.out.println("koniec gry wygrana");
                        endGame("Brawo!!");
                    }
                    return false;
                }
                if(game.enemyHaubica.health < 1)
                {
                    if(!gameEnd)
                    {
                        System.out.println("koniec gry przegrana");
                        endGame("Przegrałeś");
                    }
                    return false;

                }

                if(!game.turn)
                {
                    txtFldTurn.setStyle("-fx-text-fill: chartreuse");
                    txtFldTurn.setText("Twój ruch");
                }
                else
                {
                    txtFldTurn.setStyle("-fx-text-fill: red");
                    txtFldTurn.setText("Tura przeciwnika");
                }

                //System.out.println("refreshBoard() player2");

                ObservableList<Node> childrens = ourPane.getChildren();
                for (int i=0;i<game.size;i++)
                {
                    for (int j=0;j<game.size;j++)
                    {

                        if(game.enemyPlansza[i][j] == Stan.STAN_ZNISZCZONY) {

                            for (Node node : childrens) {
                                if(ourPane.getRowIndex(node) != null && ourPane.getColumnIndex(node) != null)
                                {
                                    if(ourPane.getRowIndex(node) == j+1 && ourPane.getColumnIndex(node) == i+1)
                                    {
                                        node.setStyle("-fx-background-color: red");
                                    }
                                }

                            }
                        }

                    }
                }

            }
            return true;
    }

    private void endGame(String messege)
    {
        gameEnd = true;
        Platform.runLater(() -> {

            Stage dialogStage = new Stage();
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.setResizable(true);

            Button endButton = new Button("Wyjdź");
            endButton.setOnAction(actionEvent -> Platform.exit());

            VBox vbox = new VBox(new Text(messege), endButton);
            vbox.setAlignment(Pos.CENTER);
            vbox.setPadding(new Insets(15,15,15,15));

            dialogStage.setScene(new Scene(vbox));
            dialogStage.show();

            try {
                PrintWriter printWriter = new PrintWriter(s.getOutputStream());
                printWriter.println("endGame " + gameIndex);
                printWriter.flush();
            }catch (IOException e){e.printStackTrace();}
        });
    }


}
