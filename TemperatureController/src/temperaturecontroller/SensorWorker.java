/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package temperaturecontroller;

import com.dalsemi.onewire.OneWireAccessProvider;
import com.dalsemi.onewire.OneWireException;
import com.dalsemi.onewire.adapter.DSPortAdapter;
import com.dalsemi.onewire.container.OneWireContainer;
import com.dalsemi.onewire.container.TemperatureContainer;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Артем
 */
public class SensorWorker {
    
    //private static final SensorWorker INSTANCE = new SensorWorker();
    private static final int TEMPERATURE_SENSOR_FAMILY = 0x28;
    
    private DSPortAdapter  adapter;
    
    public SensorWorker() throws OneWireException { 
        initializeAdapter();
    }
    
    public void initializeAdapter() throws OneWireException {
        adapter = OneWireAccessProvider.getDefaultAdapter();
        System.out.println(adapter.getAdapterName());
    }
    
    /*public SensorWorker getInstance() {
        return INSTANCE;
    }*/
    
    public void findAllTemperatureSensor() {
        
    }
    
    public Map<String, Double> getTemperature() {
        Map<String, Double> temperatureContainer = new HashMap<>();
        if(adapter != null)
        {
            boolean next = false;
            try {
                adapter.targetFamily(TEMPERATURE_SENSOR_FAMILY);
                next = adapter.findFirstDevice();
                while(next) {
                    OneWireContainer owc = adapter.getDeviceContainer();
                    System.out.println(owc.getAddressAsString() + "  " + owc.getAlternateNames());
                    TemperatureContainer tc = null;
                    tc = (TemperatureContainer) owc;
                    tc.doTemperatureConvert(tc.readDevice());
                    System.out.println(tc.getTemperature(tc.readDevice()));
                    temperatureContainer.put(owc.getAddressAsString(), tc.getTemperature(tc.readDevice()));
                    next = adapter.findNextDevice();
                }
            }
            catch (OneWireException ex) {
                System.out.println("Error reading sensors");
                System.out.println(ex.getMessage());
            }           
        }
        return temperatureContainer;
    }
    
}