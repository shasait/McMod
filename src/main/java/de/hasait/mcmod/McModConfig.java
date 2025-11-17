package de.hasait.mcmod;

public class McModConfig {

    private float repairGolemStartHealthPercentage = 75.0F;
    private float repairGolemHealthStepPercentage = 5.0f;

    private double repairGolemScanX = 15.00;
    private double repairGolemScanY = 5.00;
    private double repairGolemScanZ = 15.00;

    public float getRepairGolemStartHealthPercentage() {
        return repairGolemStartHealthPercentage;
    }

    public void setRepairGolemStartHealthPercentage(float repairGolemStartHealthPercentage) {
        this.repairGolemStartHealthPercentage = repairGolemStartHealthPercentage;
    }

    public float getRepairGolemHealthStepPercentage() {
        return repairGolemHealthStepPercentage;
    }

    public void setRepairGolemHealthStepPercentage(float repairGolemHealthStepPercentage) {
        this.repairGolemHealthStepPercentage = repairGolemHealthStepPercentage;
    }

    public double getRepairGolemScanX() {
        return repairGolemScanX;
    }

    public void setRepairGolemScanX(double repairGolemScanX) {
        this.repairGolemScanX = repairGolemScanX;
    }

    public double getRepairGolemScanY() {
        return repairGolemScanY;
    }

    public void setRepairGolemScanY(double repairGolemScanY) {
        this.repairGolemScanY = repairGolemScanY;
    }

    public double getRepairGolemScanZ() {
        return repairGolemScanZ;
    }

    public void setRepairGolemScanZ(double repairGolemScanZ) {
        this.repairGolemScanZ = repairGolemScanZ;
    }

}
