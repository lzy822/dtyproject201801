package com.geopdfviewer.android;

import org.litepal.crud.LitePalSupport;

/**
 * 权利人信息表
 *
 * 李正洋
 *
 * 2020/7/20
 */
public class QLRXXSheet extends LitePalSupport {
    private String RightHolderID;//权利人id
    private String ParcelID;//宗地id
    private String Ownship;//所属权利人
    private String Relationship;//与权利人关系
    private String RightHolderType;//权利人类型
    private String HolderPercentage;//占比
    private String Name;//姓名
    private String LegalPerson;//法人
    private String Sex;//性别
    private String Age;//年龄
    private String idNum;//身份证号
    private String PhoneNumber;//电话号码
    private String Address;//地址

    public String getRightHolderID() {
        return RightHolderID;
    }

    public void setRightHolderID(String rightHolderID) {
        RightHolderID = rightHolderID;
    }

    public String getParcelID() {
        return ParcelID;
    }

    public void setParcelID(String parcelID) {
        ParcelID = parcelID;
    }

    public String getOwnship() {
        return Ownship;
    }

    public void setOwnship(String ownship) {
        Ownship = ownship;
    }

    public String getRelationship() {
        return Relationship;
    }

    public void setRelationship(String relationship) {
        Relationship = relationship;
    }

    public String getRightHolderType() {
        return RightHolderType;
    }

    public void setRightHolderType(String rightHolderType) {
        RightHolderType = rightHolderType;
    }

    public String getHolderPercentage() {
        return HolderPercentage;
    }

    public void setHolderPercentage(String holderPercentage) {
        HolderPercentage = holderPercentage;
    }

    public String getName() {
        return Name;
    }

    public void setName(String name) {
        Name = name;
    }

    public String getLegalPerson() {
        return LegalPerson;
    }

    public void setLegalPerson(String legalPerson) {
        LegalPerson = legalPerson;
    }

    public String getSex() {
        return Sex;
    }

    public void setSex(String sex) {
        Sex = sex;
    }

    public String getAge() {
        return Age;
    }

    public void setAge(String age) {
        Age = age;
    }

    public String getIdNum() {
        return idNum;
    }

    public void setIdNum(String idNum) {
        this.idNum = idNum;
    }

    public String getPhoneNumber() {
        return PhoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        PhoneNumber = phoneNumber;
    }

    public String getAddress() {
        return Address;
    }

    public void setAddress(String address) {
        Address = address;
    }
}
