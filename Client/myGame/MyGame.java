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

public class MyGame extends VariableFrameRateGame
{
	private static Engine engine;
	private InputManager im;
	private GhostManager gm;

	private int counter=0;
	private Vector3f currentPosition;
	private Matrix4f initialTranslation, initialRotation, initialScale;
	private double startTime, prevTime, elapsedTime, amt;

	private GameObject avatar, x, y, z, pillBottle, terr;
	private ObjShape avatarS, ghostS, linxS, linyS, linzS, pillBottleS,  terrS;
	private TextureImage avatarT, ghostT, pillT, hills, grass, floor;
	private int lakeIslands, background; // skyboxes
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
		avatarS = new ImportedModel("avatar1.obj");
		pillBottleS = new ImportedModel("PillBottle.obj");
		linxS = new Line(new Vector3f(0f,0f,0f), new Vector3f(3f,0f,0f));
		linyS = new Line(new Vector3f(0f,0f,0f), new Vector3f(0f,3f,0f));
		linzS = new Line(new Vector3f(0f,0f,0f), new Vector3f(0f,0f,-3f));

		terrS = new TerrainPlane(1000); // pixels per axis = 1000x1000

		
	}

	@Override
	public void loadTextures()
	{	avatarT = new TextureImage("avatar1Texture.png");
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
		initialTranslation = (new Matrix4f()).translation(-1f,1,1f);
		avatar.setLocalTranslation(initialTranslation);
		initialRotation = (new Matrix4f()).rotationY((float)java.lang.Math.toRadians(135.0f));
		initialScale = (new Matrix4f()).scaling(0.25f);
		avatar.setLocalScale(initialScale);
		avatar.setLocalRotation(initialRotation);
		//avatar.getRenderStates().setModelOrientationCorrection((new Matrix4f()).rotationY((float)java.lang.Math.toRadians(180.0f)));


		// build torus along X axis
		pillBottle = new GameObject(GameObject.root(), pillBottleS, pillT);
		initialTranslation = (new Matrix4f()).translation(10,0,-10);
		pillBottle.setLocalTranslation(initialTranslation);
		initialScale = (new Matrix4f()).scaling(5.0f);
		initialRotation = (new Matrix4f()).rotationY((float)java.lang.Math.toRadians(360.0f));
		pillBottle.setLocalScale(initialScale);
		pillBottle.setLocalRotation(initialRotation);

		// build terrain
		terr = new GameObject(GameObject.root(), terrS, floor);
		initialTranslation = (new Matrix4f()).translation(0f,0f,0f);
		terr.setLocalTranslation(initialTranslation);
		initialScale = (new Matrix4f()).scaling(20.0f, 1.0f, 20.0f);
		terr.setLocalScale(initialScale);
		terr.setHeightMap(hills);

		terr.getRenderStates().setTiling(1);
		terr.getRenderStates().setTileFactor(10);

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

		// ----------------- INPUTS SECTION -----------------------------
		im = engine.getInputManager();
		String gpName = im.getFirstGamepadName();
		Camera c = (engine.getRenderSystem()).getViewport("MAIN").getCamera();
		orbitController = new CameraOrbit3D(c, avatar, gpName, engine);

		setupNetworking();

		// build some action objects for doing things in response to user input
		FwdAction fwdAction = new FwdAction(this, protClient);
		TurnAction turnAction = new TurnAction(this, protClient);

		// attach the action objects to keyboard and gamepad components
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
	}

	public GameObject getAvatar() { return avatar; }

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
			case KeyEvent.VK_D:
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
				{ (engine.getSceneGraph()).setActiveSkyBoxTexture(background);
				(engine.getSceneGraph()).setSkyBoxEnabled(true);
				break;
				}
				case KeyEvent.VK_3:
				{ (engine.getSceneGraph()).setSkyBoxEnabled(false);
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
}