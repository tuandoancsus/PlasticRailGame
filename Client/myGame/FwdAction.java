package myGame;

import tage.*;
import tage.input.action.AbstractInputAction;
import tage.shapes.AnimatedShape;
import net.java.games.input.Event;
import org.joml.*;
import java.lang.Math;

public class FwdAction extends AbstractInputAction
{
	private MyGame game;
	private GameObject av;
	private ProtocolClient protClient;
	private boolean isRunningAnimationPlaying = false;
	private long lastForwardTime = 0;
	private boolean isSprintingAnimationPlaying = false;


	public FwdAction(MyGame g, ProtocolClient p)
	{	game = g;
		protClient = p;
		
	}

	@Override
public void performAction(float time, Event e)
{
	av = game.getAvatar();
	float keyValue = e.getValue();

	if (Math.abs(keyValue) < 0.2) {
		if (isRunningAnimationPlaying) {
			game.getAvatarShape().stopAnimation();
			game.getAvatarShape().playAnimation("IDLE", 0.1f, AnimatedShape.EndType.LOOP, 0);
			isRunningAnimationPlaying = false;
			isSprintingAnimationPlaying = false;
		}
		return;
	}

	keyValue = keyValue * 0.5f;
	av.fwdAction((-1) * keyValue);
	protClient.sendMoveMessage(av.getWorldLocation());

	long currentTime = System.currentTimeMillis();

	if (!isRunningAnimationPlaying) {
		if (currentTime - lastForwardTime < 300) {
			game.getAvatarShape().playAnimation("RUN", 0.7f, AnimatedShape.EndType.LOOP, 0);
			isSprintingAnimationPlaying = true;
		} else {
			game.getAvatarShape().playAnimation("WALK", 0.5f, AnimatedShape.EndType.LOOP, 0);
			isSprintingAnimationPlaying = false;
		}
		isRunningAnimationPlaying = true;
	}

	lastForwardTime = currentTime;
}
}

