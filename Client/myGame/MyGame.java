package myGame;

import tage.*;
import tage.shapes.*;
import tage.input.*;
import tage.input.action.*;
import tage.audio.*;

import java.lang.Math;

import java.awt.*;

import java.awt.event.*;

import java.io.*;
import java.util.*;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
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
	//score
	private int score = 0;
	//lights
	private Light blueSpotlight, greenSpotlight, redSpotlight;
	private boolean switchedLights = false;

	//physic
	private PhysicsEngine physicsEngine;
	private PhysicsObject planeP;
	private boolean running = false;
	private float vals[] = new float[16];
	private boolean pillStoppedBouncing = false;


	//pill
	private boolean bottleHeld = false;
	private PhysicsObject pillP, bottleP;
	private float tossForce = 525.0f;

	private static Engine engine;
	private InputManager im;
	private GhostManager gm;

	private int counter=0;
	private Vector3f currentPosition;
	private Matrix4f initialTranslation, initialRotation, initialScale;
	private double startTime, prevTime, elapsedTime, amt;

	private GameObject avatar, avatar2, x, y, z, pillBottle, terr, plane,pill, virusRoot;
	private AnimatedShape avatarS;
	private ObjShape avatar2S, virusS, ghostS, linxS, linyS, linzS, pillBottleS, terrS, planeS, pillS;
	private VirusFactory viruses;
	private TextureImage avatarT, avatar2T, virusTex, ghostT, hills, grass, floor, pillT;

	private int background; // skyboxes
	private boolean avatarRendered = false;
	private Light light;
	private CameraOrbit3D orbitController;

	private IAudioManager audioManager;
	private Sound backgroundMusic, collisionSound, soundEffect2;

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
		avatarS = new AnimatedShape("test2.rkm", "test2.rks");
		avatarS.loadAnimation("WALK","walkModel.rka");
		avatarS.loadAnimation("IDLE","idle.rka");
		avatarS.loadAnimation("RUN","sprint.rka");
		avatarS.loadAnimation("JUMP","jump3.rka");

		avatar2S = new ImportedModel("finalModel3.obj");
		//virusS = new ImportedModel("avatar1.obj");
		pillBottleS = new ImportedModel("PillBottle.obj");
		pillS = new ImportedModel("pill.obj");
		// linxS = new Line(new Vector3f(0f,0f,0f), new Vector3f(3f,0f,0f));
		// linyS = new Line(new Vector3f(0f,0f,0f), new Vector3f(0f,3f,0f));
		// linzS = new Line(new Vector3f(0f,0f,0f), new Vector3f(0f,0f,-3f));

		terrS = new TerrainPlane(1000); // pixels per axis = 1000x1000

		planeS = new Plane();
		
	}

	@Override
	public void loadTextures()
	{	avatarT = new TextureImage("spiderman.png");
		virusTex = new TextureImage("spidermoon.png");
		ghostT = new TextureImage("redDolphin.jpg");
		pillT = new TextureImage("pillbottle.png");


		hills = new TextureImage("hills2.png");
		floor = new TextureImage("floor.jpg");
	}


	@Override
	public void loadSounds()
	{	AudioResource resource1, resource2, resource3;
		audioManager = engine.getAudioManager();
		resource1 = audioManager.createAudioResource("BGMusic.wav", AudioResourceType.AUDIO_STREAM);
		resource2 = audioManager.createAudioResource("collision.wav", AudioResourceType.AUDIO_SAMPLE);
		//resource3 = audioManager.createAudioResource("soundEffect2.wav", AudioResourceType.AUDIO_SAMPLE);

		backgroundMusic = new Sound(resource1, SoundType.SOUND_MUSIC, 5, true);
		collisionSound = new Sound(resource2, SoundType.SOUND_EFFECT, 100, true);
		// soundEffect2 = new Sound(resource3, SoundType.SOUND_EFFECT, 100, false);

		backgroundMusic.initialize(audioManager);
		collisionSound.initialize(audioManager);
		collisionSound.setMaxDistance(50.0f);
		collisionSound.setMinDistance(0.5f);
		collisionSound.setRollOff(1.0f);
		// soundEffect2.initialize(audioManager);
	}


	@Override
	public void buildObjects()
	{	Matrix4f initialTranslation, initialRotation, initialScale;

		// build avatar
		// avatar = new GameObject(GameObject.root(), avatarS, avatarT);
		// initialTranslation = (new Matrix4f()).translation(-1f,0f,1f);
		// avatar.setLocalTranslation(initialTranslation);
		// initialRotation = (new Matrix4f()).rotationY((float)java.lang.Math.toRadians(135.0f));
		// initialScale = (new Matrix4f()).scaling(0.25f);
		// avatar.setLocalScale(initialScale);
		// avatar.setLocalRotation(initialRotation);
		//avatar.getRenderStates().disableRendering();
		//avatar.getRenderStates().setModelOrientationCorrection((new Matrix4f()).rotationY((float)java.lang.Math.toRadians(180.0f)));

		avatar = new GameObject(GameObject.root(), avatarS, avatarT);
		initialTranslation = (new Matrix4f()).translation(-1f,2,1f);
		avatar.setLocalTranslation(initialTranslation);
		initialRotation = (new Matrix4f()).rotationY((float)java.lang.Math.toRadians(135.0f));
		initialScale = (new Matrix4f()).scaling(0.70f);
		avatar.setLocalScale(initialScale);
		avatar.setLocalRotation(initialRotation);
		avatarS.playAnimation("IDLE", 0.1f, AnimatedShape.EndType.LOOP, 0);

		//avatar.getRenderStates().disableRendering();
		//avatar.getRenderStates().setModelOrientationCorrection((new Matrix4f()).rotationY((float)java.lang.Math.toRadians(180.0f)));

		avatar2 = new GameObject(GameObject.root(), avatar2S, avatar2T);
		initialTranslation = (new Matrix4f()).translation(-1f,2,12f);
		avatar2.setLocalTranslation(initialTranslation);
		initialRotation = (new Matrix4f()).rotationY((float)java.lang.Math.toRadians(135.0f));
		initialScale = (new Matrix4f()).scaling(0.25f);
		avatar2.setLocalScale(initialScale);
		avatar2.setLocalRotation(initialRotation);
		avatar2.getRenderStates().disableRendering();


		// build Pill Bottle building
		pillBottle = new GameObject(GameObject.root(), pillBottleS, pillT);
		initialTranslation = (new Matrix4f()).translation(10,0,0);
		pillBottle.setLocalTranslation(initialTranslation);
		initialScale = (new Matrix4f()).scaling(1.0f);
		initialRotation = (new Matrix4f()).rotationY((float)java.lang.Math.toRadians(360.0f));
		pillBottle.setLocalScale(initialScale);
		pillBottle.setLocalRotation(initialRotation);
		pill = new GameObject(GameObject.root(), pillS, pillT);
		pill.setLocalTranslation(new Matrix4f().translation(10, 0, 8)); 
		double[] tempTransform = toDoubleArray(pill.getLocalTranslation().get(vals));
		pill.setLocalScale(new Matrix4f().scaling(0.5f));

		

		
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

		Matrix4f terrainScaleMatrix = terr.getLocalScale();
		Vector3f terrainScale = new Vector3f();
		terrainScaleMatrix.getScale(terrainScale);

		Vector3f terrainPos = terr.getWorldLocation();

		plane = new GameObject(GameObject.root(), planeS, grass);
		plane.setLocalTranslation(new Matrix4f().translation(terrainPos.x(), terrainPos.y(), terrainPos.z()));
		plane.setLocalScale(new Matrix4f().scaling(terrainScale));
		
		// add X,Y,-Z axes
		// x = new GameObject(GameObject.root(), linxS);
		// y = new GameObject(GameObject.root(), linyS);
		// z = new GameObject(GameObject.root(), linzS);
		// (x.getRenderStates()).setColor(new Vector3f(1f,0f,0f));
		// (y.getRenderStates()).setColor(new Vector3f(0f,1f,0f));
		// (z.getRenderStates()).setColor(new Vector3f(0f,0f,1f));

		virusRoot = new GameObject(GameObject.root());
		viruses = new VirusFactory(avatar2S, virusTex, virusRoot);
		
	}

	static float randf(float min, float max) {
    	return ThreadLocalRandom.current().nextFloat() * (max - min) + min;
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

		Matrix4f pillTransform = pill.getLocalTranslation();
		Matrix4f bottleTransform = pillBottle.getLocalTranslation();

		tempTransform = toDoubleArray(pillTransform.get(vals));
		
		
		planeP = (engine.getSceneGraph()).addPhysicsStaticPlane(tempTransform, new float[]{0,1,0}, 0.0f);
		planeP.setBounciness(1.0f);
		plane.setPhysicsObject(planeP);
		//engine.enableGraphicsWorldRender();
		//engine.enablePhysicsWorldRender();

		running = true;

		int uid = physicsEngine.nextUID();
		pillP = physicsEngine.addSphereObject(uid, 1.0f, tempTransform, 1.0f); // dynamic
		pillP.setBounciness(0.95f); // nearly elastic collision
		pill.setPhysicsObject(pillP);

		// Apply initial upward velocity
		pillP.setLinearVelocity(new float[]{0f, 2f, 0f}); 

		double[] bottleTransformVals = toDoubleArray(bottleTransform.get(vals));
		int bottleUID = physicsEngine.nextUID();
		bottleP = physicsEngine.addBoxObject(bottleUID, 0f, bottleTransformVals, new float[]{1f, 5f, 1f});
		bottleP.setBounciness(0.3f); // optional, allows slight bounce
		pillBottle.setPhysicsObject(bottleP);

		// ----------------- SOUNDS SECTION ----------------
		backgroundMusic.setLocation(avatar.getWorldLocation());
		collisionSound.setLocation(pillBottle.getWorldLocation());
		setEarParameters();
		backgroundMusic.play();
		collisionSound.play();


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
							if (!bottleHeld && isCloseTo(avatar, pill, 3f) && isCloseTo(avatar, pill, 3f)) {
								attachPillToAvatar();
							} else if (bottleHeld) {
								detachAndDropPill();
							}
						}
					},
					InputManager.INPUT_ACTION_TYPE.ON_PRESS_ONLY);

				im.associateActionWithAllGamepads(
					net.java.games.input.Component.Identifier.Button._3,
					new AbstractInputAction() {
						public void performAction(float time, net.java.games.input.Event evt) {
							if (bottleHeld) {
								tossPillForward();
							}
						}
					},
					InputManager.INPUT_ACTION_TYPE.ON_PRESS_ONLY);
					
				// Red spotlight
		redSpotlight = new Light();
		redSpotlight.setType(Light.LightType.SPOTLIGHT);
		redSpotlight.setLocation(new Vector3f(10f, 5f, 8f)); 
		redSpotlight.setDirection(new Vector3f(0f, -1f, 0f)); // shine downward
		redSpotlight.setCutoffAngle(30f);
		redSpotlight.setDiffuse(1f, 0f, 0f);  // Red
		redSpotlight.setAmbient(0.3f, 0f, 0f);
		redSpotlight.setSpecular(1f, 0.2f, 0.2f);
		engine.getSceneGraph().addLight(redSpotlight);

			// Blue spotlight 
		blueSpotlight = new Light();
		blueSpotlight.setType(Light.LightType.SPOTLIGHT);
		blueSpotlight.setLocation(new Vector3f(10f, 5f, -8f)); // above pill
		blueSpotlight.setDirection(new Vector3f(0f, -1f, 0f));  // point downward
		blueSpotlight.setCutoffAngle(30f);
		blueSpotlight.setDiffuse(1f, 0f,0f);  // Blue
		blueSpotlight.setAmbient(0f, 0f, 0.5f);
		blueSpotlight.setSpecular(0.5f, 0.5f, 1f);
		engine.getSceneGraph().addLight(blueSpotlight);

		// Green spotlight on pill bottle
		greenSpotlight = new Light();
		greenSpotlight.setType(Light.LightType.SPOTLIGHT);
		greenSpotlight.setLocation(new Vector3f(10f, 10f, 0f)); // above pill bottle
		greenSpotlight.setDirection(new Vector3f(0f, -1f, 0f)); // point downward
		greenSpotlight.setCutoffAngle(30f);
		greenSpotlight.setDiffuse(0.6f, 0f, 0.8f);  // Purple tint (mix of red + blue)
		greenSpotlight.setAmbient(0.3f, 0f, 0.4f);  // Slight ambient purple
		greenSpotlight.setSpecular(0.8f, 0.3f, 1f); // Shiny purple highlight
				
		greenSpotlight.disable(); // Start off
		engine.getSceneGraph().addLight(greenSpotlight);

			}
	

	public GameObject getAvatar() { return avatar; }

	public AnimatedShape getAvatarShape() {return avatarS; }



	public void setEarParameters()
	{	Camera camera = (engine.getRenderSystem()).getViewport("MAIN").getCamera();
		audioManager.getEar().setLocation(avatar.getWorldLocation());
		audioManager.getEar().setOrientation(camera.getN(), new Vector3f(0.0f, 1.0f, 0.0f));
	}

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
		String dispStr1 = "Time = " + elapsTimeStr + " | Score = " + score;
		if (bottleHeld) {
			dispStr1 += " | Pill in hand";
		} else {
			dispStr1 += " | Pill on ground";
		}

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

	

		if (!switchedLights && (System.currentTimeMillis() - startTime) > 20000) {
			blueSpotlight.disable();
			redSpotlight.disable();
			greenSpotlight.enable();
			switchedLights = true;
		}
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
			case KeyEvent.VK_E:
			{
				// spawn random viruses
				// Vector3f p = new Vector3f(randf(-10,10), 0, randf(-10,10));
				// viruses.spawn(p, 0.5f, randf(0,360));
				// System.out.println("spawning virus at " + p.x() + ", " + p.y() + ", " + p.z());
				protClient.sendNeedNPCMessage();
				counter++;
				break;
			}
			case KeyEvent.VK_F:
			{
				// if(!avatarRendered){
				// 	avatar2.getRenderStates().enableRendering();
				// 	avatarRendered = true;
				// 	orbitController.setAvatar(avatar2);
				// 	avatar = avatar2;
				// }
				break;
			}
			case KeyEvent.VK_P:
			{
				backgroundMusic.togglePause();
			}
		}
		super.keyPressed(e);
	}

