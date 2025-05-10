import tage.ai.behaviortrees.BTAction;
import tage.ai.behaviortrees.BTStatus;

public class SpawnAction extends BTAction {

    NPC npc;
    NPCcontroller npcc;
    GameAIServerUDP server;

    public SpawnAction(GameAIServerUDP s, NPCcontroller c, NPC n, boolean toNegate) {
        server = s;
        npcc = c;
        npc = n;
    }

    @Override
    protected BTStatus update(float elapsedTime) {
        npcc.setupNPCs(); // Spawn and randoimize the NPC's location
        return BTStatus.BH_SUCCESS;
    }
}