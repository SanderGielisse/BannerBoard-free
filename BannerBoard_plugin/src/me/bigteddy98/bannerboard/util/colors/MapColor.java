package me.bigteddy98.bannerboard.util.colors;

public class MapColor {

	private final int id;
	private final int red;
	private final int green;
	private final int blue;

	private static int idCounter = 4;

	public MapColor(int red, int green, int blue) {
		this(idCounter++, red, green, blue);
	}

	public MapColor(int id, int red, int green, int blue) {
		this.id = id;
		this.red = red;
		this.green = green;
		this.blue = blue;
	}

	public byte getId() {
		return (byte) (this.id < 128 ? //
				this.id : //
				-256 + this.id);
	}

	public MapColor(int id, double red, double green, double blue) {
		this(id, (int) red, (int) green, (int) blue);
	}

	@Override
	public String toString() {
		return "MapColor [id=" + id + ", red=" + red + ", green=" + green + ", blue=" + blue + "]";
	}

	public int getRed() {
		return red;
	}

	public int getGreen() {
		return green;
	}

	public int getBlue() {
		return blue;
	}

	// TODO: Keep or remove? It's not used.
	public int getValue(int dimension) {
		if (dimension == 0)
			return this.red;
		if (dimension == 1)
			return this.green;
		if (dimension == 2)
			return this.blue;
		throw new RuntimeException("Unknown dimension " + dimension);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + blue;
		result = prime * result + green;
		result = prime * result + id;
		result = prime * result + red;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null)
			return false;
		if (this == obj)
			return true;
		if (getClass() != obj.getClass())
			return false;
		
		MapColor other = (MapColor) obj;
		return red == other.red &&
				green == other.green &&
				blue == other.blue &&
				id == other.id;
	}
}
