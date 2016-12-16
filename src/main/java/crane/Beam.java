package crane;

import org.nevec.rjm.BigDecimalMath;
import org.sql2o.Connection;
import org.sql2o.Sql2o;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;


/**
 * Created by insan on 12/5/2016.
 */
public class Beam implements BeamInterface {

    protected Input mInput;
    protected Set<BigDecimal> mXNodes;

    protected final BigDecimal mGravity = new BigDecimal("9.81");
    protected BigDecimal mForce,mAx,mBx,mAy,mBy,mMa,mTy,mTx,mR,
        mFirstMomentOfAreaCenter,mSafetyFactor,
        mMaxPrincipalStressCZeroValue, mMaxPrincipalStressCMaxValue,
        mMaxPrincipalStressCZeroLocation, mMaxPrincipalStressCMaxLocation;

    protected Sql2o mSql2o;
    protected Connection mCon;

    protected Material mMaterial;
    protected CrossSection mCrossSection;

    protected TreeMap<BigDecimal,BigDecimal>
        mInnerVerticalForceNodes,
        mInnerHorizontalForceNodes,
        mInnerBendingMomentNodes,
        mInnerBendingMomentNodesO,

        mNormalStressNodes,
        mNormalStressNodesO,
        mNormalBendingStressNodes,
        mNormalBendingStressNodesO,
        mShearStressNodes,
        mShearStressNodesO,

        mMaxPrincipalStressCZeroNodes,
        mMaxPrincipalStressCZeroNodesO,

        mMaxPrincipalStressCMaxNodes,
        mMaxPrincipalStressCMaxNodesO
    ;

    public Beam(Input input) {

        // Set mInput sebagai field untuk data input
        mInput = input;

        // Koneksi MySQL via Sql2o
        mSql2o = new Sql2o("jdbc:sqlite:src/crane.sqlite", null, null);
        mCon = mSql2o.open();






        // Set Data Material
        setMaterial();

        // Set Data Penampang
        setCrossSection();

        // Set Gaya Akibat Massa Beban
        setForce();

        // Set Resultan Gaya Akibat Massa Jenis Beam
        setR();

        // Set First Moment of Area
        setFirstMomentOfAreaCenter();

        // Membuat TreeSet xnodes
        createXNodes();





    }

    private void setR() {
        mR = mCrossSection.getMass_per_metre()
            .multiply(mInput.getBeamLength())
            .multiply(mGravity);
    }

    protected void setMaterial(){
        String sql = "SELECT * FROM material WHERE id = :id";

            mMaterial = mCon.createQuery(sql)
                .addParameter("id", mInput.getMaterialID())
                .executeAndFetchFirst(Material.class);
    }

    protected void setCrossSection(){
        String sql = "SELECT * FROM cross_section WHERE id = :id";

            mCrossSection = mCon.createQuery(sql)
                .addParameter("id", mInput.getCrossSectionID())
                .executeAndFetchFirst(CrossSection.class);

            if(mInput.getMassPerLength() != null){
                mCrossSection.setMass_per_metre(mInput.getMassPerLength());
            }
    }

    protected void setForce() {
        mForce = mInput.getMass().multiply(mGravity);
    }

    protected BigDecimal getForceOnX(BigDecimal x){

        BigDecimal force = new BigDecimal(0);

        if(x.compareTo(mInput.getForcePosition())>=0){
            force = mForce;
        }

        //System.out.println("x = "+x+" , force_position = "+mInput.getForcePosition()+" ,F = "+force);

        return force;
    }

