package src;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.Random;



public class TetrisGame extends JFrame {

  private static final int PRESS_DOWN_MODE =0;
  private static final int HOLD_DOWN_MODE = 1;

  private static final int SPEED_RUN_MODE =2;

  private static final int RANDOMIZER_MODE = 3;

  //Grafika


  private static  int CELL_SIZE = 30;
  private static final int BOARD_WIDTH = 10;
  private static final int BOARD_HEIGHT = 26;

  private static final int SPEED_RUN_SPEED = 5;
  private static final int CLASSIC_SPEED = 50;
  private static final int INITIAL_DELAY = 1000; // Początkowe opóźnienie timera (1 sekunda)
  private static final int POINTS_THRESHOLD = 150; //Próg punktowy

    //Grafika
  private static final int WINDOW_WIDTH = 3*BOARD_WIDTH/2 * CELL_SIZE + 6 * CELL_SIZE;
  private static final int WINDOW_HEIGHT= BOARD_HEIGHT * CELL_SIZE+ 3*CELL_SIZE/2 + CELL_SIZE ;

  private boolean isGameStarted = false; // Zmienna śledząca czy gra się rozpoczęła
  private boolean isGameEnded = false; //Zmienna śledząca czy gra się skończyła -> służy do pokazania punktów po akończonej grze
  private int[][] currentShape; // Aktualny klocek
  private int[][] nextShape; //następny klocek

  private int[][] holdShape; // przytrzymany klocek
  private int currentX = BOARD_WIDTH / 2; // Pozycja X aktualnego kloka
  private int currentY = 0; // Pozycja Y aktualnego klocka
  private Color currentColor; // Kolor aktualnego klocka
  private Color nextColor; //Kolor następnego klocka
  private Color holdColor;
  private final Timer timer;
  private final Boolean[][] board = new Boolean[BOARD_HEIGHT][BOARD_WIDTH];
  private boolean isSpeedingUp = false;
  private int score = 0; // Licznik punktów
  private boolean isPaused = false; // Zmienna śledząca stan pauzy w grze
  private final ScoreManager scoreManager = new ScoreManager();

  private final int[] gamemodes = {PRESS_DOWN_MODE,HOLD_DOWN_MODE,SPEED_RUN_MODE,RANDOMIZER_MODE};
  private int gamemodeIndex =0;


