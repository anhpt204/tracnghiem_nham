//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, vhudson-jaxb-ri-2.1-558 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2011.09.20 at 07:21:37 PM MESZ 
//


package org.matsim.jaxb.signalsystems11;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for signalGroupDefinitionType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="signalGroupDefinitionType">
 *   &lt;complexContent>
 *     &lt;extension base="{http://www.matsim.org/files/dtd}matsimObjectType">
 *       &lt;sequence>
 *         &lt;element name="signalSystemDefinition" type="{http://www.matsim.org/files/dtd}idRefType"/>
 *         &lt;element name="lane" type="{http://www.matsim.org/files/dtd}idRefType" maxOccurs="unbounded"/>
 *         &lt;element name="toLink" type="{http://www.matsim.org/files/dtd}idRefType" maxOccurs="unbounded"/>
 *       &lt;/sequence>
 *       &lt;attribute name="linkIdRef" use="required" type="{http://www.matsim.org/files/dtd}matsimIdType" />
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "signalGroupDefinitionType", propOrder = {
    "signalSystemDefinition",
    "lane",
    "toLink"
})
public class XMLSignalGroupDefinitionType
    extends XMLMatsimObjectType
{

    @XmlElement(required = true)
    protected XMLIdRefType signalSystemDefinition;
    @XmlElement(required = true)
    protected List<XMLIdRefType> lane;
    @XmlElement(required = true)
    protected List<XMLIdRefType> toLink;
    @XmlAttribute(required = true)
    protected String linkIdRef;

    /**
     * Gets the value of the signalSystemDefinition property.
     * 
     * @return
     *     possible object is
     *     {@link XMLIdRefType }
     *     
     */
    public XMLIdRefType getSignalSystemDefinition() {
        return signalSystemDefinition;
    }

    /**
     * Sets the value of the signalSystemDefinition property.
     * 
     * @param value
     *     allowed object is
     *     {@link XMLIdRefType }
     *     
     */
    public void setSignalSystemDefinition(XMLIdRefType value) {
        this.signalSystemDefinition = value;
    }

    /**
     * Gets the value of the lane property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the lane property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getLane().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link XMLIdRefType }
     * 
     * 
     */
    public List<XMLIdRefType> getLane() {
        if (lane == null) {
            lane = new ArrayList<XMLIdRefType>();
        }
        return this.lane;
    }

    /**
     * Gets the value of the toLink property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the toLink property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getToLink().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link XMLIdRefType }
     * 
     * 
     */
    public List<XMLIdRefType> getToLink() {
        if (toLink == null) {
            toLink = new ArrayList<XMLIdRefType>();
        }
        return this.toLink;
    }

    /**
     * Gets the value of the linkIdRef property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getLinkIdRef() {
        return linkIdRef;
    }

    /**
     * Sets the value of the linkIdRef property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setLinkIdRef(String value) {
        this.linkIdRef = value;
    }

}
