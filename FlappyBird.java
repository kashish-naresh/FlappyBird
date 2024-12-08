import java.awt.*;

import java.awt.event.*;
import java.io.*;
import java.util.ArrayList;
import java.util.Random;
import javax.swing.*;

public class FlappyBird extends JPanel implements ActionListener, KeyListener {
    int boardWidth ;
    int boardHeight;

    Image backgroundImg;
    Image birdImg;
    Image topPipeImg;
    Image bottomPipeImg;


    int birdX = boardWidth/8;
    int birdY = boardHeight/2;
    int birdWidth ;
    int birdHeight ;

    class Bird {
        int x = birdX;
        int y = birdY;
        int width = birdWidth;
        int height = birdHeight;
        Image img;

        Bird(Image img) {
            this.img = img;
        }
    }


    int pipeX ;
    int pipeY ;
    int pipeWidth ; 
    int pipeHeight ;

    class Pipe {
        int x = pipeX;
        int y = pipeY;
        int width = pipeWidth;
        int height = pipeHeight;
        Image img;
        boolean passed = false;

        Pipe(Image img) {
            this.img = img;
        }
    }

    Bird bird;
    int velocityX ; 
    int velocityY ;  
    int gravity ;

    ArrayList<Pipe> pipes;
    Random random = new Random();

    Timer gameLoop;
    Timer placePipeTimer;
    boolean gameOver = false ;
    double score ;
    int highScore ;

    File highScoreFile = new File("highscore.txt");

    FlappyBird() {

        boardWidth = Integer.parseInt(ResourceBundleData.getBundleData("boardWidth"));
        boardHeight = Integer.parseInt(ResourceBundleData.getBundleData("boardHeight"));
        pipeX = Integer.parseInt(ResourceBundleData.getBundleData("pipeX"));
        pipeY = Integer.parseInt(ResourceBundleData.getBundleData("pipeY"));
        // birdX = Integer.parseInt(ResourceBundleData.getBundleData("birdX"));
        // birdY = Integer.parseInt(ResourceBundleData.getBundleData("birdY"));
        birdWidth = Integer.parseInt(ResourceBundleData.getBundleData("birdWidth"));
        birdHeight = Integer.parseInt(ResourceBundleData.getBundleData("birdHeight"));
        pipeWidth = Integer.parseInt(ResourceBundleData.getBundleData("pipeWidth"));
        pipeHeight = Integer.parseInt(ResourceBundleData.getBundleData("pipeHeight"));
        velocityX = Integer.parseInt(ResourceBundleData.getBundleData("velocityX"));
        velocityY = Integer.parseInt(ResourceBundleData.getBundleData("velocityY"));
        gravity = Integer.parseInt(ResourceBundleData.getBundleData("gravity"));
        score = Integer.parseInt(ResourceBundleData.getBundleData("score"));
        highScore = Integer.parseInt(ResourceBundleData.getBundleData("highScore"));
        
        setPreferredSize(new Dimension(boardWidth, boardHeight));
        setFocusable(true);
        addKeyListener(this);

        backgroundImg = new ImageIcon(getClass().getResource("./flappybirdbg.png")).getImage();
        birdImg = new ImageIcon(getClass().getResource("./flappybird.png")).getImage();
        topPipeImg = new ImageIcon(getClass().getResource("./toppipe.png")).getImage();
        bottomPipeImg = new ImageIcon(getClass().getResource("./bottompipe.png")).getImage();

        bird = new Bird(birdImg);
        pipes = new ArrayList<Pipe>();

        loadHighScore();

        placePipeTimer = new Timer(1500, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                placePipes();
            }
        });
        placePipeTimer.start();

        gameLoop = new Timer(1000 / 60, this); 
        gameLoop.start();
    }

    void placePipes() {
        int randomPipeY = (int) (pipeY - pipeHeight / 4 - Math.random() * (pipeHeight / 2));
        int openingSpace = boardHeight / 4;

        Pipe topPipe = new Pipe(topPipeImg);
        topPipe.y = randomPipeY;
        pipes.add(topPipe);

        Pipe bottomPipe = new Pipe(bottomPipeImg);
        bottomPipe.y = topPipe.y + pipeHeight + openingSpace;
        pipes.add(bottomPipe);
    }

    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        draw(g);
    }

    public void draw(Graphics g) {
        // Background
        g.drawImage(backgroundImg, 0, 0, this.boardWidth, this.boardHeight, null);

        // Bird
        g.drawImage(birdImg, bird.x, bird.y, bird.width, bird.height, null);

        // Pipes
        for (int i = 0; i < pipes.size(); i++) {
            Pipe pipe = pipes.get(i);
            g.drawImage(pipe.img, pipe.x, pipe.y, pipe.width, pipe.height, null);
        }

        // Score
        g.setColor(Color.white);
        g.setFont(new Font("Arial", Font.PLAIN, 32));
        if (gameOver) {
            g.drawString("Game Over: " + (int) score, 10, 35);
            g.drawString("High Score: " + highScore, 10, 75);
            g.drawString("Press Space to Restart", 10, 115);
            if (score > highScore) {
                g.setColor(Color.green);
                g.drawString("New High Score!", 10, 150);
            }
        } else {
            g.drawString(String.valueOf((int) score), 10, 35);
        }
    }

    public void move() {
        // Bird
        velocityY += gravity;
        bird.y += velocityY;
        bird.y = Math.max(bird.y, 0); 
        // Pipes
        for (int i = 0; i < pipes.size(); i++) {
            Pipe pipe = pipes.get(i);
            pipe.x += velocityX;

            if (!pipe.passed && bird.x > pipe.x + pipe.width) {
                score += 0.5;
                pipe.passed = true;
            }

            if (collision(bird, pipe)) {
                gameOver = true;
                checkHighScore();
            }
        }

        if (bird.y > boardHeight) {
            gameOver = true;
            checkHighScore();
        }
    }

    boolean collision(Bird a, Pipe b) {
        return a.x < b.x + b.width && a.x + a.width > b.x && a.y < b.y + b.height && a.y + a.height > b.y;  
    }

    void checkHighScore() {
        if (score > highScore) {
            highScore = (int) score;
            saveHighScore();
        }
    }

    void loadHighScore() {
        try {
            if (highScoreFile.exists()) {
                BufferedReader reader = new BufferedReader(new FileReader(highScoreFile));
                highScore = Integer.parseInt(reader.readLine());
                reader.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    void saveHighScore() {
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(highScoreFile));
            writer.write(String.valueOf(highScore));
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        move();
        repaint();
        if (gameOver) {
            placePipeTimer.stop();
            gameLoop.stop();
        }
    }

    @Override
    public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_SPACE) {
            if (gameOver) {
                // Restart game
                bird.y = birdY;
                velocityY = 0;
                pipes.clear();
                score = 0;
                gameOver = false;
                gameLoop.start();
                placePipeTimer.start();
            } else if (bird.y > 0) {
                velocityY = -9; 
            }
        }
    }

    @Override
    public void keyTyped(KeyEvent e) {}

    @Override
    public void keyReleased(KeyEvent e) {}
}
