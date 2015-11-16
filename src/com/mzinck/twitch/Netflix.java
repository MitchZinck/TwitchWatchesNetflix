package com.mzinck.twitch;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;

import javax.imageio.ImageIO;

import com.mzinck.twitch.irc.IRC;
import com.mzinck.twitch.json.ApiReader;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.GridPane;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

public class Netflix extends Application {

    public static Image   poster      = null;
    public static String  nft         = null;
    public static String  netflixDesc = null;
    public static String  title       = null;
    public static String  isShow      = "FALSE";
    public static int     countdown   = 0;
    public static State   state       = State.VOTING;
    public static boolean start       = true;

    public String         voting      = "";
    public String         topVote     = "";
    public int            timestamp   = 0;


    public static void main(String[] args) throws UnknownHostException, IOException {
        IRC irc = new IRC();
        new Thread(irc).start();
        setNewNetflix("Breaking Bad");
        launch(args);
    }

    public static void setNewNetflix(String netflix) {
        Map<String, String> nflx = ApiReader.netflixInfo(netflix.replace(" ", "+"));
        nft = netflix;
        title = netflix;
        nft += " (" + nflx.get("Released") + ") (" + nflx.get("imdbRating") + " Rating)";
        netflixDesc = nflx.get("Plot") + "\nActors: " + nflx.get("Actors");
        if(start == false && (nflx.get("Type") != null)) {
            isShow = nflx.get("Type").equals("series") ? "TRUE" : "FALSE";
        }
        start = false;
        
        BufferedImage image;
        URL url = null;
        try {
            url = new URL(nflx.get("Poster"));
            image = ImageIO.read(url);
            setImage(image);
        } catch (IOException e) {
            
        }
    }
    
    public static void setImage(BufferedImage image) {
        WritableImage wr = null;
        if (image != null) {
            wr = new WritableImage(image.getWidth(), image.getHeight());
            PixelWriter pw = wr.getPixelWriter();
            for (int x = 0; x < image.getWidth(); x++) {
                for (int y = 0; y < image.getHeight(); y++) {
                    pw.setArgb(x, y, image.getRGB(x, y));
                }
            }
        }
        
        poster = wr;
    }
    
    public static void setStage(Stage stage, Scene scene) {
        stage.setScene(scene);
        stage.show();
    }
    
    public String secondsToTimer(int seconds) {
        return String.format("%02d:%02d", seconds / 60, seconds % 60);
    }
    
