package com.example.cs121.slugset;


import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.cs121.slugset.Response.ShuffledDeck;
import com.example.cs121.slugset.Response.TopResponse;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.GsonConverterFactory;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.http.GET;
import retrofit2.http.Query;

public class MultiplayerMode extends AppCompatActivity {

    List<String> deck = new ArrayList<String>();
    List<String> boardOfCardCodes = new ArrayList<>();
    ImageButton[] boardOfCardButtons;

    ImageButton cardR0C0, cardR0C1, cardR0C2, cardR1C0, cardR1C1, cardR1C2;
    ImageButton cardR2C0, cardR2C1, cardR2C2, cardR3C0, cardR3C1, cardR3C2;

    TextView numSets;
    int countOfSets = 0; // Keeps track of Sets Available counter in game
    TextView remainingCards;
    int countOfDeck = 0; // Keeps track of Cards on Deck counter in game

    List<String> sets = new ArrayList<>(); // Tracks all sets currently available on board
    boolean gameEnded = false;

    // Keeps track of the 3 selected cards that are checked to see if they are a set
    List<String> selectedCards = new ArrayList<String>();

    private CardPaths codeDict = CardPaths.paths();

    // Online stuff
    Retrofit retrofit;
    List<ShuffledDeck> onlineDeck = new ArrayList<>();
    List<String> foundSets = new ArrayList<>();
    int gameID, onlineSetsCount;

