package myGame;

import tage.*;
import org.joml.*;

public class GhostNPC extends GameObject{
    private int id;
    public GhostNPC(int id, ObjShape s, TextureImage t, Vector3f p) { 
        super(GameObject.root(), s, t);
        this.id = id;
        setPosition(p);
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
}
