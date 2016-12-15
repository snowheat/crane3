package crane;

import java.math.BigDecimal;

/**
 * Created by insan on 12/5/2016.
 */
public class Input {
    private BigDecimal beamLength, forcePosition, mass, lTx, lTy, massPerLength = null;
    private Integer materialID, crossSectionID, simulationID;

    public BigDecimal getMassPerLength() { return massPerLength; }

    public void setMassPerLength(String massPerLength) {
        try {
            this.massPerLength = new BigDecimal(massPerLength);
        } catch (Exception e) {
            this.massPerLength = null;
        }
    }

    public BigDecimal getBeamLength() {
        return beamLength;
    }
    public void setBeamLength(String beamLength) {
        try {
            this.beamLength = new BigDecimal(beamLength);
        } catch (Exception e) {
            this.beamLength = null;
        }
    }

    public BigDecimal getForcePosition() {
        return forcePosition;
    }
    public void setForcePosition(String forcePosition) {
        try{
            this.forcePosition = new BigDecimal(forcePosition);
        }catch(Exception e){
            this.forcePosition = null;
        }
    }

    public BigDecimal getMass() {
        return mass;
    }
    public void setMass(String mass) {
        try{
            this.mass = new BigDecimal(mass);
        }catch(Exception e){
            this.mass = null;
        }
    }

    public int getMaterialID() {
        return materialID;
    }
    public void setMaterialID(String materialID) {
        try{
            this.materialID = Integer.parseInt(materialID);
        }catch(Exception e){
            this.materialID = null;
        }
    }

    public int getCrossSectionID() {
        return crossSectionID;
    }
    public void setCrossSectionID(String crossSectionID) {
        try{
            this.crossSectionID = Integer.parseInt(crossSectionID);
        }catch(Exception e){
            this.crossSectionID = null;
        }
    }

    public int getSimulationID() {
        return simulationID;
    }
    public void setSimulationID(String simulationID) {
        try{
            this.simulationID = Integer.parseInt(simulationID);
        }catch(Exception e){
            this.simulationID = null;
        }
    }

    public BigDecimal getLTx() {
        return lTx;
    }
    public void setLTx(String tx) {
        try{
            this.lTx = new BigDecimal(tx);
        }catch(Exception e){
            this.lTx = null;
        }
    }

    public BigDecimal getLTy() {
        return lTy;
    }
    public void setLTy(String ty) {
        try{
            this.lTy = new BigDecimal(ty);
        }catch(Exception e){
            this.lTy = null;
        }

    }
}
