package com.mzinck.twitch;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

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
import javafx.scene.layout.GridPane;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

public class Netflix extends Application {

    public static NetflixRoulette nflxr       = new NetflixRoulette();
    public static String          netflixTitle = null;
    public static String          netflixDesc = null;
    public static String          voting      = "";

    public static void main(String[] args) throws UnknownHostException, IOException {
        IRC irc = new IRC();
        new Thread(irc).start();
        setNewNetflix("Trailer Park Boys");
        launch(args);
    }

    public static void setNewNetflix(String netflix) {
        netflixTitle = netflix;
        try {
            netflixTitle += " (" + nflxr.getMediaReleaseYear(netflix) + ") ("
                         + nflxr.getMediaRating(netflix) + " Rating)";
            netflixDesc = nflxr.getMediaSummary(netflix) + "\nActors: " + nflxr.getMediaCast(netflix);
        } catch (JSONException e) {
            System.out.println("Unknown Error.");
        } catch (IOException e) {
            System.out.println("Unknown movie try again.");
            setNewNetflix(netflix);
        }
    }

    public static void setStage(Stage stage, Scene scene) {
        stage.setScene(scene);
        stage.show();
    }

    public void start(Stage primaryStage) {
        primaryStage.setTitle("TwitchWatchesNetflix");

        GridPane grid = new GridPane();
        grid.setAlignment(Pos.TOP_LEFT);
        grid.setHgap(50);
        grid.setVgap(10);
        grid.setPadding(new Insets(25, 25, 25, 25));

        Text nfTitle = new Text(netflixTitle);
        nfTitle.setFont(Font.font("Roboto", FontWeight.NORMAL, 30));
        grid.add(nfTitle, 1, 0);

        Label nfDesc = new Label(netflixDesc);
        nfDesc.setWrapText(true);
        nfDesc.setPrefWidth(500);
        nfDesc.setText(netflixDesc);
        nfDesc.setFont(Font.font("Roboto", FontWeight.NORMAL, 12));
        GridPane.setHalignment(nfDesc, HPos.LEFT);
        grid.add(nfDesc, 1, 1);

        Image img = new Image(
                "file:C:\\Users\\mitchell\\Desktop\\60032563.jpg");
        ImageView imgView = new ImageView(img);
        grid.add(imgView, 0, 0, 1, 2);

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
                while (true) {
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

                    voting = "";
                    for (int z = 0; z < array.length; z++) {
                        voting += array[z][0] + ": " + array[z][1] + "\n";
                    }

                    Platform.runLater(new Runnable() {
                        public void run() {
                            nfDesc.setText(voting);
                        }
                    });

                }
            }
        }.start();

    }

}
