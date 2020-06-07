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
import javafx.scene.control.TextField;
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

    Label txtFldTurn;

    GridPane enemyPane;
    GridPane ourPane;

    volatile Game game;
    boolean playerFirst;
    Thread yourTurnT;
    int gameIndex;


    public Controller() throws IOException, ClassNotFoundException {

        System.out.println("Controller() start");
        Socket s = new Socket("127.0.0.1", 1700);

        PrintWriter printWriter = new PrintWriter(s.getOutputStream());

        printWriter.println("getTurn " + 0);
        printWriter.flush();
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(s.getInputStream()));
        String str = bufferedReader.readLine();
        System.out.println(str);

        String[] instruction;
        instruction = str.split(" ");

        int YourSeat = Integer.parseInt(instruction[0]);
        gameIndex = Integer.parseInt(instruction[1]);


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
        s.close();

        s = new Socket("127.0.0.1", 1700);
        printWriter = new PrintWriter(s.getOutputStream());

        printWriter.println("get " + gameIndex);
        printWriter.flush();

        ObjectInputStream in = new ObjectInputStream(new BufferedInputStream(s.getInputStream()));

        game = (Game) in.readObject();
        s.close();


    }

    boolean CheckYourTurn()
    {
        if(playerFirst)
        {
            //System.out.println("tura gracza 1");
            //System.out.println("tura gracza 2");
            return game.turn;
        }
        else {
            //System.out.println("tura gracza 2");
            //System.out.println("tura gracza 1");
            return !game.turn;

        }
    }


    @FXML
    public void initialize()
    {
        initializePlansze();
        refreshBoard();

        Runnable yourTurn = ()->
        {
            final boolean[] flag = {true};
            while(flag[0])
            {

                    try {
                        Thread.sleep(250);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    try {

                            if(!CheckYourTurn())
                            {
                                Socket s = new Socket("127.0.0.1", 1700);

                                PrintWriter printWriter = new PrintWriter(s.getOutputStream());
                                printWriter.println("get " + gameIndex);
                                printWriter.flush();

                                ObjectInputStream in = new ObjectInputStream(new BufferedInputStream(s.getInputStream()));
                                game = (Game) in.readObject();
                                s.close();

                                Platform.runLater(new Runnable() {
                                    @Override
                                    public void run() {
                                        flag[0] = refreshBoard();
                                    }
                                });

                            }





                    } catch (IOException | ClassNotFoundException e) {
                        e.printStackTrace();
                    }


            }
        };yourTurnT = new Thread(yourTurn);
        yourTurnT.start();
    }

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
                    button.setOnAction(new EventHandler<ActionEvent>() {
                        @Override
                        public void handle(ActionEvent actionEvent) {

                            if(CheckYourTurn())
                            {

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
                                    game.changeTurn();


                                        try {
                                            Socket s = new Socket("127.0.0.1", 1700);

                                            PrintWriter printWriter = new PrintWriter(new BufferedOutputStream(s.getOutputStream()));
                                            printWriter.println("set " + gameIndex);
                                            printWriter.flush();

                                            ObjectOutputStream on = new ObjectOutputStream(new BufferedOutputStream(s.getOutputStream()));
                                            on.writeObject(game);
                                            on.flush();

                                            on.close();
                                            s.close();

                                        } catch (IOException e) {
                                            e.printStackTrace();
                                        }

                                    //refreshBoard();

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
                    button.setOnAction(new EventHandler<ActionEvent>() {
                        @Override
                        public void handle(ActionEvent actionEvent) {

                            if(CheckYourTurn())
                            {
                                if(playerFirst)
                                {
                                    if(game.enemyHaubica.getPosY() == 0 && game.enemyHaubica.getPosX() == 0)
                                    {
                                        game.enemyPlansza[0][0] = Stan.STAN_WOLNY;
                                        game.enemyHaubica.setPosX(finalI - 1);
                                        game.enemyHaubica.setPosY(finalJ - 1 );
                                        game.ourPlansza[finalI-1][finalJ - 1 ] = Stan.STAN_ZAJETY;
                                        button.setStyle("-fx-background-color: green");
                                        game.changeTurn();
                                    }
                                }
                                else {
                                    if(game.ourHaubica.getPosY() == 0 && game.ourHaubica.getPosX() == 0)
                                    {
                                        game.ourPlansza[0][0] = Stan.STAN_WOLNY;
                                        game.ourHaubica.setPosX(finalI -1);
                                        game.ourHaubica.setPosY(finalJ - 1);
                                        game.enemyPlansza[finalI-1][finalJ - 1] = Stan.STAN_ZAJETY;
                                        button.setStyle("-fx-background-color: green");
                                        game.changeTurn();
                                    }

                                }

                                try {
                                    Socket s = new Socket("127.0.0.1", 1700);

                                    PrintWriter printWriter = new PrintWriter(s.getOutputStream());
                                    printWriter.println("set " + gameIndex);
                                    printWriter.flush();

                                    ObjectOutputStream on = new ObjectOutputStream(new BufferedOutputStream(s.getOutputStream()));
                                    on.writeObject(game);
                                    on.flush();
                                    s.close();
                                }catch (IOException e){e.printStackTrace();}
                            }
                        }
                    });

                    ourPane.add(button, i, j);
                }
            }
        }

        ourPane.setGridLinesVisible(true);

        planszaMain.getChildren().add(ourPane);

        txtFldTurn = new Label("");
        txtFldTurn.setAlignment(Pos.CENTER);
        txtFldTurn.setPrefHeight(20);
        txtFldTurn.setFont(Font.font("Verdana", FontWeight.BOLD, 16));

        planszaMain.getChildren().add(txtFldTurn);
    }

//    public void endGameWin()
//    {
//        Stage dialogStage = new Stage();
//        dialogStage.initModality(Modality.WINDOW_MODAL);
//        dialogStage.setResizable(true);
//
//        Button endButton = new Button("Wyjdź");
//        endButton.setOnAction(new EventHandler<ActionEvent>() {
//            @Override
//            public void handle(ActionEvent actionEvent) {
//                Platform.exit();
//            }
//        });
//
//        VBox vbox = new VBox(new Text("Brawo!!"), endButton);
//        vbox.setAlignment(Pos.CENTER);
//        vbox.setPadding(new Insets(15,15,15,15));
//
//        dialogStage.setScene(new Scene(vbox));
//        dialogStage.show();
//    }
//
//    public void endGameLose()
//    {
//        Stage dialogStage = new Stage();
//        dialogStage.initModality(Modality.WINDOW_MODAL);
//        dialogStage.setResizable(true);
//
//        Button endButton = new Button("Wyjdź");
//        endButton.setOnAction(new EventHandler<ActionEvent>() {
//            @Override
//            public void handle(ActionEvent actionEvent) {
//                Platform.exit();
//            }
//        });
//
//        VBox vbox = new VBox(new Text("Przegrałeś :("), endButton);
//        vbox.setAlignment(Pos.CENTER);
//        vbox.setPadding(new Insets(15,15,15,15));
//
//        dialogStage.setScene(new Scene(vbox));
//        dialogStage.show();
//    }

    boolean refreshBoard()
    {

        if(playerFirst)
        {
            System.out.println("refreshBoard() player1");

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

            if(game.enemyHaubica.health < 1)
            {
                //endGameWin();
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        Stage dialogStage = new Stage();
                        dialogStage.initModality(Modality.WINDOW_MODAL);
                        dialogStage.setResizable(true);

                        Button endButton = new Button("Wyjdź");
                        endButton.setOnAction(new EventHandler<ActionEvent>() {
                            @Override
                            public void handle(ActionEvent actionEvent) {
                                Platform.exit();
                            }
                        });

                        VBox vbox = new VBox(new Text("Brawo!!"), endButton);
                        vbox.setAlignment(Pos.CENTER);
                        vbox.setPadding(new Insets(15,15,15,15));

                        dialogStage.setScene(new Scene(vbox));
                        dialogStage.show();

                    }
                });
                return false;
            }
            if(game.ourHaubica.health < 1)
            {
                //endGameLose();
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        Stage dialogStage = new Stage();
                        dialogStage.initModality(Modality.WINDOW_MODAL);
                        dialogStage.setResizable(true);

                        Button endButton = new Button("Wyjdź");
                        endButton.setOnAction(new EventHandler<ActionEvent>() {
                            @Override
                            public void handle(ActionEvent actionEvent) {
                                Platform.exit();
                            }
                        });

                        VBox vbox = new VBox(new Text("Przegrałeś :("), endButton);
                        vbox.setAlignment(Pos.CENTER);
                        vbox.setPadding(new Insets(15,15,15,15));

                        dialogStage.setScene(new Scene(vbox));
                        dialogStage.show();
                    }
                });
                return false;
            }

        }
        else {

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

            System.out.println("refreshBoard() player2");

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

            if(game.ourHaubica.health < 1)
            {
                //endGameWin();
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        Stage dialogStage = new Stage();
                        dialogStage.initModality(Modality.WINDOW_MODAL);
                        dialogStage.setResizable(true);

                        Button endButton = new Button("Wyjdź");
                        endButton.setOnAction(new EventHandler<ActionEvent>() {
                            @Override
                            public void handle(ActionEvent actionEvent) {
                                Platform.exit();
                            }
                        });

                        VBox vbox = new VBox(new Text("Brawo!!"), endButton);
                        vbox.setAlignment(Pos.CENTER);
                        vbox.setPadding(new Insets(15,15,15,15));

                        dialogStage.setScene(new Scene(vbox));
                        dialogStage.show();
                    }
                });
                return false;
            }
            if(game.enemyHaubica.health < 1)
            {
                //endGameLose();
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        Stage dialogStage = new Stage();
                        dialogStage.initModality(Modality.WINDOW_MODAL);
                        dialogStage.setResizable(true);

                        Button endButton = new Button("Wyjdź");
                        endButton.setOnAction(new EventHandler<ActionEvent>() {
                            @Override
                            public void handle(ActionEvent actionEvent) {
                                Platform.exit();
                            }
                        });

                        VBox vbox = new VBox(new Text("Przegrałeś :("), endButton);
                        vbox.setAlignment(Pos.CENTER);
                        vbox.setPadding(new Insets(15,15,15,15));

                        dialogStage.setScene(new Scene(vbox));
                        dialogStage.show();
                    }
                });
                return false;
            }

        }
        return true;
    }


}
