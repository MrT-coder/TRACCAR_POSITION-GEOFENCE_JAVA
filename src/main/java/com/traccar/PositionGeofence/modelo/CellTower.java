package com.traccar.PositionGeofence.modelo;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.traccar.PositionGeofence.config.Config;
import com.traccar.PositionGeofence.config.Keys;

import java.util.Objects;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class CellTower {

    public static CellTower from(int mcc, int mnc, int lac, long cid) {
        CellTower cellTower = new CellTower();
        cellTower.setMobileCountryCode(mcc);
        cellTower.setMobileNetworkCode(mnc);
        cellTower.setLocationAreaCode(lac);
        cellTower.setCellId(cid);
        return cellTower;
    }

    public static CellTower from(int mcc, int mnc, int lac, long cid, int rssi) {
        CellTower cellTower = CellTower.from(mcc, mnc, lac, cid);
        cellTower.setSignalStrength(rssi);
        return cellTower;
    }

    public static CellTower fromLacCid(Config config, int lac, long cid) {
        return from(config.getInteger(Keys.GEOLOCATION_MCC), config.getInteger(Keys.GEOLOCATION_MNC), lac, cid);
    }

    public static CellTower fromCidLac(Config config, long cid, int lac) {
        return fromLacCid(config, lac, cid);
    }

    private String radioType;

    public String getRadioType() {
        return radioType;
    }

    public void setRadioType(String radioType) {
        this.radioType = radioType;
    }

    private Long cellId;

    public Long getCellId() {
        return cellId;
    }

    public void setCellId(Long cellId) {
        this.cellId = cellId;
    }

    private Integer locationAreaCode;

    public Integer getLocationAreaCode() {
        return locationAreaCode;
    }

    public void setLocationAreaCode(Integer locationAreaCode) {
        this.locationAreaCode = locationAreaCode;
    }

    private Integer mobileCountryCode;

    public Integer getMobileCountryCode() {
        return mobileCountryCode;
    }

    public void setMobileCountryCode(Integer mobileCountryCode) {
        this.mobileCountryCode = mobileCountryCode;
    }

    private Integer mobileNetworkCode;

    public Integer getMobileNetworkCode() {
        return mobileNetworkCode;
    }

    public void setMobileNetworkCode(Integer mobileNetworkCode) {
        this.mobileNetworkCode = mobileNetworkCode;
    }

    private Integer signalStrength;

    public Integer getSignalStrength() {
        return signalStrength;
    }

    public void setSignalStrength(Integer signalStrength) {
        this.signalStrength = signalStrength > 0 ? -signalStrength : signalStrength;
    }

    public void setOperator(long operator) {
        String operatorString = String.valueOf(operator);
        mobileCountryCode = Integer.parseInt(operatorString.substring(0, 3));
        mobileNetworkCode = Integer.parseInt(operatorString.substring(3));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        CellTower cellTower = (CellTower) o;
        return Objects.equals(radioType, cellTower.radioType)
                && Objects.equals(cellId, cellTower.cellId)
                && Objects.equals(locationAreaCode, cellTower.locationAreaCode)
                && Objects.equals(mobileCountryCode, cellTower.mobileCountryCode)
                && Objects.equals(mobileNetworkCode, cellTower.mobileNetworkCode)
                && Objects.equals(signalStrength, cellTower.signalStrength);
    }

    @Override
    public int hashCode() {
        return Objects.hash(radioType, cellId, locationAreaCode, mobileCountryCode, mobileNetworkCode, signalStrength);
    }

}
