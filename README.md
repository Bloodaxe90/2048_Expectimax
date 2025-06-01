<h1 align="center">2048 with Expectimax</h1>

<h2>Description:</h2>

<p>
After implementing the Minimax algorithm on Connect4 (https://github.com/Bloodaxe90/Connect4_Minimax), I decided to implement the Expectimax algorithm on 2048. 
</p>
<p>
As some may already know using a Q-Table to store the Q-Values for each state-action pair is highly impractical as there are over a trillion different values. Sadly I only realised this after fully impleme am however pretty sure that the code is sound but I havn't found a good way to check it and so i have given up.

Like the Connect4 Minimax project, this one also uses JavaFX, SceneBuilder, Alpha-Beta Pruning and heuristics.
</p>

<h2>Usage:</h2>
<p>
Run the main method in the AI_2048_Application class
</p>

<h2>Controls:</h2>
<p>
  Its 2048!
</p>
<ul>
    <li><strong>Arrow Keys:</strong> Moves the number tiles in the corresponding direction</li>
    <li><strong>Restart Button:</strong> Resets the game.</li>
    <li><strong>Help Button:</strong> Makes the Expectimax AI make a move for you.</li>
    <li><strong>AI Check Box:</strong> Allows the Expectimax AI to take control of the game</li>
</ul>

<h2>Results:</h2>
This project was very successful with the final AI able to consistently reach a score of 2048

![image](https://github.com/user-attachments/assets/d3e2770c-67b0-4c6b-942e-cc1d934f81fd)