  public TetrisGame() {
    setTitle("Tetris");
    setIconImage(Toolkit.getDefaultToolkit().getImage(getClass().getResource("528110.png")));

    setDefaultCloseOperation(EXIT_ON_CLOSE);
    setSize(WINDOW_WIDTH, WINDOW_HEIGHT);

    setLocationRelativeTo(null);

    scoreManager.loadScoresFromFile("scores.txt");

    timer = new Timer(INITIAL_DELAY, e -> {
        if (isSpeedingUp && gamemodes[gamemodeIndex] == HOLD_DOWN_MODE) {
          score += 2;
        }
        if (canMove(currentShape, currentX, currentY + 1)) {
          currentY++;
          repaint();
        } else {
          stopCurrentShape();
          removeFullRows();
          generateNewBlock();
          repaint();
        }
    });
    timer.start();

      JPanel gamePanel = new JPanel() {
          @Override
          protected void paintComponent(Graphics g) {
              super.paintComponent(g);
              drawBackgroundGradient(g);
              if (!isGameStarted && !isGameEnded) {
                drawMainMenu(g);
              }else if (isGameEnded){
                drawScoreAfterFinishedGame(g);
              } else {
                  drawBoard(g);
                  drawScore(g);
                  drawNextShape(g);
                  drawScoreboard(g);
                  drawCurrentBlock(g);
                  drawHoldBlock(g);
                  drawHelpLeftPanel(g);
                  if (isPaused) {
                      drawPauseScreen(g);
                  }
              }
          }
      };


    gamePanel.setBackground(new Color(28, 28, 28, 255));
    add(gamePanel);

    generateNewBlock();

    gamePanel.setFocusable(true);
    gamePanel.requestFocusInWindow();
    gamePanel.addKeyListener(new KeyListener() {
      @Override
      public void keyPressed(KeyEvent e) {
        if ((e.getKeyCode() == KeyEvent.VK_LEFT || e.getKeyCode() == KeyEvent.VK_A ) && !isPaused) {
          if (canMove(currentShape, currentX - 1, currentY) && !isPaused) {
            currentX--; //W lewo
          }
          if (!isGameStarted && !isGameEnded) {
              if (gamemodeIndex>0){
                gamemodeIndex--;
              }
          }
          repaint();
        }else if ((e.getKeyCode() == KeyEvent.VK_RIGHT || e.getKeyCode() == KeyEvent.VK_D) && !isPaused) {
          if (canMove(currentShape, currentX + 1, currentY)){
            currentX++; //w prawo
          }
          if (!isGameStarted && !isGameEnded) {
            if (gamemodeIndex<3) {
              gamemodeIndex++;
            }
          }
          repaint();
        }
        else if ((e.getKeyCode() == KeyEvent.VK_UP || e.getKeyCode() == KeyEvent.VK_W) && !isPaused) {
          rotateShape(); //obróć kloca
          repaint();
        }else if(e.getKeyCode() == KeyEvent.VK_R){
          score = 0;
          holdShape=null;
          restartGame();
        } else if (e.getKeyCode() == KeyEvent.VK_SPACE) {
          if (!isGameStarted && !isGameEnded) {
            restartGame();
            isGameStarted = true;
            holdShape=null;
            repaint();
          } else if (isGameEnded && !isGameStarted){
            isGameEnded=false;
            score = 0;
            holdShape=null;
            repaint();
          } else  {
            togglePause();
          }
        }else if (e.getKeyCode() == KeyEvent.VK_ESCAPE && !isPaused) {
          score = 0;
          isGameStarted=false;
          restartGame();
          holdShape=null;
        }else if (((e.getKeyCode() == KeyEvent.VK_DOWN || e.getKeyCode() == KeyEvent.VK_S) && !isPaused) && (gamemodes[gamemodeIndex]==HOLD_DOWN_MODE || gamemodes[gamemodeIndex]==SPEED_RUN_MODE)) {
          speedUp();
          repaint();
        } else if (e.getKeyCode() == KeyEvent.VK_EQUALS ) {
          CELL_SIZE++;
          repaint();
        } else if (e.getKeyCode() == KeyEvent.VK_MINUS) {
          if (CELL_SIZE>=2) {
            CELL_SIZE--;
          }
          repaint();
        } else if (e.getKeyCode() == KeyEvent.VK_Q){
          if (holdShape == null){
            holdShape = currentShape;
            holdColor = currentColor;
            generateNewBlock();
          }else {
            currentX = (BOARD_WIDTH / 2)-1;
            int[][] temp = holdShape;
            Color tempColor = holdColor;
            holdShape = currentShape;
            holdColor = currentColor;
            currentShape = temp;
            currentColor = tempColor;
          }
          repaint();
        }
      }
      @Override
      public void keyTyped(KeyEvent e) {}

      @Override
      public void keyReleased(KeyEvent e) {
        if ((e.getKeyCode() == KeyEvent.VK_DOWN || e.getKeyCode() == KeyEvent.VK_S) && !isPaused) {
          if (gamemodes[gamemodeIndex] == HOLD_DOWN_MODE) {
            isSpeedingUp = false;
            updateTimerDelay();
            repaint();
          }else if (gamemodes[gamemodeIndex] == PRESS_DOWN_MODE || gamemodes[gamemodeIndex]==RANDOMIZER_MODE){
            speedUp();
            repaint();
          }
        }
      }
    });

    setVisible(true);
  }

