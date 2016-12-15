package crane;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.TreeMap;

/**
 * Created by insan on 12/11/2016.
 */
public class BeamTaliBaja extends Beam{
    public BeamTaliBaja(Input input) {

        super(input);


        // Hitung Gaya sumbu y Di Posisi Tali Baja
        setTy();

        // Hitung Gaya sumbu x Di Posisi Tali Baja
        setTx();

        // Hitung Gaya sumbu y di Ax
        setAx();

        // Hitung Gaya sumbu y di Ay
        setAy();

        // Hitung Gaya sumbu x di By
        setBy();






        setInnerVerticalForceNodes();
        setInnerHorizontalForceNodes();
        setInnerBendingMomentNodes();

        setNormalStressNodes();
        setNormalBendingStressNodes();
        setShearStressNodes();

        setMaxPrincipalStressCMaxNodes();
        setMaxPrincipalStressCZeroNodes();
        setSafetyFactor();
    }

    private void setTy() {
        // ty = -(( R*(0.5 L) + F*x ))/L_ty


        mTy = new BigDecimal(-1)
                .multiply(
                    mR
                    .multiply(new BigDecimal(0.5))
                    .multiply(mInput.getBeamLength())
                ).add(
                    mForce
                    .multiply(mInput.getForcePosition())
                ).divide(mInput.getLTy(),12,RoundingMode.HALF_EVEN);
    }

    private BigDecimal getTyOnX(BigDecimal x){

        BigDecimal ty = new BigDecimal(0);

        if(x.compareTo(mInput.getLTx())>=0){
            ty = mTy;
        }

        return ty;
    }

    private void setTx() {
        // tx = L_tx / L_ty * ty
        mTx = mInput.getLTx()
            .divide(mInput.getLTy())
            .multiply(mTy);
    }

    private BigDecimal getTxOnX(BigDecimal x){

        BigDecimal tx = new BigDecimal(0);

        if(x.compareTo(mInput.getLTx())>=0){
            tx = mTx;
        }

        return tx;
    }

    private void setAx() {
        // Ax	= tx
        mAx = mTx;
    }

    private void setAy() {
        // Ay	= R + ty + F
        mAy = mR.add(mTy).add(mForce);
    }


    private void setBy() {

    }

    private void setInnerVerticalForceNodes() {

        /**
         * Menghitung Gaya Geser Dalam di Tiap Lokasi Node
         */

        // Inisiasi Class mInnerVerticalForceNodes
        mInnerVerticalForceNodes = new TreeMap<>();

        for(BigDecimal n : mXNodes){

            // V(n) = Ay - R(n) - ty(n) - F(n)
            try {
                mInnerVerticalForceNodes.put(n, mAy
                        .subtract(mCrossSection.mass_per_metre
                                .multiply(n)
                                .multiply(mGravity)
                        )
                        .subtract(getTyOnX(n))
                        .subtract(getForceOnX(n)).setScale(12, RoundingMode.HALF_EVEN)
                );
            }catch(Exception e){
                e.printStackTrace();
            }

        }

    }

    private void setInnerHorizontalForceNodes() {
        /**
         * Menghitung Gaya Normal Dalam di Tiap Lokasi Node
         */

        // Inisiasi Class mInnerVerticalForceNodes
        mInnerHorizontalForceNodes = new TreeMap<>();

        for(BigDecimal n : mXNodes){

            // N(n) = tx(i) - Ax
            try {
                mInnerVerticalForceNodes.put(n,
                        getTxOnX(n).subtract(mAx).setScale(12, RoundingMode.HALF_EVEN)
                );
            }catch(Exception e){
                e.printStackTrace();
            }

        }
    }

    private void setInnerBendingMomentNodes() {
        /**
         * Menghitung Momen Lentur Dalam di Tiap Lokasi Node
         */

        // Referensi Class mInnerBendingMomentNodes
        mInnerBendingMomentNodes = new TreeMap<>();
        mInnerBendingMomentNodesO = new TreeMap<>();

        for(BigDecimal n : mXNodes){

            //
            try {
                mInnerBendingMomentNodes.put(n, mAy
                        .multiply(n)
                        .subtract(
                                new BigDecimal(0.5)
                                        .multiply(mCrossSection.mass_per_metre)
                                        .multiply(mGravity)
                                        .multiply(n)
                                        .multiply(n)
                        ).subtract(
                                getForceOnX(n).multiply( n.subtract(mInput.getForcePosition()) )
                        ).subtract(
                                getTxOnX(n)
                                .multiply(
                                        n.subtract(mInput.getLTy())
                                )
                        ).setScale(12, RoundingMode.HALF_EVEN)
                );
            }catch(Exception e){
                e.printStackTrace();
            }

            mInnerBendingMomentNodesO.put( n, mInnerBendingMomentNodes.get(n).setScale(3,RoundingMode.HALF_EVEN) );

        }
    }


}