// -------------------- NETWORKING SECTION --------------------

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
			protClient.sendNeedNPCMessage();
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

// ------------------ UTILITY FUNCTIONS used by physics ------------------

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

			if ((obj1 == pillP && obj2 == bottleP) || (obj1 == bottleP && obj2 == pillP)) {
       			System.out.println("Score! Pill hit the pill bottle!");
				score++;
		        System.out.println("Current Score: " + score);

    		}
			break;

			}
		}
	}
}
private boolean isCloseTo(GameObject a, GameObject b, float distance) {
    Vector3f posA = a.getWorldLocation();
    Vector3f posB = b.getWorldLocation();
    Vector2f a2D = new Vector2f(posA.x(), posA.z());
    Vector2f b2D = new Vector2f(posB.x(), posB.z());
    return a2D.distance(b2D) < distance;
}

private void attachPillToAvatar() {
    pill.setParent(avatar);
    pill.setLocalTranslation(new Matrix4f().translation(0.0f, 0.0f, 0.5f));
    pill.setLocalScale(new Matrix4f().scaling(0.5f));

    pill.propagateRotation(true);
    pill.propagateTranslation(true);
    pill.applyParentRotationToPosition(true);

    if (pillP != null) {
        physicsEngine.removeObject(pillP.getUID());
        pill.setPhysicsObject(null);
        pillP = null;
    }

    bottleHeld = true;
    System.out.println("Picked up pill");
}

