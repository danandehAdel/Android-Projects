package com.example.cs121.slugset;

import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SoloMode extends AppCompatActivity {

    Deck deck = new Deck();
    List<String> shuffledDeck = new ArrayList<String>();
    List<String> boardOfCardCodes = new ArrayList<>();
    ImageButton[] boardOfCardButtons;

    ImageButton cardR0C0, cardR0C1, cardR0C2, cardR1C0, cardR1C1, cardR1C2;
    ImageButton cardR2C0, cardR2C1, cardR2C2, cardR3C0, cardR3C1, cardR3C2;

    TextView numSets;
    int countOfSets = 0; // Keeps track of Sets Available counter in game
    TextView remainingCards;
    int countOfDeck = 0; // Keeps track of Cards on Deck counter in game

    List<String> sets = new ArrayList<>(); // Tracks all sets currently available on board
    int hintsCurrentlyShown = -1; // Keeps tracks of how many hints out of the 3 have been shown
    boolean gameEnded = false;

    // Keeps track of the 3 selected cards that are checked to see if they are a set
    List<String> selectedCards = new ArrayList<String>();

    private CardPaths codeDict = CardPaths.paths();
    MediaPlayer backgroundMusic;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_solo);
        backgroundMusic = MediaPlayer.create(this, R.raw.music);

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
    }

    @Override
    protected void onResume(){
        super.onResume();

        // Get the flag that keeps track if there is a game in progress
        SharedPreferences gamePrefs = getSharedPreferences("savedState", 0);
        boolean gameInProgress = gamePrefs.getBoolean("gameProgressSaved", false);

        // Load existing game
        if (gameInProgress) {
            loadExistingGame();

        // Create a new game
        } else {
            startNewGame();
        }

        findSetsOnBoard();
        updateCounters();
    }

    @Override
    protected void onStop() {
        // Adding the cards on the board back to the top of the deck in the proper order
        for (int i=11; i>=0; i--) {
            shuffledDeck.add(0, boardOfCardCodes.get(i));
        }
        // Getting the sharedPreferences ready, and storing the deck
        SharedPreferences gamePrefs = getSharedPreferences("savedState", 0);
        Editor editor = gamePrefs.edit();
        Gson gson = new Gson();
        String json = gson.toJson(shuffledDeck);
        editor.putString("shuffledDeck", json);
        editor.putBoolean("gameProgressSaved", true);
        editor.apply();

        super.onStop();
    }

    @Override
    protected void onPause(){
        super.onPause();
        if (backgroundMusic.isPlaying()) {
            backgroundMusic.pause();
        }
    }



    /** METHODS FOR BUTTONS **/

    public void newGamePressed(View v) {
        startNewGame();
        SharedPreferences gamePrefs = getSharedPreferences("savedState", 0);
        gamePrefs.edit().putBoolean("gameProgressSaved", false).apply();

        List<String> sets = findSetsOnBoard();
        while (sets.size() == 0) {
            shuffleDeck();
            sets = findSetsOnBoard();
        }
        updateCounters();
    }

    public void hintPressed(View v) {
        showHints();
    }

    public void shufflePressed(View v){
        shuffleDeck();

        List<String> sets = findSetsOnBoard();
        while (sets.size() == 0 && !gameEnded) {
            shuffleDeck();
            sets = findSetsOnBoard();
        }
        updateCounters();
    }

    // Volume button pressed
    public void volumeControl(View v){
        if (backgroundMusic.isPlaying()) {
            backgroundMusic.pause();
        } else {
            backgroundMusic.start();
        }
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

            removeAnyHintedCards();
            removeAnySelectedCards();

            // Check if the 3 selected cards are a set
            if (checkSet(selectedCards.get(0), selectedCards.get(1), selectedCards.get(2))) {
                // Grab 3 more cards codes from the deck if the deck allows it
                if (shuffledDeck.size() >=3) {
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
                    while (sets.size() == 0) {
                        shuffleDeck();
                        sets = findSetsOnBoard();
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

                    checkIfGameEnded();
                }

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

        if (shuffledDeck.size() == 0 && sets.size() ==0) {
            gameEnded = true;

            Button hint = (Button) findViewById(R.id.hint);
            Button shuffle = (Button) findViewById(R.id.shuffle);
            hint.setVisibility(View.INVISIBLE);
            shuffle.setVisibility(View.INVISIBLE);

            SharedPreferences gamePrefs = getSharedPreferences("savedState", 0);
            gamePrefs.edit().putBoolean("gameEnded", true).apply();
        }
    }

    public void startNewGame() {
        SharedPreferences gamePrefs = getSharedPreferences("savedState", 0);
        boolean gameEnded = gamePrefs.getBoolean("gameEnded", false);

        if (gameEnded){
            Button hint = (Button) findViewById(R.id.hint);
            Button shuffle = (Button) findViewById(R.id.shuffle);
            hint.setVisibility(View.VISIBLE);
            shuffle.setVisibility(View.VISIBLE);
            gamePrefs.edit().putBoolean("gameEnded", false).apply();

            for (int i=0; i<boardOfCardButtons.length; i++) {
                boardOfCardButtons[i].setVisibility(View.VISIBLE);
            }
        }

        removeAnyHintedCards();
        removeAnySelectedCards();
        selectedCards.clear();

        // Empty shuffledDeck and board of card codes
        shuffledDeck.clear();
        boardOfCardCodes.clear();
        // Get a new shuffled shuffledDeck, and load the board of card codes
        // with the first 12 codes from the shuffledDeck
        shuffledDeck = deck.shuffledDeck();
        loadAllCards(boardOfCardCodes);
        setCardButtonTagsAndImages();
    }

    public void loadExistingGame() {
        SharedPreferences gamePrefs = getSharedPreferences("savedState", 0);
        boolean gameEnded = gamePrefs.getBoolean("gameEnded", false);

        if (gameEnded){
            Button hint = (Button) findViewById(R.id.hint);
            Button shuffle = (Button) findViewById(R.id.shuffle);
            hint.setVisibility(View.INVISIBLE);
            shuffle.setVisibility(View.INVISIBLE);
        }

        // Grab deck from shared preferences
        Gson gson = new Gson();
        String json = gamePrefs.getString("shuffledDeck", null);
        shuffledDeck = gson.fromJson(json, new TypeToken<List<String>>(){}.getType());

        // Clear the board, and load a new one
        boardOfCardCodes.clear();
        loadAllCards(boardOfCardCodes);

        setCardButtonTagsAndImages();
        updateCounters();
    }

    public void shuffleDeck() {
        // Add the cards on board back to the deck
        for(int i=0; i<boardOfCardCodes.size(); i++){
            shuffledDeck.add(boardOfCardCodes.get(i));
        }
        Collections.shuffle(shuffledDeck);

        // Clear board of card codes and load it again
        boardOfCardCodes.clear();
        loadAllCards(boardOfCardCodes);

        removeAnySelectedCards();
        selectedCards.clear();
        removeAnyHintedCards();
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
        countOfDeck = shuffledDeck.size();
        remainingCards.setText(Integer.toString(countOfDeck));
    }

    public void showHints() {
        // I have shown all 3 hints for the first set already, therefore clear them all
        if (hintsCurrentlyShown == 2) { // 0,1,2 = 3
            removeAnyHintedCards();

        // I have NOT shown all 3 hints, therefore show one
        } else {
            hintsCurrentlyShown++;
            // Find the button that needs to be colored as hinted
            ImageButton button = getButtonFromTag(sets.get(hintsCurrentlyShown));
            button.setBackgroundResource(R.drawable.button_border_hint);
        }
    }

    public void removeAnyHintedCards() {
        // If hints have been displayed, they need to be removed
        if (hintsCurrentlyShown >= 0) {
            for (int i=0; i<=hintsCurrentlyShown; i++) {
                ImageButton button = getButtonFromTag(sets.get(i));
                button.setBackgroundResource(R.drawable.button_border_gray);
            }
            hintsCurrentlyShown = -1; // Reset hints counter
        }
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

    // Loads the first 3 cards from the top of the shuffledDeck into a row list
    // Used when a set is found
    public void loadThreeCards(List<String> row) {
        for (int i=0; i<3; i++) {
            String cardCode = shuffledDeck.get(0);
            row.add(0,cardCode);
            shuffledDeck.remove(0);
        }
    }

    // Loads the first 12 cards from the top of the shuffledDeck into the list passed as parameter
    public void loadAllCards(List<String> row) {
        for (int i=0; i<12; i++) {
            String cardCode = shuffledDeck.get(0);
            row.add(cardCode);
            shuffledDeck.remove(0);
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

}


