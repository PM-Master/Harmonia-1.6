/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.nist.csd.pm.common.util;

/**
 * Warning, this is not meant to be a completely accrurate representation of a percentage.  Do not use
 * for systems where absolute precision is required.
 * @author Administrator
 */
public class Percentage extends Number {

    /**
	 * @uml.property  name="multiplier"
	 */
    double multiplier = 1.0f;
    /**
	 * @uml.property  name="format_precision"
	 */
    int format_precision = 2;

    public Percentage(double percentage) {
        multiplier = percentage / 100.0;
    }

    private String getFormatString(){
        return "%3." + format_precision + "f %%";
    }

    public Percentage multiply(Number number){
        double otherValue = number == null ? 1.0 : number.doubleValue();

        return new Percentage(doubleValue() * otherValue * AS_PERCENT);
    }

    private static final double AS_PERCENT = 100.0;

    @Override
    public String toString() {
        return String.format(getFormatString(), (multiplier * AS_PERCENT));
    }

    public boolean isMagnification(){
        return multiplier > 1.0;
    }

    public boolean isMinification(){
        return multiplier < 1.0;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Percentage other = (Percentage) obj;
        if (Double.doubleToLongBits(this.multiplier) != Double.doubleToLongBits(other.multiplier)) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 97 * hash + (int) (Double.doubleToLongBits(this.multiplier) ^ (Double.doubleToLongBits(this.multiplier) >>> 32));
        return hash;
    }

    @Override
    public int intValue() {
        return (int) multiplier;
    }

    @Override
    public long longValue() {
        return (long) multiplier;
    }

    @Override
    public float floatValue() {
        return (float) multiplier;
    }

    @Override
    public double doubleValue() {
        return multiplier;
    }
}
