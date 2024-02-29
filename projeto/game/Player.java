package game;

import java.io.Serializable;

import environment.Cell;
import environment.Coordinate;
import environment.Direction;
import resolvers.Resolver;

/**
 * Represents a player.
 * @author luismota
 *
 */
public abstract class Player extends Thread implements Serializable{

	protected transient Game game;

	private int id;

	private  byte currentStrength;
	protected  byte originalStrength;
	
	private boolean isDead = false;
	
	private transient CountDownLatch latch;
	
	public Player(int id, Game game, CountDownLatch latch) {
		super();
		this.latch = latch;
		this.id = id;
		this.game=game;
		byte s = getInitialStrenght();
		currentStrength = s;
		originalStrength = s;
	}

	// Retorna a cell onde o player se encontra
	public Cell getCurrentCell() {
		return game.getPlayerCell(this);
	}
	
	public byte getCurrentStrength() {
		return currentStrength;
	}

	public int getIdentification() {
		return id;
	}

	
	@Override
	public String toString() {
		return "Player [id=" + id + ", currentStrength=" + currentStrength + ", getCurrentCell()=" + getCurrentCell()
		+ "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + id;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Player other = (Player) obj;
		if (id != other.id)
			return false;
		return true;
	}
	
	
	//Indica se um player está morto
	public boolean isDead() {
		return isDead;
	}
	
	//Mata um player
	public void kill() {
		isDead = true;
		currentStrength = 0;
	}
	
	//Termina um player devido ao fim de um jogo
	public void end() {
		isDead = true;
	}
	
	
	//Adiciona pontos ao player
	public void addStrength(byte s) {
		currentStrength = (byte)Math.min(currentStrength+s, 10);
	}
	
	
	public abstract boolean isHumanPlayer();
	
	//Calcula a força inicial do player
	public abstract byte getInitialStrenght();

	@Override
	public void run() {
		try {
			game.addPlayerToGame(this);
		} catch (InterruptedException e) {	
			e.printStackTrace();
			return;	}
		if(!isHumanPlayer()) {
			try {
				Thread.sleep(10000);
			} catch (InterruptedException e) {
				return;
			}
		}
		while(!isDead) {
			try {
				move();
				if(!isHumanPlayer() && !isDead)
					Thread.sleep(Game.REFRESH_INTERVAL * originalStrength);
			} catch (InterruptedException e) {
				break;
			}
		}
		if(currentStrength == 10) latch.countDown();
	}
	
	
	//Movimento de um player
	private void move() {
		Cell c  = getCurrentCell();
		Coordinate newCoordinate;
		while(true) {
			Direction direction = getDirection();
			if(direction == null)
				continue;
			newCoordinate = c.getPosition().translate(direction.getVector());
			if(game.validPosition(newCoordinate))
				break;
		}
		if(isDead) return;
		try {
			realMove(newCoordinate);
		} catch (InterruptedException e) {
			if(isDead())
				return;
			move();
			return;
		}
	}
	
	//Retorna uma direção para cada movimento
	public abstract Direction getDirection();
	
	public abstract void realMove(Coordinate newCoordinate) throws InterruptedException;
	
	
}
