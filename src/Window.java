import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Window extends JPanel implements MouseListener {

    private final int SCREEN_WIDTH = 800, SCREEN_HEIGHT = 640;
    private final int OFFSET_X = 143, OFFSET_Y = 65;
    private final int TILE_SIZE = 64;

    private final Image boardImg = new ImageIcon(getClass().getClassLoader().getResource("board.png")).getImage();
    private final Image logoImg = new ImageIcon(getClass().getClassLoader().getResource("logo.png")).getImage();
    Map<String, Image> map = new HashMap<>();

    private Piece[][] board = new Piece[8][8];

    private ArrayList<String> whiteCapture = new ArrayList<>();
    private ArrayList<String> blackCapture = new ArrayList<>();
    private final int WHITE_OFFSET_X = 705, WHITE_OFFSET_Y = 590;
    private final int BLACK_OFFSET_X = 60, BLACK_OFFSET_Y = 20;

    private ArrayList<Tile> tiles = new ArrayList<>();

    private int mouseX, mouseY;
    private int selectedX, selectedY;
    private boolean selected;
    private boolean turn = true;
    private boolean rookCanKill;

    private Piece capturedPiece;
    private Jail jailB1 = new Jail(-1.6, 3);
    private Jail jailB2 = new Jail(-1.6, 4);
    private Jail jailW1 = new Jail(8.6, 3);
    private Jail jailW2 = new Jail(8.6, 4);
    private Jail jailSelecting, canRescue, jailRescuing;

    private Piece bear = new Piece("bear");
    private boolean bearSelecting, bearSelected, bearInPlay;

    private final boolean freeMove = false;
    public static boolean activeGame = true;

    public Window()
    {
        setPreferredSize(new Dimension(SCREEN_WIDTH, SCREEN_HEIGHT));

        try
        {
            BufferedImage piecesImg = ImageIO.read(getClass().getClassLoader().getResource("pieces.png"));
            Image[] pieces;
            int size = 128;
            int rows = 2;
            int cols = 8;
            pieces = new Image[rows * cols];
            for(int i = 0; i < rows; i++)
                for(int j = 0; j < cols; j++)
                    pieces[(cols * i) + j] = piecesImg.getSubimage(j * size, i * size, size, size);

            map.put("wFish", pieces[0]);
            map.put("wRook", pieces[1]);
            map.put("wMonkey", pieces[2]);
            map.put("wElephant", pieces[3]);
            map.put("wQueen", pieces[4]);
            map.put("wKing", pieces[5]);
            map.put("wFishQueen", pieces[6]);
            map.put("bear", pieces[7]);

            map.put("bFish", pieces[8]);
            map.put("bRook", pieces[9]);
            map.put("bMonkey", pieces[10]);
            map.put("bElephant", pieces[11]);
            map.put("bQueen", pieces[12]);
            map.put("bKing", pieces[13]);
            map.put("bFishQueen", pieces[14]);
            map.put("banana", pieces[15]);

            setupPieces();
        }
        catch(Exception e)
        {
            System.out.println(e.getMessage());
        }
    }

    public void update()
    {
        if(activeGame)
        {
            double cursorX = (MouseInfo.getPointerInfo().getLocation().getX() - OFFSET_X - this.getLocationOnScreen().x) / TILE_SIZE;
            double cursorY = (MouseInfo.getPointerInfo().getLocation().getY() - OFFSET_Y - this.getLocationOnScreen().y) / TILE_SIZE;

            if(!bearInPlay && cursorX > 3.5 && cursorX < 4.5 && cursorY > 3.5 && cursorY < 4.5)
                bearSelecting = true;
            else
            {
                bearSelecting = false;
                mouseX = (int)Math.floor(cursorX);
                mouseY = (int)Math.floor(cursorY);
            }

            if(capturedPiece != null)
            {
                if(turn)
                {
                    if(selectingJail(jailW1, cursorX, cursorY) && !jailW1.hasPiece())
                        jailSelecting = jailW1;
                    else if(selectingJail(jailW2, cursorX, cursorY) && !jailW2.hasPiece())
                        jailSelecting = jailW2;
                    else
                        jailSelecting = null;
                }
                else
                {
                    if(selectingJail(jailB1, cursorX, cursorY) && !jailB1.hasPiece())
                        jailSelecting = jailB1;
                    else if(selectingJail(jailB2, cursorX, cursorY) && !jailB2.hasPiece())
                        jailSelecting = jailB2;
                    else
                        jailSelecting = null;
                }
            }

            if(canRescue != null)
            {
                if(selectingJail(canRescue, cursorX, cursorY))
                    jailSelecting = canRescue;
                else
                    jailSelecting = null;
            }
        }
    }

    public void paintComponent(Graphics g)
    {
        Graphics2D graphics = (Graphics2D) g;
        super.paintComponent(g);

        // turn
        graphics.setColor(Color.black);
        if(turn)
            graphics.drawString("White's turn", 15, 618);
        else
            graphics.drawString("Black's turn", 15, 618);

        // draw board
        graphics.drawImage(boardImg,
                SCREEN_WIDTH/2 - boardImg.getWidth(null)/4,
                SCREEN_HEIGHT/2 - boardImg.getHeight(null)/4,
                boardImg.getWidth(null)/2,
                boardImg.getHeight(null)/2,
                null);

        // draw highlight
        graphics.setColor(new Color(255, 192, 192, 192));
        for(Tile tile : tiles)
        {
            graphics.fillRect(
                    tile.x * TILE_SIZE + OFFSET_X + (TILE_SIZE - 48)/2,
                    tile.y * TILE_SIZE + OFFSET_Y + (TILE_SIZE - 48)/2,
                    48, 48);
        }
        if(capturedPiece != null)
        {
            if(turn)
            {
                if(!jailW1.hasPiece())
                    graphics.fillRect(
                        (int)(jailW1.x * TILE_SIZE + OFFSET_X + (TILE_SIZE - 48)/2),
                        (int)(jailW1.y * TILE_SIZE + OFFSET_Y + (TILE_SIZE - 48)/2),
                        48, 48);
                if(!jailW2.hasPiece())
                    graphics.fillRect(
                            (int)(jailW2.x * TILE_SIZE + OFFSET_X + (TILE_SIZE - 48)/2),
                            (int)(jailW2.y * TILE_SIZE + OFFSET_Y + (TILE_SIZE - 48)/2),
                            48, 48);
            }
            else
            {
                if(!jailB1.hasPiece())
                    graphics.fillRect(
                            (int)(jailB1.x * TILE_SIZE + OFFSET_X + (TILE_SIZE - 48)/2),
                            (int)(jailB1.y * TILE_SIZE + OFFSET_Y + (TILE_SIZE - 48)/2),
                            48, 48);
                if(!jailB2.hasPiece())
                    graphics.fillRect(
                            (int)(jailB2.x * TILE_SIZE + OFFSET_X + (TILE_SIZE - 48)/2),
                            (int)(jailB2.y * TILE_SIZE + OFFSET_Y + (TILE_SIZE - 48)/2),
                            48, 48);
            }
        }
        if(canRescue != null)
        {
            graphics.fillRect(
                    (int)(canRescue.x * TILE_SIZE + OFFSET_X + (TILE_SIZE - 48)/2),
                    (int)(canRescue.y * TILE_SIZE + OFFSET_Y + (TILE_SIZE - 48)/2),
                    48, 48);
        }

        // draw pieces
        for(int i = 0; i < board.length; i++)
        {
            for(int j = 0; j < board[i].length; j++)
            {
                if(board[i][j] != null)
                {
                    Image img = map.get(board[i][j].getName());
                    graphics.drawImage(img,
                            (j * TILE_SIZE) + OFFSET_X,
                            (i * TILE_SIZE) + OFFSET_Y,
                            img.getWidth(null)/2,
                            img.getHeight(null)/2,
                            null);

                    if(board[i][j].hasBanana())
                    {
                        Image banana = map.get("banana");
                        graphics.drawImage(banana,
                                (j * TILE_SIZE) + OFFSET_X,
                                (i * TILE_SIZE) + OFFSET_Y,
                                img.getWidth(null)/2,
                                img.getHeight(null)/2,
                                null);
                    }
                }
            }
        }

        //bear
        Image bearImg = map.get("bear");
        if(!bearInPlay)
        {
            graphics.drawImage(bearImg,
                    (int)(3.5 * TILE_SIZE) + OFFSET_X,
                    (int)(3.5 * TILE_SIZE) + OFFSET_Y,
                    bearImg.getWidth(null)/2,
                    bearImg.getHeight(null)/2,
                    null);
        }

        // selected piece
        if(selected)
        {
            graphics.setColor(new Color(255, 255, 192, 127));

            if(bearSelected && !bearInPlay)
            {
                graphics.fillRect(
                        (int)(3.5 * TILE_SIZE + OFFSET_X),
                        (int)(3.5 * TILE_SIZE + OFFSET_Y),
                        TILE_SIZE, TILE_SIZE);
            }
            else
            {
                graphics.fillRect(
                        selectedX * TILE_SIZE + OFFSET_X,
                        selectedY * TILE_SIZE + OFFSET_Y,
                        TILE_SIZE, TILE_SIZE);
            }
        }

        // mouse
        if(mouseX >= 0 && mouseX < 8 && mouseY >= 0 && mouseY < 8)
        {
            graphics.setColor(new Color(192, 192, 255, 127));

            if(bearSelecting && !bearInPlay)
                graphics.fillRect(
                        (int)(3.5 * TILE_SIZE + OFFSET_X),
                        (int)(3.5 * TILE_SIZE + OFFSET_Y),
                        TILE_SIZE, TILE_SIZE);
            else
            {
                graphics.fillRect(
                        mouseX * TILE_SIZE + OFFSET_X,
                        mouseY * TILE_SIZE + OFFSET_Y,
                        TILE_SIZE, TILE_SIZE);
            }
        }

        // mouse jail selecting
        if(jailSelecting != null)
        {
            graphics.setColor(new Color(192, 192, 255, 127));
            graphics.fillRect(
                    (int)(jailSelecting.x * TILE_SIZE + OFFSET_X),
                    (int)(jailSelecting.y * TILE_SIZE + OFFSET_Y),
                    TILE_SIZE, TILE_SIZE);
        }

        // captured pieces
        for(int i = 0; i < whiteCapture.size(); i++)
        {
            Image img = map.get(whiteCapture.get(i));
            int imgX = (i / 7 * TILE_SIZE/2);
            int imgY = (i % 7 * TILE_SIZE/2);

            graphics.drawImage(img,
                    WHITE_OFFSET_X + imgX,
                    WHITE_OFFSET_Y - imgY,
                    img.getWidth(null)/4,
                    img.getHeight(null)/4,
                    null);
        }

        for(int i = 0; i < blackCapture.size(); i++)
        {
            Image img = map.get(blackCapture.get(i));
            int imgX = (i / 7 * TILE_SIZE/2);
            int imgY = (i % 7 * TILE_SIZE/2);

            graphics.drawImage(img,
                    BLACK_OFFSET_X - imgX,
                    BLACK_OFFSET_Y + imgY,
                    img.getWidth(null)/4,
                    img.getHeight(null)/4,
                    null);
        }

        // jail
        if(jailB1.hasPiece())
        {
            Image img = map.get(jailB1.getPiece().getName());
            graphics.drawImage(img,
                    (int)(jailB1.x * TILE_SIZE + OFFSET_X),
                    (int)(jailB1.y * TILE_SIZE + OFFSET_Y),
                    img.getWidth(null)/2,
                    img.getHeight(null)/2,
                    null);
        }
        if(jailB1.hasPiece())
        {
            Image img = map.get(jailB1.getPiece().getName());
            graphics.drawImage(img,
                    (int)(jailB1.x * TILE_SIZE + OFFSET_X),
                    (int)(jailB1.y * TILE_SIZE + OFFSET_Y),
                    img.getWidth(null)/2,
                    img.getHeight(null)/2,
                    null);
            if(jailB1.getPiece().hasBanana())
                graphics.drawImage(map.get("banana"),
                        (int)(jailB1.x * TILE_SIZE + OFFSET_X),
                        (int)(jailB1.y * TILE_SIZE + OFFSET_Y),
                        img.getWidth(null)/2,
                        img.getHeight(null)/2,
                        null);
        }
        if(jailB2.hasPiece())
        {
            Image img = map.get(jailB2.getPiece().getName());
            graphics.drawImage(img,
                    (int)(jailB2.x * TILE_SIZE + OFFSET_X),
                    (int)(jailB2.y * TILE_SIZE + OFFSET_Y),
                    img.getWidth(null)/2,
                    img.getHeight(null)/2,
                    null);
            if(jailB2.getPiece().hasBanana())
                graphics.drawImage(map.get("banana"),
                        (int)(jailB2.x * TILE_SIZE + OFFSET_X),
                        (int)(jailB2.y * TILE_SIZE + OFFSET_Y),
                        img.getWidth(null)/2,
                        img.getHeight(null)/2,
                        null);
        }
        if(jailW1.hasPiece())
        {
            Image img = map.get(jailW1.getPiece().getName());
            graphics.drawImage(img,
                    (int)(jailW1.x * TILE_SIZE + OFFSET_X),
                    (int)(jailW1.y * TILE_SIZE + OFFSET_Y),
                    img.getWidth(null)/2,
                    img.getHeight(null)/2,
                    null);
            if(jailW1.getPiece().hasBanana())
                graphics.drawImage(map.get("banana"),
                        (int)(jailW1.x * TILE_SIZE + OFFSET_X),
                        (int)(jailW1.y * TILE_SIZE + OFFSET_Y),
                        img.getWidth(null)/2,
                        img.getHeight(null)/2,
                        null);
        }
        if(jailW2.hasPiece())
        {
            Image img = map.get(jailW2.getPiece().getName());
            graphics.drawImage(img,
                    (int)(jailW2.x * TILE_SIZE + OFFSET_X),
                    (int)(jailW2.y * TILE_SIZE + OFFSET_Y),
                    img.getWidth(null)/2,
                    img.getHeight(null)/2,
                    null);
            if(jailW2.getPiece().hasBanana())
                graphics.drawImage(map.get("banana"),
                        (int)(jailW2.x * TILE_SIZE + OFFSET_X),
                        (int)(jailW2.y * TILE_SIZE + OFFSET_Y),
                        img.getWidth(null)/2,
                        img.getHeight(null)/2,
                        null);
        }

        if(!activeGame)
        {
            graphics.setColor(new Color(255, 255, 255, 230));
            graphics.fillRect(0, 0, SCREEN_WIDTH, SCREEN_HEIGHT);

            graphics.drawImage(logoImg,
                    SCREEN_WIDTH/2 - logoImg.getWidth(null)/4,
                    SCREEN_HEIGHT/2 - logoImg.getHeight(null)/4 - 180,
                    logoImg.getWidth(null)/2,
                    logoImg.getHeight(null)/2,
                    null);

            graphics.setColor(Color.black);
            graphics.setFont(graphics.getFont().deriveFont(32f));
            String winText = "Black wins!";
            if(turn)
                winText = "White wins!";

            FontMetrics metrics = graphics.getFontMetrics(graphics.getFont());
            int x = SCREEN_WIDTH/2 - metrics.stringWidth(winText)/2;
            int y = SCREEN_HEIGHT/2 - metrics.getHeight()/2 + 80;
            graphics.drawString(winText, x, y);
        }
    }

    public void setupPieces()
    {
        board[0][0] = new Piece("bRook", false);
        board[0][1] = new Piece("bMonkey", false);
        board[0][2] = new Piece("bFish", false);
        board[0][3] = new Piece("bQueen", false);
        board[0][4] = new Piece("bKing", false, true);
        board[0][5] = new Piece("bFish", false);
        board[0][6] = new Piece("bMonkey", false);
        board[0][7] = new Piece("bRook", false);

        board[1][0] = new Piece("bFish", false);
        board[1][1] = new Piece("bFish", false);
        board[1][2] = new Piece("bElephant", false);
        board[1][3] = new Piece("bFish", false);
        board[1][4] = new Piece("bFish", false);
        board[1][5] = new Piece("bElephant", false);
        board[1][6] = new Piece("bFish", false);
        board[1][7] = new Piece("bFish", false);

        board[7][0] = new Piece("wRook", true);
        board[7][1] = new Piece("wMonkey", true);
        board[7][2] = new Piece("wFish", true);
        board[7][3] = new Piece("wQueen", true);
        board[7][4] = new Piece("wKing", true, true);
        board[7][5] = new Piece("wFish", true);
        board[7][6] = new Piece("wMonkey", true);
        board[7][7] = new Piece("wRook", true);

        board[6][0] = new Piece("wFish", true);
        board[6][1] = new Piece("wFish", true);
        board[6][2] = new Piece("wElephant", true);
        board[6][3] = new Piece("wFish", true);
        board[6][4] = new Piece("wFish", true);
        board[6][5] = new Piece("wElephant", true);
        board[6][6] = new Piece("wFish", true);
        board[6][7] = new Piece("wFish", true);
    }

    @Override
    public void mousePressed(MouseEvent e) {
        if(e.getButton() == MouseEvent.BUTTON1)
        {
            boolean canKillBear = false;

            // captured royalty
            if(capturedPiece != null)
            {
                if(jailSelecting != null)
                {
                    jailSelecting.setPiece(capturedPiece);
                    capturedPiece = null;
                    jailSelecting = null;

                    if(jailW1.hasPiece() && jailW2.hasPiece())
                    {
                        System.out.println("White wins!");
                        activeGame = false;
                        return;
                    }
                    if(jailB1.hasPiece() && jailB2.hasPiece())
                    {
                        System.out.println("Black wins!");
                        activeGame = false;
                        return;
                    }

                    turn = !turn;
                }
            }
            // rescuing
            else if(canRescue != null && jailSelecting != null)
            {
                jailRescuing = jailSelecting;
                canRescue = null;
                jailSelecting = null;
                tiles.clear();

                Piece temp = board[selectedY][selectedX];
                if(!turn)
                {
                    board[mouseY][7] = board[selectedY][selectedX];
                    board[selectedY][selectedX] = null;
                    selectedY = mouseY;
                    selectedX = 7;
                }
                else
                {
                    board[mouseY][0] = board[selectedY][selectedX];
                    board[selectedY][selectedX] = null;
                    selectedY = mouseY;
                    selectedX = 0;
                }
                board[selectedY][selectedX] = temp;

                highlight(board[selectedY][selectedX].getName(), selectedX, selectedY);
            }
            else if(jailRescuing != null)
            {
                if(isValidMove(board[selectedY][selectedX].getName(), selectedX, selectedY, mouseX, mouseY))
                {
                    System.out.println(jailRescuing.getPiece().getName() + " was rescued.");
                    jailRescuing.getPiece().setHasBanana(false);
                    board[mouseY][mouseX] = board[selectedY][selectedX];
                    board[selectedY][selectedX] = jailRescuing.getPiece();
                    jailRescuing.setPiece(null);
                    jailRescuing = null;

                    tiles.clear();
                    selected = false;
                    turn = !turn;
                }
            }
            else if(isWithinBounds(mouseX) && isWithinBounds(mouseY))
            {
                // kill bear
                if(selected && board[mouseY][mouseX] != null)
                {
                    for(Tile tile : tiles)
                    {
                        if(board[mouseY][mouseX].getName().equals("bear") && mouseX == tile.x && mouseY == tile.y)
                        {
                            canKillBear = true;
                            break;
                        }
                    }
                }

                canRescue = null;

                // select bear
                if(!canKillBear && (bearSelecting || (board[mouseY][mouseX] != null && board[mouseY][mouseX].getName().equals("bear"))))
                {
                    bearSelected = true;
                    selected = true;
                    selectedX = mouseX;
                    selectedY = mouseY;
                    tiles.clear();
                    highlight("bear", selectedX, selectedY);
                }
                // select piece
                else if(board[mouseY][mouseX] != null && board[mouseY][mouseX].getColor() == turn)
                {
                    bearSelected = false;
                    selected = true;
                    selectedX = mouseX;
                    selectedY = mouseY;

                    tiles.clear();
                    highlight(board[selectedY][selectedX].getName(), selectedX, selectedY);
                }
                // bear: move or capture
                else if(bearSelected)
                {
                    if(isValidMove("bear", selectedX, selectedY, mouseX, mouseY))
                    {
                        board[mouseY][mouseX] = bear;

                        if(bearInPlay)
                            board[selectedY][selectedX] = null;
                        else
                            bearInPlay = true;

                        rookCanKill = false;
                        bearSelected = false;
                        selected = false;
                        tiles.clear();
                        turn = !turn;
                    }
                }
                // move or capture
                else if(selected &&
                        (board[mouseY][mouseX] == null || board[mouseY][mouseX].getColor() != board[selectedY][selectedX].getColor()))
                {
                    if(freeMove || isValidMove(board[selectedY][selectedX].getName(), selectedX, selectedY, mouseX, mouseY))
                    {
                        rookCanKill = false;
                        if(board[mouseY][mouseX] != null)
                        {
                            //capture
                            System.out.println(board[mouseY][mouseX].getName() + " was captured.");
                            rookCanKill = true;

                            switch(board[mouseY][mouseX].getName())
                            {
                                case "bQueen":
                                case "wQueen":
                                case "bKing":
                                case "wKing":
                                    capturedPiece = board[mouseY][mouseX];
                                    break;
                                case "bear":
                                    break;

                                default:
                                    if(turn)
                                        whiteCapture.add(board[mouseY][mouseX].getName());
                                    else
                                        blackCapture.add(board[mouseY][mouseX].getName());
                            }
                        }

                        board[mouseY][mouseX] = board[selectedY][selectedX];
                        board[selectedY][selectedX] = null;
                        selected = false;
                        tiles.clear();
                        if(capturedPiece == null)
                            turn = !turn;
                    }
                }
            }
        }
        if(e.getButton() == MouseEvent.BUTTON3)
        {
            if(jailRescuing == null)
            {
                canRescue = null;
                jailSelecting = null;
                bearSelected = false;
                selected = false;
                tiles.clear();
            }
        }
    }

    public boolean isValidMove(String name, int xPos, int yPos, int newXPos, int newYPos)
    {
        int dx = newXPos - xPos;
        int dy = newYPos - yPos;

        int dir = 1;
        if(name.equals("bFish") || name.equals("wFish"))
        {
            if(board[yPos][xPos].getColor()) dir = -1;
        }

        switch(name)
        {
            case "bFish":
            case "wFish":
                if(Math.abs(dx) <= 1 && (dy == 0 || dy == dir))
                {
                    if((Math.abs(dx) == 0 || dy == 0) && board[newYPos][newXPos] != null)
                        return false;
                    else
                        if(newYPos == 0) board[yPos][xPos].setName("wFishQueen");
                        if(newYPos == 7) board[yPos][xPos].setName("bFishQueen");
                        return true;
                }
                return false;

            case "bRook":
            case "wRook":
                if(rookCanKill && ((Math.abs(dx) == 1 && Math.abs(dy) == 0) || (Math.abs(dx) == 0 && Math.abs(dy) == 1)))
                    return true;
                else return board[newYPos][newXPos] == null;

            case "bMonkey":
            case "wMonkey":
                for(Tile tile : tiles)
                    if(tile.x == newXPos && tile.y == newYPos) return true;
                return false;

            case "bElephant":
            case "wElephant":
                return Math.abs(dx) == 2 && Math.abs(dy) == 2;

            case "bQueen":
            case "wQueen":
            case "bFishQueen":
            case "wFishQueen":
                if(Math.abs(dx) == Math.abs(dy) || dx == 0 || dy == 0)
                {
                    int dirX = dx;
                    int dirY = dy;
                    if(dx != 0) dirX = dx / Math.abs(dx);
                    if(dy != 0) dirY = dy / Math.abs(dy);
                    boolean pieceFound = false;

                    while(newXPos >= 0 && newXPos < 8 && newYPos >= 0 && newYPos < 8 && !pieceFound)
                    {
                        newXPos -= dirX;
                        newYPos -= dirY;

                        if(board[newYPos][newXPos] != null)
                            return newXPos == xPos && newYPos == yPos;
                    }
                }
                return false;

            case "bKing":
            case "wKing":
                return Math.abs(dx) <= 1 && Math.abs(dy) <= 1;

            case "bear":
                if(board[newYPos][newXPos] != null)
                    return false;
                if(bearInPlay)
                    return Math.abs(dx) <= 1 && Math.abs(dy) <= 1;
                else
                    return (newXPos == 3 || newXPos == 4) && (newYPos == 3 || newYPos == 4);

            default:
                return false;
        }
    }

    public void highlight(String name, int xPos, int yPos)
    {
        switch(name)
        {
            case "bFish":
                addTile(xPos - 1, yPos + 1);
                addTile(xPos + 1, yPos + 1);
                addTile(xPos, yPos + 1, true);
                addTile(xPos - 1, yPos, true);
                addTile(xPos + 1, yPos, true);
                break;

            case "wFish":
                addTile(xPos - 1, yPos - 1);
                addTile(xPos + 1, yPos - 1);
                addTile(xPos, yPos - 1, true);
                addTile(xPos - 1, yPos, true);
                addTile(xPos + 1, yPos, true);
                break;

            case "bRook":
            case "wRook":
                for(int i = 0; i < board.length; i++)
                {
                    for(int j = 0; j < board[i].length; j++)
                        addTile(j, i, true);
                }
                if(rookCanKill)
                {
                    addTile(xPos - 1, yPos, false);
                    addTile(xPos + 1, yPos, false);
                    addTile(xPos, yPos - 1, false);
                    addTile(xPos, yPos + 1, false);
                }
                break;

            case "bMonkey":
            case "wMonkey":
                if(jailRescuing == null)
                {
                    for (int i = -1; i < 2; i++)
                        for (int j = -1; j < 2; j++)
                            if (!(i == 0 && j == 0))
                                addTile(xPos + j, yPos + i, true);
                }

                monkeyMove(xPos, yPos);

                if(jailRescuing == null)
                {
                    if(!turn && xPos == 7)
                    {
                        if(yPos == 3 && jailW1.hasPiece() && jailW1.getPiece().hasBanana())
                        {
                            if((board[2][7] != null && board[1][7] == null) ||
                                    (board[3][6] != null && board[3][5] == null) ||
                                    (board[4][7] != null && board[5][7] == null))
                                canRescue = jailW1;
                        }
                        if(yPos == 4 && jailW2.hasPiece() && jailW2.getPiece().hasBanana())
                        {
                            if((board[3][7] != null && board[2][7] == null) ||
                                    (board[4][6] != null && board[4][5] == null) ||
                                    (board[5][7] != null && board[6][7] == null))
                                canRescue = jailW2;
                        }
                    }
                    else if(turn && xPos == 0)
                    {
                        if(yPos == 3 && jailB1.hasPiece() && jailB1.getPiece().hasBanana())
                        {
                            if((board[2][0] != null && board[1][0] == null) ||
                                    (board[3][1] != null && board[3][2] == null) ||
                                    (board[4][0] != null && board[5][0] == null))
                                canRescue = jailB1;
                        }
                        if(yPos == 4 && jailB2.hasPiece() && jailB2.getPiece().hasBanana())
                        {
                            if((board[3][0] != null && board[2][0] == null) ||
                                    (board[4][1] != null && board[4][2] == null) ||
                                    (board[5][0] != null && board[6][0] == null))
                                canRescue = jailB2;
                        }
                    }
                }

                break;

            case "bElephant":
            case "wElephant":
                addTile(xPos + 2, yPos + 2);
                addTile(xPos + 2, yPos - 2);
                addTile(xPos - 2, yPos + 2);
                addTile(xPos - 2, yPos - 2);
                break;

            case "bQueen":
            case "wQueen":
            case "bFishQueen":
            case "wFishQueen":
                for(int i = -1; i < 2; i++)
                {
                    for(int j = -1; j < 2; j++)
                    {
                        int dirX = xPos + j;
                        int dirY = yPos + i;
                        boolean pieceFound = false;
                        while(dirX >= 0 && dirX < 8 && dirY >= 0 && dirY < 8 && !pieceFound)
                        {
                            addTile(dirX, dirY);
                            if(board[dirY][dirX] != null) pieceFound = true;
                            dirX += j;
                            dirY += i;
                        }
                    }
                }
                break;

            case "bKing":
            case "wKing":
                for(int i = -1; i < 2; i++)
                {
                    for(int j = -1; j < 2; j++)
                        if(i != j || i != 0)
                            addTile(xPos + j, yPos + i);
                }
                break;

            case "bear":
                if(bearInPlay)
                {
                    for(int i = -1; i < 2; i++)
                    {
                        for(int j = -1; j < 2; j++)
                            if(i != j || i != 0)
                                addTile(xPos + j, yPos + i, true);
                    }
                }
                else
                {
                    addTile(3, 3, true);
                    addTile(3, 4, true);
                    addTile(4, 3, true);
                    addTile(4, 4, true);
                }
                break;
        }
    }

    public void monkeyMove(int xPos, int yPos)
    {
        for(int i = -1; i < 2; i++)
        {
            for(int j = -1; j < 2; j++)
            {
                if(!(i == 0 && j == 0))
                {
                    if(isWithinBounds(xPos + j) && isWithinBounds(yPos + i) && board[yPos + i][xPos + j] != null)
                    {
                        if(isWithinBounds(xPos + (2 * j)) && isWithinBounds(yPos + (2 * i)))
                        {
                            boolean exists = false;

                            for(Tile tile : tiles)
                            {
                                if(tile.x == xPos + (2 * j) && tile.y == yPos + (2 * i))
                                {
                                    exists = true;
                                    break;
                                }
                            }

                            if(!exists)
                            {
                                if(jailRescuing != null)
                                    addTile(xPos + (2 * j), yPos + (2 * i), true);
                                else
                                    addTile(xPos + (2 * j), yPos + (2 * i));

                                if(turn && xPos + (2 * j) == 0 && board[yPos + (2 * i)][xPos + (2 * j)] == null)
                                {
                                    if(yPos + (2 * i) == 3 && jailB1.hasPiece() && jailB1.getPiece().hasBanana())
                                        canRescue = jailB1;
                                    if(yPos + (2 * i) == 4 && jailB2.hasPiece() && jailB2.getPiece().hasBanana())
                                        canRescue = jailB2;
                                }
                                else if(!turn && xPos + (2 * j) == 7 && board[yPos + (2 * i)][xPos + (2 * j)] == null)
                                {
                                    if(yPos + (2 * i) == 3 && jailW1.hasPiece() && jailW1.getPiece().hasBanana())
                                        canRescue = jailW1;
                                    if(yPos + (2 * i) == 4 && jailW2.hasPiece() && jailW2.getPiece().hasBanana())
                                        canRescue = jailW2;
                                }

                                if(board[yPos + (2 * i)][xPos + (2 * j)] == null)
                                    monkeyMove(xPos + (2 * j), yPos + (2 * i));
                            }
                        }
                    }
                }
            }
        }
    }

    public void addTile(int xPos, int yPos)
    {
        if(xPos >= 0 && xPos < 8 && yPos >= 0 && yPos < 8)
        {
            if(board[yPos][xPos] == null || board[yPos][xPos].getColor() != turn)
                tiles.add(new Tile(xPos, yPos));
        }
    }

    public void addTile(int xPos, int yPos, boolean nullCheck)
    {
        if(xPos >= 0 && xPos < 8 && yPos >= 0 && yPos < 8)
        {
            if(nullCheck == (board[yPos][xPos] == null))
            {
                if(board[yPos][xPos] == null || board[yPos][xPos].getColor() != turn)
                    tiles.add(new Tile(xPos, yPos));
            }
        }
    }

    public boolean isWithinBounds(int n)
    {
        return n >= 0 && n < 8;
    }

    public boolean selectingJail(Jail jail, double cursorX, double cursorY)
    {
        return cursorX > jail.x && cursorX < jail.x+1 && cursorY > jail.y && cursorY < jail.y+1;
    }

    @Override
    public void mouseClicked(MouseEvent e) {

    }

    @Override
    public void mouseReleased(MouseEvent e) {

    }

    @Override
    public void mouseEntered(MouseEvent e) {

    }

    @Override
    public void mouseExited(MouseEvent e) {

    }
}
