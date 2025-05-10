import tage.ai.behaviortrees.BTAction;
import tage.ai.behaviortrees.BTStatus;

public class AvatarWave extends BTAction {
    private NPC npc;
    
    public AvatarWave(NPC n) {
        npc = n;
    }
    
    @Override
    protected BTStatus update(float elapsedTime) {
        npc.setWaveAction();
        return BTStatus.BH_SUCCESS;
    }
}