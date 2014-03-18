package temperaturecontroller;

import com.dalsemi.onewire.OneWireException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
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
     * @return - returns the state of the connection
     */
    public boolean connectToDataBase(String userName, String password) {
        dBWorker = new DBWorker(userName, password);
        while(!conectToDb) {
            try {
                dBWorker.openConnection();
                conectToDb = true;
                
            } catch (SQLException ex) {
                Logger.getLogger(GlobalController.class.getName()).log(Level.SEVERE, null, ex);
                System.out.println("Error when connecting to the database");
                System.out.println("Repeated attempts to connect will be made through 5 seconds");
                conectToDb = false;
                try {
                    TimeUnit.SECONDS.sleep(5);
                } catch (InterruptedException ex1) {
                    Logger.getLogger(GlobalController.class.getName()).log(Level.SEVERE, null, ex1);
                }
            }
        }
        
        return conectToDb;
    }
    
    public boolean initializeOneWireAdapter() {
        try {
            sensorWorker = new SensorWorker();
            connectToAdapter = true;
        } catch (OneWireException ex) {
            Logger.getLogger(GlobalController.class.getName()).log(Level.SEVERE, null, ex);
            System.out.println("1-wire adapter not found");
            connectToAdapter = false;
        }
        return connectToAdapter;
    }
    
    /**
     *@return - list of room type. Top-level list stored id typeOfRoom and name type room
     *                            <id, typeOfRoom>
     */
    public List<List<String>> getListRoom() {
        if(conectToDb) {
            return dBWorker.getTypeRoomList();
        }
        else return null;
    }
    
    public List<List<String>> getAllSensorList() {
        if(conectToDb) {
            return dBWorker.getAllSensorList();
        }
        else return null;
    }
    
    public Map<String, Double> getCurrentTemperature() {
        Map<String, Double> tmpArray = new HashMap<>();
        if(connectToAdapter) {
            try {
                tmpArray = sensorWorker.getTemperature();
            } catch (OneWireException ex) {
                Logger.getLogger(GlobalController.class.getName()).log(Level.SEVERE, null, ex);
                System.out.println("Error while reading temperature from adapter");
                tmpArray = null;
            }
        }
        return tmpArray;
    }
    
    public boolean updateSensorDescription(String sensorId, String newDescription ) {
        boolean state = false;
        if(conectToDb) {
            try {
                state = dBWorker.updateSensorDescription(sensorId, newDescription);
            } catch (SQLException ex) {
                Logger.getLogger(GlobalController.class.getName()).log(Level.SEVERE, null, ex);
                chechDataBaseState();
                state = false;
            }
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
    
    public boolean insertNewTemperatureValue(Map<String, String> newValues) {
        boolean state = false;
        if(conectToDb) {
            try {
                dBWorker.insertTemperatureValues(newValues);
                state = true;
            } catch (SQLException ex) {
                Logger.getLogger(GlobalController.class.getName()).log(Level.SEVERE, null, ex);
                chechDataBaseState();
            }
        }
        return state;
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
        System.out.println("database is avialable? - " + conectToDb);   
    }
    
    public boolean getConnectDbState() {
        return conectToDb;
    }
    
    public boolean getConnectToAdapterState() {
        return connectToAdapter;
    }
}