  public Point returnMovingVector(){
    int centerX = getWidth() / 2;
    int centerY = getHeight() / 2;
    int rectWidth  =  BOARD_WIDTH*CELL_SIZE +CELL_SIZE;
    int rectHeight =  BOARD_HEIGHT*CELL_SIZE + CELL_SIZE/2;
    int rectX = centerX - rectWidth / 2;
    int rectY = centerY - rectHeight / 2;
    return new Point(rectX,rectY);
  }
  private void drawBackgroundGradient(Graphics g) {
    Graphics2D g2d = (Graphics2D)g;
    g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
    int w = getWidth(), h = getHeight();
    Color color1 = new Color(28, 28, 28, 255);
    Color color2 = new Color(48, 95, 105, 255);
    GradientPaint gp = new GradientPaint((float) w /2, 0, color1, (float) w /2, h, color2);
    g2d.setPaint(gp);
    g2d.fillRect(0, 0, w, h);
  }
  private void drawMainMenu(Graphics g) {
    int centerX = getWidth() / 2;
    int centerY = getHeight() / 2;
    Font font = new Font("Arial", Font.BOLD, CELL_SIZE*7/2);
    g.setFont(font);
    FontMetrics metrics = g.getFontMetrics(font);
    String title = "TETRIS";
    String startMessage = "Press Space";
    String middleMessage = "to";
    String endMessage = "Start Game";
    int titleWidth = metrics.stringWidth(title);
    int titleX =  centerX - titleWidth/2;
    int y = centerY - 5*CELL_SIZE ;
    g.setColor(Color.CYAN);
    g.drawString("T", titleX, y);
    g.setColor(Color.BLUE);
    g.drawString("E", titleX + metrics.stringWidth("T"), y);
    g.setColor(Color.ORANGE);
    g.drawString("T", titleX + metrics.stringWidth("TE"), y);
    g.setColor(Color.YELLOW);
    g.drawString("R", titleX + metrics.stringWidth("TET"), y);
    g.setColor(Color.GREEN);
    g.drawString("I", titleX + metrics.stringWidth("TETR"), y);
    g.setColor(Color.MAGENTA);
    g.drawString("S", titleX + metrics.stringWidth("TETRI"), y);
    g.setColor(Color.WHITE);
    Font Lowerfont = new Font("Arial", Font.PLAIN, 4*CELL_SIZE/3);
    g.setFont(Lowerfont);
    FontMetrics newFontMetrics = g.getFontMetrics(Lowerfont);
    int startMessageWidth = newFontMetrics.stringWidth(startMessage);
    int middleMessageWidth = newFontMetrics.stringWidth(middleMessage);
    int endMessageWidth = newFontMetrics.stringWidth(endMessage);
    int startMessageX = (getWidth() - startMessageWidth) / 2;
    int middleMessageX = (getWidth() - middleMessageWidth) / 2;
    int endMessageX = (getWidth() - endMessageWidth) / 2;
    g.drawString(startMessage, startMessageX, y + CELL_SIZE*5);
    g.drawString(middleMessage,middleMessageX, y+20*CELL_SIZE/3);
    g.drawString(endMessage,endMessageX, y+25*CELL_SIZE/3);
    Font smallerLowerFont = new Font("Noto Emoji",Font.BOLD,CELL_SIZE/2+1);
    g.setFont(smallerLowerFont);

    g.drawString("GAMEMODE",(getWidth() - g.getFontMetrics(smallerLowerFont).stringWidth("GAMEMODE"))/2, y+32*CELL_SIZE/3);
    Font emojiFont = new Font("Noto Emoji",Font.PLAIN,CELL_SIZE/2+1);
    g.setFont(emojiFont);
    if (gamemodes[gamemodeIndex]==HOLD_DOWN_MODE){
      g.setColor(new Color(0x4E9F9F));
      g.drawString("\uD83C\uDF96 HOLD DOWN MODE",(getWidth() - g.getFontMetrics(smallerLowerFont).stringWidth("\uD83C\uDF96 HOLD DOWN MODE"))/2, y+35*CELL_SIZE/3);
    }
    if (gamemodes[gamemodeIndex]==PRESS_DOWN_MODE){
      g.setColor(new Color(0x498849));
      g.drawString("⬇ PRESS DOWN MODE", (getWidth() - g.getFontMetrics(smallerLowerFont).stringWidth("⬇ PRESS DOWN MODE"))/2, y+35*CELL_SIZE/3);
    }
    if (gamemodes[gamemodeIndex]==SPEED_RUN_MODE){
      g.setColor(new Color(0xA62E2E));
      g.drawString("☠ SPEED RUN MODE", (getWidth() - g.getFontMetrics(smallerLowerFont).stringWidth("☠ SPEED RUN MODE"))/2, y+35*CELL_SIZE/3);
    }
    if (gamemodes[gamemodeIndex]==RANDOMIZER_MODE){
      g.setColor(new Color(0xCC2AF8));
      g.drawString("⭐ RANDOMIZER MODE", (getWidth() - g.getFontMetrics(smallerLowerFont).stringWidth("⭐ RANDOMIZER MODE"))/2, y+35*CELL_SIZE/3);
    }

  }

