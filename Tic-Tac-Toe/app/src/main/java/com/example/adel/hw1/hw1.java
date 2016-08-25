package com.example.adel.hw1;
import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

public class hw1 extends Activity implements OnClickListener {

    Button a1, a2, a3, b1, b2, b3, c1, c2, c3, newGame;
    Button[] buttonArray = null;

    boolean turn = true; // Let's say that X = true and O = false
    int turnCounter = 0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hw1);

        // retrieving all the buttons and storing them in an array
        a1 = (Button) findViewById(R.id.A1);
        b1 = (Button) findViewById(R.id.B1);
        c1 = (Button) findViewById(R.id.C1);
        a2 = (Button) findViewById(R.id.A2);
        b2 = (Button) findViewById(R.id.B2);
        c2 = (Button) findViewById(R.id.C2);
        a3 = (Button) findViewById(R.id.A3);
        b3 = (Button) findViewById(R.id.B3);
        c3 = (Button) findViewById(R.id.C3);

        buttonArray = new Button[] { a1, a2, a3, b1, b2, b3, c1, c2, c3 };
        for (Button b : buttonArray)
            b.setOnClickListener(this);

        Button b = (Button) findViewById(R.id.newGame);
        b.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                // reset all turn and turnCounter and enable all the buttons
                // because wee should start out fresh (is a new game).
                turn = true;
                turnCounter = 0;
                resetButtons();
            }
        });
    }

    @Override
    public void onClick(View v) {
        buttonClicked(v);
    }

    private void buttonClicked(View v) {
        Button b = (Button) v;
        if (turn) {
            // X's turn
            b.setText("X");

        } else {
            // O's turn
            b.setText("O");
        }
        turnCounter++;   // incrementing the counter with selecting a button
        b.setClickable(false);
        b.setBackgroundColor(Color.LTGRAY);
        turn = !turn;

        checkForWinner();
    }

    private void checkForWinner() {

        boolean thereIsWinner = false;

          /*===============================================================
            Chart below shows how buttons are mapped in rows and columns

                    a1 | a2 | a3
                    --- ---- ---
                    b1 | b2 | b3
                    --- ---- ---
                    c1 | c2 | c3

        /****************************************************************
         * This part is to compute the winner horizontally              *
         ****************************************************************/
        if(a1.getText() != "" && a1.getText() == a2.getText() && a2.getText() == a3.getText()) {
            thereIsWinner = true;
            a1.setBackgroundColor(Color.GREEN); // First Button form left on first row
            a2.setBackgroundColor(Color.GREEN); // Second Button from left on first row
            a3.setBackgroundColor(Color.GREEN); // Third Button from left on first row
        }
        else if(b1.getText() != "" && b1.getText() == b2.getText() && b2.getText() == b3.getText()){
            thereIsWinner = true;
            b1.setBackgroundColor(Color.GREEN); // First Button form left on second row
            b2.setBackgroundColor(Color.GREEN); // Second Button from left on second row
            b3.setBackgroundColor(Color.GREEN); // Third Button from left on second row
        }
        else if(c1.getText() != "" && c1.getText() == c2.getText() && c2.getText() == c3.getText()){
            thereIsWinner = true;
            c1.setBackgroundColor(Color.GREEN); // First Button form left on third row
            c2.setBackgroundColor(Color.GREEN); // Second Button form left on third row
            c3.setBackgroundColor(Color.GREEN); // Third Button form left on third row
        }

        /**************************************************
         * This part is to compute the winner vertically *
         *************************************************/
        else if (a1.getText() != "" && a1.getText() == b1.getText() && b1.getText() == c1.getText()) {
            thereIsWinner = true;
            a1.setBackgroundColor(Color.GREEN);
            b1.setBackgroundColor(Color.GREEN);
            c1.setBackgroundColor(Color.GREEN);
        }

        else if (a2.getText() != "" && a2.getText() == b2.getText() && b2.getText() == c2.getText()) {
            thereIsWinner = true;
            a2.setBackgroundColor(Color.GREEN);
            b2.setBackgroundColor(Color.GREEN);
            c2.setBackgroundColor(Color.GREEN);
        }

        else if (a3.getText() != "" && a3.getText() == b3.getText() && b3.getText() == c3.getText()) {
            thereIsWinner = true;
            a3.setBackgroundColor(Color.GREEN);
            b3.setBackgroundColor(Color.GREEN);
            c3.setBackgroundColor(Color.GREEN);
        }

        /***************************************
         * This part is to check diagonals     *
         ***************************************/
        else if (a1.getText() != "" && a1.getText() == b2.getText() && b2.getText() == c3.getText()) {
            thereIsWinner = true;
            a1.setBackgroundColor(Color.GREEN);
            b2.setBackgroundColor(Color.GREEN);
            c3.setBackgroundColor(Color.GREEN);
        }

        else if (a3.getText() != "" && a3.getText() == b2.getText() && b2.getText() == c1.getText()) {
            thereIsWinner = true;
            a3.setBackgroundColor(Color.GREEN);
            b2.setBackgroundColor(Color.GREEN);
            c1.setBackgroundColor(Color.GREEN);
        }

        // This is to show who is the winner to the screen
        if(thereIsWinner) {
            if(!turn) {
                toast("X is the Winner!");
            }
            else {
                toast("O is the Winner!");
            }

            disableButtons();
        }
        // This is to check if X and O tied (there is no winner in this case)
        else if(turnCounter == 9){
            toast("Tie!");
            disableButtons();
        }
    }

    // This is to disable buttons when the game is over.
    private void disableButtons() {

        for(Button b: buttonArray) {
            b.setClickable(false);
        }
    }

    // This function is to basically reset the game on New Game
    private void resetButtons(){

        for(Button b : buttonArray){
            b.setText("");
            b.setClickable(true);
            b.setBackgroundColor(Color.LTGRAY);
        }
    }

    // This is to show messages to the screen
    private void toast(String respond) {
        // This is to display a message.
        Toast.makeText(getApplicationContext(), respond, Toast.LENGTH_SHORT).show();
    }
}
