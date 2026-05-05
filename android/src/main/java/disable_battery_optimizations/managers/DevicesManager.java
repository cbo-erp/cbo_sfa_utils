package disable_battery_optimizations.managers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import disable_battery_optimizations.devices.Asus;
import disable_battery_optimizations.devices.DeviceAbstract;
import disable_battery_optimizations.devices.DeviceBase;
import disable_battery_optimizations.devices.HTC;
import disable_battery_optimizations.devices.Huawei;
import disable_battery_optimizations.devices.Letv;
import disable_battery_optimizations.devices.Meizu;
import disable_battery_optimizations.devices.OnePlus;
import disable_battery_optimizations.devices.Oppo;
import disable_battery_optimizations.devices.Samsung;
import disable_battery_optimizations.devices.Vivo;
import disable_battery_optimizations.devices.Xiaomi;
import disable_battery_optimizations.devices.ZTE;
import disable_battery_optimizations.utils.LogUtils;
import disable_battery_optimizations.utils.SystemUtils;

public class DevicesManager {

    private static List<DeviceAbstract> deviceBaseList = new ArrayList<>(Arrays.asList(
            new Asus(),
            new Huawei(),
            new Letv(),
            new Meizu(),
            new OnePlus(),
            new Oppo(),
            new Vivo(),
            new HTC(),
            new Samsung(),
            new Xiaomi(),
            new ZTE()));

    public static DeviceBase getDevice(){
        List<DeviceBase> currentDeviceBase =new ArrayList<>();
        for (DeviceBase deviceBase : deviceBaseList) {
            if(deviceBase.isThatRom()){
                currentDeviceBase.add(deviceBase);
            }
        }
        if(currentDeviceBase.size()>1){
            StringBuilder logDevices= new StringBuilder();
            for (DeviceBase deviceBase : currentDeviceBase) {
                logDevices.append(deviceBase.getDeviceManufacturer());
            }

            LogUtils.e(DevicesManager.class.getName(),"MORE THAN ONE CORRESPONDING:"+logDevices+"|"+
                    SystemUtils.getDefaultDebugInformation());
        }

        if (!currentDeviceBase.isEmpty()) {
            return currentDeviceBase.get(0);
        }else {
            return null;
        }
    }
}
