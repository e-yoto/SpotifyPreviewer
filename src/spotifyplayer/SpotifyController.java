package spotifyplayer;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Base64;


public class SpotifyController{
    final static private String SPOTIFY_CLIENT_ID     = "";
    final static private String SPOTIFY_CLIENT_SECRET = "";
    
    public static String getArtistId(String artistNameQuery)
    {
        String artistId = "";
        try
        {
            // Parse the JSON output to retrieve the ID
            
            String endpoint = "https://api.spotify.com/v1/search";
            String params = "type=artist&q=" + artistNameQuery;
            String jsonOutput = spotifyEndpointToJson(endpoint, params);
            
            
            JsonObject root = new JsonParser().parse(jsonOutput).getAsJsonObject();
            JsonObject artists = root.getAsJsonObject("artists").getAsJsonObject();
            JsonArray items = artists.get("items").getAsJsonArray();
            
                
            if(items.size() > 0)
            {
                JsonObject item = items.get(0).getAsJsonObject();
                String id = item.get("id").getAsString();
                
                return id;
            }
            
            else
                return null;
            
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }

        return artistId;
    }
    
    public static ArrayList<String> getAlbumIdsFromArtist(String artistId)
    {
        ArrayList<String> albumIds = new ArrayList<>();
        
        try
        {
            
            
            String endpoint = "https://api.spotify.com/v1/artists/" + artistId + "/albums";
            String params = "market=CA&limit=50";
            
            String jsonOutput = spotifyEndpointToJson(endpoint, params);
            
            JsonObject root = new JsonParser().parse(jsonOutput).getAsJsonObject();
            JsonArray albums = root.get("items").getAsJsonArray();
            
            
            for (int i = 0; i < albums.size(); i++)
            {
                JsonObject album = albums.get(i).getAsJsonObject();
                albumIds.add(album.get("id").getAsString());
            }
            
            //albumIds.add("0n9SWDBEftKwq09B01Pwzw");
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
        
        return albumIds;
    }
    
    public static ArrayList<Album> getAlbumDataFromArtist(String artistId)
    {
        ArrayList<String> albumIds = getAlbumIdsFromArtist(artistId);
        ArrayList<Album> albums = new ArrayList<>();
        int index = 0;
        for(String albumId : albumIds)
        {
            try
            {
                // Arguments - Filter for the CA market
                
                String endpoint = "https://api.spotify.com/v1/albums/" + albumIds.get(index);
                String params = "market=CA";
                String artistName;
                String albumName;
                String coverURL;
                String previewUrl = "";
                String imageUrl = "";
                
                ArrayList<Track> albumTracks = new ArrayList<>();
                
                String jsonOutput = spotifyEndpointToJson(endpoint, params);
                
                
                JsonObject root = new JsonParser().parse(jsonOutput).getAsJsonObject();
                JsonArray artist = root.getAsJsonArray("artists");
                artistName = artist.get(0).getAsJsonObject().get("name").getAsString();
                
                albumName = root.get("name").getAsString();
                
                JsonArray covers = root.get("images").getAsJsonArray();
                JsonObject cover = covers.get(0).getAsJsonObject();
                coverURL = cover.get("url").getAsString();
                
                String endpointForArtistImage = "https://api.spotify.com/v1/search";
                String paramsForArtistImage = "type=artist&q=" + artistName;
                String jsonOutputForArtistImage = spotifyEndpointToJson(endpointForArtistImage, paramsForArtistImage);

                JsonObject rootForArtistImage = new JsonParser().parse(jsonOutputForArtistImage).getAsJsonObject(); 
                JsonObject artists = rootForArtistImage.getAsJsonObject("artists").getAsJsonObject();
                JsonArray items = artists.get("items").getAsJsonArray();
                JsonObject item = items.get(0).getAsJsonObject();
                JsonArray images = item.get("images").getAsJsonArray();
                if (images.size() == 0)
                    imageUrl = null;
                else
                    imageUrl = images.get(2).getAsJsonObject().get("url").getAsString();
                
                JsonObject tracks = root.get("tracks").getAsJsonObject();
                JsonArray trackItems = tracks.get("items").getAsJsonArray();
                for (int i = 0, tracknum = 1; i < trackItems.size(); i ++, tracknum++)
                {
                    //Track Object:
                    //int number, String title, int durationInSeconds, String url
                    JsonObject track = trackItems.get(i).getAsJsonObject();
                    String title = track.get("name").getAsString();
                    if (track.get("preview_url").isJsonNull() == false)
                    {
                        previewUrl = track.get("preview_url").getAsString();
                    }
                    int duration = track.get("duration_ms").getAsInt();
                    duration = duration/1000;
                    
                    Track newTrack = new Track(tracknum, title, duration, previewUrl);
                    albumTracks.add(newTrack);
                }
                
                

                
                
                //artistName = "The Beatles";
                //albumName = "Live at the Hollywood Bowl";
                //coverURL = "https://i.scdn.co/image/94c04cbf2ea221d53c4ca2c93c8228c39945a180";
                
                /*
                albumTracks.add(new Track(1, "Twist And Shout - Live / Remastered", 123, ""));
                albumTracks.add(new Track(2, "Street Spirits [Radiohead, not beatles]", 123, "https://p.scdn.co/mp3-preview/204512091f67e2fb0d40b0b23c7afe2617842298?cid=cebae2ef97d645d8920cb3ff029e0549"));
                albumTracks.add(new Track(3, "Dizzy Miss Lizzy - Live / Remastered", 123, ""));
                albumTracks.add(new Track(4, "Ticket To Ride - Live / Remastered", 123, ""));
                albumTracks.add(new Track(5, "Can't Buy Me Love - Live / Remastered", 123, ""));
                albumTracks.add(new Track(6, "Things We Said Today - Live / Remastered", 123, ""));
                albumTracks.add(new Track(7, "Roll Over Beethoven - Live / Remastered", 123, ""));
                */
                albums.add(new Album(artistName, albumName, coverURL, imageUrl, albumTracks));               
                index++;
            }
            catch(Exception e)
            {
                e.printStackTrace();
            }            
        }
        
        return albums;
    }
    


    private static String spotifyEndpointToJson(String endpoint, String params)
    {
        params = params.replace(' ', '+');

        try
        {
            String fullURL = endpoint;
            if (params.isEmpty() == false)
            {
                fullURL += "?"+params;
            }
            
            URL requestURL = new URL(fullURL);
            
            HttpURLConnection connection = (HttpURLConnection)requestURL.openConnection();
            String bearerAuth = "Bearer " + getSpotifyAccessToken();
            connection.setRequestProperty ("Authorization", bearerAuth);
            connection.setRequestMethod("GET");

            BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));

            String inputLine;
            String jsonOutput = "";
            while((inputLine = in.readLine()) != null)
            {
                jsonOutput += inputLine;
            }
            in.close();
            
            return jsonOutput;
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
        
        return "";
    }

     
    private static String getSpotifyAccessToken()
    {
        try
        {
            URL requestURL = new URL("https://accounts.spotify.com/api/token");
            
            HttpURLConnection connection = (HttpURLConnection)requestURL.openConnection();
            String keys = SPOTIFY_CLIENT_ID+":"+SPOTIFY_CLIENT_SECRET;
            String postData = "grant_type=client_credentials";
            
            String basicAuth = "Basic " + new String(Base64.getEncoder().encode(keys.getBytes()));
            
            // Send header parameter
            connection.setRequestProperty ("Authorization", basicAuth);
            
            connection.setRequestMethod("POST");
            connection.setDoOutput(true);

            // Send body parameters
            OutputStream os = connection.getOutputStream();
            os.write( postData.getBytes() );
            os.close();

            BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            
            String inputLine;
            String jsonOutput = "";
            while((inputLine = in.readLine()) != null)
            {
                jsonOutput += inputLine;
            }
            in.close();
            
            JsonElement jelement = new JsonParser().parse(jsonOutput);
            JsonObject rootObject = jelement.getAsJsonObject();
            String token = rootObject.get("access_token").getAsString();

            return token;
        }
        catch(Exception e)
        {
            System.out.println("Something wrong here... make sure you set your Client ID and Client Secret properly!");
            e.printStackTrace();
        }
        
        return "";
    }
}
