package myGame;

import tage.*;
import tage.shapes.*;
import tage.input.*;
import tage.input.action.*;

import java.lang.Math;
import java.awt.*;

import java.awt.event.*;

import java.io.*;
import java.util.*;
import java.util.UUID;
import java.net.InetAddress;

import java.net.UnknownHostException;

import org.joml.*;

import net.java.games.input.*;
import net.java.games.input.Component.Identifier.*;
import tage.networking.IGameConnection.ProtocolType;
import tage.physics.PhysicsEngine;
import tage.physics.PhysicsObject;
import tage.physics.JBullet.JBulletPhysicsEngine;
import tage.physics.JBullet.JBulletPhysicsObject;

public class MyGame extends VariableFrameRateGame
{
	//physic
	private PhysicsEngine physicsEngine;
	private PhysicsObject planeP;
	private boolean running = false;
	private float vals[] = new float[16];

	//pill
	private boolean pillHeld = false;
	private PhysicsObject pillP;
	private float tossForce = 525.0f;

	private static Engine engine;
	private InputManager im;
	private GhostManager gm;

	private int counter=0;
	private Vector3f currentPosition;
	private Matrix4f initialTranslation, initialRotation, initialScale;
	private double startTime, prevTime, elapsedTime, amt;

	private GameObject avatar, avatar2, x, y, z, pillBottle, terr, plane,pill;
	private AnimatedShape avatarS;
	private ObjShape avatar2S, ghostS, linxS, linyS, linzS, pillBottleS, terrS, planeS, pillS;
	private TextureImage avatarT, avatar2T, ghostT, hills, grass, floor, pillT;
	private int lakeIslands, background; // skyboxes
	private boolean avatarRendered = false;
	private Light light;
	private CameraOrbit3D orbitController;

	private String serverAddress;
	private int serverPort;
	private ProtocolType serverProtocol;
	private ProtocolClient protClient;
	private boolean isClientConnected = false;

	public MyGame(String serverAddress, int serverPort, String protocol)
	{	super();
		gm = new GhostManager(this);
		this.serverAddress = serverAddress;
		this.serverPort = serverPort;
		if (protocol.toUpperCase().compareTo("TCP") == 0)
			this.serverProtocol = ProtocolType.TCP;
		else
			this.serverProtocol = ProtocolType.UDP;
	}

	public static void main(String[] args)
	{	MyGame game = new MyGame(args[0], Integer.parseInt(args[1]), args[2]);
		engine = new Engine(game);
		game.initializeSystem();
		game.game_loop();
	}

	@Override
	public void loadShapes()
	{
		ghostS = new ImportedModel("dolphinHighPoly.obj");
		//avatarS = new ImportedModel("avatar1.obj");
		avatarS = new AnimatedShape("model.rkm", "model.rks");
		avatarS.loadAnimation("WALK","walkModel.rka");
		avatarS.loadAnimation("IDLE","idle.rka");
		avatarS.loadAnimation("RUN","sprint.rka");
		avatarS.loadAnimation("JUMP","jump3.rka");

		avatar2S = new ImportedModel("finalModel.obj");
		pillBottleS = new ImportedModel("PillBottle.obj");
		pillS = new ImportedModel("pill.obj");
		linxS = new Line(new Vector3f(0f,0f,0f), new Vector3f(3f,0f,0f));
		linyS = new Line(new Vector3f(0f,0f,0f), new Vector3f(0f,3f,0f));
		linzS = new Line(new Vector3f(0f,0f,0f), new Vector3f(0f,0f,-3f));

		terrS = new TerrainPlane(1000); // pixels per axis = 1000x1000

		planeS = new Plane();
		
	}

