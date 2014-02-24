package temperaturecontroller;

import java.util.List;
import java.util.Map;

public class GlobalController {
    private static final GlobalController INSTANCE = new GlobalController();
    private DBWorker dBWorker = null;
    private SensorWorker sensorWorker = null;
    
    private GlobalController() { }
    
    public static GlobalController getInstance() {
        return INSTANCE;
    }
    
    public boolean connectToDataBase(String userName, String password) {
        dBWorker = new DBWorker(userName, password);
        boolean connectionState = dBWorker.openConnection();
        sensorWorker = new SensorWorker();
        return connectionState;
    }
    
    
    public List<List<String>> getListRoom() {
        return dBWorker.getTypeRoomList();
    }
    
    public List<List<String>> getAllSensorList() {
        return dBWorker.getAllSensorList();
    }
    
    public Map<String, Double> getCurrentTemperature() {
        return sensorWorker.getTemperature();
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
