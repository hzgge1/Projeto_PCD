package environment;

import java.io.Serializable;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import game.AutoPlayer;
import game.Game;
import game.Player;

public class Cell implements Serializable, Comparable{
	
	private transient Coordinate position;
	private transient Game game;
	private Player player=null;
	
	private transient Lock lock = new ReentrantLock();
	private transient Condition stuck = lock.newCondition();
	private transient Condition stop = lock.newCondition();
 	
	public Cell(Coordinate position,Game g) {
		super();
		this.position = position;
		this.game=g;
	}
	
	public Cell(Coordinate p) {
		super();
		this.position = p;
	}

	public Coordinate getPosition() {
		return position;
	}

	public boolean isOcupied() {
		return player!=null;
	}


	public Player getPlayer() {
		return player;
	}
	
	public Lock getLock() {
		return lock;
	}

	// Should not be used like this in the initial state: cell might be occupied, must coordinate this operation
	//Coloca o player na cell na colocação inicial
	public boolean setPlayer(Player player) throws InterruptedException {
		lock.lock();
		try {
			while(getPlayer() != null) {
				System.err.println("Position:" + toString() + ", player in position: " + getPlayer().toString() + ", player stuck: " + player.toString());
				if(!stuck.await(Game.MAX_WAITING_TIME_FOR_MOVE, TimeUnit.MILLISECONDS)) return false; 
			}
			this.player = player;
			return true;
		}finally {
			lock.unlock();
		}
	}
	
	//Atualiza a cell após movimento do player
	public void update(Player player) {
		lock.lock();
		try {
			this.player = player;
			if(player == null)
				stuck.signalAll();
		}finally {
			lock.unlock();
		}
	}
	
	//Decide o que fazer quando um player tenta ir para uma nova cell
	public void move(Player player) throws InterruptedException {
		Cell c = player.getCurrentCell();
		if(compareTo(c) < 0) {
			lock.lock();
			c.getLock().lock();
		}else {
			c.getLock().lock();
			lock.lock();
		}
		try {
			if(isOcupied()) {
				while(getPlayer().isDead()) {
					if(Thread.currentThread() instanceof AutoPlayer) {
						((AutoPlayer) player).changeIsStuck();
						stop.await();
					}else
						return;
				}
				battleAndKill(player, getPlayer());
			}else {
				player.getCurrentCell().update(null);
				update(player);
			}
			game.notifyChange();
		}finally {
			if(compareTo(c) < 0) {
				c.getLock().unlock();
				lock.unlock();
			}else {
				lock.unlock();
				c.getLock().unlock();
			}
		}
	}
	
	
	//Decide que player morre num confronto
	private void battleAndKill(Player player1, Player player2) {
		if(player1.getCurrentStrength()>player2.getCurrentStrength())
			playerToKill(player1, player2);
		else if(player1.getCurrentStrength()<player2.getCurrentStrength())
			playerToKill(player2, player1);
		else {
			double d = Math.random();
			if(d<0.5) playerToKill(player1, player2);
			else playerToKill(player2, player1);
		}
	}
	
	//Mata o player no confronto e adiciona pontos ao player que não morreu
	private void playerToKill(Player player1, Player player2) {
		player1.addStrength(player2.getCurrentStrength());
		if(player1.getCurrentStrength() == 10) player1.end();
		player2.kill();
		interruptPlayers(player1, player2);
	}

	private void interruptPlayers(Player player1, Player player2) {
		if(Thread.currentThread().equals(player1)) 
			player2.interrupt();
		else 
			if(player1.getCurrentStrength() == 10) player1.interrupt();
		
	}

	@Override
	public int compareTo(Object c) {
		if(c == null || !(c instanceof Cell) )
			throw new IllegalArgumentException("Erro na comparação de cells");
		return hashCode() - c.hashCode();
	}
	
	
	
	

}