    Timer timer = new Timer();
    ProgressBar progressBar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_multiplayer);

        cardR0C0 = (ImageButton) findViewById(R.id.R0C0);
        cardR0C1 = (ImageButton) findViewById(R.id.R0C1);
        cardR0C2 = (ImageButton) findViewById(R.id.R0C2);
        cardR1C0 = (ImageButton) findViewById(R.id.R1C0);
        cardR1C1 = (ImageButton) findViewById(R.id.R1C1);
        cardR1C2 = (ImageButton) findViewById(R.id.R1C2);
        cardR2C0 = (ImageButton) findViewById(R.id.R2C0);
        cardR2C1 = (ImageButton) findViewById(R.id.R2C1);
        cardR2C2 = (ImageButton) findViewById(R.id.R2C2);
        cardR3C0 = (ImageButton) findViewById(R.id.R3C0);
        cardR3C1 = (ImageButton) findViewById(R.id.R3C1);
        cardR3C2 = (ImageButton) findViewById(R.id.R3C2);

        boardOfCardButtons = new ImageButton[] {cardR0C0, cardR0C1, cardR0C2,
                                                cardR1C0, cardR1C1, cardR1C2,
                                                cardR2C0, cardR2C1, cardR2C2,
                                                cardR3C0, cardR3C1, cardR3C2};

        numSets = (TextView) findViewById(R.id.numOfSets);
        remainingCards = (TextView) findViewById(R.id.numOfCardsOnDeck);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);


        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        logging.setLevel(HttpLoggingInterceptor.Level.BODY);
        OkHttpClient httpClient = new OkHttpClient.Builder()
                .addInterceptor(logging)
                .build();

        retrofit = new Retrofit.Builder()
                .baseUrl("https://slugset.pythonanywhere.com/")
                .addConverterFactory(GsonConverterFactory.create())
                .client(httpClient) //add logging
                .build();
    }

    @Override
    protected void onResume(){
        super.onResume();

        fetchMultiplayerDeck();
    }

    @Override
    protected void onStop() {
        // Stopping the timed refresh calls if chat activity is dismissed
        timer.cancel();

        super.onStop();
    }



    /** METHODS FOR BUTTONS PRESSED**/

    public void newGamePressed(View v) {
        playAgain();
    }

    public void cardSelected(View v) {
        ImageButton card = (ImageButton) v;
        Boolean selected = isCardSelected((String) card.getTag());

        // If card is already selected, then remove blue border
        if(selected){
            card.setBackgroundResource(R.drawable.button_border_gray);
            // Also remove card from the selected card list
            removeFromSelectedCards((String)card.getTag());
            // Card is NOT selected, add blue border
        }else{
            card.setBackgroundResource(R.drawable.button_border_blue);
            // Also add card code to selectedCards list
            addToSelectedCards((String) card.getTag());
        }

        // Once 3 cards have been selected
        if (selectedCards.size() == 3) {
            removeAnySelectedCards();

            // Check if the 3 selected cards are a set
            if (checkSet(selectedCards.get(0), selectedCards.get(1), selectedCards.get(2))) {

                // Send set to server
                // Add set to foundSets list
                foundSets.add(selectedCards.get(0));
                foundSets.add(selectedCards.get(1));
                foundSets.add(selectedCards.get(2));
                onlineSetsCount++;
                // If this is the first set found we need to remove the first element in array
                // which by default is "0000" as specified on server
                if (foundSets.get(0).equals("0000")) {
                    foundSets.remove(0);
                }
                submitFoundSetsToServer();

                // Grab 3 more cards codes from the deck if the deck allows it
                if (deck.size() >=3) {
                    List<String> next3CardCodes = new ArrayList<String>();
                    loadThreeCards(next3CardCodes);

                    // Replace the selected cards with the new cards from the deck
                    for (int i=0; i<3; i++) {
                        String code = selectedCards.get(i);
                        ImageButton button = getButtonFromTag(code);
                        button.setTag(next3CardCodes.get(i));
                        loadButtonImage(next3CardCodes.get(i), button);
                        replaceCodeFromBoard(selectedCards.get(i), next3CardCodes.get(i));
                    }

                    // Find the new sets, and shuffle if needed
                    List<String> sets = findSetsOnBoard();
                    boolean flag = false;
                    while (sets.size() == 0 && deck.size()>=3) {
                        shuffleDeck();
                        sets = findSetsOnBoard();
                        flag = true;
                    }
                    if (flag) {
                        submitDeck2();
                    }
                    updateCounters();

                // There are no more cards on the deck
                }else {
                    // Show an empty card
                    for (int i=0; i<3; i++) {
                        ImageButton button = getButtonFromTag(selectedCards.get(i));
                        button.setTag("0");
                        button.setVisibility(View.INVISIBLE);
                        replaceCodeFromBoard(selectedCards.get(i), "0");
                    }
                }
                checkIfGameEnded();

            // They are NOT a set
            }else{
                notASetShakeButtons();
            }
            selectedCards.clear();
        }
    }



    /** CLASS HELPER METHODS **/

    public void checkIfGameEnded() {
        List<String> sets = findSetsOnBoard();
        updateCounters();

        if (deck.size() == 0 && sets.size() ==0) {
            gameEnded = true;

            Button newGame = (Button) findViewById(R.id.newGame);
            newGame.setVisibility(View.VISIBLE);
        }
    }

    public void initBoard() {
        // Clear the board, and load a new one
        boardOfCardCodes.clear();
        loadAllCards(boardOfCardCodes);

        // Check to see if the deck received by server has sets
        List<String> sets = findSetsOnBoard();
        // If it doesn't, add the cards back on the board back to the deck, shuffle it and
        // upload it to server.
        while (sets.size() == 0) {
            // Add the cards on board back to the deck
            for(int i=0; i<boardOfCardCodes.size(); i++){
                deck.add(boardOfCardCodes.get(i));
            }
            Collections.shuffle(deck);
            submitDeck();

            loadAllCards(boardOfCardCodes);
            sets = findSetsOnBoard();
        }

        simulateFindingSets();
        setCardButtonTagsAndImages();
        findSetsOnBoard();
        updateCounters();
        checkIfGameEnded();
    }

    public void initBoardFromLoadChangesCallback() {
        // Clear the board, and load a new one
        boardOfCardCodes.clear();
        loadAllCards(boardOfCardCodes);

        simulateFindingSets();

        setCardButtonTagsAndImages();
        findSetsOnBoard();
        updateCounters();
        checkIfGameEnded();
    }

    public void simulateFindingSets() {
        List<String> sets = findSetsOnBoard();
        if (onlineSetsCount > 0 && !sets.isEmpty()) {
            List<String> next3Cards = new ArrayList<>();
            // Simulate replacing the cards for each set in onlineSetsCount
            for (int i=0; i<onlineSetsCount; i++) {
                int x,y;
                // There are still cards left on deck, show the next 3 cards on deck
                if (deck.size() >=3) {
                    loadThreeCards(next3Cards);
                    int next3CardsIndex = 0;

                    // How foundSets looks:
                    // ["2GOE","2YDE","2RSE","2ROD","2GSD","2YDD","1GSD","1YDD","1ROD"]
                    // Example has 3 sets. Indices [0,1,2],[3,4,5],[6,7,8]
                    x=0; // found-sets start index of current set. ie. 0->3->6...
                    y=0; // found-sets start index of next set. ie. 3->6->9...

                    // Start at x, while less than y and replace the card
                    for (int j=(x=(i==0 ? 0 : i*3)); j<(y=(i==0 ? 3 : (i+1)*3)); j++) {
                        // Replace the code on the board, with the new code from the deck
                        replaceCodeFromBoard(foundSets.get(j), next3Cards.get(next3CardsIndex));
                        next3CardsIndex++;
                    }
                    next3Cards.clear();

                // There are NO more cards left on deck, show 3 empty cards instead
                }else {
                    x=0; // found-sets start index of current set. ie. 0->3->6...
                    y=0; // found-sets start index of next set. ie. 3->6->9...
                    // Go to the last 3 cards from the foundSets List
                    for (int k=(x=(i==0 ? 0 : i*3)); k<(y=(i==0 ? 3 : (i+1)*3)); k++) {
                        replaceCodeFromBoard(foundSets.get(k), "0");
                    }
                }
            }
        }
    }

    public void shuffleDeck() {
        // Add the cards on board back to the deck
        for(int i=0; i<boardOfCardCodes.size(); i++){
            deck.add(boardOfCardCodes.get(i));
        }
        Collections.shuffle(deck);

        // Clear board of card codes and load it again
        boardOfCardCodes.clear();
        loadAllCards(boardOfCardCodes);

        removeAnySelectedCards();
        selectedCards.clear();
        setCardButtonTagsAndImages();
    }

    public void setCardButtonTagsAndImages() {
        // Set the codes on the buttons tags, and load the images
        for (int i=0; i<boardOfCardCodes.size(); i++) {
            String code = boardOfCardCodes.get(i);
            boardOfCardButtons[i].setTag(code);
            if (code.equals("0")){
                boardOfCardButtons[i].setVisibility(View.INVISIBLE);
            }else{
                loadButtonImage(code, boardOfCardButtons[i]);
            }
        }
    }

    public void loadButtonImage(String code, ImageButton imageButton) {
        String path = codeDict.path.get(code);
        int card_id = getResources().getIdentifier(path, "drawable", getPackageName());
        imageButton.setImageResource(card_id);
    }

    public void updateCounters() {
        numSets.setText(Integer.toString(countOfSets));
        countOfDeck = deck.size();
        remainingCards.setText(Integer.toString(countOfDeck));
    }

    public void removeAnySelectedCards() {
        // Remove blue border from all selected cards
        for (int i=0; i<selectedCards.size(); i++) {
            ImageButton button = getButtonFromTag(selectedCards.get(i));
            if (button != null) {
                button.setBackgroundResource(R.drawable.button_border_gray);
            }
        }
    }

    public void notASetShakeButtons() {
        for (int i=0; i<selectedCards.size(); i++) {
            ImageButton button = getButtonFromTag(selectedCards.get(i));
            Animation shake = AnimationUtils.loadAnimation(this, R.anim.shake);
            button.startAnimation(shake);
        }
    }

    // Returns the ImageButton associated with the card code passed as parameter
    public ImageButton getButtonFromTag(String tag) {
        // Iterate through all card buttons on board
        for (int i=0; i<boardOfCardButtons.length; i++) {
            // Grab the tag of the button
            String tagOfButton = (String) boardOfCardButtons[i].getTag();
            // if the button tag is the same as the tag passed in param, then return the button
            if (tagOfButton.equals(tag)) {
                return boardOfCardButtons[i];
            }
        }
        return null;
    }

    // Loads the first 3 cards from the top of the deck into a row list
    // Used when a set is found
    public void loadThreeCards(List<String> row) {
        for (int i=0; i<3; i++) {
            String cardCode = deck.get(0);
            row.add(cardCode);
            deck.remove(0);
        }
    }

    // Loads the first 12 cards from the top of the deck into the list passed as parameter
    public void loadAllCards(List<String> row) {
        for (int i=0; i<12; i++) {
            String cardCode = deck.get(0);
            row.add(cardCode);
            deck.remove(0);
        }
    }

    // Keeps boardOfCardCodes up to date. Used when a set if found.
    // Replaces the card code from selectedCards List with a new code from the deck
    public void replaceCodeFromBoard (String codeOnBoard, String newCode) {
        for (int i=0; i<boardOfCardCodes.size(); i++) {
            // Replace code on boardOfCardCodes with a new code
            if (codeOnBoard.equals(boardOfCardCodes.get(i))) {
                boardOfCardCodes.set(i, newCode);
                break;
            }
        }
    }

    public String listToString (List<String> list){
        String string = "";
        for (int i=0; i<list.size(); i++) {
            string = string + " " + list.get(i);
        }
        return string;
    }

    // Returns true if card code is in selectedCards
    public boolean isCardSelected(String str){
        if (selectedCards.isEmpty()) {
            return false;
        }else{
            for (int i=0; i<selectedCards.size(); i++){
                if (selectedCards.get(i).equals(str)) {
                    return true;
                }
            }
        }
        return false;
    }

    // Removes card code from selectedCards array
    public void removeFromSelectedCards(String str){
        for (int i=0; i<selectedCards.size(); i++){
            if (selectedCards.get(i).equals(str)) {
                selectedCards.remove(i);
                break;
            }
        }
    }

    public void addToSelectedCards(String str){
        selectedCards.add(str);

    }

    // Finds all available sets in current board and returns them
    // it returns them to be able to show the hints
    public List<String> findSetsOnBoard() {
        countOfSets = 0;
        boolean set; // boolean set to true when set is found

        // Clear out the List of sets if needed
        if (!sets.isEmpty()) { sets.clear(); }

        // Iterating over List of 12 cards with 3 different cursors
        for (int spot1=0; spot1<boardOfCardCodes.size()-2; spot1++){ // First cursor starts at index 0
            for (int spot2=spot1+1; spot2<boardOfCardCodes.size()-1; spot2++){ // Second cursor starts at first+1
                for (int spot3=spot2+1; spot3<boardOfCardCodes.size(); spot3++){ // Third cursor starts at second+1

                    // Checking all combinations of 3 cards
                    set = checkSet(boardOfCardCodes.get(spot1), boardOfCardCodes.get(spot2), boardOfCardCodes.get(spot3));

                    if (set) {//If set found add it to List of sets
                        Log.i("myapp", "it is a set: " + boardOfCardCodes.get(spot1) + " " + boardOfCardCodes.get(spot2) + " " + boardOfCardCodes.get(spot3));
                        sets.add(boardOfCardCodes.get(spot1)); sets.add(boardOfCardCodes.get(spot2)); sets.add(boardOfCardCodes.get(spot3));
                        countOfSets++;
                    }
                }
            }
        }
        return sets;
    }

    // Check all 4 attributes of cards one by one and check
    // to see if they are all the same or all different
    public boolean checkSet(String card1, String card2, String card3){
        if (card1.equals("0") || card2.equals("0") || card3.equals("0")){
            return false;
        }
        boolean set = true;
        for (int i = 0; (set && i < 4); i++) {
            set = checkAttributes(card1.substring(i, i+1), card2.substring(i, i+1), card3.substring(i, i+1));
        }
        return set;
    }

    // Check whether the passed attribute is the same or different for all 3 cards
    public boolean checkAttributes(String a, String b, String c){
        return ( (a.equals(b) && a.equals(c)) || ( !a.equals(b) && !a.equals(c) && !b.equals(c) ) );
    }




    /* SERVER CALLS AND OTHER INTERNET STUFF */

    // Called only one time to get the deck from server
    public void fetchMultiplayerDeck() {
        MultiplayerCalls service = retrofit.create(MultiplayerCalls.class);
        Call<TopResponse> queryCall = service.load_deck_multiplayer();

        queryCall.enqueue(new Callback<TopResponse>() {
            @Override
            public void onResponse(Response<TopResponse> response) {
                onlineDeck = response.body().shuffledDeck;
                foundSets = response.body().foundSets;
                gameID = response.body().gameID;
                onlineSetsCount = response.body().countsets;

                deck = properStructureFrom(onlineDeck);
                progressBar.setVisibility(View.INVISIBLE);
                initBoard();
                checkForChanges();
            }

            @Override
            public void onFailure(Throwable t) {

            }
        });
    }

    // Pings server to ask if there has been a change
    // Called the first time once we received the deck from fetchMultiplayerDeck
    public void checkForChanges() {
        // A timer used to automatically refresh every 0.6 seconds
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                fetchChanges();
            }
        }, 666, // How long before first execution
           666); // How often each execution (5 seconds)
    }

    // Called in intervals to check if there has been an update on the server
    public void fetchChanges() {
        if (!gameEnded) {
            MultiplayerCalls service = retrofit.create(MultiplayerCalls.class);
            Call<TopResponse> queryCall = service.load_changes(gameID, onlineSetsCount);

            queryCall.enqueue(new Callback<TopResponse>() {
                @Override
                public void onResponse(Response<TopResponse> response) {
                    if (response.body().response.equals("ok")) {
                        onlineDeck = response.body().shuffledDeck;
                        foundSets = response.body().foundSets;
                        onlineSetsCount = response.body().countsets;

                        deck = properStructureFrom(onlineDeck);
                        initBoardFromLoadChangesCallback();
                    }
                }

                @Override
                public void onFailure(Throwable t) {}
            });
        }
    }

    public void submitFoundSetsToServer() {
        Gson gson = new Gson();
        String json = gson.toJson(foundSets);

        MultiplayerCalls service = retrofit.create(MultiplayerCalls.class);
        Call<TopResponse> queryCall = service.submit_found_sets(gameID, json, onlineSetsCount);

        queryCall.enqueue(new Callback<TopResponse>() {
            @Override
            public void onResponse(Response<TopResponse> response) {
                Log.i("myapp", "Set found was updated on server");
            }
            @Override
            public void onFailure(Throwable t) {}
        });
    }

    //Submits a reshuffled FULL-81cards deck back to the server because the one originally sent to the client
    //by the server did not have any sets available for the first 12 cards
    //Should only be done by the first person that enters multiplayer
    //Method needs to be moved to server-side
    public void submitDeck() {
        // Turn local deck into proper server form
        List<Map<String,String>> mapList = new ArrayList<Map<String,String>>();
        Map<String,String> cardCode;
        for (int i=0; i<deck.size(); i++) {
            cardCode = new HashMap<>();
            cardCode.put("code", deck.get(i));
            mapList.add(cardCode);
        }

        Gson gson = new Gson();
        String json = gson.toJson(mapList);

        MultiplayerCalls service = retrofit.create(MultiplayerCalls.class);
        Call<TopResponse> queryCall = service.submit_deck(gameID, json, onlineSetsCount);

        queryCall.enqueue(new Callback<TopResponse>() {
            @Override
            public void onResponse(Response<TopResponse> response) {
                Log.i("myapp", "Deck was updated on server");
            }
            @Override
            public void onFailure(Throwable t) {
            }
        });

    }

    //This one also submits a reshuffled deck back to the server, however, this one only
    //sends a partial deck because the game has been in progress.
    //Can be done by any player who finds a set and then there are no more sets
    public void submitDeck2() {
        // Turn local deck into proper server form
        List<Map<String,String>> mapList = new ArrayList<Map<String,String>>();
        Map<String,String> cardCode;
        for (int i=0; i<deck.size(); i++) {
            cardCode = new HashMap<>();
            cardCode.put("code", deck.get(i));
            mapList.add(cardCode);
        }

        Gson gson = new Gson();
        String json = gson.toJson(mapList);

        MultiplayerCalls service = retrofit.create(MultiplayerCalls.class);
        Call<TopResponse> queryCall = service.submit_deck2(gameID, json, onlineSetsCount);

        queryCall.enqueue(new Callback<TopResponse>() {
            @Override
            public void onResponse(Response<TopResponse> response) {
                Log.i("myapp", "Deck was updated on server");
            }
            @Override
            public void onFailure(Throwable t) {
            }
        });
    }

    public void playAgain() {
        MultiplayerCalls service = retrofit.create(MultiplayerCalls.class);
        Call<TopResponse> queryCall = service.game_ended(gameID);

        queryCall.enqueue(new Callback<TopResponse>() {
            @Override
            public void onResponse(Response<TopResponse> response) {
                gameEnded = false;
                Button newGame = (Button) findViewById(R.id.newGame);
                newGame.setVisibility(View.INVISIBLE);
                progressBar.setVisibility(View.VISIBLE);

                for (int i=0; i<boardOfCardButtons.length; i++) {
                    boardOfCardButtons[i].setVisibility(View.VISIBLE);
                }

                fetchMultiplayerDeck();
            }
            @Override
            public void onFailure(Throwable t) {
            }
        });

    }

    public List<String> properStructureFrom(List<ShuffledDeck> sd) {
        List<String> list = new ArrayList<>();
        for (int i=0; i<sd.size(); i++) {
            list.add(sd.get(i).code);
        }
        return list;
    }

    public interface MultiplayerCalls {
        @GET("load_deck_multiplayer/")
        Call<TopResponse> load_deck_multiplayer();

        @GET("load_changes/")
        Call<TopResponse> load_changes(@Query("gameID") int gameID,
                                       @Query("countsets") int countsets);

        @GET("game_ended/")
        Call<TopResponse> game_ended(@Query("gameID") int gameID);

        @GET("submit_found_sets/")
        Call<TopResponse> submit_found_sets(@Query("gameID") int gameID,
                                            @Query("found_sets") String found_sets,
                                            @Query("countsets") int countsets);

        @GET("submit_deck/")
        Call<TopResponse> submit_deck(@Query("gameID") int gameID,
                                      @Query("deck") String deck,
                                      @Query("countsets") int countSets);

        @GET("submit_deck2/")
        Call<TopResponse> submit_deck2(@Query("gameID") int gameID,
                                       @Query("deck") String deck,
                                       @Query("countsets") int countSets);
    }

}
