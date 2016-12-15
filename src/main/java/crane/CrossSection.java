package crane;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Created by insan on 12/7/2016.
 */
public class CrossSection {

    public Integer id;
    public String name,size;
    public BigDecimal
        mass_per_metre,
        depth_of_section,
        width_of_section,
        thickness_flange,
        thickness_web,
        root_radius,
        depth_between_fillets,
        area_of_section,
        ratios_local_buck_flange,
        ratios_local_buck_web,
        end_clearance,
        dim_detailing_n_upper,
        dim_detailing_n_lower,
        unit_weight,
        surface_area_per_metre,
        sec_moment_area_x,
        sec_moment_area_y;

    static enum Unit{DEFAULT,M,M4,M2};
    static enum ConversionMethod{MULTIPLY,DIVIDE};

    public void setMass_per_metre(BigDecimal mass_per_metre){
        this.mass_per_metre = mass_per_metre;
    }

    public Integer getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getSize() {
        return size;
    }

    public BigDecimal getMass_per_metre() {
        return mass_per_metre;
    }

    public BigDecimal getDepth_of_section(Unit unit) {

        BigDecimal r;
        switch(unit){

            // unit : m
            case M :
                r = convert(depth_of_section,ConversionMethod.DIVIDE,1000);
            break;

            // default unit : mm
            default : r = depth_of_section; break;
        }

        return r;

    }

    public BigDecimal getWidth_of_section(Unit unit) {

        BigDecimal r;
        switch(unit){

            // unit : m
            case M :
                r = convert(width_of_section,ConversionMethod.DIVIDE,1000);
                break;

            // default unit : mm
            default : r = width_of_section; break;
        }

        return r;
    }

    public BigDecimal getThickness_flange(Unit unit) {
        BigDecimal r;
        switch(unit){

            // unit : m
            case M :
                r = convert(thickness_flange,ConversionMethod.DIVIDE,1000);
                break;

            // default unit : mm
            default : r = thickness_flange; break;
        }

        return r;
    }

    public BigDecimal getThickness_web(Unit unit) {

        BigDecimal r;
        switch(unit){

            // unit : m
            case M :
                r = convert(thickness_web,ConversionMethod.DIVIDE,1000);
                break;

            // default unit : mm
            default : r = thickness_web; break;
        }

        return r;
    }

    public BigDecimal getRoot_radius() {
        return root_radius;
    }

    public BigDecimal getDepth_between_fillets() {
        return depth_between_fillets;
    }

    public BigDecimal getArea_of_section(Unit unit) {

        BigDecimal r;
        switch(unit){

            // unit : m^2
            case M2 :
                r = convert(area_of_section,ConversionMethod.DIVIDE,10000);
                break;

            // default unit : cm^2
            default : r = area_of_section; break;
        }

        return r;
    }

    public BigDecimal getRatios_local_buck_flange() {
        return ratios_local_buck_flange;
    }

    public BigDecimal getRatios_local_buck_web() {
        return ratios_local_buck_web;
    }

    public BigDecimal getEnd_clearance() {
        return end_clearance;
    }

    public BigDecimal getDim_detailing_n_upper() {
        return dim_detailing_n_upper;
    }

    public BigDecimal getDim_detailing_n_lower() {
        return dim_detailing_n_lower;
    }

    public BigDecimal getUnit_weight() {
        return unit_weight;
    }

    public BigDecimal getSurface_area_per_metre() {
        return surface_area_per_metre;
    }

    public BigDecimal getSec_moment_area_x(Unit unit) {
        BigDecimal r;
        switch(unit){

            // unit : m^4
            case M4 :
                r = convert(sec_moment_area_x,ConversionMethod.DIVIDE,10000);
                break;

            // default unit : cm^4
            default : r = sec_moment_area_x; break;
        }

        return r;
    }

    public BigDecimal getSec_moment_area_y() {
        return sec_moment_area_y;
    }

    private BigDecimal convert(BigDecimal value,ConversionMethod conversionMethod, Integer conversionRate){

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
