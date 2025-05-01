package myGame;

import tage.*;
import tage.input.action.AbstractInputAction;
import tage.shapes.AnimatedShape;
import net.java.games.input.Event;
import org.joml.*;

public class JumpAction extends AbstractInputAction
{
	private MyGame game;
	private GameObject av;
	private ProtocolClient protClient;
	private boolean isJumping = false;

	public JumpAction(MyGame g, ProtocolClient p)
	{
		game = g;
		protClient = p;
	}

	@Override
	public void performAction(float time, Event e)
	{
		av = game.getAvatar();

		if (!isJumping) {
			game.getAvatarShape().stopAnimation();
			game.getAvatarShape().playAnimation("JUMP", 0.5f, AnimatedShape.EndType.STOP, 0);
			isJumping = true;
			new java.util.Timer().schedule(
				new java.util.TimerTask() {
					@Override
					public void run() {
						game.getAvatarShape().playAnimation("IDLE", 0.1f, AnimatedShape.EndType.LOOP, 0);
						isJumping = false;
					}
				},
				1000
			);
		}
	}
}
