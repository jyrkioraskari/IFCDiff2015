package fi.ni.ifc2x3;
import fi.ni.ifc2x3.interfaces.*;
import fi.ni.*;
import java.util.*;

/*
 * IFC Java class
 * @author Jyrki Oraskari
 * @license This work is licensed under a Creative Commons Attribution 3.0 Unported License.
 * http://creativecommons.org/licenses/by/3.0/ 
 */

public class IfcPerson extends Thing implements IfcActorSelect, IfcObjectReferenceSelect
{
 // The property attributes
 String id;
 String familyName;
 String givenName;
 List<String> middleNames = new IfcList<String>();
 List<String> prefixTitles = new IfcList<String>();
 List<String> suffixTitles = new IfcList<String>();
 List<IfcActorRole> roles = new IfcList<IfcActorRole>();
 List<IfcAddress> addresses = new IfcList<IfcAddress>();
 // The inverse attributes

 InverseLinksList<IfcPersonAndOrganization> engagedIn= new InverseLinksList<IfcPersonAndOrganization>();


 // Getters and setters of properties

 public String getId() {
   return id;
 }
 public void setId(String value){
   this.id=value;

 }

 public String getFamilyName() {
   return familyName;
 }
 public void setFamilyName(String value){
   this.familyName=value;

 }

 public String getGivenName() {
   return givenName;
 }
 public void setGivenName(String value){
   this.givenName=value;

 }

 public List<String> getMiddleNames() {
   return middleNames;
 }
 public void setMiddleNames(String value){
   this.middleNames.add(value);

 }

 public List<String> getPrefixTitles() {
   return prefixTitles;
 }
 public void setPrefixTitles(String value){
   this.prefixTitles.add(value);

 }

 public List<String> getSuffixTitles() {
   return suffixTitles;
 }
 public void setSuffixTitles(String value){
   this.suffixTitles.add(value);

 }

 public List<IfcActorRole> getRoles() {
   return roles;

 }
 public void setRoles(IfcActorRole value){
   this.roles.add(value);

 }

 public List<IfcAddress> getAddresses() {
   return addresses;

 }
 public void setAddresses(IfcAddress value){
   this.addresses.add(value);

 }

 // Getters and setters of inverse values

 public InverseLinksList<IfcPersonAndOrganization> getEngagedIn() {
   return engagedIn;

 }
 public void setEngagedIn(IfcPersonAndOrganization value){
   this.engagedIn.add(value);

 }

}
