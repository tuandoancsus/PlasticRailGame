package myGame;

import java.util.ArrayDeque;
import java.util.Queue;
import java.lang.Math;

import tage.*;

import org.joml.*;

public final class VirusFactory {
    private final ObjShape virusShape;
    private final TextureImage virusTexture;
    private final Queue<GameObject> pool = new ArrayDeque<>();
    private final GameObject container; // The root object to which all viruses are added

    public VirusFactory(ObjShape shape, TextureImage texture, GameObject root) {
        virusShape = shape;
		virusTexture = texture;
        container = root;
    }
    

    /** Grab an inactive virus (prefer pooled) and set its per-instance state. */
    public GameObject spawn(Vector3f pos, float scale, float yawDeg) {
        GameObject virus = pool.poll();
        if (virus == null) {
            virus = new GameObject(container, virusShape, virusTexture);
        } else {
            virus.setParent(container); 
        }

        virus.setLocalTranslation(new Matrix4f().translation(pos));
        virus.setLocalScale(new Matrix4f().scaling(scale));
        virus.setLocalRotation(new Matrix4f()
                               .rotationY((float)Math.toRadians(yawDeg)));

        return virus;
    }

    /** Call when the virus 'dies' so we can reuse it. */
    public void despawn(GameObject virus) {
        pool.offer(virus);
    }
}
