package server;

import environment.Direction;
import game.Game;

public class ClientMessageSender extends Thread{
	
	private Client c;

	
	public ClientMessageSender(Client c) {
		this.c = c;
	}
	
	private Direction getDirection() throws InterruptedException {
		return c.getDirection();
	}

	
	@Override
	public void run() {
		while(!isInterrupted()) {
			Direction d;
			try {
				d = getDirection();
				while(d == null) {
					Thread.sleep(Game.REFRESH_INTERVAL);
					d = getDirection();
				}
				c.sendMessage(d);
			} catch (InterruptedException e) {
				return;
			}
		}
	}
}
