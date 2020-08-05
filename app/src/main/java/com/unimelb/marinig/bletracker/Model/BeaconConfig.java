package com.unimelb.marinig.bletracker.Model;

import android.util.Log;

import androidx.annotation.NonNull;

public class BeaconConfig {
    private static final String TAG = "BeaconConfig";
    private String mMac = null;
    private Integer mTxPowerIndex = null;
    private Integer mAdvRateIndex = null;
    private Integer mMinor = null;
    private String mExtra = "";

    public BeaconConfig(){

    }

    public BeaconConfig(String pMac, Integer pTxPower, Integer pAdvRate, Integer pMinor){
        mMac = pMac;
        mTxPowerIndex = pTxPower;
        mAdvRateIndex = pAdvRate;
        mMinor = pMinor;
    }

    @NonNull
    @Override
    public String toString() {
        String tx = mTxPowerIndex == null ? null : ScannedDeviceRecord.DOT_TRANSMIT_POWER_VALUES[mTxPowerIndex].toString();
        String adv = mAdvRateIndex == null ? null : ScannedDeviceRecord.ADV_RATE_VALUES[mAdvRateIndex].toString();
        String minor = mMinor == null ? null : mMinor.toString();
        return "[ MAC: "+mMac + ", Tx: " + tx + "dBm, Adv: " + adv + "hz, Minor: " + minor +", Extra: " +mExtra+"]";
    }

    public String getMAC(){
        return mMac;
    }
    public void setMAC(String pMac){
        mMac = pMac;
    }

    public void setMinor(){
        mMinor = null;
    }

    public void setMinor(Integer pMinor){
        mMinor = pMinor;
    }

    public void setMinor(String pMinor){
        try {
            mMinor = Integer.valueOf(pMinor);
        }
        catch(Exception ex){
            Log.e(TAG, "ERROR setting minor: " +ex.getMessage());
        }
    }

    public Integer nextMinor(){
        mMinor++;
        return mMinor;
    }

    public Integer getMinor(){
        return mMinor;
    }

    public void setAdvRateIndex(){
        mAdvRateIndex = null;
    }

    public void setAdvRateIndex(Integer pAdvRateIndex){
        mAdvRateIndex = pAdvRateIndex;
    }
    public void setAdvRateIndex(String pAdvRateIndex){
        try{
            Integer advRateIndex = Integer.valueOf(pAdvRateIndex);
            setAdvRateIndex(advRateIndex);
        }
        catch(Exception ex){
            Log.e(TAG, "ERROR setting Advertisement Rate Index: " +ex.getMessage());
        }
    }

    public void setAdvRate(Integer pAdvRate){
        for(int i = 0; i < ScannedDeviceRecord.ADV_RATE_VALUES.length; i++){
            if(ScannedDeviceRecord.ADV_RATE_VALUES[i].equals(pAdvRate)){
                mAdvRateIndex = i;
                return;
            }
        }
    }
    public void setAdvRate(String pAdvRate){
        try{
            Integer advRate = Integer.valueOf(pAdvRate);
            setAdvRate(advRate);
        }
        catch(Exception ex){
            Log.e(TAG, "ERROR setting Advertisement Rate: " +ex.getMessage());
        }
    }

    public Integer getAdvRate(){
        return mAdvRateIndex == null ? null : ScannedDeviceRecord.ADV_RATE_VALUES[mAdvRateIndex];
    }
    public Integer getAdvRateIndex(){
        return mAdvRateIndex;
    }


    public Integer nextAdvRateIndex(){
        mAdvRateIndex += 1;
        if(mAdvRateIndex >= ScannedDeviceRecord.ADV_RATE_VALUES.length) {
            mAdvRateIndex = 0;
        }
        return mAdvRateIndex;
    }

    public Integer nextAdvRate(){
        nextAdvRateIndex();
        return ScannedDeviceRecord.ADV_RATE_VALUES[mAdvRateIndex];
    }



    public void setTxPower(String pTxPower){
        try{
            Integer txPower = Integer.valueOf(pTxPower);
            setTxPower(txPower);
        }
        catch(Exception ex){
            Log.e(TAG, "ERROR setting TX Power: " +ex.getMessage());
        }
    }

    public void setTxPower(Integer pTxPower){
        for(int i = 0; i < ScannedDeviceRecord.DOT_TRANSMIT_POWER_VALUES.length; i++){
            if(ScannedDeviceRecord.DOT_TRANSMIT_POWER_VALUES[i].equals(pTxPower)){
                mTxPowerIndex = i;
                return;
            }
        }
    }

    public void setTxPowerIndex(){
        mTxPowerIndex = null;
    }
    public void setTxPowerIndex(String pTxPowerIndex){
        try{
            Integer txPowerIndex = Integer.valueOf(pTxPowerIndex);
            setTxPowerIndex(txPowerIndex);
        }
        catch(Exception ex){
            Log.e(TAG, "ERROR setting TX Power Index: " +ex.getMessage());
        }
    }

    public void setTxPowerIndex(Integer pTxPowerIndex){
        mTxPowerIndex = pTxPowerIndex;
    }

    public Integer getTxPowerIndex(){
        return mTxPowerIndex;
    }

    public Integer getTxPower(){
        return mTxPowerIndex == null ? null : ScannedDeviceRecord.DOT_TRANSMIT_POWER_VALUES[mTxPowerIndex];
    }

    public Integer nextTxPowerIndex(){
        mTxPowerIndex += 1;
        if(mTxPowerIndex >= ScannedDeviceRecord.DOT_TRANSMIT_POWER_VALUES.length) {
            mTxPowerIndex = 0;
        }
        return mTxPowerIndex;
    }

    public Integer nextTxPower(){
        nextTxPowerIndex();
        return ScannedDeviceRecord.DOT_TRANSMIT_POWER_VALUES[mTxPowerIndex];
    }

    public BeaconConfig clone(){
        BeaconConfig clone = new BeaconConfig();
        clone.mAdvRateIndex = this.mAdvRateIndex;
        clone.mTxPowerIndex = this.mTxPowerIndex;
        clone.mMinor = this.mMinor;
        clone.mMac = this.mMac;
        clone.mExtra = this.mExtra;
        return clone;
    }

    public String getExtra(){
        return mExtra;
    }

    public void setExtra(String pExtra){
        mExtra = pExtra;
    }
}
