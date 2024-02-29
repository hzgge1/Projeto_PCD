package environment;

public enum Direction {
	UP(0,-1),DOWN(0,1),LEFT(-1,0),RIGHT(1,0);
	private Coordinate vector;
	Direction(int x, int y) {
		vector=new Coordinate(x, y);
	}
	public Coordinate getVector() {
		return vector;
	}
	
	
	//Retorna uma direção random
	public static Direction randomDirection() {
		double d = Math.random();
		if(d<0.25) return Direction.UP;
		else if(d<0.5) return Direction.DOWN;
		else if(d<0.75) return Direction.LEFT;
		else return Direction.RIGHT;
	}
	
	//Retorna uma direção dada a string correspondente
	public static Direction getDirection(String s) {
		if(Direction.UP.name().equals(s))
			return Direction.UP;
		else if(Direction.DOWN.name().equals(s))
			return Direction.DOWN;
		else if(Direction.RIGHT.name().equals(s))
			return Direction.RIGHT;
		else if(Direction.LEFT.name().equals(s))
			return Direction.LEFT;
		else 
			return null;
	}
}
