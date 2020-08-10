package com.geopdfviewer.android;


import org.litepal.crud.LitePalSupport;

/**
 *
 * 宗地信息表
 *
 * 李正洋
 *
 * 2020/7/20
 *
 */
public class ZDXXSheet extends LitePalSupport {
    private String idnum;//宗地id
    private String ParcelType;//宗地类型
    private String SerialNum;//流水号
    private String Dist;//州
    private String County;//县
    private String Town;//乡镇
    private String TownNum;//乡镇代码
    private String VillageCommittee;//村委会
    private String VillageTeam;//村小组
    private String HouseNum;//门牌号
    private String RealFunction;//实际用途
    private String ActualArea;//实际面积
    private String ApprovedArea;//批准面积
    private String LicenseArea;//发证面积
    private String LandCertificateNum;//土地证书编号
    private String EastNearGround;//东临近区域
    private String SouthNearGround;//南临近区域
    private String WestNearGround;//西临近区域
    private String NorthNearGround;//北临近区域
    private String TotalSurfaceArea;//总建筑面积
    private String HousingCertificateNum;//房屋证书编号
    private String LicensedBuildingArea;//发证建筑面积
    private String ApprovedBuildingArea;//批准建筑面积
    private double CenterX;//纬度
    private double CenterY;//经度
    private String Address;//详细地址
    private String RightHolderName;//权利人名称
    private String RightHolderID;//身份证号码
    private String ConfirmTime;//确认时间
    private boolean InitialInspectionPassed;//初查是否通过

    public String getIdnum() {
        return idnum;
    }

    public void setIdnum(String idnum) {
        this.idnum = idnum;
    }

    public String getParcelType() {
        return ParcelType;
    }

    public void setParcelType(String parcelType) {
        ParcelType = parcelType;
    }

    public String getSerialNum() {
        return SerialNum;
    }

    public void setSerialNum(String serialNum) {
        SerialNum = serialNum;
    }

    public String getDist() {
        return Dist;
    }

    public void setDist(String dist) {
        Dist = dist;
    }

    public String getCounty() {
        return County;
    }

    public void setCounty(String county) {
        County = county;
    }

    public String getTown() {
        return Town;
    }

    public void setTown(String town) {
        Town = town;
    }

    public String getTownNum() {
        return TownNum;
    }

    public void setTownNum(String townNum) {
        TownNum = townNum;
    }

    public String getVillageCommittee() {
        return VillageCommittee;
    }

    public void setVillageCommittee(String villageCommittee) {
        VillageCommittee = villageCommittee;
    }

    public String getVillageTeam() {
        return VillageTeam;
    }

    public void setVillageTeam(String villageTeam) {
        VillageTeam = villageTeam;
    }

    public String getHouseNum() {
        return HouseNum;
    }

    public void setHouseNum(String houseNum) {
        HouseNum = houseNum;
    }

    public String getRealFunction() {
        return RealFunction;
    }

    public void setRealFunction(String realFunction) {
        RealFunction = realFunction;
    }

    public String getActualArea() {
        return ActualArea;
    }

    public void setActualArea(String actualArea) {
        ActualArea = actualArea;
    }

    public String getApprovedArea() {
        return ApprovedArea;
    }

    public void setApprovedArea(String approvedArea) {
        ApprovedArea = approvedArea;
    }

    public String getLicenseArea() {
        return LicenseArea;
    }

    public void setLicenseArea(String licenseArea) {
        LicenseArea = licenseArea;
    }

    public String getLandCertificateNum() {
        return LandCertificateNum;
    }

    public void setLandCertificateNum(String landCertificateNum) {
        LandCertificateNum = landCertificateNum;
    }

    public String getEastNearGround() {
        return EastNearGround;
    }

    public void setEastNearGround(String eastNearGround) {
        EastNearGround = eastNearGround;
    }

    public String getSouthNearGround() {
        return SouthNearGround;
    }

    public void setSouthNearGround(String southNearGround) {
        SouthNearGround = southNearGround;
    }

    public String getWestNearGround() {
        return WestNearGround;
    }

    public void setWestNearGround(String westNearGround) {
        WestNearGround = westNearGround;
    }

    public String getNorthNearGround() {
        return NorthNearGround;
    }

    public void setNorthNearGround(String northNearGround) {
        NorthNearGround = northNearGround;
    }

    public String getTotalSurfaceArea() {
        return TotalSurfaceArea;
    }

    public void setTotalSurfaceArea(String totalSurfaceArea) {
        TotalSurfaceArea = totalSurfaceArea;
    }

    public String getHousingCertificateNum() {
        return HousingCertificateNum;
    }

    public void setHousingCertificateNum(String housingCertificateNum) {
        HousingCertificateNum = housingCertificateNum;
    }

    public String getLicensedBuildingArea() {
        return LicensedBuildingArea;
    }

    public void setLicensedBuildingArea(String licensedBuildingArea) {
        LicensedBuildingArea = licensedBuildingArea;
    }

    public String getApprovedBuildingArea() {
        return ApprovedBuildingArea;
    }

    public void setApprovedBuildingArea(String approvedBuildingArea) {
        ApprovedBuildingArea = approvedBuildingArea;
    }

    public double getCenterX() {
        return CenterX;
    }

    public void setCenterX(double centerX) {
        CenterX = centerX;
    }

    public double getCenterY() {
        return CenterY;
    }

    public void setCenterY(double centerY) {
        CenterY = centerY;
    }

    public String getAddress() {
        return Address;
    }

    public void setAddress(String address) {
        Address = address;
    }

    public String getRightHolderName() {
        return RightHolderName;
    }

    public void setRightHolderName(String rightHolderName) {
        RightHolderName = rightHolderName;
    }

    public String getRightHolderID() {
        return RightHolderID;
    }

    public void setRightHolderID(String rightHolderID) {
        RightHolderID = rightHolderID;
    }

    public String getConfirmTime() {
        return ConfirmTime;
    }

    public void setConfirmTime(String confirmTime) {
        ConfirmTime = confirmTime;
    }

    public boolean isInitialInspectionPassed() {
        return InitialInspectionPassed;
    }

    public void setInitialInspectionPassed(boolean initialInspectionPassed) {
        InitialInspectionPassed = initialInspectionPassed;
    }
}
