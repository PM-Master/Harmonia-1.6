/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package gov.nist.csd.pm.common.model;

/**
 *
 * @author Administrator
 */
public interface Transformation<F,T>{

    public static final Boolean LOSSLESS = true;
    public static final Boolean LOSSY = false;

    /**
     * Indicates the object this transformer operates on in the forward direction.
     * @return
     */
    public Class<F> fromType();
    /**
     * Indicates the object this transformer converts to in the forward direction.
     * @param toObject
     * @return
     */
    public Class<T> toType();
    /**
     * Specifies whether or not this conversion is potentially lossless.  Meaning no data
     * would be lost in the event of the transformation.  For instance, strings cannot represent
     * repeating fractions representable by BigDecimal but can represent all primitive java data types
     * @return
     */
    public Boolean isLossless();
    /**
     * Specifies whether or not this transformer is reverseable.
     * @return
     */
    public Boolean isReversable();
    /**
     * Specifies whether or not this transformers reverse transform is lossless.
     * @return
     */
    public Boolean isReverseLossless();
    public T transform(F fromObject);
    public F reverseTransform(T toObject);
}
