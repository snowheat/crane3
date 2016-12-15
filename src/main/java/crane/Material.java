package crane;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Created by insan on 12/7/2016.
 */
public class Material {

    public Integer id;
    public String name;
    public BigDecimal yield_strength,tensile_strength;

    static enum Unit{DEFAULT,M2};
    static enum ConversionMethod{MULTIPLY,DIVIDE};

    public BigDecimal getYield_strength(Unit unit) {
        BigDecimal r;
        switch(unit){

            // unit : N/m^2
            case M2 :
                r = convert(yield_strength, ConversionMethod.MULTIPLY,1000000);
                break;

            // default unit : N/mm^2
            default : r = yield_strength; break;
        }

        return r;
    }

    private BigDecimal convert(BigDecimal value, ConversionMethod conversionMethod, Integer conversionRate){

        BigDecimal r;

        switch(conversionMethod){
            case DIVIDE:
                r = value.divide(new BigDecimal(conversionRate),6, RoundingMode.HALF_EVEN);
                break;
            default:
                r = value.multiply(new BigDecimal(conversionRate));
                break;
        }

        return r;

    }
}
