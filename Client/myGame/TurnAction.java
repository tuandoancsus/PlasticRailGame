package myGame;

import tage.*;
import tage.input.action.AbstractInputAction;
import net.java.games.input.Event;
import org.joml.*;
import java.lang.Math;

public class TurnAction extends AbstractInputAction
{
	private MyGame game;
	private GameObject av;
	private ProtocolClient protClient;
	private Matrix4f lastSentRotation = new Matrix4f();


	public TurnAction(MyGame g, ProtocolClient p)
	{	game = g;
		protClient = p;
	}

	@Override
	public void performAction(float time, Event e)
	{	float keyValue = e.getValue();
		av = game.getAvatar();

        if (Math.abs(keyValue) < 0.2) return; // Deadzone check
        av.yawGlobal(keyValue * -0.01f);
		Matrix4f currentRotation = av.getWorldRotation();
		if (!currentRotation.equals(lastSentRotation)) {
			protClient.sendTurnMessage(currentRotation);
			lastSentRotation.set(currentRotation);
		}
	}
}


