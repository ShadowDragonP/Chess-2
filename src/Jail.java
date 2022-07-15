public class Jail {

    private Piece piece;
    public double x, y;

    public Jail(double x, double y)
    {
        this.x = x;
        this.y = y;
    }

    public void setPiece(Piece piece)
    {
        this.piece = piece;
    }

    public Piece getPiece()
    {
        return piece;
    }

    public boolean hasPiece()
    {
        return piece != null;
    }
}
