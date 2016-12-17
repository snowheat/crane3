package crane;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.TreeMap;

/**
 * Created by insan on 12/5/2016.
 */
public class Beam2Penyangga extends Beam {
    public Beam2Penyangga(Input input) {

        super(input);


        System.out.println("> Beam2Penyangga initiated");

        // Hitung Gaya Vertikal di By
        setBy();

        // Hitung Gaya Vertikal di Ay
        setAy();

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

    protected void setBy(){

        // By =
        // ( 0.5 * ( mass_per_metre * beamLength * gravity ) )
        // + ( mass * gravity * forcePosition / beamLength )

        try {
            mBy = new BigDecimal(0.5)
                    .multiply(
                            mR
                    )
                    .add(
                            mInput.getMass()
                                    .multiply(mGravity)
                                    .multiply(mInput.getForcePosition())
                                    .divide(mInput.getBeamLength(), 12, RoundingMode.HALF_EVEN)
                    ).setScale(12, RoundingMode.HALF_EVEN);
        }catch (Exception e){
            e.printStackTrace();
        }


    }

    protected void setAy(){

        // Ay =
        // ( mass_per_metre * beamLength * gravity ) + force - By

        mAy = mCrossSection.mass_per_metre
            .multiply( mInput.getBeamLength() )
            .multiply( mGravity )
            .add( mForce )
            .subtract( mBy ).setScale(12, RoundingMode.HALF_EVEN);
    }

    protected void setInnerVerticalForceNodes() {

        /**
         * Menghitung Gaya Dalam Vertikal di Tiap Lokasi Node
         */

        // Inisiasi Class mInnerVerticalForceNodes
        mInnerVerticalForceNodes = new TreeMap<>();

        for(BigDecimal n : mXNodes){

                //
                try {
                    mInnerVerticalForceNodes.put(n, mAy
                        .subtract(mCrossSection.mass_per_metre
                            .multiply(n)
                            .multiply(mGravity)
                        )
                        .subtract(getForceOnX(n)).setScale(12, RoundingMode.HALF_EVEN)
                    );
                }catch(Exception e){
                    e.printStackTrace();
                }

        }
    }

    protected void setInnerHorizontalForceNodes(){

        /**
         * Menghitung Gaya Dalam Horizontal di Tiap Lokasi Node
         */

        // Referensi Class mInnerHorizontalForceNodes
        mInnerHorizontalForceNodes = new TreeMap<>();

        for(BigDecimal n : mXNodes){


                try {
                    mInnerHorizontalForceNodes.put(n, new BigDecimal(0));
                }catch(Exception e){
                    e.printStackTrace();
                }

        }
    }

    protected void setInnerBendingMomentNodes(){

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
                        ).setScale(12, RoundingMode.HALF_EVEN)
                );
            }catch(Exception e){
                e.printStackTrace();
            }

            mInnerBendingMomentNodesO.put( n, mInnerBendingMomentNodes.get(n).setScale(3,RoundingMode.HALF_EVEN) );

        }
    }
}
