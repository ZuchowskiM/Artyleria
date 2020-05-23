package sample;

import java.io.Serializable;

public class Game implements Serializable {

    public Stan[][] enemyPlansza;
    public Stan[][] ourPlansza;
    public int size;
    public Haubica ourHaubica;
    public Haubica enemyHaubica;
    boolean turn;

    public Game(int sizePlansza, int ourPosX,int ourPosY,int enemyPosX,int enemyPosY)
    {
        size = sizePlansza;
        enemyPlansza = new Stan[sizePlansza][sizePlansza];
        ourPlansza = new Stan[sizePlansza][sizePlansza];


        enemyHaubica = new Haubica(enemyPosX,enemyPosY);
        ourHaubica = new Haubica(ourPosX,ourPosY);

        for (int i=0;i<size;i++)
        {
            for (int j=0;j<size;j++)
            {
                if(enemyHaubica.getPosX() == i && enemyHaubica.getPosY() == j)
                {
                    enemyPlansza[i][j] = Stan.STAN_ZAJETY;
                }else
                    enemyPlansza[i][j] = Stan.STAN_WOLNY;
            }
        }

        for (int i=0;i<size;i++) {
            for (int j = 0; j < size; j++) {
                if (ourHaubica.getPosX() == i && ourHaubica.getPosY() == j) {
                    ourPlansza[i][j] = Stan.STAN_ZAJETY;
                } else
                    ourPlansza[i][j] = Stan.STAN_WOLNY;
            }
        }
        turn = false;
    }

    public void changeTurn()
    {
        turn = !turn;
    }

    public void hit(int posX,int posY)
    {
        if (ourHaubica.getPosX() == posX && ourHaubica.getPosY() == posY) {
            ourPlansza[posX][posY] = Stan.STAN_ZNISZCZONY;
            ourHaubica.hit();
        } else
            ourPlansza[posX][posY] = Stan.STAN_ZNISZCZONY;
    }

    public void fire(int posX,int posY)
    {
        if (enemyHaubica.getPosX() == posX && enemyHaubica.getPosY() == posY) {
            enemyPlansza[posX][posY] = Stan.STAN_ZNISZCZONY;
            enemyHaubica.hit();

        } else
            enemyPlansza[posX][posY] = Stan.STAN_ZNISZCZONY;
    }





}
