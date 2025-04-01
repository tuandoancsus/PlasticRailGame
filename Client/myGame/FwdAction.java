package myGame;

import tage.*;
import tage.input.action.AbstractInputAction;
import net.java.games.input.Event;
import org.joml.*;
import java.lang.Math;

public class FwdAction extends AbstractInputAction
{
	private MyGame game;
	private GameObject av;
	private ProtocolClient protClient;


	public FwdAction(MyGame g, ProtocolClient p)
	{	game = g;
		protClient = p;
	}

	@Override
	public void performAction(float time, Event e)
	{	av = game.getAvatar();

		float keyValue = e.getValue();
        if (Math.abs(keyValue) < 0.2) return; // Deadzone check

        keyValue = keyValue * 0.5f;

        av.fwdAction((-1) * keyValue);
		protClient.sendMoveMessage(av.getWorldLocation());
	}
}


