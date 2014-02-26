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
    private boolean conectToDb = false;
    private boolean connectToAdapter = false;
    
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
            conectToDb = true;
        } catch (SQLException ex) {
            Logger.getLogger(GlobalController.class.getName()).log(Level.SEVERE, null, ex);
            System.out.println("Error when connecting to the database");
            conectToDb = false;
        }
        return stateB;
    }
    
    public boolean initializeOneWireAdapter() {
        boolean state = false;
        try {
            sensorWorker = new SensorWorker();
            connectToAdapter = true;
        } catch (OneWireException ex) {
            Logger.getLogger(GlobalController.class.getName()).log(Level.SEVERE, null, ex);
            System.out.println("1-wire adapter not found");
            connectToAdapter = false;
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
        boolean state = false;
        try {
            state = dBWorker.updateSensorDescription(sensorId, newDescription);
        } catch (SQLException ex) {
            Logger.getLogger(GlobalController.class.getName()).log(Level.SEVERE, null, ex);
            chechDataBaseState();
            state = false;
        }
        return state;
    }

    @Override
    protected Object clone() throws CloneNotSupportedException {
        return super.clone(); //To change body of generated methods, choose Tools | Templates.
    }
    public void insertNewSensor(String sensorId) {
        if(conectToDb) {
            try {
                dBWorker.insertNewSensor(sensorId);
            } catch (SQLException ex) {
                Logger.getLogger(GlobalController.class.getName()).log(Level.SEVERE, null, ex);
                chechDataBaseState();
            }
        }
    }
    
    public void insertNewTemperatureValue(Map<String, String> newValues) {
        if(conectToDb) {
            try {
                dBWorker.insertTemperatureValues(newValues);
            } catch (SQLException ex) {
                Logger.getLogger(GlobalController.class.getName()).log(Level.SEVERE, null, ex);
                chechDataBaseState();
            }
        }
    }
    
    public void insertNewRoomType(String roomType) {
        if(conectToDb) {
            try {
                dBWorker.insertNewRoomeType(roomType);
            } catch (SQLException ex) {
                Logger.getLogger(GlobalController.class.getName()).log(Level.SEVERE, null, ex);
                chechDataBaseState();
            }
        }
    }
    
    public void deleteRoomType(String roomType) {
        if(conectToDb) {
            try {
                dBWorker.deleteRoomeType(roomType);
            } catch (SQLException ex) {
                Logger.getLogger(GlobalController.class.getName()).log(Level.SEVERE, null, ex);
                chechDataBaseState();
            }
        }
    }
    
    private void chechDataBaseState() {
        try {
                if(!dBWorker.getConnectionState()) {
                    conectToDb = false;
                    System.out.println("database is not available");
                }
            } catch (SQLException ex1) {
                Logger.getLogger(GlobalController.class.getName()).log(Level.SEVERE, null, ex1);
            }
    }
}
