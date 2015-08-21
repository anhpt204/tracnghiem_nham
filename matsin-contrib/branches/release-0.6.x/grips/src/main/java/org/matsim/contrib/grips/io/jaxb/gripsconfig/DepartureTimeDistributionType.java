//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.4-2 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2014.02.21 at 11:20:28 AM CET 
//


package org.matsim.contrib.grips.io.jaxb.gripsconfig;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for departureTimeDistributionType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="departureTimeDistributionType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="distribution" type="{}distributionType"/>
 *         &lt;element name="sigma" type="{http://www.w3.org/2001/XMLSchema}double"/>
 *         &lt;element name="mu" type="{http://www.w3.org/2001/XMLSchema}double"/>
 *         &lt;element name="earliest" type="{http://www.w3.org/2001/XMLSchema}double"/>
 *         &lt;element name="latest" type="{http://www.w3.org/2001/XMLSchema}double"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "departureTimeDistributionType", propOrder = {
    "distribution",
    "sigma",
    "mu",
    "earliest",
    "latest"
})
public class DepartureTimeDistributionType {

    @XmlElement(required = true)
    protected DistributionType distribution;
    protected double sigma;
    protected double mu;
    protected double earliest;
    protected double latest;

    /**
     * Gets the value of the distribution property.
     * 
     * @return
     *     possible object is
     *     {@link DistributionType }
     *     
     */
    public DistributionType getDistribution() {
        return distribution;
    }

    /**
     * Sets the value of the distribution property.
     * 
     * @param value
     *     allowed object is
     *     {@link DistributionType }
     *     
     */
    public void setDistribution(DistributionType value) {
        this.distribution = value;
    }

    /**
     * Gets the value of the sigma property.
     * 
     */
    public double getSigma() {
        return sigma;
    }

    /**
     * Sets the value of the sigma property.
     * 
     */
    public void setSigma(double value) {
        this.sigma = value;
    }

    /**
     * Gets the value of the mu property.
     * 
     */
    public double getMu() {
        return mu;
    }

    /**
     * Sets the value of the mu property.
     * 
     */
    public void setMu(double value) {
        this.mu = value;
    }

    /**
     * Gets the value of the earliest property.
     * 
     */
    public double getEarliest() {
        return earliest;
    }

    /**
     * Sets the value of the earliest property.
     * 
     */
    public void setEarliest(double value) {
        this.earliest = value;
    }

    /**
     * Gets the value of the latest property.
     * 
     */
    public double getLatest() {
        return latest;
    }

    /**
     * Sets the value of the latest property.
     * 
     */
    public void setLatest(double value) {
        this.latest = value;
    }

}
