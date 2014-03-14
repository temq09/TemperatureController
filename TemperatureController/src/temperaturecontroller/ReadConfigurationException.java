/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package temperaturecontroller;

/**
 *
 * @author Артем
 */
public class ReadConfigurationException extends Exception {
    private static final long serialVersionUID = 1L;
    
    
    public ReadConfigurationException() {}
    
    public ReadConfigurationException(String messageString) {
        super(messageString);
    }
    
}