    protected void setFirstMomentOfAreaCenter() {
        BigDecimal q,a1,a2,y1,y2
            ,depthOfSection
            ,thicknessFlange
            ,widthOfSection
            ,thicknessWeb
            ,secondMomentOfAreaX;

        //Depth of Section (mm)
        depthOfSection = mCrossSection.getDepth_of_section(CrossSection.Unit.DEFAULT);

        //Thickness of Flange (mm)
        thicknessFlange = mCrossSection.getThickness_flange(CrossSection.Unit.DEFAULT);

        //Width of Section (mm)
        widthOfSection = mCrossSection.getWidth_of_section(CrossSection.Unit.DEFAULT);

        //Thickness of Web (mm)
        thicknessWeb = mCrossSection.getThickness_web(CrossSection.Unit.DEFAULT);

        //Second Moment of Area X (cm^4)
        secondMomentOfAreaX = mCrossSection.getSec_moment_area_x(CrossSection.Unit.M4);

        // y1 = ( depth_of_section / 2 ) - ( thickness_flange / 2 )
        y1 = depthOfSection
            .divide( new BigDecimal(2),12,RoundingMode.HALF_EVEN )
            .subtract( thicknessFlange.divide( new BigDecimal(2),12,RoundingMode.HALF_EVEN ) );

        // y2 = ( ( depth_of_section / 2 ) - thickness_flange ) / 2
        y2 = new BigDecimal(1)
            .multiply(
                depthOfSection
                .divide( new BigDecimal(2),12,RoundingMode.HALF_EVEN)
                .subtract( thicknessFlange )
            )
            .divide( new BigDecimal(2),12,RoundingMode.HALF_EVEN );

        // a1 = thickness_flange * width_of_section
        a1 = thicknessFlange.multiply(widthOfSection);

        // a2 = ( ( depth_of_section / 2 ) - thickness_flange ) * thickness_web
        a2 = new BigDecimal(1)
                .multiply(
                    depthOfSection
                    .divide( new BigDecimal(2),12,RoundingMode.HALF_EVEN )
                    .subtract( thicknessFlange )
                )
                .multiply(thicknessWeb);

        // First Moment of Area [Center]
        // mm^3
        // a1*y1 + a2*y2
        mFirstMomentOfAreaCenter = a1.multiply(y1).add(a2.multiply(y2)).setScale(12,RoundingMode.HALF_EVEN);

        //System.out.println(a1+" * "+y1+ " + " + a2 + " * " + y2 + " = " + mFirstMomentOfAreaCenter);
    }



    protected void createXNodes(){
        /**
         * Membuat TreeSet xnodes. Kumpulan titik lokasi pada beam yang akan dilakukan pengujian
         */

        // Pembagi panjang batang
        int divider = 200;

        // Menentukan jarak antar titik
        BigDecimal step = mInput.getBeamLength().divide(new BigDecimal(divider), 4, RoundingMode.HALF_EVEN);

        // Inisiasi TreeSet xnodes sebagai penampung titik-titik pengujian
        mXNodes = new TreeSet<>();

        // Mengisi TreeSet xnodes dengan titik-titik pengujian
        for(int i=0;i<=divider;i++){

            // Titik-titik pengujian merupakan hasil kali titik pengujian ke n , dengan step
            mXNodes.add(new BigDecimal(i).multiply(step));
        }

        // Memasukan titik massa beban ke TreeSet xnodes
        mXNodes.add(mInput.getForcePosition().setScale(3,RoundingMode.HALF_EVEN));

        // Memasukan 0.999 * titik massa beban ke TreeSet xnodes
        mXNodes.add( mInput.getForcePosition().multiply(new BigDecimal(1000)).subtract(new BigDecimal(1)).divide(new BigDecimal(1000)) );

        // Memasukan 0.999 * panjang beam ke TreeSet xnodes
        mXNodes.add( mInput.getBeamLength().multiply(new BigDecimal(1000)).subtract(new BigDecimal(1)).divide(new BigDecimal(1000)) );

        // Menguji titik-titik pengujian yang telah dibuat
        for(BigDecimal xnode: mXNodes){
            // System.out.println(xnode);
        }
    }


    protected void setNormalStressNodes() {
        /**
         * Menghitung Tegangan Normal di Tiap Lokasi Node,
         * Posisi Pada Ujung Penampang [Inner Normal Bending Stress Max]
         */

        // Inisiasi mInnerBendingMomentNodes
        mNormalStressNodes = new TreeMap<>();
        mNormalStressNodesO = new TreeMap<>();

        for(BigDecimal n : mXNodes){

            try {

                // Normal Stress
                // N / A
                // mInnerHorizontalForceNodes[n] / ( secondMomentOfAreaX )
                // (N)/(cm^2)

                mNormalStressNodes.put(n, mInnerHorizontalForceNodes.get(n));

                mNormalStressNodesO.put( n, mNormalStressNodes.get(n).setScale(3,RoundingMode.HALF_EVEN) );
                System.out.println("setNormalStressNodes() : Success : n = " + n);
            }catch(Exception e){
                System.out.println("setNormalStressNodes() : Failed : n = " + n + " : "+mInnerHorizontalForceNodes.get(n));
                System.out.println(e);
            }

        }
    }

