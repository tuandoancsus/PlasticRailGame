import org.joml.*;

public class NPC { 
    double locationX, locationY, locationZ;
    double dir = 0.1;
    double size = 1.0;
    boolean waving = false;
    private long waveStartTime = 0;


    public NPC() { 
        locationX=0.0;
        locationY=0.0;
        locationZ=0.0;
    }

    public double getX() { return locationX; }
    public double getY() { return locationY; }
    public double getZ() { return locationZ; }
    public boolean getWaving() { return waving; }
    public void getBig() { size=2.0; }
    public void getSmall() { size=1.0; }
    public double getSize() { return size; }
    
    public void updateLocation() { 
        if (locationX > 10) dir=-0.1;
        if (locationX < -10) dir=0.1;
        locationX = locationX + dir;
    } 
    public void setPosition(double x, double y, double z) { 
        locationX = x;
        locationY = y;
        locationZ = z;
    }
    public void setWaveAction() {
    waving = !waving;
    System.out.println(waving ? "Started waving" : "Stopped waving");
    if (waving) {
        waveStartTime = System.nanoTime();
    }
    }

    public long getWaveStartTime() {
        return waveStartTime;
    }
}