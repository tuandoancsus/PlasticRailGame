package tage.nodeControllers;
import tage.*;
import org.joml.*;
import java.lang.Math;

/**
* A BouncingController is a node controller that, when enabled, causes any object
* it is attached to to bounce up and down along it's local Y-axis.
* @author Tuan Doan
*/
public class BouncingController extends NodeController
{
    private float bouncingSpeed = 0.005f; // Speed of bouncing
    private float amplitude = 0.5f; // Amplitude of the bounce (how high the object moves)
    private Engine engine;

    /** Creates a bouncing controller with vertical axis, and speed=1.0. */
    public BouncingController() { super(); }

    /** Creates a bouncing controller with specified engine and speed. */
    public BouncingController(Engine e, float speed)
    {   
        super();
        bouncingSpeed = speed;
        engine = e;
    }

    /** Sets the speed of the bounce. */
    public void setSpeed(float s) { bouncingSpeed = s; }

    /** Sets the amplitude (height) of the bounce. */
    public void setAmplitude(float amplitude) { this.amplitude = amplitude; }

    /** This is called automatically by the RenderSystem (via SceneGraph) once per frame
    * during display(). It applies the bouncing effect to the GameObject.
    */
    public void apply(GameObject gameObj)
    {   
        // Get elapsed time since the last frame (time in seconds)
        float elapsedTime = super.getElapsedTime();
        
        // Calculate the new Y position based on a sine wave
        float newY = amplitude * (float)Math.sin(elapsedTime * bouncingSpeed);
        
        // Get the current position of the object
        Vector3f position = gameObj.getLocalLocation();
        
        // Apply the updated position back to the object
        gameObj.setLocalLocation(new Vector3f(position.x, newY, position.z));
    }
}
