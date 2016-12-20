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
        System.out.println("> BeamTaliBaja initiated");
        try {
            // Hitung Gaya sumbu y Di Posisi Tali Baja
            setTy();

            // Hitung Gaya sumbu x Di Posisi Tali Baja
            setTx();

            // Hitung Gaya sumbu y di Ax
            setAx();

            // Hitung Gaya sumbu y di Ay
            setAy();


        }catch (Exception e){
            System.out.print(">> Set Ty - By : Failed");
            e.printStackTrace();
        }



            setInnerVerticalForceNodes();
            setInnerHorizontalForceNodes();
            setInnerBendingMomentNodes();


        try{
        setNormalStressNodes();
        setNormalBendingStressNodes();
        setShearStressNodes();

        }catch (Exception e){
            e.printStackTrace();
        }

        setMaxPrincipalStressCMaxNodes();
        setMaxPrincipalStressCZeroNodes();
        setSafetyFactor();

    }

    private void setTy() {
        // ty = -(( R*(0.5 L) + F*x ))/L_ty
        System.out.print(">> setTy() : R = " + mR + " , beam_length = "+mInput.getBeamLength() + " , l_tx = " + mInput.getLTx() + " , force = " + mForce + " , force_position = "+mInput.getForcePosition());

        mTy = new BigDecimal(-1)
                .multiply(
                    mR
                    .multiply(new BigDecimal(0.5))
                    .multiply(mInput.getBeamLength())
                            .add(
                                    mForce
                                    .multiply(mInput.getForcePosition())
                            )
                ).divide(mInput.getLTx(),12,RoundingMode.HALF_EVEN);

        System.out.println(" , ty = "+mTy);
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
        System.out.print(">> setTx() : tx = "+mTx);
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
        try {
            // Inisiasi Class mInnerVerticalForceNodes
            mInnerVerticalForceNodes = new TreeMap<>();

            for (BigDecimal n : mXNodes) {

                // V(n) = Ay - R(n) - ty(n) - F(n)

                mInnerVerticalForceNodes.put(n, mAy
                    .subtract(getROnX(n))
                    .subtract(getTyOnX(n))
                    .subtract(getForceOnX(n)).setScale(12, RoundingMode.HALF_EVEN)
                );


            }
            System.out.println(">> setInnerVerticalForceNodes() : Success");

        }catch(Exception e){
            System.out.print(">> setInnerVerticalForceNodes() : Failed");
            e.printStackTrace();
        }

    }

    private void setInnerHorizontalForceNodes() {
        /**
         * Menghitung Gaya Normal Dalam di Tiap Lokasi Node
         */
        try{
            // Inisiasi Class mInnerVerticalForceNodes
            mInnerHorizontalForceNodes = new TreeMap<>();

            for(BigDecimal n : mXNodes){

                // N(n) = tx(i) - Ax

                    mInnerHorizontalForceNodes.put(n,
                            getTxOnX(n).subtract(mAx).setScale(12, RoundingMode.HALF_EVEN)
                    );


            }
            System.out.print(">> setInnerHorizontalForceNodes() : Success");
        }catch(Exception e){
            System.out.print(">> setInnerHorizontalForceNodes() : Failed");
            e.printStackTrace();
        }

    }

    private void setInnerBendingMomentNodes() {
        /**
         * Menghitung Momen Lentur Dalam di Tiap Lokasi Node
         */
        try{
            // Referensi Class mInnerBendingMomentNodes
            mInnerBendingMomentNodes = new TreeMap<>();
            mInnerBendingMomentNodesO = new TreeMap<>();

            for(BigDecimal n : mXNodes){
                System.out.println("oleleho , n = "+n+" , R(i) = "+getROnX(n)+" , F(i) = "+getForceOnX(n)+" , s ="+mInput.getForcePosition());
                //
                try {
                    mInnerBendingMomentNodes.put(n, mAy
                            .multiply(n)
                            .subtract(
                                    new BigDecimal(0.5)
                                            .multiply(getROnX(n))
                                            .multiply(n)
                            ).subtract(
                                    getForceOnX(n).multiply( n.subtract(mInput.getForcePosition()) )
                            ).subtract(
                                    getTyOnX(n)
                                    .multiply(
                                            n.subtract(mInput.getLTx())
                                    )
                            ).setScale(12, RoundingMode.HALF_EVEN)
                    );
                }catch(Exception e){
                    e.printStackTrace();
                }

                mInnerBendingMomentNodesO.put( n, mInnerBendingMomentNodes.get(n).setScale(3,RoundingMode.HALF_EVEN) );

            }
            System.out.print(">> setInnerBendingMomentNodes() : Success");
        }catch(Exception e){
            System.out.print(">> setInnerBendingMomentNodes() : Failed");
            e.printStackTrace();
        }
    }


}
