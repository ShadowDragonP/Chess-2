public class Piece {

    private String name;
    private boolean color; //white = true, black = false
    private boolean hasBanana;

    public Piece(String name)
    {
        this.name = name;
    }

    public Piece(String name, boolean color)
    {
        this.name = name;
        this.color = color;
    }

    public Piece(String name, boolean color, boolean hasBanana)
    {
        this.name = name;
        this.color = color;
        this.hasBanana = hasBanana;
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public boolean getColor()
    {
        return color;
    }

    public boolean hasBanana()
    {
        return hasBanana;
    }

    public void setHasBanana(boolean hasBanana)
    {
        this.hasBanana = hasBanana;
    }

}
