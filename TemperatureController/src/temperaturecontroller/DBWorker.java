package temperaturecontroller;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;


public class DBWorker {
    
    public static final String DRIVER_NAME = "com.mysql.jdbc.Driver";
    //public static final String SERVER_NAME = "localhost";
    //public static final String DB_NAME = "smart_house";
    
    private final String _userName;
    private final String _password;
    private final String _connectiString;
    private Connection _connection;
    
    //private DBWorker(){};
    /**
     * 
     * @param userName - login for DataBase
     * @param password - password for DataBase
     * @param serverName - server name for DataBase (localhost or other address)
     * @param dataBaseName - data base name 
     */
    public DBWorker(String userName, String password, String serverName, String dataBaseName) {
        _userName = userName;
        _password = password;
        _connectiString = "jdbc:mysql://" + serverName + "/" + dataBaseName;
    }
    
    public void openConnection() throws SQLException {
        _connection = DriverManager.getConnection(_connectiString, _userName, _password);
        System.out.println("Connect to data base is success");
    }
    
    /**
     * Return the list of all types room.
     * @return - list of all types room
     */
    public List<List<String>> getTypeRoomList() {
        return queryHandler("Select * from type_room");
    }
    
    /**
     * Return list of all sensor for all time.
     * @return - list of all sensors which was detected for all time.
     *           This list include id, name sensor(id sensor), description and type room
     */
    public List<List<String>> getAllSensorList() {
        return queryHandler("select sensor_descriptions.id, id_sensor, description, room_type" +
                             " from sensor_descriptions" + 
                             " left join type_room" + 
                             " ON sensor_descriptions.type_of_room_id = type_room.id");
    }
    
    /**
     * Executes the query and return result.
     * @param query - query for database
     * @return - query result
     */
    public List<List<String>> queryHandler(String query) {     
        List<List<String>> resultList = new ArrayList<>();
        //Statement statement;
        try (Statement statement = _connection.createStatement(); 
                ResultSet rs = statement.executeQuery(query) ){
            
            while(rs.next()) {
                int countColumn = rs.getMetaData().getColumnCount();
                List<String> row = new ArrayList<>(countColumn);
                int i = 1;
                while(i <= countColumn) {
                    if(rs.getString(i) == null)
                        row.add(" ");
                    row.add(rs.getString(i));
                    i++;
                }
                resultList.add(row);
            }
        } catch (SQLException ex) {
            Logger.getLogger(DBWorker.class.getName()).log(Level.SEVERE, null, ex);
            System.out.println("error getting the list");
            System.out.println(ex.getMessage());
            ex.printStackTrace();
        }
        
        return resultList;        
    }
    
    /**
     * 
     * @param idSensor - Name(id sensor) of the new sensor.
     * @throws SQLException 
     */
    public void insertNewSensor(String idSensor) 
            throws SQLException {
        updateQuery("insert into sensor_descriptions values (null , '"+ idSensor + "', 'Unknown sensor', null)");
    }
    
    /**
     * Insert the new temperature values to the database.
     * @param newValues - new values of temperature <id sensor, temperature>
     * @throws SQLException 
     */
    public void insertTemperatureValues(Map<String, String> newValues) 
            throws SQLException {
        for(Entry<String, String> entry : newValues.entrySet()) {
            updateQuery("insert into sensor_values values(null, now(), " 
                    + Float.parseFloat(entry.getKey()) + ", " 
                    + Integer.parseInt(entry.getValue()) + ")" );
        }
    }
    
    /**
     * Insert the new room type to the database.
     * @param roomType - name of new room type
     * @throws SQLException 
     */
    public void insertNewRoomeType(String roomType) 
            throws SQLException {
        updateQuery("insert into type_room values (null, '" + roomType + "')");
    }
    
    /**
     * Delete the room type
     * @param roomType - name of room type which need delete
     * @throws SQLException 
     */
    public void deleteRoomeType(String roomType) 
            throws SQLException {
        updateQuery("delete from type_room where type_room.room_type = '" + roomType + "'");
    }
    
    /**
     * Update the room type at the sensor.
     * @param idRoomType - id room type
     * @param idSensor - id sensor
     * @throws SQLException 
     */
    public void updateRoomType(String idRoomType, String idSensor) throws SQLException {
        updateQuery("update sensor_descriptions " 
                + "set type_of_room_id = " + idRoomType 
                + " where sensor_descriptions.id = " + idSensor);
    }
    
    /**
     * 
     * @param sensorId - Update the description of the sensor.
     * @param newDescription - new sensor description
     * @return - return true if query is successeful
     * @throws SQLException 
     */
    public boolean updateSensorDescription(String sensorId, String newDescription) 
            throws SQLException {
        boolean queryIsSuccess = false;
        queryIsSuccess = updateQuery("update sensor_descriptions "
                + "set description = '" + newDescription 
                + "' where sensor_descriptions.id_sensor = '" + sensorId + "'");
        return queryIsSuccess;
    }
    
    /**
     * Delete sensor. Together whith the sensor will be deleted temperature values for this sensor.
     * @param idSensor - id sensor which need delete. 
     * @throws SQLException 
     */
    public void deleteSensorFromDB(String idSensor) throws SQLException {
        _connection.setAutoCommit(false);
        try {
            updateQuery("delete from sensor_values where id_sensor = " + idSensor);
            updateQuery("delete from sensor_descriptions where id = " + idSensor);
            _connection.commit();
        }
        catch(SQLException exception) {
            _connection.rollback();
            System.out.println("When you remove the temperature from "
                    + "the database error occurred. /n" 
                    + exception.getMessage());
            throw new SQLException("delete sensor failed: " + exception.getMessage());
        }
        finally {
            _connection.setAutoCommit(true);
        }
    }
    
    /**
     * Executes the query
     * @param query - query to the database.
     * @return - return true if query is successeful
     * @throws SQLException 
     */
    private boolean updateQuery(String query) throws SQLException {
        boolean queryIsSuccess = false;
        try(PreparedStatement state = _connection.prepareStatement(query))
        {
            state.executeUpdate();
            System.out.println("query is successeful");
            queryIsSuccess = true;
        } catch (SQLException ex) {
            Logger.getLogger(DBWorker.class.getName()).log(Level.SEVERE, null, ex);
            System.out.println("query is NOT successeful");
            System.out.println(ex.getMessage());
            ex.printStackTrace();
            queryIsSuccess = false;
            throw new SQLException();
        }
        return queryIsSuccess;
    }
    
    /**
     * Check the connection state
     * @return - true if connection is open.
     * @throws SQLException 
     */
    public boolean getConnectionState() throws SQLException {
        if(_connection != null)
            return _connection.isValid(50);
        else return false;
    }
    
}
