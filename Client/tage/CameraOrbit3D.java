package tage;

import tage.input.InputManager;

import java.lang.Math;

import tage.input.action.AbstractInputAction;
import net.java.games.input.Event;
import org.joml.*;

public class CameraOrbit3D {
    private Engine engine;
    private Camera camera;          // Camera being controlled
    private GameObject avatar;      // Target avatar the camera looks at
    private float cameraAzimuth;    // Rotation around the target y axis
    private float cameraElevation;  // Elevation of camera above target
    private float cameraRadius;     // Distance between camera and target
    public CameraOrbit3D(Camera cam, GameObject av, String gpName, Engine e)  {
        engine = e;
        camera = cam;
        avatar = av;
        cameraAzimuth = 0.0f;
        cameraElevation = 20.0f;
        cameraRadius = 2.0f;
        setupInputs(gpName);
        updateCameraPosition();
    }

    private void setupInputs(String gp) {
        OrbitAzimuthAction azmAction = new OrbitAzimuthAction();
        OrbitElevationAction elevationAction = new OrbitElevationAction();
        OrbitZoomAction zoomAction = new OrbitZoomAction();

        InputManager im = engine.getInputManager();
        im.associateAction(gp, net.java.games.input.Component.Identifier.Axis.RX, azmAction, InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
        im.associateAction(gp, net.java.games.input.Component.Identifier.Axis.RY, elevationAction, InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
        im.associateAction(gp, net.java.games.input.Component.Identifier.Axis.Z, zoomAction, InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);

        // Keys for testing
        im.associateAction(gp, net.java.games.input.Component.Identifier.Key.J, azmAction, InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
        im.associateAction(gp, net.java.games.input.Component.Identifier.Key.K, elevationAction, InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
        im.associateAction(gp, net.java.games.input.Component.Identifier.Key.L, zoomAction, InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
    }

    // Compute the camera’s azimuth, elevation, and distance, relative to
    // the target in spherical coordinates, then convert to world Cartesian
    // coordinates and set the camera position from that.
    public void updateCameraPosition() { 
        Vector3f avatarRot = avatar.getWorldForwardVector();
        double avatarAngle = Math.toDegrees((double)
        avatarRot.angleSigned(new Vector3f(0,0,-1), new Vector3f(0,1,0)));
        float totalAz = cameraAzimuth - (float)avatarAngle;
        double theta = Math.toRadians(totalAz);
        double phi = Math.toRadians(cameraElevation);
        float x = cameraRadius * (float)(Math.cos(phi) * Math.sin(theta));
        float y = cameraRadius * (float)(Math.sin(phi));
        float z = cameraRadius * (float)(Math.cos(phi) * Math.cos(theta));
        camera.setLocation(new
        Vector3f(x,y,z).add(avatar.getWorldLocation()));
        camera.lookAt(avatar);
    }
    
    private class OrbitAzimuthAction extends AbstractInputAction{ 
        public void performAction(float time, Event event)
        { 
            float rotAmount;
            if (event.getValue() < -0.2)
            { rotAmount=-0.2f; }
            else
            { if (event.getValue() > 0.2)
            { rotAmount=0.2f; }
            else
            { rotAmount=0.0f; }
            }
            cameraAzimuth += rotAmount;
            cameraAzimuth = cameraAzimuth % 360;
            updateCameraPosition();
        }
    }

    private class OrbitElevationAction extends AbstractInputAction{ 
        public void performAction(float time, Event event)
        { 
            float rotAmount;
            if (event.getValue() < -0.2)
            { rotAmount=-0.2f; }
            else
            { if (event.getValue() > 0.2)
            { rotAmount=0.2f; }
            else
            { rotAmount=0.0f; }
            }
            cameraElevation += rotAmount;
            cameraElevation = cameraElevation % 360;
            updateCameraPosition();
        }
    }

    private class OrbitZoomAction extends AbstractInputAction{ 
        public void performAction(float time, Event event)
        { 
            float zoomAmount;
            if (event.getValue() < -0.2)
            { zoomAmount=-0.2f; }
            else
            { if (event.getValue() > 0.2)
            { zoomAmount=0.2f; }
            else
            { zoomAmount=0.0f; }
            }
            cameraRadius += zoomAmount;
            updateCameraPosition();
        }
    }
}
