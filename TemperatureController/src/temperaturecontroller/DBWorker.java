package temperaturecontroller;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
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
    public static final String SERVER_NAME = "localhost";
    public static final String DB_NAME = "smart_house";
    
    private String _userName;
    private String _password;
    private String _connectiString;
    private Connection _connection;
    
    private DBWorker(){};
    
    public DBWorker(String userName, String password) {
        _userName = userName;
        _password = password;
        _connectiString = "jdbc:mysql://" + SERVER_NAME + "/" + DB_NAME;
    }
    
    public void openConnection() throws SQLException {
        _connection = DriverManager.getConnection(_connectiString, _userName, _password);
        System.out.println("Connect to data base is success");
    }
    
    public List<List<String>> getTypeRoomList() {
        return queryHandler("Select * from type_room");
    }
    
    public List<List<String>> getAllSensorList() {
        return queryHandler("select sensor_descriptions.id, id_sensor, description, room_type" +
                             " from sensor_descriptions" + 
                             " left join type_room" + 
                             " ON sensor_descriptions.type_of_room_id = type_room.id");
    }
    
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
    
    public void insertNewSensor(String idSensor) 
            throws SQLException {
        updateQuery("insert into sensor_descriptions values (null , '"+ idSensor + "', 'Unknown sensor', null)");
    }
    
    public void insertTemperatureValues(Map<String, String> newValues) 
            throws SQLException {
        for(Entry<String, String> entry : newValues.entrySet()) {
            updateQuery("insert into sensor_values values(null, now(), " 
                    + Float.parseFloat(entry.getKey()) + ", " 
                    + Integer.parseInt(entry.getValue()) + ")" );
        }
    }
    
    public void insertNewRoomeType(String roomType) 
            throws SQLException {
        updateQuery("insert into type_room values (null, '" + roomType + "')");
    }
    
    public void deleteRoomeType(String roomType) 
            throws SQLException {
        updateQuery("delete from type_room where type_room.room_type = '" + roomType + "'");
    }
    
    public void updateRoomType(String idRoomType, String idSensor) throws SQLException {
        updateQuery("update sensor_descriptions " 
                + "set type_of_room_id = " + idRoomType 
                + " where sensor_descriptions.id = " + idSensor);
    }
    
    public boolean updateSensorDescription(String sensorId, String newDescription) 
            throws SQLException {
        boolean queryIsSuccess = false;
        queryIsSuccess = updateQuery("update sensor_descriptions "
                + "set description = '" + newDescription 
                + "' where sensor_descriptions.id_sensor = '" + sensorId + "'");
        return queryIsSuccess;
    }
    
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
    
    public boolean getConnectionState() throws SQLException {
        if(_connection != null)
            return _connection.isValid(50);
        else return false;
    }
    
}
