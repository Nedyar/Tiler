import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

class Board {
    Tile[][] boardTiles;
    Tile[][] originalTiles;
    int[][] tileRotations;
    Map<Tile, Integer> usedTiles;
    int rows = 0;
    int cols = 0;

    public Board(int rows, int cols) {
        this.boardTiles = new Tile[rows][cols];
        this.originalTiles = new Tile[rows][cols];
        this.tileRotations = new int[rows][cols];
        this.usedTiles = new HashMap<Tile, Integer>();
        this.rows = rows;
        this.cols = cols;
    }

    public static void createValidBoards(Set<Board> validBoards, int rows, int cols) {
        Board board = new Board(rows, cols);
        board.fillTile(validBoards, 0, 0);
    }

    public static void setUpAmonHen(Set<Board> validBoards) {
        Board board = new Board(4, 4);

        // River on the west side
        board.originalTiles[0][0] = Main.mountainRiverStraightTile;
        board.boardTiles[0][0] = board.originalTiles[0][0].clone();
        board.originalTiles[1][0] = Main.straightRiverTile;
        board.boardTiles[1][0] = board.originalTiles[1][0].clone();
        board.originalTiles[2][0] = Main.straightRiverTile;
        board.boardTiles[2][0] = board.originalTiles[2][0].clone();
        board.originalTiles[3][0] = Main.straightRiverTile;
        board.boardTiles[3][0] = board.originalTiles[3][0].clone();

        board.usedTiles.merge(Main.mountainRiverStraightTile, 1, Integer::sum);
        board.usedTiles.merge(Main.straightRiverTile, 3, Integer::sum);

        // Mountain range on the east side
        board.originalTiles[1][2] = Main.mountainRangeTile;
        board.boardTiles[1][2] = board.originalTiles[1][2].clone();
        board.tileRotations[1][2] = 1;
        board.boardTiles[1][2].clockWiseRotate(board.tileRotations[1][2]);
        board.originalTiles[1][3] = Main.mountainRangeTile;
        board.boardTiles[1][3] = board.originalTiles[1][3].clone();
        board.tileRotations[1][3] = 2;
        board.boardTiles[1][3].clockWiseRotate(board.tileRotations[1][3]);
        board.originalTiles[2][2] = Main.mountainRangeTile;
        board.boardTiles[2][2] = board.originalTiles[2][2].clone();
        board.tileRotations[2][2] = 0;
        board.boardTiles[2][2].clockWiseRotate(board.tileRotations[2][2]);
        board.originalTiles[2][3] = Main.mountainRangeTile;
        board.boardTiles[2][3] = board.originalTiles[2][3].clone();
        board.tileRotations[2][3] = 3;
        board.boardTiles[2][3].clockWiseRotate(board.tileRotations[2][3]);

        board.usedTiles.merge(Main.mountainRangeTile, 4, Integer::sum);

        board.fillTile(validBoards, 0, 0);
    }

    public static void setUpAmonSun(Set<Board> validBoards) {
        Board board = new Board(2, 2);

        // Mountain range on the center
        board.originalTiles[0][0] = Main.mountainRangeTile;
        board.boardTiles[0][0] = board.originalTiles[0][0].clone();
        board.tileRotations[0][0] = 1;
        board.boardTiles[0][0].clockWiseRotate(board.tileRotations[0][0]);
        board.originalTiles[0][1] = Main.mountainRangeTile;
        board.boardTiles[0][1] = board.originalTiles[0][1].clone();
        board.tileRotations[0][1] = 2;
        board.boardTiles[0][1].clockWiseRotate(board.tileRotations[0][1]);
        board.originalTiles[1][0] = Main.mountainRangeTile;
        board.boardTiles[1][0] = board.originalTiles[1][0].clone();
        board.tileRotations[1][0] = 0;
        board.boardTiles[1][0].clockWiseRotate(board.tileRotations[1][0]);
        board.originalTiles[1][1] = Main.mountainRangeTile;
        board.boardTiles[1][1] = board.originalTiles[1][1].clone();
        board.tileRotations[1][1] = 3;
        board.boardTiles[1][1].clockWiseRotate(board.tileRotations[1][1]);

        board.usedTiles.merge(Main.mountainRangeTile, 4, Integer::sum);

        board.fillTile(validBoards, 0, 0);
    }

