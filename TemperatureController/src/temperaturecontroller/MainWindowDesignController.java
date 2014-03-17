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
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.FocusModel;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
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
    
    private final int THIRTY_SEC = 30_000;
    private final int FORTY_FIVE_SEC = 45_000;
    private final int SIXTY_SEC = 60_000;
    
    private int _updateTime;
    
    private static boolean isWork = true;
    
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
    private ComboBox<String> btn_menuAccuracy;
    @FXML
    private ComboBox<String> btn_menuTime;
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
    @FXML
    private Button btn_setRoomType;
    @FXML
    private Button btn_deleteSensor;
    
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
      
        btn_menuTime.getItems().addAll("30", "45", "60");
        btn_menuTime.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent e) {
                switch(btn_menuTime.getValue()) 
                {
                    case "30": System.out.println("Изменяем врямя обновления температуры на 30 секунд");
                        _updateTime = THIRTY_SEC;
                        break;
                    case "45": System.out.println("Изменяем время обновления температуры на 45 секунд");
                        _updateTime = FORTY_FIVE_SEC;
                        break;
                    case "60": System.out.println("Изменяем время обновления температуры на 60 секунд");
                        _updateTime = SIXTY_SEC;
                        break;
                    default : System.out.println("Неправильный выбор");
                }
            }
            
        });
        btn_menuTime.setValue("30");
        
        btn_setRoomType.setOnAction(new EventHandler<ActionEvent>() {

            @Override
            public void handle(ActionEvent t) {
                if(tv_allSensorTable.getSelectionModel().getSelectedItem() != null) {
                    System.out.println(tv_allSensorTable.getSelectionModel().getSelectedItem().getId());
                    setRoomTypeForSendor(tv_allSensorTable.getSelectionModel().getSelectedItem().getId(), 
                            tv_allSensorTable.getSelectionModel().getSelectedItem().getIdSensor(),
                            tv_allSensorTable.getSelectionModel().getSelectedItem().getRoomType());
                }
            }
        });
        
        btn_deleteSensor.setOnAction(new EventHandler<ActionEvent>() {

            @Override
            public void handle(ActionEvent t) {
                if(tv_allSensorTable.getSelectionModel().getSelectedItem() != null) {
                    deleteSensorFromDB(tv_allSensorTable.getSelectionModel().getSelectedItem().getId());
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
                        Thread.sleep(_updateTime);
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
                while (!_globalController.connectToDataBase() && isWork) {
                    
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
        for (List<String> obj : tmpRoomList) {
            System.out.println(obj.get(0) + " " + obj.get(1));
            /* добавляем в карту значения ид и типа комнаты */
            _roomList.put(obj.get(1), obj.get(0));
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
    
    @FXML
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
    
    @FXML
    public void deleteCurrentRoomType() {
        FocusModel<String> currentItem = lv_listOfRoom.getFocusModel();
        String roomType = currentItem.getFocusedItem();
        if(!roomType.isEmpty()) {
            System.out.println("Удаляем тип комнаты - " + roomType);
            _globalController.deleteRoomType(roomType);
            loadRoomList();
        }  
    }
    
    @FXML
    public void editDescriptionSensor(){
        
    }
    
    public static void destroyApplication() throws InterruptedException {
        isWork = false;
    }
    
    public void setRoomTypeForSendor(String id, String idSensor, String oldRoomType) {
        final Stage dialog = new Stage();
        dialog.initStyle(StageStyle.DECORATED);
        dialog.initModality(Modality.APPLICATION_MODAL);
        
        VBox descriptionLabelLayout = new VBox();
        Label lb_idSensorLabel = new Label("Ид сенсора");
        Label lb_idSendor = new Label(idSensor);
        descriptionLabelLayout.getChildren().addAll(lb_idSensorLabel, lb_idSendor);
        descriptionLabelLayout.setSpacing(5);
        
        VBox roomTypeLayout = new VBox();
        Label lb_roomType = new Label("Тип комнаты");
        final ComboBox<String> cb_roomTypeBox = new ComboBox<>();
        cb_roomTypeBox.setItems(_roomType);
        cb_roomTypeBox.setValue(oldRoomType);
        roomTypeLayout.getChildren().addAll(lb_roomType, cb_roomTypeBox);
        roomTypeLayout.setSpacing(5);
        
        HBox descriptionAndRoomTypeLayout = new HBox();
        descriptionAndRoomTypeLayout.getChildren().addAll(descriptionLabelLayout, roomTypeLayout);
        descriptionAndRoomTypeLayout.setSpacing(10);
        
        HBox okCanselLayout = new HBox();
        Button btn_Ok = new Button("Готово");
        btn_Ok.setOnAction(new EventHandler<ActionEvent>() {

            @Override
            public void handle(ActionEvent t) {
                String newRoomType = cb_roomTypeBox.getValue();
                System.out.println(newRoomType);
                String idRoomType = _roomList.get(newRoomType);
                String idSensor = tv_allSensorTable.getSelectionModel().getSelectedItem().getId();
                if(!idRoomType.isEmpty() && !idSensor.isEmpty()) {
                    System.out.println(idRoomType);
                    _globalController.updateRoomType(idRoomType, idSensor);
                    loadSensrorDescriptions();
                }
                    
                dialog.close();
            }
        });
        Button btn_cansel = new Button("Отмена");
        btn_cansel.setOnAction(new EventHandler<ActionEvent>() {

            @Override
            public void handle(ActionEvent t) {
                dialog.close();
            }
        });
        okCanselLayout.getChildren().addAll(btn_Ok, btn_cansel);
        okCanselLayout.setSpacing(10);
        okCanselLayout.setAlignment(Pos.BOTTOM_RIGHT);
        
        VBox mainLayout = new VBox();
        mainLayout.getChildren().addAll(descriptionAndRoomTypeLayout, okCanselLayout);
        mainLayout.setSpacing(10);
        mainLayout.setAlignment(Pos.CENTER);
        
        Scene scene = new Scene(mainLayout);
        dialog.setScene(scene);
        dialog.setResizable(false);
        dialog.show();
    }
    
    public void deleteSensorFromDB(final String idSensor) {
        final Stage dialog = new Stage();
        dialog.initStyle(StageStyle.DECORATED);
        dialog.initModality(Modality.APPLICATION_MODAL);
        
        Button btn_Ok = new Button("Дa");
        btn_Ok.setOnAction(new EventHandler<ActionEvent>() {

            @Override
            public void handle(ActionEvent t) {
                System.out.println("Удаляем датчик с из бд ");
                _globalController.deleteSensorFromDB(idSensor);
                loadSensrorDescriptions();
                dialog.close();                
            }
        });
        
        Button btn_Cansel = new Button("Нет");
        btn_Cansel.setOnAction(new EventHandler<ActionEvent>() {

            @Override
            public void handle(ActionEvent t) {
                dialog.close();
            }
        });
        
        HBox btnLayout = new HBox();
        btnLayout.setSpacing(10);
        btnLayout.getChildren().addAll(btn_Ok, btn_Cansel);
        
        Label notifyLabel = new Label("При удалении датчика будет удалена информация о температуре с этого датчика. Продолжить?");
        notifyLabel.setAlignment(Pos.CENTER);
        
        VBox mainLayout = new VBox();
        mainLayout.setSpacing(10);
        mainLayout.setAlignment(Pos.BOTTOM_LEFT);
        mainLayout.getChildren().addAll(notifyLabel, btnLayout);
        
        Scene scene = new Scene(mainLayout);
        dialog.setScene(scene);
        dialog.setResizable(false);
        dialog.show();
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
