package game;

import environment.Coordinate;
import environment.Direction;
import resolvers.Resolver;

public class AutoPlayer extends Player{
	
	private boolean isStuck = false;

	public AutoPlayer(int id, Game game, CountDownLatch latch) {
		super(id, game, latch);
	}

	@Override
	public boolean isHumanPlayer() {
		return false;
	}
	
	//Muda o valor de isStcuck
	public void changeIsStuck() {
		isStuck = !isStuck; 
	}
		
	//Indica se um player está bloqueado num movimento
	public boolean isStuck() {
		return isStuck;
	}

	@Override
	public Direction getDirection() {
		return Direction.randomDirection();
	}
	
	public byte getInitialStrenght() {
		double d = Math.random();
		if(d<0.3333) return (byte)1;
		else if(d<0.6666) return (byte)2;
		else return (byte)3;
	}

	@Override
	public void realMove(Coordinate newCoordinate) throws InterruptedException {
		Resolver r  = new Resolver(this);
		r.start();
		game.getCell(newCoordinate).move(this);
		r.interrupt();
	}

}
