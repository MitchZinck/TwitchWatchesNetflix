package com.mzinck.twitch;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;

import javax.imageio.ImageIO;

import org.json.JSONException;

import com.mzinck.twitch.irc.IRC;
import com.net.codeusa.NetflixRoulette;

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

    public static NetflixRoulette nflxr       = new NetflixRoulette();
    public static Image           poster      = null;
    public static String          nft         = null;
    public static String          netflixDesc = null;
    public static String          title       = null;
    
    public State                  state       = State.VOTING;
    public String                 voting      = "";
    public String                 topVote     = "";
    public int                    countdown   = 0;
    public int                    timestamp   = 0;

    public static void main(String[] args) throws UnknownHostException, IOException {
        IRC irc = new IRC();
        new Thread(irc).start();
        setNewNetflix("Breaking Bad");
        launch(args);
    }

    public static void setNewNetflix(String netflix) {
        nft = netflix;
        title = netflix;
        try {
            nft += " (" + nflxr.getMediaReleaseYear(netflix) + ") (" + nflxr.getMediaRating(netflix) + " Rating)";
            netflixDesc = nflxr.getMediaSummary(netflix) + "\nActors: " + nflxr.getMediaCast(netflix);
            
            BufferedImage image;
            URL url = null;
            try {
                url = new URL(nflxr.getMediaPoster(netflix));
                image = ImageIO.read(url);
                setImage(image);
            } catch (IOException e) {
                
            }           
        } catch (JSONException e) {
            System.out.println("Unknown Error.");
        } catch (IOException e) {
            System.out.println("Unknown movie try again.");
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
        grid.setHgap(50);
        grid.setVgap(10);
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
        voteLabel.setFont(Font.font("Roboto", FontWeight.NORMAL, 12));
        GridPane.setHalignment(voteLabel, HPos.LEFT);
        grid.add(voteLabel, 0, 2);
        
        Label cdTimer= new Label("Vote Timer: ");
        cdTimer.setFont(Font.font("Roboto", FontWeight.NORMAL, 12));
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
                            String s = null;
                            try {
                                s = nflxr.getAllData(title);
                            } catch (JSONException e1) {
                                // TODO Auto-generated catch block
                                e1.printStackTrace();
                            } catch (IOException e1) {
                                state = com.mzinck.twitch.State.VOTING;
                                break;
                            }
                            s = s.substring(s.indexOf("runtime\":\"") + 10, s.indexOf("runtime\":\"") + 13);
                            s = s.replace(" ", "");
                            countdown = Integer.parseInt(s) * 100;
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
                            
                            state = com.mzinck.twitch.State.VOTING;
                        break;

                        case VOTING:
                            new Thread() {
                                public void run() {
                                    countdown = 10;
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

                                        String[][] array = new String[5][2];

                                        int counter = 0;
                                        for (String value : map.keySet()) {
                                            if (counter < 5) {
                                                array[counter][0] = value;
                                                array[counter][1] = Integer.toString(map.get(value));
                                                counter++;
                                            } else {
                                                for (int i = 0; i < array.length; i++) {
                                                    if (Integer.parseInt(array[i][1]) < map.get(value)) {
                                                        for (int y = array.length - 1; y > i + 1; y--) {
                                                            array[y][0] = array[y - 1][0];
                                                            array[y][1] = array[y - 1][1];
                                                        }
                                                        array[i][0] = value;
                                                        array[i][1] = Integer.toString(map.get(value));
                                                        break;
                                                    }
                                                }
                                            }
                                        }

                                        voting = "<h1>VOTING IN PROGRESS</h1>\n";
                                        for (int z = 0; z < array.length; z++) {
                                            if(array[z][0] != null) {
                                                topVote = array[z][0];
                                            }                                            
                                            voting += array[z][0] + ": " + array[z][1] + "\n";
                                        }

                                        Platform.runLater(new Runnable() {
                                            public void run() {
                                                voteLabel.setText(voting);
                                                cdTimer.setText("Vote Time: " + countdown);
                                            }
                                        });
                                        
                                        countdown--;
                                    }
                                    System.out.println("Votes have been tallied: " + topVote + " = Winner");
                                    
                                    setNewNetflix(topVote);
                                    state = com.mzinck.twitch.State.PLAYING;
                                    
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
                        Thread.sleep(11000);
                    } catch (InterruptedException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }
            }
        }.start();                 
    }

}
