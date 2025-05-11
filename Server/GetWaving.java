import tage.ai.behaviortrees.BTAction;
import tage.ai.behaviortrees.BTStatus;

public class GetWaving extends BTAction {
    private NPC npc;
    
    public GetWaving(NPC n) {
        npc = n;
    }
    
    @Override
    protected BTStatus update(float elapsedTime) {
        if (!npc.getWaving()) {
        npc.setWaveAction(); // Only toggle if not already waving
        }
        return BTStatus.BH_SUCCESS;
    }
}