    protected void setNormalBendingStressNodes() {
        /**
         * Menghitung Tegangan Normal Bending di Tiap Lokasi Node,
         * Posisi Pada Ujung Penampang [Inner Normal Bending Stress Max]
         */

        // Inisiasi mInnerBendingMomentNodes
        mNormalBendingStressNodes = new TreeMap<>();
        mNormalBendingStressNodesO = new TreeMap<>();

        for(BigDecimal n : mXNodes){

            try {

                // Normal Bending Stress
                // (My) / (I)
                // ( mInnerBendingMomentNodes[n] * 0.5 * depthOfSection ) / ( secondMomentOfAreaX )
                // (Nm.mm)/(cm^4)

                mNormalBendingStressNodes.put(n, mInnerBendingMomentNodes.get(n)
                    .multiply(
                        new BigDecimal(0.5)
                        .multiply(mCrossSection.depth_of_section)
                    ).divide(
                        mCrossSection.sec_moment_area_x, 12, RoundingMode.HALF_EVEN

                    // Konversi (Nm.mm)/(cm^4) ke (N/m^2)
                    ).multiply(new BigDecimal(100000)).abs()
                );

                mNormalBendingStressNodesO.put( n, mNormalBendingStressNodes.get(n).setScale(3,RoundingMode.HALF_EVEN) );
            }catch(Exception e){
                System.out.println(e);
            }



            //System.out.println("Inner Normal Bending Stress on " + n + " : " + mInnerBendingMomentNodes.get(n) + " " + mCrossSection.depth_of_section + " " + mCrossSection.sec_moment_area_x + " = " + mNormalBendingStressNodes.get(n) + " ; " );

        }
    }

    protected void setShearStressNodes() {
        /**
         * Menghitung Tegangan Geser di Tiap Lokasi Node,
         * Posisi Pada Tengah Penampang [Inner Shear Stress Max]
         */

        // Inisiasi mShearStressNodes
        mShearStressNodes = new TreeMap<>();
        mShearStressNodesO = new TreeMap<>();

        for(BigDecimal n : mXNodes){

            try {

                // Shear Stress
                // (V*Q) / (I*t)
                // ( mInnerVerticalForceNodes[n] * q ) / ( secondMomentOfAreaX * thicknessWeb )
                // (N.mm^3)/(cm^4.mm)

                BigDecimal secondMomentOfAreaX = mCrossSection.getSec_moment_area_x(CrossSection.Unit.DEFAULT);
                BigDecimal thicknessWeb = mCrossSection.getThickness_web(CrossSection.Unit.DEFAULT);

                mShearStressNodes.put(n, mInnerVerticalForceNodes.get(n)
                    .multiply(mFirstMomentOfAreaCenter)
                    .divide(
                        secondMomentOfAreaX.multiply(thicknessWeb),12,RoundingMode.HALF_EVEN
                    )

                    // Konversi (N.mm^3)/(cm^4.mm) ke N/(m^2)
                    .multiply(new BigDecimal(100))
                );

                mShearStressNodesO.put( n, mShearStressNodes.get(n).setScale(3,RoundingMode.HALF_EVEN) );

            }catch(Exception e){
                System.out.println(e);
            }



            //System.out.println("Inner Normal Bending Stress on " + n + " : " + mInnerBendingMomentNodes.get(n) + " " + mCrossSection.depth_of_section + " " + mCrossSection.sec_moment_area_x + " = " + mNormalBendingStressNodes.get(n) + " ; " );

        }

    }

