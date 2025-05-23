import java.util.Random;
import tage.ai.behaviortrees.BTCompositeType;
import tage.ai.behaviortrees.BTSelector;
import tage.ai.behaviortrees.BTSequence;
import tage.ai.behaviortrees.BehaviorTree;
import tage.*;
import org.joml.*;

public class NPCcontroller { 
    private NPC npc;
    Random rn = new Random();
    BehaviorTree bt = new BehaviorTree(BTCompositeType.SELECTOR);
    boolean nearFlag = false;
    boolean gameStart = false;
    long thinkStartTime, tickStartTime;
    long lastThinkUpdateTime, lastTickUpdateTime;
    GameAIServerUDP server;
    double criteria = 2.0;

    private float speed    = 2.0f;       // units per second

    public void updateNPCs() {
        //npc.updateLocation();
    }

    public void start(GameAIServerUDP s) {
        thinkStartTime = System.nanoTime();
        tickStartTime = System.nanoTime();
        lastThinkUpdateTime = thinkStartTime;
        lastTickUpdateTime = tickStartTime;
        server = s;
        setupNPCs();
        setupBehaviorTree();
        npcLoop();
    }

    public void setupNPCs() { 
        npc = new NPC();
        System.out.println("NPC created");
    }

    public void npcLoop() { 
        while (true) { 
            long currentTime = System.nanoTime();
            float elapsedThinkMilliSecs =
            (currentTime-lastThinkUpdateTime)/(1000000.0f);
            float elapsedTickMilliSecs =
            (currentTime-lastTickUpdateTime)/(1000000.0f);
            if (elapsedTickMilliSecs >= 25.0f) { 
                lastTickUpdateTime = currentTime;
                //npc.updateLocation();
                server.sendNPCinfo();
            }
            if (elapsedThinkMilliSecs >= 250.0f) { 
                lastThinkUpdateTime = currentTime;
                bt.update(elapsedThinkMilliSecs);
            }
            Thread.yield();
        }
    }

    public void setupBehaviorTree() {
        // bt.insertAtRoot(new BTSequence(10));
        // bt.insert(10, new GetWaving(npc));

        // bt.insertAtRoot(new BTSequence(20));
        // bt.insert(20, new NSecPassed(this, npc, false, 10)); 
        // bt.insert(20, new StopWaving(npc));
    }
    

    public void setNearFlag(boolean flag) {
        this.nearFlag = flag;
    }

    public void setGameStartFlag(boolean flag) {
        this.gameStart = flag;
    }

    public boolean getGameStartFlag() {
        return gameStart;
    }

    public boolean getNearFlag() {
        return nearFlag;
    }

    public NPC getNPC() { 
        return npc; 
    }

    public void setCriteria(double c) { 
        this.criteria = c; 
    }

    public double getCriteria() { 
        return criteria; 
    }

    public float getSpeed() { 
        return speed; 
    }    
}