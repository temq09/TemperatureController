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
    private final String FILE_NAME = "configFile.xml";
    private final String LOGIN = "login";
    private final String PASSWORD = "password";
    private final String SERVER_NAME = "server_name";
    private final String DATABASE_NAME = "database_name";
    private final String ROOT_ELEMENT = "database";
    
    private DBWorker dBWorker = null;
    private SensorWorker sensorWorker = null;
    private boolean conectToDb = false;
    private boolean connectToAdapter = false;
    private String _loginName = new String();
    private String _password = new String();
    private String _serverName = new String();
    private String _databaseName = new String();
    
    private GlobalController() {
        readSetting();
    }
    
    public static GlobalController getInstance() {
        return INSTANCE;
    }
    
    private void readSetting() {
        ReaderConfigurationFile readerConfigurationFile = null;
        try {
            readerConfigurationFile = new ReaderConfigurationFile(FILE_NAME);
            readerConfigurationFile.setNodeList(ROOT_ELEMENT);
        } catch (ReadConfigurationException ex) {
            Logger.getLogger(GlobalController.class.getName()).log(Level.SEVERE, null, ex);
            System.out.println(ex.getMessage());
        }
        if(readerConfigurationFile != null) {
            _loginName = readerConfigurationFile.getNodeAttribute(LOGIN);
            _password = readerConfigurationFile.getNodeAttribute(PASSWORD);
            _databaseName = readerConfigurationFile.getNodeAttribute(DATABASE_NAME);
            _serverName = readerConfigurationFile.getNodeAttribute(SERVER_NAME);
            dBWorker = new DBWorker(_loginName, _password, _serverName, _databaseName);
        }
    }
    
    /**
     * @param userName - user name for database
     * @param password - password
     * @return - returns the state of the connection
     */
    public boolean connectToDataBase() {
        try {
            dBWorker.openConnection();
            conectToDb = true;
        } catch (SQLException ex) {
            Logger.getLogger(GlobalController.class.getName()).log(Level.SEVERE, null, ex);
            System.out.println("Error when connecting to the database");
            conectToDb = false;
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
    
    public void updateRoomType(String idRoomType, String idSensor) {
        try {
            dBWorker.updateRoomType(idRoomType, idSensor);
        } catch (SQLException ex) {
            System.out.println("Error updating type");
            chechDataBaseState();
        }
        
    }
    
    public void deleteSensorFromDB(String idSensor){
        try {
            dBWorker.deleteSensorFromDB(idSensor);
        } catch (SQLException ex) {
            Logger.getLogger(GlobalController.class.getName()).log(Level.SEVERE, null, ex);
            chechDataBaseState();
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