  private void generateNewBlock() {
    Tetromino randomTetromino = Tetromino.values()[new Random().nextInt(Tetromino.values().length)];

    if (gamemodes[gamemodeIndex]!=RANDOMIZER_MODE) {
      if (nextShape == null) {
        nextShape = randomTetromino.getShape();
        nextColor = randomTetromino.getColor();
        currentShape = randomTetromino.getShape();
        currentColor = randomTetromino.getColor();
      } else {
        currentShape = nextShape;
        currentColor = nextColor;
        nextShape = randomTetromino.getShape();
        nextColor = randomTetromino.getColor();
      }
    }
    else {
      Random rand = new Random();

      if (nextShape == null) {
        nextShape = new int[][]{{rand.nextInt(2) , 1, rand.nextInt(2)}, {rand.nextInt(2), rand.nextInt(2),rand.nextInt(2)}};
        nextColor = new Color(rand.nextInt(255)+1,rand.nextInt(255)+1,rand.nextInt(255)+1);
        currentShape = new int[][]{{rand.nextInt(2) , rand.nextInt(2), rand.nextInt(2)}, {rand.nextInt(2), rand.nextInt(2),rand.nextInt(2)}};
        currentColor = new Color(rand.nextInt(255)+1,rand.nextInt(255)+1,rand.nextInt(255)+1);
      } else {
        currentShape = nextShape;
        currentColor = nextColor;
        nextShape =  new int[][]{{rand.nextInt(2) ,1, rand.nextInt(2)}, {rand.nextInt(2), rand.nextInt(2),rand.nextInt(2)}};
        nextColor = new Color(rand.nextInt(255)+1,rand.nextInt(255)+1,rand.nextInt(255)+1);
      }
    }
    currentX = BOARD_WIDTH / 2 - currentShape[0].length / 2;
    currentY = 0;
    isSpeedingUp = false;
    updateTimerDelay();

  }
  private void restartGame() {
    generateNewBlock();
    for (int y = 0; y < BOARD_HEIGHT; y++) {
      for (int x = 0; x < BOARD_WIDTH; x++) {
        board[y][x] = null;
      }
    }
    repaint();
  }
  private void drawInsideOfBlock(Graphics g, int x, int y,Color color){
    Color brighterColor1 = new Color(color.getRed()/2 , color.getGreen()/2 , color.getBlue()/2);
    Color brighterColor2 = new Color(color.getRed()/3 , color.getGreen()/3 , color.getBlue()/3);
    Color brighterColor3 = new Color(color.getRed()/4 , color.getGreen()/4 , color.getBlue()/4);
    int[] xPoints = {x ,x+CELL_SIZE,x+CELL_SIZE/6+ 2*CELL_SIZE/3,x+CELL_SIZE/6};
    int[] yPoints = {y, y, y+CELL_SIZE/6, y+CELL_SIZE/6};
    g.setColor(brighterColor1);
    g.fillPolygon(xPoints,yPoints,4);
    xPoints = new int[]{x, x,             x + CELL_SIZE / 6, x + CELL_SIZE/6};
    yPoints = new int[]{y, y + CELL_SIZE, y + CELL_SIZE / 6 + 2*CELL_SIZE/3, y + CELL_SIZE / 6};
    g.setColor(brighterColor2);
    g.fillPolygon(xPoints,yPoints,4);
    xPoints = new int[]{x+CELL_SIZE, x+CELL_SIZE, x + CELL_SIZE - CELL_SIZE/6, x + CELL_SIZE - CELL_SIZE/6};
    yPoints = new int[]{y, y + CELL_SIZE, y + CELL_SIZE -CELL_SIZE/6, y + CELL_SIZE / 6};
    g.setColor(brighterColor3);
    g.fillPolygon(xPoints,yPoints,4);
    g.setColor(color.darker());
    g.fillRect(x+CELL_SIZE/6,y+CELL_SIZE/6,2*CELL_SIZE/3,2*CELL_SIZE/3);
  }
  private void drawBoard(Graphics g) {
    Point p = returnMovingVector();
    g.setColor(Color.WHITE);
    g.drawRect(p.x, p.y, BOARD_WIDTH * CELL_SIZE, BOARD_HEIGHT * CELL_SIZE);
    for (int y = 0; y < BOARD_HEIGHT; y++) {
      for (int x = 0; x < BOARD_WIDTH; x++) {
          g.setColor(new Color(219, 246, 255, 13));
          g.drawRect(p.x + x * CELL_SIZE, p.y + y * CELL_SIZE, CELL_SIZE, CELL_SIZE);
          g.setColor(Color.WHITE);
        if (board[y][x] != null && board[y][x]) {
          g.setColor(Color.WHITE);
          g.fillRect(p.x +x * CELL_SIZE, p.y +y * CELL_SIZE, CELL_SIZE, CELL_SIZE);
          drawInsideOfBlock(g,p.x + x * CELL_SIZE, p.y +y * CELL_SIZE,Color.WHITE);
          g.setColor(Color.BLACK);
          g.drawRect(p.x + x * CELL_SIZE, p.y + y * CELL_SIZE, CELL_SIZE, CELL_SIZE);
        }
      }
    }
  }
  private void drawScoreAfterFinishedGame(Graphics g){
    Font font = new Font("Arial", Font.BOLD, CELL_SIZE);
    g.setFont(font);
    FontMetrics metrics = g.getFontMetrics(font);
    String message = "YOUR SCORE: " + score;
    int messageWidth = metrics.stringWidth(message);
    int x = (getWidth() - messageWidth) / 2;
    int y = getHeight() / 2;
    g.setColor(Color.WHITE);
    g.fillRect(x - CELL_SIZE/3, y - 4*CELL_SIZE/3, messageWidth + 2*CELL_SIZE/3, 5*CELL_SIZE/3);
    g.setColor(Color.BLACK);
    g.drawString(message, x, y);
  }
  private void drawScoreboard(Graphics g){
    Point p = returnMovingVector();


    g.setColor(Color.WHITE);
    g.drawRect(p.x + BOARD_WIDTH*CELL_SIZE, p.y+ 4*CELL_SIZE,5*CELL_SIZE,BOARD_HEIGHT*CELL_SIZE-4*CELL_SIZE);



    Font accutalFont = g.getFont();
    Font previousFont = accutalFont;
    accutalFont = accutalFont.deriveFont(((float)CELL_SIZE/3 +2));
    g.setFont(accutalFont);
    FontMetrics metrics = getFontMetrics(accutalFont);
    g.setColor(Color.WHITE);
    g.drawString("Scoreboard", p.x + BOARD_WIDTH*CELL_SIZE + (5*CELL_SIZE)/2 -  metrics.stringWidth("Scoreboard")/2,p.y + 4*CELL_SIZE+CELL_SIZE/2);

    accutalFont = accutalFont.deriveFont(((float)CELL_SIZE/3));
    g.setFont(accutalFont);
    metrics = getFontMetrics(accutalFont);

    for (int i = 0; i < scoreManager.scores.size(); i++) {
      ScoreEntry entry = scoreManager.scores.get(i);
      g.drawString(entry.timestamp() + " " + entry.score(), p.x+ BOARD_WIDTH*CELL_SIZE  + (5*CELL_SIZE)/2
              -metrics.stringWidth(entry.timestamp() + " " + entry.score())/2,p.y + (5*CELL_SIZE) + metrics.getHeight()*i );
    }

    g.setFont(previousFont);
  }

