class Tile {
    enum SIDE {
        EMPTY,
        RIVER,
        MOUNTAIN,
        MOUNTAIN_RANGE_IN,
        MOUNTAIN_RANGE_OUT
    };

    SIDE northSide;
    SIDE eastSide;
    SIDE southSide;
    SIDE westSide;

    public Tile(SIDE north, SIDE east, SIDE south, SIDE west) {
        this.northSide = north;
        this.eastSide = east;
        this.southSide = south;
        this.westSide = west;
    }

    public void clockWiseRotate(int rotations) {
        rotations = rotations % 4;
        for (int i = 0; i < rotations; i++) {
            SIDE[] newSides = new SIDE[4];
            newSides[0] = this.westSide; // left → top
            newSides[1] = this.northSide; // top → right
            newSides[2] = this.eastSide; // right → bottom
            newSides[3] = this.southSide; // bottom → left
            this.northSide = newSides[0];
            this.eastSide = newSides[1];
            this.southSide = newSides[2];
            this.westSide = newSides[3];
        }
    }

    @Override
    public Tile clone() {
        return new Tile(this.northSide, this.eastSide, this.southSide, this.westSide);
    }

}
