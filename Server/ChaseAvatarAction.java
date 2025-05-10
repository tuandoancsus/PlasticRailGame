import tage.ai.behaviortrees.BTAction;
import tage.ai.behaviortrees.BTStatus;
import org.joml.Vector3f;
import java.util.Random;

/**
 * A BehaviorTree leaf that moves the NPC toward the avatar each tick.
 */
public class ChaseAvatarAction extends BTAction {
    private NPCcontroller controller;
    private NPC          npc;
    private GameAIServerUDP server;
    private Random       rnd = new Random();

    public ChaseAvatarAction(GameAIServerUDP s, NPCcontroller ctrl, NPC npc) {
        this.controller = ctrl;
        this.npc        = npc;
        this.server     = s;
    }

    @Override
    public BTStatus update(float elapsedTime) {
        float dt = elapsedTime / 1000f;
        Vector3f target = server.getAvatarPos();

        Vector3f me = new Vector3f(
            (float)npc.getX(),
            (float)npc.getY(),
            (float)npc.getZ()
        );

        Vector3f dir = new Vector3f();
        target.sub(me, dir);
        if (dir.lengthSquared() < 0.0001f) {
            // crash & pick a new heading
            dir.set(rnd.nextFloat()-0.5f, 0, rnd.nextFloat()-0.5f);
        }
        dir.normalize();

        float distance = controller.getSpeed() * dt;
        Vector3f newPos = me.add(dir.mul(distance, new Vector3f()));
        npc.setPosition(newPos.x, newPos.y, newPos.z);

        return BTStatus.BH_SUCCESS;
    }
}