  private void drawNextShape(Graphics g){
    Point p = returnMovingVector();
    g.setColor(Color.WHITE);
    g.drawRect(p.x+ BOARD_WIDTH*CELL_SIZE,p.y,5*CELL_SIZE,4*CELL_SIZE);

    Font accutalFont = g.getFont();
    Font previousFont = accutalFont;
    accutalFont = accutalFont.deriveFont(((float)CELL_SIZE/3 +2));
    g.setFont(accutalFont);
    FontMetrics metrics = getFontMetrics(accutalFont);

    g.drawString("Next shape", p.x+ BOARD_WIDTH*CELL_SIZE +(5*CELL_SIZE)/2 - metrics.stringWidth("Next shape")/2,p.y + CELL_SIZE/2);
    g.setColor(nextColor);
    double tetrominoPadding = 1;
    if (nextShape == Tetromino.I_SHAPE.getShape()){
      tetrominoPadding = 0.5;
    }
    if (nextShape == Tetromino.O_SHAPE.getShape()){
      tetrominoPadding = 1.5;
    }
    for (int y=0;y<nextShape.length;y++){
      for (int x =0; x<nextShape[y].length; x++){
        if(nextShape[y][x]==1){
          g.fillRect((int) (p.x + (BOARD_WIDTH+x)*CELL_SIZE+(CELL_SIZE*(tetrominoPadding))),p.y + (y)*CELL_SIZE+CELL_SIZE, CELL_SIZE, CELL_SIZE);
          drawInsideOfBlock(g,(int) (p.x + (BOARD_WIDTH+x)*CELL_SIZE+(CELL_SIZE*(tetrominoPadding))),p.y + (y)*CELL_SIZE+CELL_SIZE,nextColor);
          g.setColor(nextColor);
        }
      }
    }
    g.setFont(previousFont);
  }

