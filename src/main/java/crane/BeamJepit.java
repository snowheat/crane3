package crane;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.TreeMap;

/**
 * Created by insan on 12/11/2016.
 */
public class BeamJepit extends Beam{
    public BeamJepit(Input input) {
        super(input);
        System.out.println("> BeamJepit initiated");

        // Hitung Gaya Vertikal di Ay
        setAy();

        // Hitung Momen di A
        setMa();

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

    private void setAy() {
        // Ay =
        // ( mass_per_metre * beamLength * gravity ) + force

        mAy = mCrossSection.mass_per_metre
                .multiply( mInput.getBeamLength() )
                .multiply( mGravity )
                .add( mForce ).setScale(12, RoundingMode.HALF_EVEN);
    }

    private void setMa() {
        // Ma =
        // - ( 0.5 R L + F x )
        // -1 * ( ( mass_per_metre * beamLength * gravity ) * ( 0.5 *  beamLength ) + force * force_position )

        mMa = new BigDecimal(-1)
                .multiply(
                        mCrossSection.mass_per_metre
                        .multiply(mInput.getBeamLength())
                        .multiply(mGravity)
                        .multiply(new BigDecimal(0.5))
                        .multiply(mInput.getBeamLength())
                        .add(
                                mForce
                                .multiply(mInput.getForcePosition())
                        )
                );
    }

    private void setInnerVerticalForceNodes() {
        /**
         * Menghitung Gaya Dalam Vertikal di Tiap Lokasi Node
         */

        // Inisiasi Class mInnerVerticalForceNodes
        mInnerVerticalForceNodes = new TreeMap<>();

        for(BigDecimal n : mXNodes){

                // mAy - ( mass_per_metre * n * gravity ) - force
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

    private void setInnerHorizontalForceNodes() {
        /**
         * Menghitung Gaya Dalam Horizontal di Tiap Lokasi Node
         */

        // Referensi Class mInnerHorizontalForceNodes
        mInnerHorizontalForceNodes = new TreeMap<>();

        for(BigDecimal n : mXNodes){



                //
                try {
                    mInnerHorizontalForceNodes.put(n, new BigDecimal(0));
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
                            )
                            .add(mMa)
                            .setScale(12, RoundingMode.HALF_EVEN)
                    );
                }catch(Exception e){
                    e.printStackTrace();
                }

            mInnerBendingMomentNodesO.put( n, mInnerBendingMomentNodes.get(n).setScale(3,RoundingMode.HALF_EVEN) );

        }
    }


}
