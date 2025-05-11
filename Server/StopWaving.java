import tage.ai.behaviortrees.BTAction;
import tage.ai.behaviortrees.BTStatus;

public class StopWaving extends BTAction {
    private NPC npc;

    public StopWaving(NPC n) {
        npc = n;
    }

    @Override
    protected BTStatus update(float elapsedTime) {
        if (npc.getWaving()) {
            npc.setWaveAction(); 
        }
        return BTStatus.BH_SUCCESS;
    }
}