  void drawHoldBlock(Graphics g){
    Point p = returnMovingVector();
    p.x -= 5*CELL_SIZE;
    g.setColor(Color.WHITE);
    g.drawRect(p.x,p.y,5*CELL_SIZE,4*CELL_SIZE);
    Font accutalFont = g.getFont();
    Font previousFont = accutalFont;
    accutalFont = accutalFont.deriveFont(((float)CELL_SIZE/3 +2));
    g.setFont(accutalFont);
    FontMetrics metrics = getFontMetrics(accutalFont);

    g.drawString("Hold shape", p.x+ (5*CELL_SIZE)/2 - metrics.stringWidth("Hold shape")/2,p.y + CELL_SIZE/2);
    if (holdShape!=null) {
      g.setColor(holdColor);
      double tetrominoPadding = 1;
      if (holdShape == Tetromino.I_SHAPE.getShape()) {
        tetrominoPadding = 0.5;
      }
      if (holdShape == Tetromino.O_SHAPE.getShape()) {
        tetrominoPadding = 1.5;
      }
      for (int y = 0; y < holdShape.length; y++) {
        for (int x = 0; x < holdShape[y].length; x++) {
          if (holdShape[y][x] == 1) {
            g.fillRect((int) (p.x + x * CELL_SIZE + (CELL_SIZE * (tetrominoPadding))), p.y + (y) * CELL_SIZE + CELL_SIZE, CELL_SIZE, CELL_SIZE);
            drawInsideOfBlock(g, (int) (p.x + x * CELL_SIZE + (CELL_SIZE * (tetrominoPadding))), p.y + (y) * CELL_SIZE + CELL_SIZE, holdColor);
            g.setColor(holdColor);
          }
        }
      }
    }
    g.setFont(previousFont);
  }

