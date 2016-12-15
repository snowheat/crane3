package crane;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.TreeMap;

/**
 * Created by insan on 12/5/2016.
 */
public class Result{



    public Result() {

    }

    public TreeMap<BigDecimal,BigDecimal>
        innerVerticalForceNodes,
        innerHorizontalForceNodes,
        innerBendingMomentNodes,

        normalStressNodes,
        normalBendingStressNodes,
        shearStressNodes,

        maxPrincipalStressCZeroNodes,
        maxPrincipalStressCMaxNodes
        ;

    public BigDecimal
        maxPrincipalStressCMaxValue,
        maxPrincipalStressCMaxLocation,
        maxPrincipalStressCZeroValue,
        maxPrincipalStressCZeroLocation,
        safetyFactor;

    public Input input;
}
