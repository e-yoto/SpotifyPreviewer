package spotifyplayer;

import com.sun.javafx.collections.ObservableListWrapper;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.Slider;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.util.Callback;
import javafx.util.Duration;
import javax.imageio.ImageIO;
import javax.swing.JFileChooser;

public class FXMLDocumentController implements Initializable {

    @FXML
    private Label artistLabel;

    @FXML
    private Label albumLabel;
    
    @FXML
    private Label saveMessageLabel;

    @FXML
    private Label errorSearchLabel;

    @FXML
    private ImageView albumCoverImageView;
    
    @FXML
    private ImageView artistImageView;

    @FXML
    private TextField searchArtistTextField;

    @FXML
    private Button previousAlbumButton;

    @FXML
    private Button nextAlbumButton;
    
    @FXML
    private Button downloadAlbumButton;

    @FXML
    private TableView tracksTableView;

    @FXML
    private Button playButton;

    @FXML
    private Slider trackSlider;

    @FXML
    private Label trackTimeLabel;
    

    @FXML
    private ProgressIndicator progress;

    MediaPlayer mediaPlayer = null;
    int currentAlbum = 0;
    ArrayList<Album> albums = null;
    Boolean isPlaying = false;

    @FXML
    private void handlePlayButtonAction(ActionEvent event) {
        saveMessageLabel.setText("");
        if (mediaPlayer.getStatus() == MediaPlayer.Status.PLAYING) {
            isPlaying = false;
            mediaPlayer.pause();
            playButton.setText("Play");
        } else if (mediaPlayer.getStatus() == MediaPlayer.Status.PAUSED) {
            isPlaying = true;
            mediaPlayer.play();
            playButton.setText("Pause");
        }

    }

    @FXML
    private void handlePreviousButtonAction(ActionEvent event) {
        // TODO = Display the previous album data
        //        (wrap around if you get below 0)
        saveMessageLabel.setText("");
        if (isPlaying)
        {
            mediaPlayer.stop();
            trackSlider.setValue(0);
            playButton.setDisable(true);
            trackSlider.setDisable(true);
        }
        
        if (currentAlbum == 0) {
            currentAlbum = albums.size() - 1;
            displayAlbum(currentAlbum);
        } else {
            currentAlbum--;
            displayAlbum(currentAlbum);
        }
        
        isPlaying = false;
    }

    @FXML
    private void handleNextButtonAction(ActionEvent event) {
        // TODO = Display the next album data
        //        (wrap around if you get below 0)
        saveMessageLabel.setText("");
        if (isPlaying)
        {
            mediaPlayer.stop();
            trackSlider.setValue(0);
            playButton.setDisable(true);
            trackSlider.setDisable(true);
        }
        
        if (currentAlbum == albums.size() - 1) {
            currentAlbum = 0;
            displayAlbum(currentAlbum);
        } else {
            currentAlbum++;
            displayAlbum(currentAlbum);
        }
        
        isPlaying = false;
    }

