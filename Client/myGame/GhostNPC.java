package myGame;

import tage.*;
import tage.shapes.AnimatedShape;

import org.joml.*;

public class GhostNPC extends GameObject{
    private int id;
    private MyGame game;
    private AnimatedShape s;
    private boolean isWaving = false;

    public GhostNPC(int id, AnimatedShape s, TextureImage t, Vector3f p) { 
        super(GameObject.root(), s, t);
        this.id = id;
        setPosition(p);
        this.setLocalRotation((new Matrix4f()).rotationY((float)java.lang.Math.toRadians(90.0f)));
    }

    public void setSize(boolean big) {
        if (!big) {
            this.setLocalScale((new Matrix4f()).scaling(0.15f)); // Small size
        } else {
            this.setLocalScale((new Matrix4f()).scaling(0.15f)); // Big size
        }
    }
    
    public void setPosition(Vector3f p) {
        Matrix4f translation = new Matrix4f().translation(p);
        this.setLocalTranslation(translation);
    }
    
    public int getID() {
        return id;
    }

    public void setWaving(boolean waving) {
        if (waving && !isWaving) {
            isWaving = true;
            System.out.println("Start waving");
            game.npcWave(); 
        } else if (!waving && isWaving) {
            isWaving = false;
            System.out.println("Stop waving");
            game.npcStopWave(); 
        }
    }

    public void setGame(MyGame g) {
        game = g;
    }
}
