package sample;

import java.io.Serializable;

public class Haubica implements Serializable
{
    private int posX;
    private int posY;
    int health;

    public Haubica(int posX, int posY)
    {
        this.posX = posX;
        this.posY = posY;
        health = 1;
    }

    public int getPosX() { return posX; }

    public int getPosY() {
        return posY;
    }

    public void setPosX(int posX) {
        this.posX = posX;
    }

    public void setPosY(int posY) {
        this.posY = posY;
    }

    public void hit()
    {
        health--;
    }

}