  private void drawHelpLeftPanel(Graphics g){
    Point p = returnMovingVector();
    p.x -= 5*CELL_SIZE;
    p.y += 4*CELL_SIZE;
    g.setColor(Color.WHITE);
    g.drawRect(p.x,p.y,5*CELL_SIZE,BOARD_HEIGHT*CELL_SIZE-4*CELL_SIZE);
    Font accutalFont = g.getFont();
    Font previousFont = accutalFont;
    accutalFont = accutalFont.deriveFont(((float)CELL_SIZE/3 +2));
    g.setFont(accutalFont);
    FontMetrics metrics = getFontMetrics(accutalFont);
    int distanceBetweenLines = CELL_SIZE/2 + CELL_SIZE/3;
    int leftPadding = (int) (p.x+ 1.5* (CELL_SIZE)/2);
    g.drawString("Controls", p.x+ (5*CELL_SIZE)/2 - metrics.stringWidth("Controls")/2,p.y + CELL_SIZE/2);
    p.y+=distanceBetweenLines;
    g.drawString("A/⬅ : move left", leftPadding,p.y + CELL_SIZE/2);
    p.y+=distanceBetweenLines;
    g.drawString("D/➡ : move right", leftPadding,p.y + CELL_SIZE/2);
    p.y+=distanceBetweenLines;
    g.drawString("S/⬇ : speed up block", leftPadding,p.y + CELL_SIZE/2);
    p.y+=distanceBetweenLines;
    g.drawString("W/⬆ : rotate block", leftPadding,p.y + CELL_SIZE/2);
    p.y+=distanceBetweenLines;
    g.drawString("Q : hold block", leftPadding,p.y + CELL_SIZE/2);
    p.y+=distanceBetweenLines;
    g.drawString("R : reset lever", leftPadding,p.y + CELL_SIZE/2);
    p.y+=distanceBetweenLines;
    g.drawString("SPACE : pause game", leftPadding,p.y + CELL_SIZE/2);
    p.y+=distanceBetweenLines;
    g.drawString("ESC : back to menu", leftPadding,p.y + CELL_SIZE/2);
    p.y+=distanceBetweenLines;
    g.drawString("- : zoom out", leftPadding,p.y + CELL_SIZE/2);
    p.y+=distanceBetweenLines;
    g.drawString("+ : zoom in", leftPadding,p.y + CELL_SIZE/2);



    g.setFont(previousFont);
  }


  private void drawCurrentBlock(Graphics g) {
    Point p = returnMovingVector();
    g.setColor(currentColor);
    for (int y = 0; y < currentShape.length; y++) {
      for (int x = 0; x < currentShape[y].length; x++) {
        if (currentShape[y][x] == 1) {
          g.fillRect(p.x + (currentX + x) * CELL_SIZE, p.y + (currentY + y) * CELL_SIZE, CELL_SIZE, CELL_SIZE);
          drawInsideOfBlock(g,p.x + (currentX + x) * CELL_SIZE, p.y + (currentY + y) * CELL_SIZE,currentColor);
          g.setColor(currentColor);
        }
      }
    }
  }

  private void drawScore(Graphics g) {
    Point p = returnMovingVector();
    Font accutalFont = g.getFont();
    Font previousFont = accutalFont;
    accutalFont = accutalFont.deriveFont(((float)CELL_SIZE/3 + 2));
    g.setFont(accutalFont);
    g.setColor(Color.WHITE);
    g.drawString("Score: " + score, p.x+CELL_SIZE/3 , p.y+2*CELL_SIZE/3);
    g.setFont(previousFont);
  }