    private void fillTile(Set<Board> validBoards, int row, int col) {

        // Check new row
        if (col == this.cols) {
            row++;
            col = 0;
        }

        // Board fully filled
        if (row == this.rows) {
            // if (validBoards.add(this.clone())) {
            try {
                Main.logBoardJSON(this);
            } catch (IOException e) {
                e.printStackTrace();
            }
            // }
            return;
        }

        // Do not fill manually setted tiles
        if (isTileAlreadyFilled(row, col))
            fillTile(validBoards, row, col + 1);
        else {
            // Try to put every tile
            for (Tile originalTile : Main.originalTiles.keySet()) {

                // Discard already spent tiles
                if (!isOriginalTileAvialable(originalTile))
                    continue;

                // Try all rotations of the tile
                for (int rotation = 0; rotation < 4; rotation++) {

                    // Cloning the tile to avoid modfy the original on rotation
                    Tile tile = originalTile.clone();
                    tile.clockWiseRotate(rotation);

                    // Check Tile is valid
                    if (!isTileValid(row, col, tile))
                        continue;

                    // Place Tile
                    boardTiles[row][col] = tile;
                    originalTiles[row][col] = originalTile;
                    tileRotations[row][col] = rotation;
                    usedTiles.merge(originalTile, 1, Integer::sum);

                    // Recursive call
                    fillTile(validBoards, row, col + 1);

                    // Backtracking
                    boardTiles[row][col] = null;
                    originalTiles[row][col] = null;
                    tileRotations[row][col] = 0;
                    usedTiles.merge(originalTile, -1, Integer::sum);
                    if (usedTiles.get(originalTile) <= 0)
                        usedTiles.remove(originalTile);

                }
            }
        }
    }

    private boolean isOriginalTileAvialable(Tile originalTile) {
        Integer alreadyPlacedTiles = this.usedTiles.get(originalTile);
        Integer maxPlacedTiles = Main.originalTiles.get(originalTile);

        if (alreadyPlacedTiles == null || alreadyPlacedTiles < maxPlacedTiles)
            return true;
        return false;
    }

    private boolean isTileAlreadyFilled(int row, int col) {
        if (boardTiles[row][col] != null)
            return true;
        return false;
    }

    private boolean isTileValid(int row, int col, Tile tile) {
        // Upper tiles won't check upper neighbors
        if (row != 0)
            if (this.boardTiles[row - 1][col] != null)
                if (!isJunctionValid(tile.northSide, this.boardTiles[row - 1][col].southSide))
                    return false;

        // Left tiles won't check left neighbors
        if (col != 0)
            if (this.boardTiles[row][col - 1] != null)
                if (!isJunctionValid(tile.westSide, this.boardTiles[row][col - 1].eastSide))
                    return false;

        // Right tiles won't check right neighbors
        if (col != 3)
            if (this.boardTiles[row][col + 1] != null)
                if (!isJunctionValid(tile.eastSide, this.boardTiles[row][col + 1].westSide))
                    return false;

        // Lower tiles won't check lower neighbors
        if (row != 3)
            if (this.boardTiles[row + 1][col] != null)
                if (!isJunctionValid(tile.southSide, this.boardTiles[row + 1][col].northSide))
                    return false;

        return true;
    }

    private boolean isJunctionValid(Tile.SIDE newTileSide, Tile.SIDE previousTileSide) {
        boolean valid = false;
        switch (newTileSide) {
            case EMPTY:
                if (previousTileSide == Tile.SIDE.EMPTY)
                    valid = true;
                else
                    valid = false;
                break;
            case RIVER:
                if (previousTileSide == Tile.SIDE.RIVER)
                    valid = true;
                else
                    valid = false;
                break;
            case MOUNTAIN:
                if (previousTileSide == Tile.SIDE.MOUNTAIN)
                    valid = true;
                else
                    valid = false;
                break;
            case MOUNTAIN_RANGE_IN:
                if (previousTileSide == Tile.SIDE.MOUNTAIN_RANGE_OUT)
                    valid = true;
                else
                    valid = false;
                break;
            case MOUNTAIN_RANGE_OUT:
                if (previousTileSide == Tile.SIDE.MOUNTAIN_RANGE_IN)
                    valid = true;
                else
                    valid = false;
                break;
            default:
                valid = false;
                break;
        }
        return valid;
    }