	@Override
	public void loadTextures()
	{	avatarT = new TextureImage("spiderman.png");
		avatar2T = new TextureImage("davidbase.png");
		ghostT = new TextureImage("redDolphin.jpg");
		pillT = new TextureImage("pillbottle.png");

		hills = new TextureImage("hills.jpg");
		floor = new TextureImage("floor.jpg");
		
	}
	@Override
	public void buildObjects()
	{	Matrix4f initialTranslation, initialRotation, initialScale;

		// build avatar
		avatar = new GameObject(GameObject.root(), avatarS, avatarT);
		initialTranslation = (new Matrix4f()).translation(-1f,2,1f);
		avatar.setLocalTranslation(initialTranslation);
		initialRotation = (new Matrix4f()).rotationY((float)java.lang.Math.toRadians(135.0f));
		initialScale = (new Matrix4f()).scaling(0.25f);
		avatar.setLocalScale(initialScale);
		avatar.setLocalRotation(initialRotation);
		avatarS.playAnimation("IDLE", 0.1f, AnimatedShape.EndType.LOOP, 0);

		avatar.getRenderStates().disableRendering();
		//avatar.getRenderStates().setModelOrientationCorrection((new Matrix4f()).rotationY((float)java.lang.Math.toRadians(180.0f)));

		avatar2 = new GameObject(GameObject.root(), avatar2S, avatar2T);
		initialTranslation = (new Matrix4f()).translation(-1f,2,1f);
		avatar2.setLocalTranslation(initialTranslation);
		initialRotation = (new Matrix4f()).rotationY((float)java.lang.Math.toRadians(135.0f));
		initialScale = (new Matrix4f()).scaling(0.25f);
		avatar2.setLocalScale(initialScale);
		avatar2.setLocalRotation(initialRotation);
		avatar2.getRenderStates().disableRendering();


		// build Pill Bottle building
		pillBottle = new GameObject(GameObject.root(), pillBottleS, pillT);
		initialTranslation = (new Matrix4f()).translation(10,0,-10);
		pillBottle.setLocalTranslation(initialTranslation);
		initialScale = (new Matrix4f()).scaling(5.0f);
		initialRotation = (new Matrix4f()).rotationY((float)java.lang.Math.toRadians(360.0f));
		pillBottle.setLocalScale(initialScale);
		pillBottle.setLocalRotation(initialRotation);
		pill = new GameObject(GameObject.root(), pillS, pillT);
		pill.setLocalTranslation(new Matrix4f().translation(10, 1, -10)); // start inside pillBottle
		double[] tempTransform = toDoubleArray(pill.getLocalTranslation().get(vals));
		pill.setLocalScale(new Matrix4f().scaling(1.2f));

		// build terrain
		terr = new GameObject(GameObject.root(), terrS, floor);
		initialTranslation = (new Matrix4f()).translation(0f,0f,0f);
		terr.setLocalTranslation(initialTranslation);
		initialScale = (new Matrix4f()).scaling(20.0f, 1.0f, 20.0f);
		terr.setLocalScale(initialScale);
		terr.setHeightMap(hills);

		terr.getRenderStates().setTiling(1);
		terr.getRenderStates().setTileFactor(10);
		
		//build physic ground plane
		plane = new GameObject(GameObject.root(), planeS, grass);
		plane.setLocalTranslation((new Matrix4f()).translation(0, -2.75f, 0));
		plane.setLocalScale((new Matrix4f()).scaling(8f));
		
		// add X,Y,-Z axes
		x = new GameObject(GameObject.root(), linxS);
		y = new GameObject(GameObject.root(), linyS);
		z = new GameObject(GameObject.root(), linzS);
		(x.getRenderStates()).setColor(new Vector3f(1f,0f,0f));
		(y.getRenderStates()).setColor(new Vector3f(0f,1f,0f));
		(z.getRenderStates()).setColor(new Vector3f(0f,0f,1f));
	}

	@Override
	public void loadSkyBoxes()
	{ 	//fluffyClouds = (engine.getSceneGraph()).loadCubeMap("fluffyClouds");
		background = (engine.getSceneGraph()).loadCubeMap("background");
		(engine.getSceneGraph()).setActiveSkyBoxTexture(background);
		(engine.getSceneGraph()).setSkyBoxEnabled(true);
		}

	@Override
	public void initializeLights()
	{	Light.setGlobalAmbient(.5f, .5f, .5f);

		light = new Light();
		light.setLocation(new Vector3f(0f, 5f, 0f));
		(engine.getSceneGraph()).addLight(light);
	}

