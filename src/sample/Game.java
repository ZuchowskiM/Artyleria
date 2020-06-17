package sample;

import java.io.Serializable;

public class Game implements Serializable {

    public Stan[][] enemyPlansza;
    public Stan[][] ourPlansza;
    public int size;
    public volatile Haubica ourHaubica;
    public volatile Haubica enemyHaubica;
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

}