    protected void setMaxPrincipalStressCMaxNodes(){

        /**
         * Menghitung Tegangan Principal Maksimum di Tiap Lokasi Node,
         * Posisi Pada Ujung Penampang [Inner Normal Bending Stress Max]
         * c = y
         */

        // Inisiasi Max Principal Stress di c = y
        mMaxPrincipalStressCMaxNodes = new TreeMap<>();
        mMaxPrincipalStressCMaxNodesO = new TreeMap<>();


        for(BigDecimal n : mXNodes){

            // Perhitungan Average Stress di c = y
            // (total_normal_stress_x + total_normal_stress_y)/2
            // total_normal_stress_y = 0 ( Tidak dihitung, dianggap nol )

            BigDecimal avgStress = new BigDecimal(1)
                .multiply(

                    // Total Tegangan Normal Sumbu x
                    mNormalStressNodes.get(n)
                    .add( mNormalBendingStressNodes.get(n))

                    // Total Tegangan Normal Sumbu y
                    .add( new BigDecimal(0) )
                )
                .divide( new BigDecimal(2),12,RoundingMode.HALF_EVEN );


            BigDecimal maxInPlaneShearStressPow2 = new BigDecimal(1)
                .multiply(
                    new BigDecimal(1)
                    .multiply(
                        new BigDecimal(1)
                        .multiply(
                            // Total Tegangan Normal Sumbu x
                            mNormalStressNodes.get(n)
                            .add( mNormalBendingStressNodes.get(n))

                            // Total Tegangan Normal Sumbu y
                            .subtract( new BigDecimal(0) )
                        )

                        // Dibagi 2
                        .divide( new BigDecimal(2),12,RoundingMode.HALF_EVEN )
                    )
                        // Pangkat 2
                        .pow(2)
                    )
                    .add(
                            // Tegangan Geser XY di Ujung Batang = 0
                            new BigDecimal(0).pow(2)
                    );

            try{
                BigDecimal maxInPlaneShearStress;

                if(maxInPlaneShearStressPow2.setScale(6,RoundingMode.FLOOR).compareTo(new BigDecimal(0.000000)) < 1)
                {
                    maxInPlaneShearStress = new BigDecimal(0);
                }else{
                    maxInPlaneShearStress = BigDecimalMath.sqrt(maxInPlaneShearStressPow2);
                }

                BigDecimal maxPrincipalStress = avgStress.add(maxInPlaneShearStress);

                System.out.println(avgStress + " + " + maxInPlaneShearStress + " = " + avgStress.add(maxInPlaneShearStress));

                mMaxPrincipalStressCMaxNodes.put(n,maxPrincipalStress);
                mMaxPrincipalStressCMaxNodesO.put(n, mMaxPrincipalStressCMaxNodes.get(n).setScale(3,RoundingMode.HALF_EVEN));
                //System.out.println("n : "+n+" "+ mNormalBendingStressNodes.get(n)+" Principal Stress c = y : "+avgStress+" + "+maxInPlaneShearStress+" = "+maxPrincipalStress);

                if(mMaxPrincipalStressCMaxValue == null){
                    mMaxPrincipalStressCMaxValue = maxPrincipalStress.abs();
                    mMaxPrincipalStressCMaxLocation = n;
                }else{
                    if(mMaxPrincipalStressCMaxValue.compareTo(maxPrincipalStress)<1){
                        mMaxPrincipalStressCMaxValue = maxPrincipalStress.abs();
                        mMaxPrincipalStressCMaxLocation = n;
                    }
                }
            }catch(Exception e){
                System.out.println(e);
            }

        }

        System.out.println("==============================================================");

    }

