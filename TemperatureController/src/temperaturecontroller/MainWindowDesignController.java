/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package temperaturecontroller;

import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.FocusModel;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.MenuButton;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

/**
 * FXML Controller class
 *
 * @author Артем
 */
public class MainWindowDesignController implements Initializable {
    public static final String ID_SENSOR = "idSensor";
    public static final String DESCRIPTIONS = "descriptions";
    public static final String ID = "id";
    public static final String TEMPERATURE = "temperature";
    public static final String ROOM_TYPE = "roomType";
    
    public static boolean isWork = true;
    
    private GlobalController _globalController;
    
    //хранит список типов комнат в формате <id, тип комнаты>
    private Map<String, String> _roomList;
    
    //хранит список всех сенсоров обнаруженных за все время в формате 
    //<id записи, id сенсора, описание сенсора, тип комнаты >
    //private List<List<String>> _allSensorList;
    
    //хранит текущие показания температуры для сенсоров доступных в данный момент
    //в фомате <id sensora, значение температуры>
    private Map<String, Double> _temperatureValue;
    
    //хранит значения сенсоров обнаруженных за все время работы в формате 
    //<id sensor, description>
    private Map<String, String> _sensorForAllTime;
    
    //Хранит ид записи соответствующей данному сенсору
    //<id, idSensor>
    private Map<String, String> _idSensorFromDB;
    
    private ObservableList<DataModel> _sensorDescriptionList;
    private ObservableList<DataModel> _currentTemperatureList;
    private ObservableList<String> _roomType;
    
    @FXML
    private ListView<String> lv_listOfRoom;
    @FXML
    private Button btn_addTypeRoom;
    @FXML
    private Button btn_deleteTypeRoom;
    @FXML
    private MenuButton btn_menuAccuracy;
    @FXML
    private MenuButton btn_menuTime;
    @FXML
    private TableColumn<DataModel, String> table_description; 
    @FXML
    private TableView<DataModel> tv_allSensorTable;
    @FXML
    private TableColumn<DataModel, String> col_idSendor;
    @FXML
    private TableColumn<DataModel, String> col_sensorDescription;
    @FXML
    private TableColumn<DataModel, String> col_roomType;
    @FXML
    private TableColumn<DataModel, String> col_IdInMainMenu;
    @FXML
    private TableColumn<DataModel, String> col_currentTemperature;
    @FXML
    private TableView<DataModel> tv_temperatureTable;
    @FXML
    private VBox mainWindow;
    
