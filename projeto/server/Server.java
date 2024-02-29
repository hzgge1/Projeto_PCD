package server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import game.CountDownLatch;
import game.Game;
import game.PhoneyHumanPlayer;

public class Server extends Thread{
	
	public static final int PORTO = 8080;
	
	private Game game;
	private CountDownLatch latch;
	
	public Server(Game game, CountDownLatch latch) {
		this.game = game;
		this.latch = latch;
	}
	
	@Override
	public void run() {
		ServerSocket ss = null;
		ServerThread sender = null;
		try {
			ss = new ServerSocket(PORTO);
			sender = new ServerThread();
			sender.start();
			while(!isInterrupted()) {
				Socket s = ss.accept();
				PhoneyHumanPlayer p = new PhoneyHumanPlayer(game.getPlayerId(), game, latch, s);
				sender.addPlayer(p);
				p.start();
			}
			end(sender);
			ss.close();
		} catch (IOException | InterruptedException e) {
			e.printStackTrace();
			try {
				end(sender);
				ss.close();
			} catch (IOException | InterruptedException e1) {
				e1.printStackTrace();
				return;
			}
			return;
		}
	}
	
	//Interrompe a serverthread associada e espera que esta termine para fechar o serverscoket;
	private void end(ServerThread s) throws InterruptedException {
		if(s != null) {
			s.interrupt();
			s.join();
		}
	}


	private class ServerThread extends Thread{
		
		private ArrayList<PhoneyHumanPlayer> players = new ArrayList<>();
		
		
		//Adiciona um novo PhoneyHumanPlayer à lista de Players cujos clientes associados devem ser contactados
		public synchronized void addPlayer(PhoneyHumanPlayer p) {
			players.add(p);
		}
		
		
		//Envia uma ultima mensagem os clientes a indicar que o jogo acabou e interrompe os PhoneyHumanPlayers
		private void sendMessagesAndInterrupt() throws IOException {
			for(PhoneyHumanPlayer p : players) {
				p.getOutputChannel().writeObject(new ServerMessage(game.getBoard(),p.isDead(),game.isGameOver()));
				p.end();
				p.interrupt();
			}
		}
		
		//Remove um player da lista de players
		private synchronized void removePlayer(PhoneyHumanPlayer player) {
			players.remove(player);
		}
		
		//Retorna a lista de players atual
		private synchronized List<PhoneyHumanPlayer> getListPlayers(){
			return players;
		}
		
		
		//Obtem dlista de players atuais e envia mensagens para os mesmos
		private void sendMessages() {
			List<PhoneyHumanPlayer> aux = getListPlayers();
			Iterator it = aux.iterator();
			while(it.hasNext()) {
				PhoneyHumanPlayer p = (PhoneyHumanPlayer) it.next();
				try {
					p.getOutputChannel().reset();
					p.getOutputChannel().writeObject(new ServerMessage(game.getBoard(), p.isDead(), game.isGameOver()));
				} catch (IOException e) {
					p.kill();
					p.interrupt();
					it.remove();
				}
			}
		}
		
		
		//Envia mensagens finais e espera que os players terminem, garantindo que os sockets estejam fechados e que o serversocket possa ser fechado
		private void end() {
			try {
				sendMessagesAndInterrupt();
				for(PhoneyHumanPlayer p : players)
					p.join();
			} catch (IOException | InterruptedException e) {
				return;
			}
		}
		
		
		@Override
		public void run() {
			while(!isInterrupted()) {
				try {
					sendMessages();
					Thread.sleep(Game.REFRESH_INTERVAL);
				} catch (InterruptedException e) {
					end();
					return;
				}
			}
			end();
		}
		
	}
	
	

}
