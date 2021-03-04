package de.incentergy.iso11783.part10.geotools;

import java.util.HashMap;
import java.util.Map;

import javax.xml.bind.annotation.adapters.XmlAdapter;

import de.incentergy.iso11783.part10.v4.DeviceElement;
import de.incentergy.iso11783.part10.v4.ISO11783TaskDataFile;

public class TLGAdapter extends XmlAdapter<String, DeviceElement> {

    private Map<String,DeviceElement> deviceElements = new HashMap<String,DeviceElement>();

    public void setDeviceElementList(ISO11783TaskDataFile taskDataFile){
        taskDataFile.getDevice().forEach((device)->{
            device.getDeviceElement().forEach((det)->{
                this.deviceElements.put(det.getDeviceElementId(), det);
            });
        });
    }

    @Override
    public DeviceElement unmarshal(String v) throws Exception {
        return this.deviceElements.get(v);
    }

    @Override
    public String marshal(DeviceElement v) throws Exception {
        for(Map.Entry<String, DeviceElement> entry: this.deviceElements.entrySet()){
            if (entry.getValue().equals(v)){
                return entry.getKey();
            }
        }
        return null;
    }
    
}