    @FXML
    private void handleSearchButtonAction(ActionEvent event) {
        saveMessageLabel.setText("");
        progress.setVisible(true);
        searchArtistTextField.setDisable(true);
        previousAlbumButton.setDisable(true);
        nextAlbumButton.setDisable(true);

        // This insures we don't block the GUI when executing slow Web Requests
        ExecutorService executor = Executors.newSingleThreadScheduledExecutor();
        executor.submit(new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                // You can't change any JavaFX Controls here...
                searchArtist(searchArtistTextField.getText());
                return null;
            }

            @Override
            protected void succeeded() {
                progress.setVisible(false);
                searchArtistTextField.setDisable(false);
                downloadAlbumButton.setDisable(false);

                if (albums.size() > 0) {
                    displayAlbum(0);
                }
                else
                {
                    
                    cancelled();
                    
                }
            }

            @Override
            protected void cancelled() {
                progress.setVisible(false);
                searchArtistTextField.setDisable(false);
                downloadAlbumButton.setDisable(true);

                File file = new File("images/noImage.png");
                Image image = new Image(file.toURI().toString());
                artistImageView.setImage(image);
                albumCoverImageView.setImage(null);
                artistLabel.setText("Error!");
                albumLabel.setText("Error retrieving " + searchArtistTextField.getText());
                tracksTableView.getItems().clear();
            }

        });
    }
    
    @FXML
    private void handleDownloadButtonAction(ActionEvent event) {
        
        Album album = albums.get(currentAlbum);
        
        saveMessageLabel.setText("");
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Save as");
        fileChooser.setApproveButtonText("Save");
        fileChooser.setCurrentDirectory(new File("./images"));
        
        if (fileChooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
            File fileToSave = new File(fileChooser.getSelectedFile().toString() + ".png");
            String filename = fileChooser.getSelectedFile().toString();
            
            System.out.println(filename);

            BufferedImage image = null;
            try {
                try {
                    URL url = new URL(album.getImageURL());
                    image = ImageIO.read(url);
                    ImageIO.write(image, "png", fileToSave);
                    saveMessageLabel.setText("Saved");
                } catch (MalformedURLException ex) {
                    saveMessageLabel.setText("Error looking for URL");
                }
                
            } catch (IOException ex) {
                saveMessageLabel.setText("Error saving image;");
            }
        }
    }

    private void displayAlbum(int albumIndex) {

        if (albumIndex >= 0 && albumIndex < albums.size()) {
            Album album = albums.get(albumIndex);
            System.out.println("Album: " + album);
            
            artistLabel.setText(album.getArtistName());
            albumLabel.setText(album.getAlbumName());
            
            Image artistImage;
            
            if (album.getArtistPictureUrl() == null)
            {
                File file = new File("images/noImage.png");
                artistImage = new Image(file.toURI().toString());
                artistImageView.setImage(artistImage);
                
            }
            else
            {
                artistImage = new Image(album.getArtistPictureUrl());
                artistImageView.setImage(artistImage);
            }
            
            //Image albumImage = new Image(album.getArtistPictureUrl());
            //albumCoverImageView.setImage(albumImage);
            

            // Set tracks
            ArrayList<TrackForTableView> tracks = new ArrayList<>();
            for (int i = 0; i < album.getTracks().size(); ++i) {
                TrackForTableView trackForTable = new TrackForTableView();
                Track track = album.getTracks().get(i);
                trackForTable.setTrackNumber(track.getNumber());
                trackForTable.setTrackTitle(track.getTitle());
                trackForTable.setTrackPreviewUrl(track.getUrl());
                tracks.add(trackForTable);
            }
            tracksTableView.setItems(new ObservableListWrapper(tracks));

            // Previous and next buttons
            if (albums.size() > 1) {
                previousAlbumButton.setDisable(false);
                nextAlbumButton.setDisable(false);
            } else {
                previousAlbumButton.setDisable(true);
                nextAlbumButton.setDisable(true);
            }

            // Cover image
            Image coverImage = new Image(album.getImageURL());
            albumCoverImageView.setImage(coverImage);

            // Track 1 slider / time information
            int minutes = 0;
            int seconds = 30;
            String secondsStr = seconds < 10 ? "0" + seconds : "" + seconds;

            trackSlider.setValue(0.0);
            trackTimeLabel.setText("0:00 / " + minutes + ":" + secondsStr);
        }
    }

    public void searchArtist(String artistName) {
        
        String artistId = SpotifyController.getArtistId(artistName);
        
        if (isPlaying == true)
        {
            mediaPlayer.stop();
        }
        
        currentAlbum = 0;
        albums = SpotifyController.getAlbumDataFromArtist(artistId);
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Setup Table View
        TableColumn<TrackForTableView, Number> trackNumberColumn = new TableColumn("#");
        trackNumberColumn.setCellValueFactory(new PropertyValueFactory("trackNumber"));

        TableColumn trackTitleColumn = new TableColumn("Title");
        trackTitleColumn.setCellValueFactory(new PropertyValueFactory("trackTitle"));
        trackTitleColumn.setPrefWidth(250);

        TableColumn playColumn = new TableColumn("Preview");
        playColumn.setCellValueFactory(new PropertyValueFactory("trackPreviewUrl"));
        Callback<TableColumn<TrackForTableView, String>, TableCell<TrackForTableView, String>> cellFactory = new Callback<TableColumn<TrackForTableView, String>, TableCell<TrackForTableView, String>>() {
            @Override
            public TableCell<TrackForTableView, String> call(TableColumn<TrackForTableView, String> param) {
                final TableCell<TrackForTableView, String> cell = new TableCell<TrackForTableView, String>() {
                    final Button playButton = new Button("Play");

                    @Override
                    public void updateItem(String item, boolean empty) {
                        if (item != null && item.equals("") == false) {
                            playButton.setOnAction(event -> {
                                if (mediaPlayer != null) {
                                    mediaPlayer.stop();
                                }

                                Media music = new Media(item);
                                mediaPlayer = new MediaPlayer(music);
                                mediaPlayer.play();
                                trackSlider.setValue(0);
                                isPlaying = true;
                                saveMessageLabel.setText("");
                                displayAlbum(currentAlbum);
                                
                            });

                            setGraphic(playButton);

                        } else {
                            setGraphic(null);
                        }

                        setText(null);

                    }
                };
                return cell;
            }
        };
        playColumn.setCellFactory(cellFactory);

        tracksTableView.getColumns().setAll(trackNumberColumn, trackTitleColumn, playColumn);

        searchArtist("pink floyd");
        displayAlbum(0);
        playButton.setDisable(true);
        trackSlider.setDisable(true);
        /**
         * Use ScheduledExecutorService start it at ticking every second. but
         * only tick when the music is playing, so check if the music is
         * playing. Use scheduleAtFixedRate(new Runnable({void run(){ . . .
         * }},0,1, TimeUnit.SECONDS);
         *
         */
        
        //you can make it so the music is paused while dragging
        trackSlider.setOnMouseReleased(event -> {
            isPlaying = true;
            mediaPlayer.seek(Duration.seconds(trackSlider.getValue()));
            mediaPlayer.play();
        });

        trackSlider.setOnMousePressed(event -> {
            isPlaying = false;
            mediaPlayer.pause();
        });

        ScheduledExecutorService mainloop = Executors.newSingleThreadScheduledExecutor();
        mainloop.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {

                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        if (isPlaying) {
                            //if it reaches the end (30 secs) reset values
                            Duration duration = mediaPlayer.getCurrentTime();
                            double seconds = duration.toSeconds();
                            String doubleStr = Double.toString(seconds) + "";
                            String secStr = "";
                            if (seconds < 10)
                            {
                                secStr = doubleStr.charAt(0) + "";
                            }
                            else
                            {
                                secStr = doubleStr.substring(0, 2);
                            }
                                
                            if (Integer.parseInt(secStr) == 30) {
                                isPlaying = false;
                                playButton.setDisable(true);
                                playButton.setText("Play");
                                mediaPlayer.stop();
                                trackSlider.setDisable(true);
                                trackSlider.setValue(0);
                                trackTimeLabel.setText("0:00 / 0:30");
                                
                            } else {
                                playButton.setText("Pause");
                                playButton.setDisable(false);
                                trackSlider.setDisable(false);
                                
                                trackSlider.setValue(Integer.parseInt(secStr));
                                if (Integer.parseInt(secStr) < 10) {
                                    trackTimeLabel.setText("0:0" + secStr + " / " + 0 + ":" + 30);
                                } else {
                                    trackTimeLabel.setText("0:" + secStr + " / " + 0 + ":" + 30);
                                }
                            }
                        }
                    }
                });
            }
        }, 0, 1, TimeUnit.SECONDS);
    }
}