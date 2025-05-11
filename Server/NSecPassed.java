import tage.ai.behaviortrees.BTCondition;

public class NSecPassed extends BTCondition {
    private NPC npc;
    private float seconds;

    public NSecPassed(NPCcontroller c, NPC n, boolean toNegate, float seconds) {
        super(toNegate);
        npc = n;
        this.seconds = seconds;
    }

    @Override
    protected boolean check() {
        if (!npc.getWaving()) return false;

        long now = System.nanoTime();
        float elapsedMillis = (now - npc.getWaveStartTime()) / 1_000_000.0f;
        return elapsedMillis >= (seconds * 1000);
    }
}
