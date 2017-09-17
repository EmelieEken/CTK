/*
Program displaying information about the different bike stations in Gothenburg. Calls the SelfServiceBicycleService webserver
to retrieve information about the stations.
The user can select a desired station name from a drop-down list (sorted with raising station id) and that stations id, open or closed status,
available bikes, number of bikestands, position and when the information was last updated is displayed.

To compile in terminal:
    javac BikeStationsDisplay.java
To run:
    java BikeStationsDisplay
*/

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.LinkedList;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamReader;
import java.util.*;
import java.io.*;

/*
Class setting up the display
*/
public class BikeStationsDisplay {

    private BikeStations stations; //Stores information about all bike stations

    //To select a particular station
    private JComboBox<String> stationChoise; //Dropdown list of all station names

    //For display 
    private JLabel stationIdLabel = new JLabel("StationId: ");
    private JLabel isOpenLabel = new JLabel("Status: NA");
    private JLabel bikeStandLabel = new JLabel("Number of bike stands: ");
    private JLabel latsAndLongs = new JLabel("Latitude - Longitude: ");
    private JLabel availableBikesLabel = new JLabel("Available bikes: ");
    private JLabel updateLabel = new JLabel("Last updated: ");

    //Constructor
    public BikeStationsDisplay(String xmlString) {
        stations = new BikeStations(xmlString);
        stationChoise = new JComboBox<String>(stations.names.toArray(new String[stations.names.size()]));

        //Set itemlistener to detect which bikestation selected by user
        stationChoise.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent event) {
                if (event.getStateChange() == ItemEvent.SELECTED) { //If item selected from list
                    String selectedName = (String) event.getItem(); //Get selected name
                    fillFields(selectedName);
                }
            }
        });

    }
    /*
    Main function, requests data from the remote server
    */
    public static void main(String[] args) { 
        //Set up connection to the remote server and make a request for data in the form of XML 
        HttpURLConnection connection = null;
        String key = "e4a39ce4-4c88-4494-9eac-15726f63ef79"; //Application id
        String serverURL = "http://data.goteborg.se/SelfServiceBicycleService/v1.0/Stations/"+ key +"?getclosingperiods=true&format=Xml";

        try {
            //Open up a connection to the server. Ask user for application key to allow access to server
            URL url = new URL(serverURL);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");

            //Get response
            BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            StringBuilder xmlResponse = new StringBuilder();
            String partialResponse;
            while ((partialResponse = br.readLine()) != null) {
                xmlResponse.append(partialResponse);
            }
            br.close();
            String totalResponse = xmlResponse.toString(); //Convert to one single string

            //Save received information about the stations
            BikeStationsDisplay display = new BikeStationsDisplay(totalResponse); 

            display.openUserWindow(); //Set up and show display to user with a selectable drop-down list with station names

        } catch (Exception e){
            System.out.println("Error when setting up connection and reading response");
            e.printStackTrace();
            System.exit(1);
        }
    }
    /*
    Set up display window with information about the selected station
    */
    private void openUserWindow() {

        //Create main frame
        JFrame frame = new JFrame("Bike Stations");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(450, 350); //Set width and height of frame

        //Create main panel to place in frame
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.PAGE_AXIS));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(30,30,30,30)); //Create padding
        JPanel namePanel = new JPanel();
        JPanel infoPanel = new JPanel(new GridLayout(0, 1, 0, 10)); //Create vertical layout
        infoPanel.setBorder(BorderFactory.createEmptyBorder(30,0,30,0));
        JLabel namesLabel = new JLabel("Station Names");
        namePanel.add(namesLabel);
        namePanel.add(stationChoise);
        namePanel.setAlignmentX(Box.CENTER_ALIGNMENT); //Center station name list and last updated label
        updateLabel.setAlignmentX(Box.CENTER_ALIGNMENT);

        infoPanel.add(stationIdLabel);
        infoPanel.add(isOpenLabel);
        infoPanel.add(bikeStandLabel);
        infoPanel.add(availableBikesLabel);
        infoPanel.add(latsAndLongs);

        mainPanel.add(namePanel);
        mainPanel.add(infoPanel);
        mainPanel.add(updateLabel);

        //Add panel to frame and make frame visible
        frame.getContentPane().add(mainPanel);
        frame.setVisible(true);

        if (stations.names.size() > 0) { //Check if any entry in names list
            fillFields(stations.names.get(0)); //Set displayed fields to match the first name in the list
        }
    }

    /* 
    Fill fields in window with info about the selected station specified by name.
    */
    private void fillFields(String name) { 
        BikeStation selectedStation = stations.getStation(name);
        if (selectedStation != null) {
            stationIdLabel.setText("StationId: " + selectedStation.station_stationId);
            bikeStandLabel.setText("Number of bike stands: " + selectedStation.station_bikeStands);
            latsAndLongs.setText("Latitude - Longitude: " + selectedStation.station_lat + " - " + selectedStation.station_long);
            availableBikesLabel.setText("Available bikes: " + selectedStation.station_availableBikes);
            updateLabel.setText("Last updated: " + selectedStation.station_lastUpdate);
            //Display NA/OPEN/CLOSED in stead of "", true or false
            if (selectedStation.station_isOpen == "NA") {
                isOpenLabel.setText("Status: NA");
            } else if (Boolean.parseBoolean(selectedStation.station_isOpen)) {
                isOpenLabel.setText("Status: OPEN");
            } else {
                isOpenLabel.setText("Status: CLOSED");
            } 
        }
    }

    /* 
    Class to save information received from the webserver: name, station id, latitude, longitude, if open, bikestands, 
    available bikes and when the information was last updated.
    */
    private class BikeStations {

        List<String> names = new ArrayList<String>();
        List<String> stationIds = new ArrayList<String>();
        List<String> lats = new ArrayList<String>();
        List<String> longs = new ArrayList<String>();
        List<String> isOpens = new ArrayList<String>();
        List<String> bikeStands = new ArrayList<String>();
        List<String> availableBikes = new ArrayList<String>();
        List<String> lastUpdates = new ArrayList<String>();

        private BikeStation[] stationsArray; //Used to send information to display

        //Constructor
        public BikeStations(String xmlString) {

            //Read the XML and fill in the property lists
            parseXMLToBikeStations(xmlString);

            //Create array of individual BikeStation objects that later are used to pass information to the fillFields function
            stationsArray = new BikeStation[names.size()];
            for (int i = 0; i<names.size(); i++) {
                stationsArray[i] = new BikeStation(names.get(i), stationIds.get(i), lats.get(i), longs.get(i), 
                    isOpens.get(i), bikeStands.get(i), availableBikes.get(i), lastUpdates.get(i));
            }       
        }
        //Return the BikeStation object with the name specified by the parameter name. If name doesn't exist, return null.
        private BikeStation getStation(String name) {
            int index = names.indexOf(name); //Find index of name in the list names
            if (index != -1) {
                return stationsArray[index];
            } else {
                return null;
            }
        }
        //Read the received XML and save the desired information in the objects lists
        private void parseXMLToBikeStations(String totalResponse) {
            try {
                //Convert String response to a XMLStreamReader
                Reader strReader = new StringReader(totalResponse);
                XMLInputFactory factory = XMLInputFactory.newInstance();
                XMLStreamReader xmlReader = factory.createXMLStreamReader(strReader);

                List<String> currentList; //Used to sort text from XML into separate lists
                int listLength = -1; //Keep track of list length in case some stations are missing values

                //Extract information from received XML
                while (xmlReader.hasNext()) {
                    
                    if (xmlReader.isStartElement()) { //if start tag

                        //Check if current XML tag encloses desired text to save to local lists
                        String name = xmlReader.getLocalName();
                        if (name == "StationId") {
                            currentList = this.stationIds;
                            //Make a Not Available=NA entry in all list and replace if tag exists for current station
                            listLength++;
                            names.add("NA");
                            stationIds.add("NA");
                            lats.add("NA");
                            longs.add("NA");
                            isOpens.add("NA");
                            bikeStands.add("NA");
                            availableBikes.add("NA");
                            lastUpdates.add("NA");
                        }
                        else if (name == "Name") {
                            currentList = this.names;
                        }
                        else if (name == "Lat") {
                            currentList = this.lats;
                        }
                        else if (name == "Long") {
                            currentList = this.longs;
                        }
                        else if (name == "IsOpen") {
                            currentList = this.isOpens;
                        }
                        else if(name == "BikeStands") {
                            currentList = this.bikeStands;
                        }
                        else if (name == "AvailableBikes") {
                            currentList = this.availableBikes;
                        }
                        else if (name == "LastUpdate") {
                            currentList = this.lastUpdates;
                        }
                        else { //If XML tag not any of the ones we want to use
                            currentList = null;
                        }

                        if (currentList != null) {
                            while (xmlReader.hasNext()) { //Continue until text encontered = text enclosed by XML tags
                                xmlReader.next();
                                if (xmlReader.hasText()){
                                    currentList.set(listLength, xmlReader.getText()); //Add text to list
                                    break;
                                }
                            }
                        }
                    }  
                    xmlReader.next(); //Go to next
                }
                strReader.close();
                xmlReader.close();
            }
            catch (Exception e) { //Exception thrown, exit program and inform user about the problem
                System.out.println("Error parsing XML");
                e.printStackTrace();
                System.exit(1);
            }
        }       
    }
    /*
    Class used to pass information about the selected station to the fillField function. 
    Saves all information about a station as individual strings.
    Class variables are kept public to make this code shorter
    */
    private class BikeStation {
        String station_name;
        String station_stationId;
        String station_lat;
        String station_long;
        String station_isOpen;
        String station_bikeStands;
        String station_availableBikes;
        String station_lastUpdate;

        //Constructor
        public BikeStation(String name, String id, String lat, String s_long, String isOpen, String stands,
            String available, String update) {
            
            station_name = name;
            station_lat = lat;
            station_long = s_long;
            station_lastUpdate = update;
            station_stationId = id;
            station_bikeStands = stands;
            station_availableBikes = available;
            station_isOpen = isOpen;
        }
    }
}