  private void drawPauseScreen(Graphics g) {
    Font font = new Font("Arial", Font.BOLD, CELL_SIZE);
    g.setFont(font);
    FontMetrics metrics = g.getFontMetrics(font);
    String message = "GAME IS PAUSED";
    int messageWidth = metrics.stringWidth(message);
    int x = (getWidth() - messageWidth) / 2;
    int y = getHeight() / 2;
    g.setColor(Color.WHITE);
    g.fillRect(x - CELL_SIZE/3, y - 4*CELL_SIZE/3, messageWidth + 2*CELL_SIZE/3, 5*CELL_SIZE/3);
    g.setColor(Color.BLACK);
    g.drawString(message, x, y);
  }
  private boolean canMove(int[][] shape, int newX, int newY) {
    for (int y = 0; y < shape.length; y++) {
      for (int x = 0; x < shape[y].length; x++) {
        if (shape[y][x] == 1) {
          int boardX = newX + x;
          int boardY = newY + y;
          if (boardX < 0 || boardX >= BOARD_WIDTH || boardY >= BOARD_HEIGHT || boardY >= 0 && board[boardY][boardX] != null && board[boardY][boardX]) {
            return false;
          }
        }
      }
    }
    return true;
  }

  private void stopCurrentShape() {
    for (int y = 0; y < currentShape.length; y++) {
      for (int x = 0; x < currentShape[y].length; x++) {
        if (currentShape[y][x] == 1) {
            board[currentY + y][currentX + x] = true;
          }
        }
    }
    if (isHighestRowInUsage()) {
      scoreManager.addScore(score);
      scoreManager.saveScoresToFile("scores.txt");
      isGameStarted=false;
      isGameEnded = true;
      restartGame();
    }
  }

  private void togglePause() {
    isPaused = !isPaused;
    if (isPaused) {
      timer.stop();
      repaint();
    } else {
      timer.start();
      repaint();
    }
  }

  private void rotateShape() {
    int[][] newShape = new int[currentShape[0].length][currentShape.length];
    for (int i = 0; i < currentShape.length; i++) {
      for (int j = 0; j < currentShape[0].length; j++) {
        newShape[j][currentShape.length - 1 - i] = currentShape[i][j];
      }
    }
    if (canMove(newShape, currentX, currentY)) {
      currentShape = newShape;
    }
  }

  private void speedUp() {
    int ARROW_DOWN_SPEED_UP = CLASSIC_SPEED;
    if (gamemodes[gamemodeIndex] == SPEED_RUN_MODE){
      ARROW_DOWN_SPEED_UP = SPEED_RUN_SPEED;
    }
    if (!isSpeedingUp) {
      timer.setDelay(ARROW_DOWN_SPEED_UP);
      isSpeedingUp = true;
    }
  }

  private void updateTimerDelay() {
    int newDelay = INITIAL_DELAY - (score / POINTS_THRESHOLD) * 25;
    if (newDelay < 100) {
      newDelay = 100;
    }
    timer.setDelay(newDelay);
  }
  private boolean isHighestRowInUsage() {
    int highestRow = 0;
    int middleColumn = BOARD_WIDTH / 2;
    for (int x = middleColumn - 1; x <= middleColumn + 1; x++) {
      if (board[highestRow][x] != null && board[highestRow][x]) {
        return true;
      }
    }
    return false;
  }
  private void removeFullRows() {
    int fullRows = 0;
    for (int y = 0; y < BOARD_HEIGHT; y++) {
      boolean isFull = true;
      for (int x = 0; x < BOARD_WIDTH; x++) {
        if (board[y][x] == null || !board[y][x]) {
          isFull = false;
          break;
        }
      }
      if (isFull) {
        for (int yy = y; yy > 0; yy--) {
          System.arraycopy(board[yy - 1], 0, board[yy], 0, BOARD_WIDTH);
        }
        fullRows++;
      }
    }
    score += fullRows * 100;
    updateTimerDelay();
  }


  public static void main(String[] args) {
    SwingUtilities.invokeLater(TetrisGame::new);
  }
}


