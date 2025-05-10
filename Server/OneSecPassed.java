import tage.ai.behaviortrees.BTCondition;

public class OneSecPassed extends BTCondition {

    private NPCcontroller npcc;
    private NPC npc;
    private long lastUpdateTime;

    public OneSecPassed(NPCcontroller c, NPC n, boolean toNegate) {
        super(toNegate);
        npcc = c;
        npc = n;
        lastUpdateTime = System.nanoTime();
    }

    protected boolean check() {
        long currentTime = System.nanoTime();
        float elapsedMilliSecs = (currentTime - lastUpdateTime) / (1000000.0f);

        if (elapsedMilliSecs >= 1000.0f) {  // 1 second
            lastUpdateTime = currentTime;
            return true;
        }
        return false;
    }
}