    protected void setMaxPrincipalStressCZeroNodes(){

        /**
         * Menghitung Tegangan Principal Maksimum di Tiap Lokasi Node,
         * Posisi Pada Tengah Penampang [Inner Shear Stress Max]
         * c = 0
         */
        // Inisiasi Max Principal Stress di c = 0
        mMaxPrincipalStressCZeroNodes = new TreeMap<>();
        mMaxPrincipalStressCZeroNodesO = new TreeMap<>();

        for(BigDecimal n : mXNodes){

            // Perhitungan Average Stress di c = 0
            // (total_normal_stress_x + total_normal_stress_y)/2
            // total_normal_stress_y = 0 ( Tidak dihitung, dianggap nol )

            BigDecimal avgStress = new BigDecimal(1)
                .multiply(

                    // Total Tegangan Normal Sumbu x
                    // Pada c = 0 , Tegangan Normal sumbu x akibat Momen Lentur = 0
                    // Sehingga yang dihitung hanya Tegangan Normal sumbu x
                    mNormalStressNodes.get(n)


                    // Total Tegangan Normal Sumbu y
                    .add( new BigDecimal(0) )
                )
                .divide( new BigDecimal(2),12,RoundingMode.HALF_EVEN );

            BigDecimal maxInPlaneShearStressPow2 = new BigDecimal(1)
                .multiply(
                    new BigDecimal(1)
                        .multiply(
                            new BigDecimal(1)
                                .multiply(
                                    // Total Tegangan Normal Sumbu x
                                    // Pada c = 0 , Tegangan Normal sumbu x akibat Momen Lentur = 0
                                    // Sehingga yang dihitung hanya Tegangan Normal sumbu x
                                    mNormalStressNodes.get(n)

                                    // Total Tegangan Normal Sumbu y
                                    .subtract( new BigDecimal(0) )
                                )

                                // Dibagi 2
                                .divide( new BigDecimal(2),12,RoundingMode.HALF_EVEN )
                        )
                        // Pangkat 2
                        .pow(2)
                    )
                    .add(
                        // Tegangan Geser XY di c = 0
                        mShearStressNodes.get(n).pow(2)
                    );

            try{
                BigDecimal maxInPlaneShearStress;

                if(maxInPlaneShearStressPow2.setScale(6,RoundingMode.FLOOR).compareTo(new BigDecimal(0.000000)) < 1)
                {
                    maxInPlaneShearStress = new BigDecimal(0);
                }else{
                    maxInPlaneShearStress = BigDecimalMath.sqrt(maxInPlaneShearStressPow2);
                }

                BigDecimal maxPrincipalStress = avgStress.add(maxInPlaneShearStress);

                mMaxPrincipalStressCZeroNodes.put(n,maxPrincipalStress);
                mMaxPrincipalStressCZeroNodesO.put(n, mMaxPrincipalStressCZeroNodes.get(n).setScale(3,RoundingMode.HALF_EVEN));
                //System.out.println("n : "+n+" "+ mNormalBendingStressNodes.get(n)+" Principal Stress c = 0 : "+avgStress+" + "+maxInPlaneShearStress+" = "+maxPrincipalStress);

                if(mMaxPrincipalStressCZeroValue == null){
                    mMaxPrincipalStressCZeroValue = maxPrincipalStress.abs();
                    mMaxPrincipalStressCZeroLocation = n;
                }else{
                    if(mMaxPrincipalStressCZeroValue.compareTo(maxPrincipalStress)<1){
                        mMaxPrincipalStressCZeroValue = maxPrincipalStress.abs();
                        mMaxPrincipalStressCZeroLocation = n;
                    }
                }
            }catch(Exception e){
                e.printStackTrace();
            }

        }


    }


    protected void setSafetyFactor(){
        try{
            BigDecimal maxPrincipalStress;

            if(mMaxPrincipalStressCMaxValue.compareTo(mMaxPrincipalStressCZeroValue)>0){

                // Max Principal Stress paling tinggi ada di c = y
                maxPrincipalStress = mMaxPrincipalStressCMaxValue;

            }else{
                // Max Principal Stress paling tinggi ada di c = 0
                maxPrincipalStress = mMaxPrincipalStressCZeroValue;
            }

            mSafetyFactor = mMaterial.getYield_strength(Material.Unit.M2)
                    .divide(maxPrincipalStress,12,RoundingMode.HALF_EVEN)
                    .setScale(3,RoundingMode.HALF_EVEN);


        }catch(Exception e){
            e.printStackTrace();
        }


    }

    @Override
    public Result getResultObject(){

        //System.out.println("mass_per_length = " + mCrossSection.mass_per_metre);
        //System.out.println("length = " + mInput.getBeamLength());
        //System.out.println("length = " + mGravity);
        //System.out.println("By = "+mBy);
        //System.out.println("Ay = "+mAy);

        Result result = new Result();
        result.input = mInput;

        result.innerVerticalForceNodes = mInnerVerticalForceNodes;
        result.innerHorizontalForceNodes = mInnerHorizontalForceNodes;
        result.innerBendingMomentNodes = mInnerBendingMomentNodesO;

        result.normalStressNodes = mNormalStressNodesO;
        result.normalBendingStressNodes = mNormalBendingStressNodesO;
        result.shearStressNodes = mShearStressNodesO;

        result.maxPrincipalStressCZeroNodes = mMaxPrincipalStressCZeroNodesO;
        result.maxPrincipalStressCMaxNodes = mMaxPrincipalStressCMaxNodesO;

        result.maxPrincipalStressCMaxValue = mMaxPrincipalStressCMaxValue;
        result.maxPrincipalStressCMaxLocation = mMaxPrincipalStressCMaxLocation;

        result.safetyFactor = mSafetyFactor;

        return result;
    }
}