    public void start(Stage primaryStage) {
        primaryStage.setTitle("TwitchWatchesNetflix");

        GridPane grid = new GridPane();
        grid.setAlignment(Pos.TOP_LEFT);
        grid.setHgap(10);
        grid.setPadding(new Insets(25, 25, 25, 25));

        Text nfTitle = new Text(nft);
        nfTitle.setFont(Font.font("Roboto", FontWeight.NORMAL, 30));
        grid.add(nfTitle, 1, 0);

        Label nfDesc = new Label(netflixDesc);
        nfDesc.setWrapText(true);
        nfDesc.setPrefWidth(500);
        nfDesc.setFont(Font.font("Roboto", FontWeight.NORMAL, 12));
        GridPane.setHalignment(nfDesc, HPos.LEFT);
        grid.add(nfDesc, 1, 1);
        
        Label voteLabel= new Label("");
        voteLabel.setFont(Font.font("Roboto", FontWeight.NORMAL, 24));
        GridPane.setHalignment(voteLabel, HPos.LEFT);
        grid.add(voteLabel, 0, 2);
        
        Label cdTimer= new Label("Vote Timer: ");
        cdTimer.setFont(Font.font("Roboto", FontWeight.NORMAL, 24));
        GridPane.setHalignment(cdTimer, HPos.LEFT);
        grid.add(cdTimer, 1, 2);

        ImageView nfPoster = new ImageView(poster);
        grid.add(nfPoster, 0, 0, 1, 2);

        Scene scene = new Scene(grid, 1280, 720);
        primaryStage.setScene(scene);

        primaryStage.show();
        primaryStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent t) {
                Platform.exit();
                System.exit(0);
            }
        });
        
        new Thread() {
            public void run() {
                while(true) {
                    System.out.println(state);
                    switch (state) {
                        
                        
                        case PLAYING:
                            countdown = 0;
                            timestamp = 0;
                            String rt = null;
                            rt = ApiReader.netflixInfo(title.replace(" ", "+")).get("Runtime");
                            
                            if(!nft.contains("null")) {
                                countdown = Integer.parseInt(rt.substring(0, 2).replace(" ", "")) * 60;
                            } else {
                                state = com.mzinck.twitch.State.VOTING;
                                countdown = 1;
                                isShow = "FALSE";
                                break;
                            }
                            while(countdown > 0) {
                                try {
                                    Thread.sleep(1000);
                                } catch (InterruptedException e) {
                                    // TODO Auto-generated catch block
                                    e.printStackTrace();
                                }
                                Platform.runLater(new Runnable() {
                                    public void run() {
                                        voteLabel.setText(voting);
                                        cdTimer.setText("Run Time: " + secondsToTimer(timestamp) + "/" +secondsToTimer(countdown + timestamp));
                                    }
                                });
                                countdown--;
                                timestamp++;
                            }
                            
                            isShow = "FALSE";
                            state = com.mzinck.twitch.State.VOTING;
                        break;

                        case VOTING:
                            countdown = 500;
                            new Thread() {
                                public void run() {
                                    topVote = "";
                                    IRC.map.clear();
                                    while (countdown > 0) {
                                        try {
                                            Thread.sleep(1000);
                                        } catch (InterruptedException e) {
                                            // TODO Auto-generated catch block
                                            e.printStackTrace();
                                        }

                                        Map<String, Integer> map = new HashMap<String, Integer>();
                                        for (String value : IRC.map.values()) {
                                            if (map.containsKey(value)) {
                                                map.put(value, map.get(value) + 1);
                                            } else {
                                                map.put(value, 1);
                                            }
                                        }

                                        String[][] array = new String[5][2]; //Array of top 5 votes

                                        int counter = 0;
                                        for (String value : map.keySet()) {
                                            if (counter < 5) { //Wait for first 5 spots to fill up
                                                array[counter][0] = value; //Add the vote to the first index of the array
                                                array[counter][1] = Integer.toString(map.get(value)); //Add the vote count to the second index
                                                counter++;
                                            } else {
                                                for (int i = 0; i < array.length; i++) { //Go through the array
                                                    if (Integer.parseInt(array[i][1]) < map.get(value)) { //If the highest vote is less than the map value 
                                                                                                          //then we put the map value in front of it and set everything back a index
                                                        for (int y = array.length - 1; y > i; y--) { //Go backwards through the array up to the point we last checked
                                                            array[y][0] = array[y - 1][0]; // Move the index back one
                                                            array[y][1] = array[y - 1][1];
                                                        }
                                                        array[i][0] = value; //Insert the new value 
                                                        array[i][1] = Integer.toString(map.get(value));
                                                        break;
                                                    }
                                                }
                                            }
                                        }                                      

                                        voting = "VOTING IN PROGRESS\n";
                                        for (int z = 0; z < array.length; z++) { 
                                            if(array[z][0] != null && !array[z][0].contains("null")) {
                                                voting += array[z][0] + ": " + array[z][1] + "\n";
                                            }
                                        }
                                        topVote = array[0][0];  

                                        Platform.runLater(new Runnable() {
                                            public void run() {
                                                voteLabel.setText(voting);
                                                if(isShow.equals("FALSE")) {
                                                    cdTimer.setText("Vote Time: " + countdown + "\nType \"!vote yourpreferencehere");
                                                } else {
                                                    cdTimer.setText("Vote Time: " + countdown + "\nType \"!vote S0E0");
                                                }
                                            }
                                        });
                                        
                                        countdown--;
                                    }
                                    System.out.println("Votes have been tallied: " + topVote + " = Winner");
                                    voting = "PREVIOUS VOTE" + voting.replace("VOTING IN PROGRESS\n", "\n");
                                    if(topVote == null) {
                                        topVote = "null";
                                    }                                    
                                    
                                    if(isShow.equals("FALSE")) {
                                        setNewNetflix(topVote);
                                        if(isShow.equals("FALSE")) {
                                            state = com.mzinck.twitch.State.PLAYING;   
                                        }
                                    } else if(isShow.equals("TRUE")){
                                        nft = nft + "\n" + topVote;
                                       state = com.mzinck.twitch.State.PLAYING;
                                    }
                                    
                                    Platform.runLater(new Runnable() {
                                        public void run() {
                                            nfTitle.setText(nft);
                                            nfDesc.setText(netflixDesc);
                                            nfPoster.setImage(poster);
                                            
                                        }
                                    });                                    
                                }
                            }.start();
                        break;
                            
                    }
                    
                    try {
                        Thread.sleep(countdown * 1100);
                    } catch (InterruptedException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }
            }
        }.start();                 
    }
    
    public static void setTime(int time) {
        countdown = time * 60;
    }

}