	@Override
	public void initializeGame()
	{	prevTime = System.currentTimeMillis();
		startTime = System.currentTimeMillis();
		(engine.getRenderSystem()).setWindowDimensions(1900,1000);

		// ----------------- initialize camera ----------------
		//positionCameraBehindAvatar();

		// --- initialize physics system ---
		float[] gravity = {0f, -5f, 0f};
		physicsEngine = (engine.getSceneGraph()).getPhysicsEngine();
		physicsEngine.setGravity(gravity);

		// --- create physics world ---
		float mass = 1.0f;
		float up[ ] = {0,1,0};
		float radius = 0.75f;
		float height = 2.0f;
		double[ ] tempTransform;

		Matrix4f translation = new Matrix4f(plane.getLocalTranslation());
		tempTransform = toDoubleArray(translation.get(vals));
		
		PhysicsObject planeP = (engine.getSceneGraph()).addPhysicsStaticPlane(tempTransform, up, 0.0f);
		
		planeP.setBounciness(1.0f);
		plane.setPhysicsObject(planeP);
		engine.enableGraphicsWorldRender();
		engine.enablePhysicsWorldRender();

		running = true;

		// ----------------- INPUTS SECTION -----------------------------
		im = engine.getInputManager();
		String gpName = im.getFirstGamepadName();
		Camera c = (engine.getRenderSystem()).getViewport("MAIN").getCamera();
		orbitController = new CameraOrbit3D(c, avatar, gpName, engine);

		setupNetworking();

		// build some action objects for doing things in response to user input
		FwdAction fwdAction = new FwdAction(this, protClient);
		TurnAction turnAction = new TurnAction(this, protClient);
		JumpAction jumpAction = new JumpAction(this, protClient);

		// attach the action objects to keyboard and gamepad components
		im.associateActionWithAllGamepads(
			net.java.games.input.Component.Identifier.Button._0,
			jumpAction, InputManager.INPUT_ACTION_TYPE.ON_PRESS_ONLY);
		im.associateActionWithAllGamepads(
			net.java.games.input.Component.Identifier.Button._1,
			fwdAction, InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
		im.associateActionWithAllGamepads(
			net.java.games.input.Component.Identifier.Axis.X,
			turnAction, InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
		im.associateAction(gpName, net.java.games.input.Component.Identifier.Axis.Y,
		 	fwdAction, InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
		im.associateAction(gpName, net.java.games.input.Component.Identifier.Axis.X,
		 	turnAction, InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
			

			 im.associateActionWithAllGamepads(
					net.java.games.input.Component.Identifier.Button._2,
					new AbstractInputAction() {
						public void performAction(float time, net.java.games.input.Event evt) {
							if (!pillHeld && isCloseTo(avatar, pill, 3f) && isCloseTo(avatar, pillBottle, 3f)) {
								attachPillToAvatar();
							} else if (pillHeld) {
								detachAndDropPill();
							}
						}
					},
					InputManager.INPUT_ACTION_TYPE.ON_PRESS_ONLY);

				im.associateActionWithAllGamepads(
					net.java.games.input.Component.Identifier.Button._3,
					new AbstractInputAction() {
						public void performAction(float time, net.java.games.input.Event evt) {
							if (pillHeld) {
								tossPillForward();
							}
						}
					},
					InputManager.INPUT_ACTION_TYPE.ON_PRESS_ONLY);
						
			}
	

	public GameObject getAvatar() { return avatar; }

	public AnimatedShape getAvatarShape() {return avatarS; }



	@Override
	public void update()
	{	elapsedTime = System.currentTimeMillis() - prevTime;
		prevTime = System.currentTimeMillis();
		amt = elapsedTime * 0.03;
		Camera c = (engine.getRenderSystem()).getViewport("MAIN").getCamera();

		Vector3f loc = avatar.getWorldLocation();
		float height = terr.getHeight(loc.x(), loc.z());
		avatar.setLocalLocation(new Vector3f(loc.x(), height, loc.z()));
		

		// build and set HUD
		int elapsTimeSec = Math.round((float)(System.currentTimeMillis()-startTime)/1000.0f);
		String elapsTimeStr = Integer.toString(elapsTimeSec);
		String counterStr = Integer.toString(counter);
		String dispStr1 = "Time = " + elapsTimeStr;
		String dispStr2 = "camera position = "
			+ (c.getLocation()).x()
			+ ", " + (c.getLocation()).y()
			+ ", " + (c.getLocation()).z();
		Vector3f hud1Color = new Vector3f(1,0,0);
		Vector3f hud2Color = new Vector3f(1,1,1);
		(engine.getHUDmanager()).setHUD1(dispStr1, hud1Color, 15, 15);
		(engine.getHUDmanager()).setHUD2(dispStr2, hud2Color, 500, 15);

		avatarS.updateAnimation();

		if (running) {
            AxisAngle4f aa = new AxisAngle4f();
            Matrix4f mat = new Matrix4f();
            Matrix4f mat2 = new Matrix4f().identity();
            Matrix4f mat3 = new Matrix4f().identity();
            checkForCollisions();
            physicsEngine.update((float) elapsedTime);
            for (GameObject go : engine.getSceneGraph().getGameObjects()) {
                if (go.getPhysicsObject() != null) {
                 
                    mat.set(toFloatArray(go.getPhysicsObject().getTransform()));
                    mat2.set(3, 0, mat.m30());
                    mat2.set(3, 1, mat.m31());
                    mat2.set(3, 2, mat.m32());
                    go.setLocalTranslation(mat2);

                    // set rotation
                    mat.getRotation(aa);
                    mat3.rotation(aa);
                    go.setLocalRotation(mat3);
				}
			}
		}

		// update inputs and camera
		im.update((float)elapsedTime);
		orbitController.updateCameraPosition();
		processNetworking((float)elapsedTime);
	}

	@Override
	public void keyPressed(KeyEvent e)
	{	switch (e.getKeyCode())
		{	case KeyEvent.VK_W:
			{	Vector3f oldPosition = avatar.getWorldLocation();
				Vector4f fwdDirection = new Vector4f(0f,0f,1f,1f);
				fwdDirection.mul(avatar.getWorldRotation());
				fwdDirection.mul(0.05f);
				Vector3f newPosition = oldPosition.add(fwdDirection.x(), fwdDirection.y(), fwdDirection.z());
				avatar.setLocalLocation(newPosition);
				Matrix4f lastSentRotation = new Matrix4f();

				protClient.sendMoveMessage(avatar.getWorldLocation());
				Matrix4f currentRotation = avatar.getWorldRotation();
				if (!currentRotation.equals(lastSentRotation)) {
					protClient.sendTurnMessage(currentRotation);
					lastSentRotation.set(currentRotation);
				}
				break;
			}
			case KeyEvent.VK_1:
			{	Matrix4f oldRotation = new Matrix4f(avatar.getWorldRotation());
				Vector4f oldUp = new Vector4f(0f,1f,0f,1f).mul(oldRotation);
				Matrix4f rotAroundAvatarUp = new Matrix4f().rotation(-.01f, new Vector3f(oldUp.x(), oldUp.y(), oldUp.z()));
				Matrix4f newRotation = oldRotation;
				newRotation.mul(rotAroundAvatarUp);
				avatar.setLocalRotation(newRotation);
				protClient.sendTurnMessage(avatar.getWorldRotation());
				break;
			}
			case KeyEvent.VK_2:
			{ 	(engine.getSceneGraph()).setActiveSkyBoxTexture(background);
				(engine.getSceneGraph()).setSkyBoxEnabled(true);
				break;
				}
				case KeyEvent.VK_3:
				{ (engine.getSceneGraph()).setSkyBoxEnabled(false);
				break;
			}
			case KeyEvent.VK_D:
			{
				if(!avatarRendered){
					avatar.getRenderStates().enableRendering();
					avatarRendered = true;
					orbitController.setAvatar(avatar);
				}
				break;
			}
			case KeyEvent.VK_F:
			{
				if(!avatarRendered){
					avatar2.getRenderStates().enableRendering();
					avatarRendered = true;
					orbitController.setAvatar(avatar2);
					avatar = avatar2;
				}
				break;
			}
		}
		super.keyPressed(e);
	}

	// ---------- NETWORKING SECTION ----------------

	public ObjShape getGhostShape() { return ghostS; }
	public TextureImage getGhostTexture() { return ghostT; }
	public GhostManager getGhostManager() { return gm; }
	public Engine getEngine() { return engine; }
	
	private void setupNetworking()
	{	isClientConnected = false;	
		try 
		{	protClient = new ProtocolClient(InetAddress.getByName(serverAddress), serverPort, serverProtocol, this);
		} 	catch (UnknownHostException e) 
		{	e.printStackTrace();
		}	catch (IOException e) 
		{	e.printStackTrace();
		}
		if (protClient == null)
		{	System.out.println("missing protocol host");
		}
		else
		{	// Send the initial join message with a unique identifier for this client
			System.out.println("sending join message to protocol host");
			protClient.sendJoinMessage();
		}
	}
	
	protected void processNetworking(float elapsTime)
	{	// Process packets received by the client from the server
		if (protClient != null)
			protClient.processPackets();
	}

	public Vector3f getPlayerPosition() { return avatar.getWorldLocation(); }

	public void setIsConnected(boolean value) { this.isClientConnected = value; }
	
	private class SendCloseConnectionPacketAction extends AbstractInputAction
	{	@Override
		public void performAction(float time, net.java.games.input.Event evt) 
		{	if(protClient != null && isClientConnected == true)
			{	protClient.sendByeMessage();
			}
		}
	}

	// ------------------ UTILITY FUNCTIONS used by physics
		private float[] toFloatArray(double[] arr)
		{ if (arr == null) return null;
		int n = arr.length;
		float[] ret = new float[n];
		for (int i = 0; i < n; i++)
		{ ret[i] = (float)arr[i];
		}
		return ret;
		}
		private double[] toDoubleArray(float[] arr)
		{ if (arr == null) return null;
		int n = arr.length;
		double[] ret = new double[n];
		for (int i = 0; i < n; i++)
		{ ret[i] = (double)arr[i];
		}
		return ret;
		}

private void checkForCollisions()
{	com.bulletphysics.dynamics.DynamicsWorld dynamicsWorld;
	com.bulletphysics.collision.broadphase.Dispatcher dispatcher;
	com.bulletphysics.collision.narrowphase.PersistentManifold manifold;
	com.bulletphysics.dynamics.RigidBody object1, object2;
	com.bulletphysics.collision.narrowphase.ManifoldPoint contactPoint;

	dynamicsWorld = ((JBulletPhysicsEngine)physicsEngine).getDynamicsWorld();
	dispatcher = dynamicsWorld.getDispatcher();
	int manifoldCount = dispatcher.getNumManifolds();
	for (int i=0; i < manifoldCount; i++)
	{	manifold = dispatcher.getManifoldByIndexInternal(i);
		object1 = (com.bulletphysics.dynamics.RigidBody)manifold.getBody0();
		object2 = (com.bulletphysics.dynamics.RigidBody)manifold.getBody1();
		JBulletPhysicsObject obj1 = JBulletPhysicsObject.getJBulletPhysicsObject(object1);
		JBulletPhysicsObject obj2 = JBulletPhysicsObject.getJBulletPhysicsObject(object2);
		for (int j = 0; j < manifold.getNumContacts(); j++)
		{	contactPoint = manifold.getContactPoint(j);
			if (contactPoint.getDistance() < 0.0f)
			{	System.out.println("---- hit between " + obj1 + " and " + obj2);
				break;
			}
		}
	}
}
private boolean isCloseTo(GameObject a, GameObject b, float distance) {
    return a.getWorldLocation().distance(b.getWorldLocation()) < distance;
}

private void attachPillToAvatar() {
    pill.setParent(avatar);
    pill.setLocalTranslation(new Matrix4f().translation(0.0f, 0.0f, 0.5f));
    pill.setLocalScale(new Matrix4f().scaling(1.2f));

    pill.propagateRotation(true);
    pill.propagateTranslation(true);
    pill.applyParentRotationToPosition(true);

    if (pillP != null) {
        physicsEngine.removeObject(pillP.getUID());
        pill.setPhysicsObject(null);
        pillP = null;
    }

    pillHeld = true;
    System.out.println("Picked up pill from bottle");
}

private void detachAndDropPill() {
    pill.setParent(GameObject.root());

    Vector3f worldPos = pill.getWorldLocation();
    pill.setLocalTranslation(new Matrix4f().translation(worldPos));
    pill.setLocalScale(new Matrix4f().scaling(1.2f));

    double[] tempTransform = toDoubleArray(pill.getLocalTranslation().get(vals));
    int uid = physicsEngine.nextUID();
    pillP = physicsEngine.addSphereObject(uid, 1.0f, tempTransform, 0.8f);
    pillP.setBounciness(0.8f);
    pill.setPhysicsObject(pillP);

    pillHeld = false;
    System.out.println("Dropped pill");
}

private void tossPillForward() {
    Vector3f worldPos = pill.getWorldLocation();
    Vector4f direction = new Vector4f(0f, 0f, 1f, 1f).mul(avatar.getWorldRotation());
    Vector3f tossDir = new Vector3f(direction.x(), direction.y(), direction.z()).normalize().add(0f, 0.3f, 0f);

    pill.setParent(GameObject.root());
    pill.setLocalTranslation(new Matrix4f().translation(worldPos));
    pill.setLocalScale(new Matrix4f().scaling(1.2f));

    if (pillP != null) {
        physicsEngine.removeObject(pillP.getUID());
    }

    double[] tempTransform = toDoubleArray(pill.getLocalTranslation().get(vals));
    int uid = physicsEngine.nextUID();
    pillP = physicsEngine.addSphereObject(uid, 1.0f, tempTransform, 0.8f);
    pillP.setBounciness(0.8f);
    pill.setPhysicsObject(pillP);

    pillP.applyForce(
        tossDir.x() * tossForce,
        tossDir.y() * tossForce,
        tossDir.z() * tossForce,
        0f, 0f, 0f
    );

    pillHeld = false;
    System.out.println("Pill tossed");
}

}