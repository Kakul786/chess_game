// Save as SimpleChess.java
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.awt.Point;

public class SimpleChess {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            ChessUI ui = new ChessUI();
            ui.frame.setVisible(true);
        });
    }

    enum PieceColor { WHITE, BLACK }

    // --- Piece base ---
    static abstract class Piece {
        PieceColor color;
        char shortName; // P N B R Q K
        Piece(PieceColor c, char name) { color = c; shortName = name; }
        abstract java.util.List<Point> legalMoves(int r, int c, Board board);
        boolean sameColor(Piece other) { return other != null && this.color == other.color; }
        public String toString() { return (color==PieceColor.WHITE?"W":"B") + shortName; }
    }

    // --- Board ---
    static class Board {
        Piece[][] b = new Piece[8][8];
        boolean inBounds(int r,int c){ return r>=0 && r<8 && c>=0 && c<8; }
        Piece get(int r,int c){ return inBounds(r,c) ? b[r][c] : null; }
        void set(int r,int c, Piece p){ if(inBounds(r,c)) b[r][c]=p; }

        void initStandard(){
            // Pawns
            for(int c=0;c<8;c++){ b[1][c] = new Pawn(PieceColor.BLACK); b[6][c] = new Pawn(PieceColor.WHITE); }
            // Rooks
            b[0][0] = new Rook(PieceColor.BLACK); b[0][7] = new Rook(PieceColor.BLACK);
            b[7][0] = new Rook(PieceColor.WHITE); b[7][7] = new Rook(PieceColor.WHITE);
            // Knights
            b[0][1] = new Knight(PieceColor.BLACK); b[0][6] = new Knight(PieceColor.BLACK);
            b[7][1] = new Knight(PieceColor.WHITE); b[7][6] = new Knight(PieceColor.WHITE);
            // Bishops
            b[0][2] = new Bishop(PieceColor.BLACK); b[0][5] = new Bishop(PieceColor.BLACK);
            b[7][2] = new Bishop(PieceColor.WHITE); b[7][5] = new Bishop(PieceColor.WHITE);
            // Queens
            b[0][3] = new Queen(PieceColor.BLACK); b[7][3] = new Queen(PieceColor.WHITE);
            // Kings
            b[0][4] = new King(PieceColor.BLACK); b[7][4] = new King(PieceColor.WHITE);
        }
    }

    // --- Pieces implementations ---
    static class Pawn extends Piece {
        Pawn(PieceColor c){ super(c,'P'); }
        java.util.List<Point> legalMoves(int r,int c, Board board){
            java.util.List<Point> moves = new ArrayList<>();
            int dir = (color==PieceColor.WHITE) ? -1 : 1;
            int start = (color==PieceColor.WHITE) ? 6 : 1;
            int nr = r + dir;
            // forward one
            if(board.inBounds(nr,c) && board.get(nr,c) == null) moves.add(new Point(nr,c));
            // forward two from start
            int nr2 = r + 2*dir;
            if(r==start && board.inBounds(nr2,c) && board.get(nr,c)==null && board.get(nr2,c)==null) moves.add(new Point(nr2,c));
            // captures
            for(int dc=-1; dc<=1; dc+=2){
                int nc = c+dc;
                if(board.inBounds(nr,nc)){
                    Piece p = board.get(nr,nc);
                    if(p != null && p.color != this.color) moves.add(new Point(nr,nc));
                }
            }
            // (en-passant not implemented)
            return moves;
        }
    }

    static class Rook extends Piece {
        Rook(PieceColor c){ super(c,'R'); }
        java.util.List<Point> legalMoves(int r,int c, Board board){
            java.util.List<Point> moves = new ArrayList<>();
            int[][] dirs = {{1,0},{-1,0},{0,1},{0,-1}};
            for(int[] d: dirs){
                int rr=r+d[0], cc=c+d[1];
                while(board.inBounds(rr,cc)){
                    Piece p = board.get(rr,cc);
                    if(p==null) moves.add(new Point(rr,cc));
                    else {
                        if(p.color != this.color) moves.add(new Point(rr,cc));
                        break;
                    }
                    rr += d[0]; cc += d[1];
                }
            }
            return moves;
        }
    }

    static class Bishop extends Piece {
        Bishop(PieceColor c){ super(c,'B'); }
        java.util.List<Point> legalMoves(int r,int c, Board board){
            java.util.List<Point> moves = new ArrayList<>();
            int[][] dirs = {{1,1},{1,-1},{-1,1},{-1,-1}};
            for(int[] d: dirs){
                int rr=r+d[0], cc=c+d[1];
                while(board.inBounds(rr,cc)){
                    Piece p = board.get(rr,cc);
                    if(p==null) moves.add(new Point(rr,cc));
                    else { if(p.color != this.color) moves.add(new Point(rr,cc)); break; }
                    rr += d[0]; cc += d[1];
                }
            }
            return moves;
        }
    }

    static class Queen extends Piece {
        Queen(PieceColor c){ super(c,'Q'); }
        java.util.List<Point> legalMoves(int r,int c, Board board){
            java.util.List<Point> moves = new ArrayList<>();
            // combine rook + bishop dirs
            int[][] dirs = {{1,0},{-1,0},{0,1},{0,-1},{1,1},{1,-1},{-1,1},{-1,-1}};
            for(int[] d: dirs){
                int rr=r+d[0], cc=c+d[1];
                while(board.inBounds(rr,cc)){
                    Piece p = board.get(rr,cc);
                    if(p==null) moves.add(new Point(rr,cc));
                    else { if(p.color != this.color) moves.add(new Point(rr,cc)); break; }
                    rr += d[0]; cc += d[1];
                }
            }
            return moves;
        }
    }

    static class Knight extends Piece {
        Knight(PieceColor c){ super(c,'N'); }
        java.util.List<Point> legalMoves(int r,int c, Board board){
            java.util.List<Point> moves = new ArrayList<>();
            int[][] offs = {{2,1},{2,-1},{-2,1},{-2,-1},{1,2},{1,-2},{-1,2},{-1,-2}};
            for(int[] o: offs){
                int rr=r+o[0], cc=c+o[1];
                if(board.inBounds(rr,cc)){
                    Piece p = board.get(rr,cc);
                    if(p==null || p.color != this.color) moves.add(new Point(rr,cc));
                }
            }
            return moves;
        }
    }

    static class King extends Piece {
        King(PieceColor c){ super(c,'K'); }
        java.util.List<Point> legalMoves(int r,int c, Board board){
            java.util.List<Point> moves = new ArrayList<>();
            for(int dr=-1; dr<=1; dr++){
                for(int dc=-1; dc<=1; dc++){
                    if(dr==0 && dc==0) continue;
                    int rr=r+dr, cc=c+dc;
                    if(board.inBounds(rr,cc)){
                        Piece p = board.get(rr,cc);
                        if(p==null || p.color != this.color) moves.add(new Point(rr,cc));
                    }
                }
            }
            // (castling not implemented)
            return moves;
        }
    }

    // --- Game logic (turns, moving) ---
    static class Game {
        Board board;
        PieceColor turn;
        Game(){ board = new Board(); board.initStandard(); turn = PieceColor.WHITE; }

        boolean move(int sr,int sc,int tr,int tc){
            if(!board.inBounds(sr,sc) || !board.inBounds(tr,tc)) return false;
            Piece p = board.get(sr,sc);
            if(p==null) return false;
            if(p.color != turn) return false;
            java.util.List<Point> legal = p.legalMoves(sr,sc,board);
            boolean ok = false;
            for(Point pt: legal) if(pt.x==tr && pt.y==tc){ ok = true; break; }
            if(!ok) return false;
            // perform move
            board.set(tr,tc, p);
            board.set(sr,sc, null);
            // pawn promotion (automatically to queen)
            if(p instanceof Pawn){
                if((p.color==PieceColor.WHITE && tr==0) || (p.color==PieceColor.BLACK && tr==7)){
                    board.set(tr,tc, new Queen(p.color));
                }
            }
            // switch turn
            turn = (turn==PieceColor.WHITE) ? PieceColor.BLACK : PieceColor.WHITE;
            return true;
        }
    }

    // --- Simple Swing UI ---
    static class ChessUI {
        JFrame frame;
        BoardPanel boardPanel;
        JLabel status;
        Game game;
        Point selected = null;
        java.util.List<Point> lastLegal = new ArrayList<>();

        ChessUI(){
            frame = new JFrame("Simple Java Chess");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(640, 700);
            frame.setLayout(new BorderLayout());
            game = new Game();
            boardPanel = new BoardPanel();
            frame.add(boardPanel, BorderLayout.CENTER);
            status = new JLabel("Turn: WHITE");
            status.setHorizontalAlignment(SwingConstants.CENTER);
            frame.add(status, BorderLayout.SOUTH);
        }

        class BoardPanel extends JPanel {
            final int tileSize = 80;
            BoardPanel(){
                setPreferredSize(new Dimension(8*tileSize, 8*tileSize));
                addMouseListener(new MouseAdapter(){
                    public void mouseClicked(MouseEvent e){
                        int c = e.getX() / tileSize;
                        int r = e.getY() / tileSize;
                        if(!game.board.inBounds(r,c)) return;
                        Piece p = game.board.get(r,c);
                        if(selected == null){
                            if(p != null && p.color == game.turn){
                                selected = new Point(r,c);
                                lastLegal = p.legalMoves(r,c, game.board);
                            }
                        } else {
                            // attempt move
                            boolean moved = game.move(selected.x, selected.y, r, c);
                            selected = null;
                            lastLegal.clear();
                            if(!moved){
                                // if clicked another of same-color pieces, change selection
                                if(p != null && p.color == game.turn){
                                    selected = new Point(r,c);
                                    lastLegal = p.legalMoves(r,c, game.board);
                                }
                            } else {
                                status.setText("Turn: " + game.turn.toString());
                            }
                        }
                        repaint();
                    }
                });
            }

            protected void paintComponent(Graphics g){
                super.paintComponent(g);
                // draw board
                for(int r=0;r<8;r++){
                    for(int c=0;c<8;c++){
                        boolean light = (r + c) % 2 == 0;
                        g.setColor(light ? new Color(240,217,181) : new Color(181,136,99));
                        g.fillRect(c*tileSize, r*tileSize, tileSize, tileSize);
                        // highlight selected or legal
                        if(selected != null && selected.x==r && selected.y==c){
                            g.setColor(new Color(255,255,0,100));
                            g.fillRect(c*tileSize, r*tileSize, tileSize, tileSize);
                        } else {
                            for(Point m: lastLegal) if(m.x==r && m.y==c){
                                g.setColor(new Color(50,205,50,120));
                                g.fillRect(c*tileSize, r*tileSize, tileSize, tileSize);
                            }
                        }
                        // draw piece
                        Piece p = game.board.get(r,c);
                        if(p != null){
                            String txt = String.valueOf(p.shortName);
                            g.setFont(new Font("SansSerif", Font.BOLD, 36));
                            g.setColor(p.color==PieceColor.WHITE ? Color.WHITE : Color.BLACK);
                            // draw text centered
                            FontMetrics fm = g.getFontMetrics();
                            int tx = c*tileSize + (tileSize - fm.stringWidth(txt)) / 2;
                            int ty = r*tileSize + (tileSize + fm.getAscent()) / 2 - 6;
                            // draw outline for white pieces
                            if(p.color==PieceColor.WHITE){
                                g.setColor(Color.DARK_GRAY);
                                g.drawString(txt, tx-1, ty-1);
                                g.drawString(txt, tx+1, ty+1);
                                g.setColor(Color.WHITE);
                            }
                            g.drawString(txt, tx, ty);
                        }
                    }
                }
            }
        }
    }
}
