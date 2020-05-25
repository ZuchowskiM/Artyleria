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
import javafx.scene.layout.*;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.*;
import java.net.Socket;


public class Controller {

    @FXML
    VBox planszaMain;

    GridPane enemyPane;
    GridPane ourPane;

    Game game;



    public Controller() throws IOException, ClassNotFoundException {

        System.out.println("tak");
        Socket s = new Socket("127.0.0.1", 1700);

        PrintWriter printWriter = new PrintWriter(s.getOutputStream());
        printWriter.println("get");
        printWriter.flush();

        ObjectInputStream in = new ObjectInputStream(new BufferedInputStream(s.getInputStream()));

        game = (Game) in.readObject();

    }


    @FXML
    public void initialize()
    {
        initializePlansze();
    }

    private void initializePlansze()
    {
        planszaMain.setSpacing(50);

        enemyPane = new GridPane();
        enemyPane.setAlignment(Pos.CENTER);

        //game = new Game(10,0,0,0,0);

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



                            try {
                                Socket s = new Socket("127.0.0.1", 1700);

                              PrintWriter printWriter = new PrintWriter(s.getOutputStream());
                              printWriter.println("get");
                              printWriter.flush();

                                ObjectInputStream in = new ObjectInputStream(new BufferedInputStream(s.getInputStream()));
                                game = (Game) in.readObject();
                                s.close();

                                refreshBoard();
                            } catch (IOException | ClassNotFoundException e) {
                                e.printStackTrace();
                            }

                            if(!game.turn)
                            {
                                game.changeTurn();
                                if(game.enemyHaubica.getPosY() == 0 && game.enemyHaubica.getPosX() == 0)
                                {
                                    game.enemyHaubica.setPosX(finalI);
                                    game.enemyHaubica.setPosY(finalJ);
                                    game.enemyPlansza[finalI-1][finalJ-1] = Stan.STAN_ZAJETY;
                                    button.setStyle("-fx-background-color: green");
                                }
                                else if(game.enemyPlansza[finalI-1][finalJ-1] == Stan.STAN_WOLNY)
                                {
                                    game.enemyPlansza[finalI-1][finalJ-1] = Stan.STAN_ZNISZCZONY;
                                    button.setStyle("-fx-background-color: red");
                                }
                                else if(game.enemyPlansza[finalI-1][finalJ-1] == Stan.STAN_ZAJETY)
                                {
                                    game.enemyPlansza[finalI-1][finalJ-1] = Stan.STAN_ZNISZCZONY;
                                    game.enemyHaubica.hit();
                                    button.setStyle("-fx-background-color: black");
                                    if(game.enemyHaubica.health < 1)
                                    {
                                        endGameWin();
                                    }
                                }

                                }

                            try {
                                Socket s = new Socket("127.0.0.1", 1700);

                                PrintWriter printWriter = new PrintWriter(s.getOutputStream());
                                printWriter.println("set");
                                printWriter.flush();

                                ObjectOutputStream on = new ObjectOutputStream(new BufferedOutputStream(s.getOutputStream()));
                                on.writeObject(game);
                                on.flush();
                                s.close();

                                refreshBoard();
                            }catch (IOException e){e.printStackTrace();}


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
                    label.setText(String.valueOf(10-j));
                    ourPane.add(label,i,j);
                }
                else if (j==10) {
                    label.setText(String.valueOf(i));
                    ourPane.add(label, i, j);
                }
                else {
                    int finalI = i;
                    int finalJ = j;
                    button.setOnAction(new EventHandler<ActionEvent>() {
                        @Override
                        public void handle(ActionEvent actionEvent) {

                            try {
                                Socket s = new Socket("127.0.0.1", 1700);

                                PrintWriter printWriter = new PrintWriter(s.getOutputStream());
                                printWriter.println("get");
                                printWriter.flush();

                                ObjectInputStream in = new ObjectInputStream(new BufferedInputStream(s.getInputStream()));
                                game = (Game) in.readObject();
                                s.close();
                            } catch (IOException | ClassNotFoundException e) {
                                e.printStackTrace();
                            }

                            if(game.turn)
                            {
                                game.changeTurn();
                                if(game.ourHaubica.getPosY() == 0 && game.ourHaubica.getPosX() == 0)
                                {
                                    game.ourHaubica.setPosX(finalI);
                                    game.ourHaubica.setPosY(finalJ);
                                    game.ourPlansza[finalI-1][finalJ] = Stan.STAN_ZAJETY;
                                    button.setStyle("-fx-background-color: green");
                                }
                                else if(game.ourPlansza[finalI-1][finalJ] == Stan.STAN_WOLNY)
                                {
                                    game.ourPlansza[finalI-1][finalJ] = Stan.STAN_ZNISZCZONY;
                                    button.setStyle("-fx-background-color: red");
                                }
                                else if(game.ourPlansza[finalI-1][finalJ] == Stan.STAN_ZAJETY)
                                {

                                    game.ourPlansza[finalI-1][finalJ] = Stan.STAN_ZNISZCZONY;
                                    game.ourHaubica.hit();
                                    button.setStyle("-fx-background-color: black");
                                    if(game.ourHaubica.health < 1)
                                    {
                                        endGameLose();
                                    }
                                }
                            }
                            try {
                                Socket s = new Socket("127.0.0.1", 1700);

                                PrintWriter printWriter = new PrintWriter(s.getOutputStream());
                                printWriter.println("set");
                                printWriter.flush();

                                ObjectOutputStream on = new ObjectOutputStream(new BufferedOutputStream(s.getOutputStream()));
                                on.writeObject(game);
                                on.flush();
                                s.close();
                            }catch (IOException e){}

                        }
                    });

                    ourPane.add(button, i, j);
                }
            }
        }

        ourPane.setGridLinesVisible(true);

        planszaMain.getChildren().add(ourPane);
    }

    public void endGameWin()
    {
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

    public void endGameLose()
    {
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

    void refreshBoard()
    {
        ObservableList<Node> childrens = ourPane.getChildren();
        for (int i=0;i<game.size;i++)
        {
            for (int j=0;j<game.size;j++)
            {

                if(game.ourPlansza[i][j] == Stan.STAN_ZNISZCZONY) {

                    for (Node node : childrens) {
                        if(ourPane.getRowIndex(node) == i && ourPane.getColumnIndex(node) == j)
                        {
                            node.setStyle("-fx-background-color: red");
                        }
                    }
                }


            }
        }
    }


}
