package com.appspot.autoanom.core.events;

public class SelectDeviceFromListEvent {

    private final CharSequence devicesName[];

    public SelectDeviceFromListEvent(CharSequence devicesName[]) {
        this.devicesName = devicesName;
    }

    public CharSequence[] getCharSequenceArray() {
        return devicesName;
    }

}