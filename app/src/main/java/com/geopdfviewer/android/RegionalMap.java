package com.geopdfviewer.android;

import java.util.List;

public class RegionalMap{
    private String RegionName;
    private List<RegionalMap> SubRegionalMap;
    private List<ElectronicAtlasMap> Maps;

    public RegionalMap(String regionName, List<ElectronicAtlasMap> maps) {
        RegionName = regionName;
        Maps = maps;
    }

    public String getRegionName() {
        return RegionName;
    }

    public void setRegionName(String regionName) {
        RegionName = regionName;
    }

    public List<RegionalMap> getSubRegionalMap() {
        return SubRegionalMap;
    }

    public void setSubRegionalMap(List<RegionalMap> subRegionalMap) {
        SubRegionalMap = subRegionalMap;
    }

    public List<ElectronicAtlasMap> getMaps() {
        return Maps;
    }

    public void setMaps(List<ElectronicAtlasMap> maps) {
        Maps = maps;
    }
}
