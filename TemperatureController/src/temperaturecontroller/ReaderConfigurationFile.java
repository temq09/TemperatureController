/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package temperaturecontroller;

import java.io.File;
import java.io.IOException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 *
 * @author Артем
 */
public class ReaderConfigurationFile {
    
    private NodeList _nodeList = null;
    private Document _document;
    
    public ReaderConfigurationFile(String pathToFile) throws ReadConfigurationException{
        try {
            File xmlFile = new File(pathToFile);
            DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder documentBuilder = docBuilderFactory.newDocumentBuilder();
            _document = documentBuilder.parse(xmlFile);
            //_document.getDocumentElement().normalize();
            System.out.println("Открытие файла настроек завершилось удачно."
                    + "Корневой элемент - " + _document.getDocumentElement().getNodeName());
        } catch( ParserConfigurationException | SAXException | IOException exception) {
            System.out.println(exception.getMessage());
            throw new ReadConfigurationException("Ошибка при чтении файла конфигурации");
        }
    }
    
    public void setNodeList(String tag) {
        _nodeList = _document.getElementsByTagName(tag);;
    }
    
    public String getNodeAttribute(String tag) {
        String returnValue = new String();
        if(_nodeList != null) {
            Node node = _nodeList.item(0);
            if(node.getNodeType() == Node.ELEMENT_NODE) {
                Element element = (Element)node;
                returnValue = element.getElementsByTagName(tag).toString();
            }
        }       
        return returnValue;
    }
    
}