    @Override
    public Board clone() {
        Board clonedBoard = new Board(rows, cols);
        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < cols; col++) {
                if (this.boardTiles[row][col] != null)
                    clonedBoard.boardTiles[row][col] = this.boardTiles[row][col].clone();
                clonedBoard.originalTiles[row][col] = this.originalTiles[row][col];
                clonedBoard.tileRotations[row][col] = this.tileRotations[row][col];
            }
        }
        for (Map.Entry<Tile, Integer> entry : this.usedTiles.entrySet()) {
            clonedBoard.usedTiles.put(entry.getKey(), entry.getValue().intValue());
        }
        return clonedBoard;
    }

    @Override
    public String toString() {
        int tileHeight = 5;
        int tileWidth = 11;

        StringBuilder sb = new StringBuilder();

        for (int row = 0; row < rows; row++) {
            // each tile has 5 rows
            for (int subRow = 0; subRow < tileHeight; subRow++) {
                for (int col = 0; col < cols; col++) {
                    Tile t = boardTiles[row][col];

                    if (t == null) {
                        sb.append(" ".repeat(tileWidth));
                        continue;
                    }

                    sb.append(renderTileLine(t, subRow, row, col));
                }
                sb.append("\n");
            }
        }
        return sb.toString();
    }

    private String renderTileLine(Tile t, int line, int boardRow, int boardCol) {
        char[] chars = new char[11];

        // base filling
        for (int i = 0; i < 11; i++)
            chars[i] = ' ';

        boolean top = boardRow == 0;
        boolean bottom = boardRow == rows - 1;
        boolean left = boardCol == 0;
        boolean right = boardCol == cols - 1;

        if (line == 0) {
            chars[0] = top && left ? '┌' : top ? '┬' : left ? '├' : '┼';
            chars[10] = top && right ? '┐' : top ? '┬' : right ? '┤' : '┼';
            for (int i = 1; i < 10; i++)
                chars[i] = '─';
        } else if (line == 4) {
            chars[0] = bottom && left ? '└' : bottom ? '┴' : left ? '├' : '┼';
            chars[10] = bottom && right ? '┘' : bottom ? '┴' : right ? '┤' : '┼';
            for (int i = 1; i < 10; i++)
                chars[i] = '─';
        } else {
            chars[0] = '│';
            chars[10] = '│';

            if (line == 1)
                placeNorth(t, chars);
            if (line == 2)
                placeWestEast(t, chars);
            if (line == 3)
                placeSouth(t, chars);
        }

        return new String(chars);
    }

    private void placeNorth(Tile t, char[] c) {
        c[5] = switch (t.northSide) {
            case RIVER -> '║';
            case MOUNTAIN -> 'U';
            case MOUNTAIN_RANGE_IN ->
                (t.eastSide == Tile.SIDE.MOUNTAIN_RANGE_OUT) ? 'L'
                        : (t.westSide == Tile.SIDE.MOUNTAIN_RANGE_OUT) ? 'J' : ' ';
            default -> ' ';
        };
    }

    private void placeSouth(Tile t, char[] c) {
        c[5] = switch (t.southSide) {
            case RIVER -> '║';
            case MOUNTAIN -> 'n';
            case MOUNTAIN_RANGE_IN ->
                (t.eastSide == Tile.SIDE.MOUNTAIN_RANGE_OUT) ? 'r'
                        : (t.westSide == Tile.SIDE.MOUNTAIN_RANGE_OUT) ? '7' : ' ';
            default -> ' ';
        };
    }

    private void placeWestEast(Tile t, char[] c) {
        c[1] = switch (t.westSide) {
            case RIVER -> '═';
            case MOUNTAIN -> ')';
            case MOUNTAIN_RANGE_IN ->
                (t.northSide == Tile.SIDE.MOUNTAIN_RANGE_OUT) ? 'J'
                        : (t.southSide == Tile.SIDE.MOUNTAIN_RANGE_OUT) ? '7' : ' ';
            default -> ' ';
        };

        c[9] = switch (t.eastSide) {
            case RIVER -> '═';
            case MOUNTAIN -> '(';
            case MOUNTAIN_RANGE_IN ->
                (t.northSide == Tile.SIDE.MOUNTAIN_RANGE_OUT) ? 'L'
                        : (t.southSide == Tile.SIDE.MOUNTAIN_RANGE_OUT) ? 'r' : ' ';
            default -> ' ';
        };
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Board))
            return false;
        Board b = (Board) o;
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                if (this.originalTiles[r][c] != b.originalTiles[r][c] ||
                        this.tileRotations[r][c] != b.tileRotations[r][c])
                    return false;
            }
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                hash = 31 * hash + (originalTiles[r][c] != null ? originalTiles[r][c].hashCode() : 0);
                hash = 31 * hash + tileRotations[r][c];
            }
        }
        return hash;
    }

}
