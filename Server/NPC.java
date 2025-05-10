import org.joml.*;

public class NPC { 
    double locationX, locationY, locationZ;
    double dir = 0.1;
    double size = 1.0;
    boolean waving = false;

    public NPC() { 
        locationX=0.0;
        locationY=0.0;
        locationZ=0.0;
    }
    
    public void randomizeLocation(int seedX, int seedZ) { 
        locationX = ((double)seedX)/4.0 - 5.0;
        locationY = 0;
        locationZ = -2;
    }

    public double getX() { return locationX; }
    public double getY() { return locationY; }
    public double getZ() { return locationZ; }
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
        System.out.println("Waving");
        waving = !waving;
    }
}