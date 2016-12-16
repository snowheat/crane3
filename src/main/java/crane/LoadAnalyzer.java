package crane;


/**
 * Created by insan on 12/5/2016.
 */
public class LoadAnalyzer{

    private Input mInput;
    private Result mResult1,mResult2;
    private ResultOutput mResultOutput;


    public LoadAnalyzer(Input input) {
        mInput = input;
        System.out.println("> LoadAnalyzer initiated");
        Beam beam1,beam2;

    }

    public ResultOutput getResultObject(){

        System.out.println("> LoadAnalyzer.getResultObject()");

            Beam beam1,beam2;
            ResultOutput mResultOutput = new ResultOutput();



            switch (mInput.getSimulationID()) {
                case 1:
                    System.out.println(">> case 1 :");


                    beam1 = new Beam2Penyangga(mInput);
                    mResult1 = beam1.getResultObject();

                    mInput.setMassPerLength("0");
                    beam2 = new Beam2Penyangga(mInput);
                    mResult2 = beam2.getResultObject();

                break;

                case 2:
                    System.out.println(">> case 2 :");

                    beam1 = new BeamJepit(mInput);
                    mResult1 = beam1.getResultObject();

                    mInput.setMassPerLength("0");
                    beam2 = new BeamJepit(mInput);
                    mResult2 = beam2.getResultObject();

                break;

                case 3:
                    System.out.println(">> case 3 :");

                    beam1 = new BeamTaliBaja(mInput);
                    mResult1 = beam1.getResultObject();

                    mInput.setMassPerLength("0");
                    beam2 = new BeamTaliBaja(mInput);
                    mResult2 = beam2.getResultObject();

                    break;
            }

            mResultOutput.result1 = mResult1;
            mResultOutput.result2 = mResult2;

        return mResultOutput;
    }


}

class ResultOutput{
    public Result result1,result2;
}