    /**
     * Initializes the controller class.
     * @param url
     * @param rb
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // TODO
        
        _roomList = new HashMap<>();
        //_allSensorList = new ArrayList<>();
        _sensorForAllTime = new HashMap<>();
        _idSensorFromDB = new HashMap<>();
        
        col_sensorDescription.setCellFactory(TextFieldTableCell.<DataModel>forTableColumn());
        col_sensorDescription.setOnEditCommit(new EventHandler<TableColumn.CellEditEvent<DataModel, String>>() {

            @Override
            public void handle(TableColumn.CellEditEvent<DataModel, String> t) {
                //t.getTableView().getItems().get(t.getTablePosition().getRow()).setDescription(t.getNewValue());
                String sensorId = t.getTableView().getItems().get(t.getTablePosition().getRow()).getIdSensor().toString();
                String oldDescription = t.getTableView().getItems().get(t.getTablePosition().getRow()).getDescriptions();
                String oldValue = t.getOldValue();
                System.out.println(oldValue);
                System.out.println(oldDescription);
                if(_globalController.updateSensorDescription(sensorId, t.getNewValue())) {
                    loadSensrorDescriptions();
                    initializeTemperatureList();
                }
                else {
                    System.out.println("Ошибка при обновлении описания датчика");
                    t.getRowValue().setDescription(oldDescription);
                    tv_allSensorTable.getColumns().get(0).setVisible(false);
                    tv_allSensorTable.getColumns().get(0).setVisible(true);
                }
            }
            
        });
        _sensorDescriptionList = FXCollections.observableArrayList();
        _currentTemperatureList = FXCollections.observableArrayList();
        _roomType = FXCollections.observableArrayList();
        
        _globalController = GlobalController.getInstance();
        
        this.connectToDataBase();
        this.initizlizeOneWireAdapter();

        Thread thread_workThread = new Thread( new Runnable() {
            @Override
            public void run() {
                Map<String, String> temperatureValue = new HashMap<>();
                while(isWork)
                {
                    temperatureValue.clear();
                    temperatureValue = getAndShowCurrentTemperature();
                    if(temperatureValue!=null && !temperatureValue.isEmpty())
                        insertDataIntoDB(temperatureValue);
                    try {
                        Thread.sleep(2000);
                    } catch (InterruptedException ex) {
                        Logger.getLogger(MainWindowDesignController.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }
        }, "work thread");
        thread_workThread.start();
    }
    
    private void connectToDataBase() {
        Thread thread_ConnectToDataBase = new Thread ( new Runnable() {
            @Override
            public void run() {  
                while (!_globalController.connectToDataBase("root", "7581557") && isWork) {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException ex) {
                        Logger.getLogger(MainWindowDesignController.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
                
                loadRoomList();
                initializeDescriptionList();
                loadSensrorDescriptions();
            }
        } , "Connect to data base thread");
        thread_ConnectToDataBase.start();
    }
    
    private void initizlizeOneWireAdapter() {
        Thread thread_initializeOneWireSensor = new Thread( new Runnable() {
            @Override
            public void run() {
                while(!_globalController.initializeOneWireAdapter() && isWork) {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException ex) {
                        Logger.getLogger(MainWindowDesignController.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
                initializeTemperatureList();
                
            }
        }, "Initialize 1-wire adapter");
        thread_initializeOneWireSensor.start();
    }
    
    private void loadRoomList() {
        System.out.println("Загружаем список комнат");
        lv_listOfRoom.setItems(_roomType);
        List<List<String>> tmpRoomList = new ArrayList<>();
        tmpRoomList = _globalController.getListRoom();
        for(Iterator it = tmpRoomList.iterator(); it.hasNext(); )
        {
            List<String> obj = (List<String>)it.next();
            System.out.println(obj.get(0) + " " + obj.get(1));
            /* добавляем в карту значения ид и типа комнаты */
            _roomList.put(obj.get(0), obj.get(1));
            /* добавляем в лист для отображения на форме типов комнат */
            _roomType.add(obj.get(1));
        }
    }
    
    /**в этом методе из базы данных вытягиваются датчики, которые были обнаружены
     * за все время работы пограммы. Перед этим объекты содержащие информацию 
     * о сенсорах обнуляются
     */
    private void loadSensrorDescriptions() {
        System.out.println("Загружаем список датчиков");
        _sensorDescriptionList.removeAll(_sensorDescriptionList);
        List<List<String>> _allSensorList = _globalController.getAllSensorList();
        _sensorForAllTime.clear();
        _idSensorFromDB.clear();
        
        for(List<String> d : _allSensorList) {
            _sensorDescriptionList.add(new DataModel(d.get(0).toString(), 
                    d.get(1).toString(), d.get(2).toString(), 
                    d.get(3).toString(), null));
            
            _sensorForAllTime.put(d.get(1).toString(), d.get(2).toString());
            _idSensorFromDB.put(d.get(1), d.get(0));
        }
        
        //tv_allSensorTable.setItems(_sensorDescriptionList);
    }
    
    private Map<String, String> getAndShowCurrentTemperature() {
        
        /* получаем новые значения температур */
        _temperatureValue = _globalController.getCurrentTemperature();
        if(_temperatureValue == null) {
            System.out.println("No adapter or sensor.");
            return null;
        }
            
        Map<String, String> dataForInsertIntoDB = new HashMap<>();
        _currentTemperatureList.removeAll(_currentTemperatureList);
        for(Entry<String, Double> entry: _temperatureValue.entrySet()) {
            checkSensorList(entry.getKey());
            _currentTemperatureList.add(
                    new DataModel(
                            null, 
                            entry.getKey(), 
                            _sensorForAllTime.get(entry.getKey()), 
                            null, 
                            entry.getValue().toString()
                    )
            );
            dataForInsertIntoDB.put(entry.getValue().toString(),
                    _idSensorFromDB.get(entry.getKey()));
        }
        return dataForInsertIntoDB;
    }
    
    /**
     * Проверяет был ли когода либо обнаружен датчик с таким id
     * @param idSensor - ид обнаруженного сенсора, который необходимо проверить.
     */
    private void checkSensorList(String idSensor) {
        String descriptionString = null;
        /**В этом месте если данные из бд не загружены 
         * то будет считаться что таких датчков небыло никогда, 
         * хотя на самом деле данные из бд просто не были считанны
         */
        if(_globalController.getConnectDbState()){
            descriptionString = _sensorForAllTime.get(idSensor);
            if(descriptionString == null) {
                //добавляем в таблицу бд со всеми сенсорами новый сенсор
                _globalController.insertNewSensor(idSensor);
                System.out.println("добавляем");
                loadSensrorDescriptions();
            }
        }
    }
    
    private void insertDataIntoDB(Map<String, String> newValues) {
        if(!newValues.isEmpty()) {
            _globalController.insertNewTemperatureValue(newValues);
        }
    }
    
    private void initializeTemperatureList() {
        //_currentTemperatureList = FXCollections.observableArrayList();
        col_IdInMainMenu.setCellValueFactory(
                new PropertyValueFactory<DataModel, String>(ID_SENSOR));
        table_description.setCellValueFactory(
                new PropertyValueFactory<DataModel, String>(DESCRIPTIONS));
        col_currentTemperature.setCellValueFactory(
                new PropertyValueFactory<DataModel, String>(TEMPERATURE));
        tv_temperatureTable.setItems(_currentTemperatureList);
    }
    
    private void initializeDescriptionList() {
        System.out.println("Инициализация списка датчиков");
        col_idSendor.setCellValueFactory(
                new PropertyValueFactory<DataModel, String>(ID_SENSOR));
        col_sensorDescription.setCellValueFactory(
                new PropertyValueFactory<DataModel, String>(DESCRIPTIONS));
        col_roomType.setCellValueFactory(
                new PropertyValueFactory<DataModel, String>(ROOM_TYPE));
        tv_allSensorTable.setItems(_sensorDescriptionList);
    }
    
    public void addNewRoomType() {
        final Stage dialog = new Stage();
        dialog.initStyle(StageStyle.DECORATED);
        VBox box = new VBox();
        box.setAlignment(Pos.CENTER);
        HBox buttons = new HBox();
        buttons.setAlignment(Pos.CENTER);
        Button btn_ok = new Button("Ok");
        Button btn_cancel = new Button("Cancel");
        final TextField tf = new TextField();
        btn_ok.addEventHandler(MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent>() {

            @Override
            public void handle(MouseEvent t) {
                String typeRoom = tf.getText();
                if(!typeRoom.isEmpty()) {
                    _globalController.insertNewRoomType(typeRoom);
                }
                loadRoomList();
                dialog.close();
            }
        });
        btn_cancel.addEventHandler(MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent>() {

            @Override
            public void handle(MouseEvent t) {
                dialog.close();
            }
        });
        buttons.getChildren().addAll(btn_ok, btn_cancel);
        box.getChildren().addAll(new Label("Введите тип комнаты"), tf, buttons);
        Scene scene = new Scene(box);
        dialog.setScene(scene);
        dialog.show();
    }
    
    public void deleteCurrentRoomType() {
        FocusModel<String> currentItem = lv_listOfRoom.getFocusModel();
        String roomType = currentItem.getFocusedItem();
        if(!roomType.isEmpty()) {
            System.out.println("Удаляем тип комнаты - " + roomType);
            _globalController.deleteRoomType(roomType);
            loadRoomList();
        }  
    }
    
    public void editDescriptionSensor(){
        
    }
    
    public static void destroyApplication() throws InterruptedException {
        isWork = false;
    }
    
    public static class DataModel {
        private final SimpleStringProperty _idSensor;
        private final SimpleStringProperty _descriptions;
        private final SimpleStringProperty _roomType;
        private final SimpleStringProperty _id;
        private final SimpleStringProperty _temperature;
        
        private DataModel(String id, String idSensor, String descriptions, String roomType, String temperature ) {
            this._idSensor = new SimpleStringProperty(idSensor);
            this._descriptions = new SimpleStringProperty(descriptions);
            this._roomType = new SimpleStringProperty(roomType);
            this._id = new SimpleStringProperty(id);
            this._temperature = new SimpleStringProperty(temperature);
        }

        public String getIdSensor() {
            return _idSensor.get();
        }
        
        public void setIdSensor(String idSensor) {
            _idSensor.set(idSensor);
        }

        public String getDescriptions() {
            return _descriptions.get();
        }
        public void setDescription(String description) {
            _descriptions.set(description);
        }

        public String getRoomType() {
            return _roomType.get();
        }
        public void setRoomType(String roomType) {
            _roomType.set(roomType);
        }
        
        public String getId() {
            return _id.get();
        }
        public void setId(String id) {
            _id.set(id);
        }
        
        public String getTemperature() {
            return _temperature.get();
        }        
        public void setTemperature(String temperature) {
            _temperature.set(temperature);
        }
    }
    
}
