import tage.ai.behaviortrees.BTAction;
import tage.ai.behaviortrees.BTStatus;

public class GetSmall extends BTAction {
    private NPC npc;
    
    public GetSmall(NPC n) {
        npc = n;
    }
    
    @Override
    protected BTStatus update(float elapsedTime) {
        npc.getSmall();
        return BTStatus.BH_SUCCESS;
    }
}