import tage.ai.behaviortrees.BTAction;
import tage.ai.behaviortrees.BTStatus;

public class GetBig extends BTAction {
    private NPC npc;
    
    public GetBig(NPC n) {
        npc = n;
    }
    
    @Override
    protected BTStatus update(float elapsedTime) {
        npc.getBig();
        return BTStatus.BH_SUCCESS;
    }
}