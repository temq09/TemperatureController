package temperaturecontroller;

import com.dalsemi.onewire.OneWireException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class GlobalController {
    private static final GlobalController INSTANCE = new GlobalController();
    private DBWorker dBWorker = null;
    private SensorWorker sensorWorker = null;
    
    private GlobalController() { }
    
    public static GlobalController getInstance() {
        return INSTANCE;
    }
    
    /**
     * @param userName - user name for database
     * @param password - password
     */
    public boolean connectToDataBase(String userName, String password) {
        boolean stateB = false;
        dBWorker = new DBWorker(userName, password);
        try {
            dBWorker.openConnection();
            stateB = true;
        } catch (SQLException ex) {
            Logger.getLogger(GlobalController.class.getName()).log(Level.SEVERE, null, ex);
            System.out.println("Error when connecting to the database");
        }
        return stateB;
    }
    
    public boolean initializeOneWireAdapter() {
        boolean state = false;
        try {
            sensorWorker = new SensorWorker();
            state = true;
        } catch (OneWireException ex) {
            Logger.getLogger(GlobalController.class.getName()).log(Level.SEVERE, null, ex);
            System.out.println("1-wire adapter not found");
        }
        return state;
    }
    
    /**
     @return - list of room type. Top-level list stored id typeOfRoom and name type room
                                 <id, typeOfRoom>
    */
    public List<List<String>> getListRoom() {
        return dBWorker.getTypeRoomList();
    }
    
    public List<List<String>> getAllSensorList() {
        return dBWorker.getAllSensorList();
    }
    
    public Map<String, Double> getCurrentTemperature() {
        Map<String, Double> tmpArray = new HashMap<>();
        try {
            tmpArray = sensorWorker.getTemperature();
        } catch (OneWireException ex) {
            Logger.getLogger(GlobalController.class.getName()).log(Level.SEVERE, null, ex);
            System.out.println("Error while reading temperature from adapter");
            tmpArray = null;
        }
        return tmpArray;
    }
    
    public boolean updateSensorDescription(String sensorId, String newDescription ) {
        return dBWorker.updateSensorDescription(sensorId, newDescription);
    }

    @Override
    protected Object clone() throws CloneNotSupportedException {
        return super.clone(); //To change body of generated methods, choose Tools | Templates.
    }
    public void insertNewSensor(String sensorId) {
        dBWorker.insertNewSensor(sensorId);
    }
    
    public void insertNewTemperatureValue(Map<String, String> newValues) {
        dBWorker.insertTemperatureValues(newValues);
    }
    
    public void insertNewRoomType(String roomType) {
        dBWorker.insertNewRoomeType(roomType);
    }
    
    public void deleteRoomType(String roomType) {
        dBWorker.deleteRoomeType(roomType);
    }
}