private void detachAndDropPill() {
    pill.setParent(GameObject.root());

		Vector3f avatarPos = avatar.getWorldLocation();
		Vector3f dropPos = new Vector3f(avatarPos.x(), avatarPos.y() + 1.0f, avatarPos.z());

    pill.setLocalTranslation(new Matrix4f().translation(dropPos));
    pill.setLocalScale(new Matrix4f().scaling(0.5f));

    double[] tempTransform = toDoubleArray(pill.getLocalTranslation().get(vals));
    int uid = physicsEngine.nextUID();
    pillP = physicsEngine.addSphereObject(uid, 1.0f, tempTransform, 0.8f);
    pillP.setBounciness(0.8f);
    pill.setPhysicsObject(pillP);

    bottleHeld = false;
    System.out.println("Dropped pill");
}

private void tossPillForward() {
    Vector3f worldPos = pill.getWorldLocation();
    Vector4f direction = new Vector4f(0f, 0f, 1f, 1f).mul(avatar.getWorldRotation());
    Vector3f tossDir = new Vector3f(direction.x(), direction.y(), direction.z()).normalize().add(0f, 0.3f, 0f);

    pill.setParent(GameObject.root());
    Vector3f startPos = new Vector3f(worldPos).add(tossDir.mul(1.0f));
    pill.setLocalTranslation(new Matrix4f().translation(startPos));
    pill.setLocalScale(new Matrix4f().scaling(0.5f));

    if (pillP != null) {
        physicsEngine.removeObject(pillP.getUID());
    }

    double[] tempTransform = toDoubleArray(pill.getLocalTranslation().get(vals));
    int uid = physicsEngine.nextUID();
    pillP = physicsEngine.addSphereObject(uid, 1.0f, tempTransform, 0.8f);
    pillP.setBounciness(0.8f);
    pill.setPhysicsObject(pillP);

    float force = 700.0f;
    pillP.applyForce(
        tossDir.x() * force,
        tossDir.y() * force,
        tossDir.z() * force,
        0f, 0f, 0f
    );

		bottleHeld = false;
		System.out.println("Pill tossed");
		
	}	

	public ObjShape getNPCshape() { return avatar2S; }
	public TextureImage getNPCtexture() { return virusTex; }
}