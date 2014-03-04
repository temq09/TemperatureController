/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package temperaturecontroller;

import com.dalsemi.onewire.OneWireAccessProvider;
import com.dalsemi.onewire.OneWireException;
import com.dalsemi.onewire.adapter.DSPortAdapter;
import com.dalsemi.onewire.adapter.OneWireIOException;
import com.dalsemi.onewire.container.OneWireContainer;
import com.dalsemi.onewire.container.TemperatureContainer;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Артем
 */
public final class SensorWorker {
    
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
    
    public void findAllTemperatureSensor() {
        
    }
    
    public Map<String, Double> getTemperature() throws OneWireException {
        Map<String, Double> temperatureContainer = new HashMap<>();
        if(adapter != null)
        {
            boolean next = false;
            adapter.targetFamily(TEMPERATURE_SENSOR_FAMILY);
            next = adapter.findFirstDevice();
            while(next) {
                OneWireContainer owc = adapter.getDeviceContainer();
                System.out.println(owc.getAddressAsString() + "  " + owc.getAlternateNames());
                TemperatureContainer tc = null;
                tc = (TemperatureContainer) owc;
                try{
                    tc.doTemperatureConvert(tc.readDevice());
                    System.out.println(tc.getTemperature(tc.readDevice()));
                    
                    /* 85 возрашается при неправильном чтении данных с датчика */
                    if(tc.getTemperature(tc.readDevice()) == 85) {
                        System.out.println("Температура прочитанна не правильно");
                    } else {
                        temperatureContainer.put(owc.getAddressAsString(), tc.getTemperature(tc.readDevice()));
                    }
                }
                catch (OneWireIOException ex) {
                    System.out.println("Ошибка при чтении данных из температуры");
                }
                
                next = adapter.findNextDevice();
            }          
        }
        return temperatureContainer;
    }
    
}